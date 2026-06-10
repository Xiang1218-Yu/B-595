package com.lineage.repository;

import com.lineage.entity.DataLineage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据血缘数据访问层
 */
@Repository
public interface DataLineageRepository extends JpaRepository<DataLineage, Long> {

    /**
     * 查询某表作为源表的所有血缘关系
     */
    List<DataLineage> findBySourceTableId(Long sourceTableId);

    /**
     * 查询某表作为目标表的所有血缘关系
     */
    List<DataLineage> findByTargetTableId(Long targetTableId);

    /**
     * 查询表的所有血缘关系（上游+下游）
     */
    @Query("SELECT l FROM DataLineage l WHERE l.sourceTableId = :tableId OR l.targetTableId = :tableId")
    List<DataLineage> findAllByTableId(@Param("tableId") Long tableId);
}
