package io.kyligence.benchmark.service;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.kyligence.benchmark.BenchmarkConfig;
import io.kyligence.benchmark.entity.QueryRequest;
import io.kyligence.benchmark.entity.QueryResponse;
import io.kyligence.benchmark.entity.RoundMetricsSnapshot;
import io.kyligence.benchmark.entity.SQLResponseTrace;
import io.kyligence.benchmark.enums.QuerySpanEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MetricsCollector {

    @Autowired
    private BenchmarkConfig benchmarkConfig;

    private Integer round;
    /**
     * total histograms
     */
    private MetricRegistry registry;
    @Getter
    private Histogram totalHistogram;
    @Getter
    private Map<QuerySpanEnum, Histogram> stepHistogramMap;
    /**
     * total slowest query info
     */
    @Getter
    private String maxQueryId;
    @Getter
    private String maxQuerySql;
    @Getter
    private List<SQLResponseTrace> maxQueryTraceList;
    @Getter
    private AtomicInteger totalSuccess;
    @Getter
    private AtomicInteger totalFailed;
    @Getter
    private Set<String> failedSets;


    /**
     * round histogram， 每一轮开启新的收集
     */
    private Histogram roundHistogram;
    private Map<QuerySpanEnum, Histogram> roundStepHistogramMap;
    /**
     * round slowest query info
     */
    private String maxRoundQueryId;
    private String maxRoundQuerySql;
    private AtomicInteger roundSuccess;
    private AtomicInteger roundFailed;
    private List<SQLResponseTrace> maxRoundQueryTraceList;


    /**
     * metrics snapshots 存储每一轮的metric快照
     */
    @Getter
    private Map<Integer, RoundMetricsSnapshot> roundSnapshotMap;

    @PostConstruct
    public void init() {
        MetricRegistry.name("ke-benchmark");
        this.registry = new MetricRegistry();
        this.round = 0;
        this.totalSuccess = new AtomicInteger();
        this.totalFailed = new AtomicInteger();
        this.failedSets = Sets.newConcurrentHashSet();
        totalHistogram = registry.histogram("total.histogram");
        stepHistogramMap = Maps.newTreeMap((a1, a2) -> {
            return a1.getSequence() - a2.getSequence();
        });
        roundStepHistogramMap = Maps.newTreeMap((a1, a2) -> {
            return a1.getSequence() - a2.getSequence();
        });
        for (QuerySpanEnum step : QuerySpanEnum.values()) {
            if (step.isNeedMetric()) {
                String histogramName = String.format("total.%s.histogram", step.getName());
                stepHistogramMap.put(step, registry.histogram(histogramName));
            }
        }
        roundSnapshotMap = Maps.newHashMap();
    }

    /**
     * new round, save the snapshot
     */
    public void startRound() {
        round++;
        roundHistogram = registry.histogram("round." + round + ".histogram");
        for (QuerySpanEnum step : QuerySpanEnum.values()) {
            if (step.isNeedMetric()) {
                String histogramName = String.format("round.%d.%s.histogram", round, step.getName());
                roundStepHistogramMap.put(step, registry.histogram(histogramName));
            }
        }
        this.maxRoundQueryTraceList = null;
        this.maxRoundQueryId = null;
        this.maxRoundQuerySql = null;
        this.roundSuccess = new AtomicInteger();
        this.roundFailed = new AtomicInteger();
    }

    public void endRound() {
        // save snapShots
        RoundMetricsSnapshot roundMetricsSnapshot = new RoundMetricsSnapshot(round);
        roundMetricsSnapshot.setRoundSnapshot(roundHistogram.getSnapshot());
        roundMetricsSnapshot.setMaxQueryId(this.maxRoundQueryId);
        roundMetricsSnapshot.setMaxQueryTraceList(this.maxRoundQueryTraceList);
        roundMetricsSnapshot.setMaxQuerySql(this.maxRoundQuerySql);
        roundMetricsSnapshot.setSuccessCnt(this.roundSuccess.get());
        roundMetricsSnapshot.setFailedCnt(this.roundFailed.get());
        roundStepHistogramMap.forEach((k, v) -> {
            roundMetricsSnapshot.addStepSnapshot(k, v.getSnapshot());
        });
        roundSnapshotMap.put(round, roundMetricsSnapshot);
    }

    public void collect(QueryRequest request, QueryResponse response) {
        Long duration = response.getDuration();
        List<SQLResponseTrace> metricsList = response.getTraces();
        // success & failed count
        if (response.isException()) {
            this.totalFailed.incrementAndGet();
            this.roundFailed.incrementAndGet();
            this.failedSets.add(request.getQueryId());
            // 不计入统计时间
            return;
        } else {
            this.totalSuccess.incrementAndGet();
            this.roundSuccess.incrementAndGet();
        }

        // * update total info
        totalHistogram.update(duration);
        if (totalHistogram.getSnapshot().getMax() == duration) {
            this.maxQueryId = request.getQueryId();
            this.maxQueryTraceList = metricsList;
            this.maxQuerySql = request.getSql();
        }
        Map<String, SQLResponseTrace> metricsMap = metricsList.stream()
                .collect(Collectors.toMap(m -> m.getName(), p -> p, (a, b) -> a));
        // ! 没有经历的Step 也收集下信息，方便后面详情的时候可以清楚的看到那些Step没有经历
        stepHistogramMap.forEach((k, v) -> {
            val metrics = metricsMap.get(k.getName());
            if (metrics != null) {
                v.update(metrics.getDuration());
            } else {
                v.update(0);
            }
        });

        // * update round metrics
        roundHistogram.update(duration);
        if (roundHistogram.getSnapshot().getMax() == duration) {
            this.maxRoundQueryId = request.getQueryId();
            this.maxRoundQueryTraceList = metricsList;
            this.maxRoundQuerySql = request.getSql();
        }
        roundStepHistogramMap.forEach((k, v) -> {
            val metrics = metricsMap.get(k.getName());
            if (metrics != null) {
                v.update(metrics.getDuration());
            } else {
                v.update(0);
            }
        });

        // check if step not in QuerySpanEnum
        metricsList.forEach(metrics -> {
            if (!QuerySpanEnum.contains(metrics.getName())) {
                log.error(" can't find the query step Metrics Collector, name:{}", metrics.getName());
            }
        });
    }

    public void consoleReport() {
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS)
                .convertRatesTo(TimeUnit.MILLISECONDS).build();
//        System.out.println(JSONObject.toJSONString(this.totalHistogram.getSnapshot()));
    }
}
