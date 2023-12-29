package io.kyligence.benchmark.service;

import au.com.bytecode.opencsv.CSVWriter;
import cn.hutool.core.date.DateUtil;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.google.common.collect.Lists;
import io.kyligence.benchmark.BenchmarkConfig;
import io.kyligence.benchmark.entity.RoundMetricsSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CsvExportService implements ExportService {

    @Autowired
    MetricsCollector metricsCollector;
    @Autowired
    BenchmarkConfig config;

    private String[] header = new String[]{
            "Ke_Node", "Time", "Type", "Rounds", "Concurrency", "Query_Total", "Success", "Failed", "Total_Duration", "Total_Avg", "Max_Duration",
            "Max_Query_Id", "HTTP_RECEPTION", "GET_ACL_INFO", "SQL_TRANSFORMATION", "SQL_PARSE_AND_OPTIMIZE", "MODEL_MATCHING",
            "PREPARE_AND_SUBMIT_JOB", "WAIT_FOR_EXECUTION", "EXECUTION", "FETCH_RESULT", "SPARK_JOB_EXECUTION", "SQL_PUSHDOWN_TRANSFORMATION",
            "HIT_CACHE"
    };

    @Override
    public void export() {
        String endAt = DateUtil.format(metricsCollector.getEndAt(), "YYYY-MM-dd_HH:mm:ss");
        File file = new File(config.getREPORT_OUTPUT_DIR() + File.separator + "benchmark-report-" + endAt + "_total.csv");

        log.info("[ REPORT ] ==========\twriting metrics info to  csv file ==========\t");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(header);
            csvWriter.writeNext(getTotalMetrics(endAt));
            csvWriter.writeAll(getRoundsMetrics(endAt));
            csvWriter.flush();
        } catch (Exception e) {
            log.error(" write csv file err ", e);
        }

    }

    public String[] getTotalMetrics(String timeStamp) {
        Histogram totalHistogram = metricsCollector.getTotalHistogram();
        try {
            totalHistogram.getSnapshot().dump(new FileOutputStream("./total.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String[] strs = new String[header.length];
        ArrayList<String> list = Lists.newArrayList();
        list.add(config.getKYLIN_QUERY_NODE());
        list.add(String.valueOf(timeStamp));
        list.add("Total");
        list.add(String.valueOf(config.getROUNDS()));
        list.add(String.valueOf(config.getCONCURRENCY()));
        list.add(String.valueOf(totalHistogram.getCount()));
        list.add(String.valueOf(metricsCollector.getTotalSuccess()));
        list.add(String.valueOf(metricsCollector.getTotalFailed()));
        list.add(String.valueOf(metricsCollector.getTotalDuration()));
        list.add(String.valueOf(totalHistogram.getSnapshot().getMean()));
        list.add(String.valueOf(totalHistogram.getSnapshot().getMax()));
        list.add(String.valueOf(metricsCollector.getMaxQueryId()));
        metricsCollector.getStepHistogramMap().forEach((k, v) -> {
            list.add(String.valueOf(v.getSnapshot().getMean()));
        });
        list.toArray(strs);
        return strs;
    }

    public List<String[]> getRoundsMetrics(String timeStamp) {
        List<String[]> dataList = Lists.newArrayList();
        Map<Integer, RoundMetricsSnapshot> roundSnapshotMap = metricsCollector.getRoundSnapshotMap();
        roundSnapshotMap.forEach((round, snapshot) -> {
            String[] strs = new String[header.length];
            ArrayList<String> list = Lists.newArrayList();
            list.add(config.getKYLIN_QUERY_NODE());
            list.add(String.valueOf(timeStamp));
            list.add("Round");
            list.add(String.valueOf(round));
            list.add(String.valueOf(config.getCONCURRENCY()));
            list.add(String.valueOf(snapshot.getSuccessCnt() + snapshot.getFailedCnt()));
            list.add(String.valueOf(snapshot.getSuccessCnt()));
            list.add(String.valueOf(snapshot.getFailedCnt()));
            list.add(String.valueOf(snapshot.getDuration()));
            list.add(String.valueOf(snapshot.getRoundSnapshot().getMean()));
            list.add(String.valueOf(snapshot.getRoundSnapshot().getMax()));
            list.add(String.valueOf(snapshot.getMaxQueryId()));
            snapshot.getStepSnapshotMap().forEach((k, v) -> {
                list.add(String.valueOf(v.getMean()));
            });
            list.toArray(strs);
            dataList.add(strs);
        });
        return dataList;
    }
}
