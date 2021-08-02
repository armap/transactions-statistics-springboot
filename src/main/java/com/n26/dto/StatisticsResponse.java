package com.n26.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatisticsResponse {
    private String sum;
    private String avg;
    private String max;
    private String min;
    private long count;
}
