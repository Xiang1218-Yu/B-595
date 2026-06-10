# 血缘数据全链路流转说明（后端构建 → 前端 ECharts 渲染）

本文档梳理 `B-595_summer` 项目中"数据血缘图谱"功能从数据库实体到前端 ECharts `graph` 图表渲染的完整数据流，重点标注每一处 **数据形态转换** 的关键节点。

---

## 1. 总体链路

```
MySQL 表(data_lineage / data_table / data_source)
        │  JPA Repository
        ▼
 实体对象 List<DataLineage>、Map<Long,DataTable>、Map<Long,DataSource>
        │  LineageService 聚合 / 组装
        ▼
 LineageGraphDTO { nodes:[GraphNode], edges:[GraphEdge] }
        │  Controller 包裹 Result<T>
        ▼
 HTTP JSON: { code, message, data:{ nodes, edges } }
        │  axios 响应拦截器拆包 → 返回 res.data
        ▼
 前端 graphData.value = { nodes, edges }
        │  computed(chartOption) 二次映射
        ▼
 ECharts series.data / series.edges → <v-chart> 渲染力导向图
```

---

## 2. 后端数据构建

### 2.1 数据源层（实体 & 仓库）

- 血缘实体 [DataLineage.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/backend/src/main/java/com/lineage/entity/DataLineage.java)：以 `sourceTableId / targetTableId / lineageType / transformRule` 描述一条有向边。
- 表实体 [DataTable.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/backend/src/main/java/com/lineage/entity/DataTable.java)：提供节点的展示名 `tableName` 与 `sourceId`。
- Repository [DataLineageRepository.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/backend/src/main/java/com/lineage/repository/DataLineageRepository.java#L29-L31) 提供 `findAllByTableId`，一次性取出某表"上游+下游"的所有边。

### 2.2 服务层组装（关键转换点 ①）

[LineageService.getLineageGraph()](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/backend/src/main/java/com/lineage/service/LineageService.java#L72-L128) 完成 **实体 → 图谱 DTO** 的核心转换，逻辑分四步：

1. **取边**：`lineageRepository.findAllByTableId(tableId)` 拿到所有 `DataLineage`。
2. **聚合相关表 ID**：把当前 `tableId` 与每条边的 `sourceTableId / targetTableId` 收进 `Set<Long> tableIds`，避免重复查询。
3. **批量回填**：用 `findAllById` 拿到 `Map<Long, DataTable>` 与 `Map<Long, DataSource>`，将"边里只有 ID"补成"节点带名字、带数据源名"。
4. **节点 / 边构造**：
   - 节点 ID 规则：`"table_" + table.id`（前端必须用同样的字符串当作 ECharts node id）。
   - 边 source/target 同样写成 `"table_" + lineage.sourceTableId/targetTableId`。
   - 边 label 取 `transformRule`（转换规则，会显示在连线上）。

搜索接口 [searchLineage()](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/backend/src/main/java/com/lineage/service/LineageService.java#L134-L145) 复用同一构建逻辑：模糊匹配 `tableName`，取第一条命中后调用 `getLineageGraph`。空结果返回 `nodes=[], edges=[]`。

### 2.3 DTO 数据形态（关键转换点 ②）

[LineageGraphDTO.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/backend/src/main/java/com/lineage/dto/LineageGraphDTO.java) 即图谱契约：

```text
LineageGraphDTO
├─ nodes: List<GraphNode>
│    GraphNode { id, label, type, tableId, columnId, sourceName }
└─ edges: List<GraphEdge>
     GraphEdge { id, source, target, label, lineageType }
```

字段映射对照：

| 数据库字段 | DTO 字段 | 用途 |
|---|---|---|
| `data_table.id` | `node.tableId` + `node.id="table_<id>"` | 节点身份 |
| `data_table.tableName` | `node.label` | 节点显示名 |
| `data_source.name` | `node.sourceName` | tooltip 中"数据源" |
| `data_lineage.sourceTableId` | `edge.source="table_<id>"` | 起点 |
| `data_lineage.targetTableId` | `edge.target="table_<id>"` | 终点 |
| `data_lineage.transformRule` | `edge.label` | 连线上的文字 |
| `data_lineage.lineageType` | `edge.lineageType` | TABLE / COLUMN |

### 2.4 控制器与统一响应（关键转换点 ③）

[LineageController.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/backend/src/main/java/com/lineage/controller/LineageController.java#L49-L61) 的图谱接口：

- `GET /api/lineage/graph/{tableId}` → 按 ID 查询。
- `GET /api/lineage/search?tableName=xxx` → 按表名搜索。

返回值统一通过 [Result.success(data)](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/backend/src/main/java/com/lineage/dto/Result.java#L22-L24) 包装成：

```json
{ "code": 200, "message": "操作成功", "data": { "nodes": [...], "edges": [...] } }
```

---

## 3. 前端数据消费

### 3.1 请求层（关键转换点 ④：拆 `Result` 包装）

[request.js](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/frontend/src/utils/request.js#L27-L37) 的响应拦截器统一：

- 校验 `res.code === 200`，否则 `ElMessage.error` + reject。
- **直接返回 `res.data`**：因此业务代码拿到的就是 `LineageGraphDTO` 本体，不再有 `code/message` 包装。

### 3.2 API 封装

[api/index.js](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/frontend/src/api/index.js#L215-L230)：

- `getLineageGraph(tableId)` → `GET /lineage/graph/:tableId`
- `searchLineage(tableName)` → `GET /lineage/search?tableName=...`
- `getAllLineages / createLineage / deleteLineage` 用于下方列表与新建。

### 3.3 视图层状态管理

[Lineage.vue](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/frontend/src/views/Lineage.vue#L152-L155) 维护核心响应式状态：

```js
const graphData = ref({ nodes: [], edges: [] })
```

搜索流程 [handleSearch](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/frontend/src/views/Lineage.vue#L255-L272)：
1. `searchLineage(searchText.value)` 拉取后端图谱。
2. 直接赋值 `graphData.value = await searchLineage(...)`（结构与后端 DTO 完全对齐）。
3. `nodes.length === 0` 时给出空态提示。

### 3.4 ECharts 适配（关键转换点 ⑤：DTO → ECharts option）

[chartOption computed](file:///Users/tog/Desktop/code/gsb/B-595/B-595_summer/frontend/src/views/Lineage.vue#L168-L222) 把 `graphData` 二次映射成 ECharts `graph` 系列所需结构：

| 后端 GraphNode | ECharts series.data 项 |
|---|---|
| `id` | `id` |
| `label` | `name`（节点文字） |
| `sourceName` | `sourceName`（自定义字段，给 tooltip 用） |
| —— | `symbolSize: 50` |
| —— | `itemStyle.color: '#5470c6'` |
| —— | `label.show / position / fontSize` |

| 后端 GraphEdge | ECharts series.edges 项 |
|---|---|
| `source` | `source`（必须与某个节点 `id` 完全一致） |
| `target` | `target` |
| `label` | `label.formatter`（连线文字，转换规则） |
| —— | `lineStyle.color: '#91cc75'`、`curveness: 0.2`、`width: 2` |

布局与交互固定参数：

- `layout: 'force'`，`force: { repulsion: 300, edgeLength: 150 }`：力导向布局。
- `roam: true`：允许缩放/拖拽画布。
- `emphasis.focus: 'adjacency'`：hover 节点时高亮邻接边。
- `tooltip.formatter`：节点显示 `名称 + 数据源`，边显示 `转换规则`。

### 3.5 渲染

模板中通过 `<v-chart :option="chartOption" autoresize />` 把上述 option 传给 `vue-echarts`，并按需 `use([CanvasRenderer, GraphChart, TitleComponent, TooltipComponent, LegendComponent])` 注册组件，即完成绘制。

---

## 4. 关键转换节点速查

| # | 位置 | 输入 | 输出 | 作用 |
|---|---|---|---|---|
| ① | `LineageService.getLineageGraph` | `List<DataLineage>` + `List<DataTable>` + `List<DataSource>` | `LineageGraphDTO` | 实体聚合，给节点补名字与数据源 |
| ② | `LineageGraphDTO` | —— | `nodes/edges` 结构 | 后端契约，统一 ID 前缀 `table_` |
| ③ | `Result.success` + Controller | `LineageGraphDTO` | `{code,message,data}` JSON | 统一响应包装 |
| ④ | `request.js` 响应拦截 | `{code,message,data}` | `data`（即 DTO 本体） | 自动拆包，简化业务调用 |
| ⑤ | `chartOption` computed | `graphData.nodes/edges` | ECharts `series.data/edges` | 加上样式、symbolSize、自定义字段供 tooltip 使用 |

---

## 5. 易错点 / 注意事项

1. **节点 ID 必须前缀化**：后端给所有节点 ID 加 `"table_"` 前缀，边的 `source/target` 必须用同样格式，否则 ECharts 找不到对应节点会报"can't find node"。
2. **空数据兜底**：`searchLineage` 未命中表名时返回 `nodes=[], edges=[]`，前端 `v-if="!graphData.nodes.length"` 显示 `<el-empty>` 占位，避免 ECharts 渲染异常。
3. **响应拦截已拆包**：业务代码不能再读 `res.data.data`，直接得到的就是 DTO；新增接口需要遵循 `Result<T>` 规范，否则拦截器会因 `code !== 200` 而抛错。
4. **节点连接同时遍历上下游**：`findAllByTableId` 用 `OR` 把当前表既当 source 又当 target 的边都拿出来，因此图谱中包含上游与下游两侧。
5. **新建血缘不会自动刷新图谱**：`handleSave` 只重新加载 `lineageList`，要查看新边需再次执行"搜索"触发 `searchLineage`。
6. **力导向布局是非确定性的**：每次刷新节点位置都不同，如需稳定坐标可改用 `layout: 'none'` 并手动给 `x/y`。
