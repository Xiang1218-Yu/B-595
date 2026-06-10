package com.lineage.controller;

import com.lineage.dto.LineageGraphDTO;
import com.lineage.dto.Result;
import com.lineage.entity.DataLineage;
import com.lineage.service.LineageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 血缘关系控制器
 */
@Api(tags = "血缘管理")
@RestController
@RequestMapping("/api/lineage")
public class LineageController {

    @Autowired
    private LineageService lineageService;

    @ApiOperation("获取所有血缘关系")
    @GetMapping
    public Result<List<DataLineage>> getAllLineages() {
        List<DataLineage> list = lineageService.getAllLineages();
        return Result.success(list);
    }

    @ApiOperation("创建血缘关系")
    @PostMapping
    public Result<DataLineage> createLineage(@RequestBody DataLineage lineage,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        DataLineage created = lineageService.createLineage(lineage, userId);
        return Result.success(created);
    }

    @ApiOperation("删除血缘关系")
    @DeleteMapping("/{id}")
    public Result<Void> deleteLineage(@PathVariable Long id) {
        lineageService.deleteLineage(id);
        return Result.success();
    }

    @ApiOperation("获取血缘图谱")
    @GetMapping("/graph/{tableId}")
    public Result<LineageGraphDTO> getLineageGraph(@PathVariable Long tableId) {
        LineageGraphDTO graph = lineageService.getLineageGraph(tableId);
        return Result.success(graph);
    }

    @ApiOperation("搜索血缘关系")
    @GetMapping("/search")
    public Result<LineageGraphDTO> searchLineage(@RequestParam String tableName) {
        LineageGraphDTO graph = lineageService.searchLineage(tableName);
        return Result.success(graph);
    }
}
