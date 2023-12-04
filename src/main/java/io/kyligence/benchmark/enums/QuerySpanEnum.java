package io.kyligence.benchmark.enums;

import lombok.Getter;

public enum QuerySpanEnum {
    HTTP_RECEPTION(1, "HTTP_RECEPTION", true)
    ,GET_ACL_INFO(2, "GET_ACL_INFO", true)
    ,SQL_TRANSFORMATION(3, "SQL_TRANSFORMATION", true)
    ,SQL_PARSE_AND_OPTIMIZE(4, "SQL_PARSE_AND_OPTIMIZE", true)
    ,MODEL_MATCHING(5, "MODEL_MATCHING", true)
    ,PREPARE_AND_SUBMIT_JOB(6, "PREPARE_AND_SUBMIT_JOB", true)
    ,WAIT_FOR_EXECUTION(7, "WAIT_FOR_EXECUTION", true)
    ,EXECUTION(8, "EXECUTION", true)
    ,FETCH_RESULT(9, "FETCH_RESULT", true)
    ,SPARK_JOB_EXECUTION(10, "SPARK_JOB_EXECUTION", true)
    ,SQL_PUSHDOWN_TRANSFORMATION(11, "SQL_PUSHDOWN_TRANSFORMATION", true)
    ,HIT_CACHE(12, "HIT_CACHE", true);


    public static boolean contains(String spanName) {
        for (QuerySpanEnum span : QuerySpanEnum.values()) {
            if (span.getName().equalsIgnoreCase(spanName)) {
                return true;
            }
        }
        return false;
    }

    @Getter
    private Integer sequence;
    @Getter
    private String name;
    @Getter
    private boolean needMetric;

    QuerySpanEnum(Integer sequence, String name, Boolean needMetric) {
        this.sequence = sequence;
        this.name = name;
        this.needMetric = needMetric;
    }

}
