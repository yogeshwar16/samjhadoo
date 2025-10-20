package com.samjhadoo.service.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMetricsService {

    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    private final Map<String, Counter> messageCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> messageTimers = new ConcurrentHashMap<>();

    /**
     * Track a new WebSocket connection
     */
    public void incrementConnection(String endpoint) {
        connectionCounts.computeIfAbsent(endpoint, k -> new AtomicInteger(0)).incrementAndGet();
        updateConnectionMetrics();
    }

    /**
     * Track a closed WebSocket connection
     */
    public void decrementConnection(String endpoint) {
        connectionCounts.computeIfPresent(endpoint, (k, v) -> {
            v.decrementAndGet();
            return v.get() <= 0 ? null : v;
        });
        updateConnectionMetrics();
    }

    /**
     * Track an incoming message
     */
    public void trackMessage(String destination, long processingTimeMs) {
        String metricName = "websocket.messages.received" + getMetricSuffix(destination);
        messageCounters
            .computeIfAbsent(metricName, k -> 
                Counter.builder(metricName)
                    .description("Number of messages received on " + destination)
                    .register(meterRegistry)
            )
            .increment();

        String timerName = "websocket.message.processing.time" + getMetricSuffix(destination);
        messageTimers
            .computeIfAbsent(timerName, k -> 
                Timer.builder(timerName)
                    .description("Processing time for messages on " + destination)
                    .register(meterRegistry)
            )
            .record(processingTimeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Log connection statistics periodically
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void logConnectionStats() {
        if (log.isInfoEnabled()) {
            connectionCounts.forEach((endpoint, count) -> 
                log.info("WebSocket connections on {}: {}", endpoint, count.get())
            );
        }
    }

    private void updateConnectionMetrics() {
        int totalConnections = connectionCounts.values().stream()
            .mapToInt(AtomicInteger::get)
            .sum();

        // Update gauge for total connections
        meterRegistry.gauge("websocket.connections.total", totalConnections);
        
        // Update per-endpoint gauges
        connectionCounts.forEach((endpoint, count) -> 
            meterRegistry.gauge("websocket.connections", 
                List.of("endpoint"), 
                endpoint, 
                count,
                AtomicInteger::get
            )
        );
    }

    private String getMetricSuffix(String destination) {
        if (destination == null) {
            return ".unknown";
        }
        
        if (destination.startsWith("/topic/")) {
            return ".topic";
        } else if (destination.startsWith("/queue/")) {
            return ".queue";
        } else if (destination.startsWith("/user/")) {
            return ".user";
        }
        
        return ".other";
    }
}
