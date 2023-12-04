package io.kyligence.benchmark.service;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import io.kyligence.benchmark.BenchmarkConfig;
import io.kyligence.benchmark.entity.RoundMetricsSnapshot;
import io.kyligence.benchmark.enums.QuerySpanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.StringJoiner;

@Service
@Slf4j
public class ConsoleExportService implements ExportService {

    @Autowired
    MetricsCollector metricsCollector;
    @Autowired
    BenchmarkConfig config;

    private StringBuilder reportInfo;

    @Override
    public void export() {
        File file = new File(config.getREPORT_OUTPUT_DIR() + File.separator + "benchmark-report-" + System.currentTimeMillis() + ".txt");
        reportInfo = new StringBuilder();
        log.info("[ REPORT ] ==========\tbenchmark report is as follow : ==========\t");
        processTotalMetrics();
        processRoundMetrics();
        processDetailMetrics();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(reportInfo.toString());
            writer.flush();
            String endString = String.format("\n\n________ %-20s%s _______ \n\n", " Benchmark Report has been stored at :", file.getAbsolutePath());
            System.out.println(colorfulText(endString, "36"));
        } catch (Exception e) {
            log.error(" write report file err ", e);
        }
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
        StringJoiner joiner = new StringJoiner("  ===>  ", "", "");
        metricsCollector.getMaxQueryTraceList().forEach(d -> {
            joiner.add(String.format("[%s] %d ms", d.getName(), d.getDuration()));
        });

        String totalInfo = String.format("\n\n===== Total_Info =====") +
                String.format("\n%-15s %d", "Total_Count：", totalHistogram.getCount()) +
                String.format("\n%-15s %d", "Success：", metricsCollector.getTotalSuccess().get()) +
                String.format("\n%-15s %d", "Failed：", metricsCollector.getTotalFailed().get()) +
                String.format(" \n%-15s %.2f", "Avg_Duration：", snapshot.getMean()) +
                String.format(" \n%-15s %d", "Min_Duration：", snapshot.getMin()) +
                String.format(" \n%-15s %d", "Max_Duration：", snapshot.getMax()) +
                String.format(" \n%-15s %s", "Query_Id：", metricsCollector.getMaxQueryId()) +
                String.format(" \n%-15s %s", "Query_SQL：", metricsCollector.getMaxQuerySql()) +
                String.format(" \n%-15s %s", "Query_Traces：", joiner);

        System.out.println(colorfulText(totalInfo, "32"));

        // * step overview
        Map<QuerySpanEnum, Histogram> stepHistogramMap = metricsCollector.getStepHistogramMap();
        System.out.println(colorfulText("\n\n-- Steps_AVG_Info --", "32"));
        stepHistogramMap.forEach((k, v) -> {
            String info = String.format("%-30s %-5s %-10.2f", k.getName(), v.getSnapshot().getMean() > 0 ? "√" : "×", v.getSnapshot().getMean());
            System.out.println(colorfulText(info, "32"));
        });

    }

    private void processRoundMetrics() {
        Map<Integer, RoundMetricsSnapshot> roundSnapshotMap = metricsCollector.getRoundSnapshotMap();
        String roundInfo = String.format("\n\n===== Round_Info =====");
        System.out.println(colorfulText(roundInfo, "35"));
        roundSnapshotMap.forEach((k, v) -> {
            Snapshot snapshot = v.getRoundSnapshot();
            Map<QuerySpanEnum, Snapshot> stepSnapshotMap = v.getStepSnapshotMap();
            StringJoiner joiner = new StringJoiner("  ===>  ", "", "");
            v.getMaxQueryTraceList().forEach(d -> {
                joiner.add(String.format("[%s] %d ms", d.getName(), d.getDuration()));
            });
            String totalInfo = String.format("----------------------------------------" +
                    "\n\n\n--- Round%d ---", k) +
                    String.format("\n%-15s %d", "Total_Count：", snapshot.size()) +
                    String.format("\n%-15s %d", "Success：", v.getSuccessCnt()) +
                    String.format("\n%-15s %d", "Failed：", v.getFailedCnt()) +
                    String.format(" \n%-15s %.2f", "Avg_Duration：", snapshot.getMean()) +
                    String.format(" \n%-15s %d", "Min_Duration：", snapshot.getMin()) +
                    String.format(" \n%-15s %d", "Max_Duration：", snapshot.getMax()) +
                    String.format(" \n%-15s %s", "Query_Id：", v.getMaxQueryId()) +
                    String.format(" \n%-15s %s", "Query_SQL：", v.getMaxQuerySql()) +
                    String.format(" \n%-15s %s", "Query_Traces：", joiner);
            System.out.println(colorfulText(totalInfo, "35"));
            System.out.println(colorfulText("\n\n-- Steps_AVG_Info --", "35"));
            stepSnapshotMap.forEach((k1, v1) -> {
                String info = String.format("%-30s %-5s %-10.2f", k1.getName(), v1.getMean() > 0 ? "√" : "×", v1.getMean());
                System.out.println(colorfulText(info, "35"));
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
        reportInfo.append(text).append("\n");
        return String.format("\u001B[%sm%s\u001B[0m", color, text);
    }
}
