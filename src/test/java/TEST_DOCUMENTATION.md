# 垃圾分类识别与投放监管系统 - 测试文档

## 1. 概述

本测试套件基于需求文档（spec.md）和任务文档（tasks.md）编写，覆盖垃圾分类识别与投放监管系统的核心功能。测试框架采用 **JUnit 4.13.2**，测试策略按经典三层架构分层覆盖：Model → DAO → Service → Integration。

### 技术栈

| 项目 | 版本/说明 |
|------|-----------|
| 测试框架 | JUnit 4.13.2 |
| 编译 | Maven + Java 21 |
| 数据库 | MySQL 8.0（真实数据库，非Mock） |
| 运行命令 | `mvn test` |

### 测试统计

| 分类 | 测试类数量 | 说明 |
|------|-----------|------|
| DAO 层 | 8 | 数据访问层CRUD及查询 |
| Service 层 | 6 | 业务逻辑及参数校验 |
| Model 层 | 4 | 实体类、工具类、常量 |
| 集成测试 | 2 | 端到端业务流程 |
| **合计** | **20** | 不含BaseTest基类 |

---

## 2. 目录结构

```
src/test/java/com/example/test/
├── BaseTest.java                          # 测试基类，提供DB连接与数据初始化
├── dao/
│   ├── GarbageRecordDAOTest.java          # 投放记录DAO
│   ├── GarbageRuleDAOTest.java            # 分类规则DAO
│   ├── ViolationRecordDAOTest.java        # 违规记录DAO
│   ├── RectificationTaskDAOTest.java      # 整改任务DAO
│   ├── DetectionResultDAOTest.java        # 检测明细DAO
│   ├── KnowledgeBaseDAOTest.java          # 知识库DAO
│   ├── UserDAOTest.java                   # 用户DAO
│   └── StatisticsDAOTest.java             # 统计DAO
├── service/
│   ├── RuleServiceTest.java               # 分类规则Service
│   ├── ViolationServiceTest.java          # 违规Service
│   ├── RectificationServiceTest.java      # 整改Service
│   ├── GarbageRecordServiceTest.java      # 投放记录Service
│   ├── KnowledgeServiceTest.java          # 知识库Service
│   └── StatisticsServiceTest.java         # 统计Service
├── model/
│   ├── ModelTest.java                     # 所有实体类getter/setter
│   ├── PageResultTest.java                # 分页结果封装
│   ├── BusinessExceptionTest.java         # 业务异常类
│   └── AppConstantsTest.java              # 应用常量
└── integration/
    ├── GarbageDisposalIntegrationTest.java # 垃圾投放全流程集成
    └── RectificationFlowIntegrationTest.java # 整改闭环全流程集成
```

---

## 3. 测试基类

### BaseTest.java

所有DAO/Service/集成测试的基类，提供：

- `@Before setUp()`：获取数据库连接，调用 `initTestData()` 初始化测试数据
- `@After tearDown()`：调用 `cleanTestData()` 清理数据，关闭连接
- `executeSQL(sql)`：执行任意SQL语句
- `truncateTable(tableName)`：清空指定表
- `deleteFromTable(tableName, condition)`：按条件删除

子类通过重写 `initTestData()` 和 `cleanTestData()` 定制数据生命周期。

---

## 4. DAO 层测试

### 4.1 GarbageRecordDAOTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testInsert` | 新增投放记录，验证返回ID及字段 |
| `testFindById` | 按ID查询（存在/不存在） |
| `testFindByUserId` | 按用户ID分页查询，验证归属和分页 |
| `testFindAll` | 管理员分页查询，支持状态和关键词筛选 |
| `testCountByUserId` | 按用户统计记录数 |
| `testCountAll` | 管理员统计总记录数（含筛选） |
| `testUpdateReviewResult` | 更新复核结果（finalCategory、isCorrect、status、reviewComment） |

**测试数据**：2个用户，3条投放记录（2条PENDING，1条REVIEWED）

### 4.2 GarbageRuleDAOTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testFindAll` | 查询所有规则（含禁用） |
| `testFindAllEnabled` | 仅查询启用规则 |
| `testFindByClassName` | 按类名查询（启用/禁用/不存在） |
| `testInsert` | 新增规则 |
| `testUpdate` | 更新规则字段和状态 |
| `testInsertDuplicate` | 重复className插入应失败（UNIQUE约束） |

**测试数据**：4条规则（METAL/PLASTIC/PAPER启用，GLASS禁用）

