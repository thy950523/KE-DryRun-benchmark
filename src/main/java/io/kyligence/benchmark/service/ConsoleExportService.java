package io.kyligence.benchmark.service;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import io.kyligence.benchmark.BenchmarkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        String overviewInfo = String.format("\n-- Overview --") +
                String.format("\n%-15s %d", "Queries_Count：", snapshot.getValues().length) +
                String.format(" \n%-15s %d", "Concurrency：", config.getCONCURRENCY()) +
                String.format(" \n%-15s %s", "KE_Node：", config.getKYLIN_QUERY_NODE());
        System.out.println(colorfulText(overviewInfo, "32"));

        String totalInfo = String.format("\n\n-- Total_Info --") +
                String.format("\n%-15s %d", "Total_Count：", totalHistogram.getCount()) +
                String.format(" \n%-15s %.2f", "Avg_Duration：", snapshot.getMean()) +
                String.format(" \n%-15s %d", "Min_Duration：", snapshot.getMin()) +
                String.format(" \n%-15s %d", "Max_Duration：", snapshot.getMax());
        System.out.println(colorfulText(totalInfo, "32"));
    }

    private void processRoundMetrics() {

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
