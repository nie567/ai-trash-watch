# AI-TrashWatch

> A garbage classification AI recognition and disposal supervision platform based on Java Web (Servlet + JSP + JDBC)

AI-TrashWatch integrates DJL deep learning inference microservice with a **custom YOLOv8 garbage classification model** to intelligently recognize 6 categories of household waste. It combines disposal record management, automated violation detection, rectification closed-loop processing, and statistical analysis to build a complete pipeline from recognition to supervision.

## ✨ Core Features

- **🤖 AI Recognition** — DJL + YOLOv8 custom model, 6-class waste detection
- **🚮 Disposal Records** — Full record of each disposal with original & detection images
- **⚖️ Auto Violation Detection** — Automatically generates violation when user selection differs from AI recommendation, with tiered determination
- **🔄 Rectification Workflow** — Initiate → User submits → Admin reviews, full traceability
- **📊 Statistics & Analytics** — ECharts visualizations: category distribution, accuracy, 7-day trends, violation rankings
- **🔧 Classification Rules** — Flexible mapping from detection classes to business categories
- **📚 Knowledge Base** — Category-based waste knowledge with CRUD management

## 🎯 AI Model Capabilities

The system runs a **custom-trained YOLOv8s waste classification model** that recognizes 6 common household waste types:

| Category | Label | Examples |
|----------|-------|----------|
| 🟤 Biodegradable | BIODEGRADABLE | Kitchen waste, fruit peels, leaves |
| 📦 Cardboard | CARDBOARD | Shipping boxes, cartons |
| 🟢 Glass | GLASS | Glass bottles, broken glass |
| 🔩 Metal | METAL | Cans, metal lids |
| 📄 Paper | PAPER | Newspapers, printer paper, books |
| 🧴 Plastic | PLASTIC | Plastic bottles, plastic bags |

> The confidence threshold is configurable (default 0.5). Detection results can be manually reviewed and corrected.

## 🏗️ System Architecture

![System Architecture Diagram](docs/architecture.svg)

### Layered Architecture

| Layer | Technology | Description |
|-------|-----------|-------------|
| Presentation | JSP + JSTL + CSS + JS | Server-side rendering, ECharts charts |
| Controller | Jakarta Servlet | Request routing, validation, file upload |
| Business | Java Service | Rule mapping, violation logic, rectification workflow, cascade handling |
| Data Access | JDBC + HikariCP | Native SQL, connection pooling |
| Security | Filter | Authentication / Authorization / XSS / Encoding |
| Inference | DJL + YOLOv8 (Spring Boot) | Object detection, HTTP REST communication |

### Core Business Flow

![Core Business Flow Diagram](docs/flowchart.svg)

## 🛠️ Technology Stack

| Category | Technology |
|----------|-----------|
| Language | Java 21 |
| Web | Jakarta Servlet 6.0 + JSP 3.1 + JSTL 2.0 |
| Database | MySQL 8.0 |
| Connection Pool | HikariCP 4.0 |
| Password Hashing | jBCrypt 0.4 |
| JSON | Jackson 2.16 |
| Logging | SLF4J + Logback |
| AI Inference | DJL (Deep Java Library) + Custom YOLOv8 Model |
| Charts | ECharts 5 |
| Build | Maven |
| Container | Tomcat 9.0 / 10.0 |

## 📁 Project Structure

```
src/main/java/com/example/
├── controller/          # Servlet controllers
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
├── dao/                 # Data access layer
├── filter/              # Filters (auth, XSS, encoding)
├── model/               # Entities / VO / DTO
├── service/             # Business layer
│   ├── DjlInferenceClient.java
│   ├── GarbageRecordService.java
│   ├── KnowledgeService.java
│   ├── RectificationService.java
│   ├── RuleService.java
│   ├── StatisticsService.java
│   ├── ViolationService.java
│   └── UserService.java
└── util/                # Utilities

src/main/webapp/
├── css/style.css        # Global styles
└── WEB-INF/jsp/
    ├── nav-user.jsp      # User navigation
    ├── nav-admin.jsp     # Admin navigation
    ├── user/             # User-facing pages
    └── admin/            # Admin-facing pages
```

## 🗄️ Database Design

8 tables running in the `user_management` MySQL database:

| Table | Description |
|-------|-------------|
| `user` | Users |
| `operation_log` | Operation logs |
| `garbage_rule` | Classification rules (className → mappedCategory) |
| `garbage_record` | Disposal records |
| `detection_result` | Detection details |
| `violation_record` | Violation records |
| `rectification_task` | Rectification tasks |
| `knowledge_base` | Knowledge base entries |

Init script: `src/main/resources/init-garbage-tables.sql`

## 🚀 Quick Start

### Prerequisites

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Tomcat 9.0+ (main app) or Tomcat 10.0+ (microservice)
- DJL Detection Microservice ([djl-spring-boot-starter-demo](https://github.com/deepjavalibrary/djl-demo))

### 1. Initialize Database

```sql
CREATE DATABASE IF NOT EXISTS user_management DEFAULT CHARSET utf8mb4;
USE user_management;

-- Apply existing schema
source src/main/resources/schema.sql;

-- Apply garbage extension tables
source src/main/resources/init-garbage-tables.sql;
```

### 2. Configure Database Connection

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/user_management?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
db.username=root
db.password=your_password
```

### 3. Configure App Constants

Edit `src/main/java/com/example/util/AppConstants.java`:

```java
// DJL microservice URL
public static final String DJL_INFERENCE_URL = "http://localhost:8080";

// Image directories
public static final String DJL_INPUT_DIR = "/path/to/data_set/input";
public static final String DJL_OUTPUT_DIR = "/path/to/data_set/output";
```

### 4. Start DJL Inference Microservice

```bash
cd djl-spring-boot-starter-demo
mvn spring-boot:run
# Microservice runs at http://localhost:8080 by default
```

### 5. Build & Deploy

```bash
# Build WAR
mvn clean package -DskipTests

# Deploy to Tomcat
cp target/user-management.war /path/to/tomcat/webapps/
```

### 6. Access the System

- App URL: `http://localhost:8081/user_management_war_exploded/`
- Default admin credentials: see initialization data in `schema.sql`

## 🔍 Key Business Logic

### Manual Review & Determination

An admin sets the `finalCategory` (correct category), which is compared against the user's `selectedCategory`:
- **Match** → User was correct → Violation marked as `IGNORED` (false positive)
- **Mismatch** → User was wrong → Violation stays/reverts to `PENDING`

### Automatic Violation Generation

When a disposal record is saved, if the user's chosen category differs from the AI recommendation, a violation is auto-generated:
- Violation type: `WRONG_CATEGORY` or `MIXED_DISPOSAL`
- Violation level: `LOW` (first offense) / `MEDIUM` (mixed) / `HIGH` (cumulative ≥ 3)

### Cascade Deletion

Deleting a disposal record cascades: rectification tasks → violation records → detection details → disposal record

## 📄 License

This project is open-sourced under the MIT License.