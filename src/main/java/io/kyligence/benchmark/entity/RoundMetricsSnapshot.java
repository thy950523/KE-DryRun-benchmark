package io.kyligence.benchmark.entity;

import java.util.Map;

import com.codahale.metrics.Snapshot;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * snapshot of each round
 */
@Getter
@Setter
@NoArgsConstructor
public class RoundMetricsSnapshot {
    private Integer round;
    private Snapshot roundSnapshot;
    private Map<String, Snapshot> stepSnapshotMap;

    public RoundMetricsSnapshot(Integer round) {
        this.round = round;
        stepSnapshotMap = Maps.newHashMap();
    }

    public void addStepSnapshot(String step,Snapshot snapshot){
        stepSnapshotMap.put(step, snapshot);
    }


}
