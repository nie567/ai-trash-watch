# AI-TrashWatch 测试文档

## 1. 概述

本测试套件覆盖 AI-TrashWatch 垃圾分类监管系统的核心功能、业务逻辑、安全防护和边界条件。框架：**JUnit 4.13.2 + Mockito 4.11.0**，策略：Model → DAO → Service → Business Logic → Security → Integration。

### 技术栈

| 项目 | 版本 |
|------|------|
| 测试框架 | JUnit 4.13.2 |
| Mock框架 | Mockito 4.11.0 |
| 编译 | Maven + Java 21 |
| 数据库 | MySQL 8.0（真实DB，非Mock） |

### 测试统计

| 分类 | 类数 | 测试数 |
|------|------|--------|
| DAO 层 | 9 | 71 |
| Service 层 | 11 | 85 |
| Util 层 | 8 | 43 |
| Filter 层 | 2 | 11 |
| Model 层 | 4 | 26 |
| Integration | 2 | 9 |
| Config | 1 | 19 |
| **合计** | **37** | **264** |

---

## 2. 目录结构

```
src/test/java/com/example/test/
├── BaseTest.java                              # DB测试基类
├── dao/
│   ├── UserDAOTest.java                       # 13
│   ├── GarbageRecordDAOTest.java              # 7
│   ├── GarbageRuleDAOTest.java                # 6
│   ├── ViolationRecordDAOTest.java            # 10
│   ├── RectificationTaskDAOTest.java          # 10
│   ├── DetectionResultDAOTest.java            # 5
│   ├── KnowledgeBaseDAOTest.java              # 6
│   ├── StatisticsDAOTest.java                 # 5
│   └── OperationLogDAOTest.java               # 9
├── service/
│   ├── UserServiceTest.java                   # 13
│   ├── GarbageRecordServiceTest.java          # 13
│   ├── ViolationServiceTest.java              # 7
│   ├── ViolationBusinessLogicTest.java        # 5  ← 核心业务逻辑
│   ├── RectificationServiceTest.java          # 14
│   ├── RectificationBusinessLogicTest.java    # 2  ← 核心业务逻辑
│   ├── RuleServiceTest.java                   # 8
│   ├── KnowledgeServiceTest.java              # 9
│   ├── StatisticsServiceTest.java             # 5
│   ├── LoginSecurityTest.java                 # 6  ← 登录安全
│   └── UserSecurityLogicTest.java             # 3  ← 角色约束
├── util/
│   ├── BCryptUtilTest.java                    # 7
│   ├── PasswordStrengthFullTest.java          # 10 ← 密码强度全规则
│   ├── ResultTest.java                        # 6
│   ├── AppConfigTest.java                     # 4
│   ├── RequestUtilTest.java                   # 4
│   ├── BusinessExceptionTest.java             # 4
│   ├── PaginationLogicTest.java               # 3  ← 分页保护
│   └── AppContextTest.java                    # 5  ← DI容器
├── filter/
│   ├── XssRequestWrapperTest.java             # 7
│   └── SecurityLogicTest.java                 # 4  ← CSRF+XSS白名单
├── model/
│   ├── ModelTest.java                         # 7
│   ├── PageResultTest.java                    # 5
│   ├── BusinessExceptionTest.java             # 6
│   └── AppConstantsTest.java                  # 8
├── integration/
│   ├── GarbageDisposalIntegrationTest.java    # 4
│   └── RectificationFlowIntegrationTest.java # 5
└── config/
    └── MaskingPatternLayoutTest.java          # 19
```

---

## 3. 核心业务逻辑覆盖矩阵

### 3.1 违规业务规则

| 业务逻辑 | 测试 | 状态 |
|---------|------|------|
| isCorrect==0 + isMixed==1 → "混投" | ViolationServiceTest.testCreateViolationForMixedDisposal | ✅ |
| isCorrect==0 + isMixed!=1 → "分类错误" | ViolationServiceTest.testCreateViolationForClassificationError | ✅ |
| isMixed==1 + selectedCategory!="混合待分拣" → "混投" | ViolationBusinessLogicTest.testMixedDisposalWithoutMixedCategory | ✅ |
| 管理员(role=admin)豁免 | ViolationBusinessLogicTest.testAdminExemption | ✅ |
| 混投 → MEDIUM（固定） | ViolationServiceTest.testCreateViolationForMixedDisposal | ✅ |
| 首次分类错误 → LOW | ViolationServiceTest.testViolationLevelDetermination | ✅ |
| 累计≥3次历史后 → HIGH（三击规则） | ViolationBusinessLogicTest.testThreeStrikeRuleHighLevel | ✅ |
| PENDING → IGNORED（复核正确） | GarbageRecordServiceTest.testReviewRecordCorrect | ✅ |
| IGNORED → PENDING（复核错误恢复） | ViolationBusinessLogicTest.testViolationStatusRestoreFromIgnored | ✅ |
| PENDING → RECTIFIED（整改通过） | RectificationServiceTest.testReviewTaskApproved | ✅ |
| 复核错误无违规 → 新建违规 | ViolationBusinessLogicTest.testReviewIncorrectCreatesViolation | ✅ |

### 3.2 整改业务规则

