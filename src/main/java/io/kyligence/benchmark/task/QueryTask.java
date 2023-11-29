package io.kyligence.benchmark.task;

import java.util.Base64;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import io.kyligence.benchmark.BenchmarkConfig;
import io.kyligence.benchmark.entity.QueryHistoryDTO;
import io.kyligence.benchmark.entity.QueryRequest;
import io.kyligence.benchmark.entity.QueryResponse;
import io.kyligence.benchmark.service.MetricsCollector;
import io.kyligence.benchmark.utils.HttpUtil;
import io.kyligence.benchmark.utils.SpringContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryTask implements Runnable {

    private QueryHistoryDTO queryHistoryDTO;
    private MetricsCollector metricCollector;
    private BenchmarkConfig benchmarkConfig;

    private static String QUERY_API_URL = "/kylin/api/query";
    private Integer round;
    /** query-id */
    private String taskId;

    public QueryTask(QueryHistoryDTO qh, Integer round) {
        this.queryHistoryDTO = qh;
        this.round = round;
        this.taskId = qh.getQuery_id();
        this.metricCollector = SpringContext.getBean(MetricsCollector.class);
        this.benchmarkConfig = SpringContext.getBean(BenchmarkConfig.class);
    }

    @Override
    public void run() {
        // * build query request & header
        QueryRequest queryRequest = new QueryRequest(queryHistoryDTO);
        String url = String.format("%s://%s", benchmarkConfig.HTTP_PROTOCAL,
                queryHistoryDTO.getServer() + QUERY_API_URL);
        Map<String, String> headers = Maps.newHashMap();
        String authenticationStr = String.format("%s:%s", benchmarkConfig.KYLIN_USER_NAME,
                benchmarkConfig.KYLIN_PASSWD);
        headers.put("Authorization",
                String.format("Basic %s", new String(Base64.getEncoder().encode(authenticationStr.getBytes()))));
        // * http_post
        try {
            String result = HttpUtil.httpsPost(url, JSONObject.toJSONString(queryRequest), headers);
            QueryResponse response = JSONObject.toJavaObject(JSONObject.parseObject(result), QueryResponse.class);
            // * analyse response

            // * metric collect
            metricCollector.collect(round,response.getDuration(),response.getTraces());
        } catch (Exception e) {
            log.error("error to ");
        }



        // * exception process
    }

    public static void main(String[] args) {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setQueryId("28e3422a-fde8-4db3-84f9-83c08f767646");
        queryRequest.setSql("select count(0) from dates");
        queryRequest.setProject("zhuzhong_project");
        queryRequest.setDryRunMode(true);
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Authorization", "Basic QURNSU46S1lMSU4=");
        try {
            String result = HttpUtil.httpsPost("http://localhost:8080" + QUERY_API_URL,
                    JSONObject.toJSONString(queryRequest), headers);
            System.out.println(result);
        } catch (Exception e) {
            log.error("error !!!!!! ", e);
        }
    }
}