### 4.3 ViolationRecordDAOTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testInsert` | 插入违规记录，验证返回ID |
| `testFindById` | 按ID查询（存在/不存在） |
| `testFindByRecordId` | 按投放记录ID查询违规 |
| `testFindByUserId` | 按用户ID分页查询 |
| `testFindAllWithStatus` | 管理员分页查询，支持状态筛选 |
| `testCountByUserId` | 按用户统计违规数 |
| `testCountAll` | 管理员统计违规数（含筛选） |
| `testUpdateStatus` | 更新违规状态 |
| `testCountByUserIdAll` | 统计用户历史总违规次数（所有状态） |
| `testDeleteById` | 按ID删除（成功/重复删除） |

**测试数据**：2个用户，3条违规（2条PENDING，1条RECTIFIED）

### 4.4 RectificationTaskDAOTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testInsert` | 插入整改任务 |
| `testFindById` | 按ID查询 |
| `testFindByViolationId` | 按违规ID查询 |
| `testFindByUserId` | 按用户ID分页查询 |
| `testFindAllWithStatus` | 管理员分页查询，支持状态筛选 |
| `testCountByUserId` / `testCountAll` | 统计 |
| `testSubmit` | 用户提交整改（更新submitDesc、submitImagePath、status） |
| `testReview` | 管理员复核整改（更新reviewResult、reviewComment、status） |
| `testDeleteById` | 按ID删除 |

**测试数据**：2条整改任务（1条PENDING，1条SUBMITTED）

### 4.5 DetectionResultDAOTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testBatchInsert` | 批量插入检测明细 |
| `testBatchInsertEmpty` | 空列表/null列表不报错 |
| `testFindByRecordId` | 按投放记录ID查询 |
| `testDeleteByRecordId` | 按投放记录ID删除 |
| `testBatchInsertWithNullValues` | null的confidence/坐标字段处理 |

**测试数据**：recordId=1有2条明细，recordId=2有1条明细

### 4.6 KnowledgeBaseDAOTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testFindAll` | 查询所有知识条目 |
| `testFindByType` | 按垃圾类型查询 |
| `testInsert` / `testUpdate` / `testDelete` | CRUD操作 |
| `testDeleteNonExistent` | 删除不存在的ID返回false |

**测试数据**：4条知识（2条可回收物，1条厨余垃圾，1条有害垃圾）

### 4.7 UserDAOTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testFindByUsername` | 按用户名查询 |
| `testLogin` | 登录验证（正确密码/错误密码/不存在用户） |
| `testFindById` | 按ID查询 |
| `testExistsByUsername` / `testExistsByEmail` | 唯一性检查 |
| `testCreate` | 创建用户（含BCrypt密码） |
| `testUpdateProfile` | 更新资料（email、phone） |
| `testUpdatePassword` | 更新密码并验证新密码可登录 |
| `testUpdateStatus` | 更新用户状态 |
| `testCountAll` / `testCountByRole` | 统计 |
| `testSearch` | 关键词搜索 |
| `testDelete` | 删除用户 |

**测试数据**：3个测试用户（2个user+1个admin），使用BCrypt加密密码，测试后自动清理

### 4.8 StatisticsDAOTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testCountByGarbageType` | 按垃圾分类统计投放数量 |
| `testCountCorrectAndWrong` | 统计正确/错误投放数 |
| `testCountByDate` | 近7天投放趋势 |
| `testCountByDateWithZeroDays` | 边界值：0天 |
| `testGetViolationUserRank` | 违规用户排名 |

**测试数据**：5条投放记录（4正确1错误），覆盖可回收物/厨余垃圾/有害垃圾/其他垃圾

---

## 5. Service 层测试

### 5.1 RuleServiceTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testMapCategory` | 类别映射（启用/禁用/不存在/null/空串/trim） |
| `testGetAllRuleMap` | 获取所有启用规则映射 |
| `testListRules` | 查询所有规则（含禁用） |
| `testSaveRuleInsert` | 新增规则 |
| `testSaveRuleUpdate` | 更新规则 |
| `testSaveRuleDuplicate` | 重复className应抛异常 |
| `testSaveRuleEmptyClassName` / `testSaveRuleEmptyMappedCategory` | 空字段校验 |

### 5.2 ViolationServiceTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testCreateViolationForClassificationError` | 分类错误→生成违规（类型=分类错误） |
| `testCreateViolationForMixedDisposal` | 混投→生成违规（类型=混投，级别=MEDIUM） |
| `testNoViolationForCorrectDisposal` | 正确投放→不生成违规 |
| `testGetUserViolations` / `testGetAllViolations` | 分页查询 |
| `testGetById` | 按ID查询（含null检查） |
| `testViolationLevelDetermination` | 违规级别判定逻辑（首次LOW，混投MEDIUM，累计3次HIGH） |

