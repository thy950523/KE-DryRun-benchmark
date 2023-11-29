package io.kyligence.benchmark.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.codahale.metrics.MetricRegistry;

import io.kyligence.benchmark.entity.Metrics;

@Component
public class MetricCollector {
    private MetricRegistry registry;

    @PostConstruct
    public void init() {
        registry = new MetricRegistry();
        MetricRegistry.name("ke-dry-run-benchmark");
    }

    public void collect(Integer round, String metricId, List<? extends Metrics> metricsList){

    }
}
