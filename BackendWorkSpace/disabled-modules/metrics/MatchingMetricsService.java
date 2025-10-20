package com.samjhadoo.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MatchingMetricsService {
    
    private final Counter matchRequestsCounter;
    private final Counter matchSuccessCounter;
    private final Counter matchFailureCounter;
    private final Timer matchDurationTimer;
    
    public MatchingMetricsService(MeterRegistry registry) {
        this.matchRequestsCounter = Counter.builder("ai.matching.requests")
                .description("Total number of match requests")
                .tag("type", "all")
                .register(registry);
                
        this.matchSuccessCounter = Counter.builder("ai.matching.success")
                .description("Number of successful matches")
                .tag("type", "success")
                .register(registry);
                
        this.matchFailureCounter = Counter.builder("ai.matching.failures")
                .description("Number of failed matches")
                .tag("type", "failure")
                .register(registry);
                
        this.matchDurationTimer = Timer.builder("ai.matching.duration")
                .description("Time taken for matching operations")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
    
    public void incrementMatchRequests() {
        matchRequestsCounter.increment();
    }
    
    public void recordMatchSuccess() {
        matchSuccessCounter.increment();
    }
    
    public void recordMatchFailure() {
        matchFailureCounter.increment();
    }
    
    public Timer.Sample startTimer() {
        return Timer.start();
    }
    
    public void recordDuration(Timer.Sample sample, String... tags) {
        if (sample != null) {
            sample.stop(matchDurationTimer);
        }
    }
    
    public void recordDuration(Timer.Sample sample, long duration, TimeUnit unit) {
        if (sample != null) {
            matchDurationTimer.record(duration, unit);
        }
    }
}
