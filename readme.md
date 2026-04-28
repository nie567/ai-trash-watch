# Garbage Classification Monitoring System

基于 Java Web (Servlet + JSP + JDBC) 的垃圾分类识别与投放监管系统，集成 DJL 深度学习目标检测微服务，实现垃圾图片智能识别、投放记录管理、违规自动判定、整改闭环处理及统计分析。

## 功能概览

### 用户端
- **垃圾投放识别** — 上传图片或选择本地图片，调用 AI 微服务检测，返回检测结果与推荐投放类别
- **投放记录** — 查看个人投放历史、详情（含原图/结果图/检测明细/违规/整改信息）
- **违规记录** — 查看个人违规记录及状态
- **整改任务** — 查看整改要求、提交整改说明与图片
- **分类知识** — 按类别浏览垃圾分类知识条目

### 管理员端
- **仪表盘** — 用户统计 + 垃圾分类统计概览 + 快捷操作
- **投放记录管理** — 全量记录查看、人工复核（最终类别判定，联动违规/整改状态）
- **违规管理** — 违规记录筛选、发起整改任务
- **整改任务管理** — 复核整改结果（通过/驳回），联动违规状态更新
- **统计分析** — 类别分布饼图、正确率统计、近7天趋势折线图、违规排名
- **分类规则管理** — 检测类别到业务类别的映射规则 CRUD
- **知识库管理** — 分类知识条目 CRUD
- **用户管理** — 用户 CRUD、状态切换

## 系统架构

```
┌─────────────┐     ┌──────────────────────────────────────┐     ┌──────────────┐
│   Browser   │────▶│         Java Web Application         │────▶│  DJL Service  │
│  (JSP/JS)   │◀────│  Servlet + Service + DAO + MySQL     │◀────│  (Port 8080) │
└─────────────┘     └──────────────────────────────────────┘     └──────────────┘
```

### 分层架构

| 层次 | 技术选型 | 说明 |
|------|---------|------|
| 表现层 | JSP + JSTL + CSS + JS | 服务端渲染，ECharts 统计图表 |
| 控制层 | Jakarta Servlet | 请求路由、参数校验、文件上传 |
| 业务层 | Java Service | 规则映射、违规判定、整改流程、级联处理 |
| 数据访问层 | JDBC + HikariCP | 原生 SQL，连接池管理 |
| 安全层 | Filter | 认证/授权/XSS防护/编码过滤 |
| 外部服务 | DJL (Deep Java Library) | 目标检测推理微服务，HTTP REST 通信 |

### 核心业务流程

```
用户上传图片 → AI检测识别 → 规则映射(类别判定) → 用户选择类别
    │                                              │
    ├─ 推荐==选择 → 投放正确                         ├─ 推荐!=选择 → 投放错误
    │                                              │
    │                                              ├─ 自动生成违规记录
    │                                              │
    │                                              ├─ 管理员复核 → 更新违规状态
    │                                              │
    │                                              └─ 发起整改 → 用户提交 → 管理员复核
```

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 21 |
| Web | Jakarta Servlet 6.0 + JSP 3.1 + JSTL 2.0 |
| 数据库 | MySQL 8.0 |
| 连接池 | HikariCP 4.0 |
| 密码加密 | jBCrypt 0.4 |
| JSON | Jackson 2.16 |
| 日志 | SLF4J + Logback |
| AI 推理 | DJL (Deep Java Library) + PyTorch |
| 图表 | ECharts 5 |
| 构建 | Maven |
| 容器 | Tomcat 9.0 / 10.0 |

## 项目结构

