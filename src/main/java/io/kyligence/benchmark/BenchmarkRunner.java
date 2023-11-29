package io.kyligence.benchmark;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.kyligence.benchmark.entity.QueryHistoryDTO;
import io.kyligence.benchmark.service.MetricsCollector;
import io.kyligence.benchmark.task.QueryTask;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BenchmarkRunner implements ApplicationRunner {

    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    BenchmarkConfig benchmarkConfig;

    @Autowired
    MetricsCollector metricCollector;



    @Value("${file-dir:/Users/kyligence/Documents/105-test_lab/dump/full_2023_11_27_17_23_19/query_history}")
    private String fileDir;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("[STATUS]: ==========\tbenchmark started\t==========");

        // * args check

        // * rounds start
        // only start after the last round finished

        // * csv parse
        File csvDir = new File(fileDir);
        for (File csv : csvDir.listFiles()) {
            FileReader fileReader = new FileReader(csv);
            HeaderColumnNameMappingStrategy strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(QueryHistoryDTO.class);
            CsvToBean csvToBean = new CsvToBean<>();
            List<QueryHistoryDTO> queryHistoryDTOList = csvToBean.parse(strategy, fileReader);
            for (QueryHistoryDTO qh : queryHistoryDTOList) {
                // wrap queryTask
                executor.submit(new QueryTask(qh, 1));

            }
        }

        while (executor.getQueue().size() > 0) {
            log.info("query task remains : {}", executor.getQueue().size());
            Thread.sleep(3000);
        }

        log.info("[STATUS]: ==========\tbenchmark finished\t==========");
        metricCollector.endRound();
        metricCollector.consoleReport();
        System.exit(0);
        // TODO 可以写个生产者消费者模式
    }
}
