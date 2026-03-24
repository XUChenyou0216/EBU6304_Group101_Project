# TA Recruitment System

## Group Name-list

- ZhaoWantong: 2025110949  (Support TA)
- XUChenyou0216: 231223782 (Lead in Phase1/Member)
- NancyLis1: 231223793 (Member/Lead in Phase2)
- Aurora050214: 231223519 (Member)
- Lar-me-s: 231223748 (Member)
- Valentina_Yang: 231224044 (Member)
- lylybay: 231224033 (Member)

Software Engineering group project for `EBU6304` .
# TA Recruitment System

BUPT International School Teaching Assistant Recruitment System
**EBU6304 — Software Engineering Group Project**

## Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+

### Run with Embedded Tomcat
```bash
cd ta-recruitment
mvn tomcat7:run
```
Open: **http://localhost:8080/**

### Test Accounts
| Username | Password | Role |
|----------|----------|------|
| admin | admin | ADMIN |
| prof_wang | password | MO |
| student_li | password | TA |

### Build WAR
```bash
mvn clean package
# Deploy target/ta-recruitment.war to Tomcat webapps/
```

## Project Structure
```
ta-recruitment/
├── pom.xml
├── src/main/java/com/ta/
│   ├── model/        User, Job, Application, TAProfile
│   ├── dao/          UserDAO, JobDAO, ApplicationDAO, TAProfileDAO
│   ├── servlet/      LoginServlet, RegisterServlet, AuthFilter, EncodingFilter
│   ├── service/      (business logic - to be added)
│   └── util/         FileManager, PasswordUtil, SessionUtil, Validator
├── src/main/webapp/
│   ├── WEB-INF/web.xml
│   ├── css/style.css
│   ├── login.jsp, register.jsp, recover.jsp
│   ├── ta/           dashboard, jobs, profile, applications
│   ├── mo/           dashboard, jobs, post-job, applicants
│   ├── admin/        dashboard
│   ├── jsp/common/   header.jsp (shared navigation)
│   ├── data/         CSV files (users, jobs, applications, profiles)
│   └── uploads/      CV files
└── src/test/java/    BasicTest.java
```

## Team Assignment — Phase 2

| Person | Branch | Sprint 1 (v1: 3/30) | Sprint 2 (v2: 4/12) |
|--------|--------|---------------------|---------------------|
| P1 | feature/scaffold | Project scaffold, FileManager, data format | Integration, merge, demo prep |
| P2 | feature/auth | Registration, Login, Password Recovery (US-A06) | TA Profile + CV Upload (US-TA01, US-TA02) |
| P3 | feature/access | Role Permission / AuthFilter (US-D02) | TA Apply + Status (US-TA04, US-TA05) |
| P4 | feature/jobs | TA View Jobs (US-TA03) | MO Post & Edit Jobs (US-MO01) |
| P5 | feature/mo | MO View CV (US-MO03) | MO Review + Update Status (US-MO02, US-MO04) |
| P6 | feature/test | Form Validation (US-T04) | End-to-End Test (US-T01) + Export (US-C02) |

### Branch Workflow
1. `git checkout -b feature/xxx` from main
2. Daily commits
3. **3/30**: Sprint 1 freeze → PR → merge as v1.0
4. **4/9**: Sprint 2 freeze → PR → merge as v2.0
5. **4/12**: Demo & Viva

### Commit Convention
```
[AUTH] Add login validation
[DAO] Fix CSV parsing
[MO] Implement post job servlet
[TEST] Add e2e workflow test
```

## Data Files (CSV, no database)
- `users.csv`: userId, username, passwordHash, role, email, securityQuestion, securityAnswer, status
- `jobs.csv`: jobId, moUserId, moduleName, description, requirements, vacancies, deadline, status, createdDate
- `applications.csv`: applicationId, taUserId, jobId, status, appliedDate, reviewNote
- `profiles.csv`: userId, studentId, fullName, programme, yearOfStudy, phone, cvFilePath

## Prototype
Figma: https://www.figma.com/make/aWKU1wtaNm8S1bYDUv7O76/University-TA-Recruitment-Prototype
