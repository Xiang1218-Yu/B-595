package com.lineage.repository;

import com.lineage.entity.DataColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据列数据访问层
 */
@Repository
public interface DataColumnRepository extends JpaRepository<DataColumn, Long> {

    /**
     * 根据表ID查询列列表
     */
    List<DataColumn> findByTableIdOrderByColumnOrder(Long tableId);

    /**
     * 删除表的所有列
     */
    void deleteByTableId(Long tableId);
}
