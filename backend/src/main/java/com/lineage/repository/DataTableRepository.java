package com.lineage.repository;

import com.lineage.entity.DataTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据表数据访问层
 */
@Repository
public interface DataTableRepository extends JpaRepository<DataTable, Long> {

    /**
     * 根据数据源ID查询表列表
     */
    List<DataTable> findBySourceId(Long sourceId);

    /**
     * 根据表名模糊查询
     */
    List<DataTable> findByTableNameContaining(String tableName);
}
