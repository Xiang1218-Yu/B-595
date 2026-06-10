package com.lineage.service;

import com.lineage.dto.DashboardStatsDTO;
import com.lineage.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 工作台服务类
 */
@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private DataTableRepository dataTableRepository;

    @Autowired
    private DataLineageRepository dataLineageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取工作台统计数据
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        logger.info("获取工作台统计数据");

        Long dataSourceCount = dataSourceRepository.count();
        Long tableCount = dataTableRepository.count();
        Long lineageCount = dataLineageRepository.count();
        Long userCount = userRepository.count();

        return new DashboardStatsDTO(dataSourceCount, tableCount, lineageCount, userCount);
    }
}
