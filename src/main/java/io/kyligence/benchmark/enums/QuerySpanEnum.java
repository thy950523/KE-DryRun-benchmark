package io.kyligence.benchmark.enums;

import lombok.Getter;

public enum QuerySpanEnum {
    HTTP_RECEPTION("HTTP_RECEPTION",true),
    GET_ACL_INFO("GET_ACL_INFO",true),
    SQL_TRANSFORMATION("SQL_TRANSFORMATION",true),
    SQL_PARSE_AND_OPTIMIZE("SQL_PARSE_AND_OPTIMIZE",true),
    MODEL_MATCHING("MODEL_MATCHING",true),
    PREPARE_AND_SUBMIT_JOB("PREPARE_AND_SUBMIT_JOB",true),
    WAIT_FOR_EXECUTION("WAIT_FOR_EXECUTION",true),
    EXECUTION("EXECUTION",true),
    FETCH_RESULT("FETCH_RESULT",true),
    SPARK_JOB_EXECUTION("SPARK_JOB_EXECUTION",true),
    SQL_PUSHDOWN_TRANSFORMATION("SQL_PUSHDOWN_TRANSFORMATION",true),
    HIT_CACHE("HIT_CACHE",true);


    public static boolean contains(String spanName){
        for (QuerySpanEnum span : QuerySpanEnum.values()) {
            if (span.getName().equalsIgnoreCase(spanName)) {
                return true;
            }
        }
        return false;
    }

    @Getter
    private String name;
    @Getter
    private boolean needMetric;

    QuerySpanEnum(String name,Boolean needMetric) {
        this.name = name;
        this.needMetric = needMetric;
    }

}