### 5.3 RectificationServiceTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testCreateTask` | 创建整改任务 |
| `testCreateTaskNullViolationId` | violationId为空→400异常 |
| `testCreateTaskEmptyRequirement` | requirement为空→400异常 |
| `testCreateTaskNonExistentViolation` | 违规不存在→404异常 |
| `testCreateDuplicateTask` | 重复创建（未完成）→400异常 |
| `testSubmitRectification` | 用户提交整改 |
| `testSubmitNullTaskId` / `testSubmitEmptyDesc` | 参数校验 |
| `testReviewTaskApproved` | 复核通过→任务APPROVED + 违规RECTIFIED |
| `testReviewTaskRejected` | 复核驳回→任务REJECTED |
| `testReviewNotSubmittedTask` | PENDING状态不允许复核→400异常 |
| `testGetUserTasks` / `testGetAllTasks` / `testGetById` | 查询 |

### 5.4 GarbageRecordServiceTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testSaveRecordCorrect` | 正确投放→isCorrect=1，无违规 |
| `testSaveRecordIncorrect` | 错误投放→isCorrect=0，自动生成违规 |
| `testSaveRecordWithDetections` | 保存投放记录+检测明细 |
| `testSaveRecordNullUserId` / `testSaveRecordNullSelectedCategory` | 参数校验→400 |
| `testGetUserRecords` / `testGetAllRecords` | 分页查询 |
| `testGetRecordDetail` | 详情查询（含检测明细） |
| `testGetRecordDetailNonExistent` | 不存在→404 |
| `testReviewRecordCorrect` | 复核确认正确→isCorrect=1，违规IGNORED |
| `testReviewRecordIncorrect` | 复核确认错误→isCorrect=0 |
| `testDeleteRecord` | 级联删除（记录+检测明细） |
| `testDeleteRecordNonExistent` | 不存在→404 |

### 5.5 KnowledgeServiceTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testListAll` / `testListByType` | 查询 |
| `testListByTypeNullReturnsAll` | null/空类型→返回全部 |
| `testSaveNew` / `testSaveUpdate` | 新增和更新 |
| `testSaveEmptyTitle` / `testSaveEmptyGarbageType` | 参数校验→400 |
| `testDelete` / `testDeleteNullId` | 删除及参数校验 |

### 5.6 StatisticsServiceTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testCountByGarbageType` / `testCountCorrectAndWrong` | 统计接口 |
| `testCountByDate` / `testCountByDateZeroOrNegative` | 趋势及边界值 |
| `testGetUserViolationRank` | 违规排名 |

---

## 6. Model 层测试

### 6.1 ModelTest

验证所有实体类的 getter/setter 正确性：

- `GarbageRecord`：17个字段
- `ViolationRecord`：7个字段
- `RectificationTask`：11个字段
- `DetectionResult`：10个字段
- `KnowledgeBase`：6个字段
- `GarbageRule`：5个字段
- `User`：6个核心字段

### 6.2 PageResultTest

| 测试方法 | 测试内容 |
|---------|---------|
| `testConstructor` | 构造函数初始化 |
| `testTotalPagesCeiling` | 总页数向上取整 |
| `testHasNextAndPrevious` | 翻页判断逻辑 |
| `testDefaultConstructor` | 默认构造 |
| `testSetters` | setter正确性 |

### 6.3 BusinessExceptionTest

覆盖所有5个构造函数 + `setCode()`，验证 `code` 和 `message` 正确设置。

### 6.4 AppConstantsTest

验证所有业务常量值：

- 垃圾类别（可回收物/厨余垃圾/有害垃圾/其他垃圾/混合待分拣）
- 记录状态（PENDING/REVIEWED）
- 违规状态（PENDING/RECTIFIED/IGNORED）
- 违规级别（LOW/MEDIUM/HIGH）
- 整改状态（PENDING/SUBMITTED/APPROVED/REJECTED）
- 分页参数、DJL端口、图片大小限制

---

## 7. 集成测试

### 7.1 GarbageDisposalIntegrationTest

测试垃圾投放的完整业务链路：

| 测试方法 | 测试流程 |
|---------|---------|
| `testCorrectDisposalFlow` | AI检测METAL→推荐可回收物→用户选择正确→记录isCorrect=1→无违规 |
| `testIncorrectDisposalFlow` | AI检测BIODEGRADABLE→推荐厨余垃圾→用户选择错误→记录isCorrect=0→自动生成违规 |
| `testMixedDisposalFlow` | AI检测多种类别→推荐混合待分拣→isMixed=1→用户选择混合待分拣→isCorrect=1 |
| `testRuleChangeImpact` | 创建记录→修改规则映射→历史记录不受影响 |

### 7.2 RectificationFlowIntegrationTest

测试整改闭环的完整业务链路：

