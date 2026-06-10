package com.lineage.controller;

import com.lineage.dto.Result;
import com.lineage.entity.DataColumn;
import com.lineage.entity.DataTable;
import com.lineage.service.MetadataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 元数据控制器
 */
@Api(tags = "元数据管理")
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    @Autowired
    private MetadataService metadataService;

    @ApiOperation("获取所有数据表")
    @GetMapping("/tables")
    public Result<List<DataTable>> getAllTables() {
        List<DataTable> list = metadataService.getAllTables();
        return Result.success(list);
    }

    @ApiOperation("根据数据源获取表列表")
    @GetMapping("/tables/source/{sourceId}")
    public Result<List<DataTable>> getTablesBySource(@PathVariable Long sourceId) {
        List<DataTable> list = metadataService.getTablesBySourceId(sourceId);
        return Result.success(list);
    }

    @ApiOperation("搜索数据表")
    @GetMapping("/tables/search")
    public Result<List<DataTable>> searchTables(@RequestParam String keyword) {
        List<DataTable> list = metadataService.searchTables(keyword);
        return Result.success(list);
    }

    @ApiOperation("获取表详情")
    @GetMapping("/tables/{tableId}")
    public Result<Map<String, Object>> getTableDetail(@PathVariable Long tableId) {
        Map<String, Object> detail = metadataService.getTableDetail(tableId);
        return Result.success(detail);
    }

    @ApiOperation("创建数据表")
    @PostMapping("/tables")
    public Result<DataTable> createTable(@RequestBody DataTable table) {
        DataTable created = metadataService.createTable(table);
        return Result.success(created);
    }

    @ApiOperation("更新数据表")
    @PutMapping("/tables/{id}")
    public Result<DataTable> updateTable(@PathVariable Long id, @RequestBody DataTable table) {
        DataTable updated = metadataService.updateTable(id, table);
        return Result.success(updated);
    }

    @ApiOperation("删除数据表")
    @DeleteMapping("/tables/{id}")
    public Result<Void> deleteTable(@PathVariable Long id) {
        metadataService.deleteTable(id);
        return Result.success();
    }

    @ApiOperation("保存表列信息")
    @PostMapping("/tables/{tableId}/columns")
    public Result<Void> saveColumns(@PathVariable Long tableId,
            @RequestBody List<DataColumn> columns) {
        metadataService.saveColumns(tableId, columns);
        return Result.success();
    }
}
