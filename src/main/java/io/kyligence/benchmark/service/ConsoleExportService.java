package io.kyligence.benchmark.service;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import io.kyligence.benchmark.BenchmarkConfig;
import io.kyligence.benchmark.entity.RoundMetricsSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class ConsoleExportService implements ExportService {

    @Autowired
    MetricsCollector metricsCollector;
    @Autowired
    BenchmarkConfig config;

    @Override
    public void export() {
        log.info("[ REPORT ] ==========\tbenchmark report is as follow : ==========\t");
        processTotalMetrics();
        processRoundMetrics();
        processDetailMetrics();
    }

    private void processTotalMetrics() {
        Snapshot snapshot = metricsCollector.getTotalHistogram().getSnapshot();
        Histogram totalHistogram = metricsCollector.getTotalHistogram();
        System.out.println(colorfulText("========== Dry-Run-Benchmark-Report ==========", "32"));
        String overviewInfo = String.format("\n***** Overview *****") +
                String.format("\n%-15s %d", "Queries_Count：", snapshot.getValues().length) +
                String.format(" \n%-15s %d", "Concurrency：", config.getCONCURRENCY()) +
                String.format(" \n%-15s %s", "KE_Node：", config.getKYLIN_QUERY_NODE());
        System.out.println(colorfulText(overviewInfo, "32"));
        // * overview
        String totalInfo = String.format("\n\n===== Total_Info =====") +
                String.format("\n%-15s %d", "Total_Count：", totalHistogram.getCount()) +
                String.format(" \n%-15s %.2f", "Avg_Duration：", snapshot.getMean()) +
                String.format(" \n%-15s %d", "Min_Duration：", snapshot.getMin()) +
                String.format(" \n%-15s %d", "Max_Duration：", snapshot.getMax());
        System.out.println(colorfulText(totalInfo, "32"));
        // * step overview
        Map<String, Histogram> stepHistogramMap = metricsCollector.getStepHistogramMap();
        System.out.println(colorfulText("\n\n-- Steps_AVG_Info --", "32"));
        stepHistogramMap.forEach((k,v)->{
            String info = String.format("%-30s %.2f", k, v.getSnapshot().getMean());
            System.out.println(colorfulText(info,"32"));
        });


    }

    private void processRoundMetrics() {
        Map<Integer, RoundMetricsSnapshot> roundSnapshotMap = metricsCollector.getRoundSnapshotMap();
        String roundInfo = String.format("\n\n===== Round_Info =====");
        System.out.println(colorfulText(roundInfo, "35"));
        roundSnapshotMap.forEach((k, v) -> {
            Snapshot snapshot = v.getRoundSnapshot();
            Map<String, Snapshot> stepSnapshotMap = v.getStepSnapshotMap();
            String totalInfo = String.format("----------------------------------------" +
                    "\n\n\n--- Round%d ---", k) +
                    String.format("\n%-15s %d", "Total_Count：", snapshot.size()) +
                    String.format(" \n%-15s %.2f", "Avg_Duration：", snapshot.getMean()) +
                    String.format(" \n%-15s %d", "Min_Duration：", snapshot.getMin()) +
                    String.format(" \n%-15s %d\n", "Max_Duration：", snapshot.getMax());
            System.out.println(colorfulText(totalInfo,"35"));

            stepSnapshotMap.forEach((k1,v1)->{
                String info = String.format("%-30s %.2f", k1, v1.getMean());
                System.out.println(colorfulText(info,"35"));
            });
        });


    }

    private void processDetailMetrics() {

    }

    /**
     * 控制台 文本颜色控制，30-37
     *
     * @param color <br>
     *              30m: 黑色<br>
     *              31m: 红色<br>
     *              32m: 绿色<br>
     *              33m: 黄色<br>
     *              34m: 蓝色<br>
     *              35m: 洋红色<br>
     *              36m: 青色<br>
     *              37m: 白色
     */
    private String colorfulText(String text, String color) {
        return String.format("\u001B[%sm%s\u001B[0m", color, text);
    }
}
