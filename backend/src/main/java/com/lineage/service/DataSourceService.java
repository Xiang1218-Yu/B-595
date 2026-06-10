package com.lineage.service;

import com.lineage.entity.DataSource;
import com.lineage.repository.DataSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据源服务类
 */
@Service
public class DataSourceService {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceService.class);

    @Autowired
    private DataSourceRepository dataSourceRepository;

    /**
     * 获取所有数据源
     */
    @Transactional(readOnly = true)
    public List<DataSource> getAllDataSources() {
        logger.debug("获取所有数据源");
        return dataSourceRepository.findAll();
    }

    /**
     * 根据ID获取数据源
     */
    @Transactional(readOnly = true)
    public DataSource getDataSourceById(Long id) {
        logger.debug("获取数据源: {}", id);
        return dataSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据源不存在"));
    }

    /**
     * 创建数据源
     */
    @Transactional
    public DataSource createDataSource(DataSource dataSource, Long userId) {
        logger.info("创建数据源: {}", dataSource.getName());
        dataSource.setCreateBy(userId);
        return dataSourceRepository.save(dataSource);
    }

    /**
     * 更新数据源
     */
    @Transactional
    public DataSource updateDataSource(Long id, DataSource dataSource) {
        logger.info("更新数据源: {}", id);

        DataSource existing = getDataSourceById(id);
        existing.setName(dataSource.getName());
        existing.setType(dataSource.getType());
        existing.setHost(dataSource.getHost());
        existing.setPort(dataSource.getPort());
        existing.setDatabase(dataSource.getDatabase());
        existing.setUsername(dataSource.getUsername());
        if (dataSource.getPassword() != null) {
            existing.setPassword(dataSource.getPassword());
        }
        existing.setDescription(dataSource.getDescription());
        existing.setStatus(dataSource.getStatus());

        return dataSourceRepository.save(existing);
    }

    /**
     * 删除数据源
     */
    @Transactional
    public void deleteDataSource(Long id) {
        logger.info("删除数据源: {}", id);
        dataSourceRepository.deleteById(id);
    }

    /**
     * 测试数据源连接
     */
    public boolean testConnection(Long id) {
        logger.info("测试数据源连接: {}", id);
        // 这里可以实现实际的数据库连接测试逻辑
        return true;
    }
}