```
src/main/java/com/example/
├── controller/          # Servlet 控制层
│   ├── AdminDashboardServlet.java
│   ├── AdminGarbageRecordServlet.java
│   ├── AdminRectificationServlet.java
│   ├── AdminUserServlet.java
│   ├── AdminViolationServlet.java
│   ├── GarbageRecordServlet.java
│   ├── ImageServlet.java
│   ├── InferenceServlet.java
│   ├── KnowledgeServlet.java
│   ├── RectificationServlet.java
│   ├── RuleServlet.java
│   ├── StatisticsServlet.java
│   └── ViolationServlet.java
├── dao/                 # 数据访问层
├── filter/              # 过滤器（认证/授权/XSS/编码）
├── model/               # 实体/VO/DTO
├── service/             # 业务层
│   ├── DjlInferenceClient.java
│   ├── GarbageRecordService.java
│   ├── KnowledgeService.java
│   ├── RectificationService.java
│   ├── RuleService.java
│   ├── StatisticsService.java
│   ├── ViolationService.java
│   └── UserService.java
└── util/                # 工具类

src/main/webapp/
├── css/style.css        # 全局样式
└── WEB-INF/jsp/
    ├── nav-user.jsp      # 用户导航栏
    ├── nav-admin.jsp     # 管理员导航栏
    ├── user/             # 用户端页面
    └── admin/            # 管理员端页面
```

## 数据库设计

共 8 张表，在 MySQL `user_management` 数据库中：

| 表名 | 说明 |
|------|------|
| `user` | 用户表（复用已有） |
| `operation_log` | 操作日志表（复用已有） |
| `garbage_rule` | 分类规则表（className → mappedCategory） |
| `garbage_record` | 投放记录表 |
| `detection_result` | 检测明细表 |
| `violation_record` | 违规记录表 |
| `rectification_task` | 整改任务表 |
| `knowledge_base` | 知识库表 |

初始化脚本：`src/main/resources/init-garbage-tables.sql`

## 快速开始

### 前置条件

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Tomcat 9.0+（主体应用）或 Tomcat 10.0+（微服务）
- DJL 目标检测微服务（[djl-spring-boot-starter-demo](https://github.com/deepjavalibrary/djl-demo)）

### 1. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS user_management DEFAULT CHARSET utf8mb4;
USE user_management;

-- 执行已有schema
source src/main/resources/schema.sql;

-- 执行垃圾分类扩展表
source src/main/resources/init-garbage-tables.sql;
```

### 2. 配置数据库连接

编辑 `src/main/resources/db.properties`：

```properties
db.url=jdbc:mysql://localhost:3306/user_management?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
db.username=root
db.password=your_password
```

### 3. 配置应用常量

编辑 `src/main/java/com/example/util/AppConstants.java`：

```java
// DJL 微服务地址
public static final String DJL_INFERENCE_URL = "http://localhost:8080";

// 图片目录
public static final String DJL_INPUT_DIR = "/path/to/data_set/input";
public static final String DJL_OUTPUT_DIR = "/path/to/data_set/output";
```

### 4. 启动 DJL 微服务

```bash
cd djl-spring-boot-starter-demo
mvn spring-boot:run
# 微服务默认运行在 http://localhost:8080
```

### 5. 构建并部署

```bash
# 构建 WAR
mvn clean package -DskipTests

# 部署到 Tomcat
cp target/user-management.war /path/to/tomcat/webapps/
```

### 6. 访问系统

- 应用地址：`http://localhost:8081/user_management_war_exploded/`
- 默认管理员账号：见 `schema.sql` 初始化数据

## 关键业务逻辑

### 人工复核判定

管理员指定 `finalCategory`（正确类别），与用户的 `selectedCategory` 对比：
- **一致** → 用户正确 → 违规记录标记为 `IGNORED`（误判）
- **不一致** → 用户错误 → 违规记录保持/恢复为 `PENDING`

### 违规自动生成

投放记录保存时，若用户选择类别与推荐类别不同，自动生成违规记录：
- 违规类型：`分类错误` 或 `混投`
- 违规级别：`LOW`（首次）/ `MEDIUM`（混投）/ `HIGH`（累计≥3次）

### 级联删除

删除投放记录时，按顺序级联删除：整改任务 → 违规记录 → 检测明细 → 投放记录

## License

This project is licensed under the MIT License.
