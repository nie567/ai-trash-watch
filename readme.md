# AI-TrashWatch

> 基于 Java Web (Servlet + JSP + JDBC) 的垃圾分类 AI 识别与投放监管平台

AI-TrashWatch 通过集成 DJL 深度学习推理微服务，加载**自定义 YOLOv8 垃圾分类模型**，实现对 6 类生活垃圾的智能识别，并结合投放记录管理、违规自动判定、整改闭环处理及统计分析，构建从识别到监管的完整流程。

## ✨ 核心特性

- **🤖 AI 智能识别** — 集成 DJL + YOLOv8 自定义模型，支持 6 类垃圾检测
- **🚮 投放记录管理** — 完整记录每次投放，含原始图片与检测结果图对比
- **⚖️ 违规自动判定** — 用户选择与 AI 推荐不一致时自动生成违规记录，支持分级判定
- **🔄 整改闭环处理** — 发起整改 → 用户提交 → 管理员复核，全流程追溯
- **📊 统计分析** — ECharts 可视化：类别分布、正确率、7 天趋势、违规排名
- **🔧 分类规则管理** — 检测类别到业务类别的映射规则，可灵活配置
- **📚 分类知识库** — 按类别展示垃圾分类知识，支持 CRUD 管理
- **🔐 安全防护** — CSRF 令牌、XSS 过滤、BCrypt 密码加密、认证授权过滤链
- **👤 用户体系** — 注册/登录/个人中心/密码修改，支持管理员与普通用户双角色
- **📝 操作日志** — 关键操作自动记录，支持审计追溯

## 🎯 AI 模型识别能力

系统搭载**自定义训练的 YOLOv8s 垃圾分类模型**，可识别 6 类常见生活垃圾：

| 类别 | 英文标识 | 示例物品 |
|------|----------|----------|
| 🟤 可生物降解 | BIODEGRADABLE | 厨余、果皮、树叶 |
| 📦 纸板 | CARDBOARD | 快递纸箱、纸盒 |
| 🟢 玻璃 | GLASS | 玻璃瓶、碎玻璃 |
| 🔩 金属 | METAL | 易拉罐、金属瓶盖 |
| 📄 纸张 | PAPER | 报纸、打印纸、书籍 |
| 🧴 塑料 | PLASTIC | 塑料瓶、塑料袋 |

> 模型置信度阈值可配置（默认 0.5），识别结果支持人工复核修正。

## 🏗️ 系统架构

![系统架构图](docs/architecture.svg)

### 分层架构

| 层次 | 技术选型 | 说明 |
|------|---------|------|
| 表现层 | JSP + JSTL + CSS + JS | 服务端渲染，ECharts 统计图表，暗色主题 |
| 控制层 | Jakarta Servlet | 请求路由、参数校验、文件上传、JSON API |
| 业务层 | Java Service | 规则映射、违规判定、整改流程、级联处理、操作日志 |
| 数据访问层 | JDBC + HikariCP | 原生 SQL，连接池管理，分页查询 |
| 安全层 | Filter + CSRF Token | 认证/授权/XSS 防护/CSRF 防护/编码过滤 |
| 配置层 | AppConfig + AppContext | 外部属性加载，应用全局上下文 |
| 推理服务 | DJL + YOLOv8 (Spring Boot) | 目标检测推理，HTTP REST 通信 |

### 核心业务流程

