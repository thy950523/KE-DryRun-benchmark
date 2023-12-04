package io.kyligence.benchmark;

import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.kyligence.benchmark.entity.QueryHistoryDTO;
import io.kyligence.benchmark.service.ExportService;
import io.kyligence.benchmark.service.MetricsCollector;
import io.kyligence.benchmark.task.QueryTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class BenchmarkRunner implements ApplicationRunner {

    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    BenchmarkConfig benchmarkConfig;

    @Autowired
    MetricsCollector metricCollector;
    @Autowired
    ExportService exportService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("[STATUS]: ==========\tbenchmark started\t==========");
        // * args check
        if (!benchmarkConfig.checkArgs()) {
            System.exit(1);
        }

        int round = 1;
        // * rounds start
        while (round <= benchmarkConfig.ROUNDS) {
            log.info("[RUN]-[NEW_ROUND]：start to run round {}", round);
            metricCollector.startRound();
            // * csv parse
            File csvDir = new File(benchmarkConfig.FILE_DIR);
            for (File csv : csvDir.listFiles()) {
                FileReader fileReader = new FileReader(csv);
                HeaderColumnNameMappingStrategy strategy = new HeaderColumnNameMappingStrategy<>();
                strategy.setType(QueryHistoryDTO.class);
                CsvToBean csvToBean = new CsvToBean<>();
                List<QueryHistoryDTO> queryHistoryDTOList = csvToBean.parse(strategy, fileReader);
                for (QueryHistoryDTO qh : queryHistoryDTOList) {
                    // wrap queryTask
                    executor.submit(new QueryTask(qh, round));
                }
            }

            while (executor.getQueue().size() > 0) {
                log.info("...... current round:{} ,query task remains : {}", round, executor.getQueue().size());
                Thread.sleep(3000);
            }
            // ! 只有在上一轮全部跑完再开始下一轮
            metricCollector.endRound();
            round++;
        }

        log.info("[STATUS]: ==========\tbenchmark finished\t==========");
        exportService.export();

        metricCollector.consoleReport();
        System.exit(0);
        // TODO 可以写个生产者消费者模式
    }

}
