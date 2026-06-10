package com.lineage.controller;

import com.lineage.dto.Result;
import com.lineage.entity.DataSource;
import com.lineage.service.DataSourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 数据源控制器
 */
@Api(tags = "数据源管理")
@RestController
@RequestMapping("/api/datasources")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @ApiOperation("获取所有数据源")
    @GetMapping
    public Result<List<DataSource>> getAllDataSources() {
        List<DataSource> list = dataSourceService.getAllDataSources();
        return Result.success(list);
    }

    @ApiOperation("获取数据源详情")
    @GetMapping("/{id}")
    public Result<DataSource> getDataSource(@PathVariable Long id) {
        DataSource dataSource = dataSourceService.getDataSourceById(id);
        return Result.success(dataSource);
    }

    @ApiOperation("创建数据源")
    @PostMapping
    public Result<DataSource> createDataSource(@RequestBody DataSource dataSource,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        DataSource created = dataSourceService.createDataSource(dataSource, userId);
        return Result.success(created);
    }

    @ApiOperation("更新数据源")
    @PutMapping("/{id}")
    public Result<DataSource> updateDataSource(@PathVariable Long id,
            @RequestBody DataSource dataSource) {
        DataSource updated = dataSourceService.updateDataSource(id, dataSource);
        return Result.success(updated);
    }

    @ApiOperation("删除数据源")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return Result.success();
    }

    @ApiOperation("测试连接")
    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        boolean success = dataSourceService.testConnection(id);
        return Result.success(success);
    }
}
