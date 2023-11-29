package io.kyligence.benchmark.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public abstract class AbstractMetricsCollector {
    private String name;

    private MetricRegistry registry;
    private Histogram histogram;
    private Timer timer;

    @PostConstruct
    public void init() {
        this.registry = new MetricRegistry();
        this.histogram = registry.histogram(name + ".histogram");
        this.timer = registry.timer(name + ".timer");
    }

    void collectHisto(long value) {
        this.histogram.update(value);
    }

    void collectTimer(long duration, TimeUnit timeUnit){
        this.timer.update(duration, timeUnit);
    }
}
