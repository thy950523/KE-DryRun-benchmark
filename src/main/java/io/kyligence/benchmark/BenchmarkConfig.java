package io.kyligence.benchmark;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.kyligence.benchmark.exception.HttpRequestException;
import io.kyligence.benchmark.utils.HttpUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Base64;
import java.util.Map;

@Component
@Getter
@Slf4j
public class BenchmarkConfig {
    /** 核心配置 */
    @Value("${file.dir:}")
    public String FILE_DIR;
    @Value("${rounds:3}")
    public Integer ROUNDS;
    @Value("${args.check.enabled:true}")
    public boolean CHECK_ENABLED;
    @Value("${metadata.recover.enabled:false}")
    private boolean METADATA_RECOVER_ENABLED;

    /** 并发配置 */
    @Value("${concurrency:4}")
    public Integer CONCURRENCY;
    @Value("${queue.size:2048}")
    public Integer QUEUE_SIZE;

    /** query 查询配置 */
    @Value("${http.protocol:http}")
    public String HTTP_PROTOCAL;
    @Value("${kylin.query.node:}")
    public String KYLIN_QUERY_NODE;
    @Value("${kylin.userName:ADMIN}")
    public String KYLIN_USER_NAME;
    @Value("${kylin.passwd:KYLIN}")
    public String KYLIN_PASSWD;

    /**
     * 报告相关
     */
    @Value("${report.output.dir:.}")
    public String REPORT_OUTPUT_DIR;

    /**
     * 错误请求过滤器配置
     */
    @Value("${query.exception.filter.enabled:false}")
    public boolean EXCEPTION_FILTER_ENABLED;
    @Value("${query.exception.filter.regex:}")
    public String EXCEPTION_FILTER_REGEX;


    public boolean checkArgs() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            log.info("[CHECK]-[CONFIGURATION]: \tBenchmark config is as below：");
            System.out.println(gson.toJson(this));

            // check file & ifExist
            Assert.hasText(FILE_DIR, "file-dir is empty");
            File file = new File(FILE_DIR);
            Assert.isTrue(file.exists(), String.format("file not found:%s", FILE_DIR));

            // check http-query is reachable
            if (CHECK_ENABLED) {
                Assert.hasText(KYLIN_QUERY_NODE,"query.node is empty");
                // check kylin user & passwd
                String url = String.format("%S://%S/kylin/api/user/authentication", HTTP_PROTOCAL, KYLIN_QUERY_NODE);
                log.info("[CHECK]-[CONFIGURATION]-[HTTP]: try to fetch kylin user info, url:{}",url);
                Map<String, String> headers = Maps.newHashMap();
                String authenticationStr = String.format("%s:%s", KYLIN_USER_NAME, KYLIN_PASSWD);
                headers.put("Authorization", String.format("Basic %s",
                        new String(Base64.getEncoder().encode(authenticationStr.getBytes()))));
                String response = HttpUtil.httpsGet(url, headers);
                log.info("[CHECK]-[CONFIGURATION]-[HTTP]: user authentication passed ");
            }
        }catch (HttpRequestException e){
            log.error("User info authenticate failed, Plz check ur configuration !!!");
            return false;
        }
        catch (Exception e) {
            log.error("Args check failed,\t: {}", e.getMessage());
            return false;
        }
        return true;
    }
}
