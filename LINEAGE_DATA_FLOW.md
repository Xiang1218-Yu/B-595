# 数据血缘图谱：后端到前端完整数据流路径

## 一、数据流总览

```
MySQL数据库 → JPA实体 → Repository查询 → Service数据组装 → Controller/Result包装
    → HTTP JSON传输 → Axios拦截器 → 前端API调用 → 响应数据解包
    → Vue响应式状态 → Computed属性转换 → vue-echarts组件 → ECharts力导向图渲染
```

---

## 二、后端数据构建链路

### 2.1 数据库存储层

血缘数据持久化在 MySQL 的 `lineage_db` 数据库中，涉及 4 张核心表：

| 表名              | 作用                                | 关键字段                                  |
|-------------------|-------------------------------------|-------------------------------------------|
| `data_source`     | 数据源定义（MySQL/PG/Hive等）       | id, name, type, host, port, database       |
| `data_table`      | 数据表元信息                        | id, source_id, table_name, table_comment  |
| `data_column`     | 数据列元信息（预留列级血缘）        | id, table_id, column_name, data_type      |
| `data_lineage`    | 血缘关系边表                        | id, source_table_id, target_table_id, lineage_type, transform_rule |

**初始化数据见**：[init.sql](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/sql/init.sql)

表关系：
```
data_source 1───* data_table 1───* data_column
                      │
                      │ 作为 source_table_id / target_table_id
                      ▼
                  data_lineage
```

---

### 2.2 JPA 实体层（ORM 映射）

每个数据库表对应一个 JPA 实体类，使用 Hibernate 注解完成 ORM 映射：

- **[DataLineage.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/entity/DataLineage.java)**：血缘边实体，映射 `data_lineage` 表
  - 核心字段：`sourceTableId`, `targetTableId`, `sourceColumnId`, `targetColumnId`, `lineageType`, `transformRule`

- **[DataTable.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/entity/DataTable.java)**：表实体，映射 `data_table` 表
  - 核心字段：`id`, `sourceId`, `tableName`, `tableComment`

- **[DataSource.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/entity/DataSource.java)**：数据源实体，映射 `data_source` 表
  - 核心字段：`id`, `name`, `type`

- **[DataColumn.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/entity/DataColumn.java)**：列实体（预留），映射 `data_column` 表

---

### 2.3 Repository 数据访问层

基于 Spring Data JPA，提供声明式查询：

**[DataLineageRepository.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/repository/DataLineageRepository.java)**

关键查询方法：
```java
// 查询某表关联的所有血缘（上游+下游）— 图谱查询核心SQL
@Query("SELECT l FROM DataLineage l WHERE l.sourceTableId = :tableId OR l.targetTableId = :tableId")
List<DataLineage> findAllByTableId(@Param("tableId") Long tableId);
```

其他关联 Repository：
- `DataTableRepository`：提供 `findAllById(Set)`、`findByTableNameContaining()` 批量查表与搜索
- `DataSourceRepository`：提供 `findAllById(Set)` 批量查数据源
- `DataColumnRepository`：预留列级血缘查询

---

### 2.4 Service 层：**关键数据转换节点**

**[LineageService.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/service/LineageService.java)** 中的 `getLineageGraph(Long tableId)` 方法是后端最核心的**图结构组装逻辑**，步骤如下：

**步骤1：查询血缘边记录**
```java
List<DataLineage> lineages = lineageRepository.findAllByTableId(tableId);
```

**步骤2：收集所有关联表ID（起点表 + 血缘中的源表 + 血缘中的目标表）**
```java
Set<Long> tableIds = new HashSet<>();
tableIds.add(tableId);
for (DataLineage lineage : lineages) {
    tableIds.add(lineage.getSourceTableId());
    tableIds.add(lineage.getTargetTableId());
}
```

