package io.kyligence.benchmark.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Metrics {
    private String type;
    private String group;
    private String name;
    private long duration;
}
