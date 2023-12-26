package io.kyligence.benchmark.filter;

import io.kyligence.benchmark.BenchmarkConfig;
import io.kyligence.benchmark.entity.QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.Regexp;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * filter the query with exception and msg match the regex
 * do not collect these query metrics info
 */
@Aspect
@Component
@Slf4j
public class MetricCollectFilter {
    @Autowired
    private BenchmarkConfig config;

    @Pointcut("execution(* io.kyligence.benchmark.service.MetricsCollector.collect(..))")
    private void collect() {}

    @Around("collect()")
    public void beforeCollect(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean skip = false;
        if (config.EXCEPTION_FILTER_ENABLED) {
            QueryResponse response = (QueryResponse)joinPoint.getArgs()[1];
            if(response.isException()){
                Pattern pattern = Pattern.compile(config.getEXCEPTION_FILTER_REGEX());
                Matcher matcher = pattern.matcher(response.getExceptionMessage());
                if(matcher.find()){
                    skip = true;
                }
            }
        }
        if(!skip){
            joinPoint.proceed(joinPoint.getArgs());
        }
    }
}