**步骤3：批量加载表信息与数据源信息，构建 Map 加速关联**
```java
Map<Long, DataTable> tableMap = tableRepository.findAllById(tableIds)
    .stream().collect(Collectors.toMap(DataTable::getId, t -> t));
Map<Long, DataSource> sourceMap = sourceRepository.findAllById(sourceIds)
    .stream().collect(Collectors.toMap(DataSource::getId, s -> s));
```

**步骤4：将 DataTable 实体转换为 GraphNode DTO（节点ID前缀 "table_"）**
```java
GraphNode node = new GraphNode(
    "table_" + table.getId(),      // 节点ID
    table.getTableName(),          // 显示标签
    "table",                       // 节点类型
    table.getId(),                 // 原始表ID
    null,                          // columnId（列级预留）
    sourceName                     // 数据源名称（用于tooltip显示）
);
```

**步骤5：将 DataLineage 实体转换为 GraphEdge DTO（边ID前缀 "lineage_"）**
```java
GraphEdge edge = new GraphEdge(
    "lineage_" + lineage.getId(),
    "table_" + lineage.getSourceTableId(),   // 指向 GraphNode.id
    "table_" + lineage.getTargetTableId(),   // 指向 GraphNode.id
    lineage.getTransformRule(),              // 边标签
    lineage.getLineageType()
);
```

**步骤6：组装为 LineageGraphDTO 返回**
```java
return new LineageGraphDTO(nodes, edges);
```

> **搜索入口**：`searchLineage(String tableName)` 先模糊匹配表名，再委托给 `getLineageGraph()`。

---

### 2.5 DTO 传输对象层

**[LineageGraphDTO.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/dto/LineageGraphDTO.java)** 是前后端契约的核心数据结构：

```
LineageGraphDTO
├── nodes: List<GraphNode>
│   ├── id: String         → "table_{id}" 格式
│   ├── label: String      → 表名
│   ├── type: String       → "table" / "column"
│   ├── tableId: Long
│   ├── columnId: Long
│   └── sourceName: String → 数据源名称
└── edges: List<GraphEdge>
    ├── id: String         → "lineage_{id}" 格式
    ├── source: String     → 对应 GraphNode.id
    ├── target: String     → 对应 GraphNode.id
    ├── label: String      → 转换规则
    └── lineageType: String → "TABLE" / "COLUMN"
```

**[Result.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/dto/Result.java)** 作为统一响应包装：
```json
{ "code": 200, "message": "操作成功", "data": { "nodes": [...], "edges": [...] } }
```

---

### 2.6 Controller 层：API 暴露

**[LineageController.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/controller/LineageController.java)**

| HTTP方法 | 路径                         | 方法                     | 返回类型                |
|----------|------------------------------|--------------------------|-------------------------|
| GET      | `/api/lineage/graph/{tableId}` | `getLineageGraph()`      | `Result<LineageGraphDTO>` |
| GET      | `/api/lineage/search?tableName=` | `searchLineage()`     | `Result<LineageGraphDTO>` |
| GET      | `/api/lineage`               | `getAllLineages()`       | `Result<List<DataLineage>>` |
| POST     | `/api/lineage`               | `createLineage()`        | `Result<DataLineage>`   |
| DELETE   | `/api/lineage/{id}`          | `deleteLineage()`        | `Result<Void>`          |

---

## 三、HTTP 传输链路

### 3.1 跨域与代理

- 后端 [WebConfig.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/config/WebConfig.java) 配置 CORS
- 开发环境通过 Vite 代理转发（[vite.config.dev.js](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/vite.config.dev.js)）：
  ```
  前端 /api/*  → 代理到 http://backend:8080/*
  ```

### 3.2 鉴权

- 请求头携带 JWT：`Authorization: Bearer <token>`
- 由 [JwtAuthenticationFilter.java](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/filter/JwtAuthenticationFilter.java) 解析

---

## 四、前端数据消费链路

### 4.1 Axios 请求封装

**[request.js](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/utils/request.js)**

