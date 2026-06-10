package com.lineage.repository;

import com.lineage.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源数据访问层
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {

    /**
     * 根据状态查询数据源列表
     */
    List<DataSource> findByStatus(Integer status);

    /**
     * 根据类型查询数据源
     */
    List<DataSource> findByType(String type);
}
