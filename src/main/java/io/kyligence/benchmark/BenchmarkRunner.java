package io.kyligence.benchmark;

import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.kyligence.benchmark.entity.QueryHistoryDTO;
import io.kyligence.benchmark.service.ConsoleExportService;
import io.kyligence.benchmark.service.CsvExportService;
import io.kyligence.benchmark.service.MetricsCollector;
import io.kyligence.benchmark.task.QueryTask;
import io.kyligence.benchmark.utils.ProgressBarUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
    ConsoleExportService consoleExportService;
    @Autowired
    CsvExportService csvExportService;


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
            File csvDir = new File(benchmarkConfig.FILE_DIR);
            log.info("[RUN]-[NEW_ROUND]：start to run round {}", round);
            metricCollector.startRound();
            // * csv parse
            for (File csv : csvDir.listFiles()) {
                FileReader fileReader = new FileReader(csv);
                HeaderColumnNameMappingStrategy strategy = new HeaderColumnNameMappingStrategy<>();
                strategy.setType(QueryHistoryDTO.class);
                CsvToBean csvToBean = new CsvToBean<>();
                List<QueryHistoryDTO> queryHistoryDTOList = csvToBean.parse(strategy, fileReader);
                long currentTotal = queryHistoryDTOList.size();
                CountDownLatch latch = new CountDownLatch(queryHistoryDTOList.size());
                for (QueryHistoryDTO qh : queryHistoryDTOList) {
                    // wrap queryTask
                    executor.submit(new QueryTask(qh, round, latch));
                    ProgressBarUtil.printProgressBar(
                            String.format("Current Project : %-45s, failed: %6d ", csv.getName(),metricCollector.getTotalFailed().get()),
                            (int) Math.ceil((currentTotal - latch.getCount()) * 100 / currentTotal), currentTotal - latch.getCount(), currentTotal);
                }
                // progress bar
                while (latch.getCount() > 0) {
                    ProgressBarUtil.printProgressBar(
                            String.format("Current Project : %-45s, failed: %6d ", csv.getName(),metricCollector.getTotalFailed().get()),
                            (int) Math.ceil((currentTotal - latch.getCount()) * 100 / currentTotal), currentTotal - latch.getCount(), currentTotal);
                    Thread.sleep(500);
                }
                ProgressBarUtil.printProgressBar(
                        String.format("Current Project : %-45s, failed: %6d ", csv.getName(),metricCollector.getTotalFailed().get()),
                        100, currentTotal, currentTotal);
                System.out.println();
                latch.await();
            }
            // ! 只有在上一轮全部跑完再开始下一轮
            metricCollector.endRound();
            round++;
        }
        consoleExportService.export();
        csvExportService.export();
        log.info("[STATUS]: ==========\tbenchmark finished\t==========");
        System.exit(0);
        // TODO 可以写个生产者消费者模式
    }

}
