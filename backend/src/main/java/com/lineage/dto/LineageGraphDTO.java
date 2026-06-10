package com.lineage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 血缘图谱DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineageGraphDTO {

    /**
     * 节点列表
     */
    private List<GraphNode> nodes;

    /**
     * 边列表
     */
    private List<GraphEdge> edges;

    /**
     * 图谱节点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphNode {
        private String id;
        private String label;
        private String type; // table, column
        private Long tableId;
        private Long columnId;
        private String sourceName; // 数据源名称
    }

    /**
     * 图谱边
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphEdge {
        private String id;
        private String source;
        private String target;
        private String label; // 转换规则
        private String lineageType;
    }
}
