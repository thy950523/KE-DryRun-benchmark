package io.kyligence.benchmark.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;

import io.kyligence.benchmark.BenchmarkConfig;
import io.kyligence.benchmark.entity.Metrics;
import io.kyligence.benchmark.entity.RoundMetricsSnapshot;
import io.kyligence.benchmark.enums.QuerySpanEnum;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MetricsCollector {

    @Autowired
    private BenchmarkConfig benchmarkConfig;

    private Integer round;
    /** total histograms */
    private MetricRegistry registry;
    private MetricRegistry roundRegistry;
    private Histogram totalHistogram;
    private Map<String, Histogram> stepHistogramMap;
    /** round histogram， 每一轮开启新的收集 */
    private Histogram roundHistogram;
    private Map<String, Histogram> roundStepHistogramMap;
    /** metrics snapshots 存储每一轮的metric快照 */
    private Map<Integer, RoundMetricsSnapshot> roundSnapshotMap;


    @PostConstruct
    public void init() {
        MetricRegistry.name("ke-benchmark");
        registry = new MetricRegistry();
        round = 1;
        totalHistogram = registry.histogram("total.histogram");
        roundHistogram = registry.histogram("round." + round + ".histogam");
        stepHistogramMap = Maps.newHashMap();
        roundStepHistogramMap = Maps.newHashMap();
        for (QuerySpanEnum step : QuerySpanEnum.values()) {
            if (step.isNeedMetric()) {
                String histogramName = String.format("total.%s.histogram", step.getName());
                stepHistogramMap.put(step.getName(), registry.histogram(histogramName));
                histogramName = String.format("round.%d.%s.histogram", round, step.getName());
                roundStepHistogramMap.put(step.getName(), registry.histogram(histogramName));
            }
        }
        roundSnapshotMap = Maps.newHashMap();
    }



    /**
     * new round, save the snapshot
     */
    public void endRound() {
        // save snapShots
        RoundMetricsSnapshot roundMetricsSnapshot = new RoundMetricsSnapshot(round);
        roundMetricsSnapshot.setRoundSnapshot(roundHistogram.getSnapshot());
        roundStepHistogramMap.forEach((k, v) -> {
            roundMetricsSnapshot.addStepSnapshot(k, v.getSnapshot());
        });
        roundSnapshotMap.put(round, roundMetricsSnapshot);

        // refresh histograms
        round++;
        roundHistogram = registry.histogram("round." + round + ".histogram");
        for (QuerySpanEnum step : QuerySpanEnum.values()) {
            if (step.isNeedMetric()) {
                String histogramName = String.format("round.%d.%s.histogram", round, step.getName());
                roundStepHistogramMap.put(step.getName(), registry.histogram(histogramName));
            }
        }
    }

    public void collect(Integer round, Long duration, List<? extends Metrics> metricsList) {
        totalHistogram.update(duration);
        roundHistogram.update(duration);
        metricsList.forEach(metrics -> {
            Histogram stepHis = stepHistogramMap.get(metrics.getName());
            Histogram roundStepHis = roundStepHistogramMap.get(metrics.getName());
            if (stepHis != null && roundStepHis != null) {
                stepHis.update(metrics.getDuration());
                roundStepHis.update(metrics.getDuration());
            }else{
                log.error(" can't find the query step Metrics Collector, name:{}", metrics.getName());
            }
        });
    }


    public void consoleReport() {
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                .convertRatesTo(TimeUnit.MILLISECONDS).build();
//        consoleReporter.report();
        CsvReporter csvReporter = CsvReporter.forRegistry(registry).build(new File("/Users/kyligence/Downloads/"));
//        csvReporter.report();
        System.out.println(JSONObject.toJSONString(this.roundSnapshotMap));
    }
}
