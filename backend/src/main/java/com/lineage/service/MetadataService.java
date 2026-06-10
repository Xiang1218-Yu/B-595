package com.lineage.service;

import com.lineage.entity.DataColumn;
import com.lineage.entity.DataTable;
import com.lineage.repository.DataColumnRepository;
import com.lineage.repository.DataTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 元数据服务类
 */
@Service
public class MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    @Autowired
    private DataTableRepository dataTableRepository;

    @Autowired
    private DataColumnRepository dataColumnRepository;

    /**
     * 获取所有数据表
     */
    @Transactional(readOnly = true)
    public List<DataTable> getAllTables() {
        logger.debug("获取所有数据表");
        return dataTableRepository.findAll();
    }

    /**
     * 根据数据源ID获取表列表
     */
    @Transactional(readOnly = true)
    public List<DataTable> getTablesBySourceId(Long sourceId) {
        logger.debug("获取数据源{}的表列表", sourceId);
        return dataTableRepository.findBySourceId(sourceId);
    }

    /**
     * 根据表名搜索
     */
    @Transactional(readOnly = true)
    public List<DataTable> searchTables(String keyword) {
        logger.debug("搜索数据表: {}", keyword);
        return dataTableRepository.findByTableNameContaining(keyword);
    }

    /**
     * 获取表详情（包含列信息）
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTableDetail(Long tableId) {
        logger.debug("获取表详情: {}", tableId);

        DataTable table = dataTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("数据表不存在"));

        List<DataColumn> columns = dataColumnRepository.findByTableIdOrderByColumnOrder(tableId);

        Map<String, Object> result = new HashMap<>();
        result.put("table", table);
        result.put("columns", columns);

        return result;
    }

    /**
     * 创建数据表
     */
    @Transactional
    public DataTable createTable(DataTable table) {
        logger.info("创建数据表: {}", table.getTableName());
        return dataTableRepository.save(table);
    }

    /**
     * 更新数据表
     */
    @Transactional
    public DataTable updateTable(Long id, DataTable table) {
        logger.info("更新数据表: {}", id);

        DataTable existing = dataTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据表不存在"));

        existing.setTableName(table.getTableName());
        existing.setTableComment(table.getTableComment());
        existing.setTableType(table.getTableType());
        existing.setDescription(table.getDescription());

        return dataTableRepository.save(existing);
    }

    /**
     * 删除数据表
     */
    @Transactional
    public void deleteTable(Long id) {
        logger.info("删除数据表: {}", id);
        dataColumnRepository.deleteByTableId(id);
        dataTableRepository.deleteById(id);
    }

    /**
     * 批量保存列信息
     */
    @Transactional
    public void saveColumns(Long tableId, List<DataColumn> columns) {
        logger.info("保存表{}的列信息", tableId);

        // 先删除旧的列信息
        dataColumnRepository.deleteByTableId(tableId);

        // 保存新的列信息
        for (DataColumn column : columns) {
            column.setTableId(tableId);
        }
        dataColumnRepository.saveAll(columns);

        // 更新表的列数量
        DataTable table = dataTableRepository.findById(tableId).orElse(null);
        if (table != null) {
            table.setColumnCount(columns.size());
            dataTableRepository.save(table);
        }
    }
}
