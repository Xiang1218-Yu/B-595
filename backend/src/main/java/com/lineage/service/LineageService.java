package com.lineage.service;

import com.lineage.dto.LineageGraphDTO;
import com.lineage.dto.LineageGraphDTO.GraphEdge;
import com.lineage.dto.LineageGraphDTO.GraphNode;
import com.lineage.entity.DataColumn;
import com.lineage.entity.DataLineage;
import com.lineage.entity.DataSource;
import com.lineage.entity.DataTable;
import com.lineage.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据血缘服务类
 */
@Service
public class LineageService {

    private static final Logger logger = LoggerFactory.getLogger(LineageService.class);

    @Autowired
    private DataLineageRepository lineageRepository;

    @Autowired
    private DataTableRepository tableRepository;

    @Autowired
    private DataColumnRepository columnRepository;

    @Autowired
    private DataSourceRepository sourceRepository;

    /**
     * 获取所有血缘关系
     */
    @Transactional(readOnly = true)
    public List<DataLineage> getAllLineages() {
        logger.debug("获取所有血缘关系");
        return lineageRepository.findAll();
    }

    /**
     * 创建血缘关系
     */
    @Transactional
    public DataLineage createLineage(DataLineage lineage, Long userId) {
        logger.info("创建血缘关系");
        lineage.setCreateBy(userId);
        return lineageRepository.save(lineage);
    }

    /**
     * 删除血缘关系
     */
    @Transactional
    public void deleteLineage(Long id) {
        logger.info("删除血缘关系: {}", id);
        lineageRepository.deleteById(id);
    }

    /**
     * 根据表ID获取血缘图谱
     */
    @Transactional(readOnly = true)
    public LineageGraphDTO getLineageGraph(Long tableId) {
        logger.info("获取表{}的血缘图谱", tableId);

        // 获取该表的所有血缘关系
        List<DataLineage> lineages = lineageRepository.findAllByTableId(tableId);

        // 收集所有相关的表ID
        Set<Long> tableIds = new HashSet<>();
        tableIds.add(tableId);
        for (DataLineage lineage : lineages) {
            tableIds.add(lineage.getSourceTableId());
            tableIds.add(lineage.getTargetTableId());
        }

        // 获取所有相关表信息
        Map<Long, DataTable> tableMap = tableRepository.findAllById(tableIds)
                .stream()
                .collect(Collectors.toMap(DataTable::getId, t -> t));

        // 获取数据源信息
        Set<Long> sourceIds = tableMap.values().stream()
                .map(DataTable::getSourceId)
                .collect(Collectors.toSet());
        Map<Long, DataSource> sourceMap = sourceRepository.findAllById(sourceIds)
                .stream()
                .collect(Collectors.toMap(DataSource::getId, s -> s));

        // 构建节点
        List<GraphNode> nodes = new ArrayList<>();
        for (DataTable table : tableMap.values()) {
            DataSource source = sourceMap.get(table.getSourceId());
            String sourceName = source != null ? source.getName() : "未知";

            GraphNode node = new GraphNode(
                    "table_" + table.getId(),
                    table.getTableName(),
                    "table",
                    table.getId(),
                    null,
                    sourceName);
            nodes.add(node);
        }

        // 构建边
        List<GraphEdge> edges = new ArrayList<>();
        for (DataLineage lineage : lineages) {
            GraphEdge edge = new GraphEdge(
                    "lineage_" + lineage.getId(),
                    "table_" + lineage.getSourceTableId(),
                    "table_" + lineage.getTargetTableId(),
                    lineage.getTransformRule(),
                    lineage.getLineageType());
            edges.add(edge);
        }

        return new LineageGraphDTO(nodes, edges);
    }

    /**
     * 搜索血缘关系
     */
    @Transactional(readOnly = true)
    public LineageGraphDTO searchLineage(String tableName) {
        logger.info("搜索血缘关系: {}", tableName);

        // 查找匹配的表
        List<DataTable> tables = tableRepository.findByTableNameContaining(tableName);
        if (tables.isEmpty()) {
            return new LineageGraphDTO(new ArrayList<>(), new ArrayList<>());
        }

        // 返回第一个匹配表的血缘图谱
        return getLineageGraph(tables.get(0).getId());
    }
}
