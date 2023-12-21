package io.kyligence.benchmark.service;

import au.com.bytecode.opencsv.CSVWriter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.google.common.collect.Lists;
import io.kyligence.benchmark.BenchmarkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

@Service
@Slf4j
public class CsvExportService implements ExportService {

    @Autowired
    MetricsCollector metricsCollector;
    @Autowired
    BenchmarkConfig config;

    private StringBuilder reportInfo;

    @Override
    public void export() {
        long timeStamp = System.currentTimeMillis();
        File file = new File(config.getREPORT_OUTPUT_DIR() + File.separator + "benchmark-report-" + timeStamp + ".csv");

        log.info("[ REPORT ] ==========\twriting metrics info to  csv file ==========\t");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(getTotalMetrics(timeStamp));
//            csvWriter.writeAll(getRoundsMetrics(timeStamp));
            csvWriter.flush();
        } catch (Exception e) {
            log.error(" write csv file err ", e);
        }

    }

    public String[] getTotalMetrics(long timeStamp) {
        Snapshot snapshot = metricsCollector.getTotalHistogram().getSnapshot();
        Histogram totalHistogram = metricsCollector.getTotalHistogram();
        String[] strs = new String[40];
        ArrayList<String> list = Lists.newArrayList();
        list.add(String.valueOf(timeStamp));
        list.add(String.valueOf(timeStamp));
        list.add("total");
        list.add(String.valueOf(config.getROUNDS()));
        list.add(String.valueOf(totalHistogram.getCount()));
        list.add(String.valueOf(metricsCollector.getTotalSuccess()));
        list.add(String.valueOf(metricsCollector.getTotalFailed()));
        list.add(String.valueOf(totalHistogram.getSnapshot().getMean()));
        list.add(String.valueOf(totalHistogram.getSnapshot().getMax()));
        list.add(String.valueOf(metricsCollector.getMaxQueryId()));
        metricsCollector.getStepHistogramMap().forEach((k, v) -> {
            list.add(String.valueOf(v.getSnapshot().getMean()));
        });
        list.toArray(strs);
        return strs;
    }
}