| 测试方法 | 测试流程 |
|---------|---------|
| `testFullRectificationFlow` | 错误投放→自动违规→创建整改→提交整改→复核通过→违规状态RECTIFIED（7步完整闭环） |
| `testRectificationRejectedFlow` | 错误投放→违规→整改→提交→复核驳回→允许创建新整改任务 |
| `testReviewCorrectIgnoresViolation` | 错误投放→自动违规→管理员复核确认用户正确→违规IGNORED |
| `testCascadeDelete` | 投放+违规+整改→级联删除→所有关联数据清除 |
| `testFullCorrectDisposalNoViolation` | 正确投放→无违规→无整改→详情页无关联数据 |

---

## 8. 运行指南

### 8.1 前置条件

1. MySQL 8.0 运行中，数据库 `garbage_sorting` 可访问
2. 数据库连接配置正确（`DBUtil` 中的连接参数）
3. 基础表结构已创建（执行过SQL建表脚本）

### 8.2 运行命令

```bash
# 编译测试代码
mvn test-compile

# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=GarbageRecordServiceTest

# 运行指定包下测试
mvn test -Dtest=com.example.test.dao.*

# 跳过测试（打包时）
mvn package -DskipTests
```

### 8.3 在IDEA中运行

1. 右键点击测试类或测试方法
2. 选择"Run 'XXXTest'"
3. 查看测试结果

---

## 9. 测试数据策略

### 数据初始化

每个测试类在 `@Before` 中通过重写 `initTestData()` 初始化专属测试数据，使用 `truncateTable()` 清空表后插入，确保数据隔离和幂等性。

### 数据清理

- 大部分测试类：依赖 `truncateTable()` 在下次 `initTestData()` 时清空
- `UserDAOTest`：额外重写 `cleanTestData()`，使用 `DELETE ... LIKE 'test_user_%'` 避免影响生产用户数据
- 集成测试：按外键依赖顺序清空：`detection_result → rectification_task → violation_record → garbage_record → garbage_rule`

### 注意事项

- 测试使用**真实数据库**，建议使用独立的测试数据库
- 部分测试类使用 `TRUNCATE TABLE` 重置自增ID
- 测试之间可能存在数据竞争，建议串行运行（`mvn -DforkCount=0 test`）

---

## 10. 覆盖矩阵

| 业务功能 | DAO层 | Service层 | 集成测试 |
|---------|-------|-----------|---------|
| 垃圾投放记录 | GarbageRecordDAO | GarbageRecordService | GarbageDisposal |
| AI检测明细 | DetectionResultDAO | (通过GarbageRecordService) | GarbageDisposal |
| 分类规则 | GarbageRuleDAO | RuleService | GarbageDisposal |
| 违规判定 | ViolationRecordDAO | ViolationService | RectificationFlow |
| 整改闭环 | RectificationTaskDAO | RectificationService | RectificationFlow |
| 人工复核 | (通过GarbageRecordDAO) | GarbageRecordService | RectificationFlow |
| 级联删除 | (各DAO deleteById) | GarbageRecordService | RectificationFlow |
| 知识库 | KnowledgeBaseDAO | KnowledgeService | - |
| 统计分析 | StatisticsDAO | StatisticsService | - |
| 用户管理 | UserDAO | (已有UserService) | - |
| 分页封装 | - | PageResult | - |
| 常量/异常 | - | AppConstants / BusinessException | - |

---

## 11. 验证维度总结

### 功能验证

- CRUD操作正确性
- 分页查询正确性
- 数据筛选和统计正确性
- 业务逻辑正确性（违规判定、级别确定、状态流转）

### 边界验证

- 空值和null处理
- 不存在的数据查询
- 分页边界情况
- 数据约束（UNIQUE、NOT NULL等）

### 异常验证

- 重复数据插入（UNIQUE约束违反）
- 非法参数处理（BusinessException 400/404/500）
- 状态约束（如PENDING状态不允许复核）
- 重复创建整改任务

### 集成验证

- 完整业务流程（投放→违规→整改→闭环）
- 多表关联操作
- 数据一致性（级联删除、状态联动）
- 业务规则执行（复核忽略违规、驳回后重整改）

---

## 12. 测试报告

### Surefire报告

运行测试后，XML报告位于：
```
target/surefire-reports/
├── TEST-com.example.test.dao.GarbageRecordDAOTest.xml
├── TEST-com.example.test.dao.ViolationRecordDAOTest.xml
├── ...（共20个测试类）
└── TEST-com.example.test.integration.RectificationFlowIntegrationTest.xml
```

### 覆盖率报告

建议使用JaCoCo插件生成覆盖率报告，在 `pom.xml` 中添加：
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

运行后查看：`target/site/jacoco/index.html`

---

## 13. 持续集成

可将测试集成到CI/CD流程：
```yaml
name: Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run tests
        run: mvn test
```
