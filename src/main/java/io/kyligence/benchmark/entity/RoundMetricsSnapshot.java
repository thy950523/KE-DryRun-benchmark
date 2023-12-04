package io.kyligence.benchmark.entity;

import com.codahale.metrics.Snapshot;
import com.google.common.collect.Maps;
import io.kyligence.benchmark.enums.QuerySpanEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * snapshot of each round
 */
@Getter
@Setter
@NoArgsConstructor
public class RoundMetricsSnapshot {
    private Integer round;
    private Snapshot roundSnapshot;
    private Map<QuerySpanEnum, Snapshot> stepSnapshotMap;
    private String maxQueryId;
    private String maxQuerySql;
    private List<SQLResponseTrace> maxQueryTraceList;
    private Integer successCnt;
    private Integer failedCnt;

    public RoundMetricsSnapshot(Integer round) {
        this.round = round;
        stepSnapshotMap = Maps.newTreeMap((a,b)->{
            return a.getSequence() - b.getSequence();});
    }

    public void addStepSnapshot(QuerySpanEnum step,Snapshot snapshot){
        stepSnapshotMap.put(step, snapshot);
    }


}
