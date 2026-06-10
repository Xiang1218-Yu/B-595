package com.lineage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作台统计数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    /**
     * 数据源总数
     */
    private Long dataSourceCount;

    /**
     * 数据表总数
     */
    private Long tableCount;

    /**
     * 血缘关系总数
     */
    private Long lineageCount;

    /**
     * 用户总数
     */
    private Long userCount;
}
