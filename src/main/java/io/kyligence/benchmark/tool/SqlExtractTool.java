package io.kyligence.benchmark.tool;

import au.com.bytecode.opencsv.CSVWriter;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.kyligence.benchmark.entity.QueryRequest;
import io.kyligence.benchmark.entity.QueryResponse;
import io.kyligence.benchmark.exception.HttpRequestException;
import io.kyligence.benchmark.task.QueryTask;
import io.kyligence.benchmark.utils.HttpUtil;
import io.kyligence.benchmark.utils.ProgressBarUtil;
import org.checkerframework.checker.units.qual.C;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * a tool that can extract sql ---> csv
 * such as txt with only sql
 */
public class SqlExtractTool {

    private static ThreadPoolExecutor executor;
    private static ReentrantLock writeLock = new ReentrantLock(true);
    private static CountDownLatch latch = null;
    private static Integer total = 0;
    private static Integer success = 0;
    private static Integer failed = 0;

    public static void main(String[] args) {
        String inputFilePath = args[0];
        String outputFilePath = args[1];
        String keNode = args[2];
        String project = args[3];
        String userName = args[4];
        String passwd = args[5];
        System.out.println(Arrays.toString(args));
        executor = new ThreadPoolExecutor(10, 10, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100000, true));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // parse txt
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath))) {
            String[] header = new String[]{"query_id", "sql_pattern", "duration", "server", "engine_type",
                    "query_time", "project_name"};
            writer.writeNext(header);
            for (String sql = reader.readLine(); !StrUtil.isEmptyIfStr(sql); sql = reader.readLine()) {
                System.out.print("\r" + "已解析 " + (++total) + "条SQL");
                String finalSql = sql;
                executor.submit(() -> {
                    // wait latch
                    while (null == latch) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // sendRequest
                    QueryResponse respons = sendQueryRequset(finalSql, keNode, project, userName, passwd);
                    if (null == respons || respons.isException() || !"NATIVE".equalsIgnoreCase(respons.getEngineType())) {
                        failed++;
                        latch.countDown();
                        return;
                    }
                    // 并发控制写文件
                    writeLock.lock();
                    try {
                        String[] data = new String[]{
                                respons.getQueryId(), finalSql, String.valueOf(respons.getDuration()), keNode,
                                respons.getEngineType(), String.valueOf(System.currentTimeMillis()), project
                        };
                        writer.writeNext(data);
                        writer.flush();
                        success++;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                        writeLock.unlock();
                    }
                });
            }
            System.out.print("\r" + "已解析 " + total + "条SQL");
            System.out.println("\n");
            latch = new CountDownLatch(total);
            while (latch.getCount() > 0) {
                ProgressBarUtil.printProgressBar(String.format(" extracting ... - success:%d", success),
                        (int) Math.ceil((success + failed) * 100 / total), success + failed, total);
                Thread.sleep(500);
            }
            ProgressBarUtil.printProgressBar(String.format(" extracting ... - success:%d", success),
                    (int) Math.ceil((success + failed) * 100 / total), success + failed, total);
            System.out.println("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * 判断sql是否能够命中模型
     */
    public static QueryResponse sendQueryRequset(String sql, String keNode, String project, String userName, String passwd) {
        QueryResponse response = null;
        QueryRequest request = new QueryRequest();
        request.setSql(sql);
        request.setQueryId(UUID.randomUUID().toString());
        request.setDry_run_mode(true);
        request.setProject(project);
        Map<String, String> headers = Maps.newHashMap();
        String authenticationStr = String.format("%s:%s", userName, passwd);
        headers.put("Authorization", String.format("Basic %s", new String(Base64.getEncoder().encode(authenticationStr.getBytes()))));
        try {
            String result = HttpUtil.httpsPost(String.format("http://%s%s", keNode, QueryTask.QUERY_API_URL), JSONObject.toJSONString(request), headers);
            response = JSONObject.toJavaObject(JSONObject.parseObject(result), QueryResponse.class);
        } catch (HttpRequestException e) {
//            log.error("query with error",e);
        } finally {
            return response;
        }
    }

}