| 业务逻辑 | 测试 | 状态 |
|---------|------|------|
| 去重：已有未完成任务→异常 | RectificationServiceTest.testCreateDuplicateTask | ✅ |
| null userId → 使用违规记录userId | RectificationBusinessLogicTest.testCreateTaskWithNullUserIdUsesViolationUserId | ✅ |
| PENDING→SUBMITTED→APPROVED/REJECTED | RectificationServiceTest (多个方法) | ✅ |
| REJECTED后违规状态不变 | RectificationBusinessLogicTest.testReviewRejectedViolationStatusUnchanged | ✅ |
| 驳回后可创建新整改 | RectificationFlowIntegrationTest.testRectificationRejectedFlow | ✅ |

### 3.3 登录安全

| 业务逻辑 | 测试 | 状态 |
|---------|------|------|
| 空用户名 → 400 | LoginSecurityTest.testLoginEmptyUsername | ✅ |
| 空密码 → 400 | LoginSecurityTest.testLoginEmptyPassword | ✅ |
| 禁用账号 → 401 | LoginSecurityTest.testLoginDisabledAccount | ✅ |
| 明文密码自动迁移BCrypt | LoginSecurityTest.testPlaintextPasswordAutoMigration | ✅ |
| 不能删除自己 | LoginSecurityTest.testCannotDeleteSelf | ✅ |
| 不能禁用自己 | LoginSecurityTest.testCannotDisableSelf | ✅ |
| 不能修改自己角色 | UserSecurityLogicTest.testCannotModifyOwnRole | ✅ |
| clearSensitiveFields清空密码 | UserSecurityLogicTest.testClearSensitiveFields | ✅ |
| 注册强制role=user | UserSecurityLogicTest.testRegisterForcesUserRole | ✅ |
| 用户不存在→"用户名或密码错误" | UserServiceTest.testLogin_nonExistent | ✅ |

### 3.4 密码强度全规则

| 业务逻辑 | 测试 | 状态 |
|---------|------|------|
| 非空 | BCryptUtilTest.testCheckStrength_null | ✅ |
| 长度 ≥ 8 | BCryptUtilTest.testCheckStrength_short | ✅ |
| 长度 ≤ 64 | PasswordStrengthFullTest.testTooLong + testExactly64 | ✅ |
| 不在弱口令黑名单 | PasswordStrengthFullTest.testWeakPassword + testWeakPassword2 | ✅ |
| 非纯重复字符 | PasswordStrengthFullTest.testRepeatedChar | ✅ |
| 至少两类字符 | PasswordStrengthFullTest.testPureDigits + testPureSymbols + testLetterAndDigit | ✅ |

### 3.5 安全防护

| 业务逻辑 | 测试 | 状态 |
|---------|------|------|
| \<script\>标签移除 | XssRequestWrapperTest.testSanitize_scriptTag | ✅ |
| javascript:协议移除 | XssRequestWrapperTest.testSanitize_javascriptProtocol | ✅ |
| onXXX=事件属性移除 | XssRequestWrapperTest.testSanitize_eventHandler | ✅ |
| 多轮清理防嵌套绕过 | XssRequestWrapperTest.testSanitize_nestedScript | ✅ |
| 正常文本保留 | XssRequestWrapperTest.testSanitize_normalText | ✅ |
| CSRF常量时间比较 | SecurityLogicTest.testCsrfConstantTimeEquals | ✅ |
| CSRF Token生成唯一性 | SecurityLogicTest.testCsrfTokenGeneration | ✅ |
| XSS密码参数白名单 | SecurityLogicTest.testXssSkipParams | ✅ |
| X-Real-IP优先级 | SecurityLogicTest.testXRealIpPriority | ✅ |

### 3.6 分页参数保护

| 业务逻辑 | 测试 | 状态 |
|---------|------|------|
| page<1 → 默认1 | PaginationLogicTest.testPageLessThan1 | ✅ |
| pageSize<1 → 默认10 | PaginationLogicTest.testPageSizeLessThan1 | ✅ |
| pageSize>100 → 上限100 | PaginationLogicTest.testPageSizeExceedsMax | ✅ |

### 3.7 DI容器（AppContext）

| 业务逻辑 | 测试 | 状态 |
|---------|------|------|
| 未初始化→IllegalStateException | AppContextTest.testGetBeforeInitThrows | ✅ |
| init创建实例 | AppContextTest.testInitCreatesInstance | ✅ |
| 所有Service非空 | AppContextTest.testGetServicesNonNull | ✅ |
| destroy清除实例 | AppContextTest.testDestroyClearsInstance | ✅ |
| 双重init安全 | AppContextTest.testDoubleInitSafe | ✅ |

---

## 4. 未覆盖项（低优先级，需Servlet容器环境）

| 业务逻辑 | 原因 |
|---------|------|
| Semaphore并发控制返回429 | 需Servlet容器模拟并发请求 |
| Session Fixation防护(changeSessionId) | 需HttpServletRequest模拟 |
| 事务回滚验证 | 需模拟DB异常触发rollback |
| Dashboard趋势零值填充 | 需验证Servlet层Map构建逻辑 |
| AuthFilter/AdminFilter过滤链 | 需FilterChain模拟 |
| RememberMe Cookie属性 | 需HttpServletResponse模拟 |

---

## 5. 运行指南

```bash
mvn test                                                     # 全部264个
mvn test -Dtest=ViolationBusinessLogicTest                   # 违规业务逻辑
mvn test -Dtest="com.example.test.util.*,com.example.test.filter.*"  # 纯单元测试(无DB)
mvn test -Dtest=com.example.test.service.*                   # Service层
mvn package -DskipTests                                      # 跳过测试
```