- baseURL 固定为 `/api`
- **请求拦截器**：自动注入 JWT Token
- **响应拦截器（关键解包节点）**：
  ```javascript
  response => {
    const res = response.data
    if (res.code !== 200) { /* 错误处理 */ }
    return res.data   // ← 直接解包 Result<T>，返回 data 字段，丢弃 code/message
  }
  ```
  因此前端 API 调用拿到的**已经是裸的 `LineageGraphDTO` 对象**，不再有外层 `code/message` 包装。

---

### 4.2 API 调用层

**[api/index.js](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/api/index.js)** 中血缘相关方法：

```javascript
// 获取血缘图谱（按表ID）
export function getLineageGraph(tableId) {
  return request({ url: `/lineage/graph/${tableId}`, method: 'get' })
}

// 按表名搜索血缘
export function searchLineage(tableName) {
  return request({ url: '/lineage/search', method: 'get', params: { tableName } })
}
```

---

### 4.3 Vue 组件状态层

**[Lineage.vue](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/views/Lineage.vue)**

使用 Vue 3 Composition API：

```javascript
// 核心响应式状态，直接存储后端返回的 LineageGraphDTO
const graphData = ref({ nodes: [], edges: [] })
```

**触发搜索时的数据赋值**（[Lineage.vue#L255-L272](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/views/Lineage.vue#L255-L272)）：
```javascript
const handleSearch = async () => {
  searching.value = true
  try {
    graphData.value = await searchLineage(searchText.value)
    // 此时 graphData.value = { nodes: GraphNode[], edges: GraphEdge[] }
  } finally {
    searching.value = false
  }
}
```

---

### 4.4 **关键转换节点：computed 属性 chartOption**

**[Lineage.vue#L168-L222](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/views/Lineage.vue#L168-L222)**

这是前端**最核心的数据转换**，将后端 DTO 映射为 ECharts graph series 所需格式：

**节点转换（GraphNode → ECharts data）**：
```javascript
data: graphData.value.nodes.map(node => ({
  id: node.id,                              // 保留 "table_{id}"
  name: node.label,                         // 显示名称 = 表名
  sourceName: node.sourceName,              // 透传用于tooltip
  symbolSize: 50,                           // 节点大小
  itemStyle: { color: '#5470c6' },          // 蓝色节点
  label: { show: true, position: 'bottom', fontSize: 12 }
}))
```

**边转换（GraphEdge → ECharts edges）**：
```javascript
edges: graphData.value.edges.map(edge => ({
  source: edge.source,                      // 直接使用 node.id 引用
  target: edge.target,
  label: { show: true, formatter: edge.label || '', fontSize: 10 },
  lineStyle: { color: '#91cc75', curveness: 0.2, width: 2 }
}))
```

**图谱布局配置**：
```javascript
layout: 'force',        // 力导向布局
roam: true,             // 支持缩放拖拽
force: { repulsion: 300, edgeLength: 150 },
emphasis: { focus: 'adjacency' }  // 高亮相邻节点
```

**Tooltip 格式化**：
- 节点：显示表名 + 数据源名称
- 边：显示转换规则（transformRule）

---

### 4.5 ECharts 组件注册与渲染

**ECharts 按需引入**（[Lineage.vue#L137-L144](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/views/Lineage.vue#L137-L144)）：
```javascript
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GraphChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
use([CanvasRenderer, GraphChart, TitleComponent, TooltipComponent, LegendComponent])
```

**模板渲染**（[Lineage.vue#L27-L32](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/views/Lineage.vue#L27-L32)）：
```html
<v-chart
  ref="chartRef"
  class="chart"
  :option="chartOption"
  autoresize
/>
```

`autoresize` 保证容器尺寸变化时图表自动重绘，`:option` 绑定 computed 属性，响应式驱动图表更新。

---

## 五、完整数据字段映射对照表

### 5.1 节点字段映射

| 数据库字段          | JPA实体        | DTO GraphNode    | ECharts data       | 用途           |
|---------------------|----------------|------------------|--------------------|----------------|
| `data_table.id`     | `DataTable.id` | `id` ("table_"+) | `id`               | 唯一标识       |
| `data_table.table_name` | `tableName` | `label`          | `name`             | 节点显示文字   |
| 常量 "table"        | -              | `type`           | -                  | 节点类型标记   |
| `data_table.id`     | `id`           | `tableId`        | -                  | 业务ID         |
| `data_source.name`  | -              | `sourceName`     | `sourceName`       | Tooltip显示    |

### 5.2 边字段映射

| 数据库字段              | JPA实体             | DTO GraphEdge     | ECharts edge  | 用途              |
|-------------------------|---------------------|-------------------|---------------|-------------------|
| `data_lineage.id`       | `DataLineage.id`    | `id` ("lineage_"+)| -             | 边标识            |
| `source_table_id`       | `sourceTableId`     | `source` ("table_"+) | `source`   | 边起点（引用节点）|
| `target_table_id`       | `targetTableId`     | `target` ("table_"+) | `target`   | 边终点（引用节点）|
| `transform_rule`        | `transformRule`     | `label`           | `label.formatter` | 边标签文字    |
| `lineage_type`          | `lineageType`       | `lineageType`     | -             | 血缘类型标记      |

---

## 六、数据转换关键节点汇总

共 **6 个关键数据转换节点**：

| # | 节点位置 | 转换内容 | 文件 |
|---|----------|----------|------|
| 1 | **Repository → Service** | SQL查询结果集 → Java实体列表 | [DataLineageRepository.java#L30-L31](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/repository/DataLineageRepository.java#L30-L31) |
| 2 | **Service内** | DataTable/DataLineage/DataSource实体 → GraphNode/GraphEdge DTO（核心图结构组装，加ID前缀） | [LineageService.java#L72-L128](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/service/LineageService.java#L72-L128) |
| 3 | **Controller → HTTP** | DTO → Result包装 → JSON序列化（Jackson） | [LineageController.java#L50-L54](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/backend/src/main/java/com/lineage/controller/LineageController.java#L50-L54) |
| 4 | **Axios响应拦截器** | `Result<LineageGraphDTO>` JSON → 裸DTO对象（解包 `res.data`） | [request.js#L27-L37](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/utils/request.js#L27-L37) |
| 5 | **Vue ref赋值** | API返回Promise resolve → `graphData.value` 响应式状态 | [Lineage.vue#L263](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/views/Lineage.vue#L263) |
| 6 | **computed chartOption** | DTO nodes/edges → ECharts series data/edges（视觉属性注入：颜色、大小、力导向参数） | [Lineage.vue#L182-L209](file:///Users/tog/Desktop/code/gsb/B-595/B-595_autumn/frontend/src/views/Lineage.vue#L182-L209) |

---

## 七、典型一次"搜索血缘"请求时序

```
用户输入表名 → 回车/点击搜索
    ↓
Lineage.vue::handleSearch()
    ↓  await searchLineage(tableName)
api/index.js::searchLineage()
    ↓  request({ url: '/lineage/search' })
request.js (axios, 添加Authorization头)
    ↓  HTTP GET /api/lineage/search?tableName=xxx
    ↓  Vite代理 → backend:8080/lineage/search?tableName=xxx
LineageController::searchLineage()
    ↓
LineageService::searchLineage()
    ↓  tableRepository.findByTableNameContaining()
    ↓  getLineageGraph(tableId)
LineageService::getLineageGraph()
    ↓  lineageRepository.findAllByTableId()
    ↓  tableRepository.findAllById()
    ↓  sourceRepository.findAllById()
    ↓  构建 GraphNode / GraphEdge 列表
Result.success(graphDTO) → JSON序列化
    ↓  HTTP 200 { code:200, data: { nodes:[...], edges:[...] } }
request.js 响应拦截器 → return res.data（解包）
    ↓
graphData.value = { nodes:[...], edges:[...] }
    ↓  Vue响应式触发
chartOption computed 重新计算
    ↓  map转换为ECharts格式
v-chart 组件 watch option → ECharts setOption()
    ↓
Canvas 力导向图渲染到浏览器
```
