# 数据血缘管理系统

## 🛠 技术栈

### 前端
- **框架**: Vue 3 + Vite 4
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **图表可视化**: ECharts (Vue-ECharts)
- **路由**: Vue Router 4

### 后端
- **框架**: Spring Boot 2.7.18 (Java 8)
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA (Hibernate)
- **安全**: Spring Security + JWT
- **API文档**: Swagger (Springfox)
- **构建工具**: Maven 3.9

### 数据库
- **类型**: MySQL 8.0
- **数据持久化**: Docker Volume

## 🚀 启动指南 (How to Run)

### 前置要求
- 已安装 Docker Desktop 并确保其正在运行
- Docker Compose 已安装
- 测试账号需要注册

### 启动步骤

1. 克隆或下载项目到本地
2. 在项目根目录执行：
```bash
docker compose up -d --build
```
3. 等待容器构建和启动完成（首次启动需要下载依赖）。

**访问地址:**
- **前端页面**: [http://localhost:3595](http://localhost:3595)
- **后端接口**: [http://localhost:8595](http://localhost:8595)
- **Swagger文档**: [http://localhost:8595/swagger-ui/index.html](http://localhost:8595/swagger-ui/index.html)
- **数据库**: `localhost:13595` (root/root123)

## 🔗 服务地址 (Services)

### 服务访问
- **前端页面**: [http://localhost:3595](http://localhost:3595)
- **后端接口**: [http://localhost:8595](http://localhost:8595)
- **Swagger文档**: [http://localhost:8595/swagger-ui/index.html](http://localhost:8595/swagger-ui/index.html)
- **数据库**: `localhost:13595` (root/root123)

## 📋 系统功能

### 1. 系统管理
- ✅ 用户登录 / 注册
- ✅ JWT令牌认证
- ✅ 权限管理（ADMIN / USER）

### 2. 工作台 / 看板
- ✅ 数据源统计
- ✅ 数据表统计
- ✅ 血缘关系统计
- ✅ 用户统计
- ✅ 系统功能概览

### 3. 数据源管理
- ✅ 数据源CRUD操作
- ✅ 支持多种数据库类型（MySQL、PostgreSQL、Oracle、Hive）
- ✅ 连接测试功能
- ✅ 数据源状态管理

### 4. 元数据管理
- ✅ 数据表列表展示
- ✅ 表详情查看（包含列信息）
- ✅ 表级搜索功能
- ✅ 数据表CRUD操作
- ✅ 可展开查看列详情

### 5. 血缘可视化
- ✅ 血缘关系图谱展示（基于ECharts力导向图）
- ✅ 表名搜索血缘关系
- ✅ 交互式图谱（缩放、拖拽）
- ✅ 血缘关系列表管理
- ✅ 创建和删除血缘关系
- ✅ 支持表级和列级血缘

## 📁 项目结构

```
595/
├── backend/                 # Spring Boot后端
│   ├── src/
│   │   └── main/
│   │       ├── java/com/lineage/
│   │       │   ├── controller/       # 控制器层
│   │       │   ├── service/          # 服务层
│   │       │   ├── repository/       # 数据访问层
│   │       │   ├── entity/           # 实体类
│   │       │   ├── dto/              # 数据传输对象
│   │       │   ├── config/           # 配置类
│   │       │   ├── filter/           # 过滤器
│   │       │   ├── util/             # 工具类
│   │       │   └── exception/        # 异常处理
│   │       └── resources/
│   │           └── application.yml   # 配置文件
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # Vue前端
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── layout/          # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia状态管理
│   │   ├── api/             # API接口
│   │   └── utils/           # 工具函数
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.js
├── sql/
│   └── init.sql             # 数据库初始化脚本
└── docker-compose.yml       # Docker编排配置
```

## 🎨 界面特性

- ✨ 现代化的渐变色设计
- 🎯 响应式布局，支持不同屏幕尺寸
- 📊 数据可视化图表
- 🔄 流畅的交互动画
- 🎭 优雅的暗色侧边栏
- 💎 Element Plus组件库，UI美观统一

## 🔧 开发说明

### 停止服务
```bash
docker compose down
```

### 查看日志
```bash
docker compose logs -f
```

### 重新构建
```bash
docker compose up --build --force-recreate
```

### 清理数据
```bash
docker compose down -v  # 删除所有容器和数据卷
```

## 📝 注意事项

1. 首次启动时会自动初始化数据库并导入示例数据
2. 前端通过Nginx反向代理访问后端API
3. 数据持久化在Docker Volume中，停止容器不会丢失数据
4. 建议使用Chrome或Edge浏览器以获得最佳体验

## 🚨 常见问题

**Q: 前端无法连接后端？**  
A: 确保所有容器都已启动，可通过 `docker compose ps` 查看状态

**Q: 数据库连接失败？**  
A: 等待MySQL容器健康检查通过后，后端会自动连接

**Q: 端口被占用？**  
A: 修改 `docker-compose.yml` 中的端口映射，如将 `3595:80` 改为其他空闲端口。

**Q: Maven依赖下载慢？**  
A: 后端已配置专门的 `settings.xml` 使用阿里云镜像加速，并采用了 Docker 分层缓存策略，首次下载后，后续代码变更的构建将非常快速。

## 📄 开源协议

MIT License

---

**Author**: Data Lineage Team  
**Version**: 1.0.0  
**Last Updated**: 2026-01-21
