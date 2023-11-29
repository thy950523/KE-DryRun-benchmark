/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kyligence.benchmark.entity;

import java.util.HashMap;
import java.util.Map;

public class QueryTrace {

    // span name
    public static final String HTTP_RECEPTION = "HTTP_RECEPTION";
    public static final String GET_ACL_INFO = "GET_ACL_INFO";
    public static final String SQL_TRANSFORMATION = "SQL_TRANSFORMATION";
    public static final String SQL_PARSE_AND_OPTIMIZE = "SQL_PARSE_AND_OPTIMIZE";
    public static final String MODEL_MATCHING = "MODEL_MATCHING";
    public static final String PREPARE_AND_SUBMIT_JOB = "PREPARE_AND_SUBMIT_JOB";
    public static final String WAIT_FOR_EXECUTION = "WAIT_FOR_EXECUTION";
    public static final String EXECUTION = "EXECUTION";
    public static final String FETCH_RESULT = "FETCH_RESULT";
    /**
     * SPARK_JOB_EXECUTION: PREPARE_AND_SUBMIT_JOB + WAIT_FOR_EXECUTION + EXECUTION + FETCH_RESULT
     */
    public static final String SPARK_JOB_EXECUTION = "SPARK_JOB_EXECUTION";

    public static final String SQL_PUSHDOWN_TRANSFORMATION = "SQL_PUSHDOWN_TRANSFORMATION";
    public static final String HIT_CACHE = "HIT_CACHE";

    // group name
    static final String PREPARATION = "PREPARATION";
    static final String JOB_EXECUTION = "JOB_EXECUTION";

    public static final Map<String, String> SPAN_GROUPS = new HashMap<>();
    static {
        SPAN_GROUPS.put(GET_ACL_INFO, PREPARATION);
        SPAN_GROUPS.put(SQL_TRANSFORMATION, PREPARATION);
        SPAN_GROUPS.put(SQL_PARSE_AND_OPTIMIZE, PREPARATION);
        SPAN_GROUPS.put(MODEL_MATCHING, PREPARATION);

        SPAN_GROUPS.put(PREPARE_AND_SUBMIT_JOB, JOB_EXECUTION);
        SPAN_GROUPS.put(WAIT_FOR_EXECUTION, JOB_EXECUTION);
        SPAN_GROUPS.put(EXECUTION, JOB_EXECUTION);
        SPAN_GROUPS.put(FETCH_RESULT, JOB_EXECUTION);
    }

}
