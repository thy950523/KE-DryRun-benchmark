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

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class QueryResponse implements Serializable {
    protected static final long serialVersionUID = 1L;

    // the data type for each column
//    private List<SelectedColumnMeta> columnMetas;

    // the results rows, each row contains several columns
    private Iterable<List<String>> results;

    // if not select query, only return affected row count
    protected int affectedRowCount;

    // queryTagInfo indicating whether an exception occurred
    protected boolean isException;

    // if isException, the detailed exception message
    protected String exceptionMessage;

    // if isException, the related Exception
    protected Throwable throwable;

    protected long duration;

    protected boolean isPartial = false;

    private boolean isVacant;

    private List<Long> scanRows;

    private List<Long> scanBytes;

    private String appMasterURL = "";

    protected int failTimes = -1;

    protected long resultRowCount;

    protected int shufflePartitions;

    protected boolean hitExceptionCache = false;

    protected boolean storageCacheUsed = false;

    protected String storageCacheType;

    protected long dataFetchTime;

    protected boolean queryPushDown = false;

    private boolean isPrepare = false;

    private boolean isTimeout;

    private boolean isRefused;

    protected byte[] queryStatistics;

    protected String queryId;

    private String server;

    private boolean isStopByUser;

    private String signature;

//    private List<NativeQueryRealization> nativeRealizations;

    private String engineType;

    private List<SQLResponseTrace> traces;

    private String executedPlan;

    private boolean isBigQuery = false;


//    public static failResponse(){
//
//    }
}
