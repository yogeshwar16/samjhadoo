package com.samjhadoo.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config().commonTags(
                "application", "samjhadoo-backend",
                "region", System.getenv().getOrDefault("REGION", "local")
            );
        };
    }

    @Bean
    public CompositeMeterRegistry compositeMeterRegistry(PrometheusMeterRegistry prometheusRegistry) {
        CompositeMeterRegistry registry = new CompositeMeterRegistry();
        registry.add(prometheusRegistry);
        return registry;
    }
}