![核心业务流程图](docs/flowchart.svg)

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 21 |
| Web | Jakarta Servlet 6.0 + JSP 3.1 + JSTL 2.0 |
| 数据库 | MySQL 8.0 |
| 连接池 | HikariCP 4.0 |
| 密码加密 | jBCrypt 0.4 |
| JSON | Jackson 2.16 |
| 日志 | SLF4J 1.7 + Logback 1.2（含敏感信息脱敏） |
| 测试 | JUnit 4.13 + Mockito 4.11 |
| AI 推理 | DJL (Deep Java Library) + YOLOv8 自定义模型 |
| 图表 | ECharts 5 + Day.js |
| 构建 | Maven |
| 容器 | Tomcat 10.0+（Jakarta EE） |

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── config/                    # 日志脱敏配置
│   │   │   └── MaskingPatternLayout.java
│   │   ├── controller/                # Servlet 控制层（20 个）
│   │   │   ├── AdminDashboardServlet.java
│   │   │   ├── AdminGarbageRecordServlet.java
│   │   │   ├── AdminRectificationServlet.java
│   │   │   ├── AdminUserServlet.java
│   │   │   ├── AdminViolationServlet.java
│   │   │   ├── FileUploadServlet.java        # 文件上传 API
│   │   │   ├── GarbageRecordServlet.java
│   │   │   ├── ImageServlet.java             # 图片代理/展示
│   │   │   ├── InferenceServlet.java         # AI 推理入口
│   │   │   ├── KnowledgeServlet.java
│   │   │   ├── LoginServlet.java             # 登录
│   │   │   ├── LogoutServlet.java            # 登出
│   │   │   ├── PasswordServlet.java          # 密码修改
│   │   │   ├── RectificationServlet.java
│   │   │   ├── RegisterServlet.java          # 注册
│   │   │   ├── RuleServlet.java
│   │   │   ├── StatisticsServlet.java
│   │   │   ├── UserProfileServlet.java       # 个人中心
│   │   │   ├── UserServlet.java
│   │   │   └── ViolationServlet.java
│   │   ├── dao/                       # 数据访问层（9 个）
│   │   │   ├── DetectionResultDAO.java
│   │   │   ├── GarbageRecordDAO.java
│   │   │   ├── GarbageRuleDAO.java
│   │   │   ├── KnowledgeBaseDAO.java
│   │   │   ├── OperationLogDAO.java
│   │   │   ├── RectificationTaskDAO.java
│   │   │   ├── StatisticsDAO.java
│   │   │   ├── UserDAO.java
│   │   │   └── ViolationRecordDAO.java
│   │   ├── filter/                    # 过滤器（6 个）
│   │   │   ├── AdminFilter.java              # 管理员权限
│   │   │   ├── AppContextListener.java       # 应用启动/关闭
│   │   │   ├── AuthFilter.java               # 认证检查
│   │   │   ├── CsrfFilter.java               # CSRF 防护
│   │   │   ├── XssFilter.java                # XSS 过滤入口
│   │   │   └── XssRequestWrapper.java        # XSS 请求包装
│   │   ├── model/                     # 实体/VO/DTO（15 个）
│   │   │   ├── DetectionResult.java          # 检测结果实体
│   │   │   ├── DetectionResultDTO.java       # 检测结果传输对象
│   │   │   ├── GarbageRecord.java            # 投放记录实体
│   │   │   ├── GarbageRecordDetailVO.java    # 投放详情视图
│   │   │   ├── GarbageRecordSubmitDTO.java   # 投放提交传输
│   │   │   ├── GarbageRule.java              # 分类规则实体
│   │   │   ├── KnowledgeBase.java            # 知识库实体
│   │   │   ├── OperationLog.java             # 操作日志实体
│   │   │   ├── PageResult.java               # 通用分页结果
│   │   │   ├── RectificationTask.java        # 整改任务实体
│   │   │   ├── TrendVO.java                  # 趋势视图对象
│   │   │   ├── TypeCountVO.java              # 类别统计视图
│   │   │   ├── User.java                     # 用户实体
│   │   │   ├── UserRankVO.java               # 用户排名视图
│   │   │   └── ViolationRecord.java          # 违规记录实体
│   │   ├── service/                   # 业务层（10 个）
│   │   │   ├── DjlInferenceClient.java       # DJL 推理客户端
│   │   │   ├── GarbageRecordService.java
│   │   │   ├── InferenceResult.java          # 推理结果封装
│   │   │   ├── KnowledgeService.java
│   │   │   ├── OperationLogService.java      # 操作日志服务
│   │   │   ├── RectificationService.java
│   │   │   ├── RuleService.java
│   │   │   ├── StatisticsService.java
│   │   │   ├── UserService.java
│   │   │   └── ViolationService.java
│   │   └── util/                      # 工具类（10 个）
│   │       ├── AppConfig.java                # 外部配置加载
│   │       ├── AppConstants.java             # 应用常量
│   │       ├── AppContext.java               # 应用全局上下文
│   │       ├── BCryptUtil.java               # BCrypt 工具
│   │       ├── BusinessException.java        # 业务异常
│   │       ├── CharacterEncodingFilter.java  # 编码过滤器
│   │       ├── CsrfTokenUtil.java            # CSRF 令牌工具
│   │       ├── DBUtil.java                   # 数据库连接工具
│   │       ├── RequestUtil.java              # 请求工具
│   │       └── Result.java                   # 统一 JSON 响应
│   ├── resources/
│   │   ├── app.properties                   # 应用配置（DJL 地址/图片路径）
│   │   ├── db.properties.example            # 数据库配置模板
│   │   ├── logback.xml                      # 日志配置（含脱敏规则）
│   │   └── schema.sql                       # 数据库初始化脚本
│   └── webapp/
│       ├── css/style.css                    # 全局暗色主题样式
│       ├── index.jsp                        # 首页入口
│       ├── js/                              # 前端 JS 模块（IIFE 封装）
│       │   ├── common.js                    # 公共工具（apiFetch/showToast/confirmDialog）
│       │   ├── dashboard.js                 # 仪表盘图表
│       │   ├── garbage-detail.js            # 投放详情交互
│       │   ├── garbage-inference.js         # AI 推理交互
│       │   ├── garbage-record-detail.js     # 管理员投放详情
│       │   ├── garbage-upload.js            # 投放上传
│       │   ├── knowledge-list.js            # 知识库列表
│       │   ├── rectification-detail.js      # 整改详情
│       │   ├── rectification-list.js        # 整改列表
│       │   ├── rule-list.js                 # 规则管理
│       │   ├── statistics.js                # 统计图表
│       │   ├── user-list.js                 # 用户管理
│       │   ├── violation-list.js            # 违规列表
│       │   └── lib/                         # 第三方库
│       │       ├── dayjs.min.js
│       │       └── echarts.min.js
│       └── WEB-INF/
│           ├── web.xml
│           └── jsp/
│               ├── login.jsp                # 登录页
│               ├── error.jsp                # 通用错误页
│               ├── nav-user.jsp             # 用户导航栏
│               ├── nav-admin.jsp            # 管理员导航栏
│               ├── user-form.jsp            # 用户表单片段
│               ├── user-list.jsp            # 用户列表片段
│               ├── error/                   # 错误页面
│               │   ├── 403.jsp
│               │   ├── 404.jsp
│               │   └── 500.jsp
│               ├── admin/                   # 管理员端页面
│               │   ├── dashboard.jsp        # 仪表盘
│               │   ├── statistics.jsp       # 统计分析
│               │   ├── user-list.jsp        # 用户管理
│               │   ├── user-create.jsp      # 创建用户
│               │   ├── user-edit.jsp        # 编辑用户
│               │   ├── user-form.jsp        # 用户表单
│               │   ├── garbage-record-list.jsp  # 投放记录
│               │   ├── garbage-record-detail.jsp # 投放详情
│               │   ├── violation-list.jsp   # 违规记录
│               │   ├── rectification-list.jsp   # 整改任务
│               │   ├── rule-list.jsp        # 分类规则
│               │   └── knowledge-list.jsp   # 知识库
│               └── user/                    # 用户端页面
│                   ├── garbage-upload.jsp   # 投放上传
│                   ├── garbage-detail.jsp   # 投放详情
│                   ├── garbage-history.jsp  # 投放历史
│                   ├── violation-list.jsp   # 我的违规
│                   ├── rectification-list.jsp   # 我的整改
│                   ├── rectification-detail.jsp # 整改详情
│                   ├── knowledge-list.jsp   # 分类知识
│                   ├── profile.jsp          # 个人信息
│                   ├── profile-edit.jsp     # 编辑信息
│                   └── password.jsp         # 修改密码
└── test/
    └── java/com/example/test/         # 单元测试（38 个）
        ├── BaseTest.java              # 测试基类
        ├── config/                    # 配置测试
        ├── dao/                       # DAO 层测试（9 个）
        ├── filter/                    # 过滤器测试
        ├── integration/               # 集成测试
        ├── model/                     # 模型测试
        ├── service/                   # 业务层测试（10 个）
        └── util/                      # 工具类测试
