package io.kyligence.benchmark;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class BenchmarkConfig {
    @Value("${file-dir:}")
    public String FILE_DIR;
    @Value("${concurrency:4}")
    public Integer CONCURRENCY;
    @Value("${rounds:3}")
    public Integer ROUNDS;

    @Value("${http.protocol:http}")
    public String HTTP_PROTOCAL;

    @Value("${kylin.userName:ADMIN}")
    public String KYLIN_USER_NAME;
    @Value("${kylin.passwd:KYLIN}")
    public String KYLIN_PASSWD;
}