```

## 🗄️ 数据库设计

共 8 张表，运行于 MySQL `AI-TrashWatch` 数据库中：

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `user` | 用户表 | username, password_hash, role, status |
| `operation_log` | 操作日志表 | user_id, action, target, ip |
| `garbage_rule` | 分类规则表 | class_name → mapped_category |
| `garbage_record` | 投放记录表 | user_id, image_path, recommended/selected/final_category |
| `detection_result` | 检测明细表 | record_id, class_name, confidence, bbox |
| `violation_record` | 违规记录表 | record_id, user_id, type, level, status |
| `rectification_task` | 整改任务表 | violation_id, user_id, deadline, status |
| `knowledge_base` | 知识库表 | title, garbage_type, content |

**表关系：** `user` ← `operation_log` / `garbage_record` ← `detection_result` ← `violation_record` ← `rectification_task`，级联删除。

初始化脚本：`src/main/resources/schema.sql`

## 🚀 快速开始

### 前置条件

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Tomcat 10.0+（Jakarta EE 10 兼容）
- DJL 目标检测微服务（[djl-spring-boot-starter-demo](https://github.com/deepjavalibrary/djl-demo)）

### 1. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS `AI-TrashWatch` DEFAULT CHARSET utf8mb4;
USE `AI-TrashWatch`;

-- 执行完整初始化脚本（含建表、默认规则、管理员账号）
source src/main/resources/schema.sql;
```

默认管理员：`admin` / `admin123`

### 2. 配置数据库连接

复制模板并编辑 `src/main/resources/db.properties`：

```properties
db.url=jdbc:mysql://localhost:3306/AI-TrashWatch?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
db.username=root
db.password=your_password
```

> ⚠️ `db.properties` 未纳入版本控制，请参考 `db.properties.example` 创建。

### 3. 配置应用常量

编辑 `src/main/resources/app.properties`：

```properties
# DJL 推理服务地址
djl.inference.url=http://localhost:8080

# 图片目录
djl.input.dir=/path/to/data_set/input
djl.output.dir=/path/to/data_set/output
```

### 4. 启动 DJL 推理微服务

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
cp target/AI-TrashWatch.war /path/to/tomcat/webapps/
```

或使用 Maven 热部署：

```bash
mvn tomcat7:redeploy
```

### 6. 访问系统

- 应用地址：`http://localhost:8081/AI-TrashWatch/`
- 默认管理员账号：`admin` / `admin123`

## 🔒 安全机制

| 安全措施 | 实现方式 | 说明 |
|---------|---------|------|
| 密码加密 | BCrypt（10 轮哈希） | 注册/登录均使用 BCrypt 验证 |
| 认证过滤 | AuthFilter | 未登录请求重定向至登录页 |
| 授权过滤 | AdminFilter | 管理员页面访问控制 |
| CSRF 防护 | CsrfFilter + CsrfTokenUtil | 表单提交验证令牌，常量时间比较 |
| XSS 过滤 | XssFilter + XssRequestWrapper | 请求参数 HTML 转义 |
| 编码过滤 | CharacterEncodingFilter | 统一 UTF-8 编码 |
| 敏感日志脱敏 | MaskingPatternLayout | 密码、手机号等敏感字段自动脱敏 |
| 环境配置隔离 | db.properties / app.properties | 敏感配置不入版本控制 |

## 🔍 关键业务逻辑

### 人工复核判定

管理员指定 `finalCategory`（正确类别），与用户的 `selectedCategory` 对比：
- **一致** → 用户正确 → 违规记录标记为 `IGNORED`（误判）
- **不一致** → 用户错误 → 违规记录保持/恢复为 `PENDING`

### 违规自动生成

投放记录保存时，若用户选择类别与 AI 推荐类别不同，自动生成违规记录：
- 违规类型：`分类错误` 或 `混投`
- 违规级别：`LOW`（首次）/ `MEDIUM`（混投）/ `HIGH`（累计 ≥ 3 次）

### 整改闭环流程

```
管理员发起整改 → 用户查看并提交整改说明/图片 → 管理员审核（通过/驳回）
```

状态流转：`PENDING` → `SUBMITTED` → `REVIEWED`

### 级联删除

删除投放记录时，按顺序级联删除：整改任务 → 违规记录 → 检测明细 → 投放记录

### 操作日志

关键操作（登录、注册、投放、违规判定、整改审核等）自动记录操作人、操作类型、目标对象及 IP 地址。

## 🧪 测试

项目包含 38 个单元/集成测试，覆盖各层：

```bash
# 运行全部测试
mvn test

# 运行指定层测试
mvn test -Dtest="com.example.test.service.*"
mvn test -Dtest="com.example.test.dao.*"
mvn test -Dtest="com.example.test.filter.*"
```

| 测试分类 | 数量 | 覆盖范围 |
|---------|------|---------|
| DAO 层 | 9 | 全部 DAO 的 CRUD 操作 |
| Service 层 | 10 | 业务逻辑、安全验证、违规判定 |
| Filter 层 | 2 | XSS 过滤、安全逻辑 |
| Integration | 2 | 投放流程、整改流程端到端 |
| Model | 4 | 常量、异常、分页、模型 |
| Util | 8 | 配置、加密、令牌、请求工具 |
| Config | 1 | 日志脱敏配置 |

## 📦 依赖清单

| 依赖 | 版本 | 用途 |
|------|------|------|
| jakarta.servlet-api | 6.0.0 | Servlet API |
| jakarta.servlet.jsp-api | 3.1.0 | JSP API |
| jakarta.servlet.jsp.jstl | 2.0.0 | JSTL 标签库 |
| mysql-connector-java | 8.0.33 | MySQL 驱动 |
| HikariCP | 4.0.3 | 连接池 |
| jbcrypt | 0.4 | 密码哈希 |
| jackson-databind | 2.16.1 | JSON 序列化 |
| slf4j-api | 1.7.36 | 日志门面 |
| logback-classic | 1.2.11 | 日志实现 |
| junit | 4.13.2 | 单元测试 |
| mockito-core | 4.11.0 | Mock 框架 |

## 🌐 仓库地址

| 平台 | 地址 |
|------|------|
| GitHub | https://github.com/nie567/ai-trash-watch |
| Gitee | https://gitee.com/Nie-----------Ying/ai-trash-watch |

## 📄 License

本项目基于 MIT License 开源。
