# TA Recruitment System

## Group Name-list

- ZhaoWantong: 2025110949  (Support TA)
- NancyLis1 (Leyan Li) : 231223793 (Member / Lead in Phase2 & Phase3)
- XUChenyou0216 (Chenyou Xu) : 231223782 (Lead in Phase1 / Member)
- Aurora050214 (Shiqi Xu) : 231223519 (Member)
- Lar-me-s (Meiyu Fu) : 231223748 (Member)
- Valentina_Yang (Lingyue Yang) : 231224044 (Member)
- lylybay (Yuehan Meng) : 231224033 (Member)

Software Engineering group project for `EBU6304` .

**EBU6304 — Software Engineering Group Project | Group 101**

## Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+

### Run
```bash
cd ta-recruitment
mvn clean tomcat7:run
```
Open: **http://localhost:8080/**

### Test Accounts
| Username | Password | Role |
|----------|----------|------|
| admin | admin | Administrator |
| prof_wang | password | Module Organiser |
| student_li | password | Teaching Assistant |
| student_zhang | password | Teaching Assistant |

### Run Tests
```bash
cd ta-recruitment
mvn clean test
```

### Generate JavaDoc
```bash
cd ta-recruitment
mvn javadoc:javadoc
# Output: target/reports/apidocs/index.html
```

### Build WAR
```bash
mvn clean package
# Deploy target/ta-recruitment.war to Tomcat 9 webapps/
```

## Features

### TA (Teaching Assistant)
- Register and login with secure password hashing (SHA-256)
- Create and edit personal profile (student ID, programme, year of study)
- Upload CV (PDF/DOCX, max 10MB, file type validation)
- Browse available TA positions with status badges
- Apply for jobs (duplicate prevention, profile check)
- Track application status (Submitted → Under Review → Accepted/Rejected)
- Receive notifications when application status changes

### MO (Module Organiser)
- Post new TA job vacancies with details and deadline
- Edit existing job postings
- View applicant list per job
- Review applicant profiles and CVs
- Update application status (accept/reject) individually or in batch
- Track recruitment progress with fill rate and statistics
- Close jobs manually or auto-close when all positions filled
- Reopen closed jobs when vacancies become available
- Receive notifications when TAs apply for positions

### Admin (Administrator)
- View system statistics (total jobs, pending applications, positions filled)
- Manage user accounts (suspend, reactivate, delete)
- Monitor TA workload with overload detection (>48 hours threshold)
- AI-powered workload balancing with explainable recommendations
- Human-in-the-Loop control for AI suggestions (approve/reject)
- Export Final Allocation report (.csv)
- Export Application History Archive (.txt)
- Configure system settings (general, security, notifications, data & privacy)
- Receive notifications for workload anomalies

### Security
- Role-based access control (TA/MO/Admin) via AuthFilter
- 403 error page for unauthorized access
- Unauthenticated users redirected to login
- Password recovery via security questions
- Strong password enforcement (uppercase + lowercase + digit)
- Login attempt limiting (5 failures → 5-minute lockout)
- Concurrent file access protection with synchronized operations

### Notification System
- Bell icon with unread count badge on all portals
- TA notifications: application submitted, status changed by MO
- MO notifications: new application received
- Admin notifications: workload threshold exceeded
- Mark as read (individual or all)

## Technology Stack
- **Backend**: Java 11, Servlet 4.0, JSP 2.3
- **Frontend**: HTML5, CSS3, JavaScript ES5, inline SVG icons
- **Data Storage**: CSV files (no database, as per project requirements)
- **AI**: Rule-based engine with optional Claude/OpenAI API integration via configurable settings
- **Build**: Apache Maven 3.6+, Embedded Tomcat 7 (tomcat7-maven-plugin)
- **Security**: SHA-256 password hashing, HttpSession management, Servlet Filter RBAC
- **Testing**: JUnit 4.13.2
- **Version Control**: Git, GitHub (feature branches + PR workflow)
- **Dependencies**: Gson 2.10.1 (JSON parsing for AI responses)

## Project Structure
```
ta-recruitment/
├── pom.xml
├── docs/
│   ├── javadoc/              JavaDoc API documentation
│   ├── USER_MANUAL.md        User manual with screenshots
│   └── screenshots/          Screenshot images for user manual
├── src/main/java/com/ta/
│   ├── model/                User, Job, Application, TAProfile, Notification
│   ├── dao/                  UserDAO, JobDAO, ApplicationDAO, TAProfileDAO, NotificationDAO
│   ├── servlet/              LoginServlet, RegisterServlet, RecoverServlet, AuthFilter,
│   │                         EncodingFilter, ProfileServlet, UploadCvServlet, ApplyServlet,
│   │                         PostJobServlet, EditJobServlet, MoApplicantsServlet,
│   │                         MoUpdateApplicationStatusServlet, MoBatchUpdateApplicationStatusServlet,
│   │                         MoCloseFullJobsServlet, MoReopenJobServlet, MoProgressServlet,
│   │                         CvServeServlet, NotificationServlet, AdminUserServlet,
│   │                         AdminWorkloadServlet, AIWorkloadServlet, AdminSettingsServlet,
│   │                         ExportAllocationServlet, ExportHistoryServlet, AppInitializer
│   └── util/                 FileManager, PasswordUtil, SessionUtil, Validator,
│                             ApplicationStatusUtil, CvMimeUtil, DataDirUtil,
│                             JobDeadlineUtil, AIService, ConfigDAO
├── src/main/webapp/
│   ├── css/                  style.css, applicants-review.css
│   ├── jsp/common/           header.jsp (sidebar navigation), footer.jsp
│   ├── ta/                   dashboard, jobs, job-detail, profile, applications
│   ├── mo/                   dashboard, jobs, post-job, edit-job, applicants, progress
│   ├── admin/                dashboard, users, ai-workload, workload, settings
│   ├── error/                403.jsp
│   ├── WEB-INF/              web.xml
│   ├── data/                 CSV files (users, jobs, applications, profiles, notifications)
│   └── uploads/              Uploaded CV files
├── src/test/java/com/ta/     FullIterationFeatureTest, SystemEndToEndTest, ValidatorTest,
│                             ApplicationAcceptanceCapTest
└── login.jsp, register.jsp, recover.jsp
```

## Data Files (CSV)
- `users.csv` — user accounts with hashed passwords and status
- `jobs.csv` — TA job postings with module details and deadlines
- `applications.csv` — job applications with status and review notes
- `profiles.csv` — TA personal profiles with CV file paths
- `notifications.csv` — notification records with read/unread status

## Version History
- **v1.0** (2026-03-30) — Sprint 1: Project scaffold, login/register, password recovery, role-based access control, TA job listing, form validation, unit tests
- **v2.0** (2026-04-08) — Sprint 2: TA profile/CV upload, job application with duplicate prevention, MO post/edit jobs, applicant review, status management, end-to-end testing
- **v3.0** (2026-04-28) — Sprint 3: Admin user management (suspend/delete), TA workload monitoring with overload detection, notification system (bell icon with red badge), MO recruitment progress tracking, auto-close expired jobs
- **v4.0** (2026-05-10) — Sprint 4: AI workload balancing with explainable recommendations, admin settings page, security hardening (strong password + login lockout), MO batch operations (close filled jobs, reopen jobs), concurrent CSV file safety, regression test suite

## Team — Group 101
| Member           | QM ID      | Role              | Key Contributions |
|------------------|------------|-------------------|-------------------|
| P1 (LeyanLi)     | 231223793  | Phase 2&3 Lead    | Project architecture & MVC skeleton, UI framework (sidebar layout matching Figma), AppInitializer auto-data-init, integration & merge management across all sprints, code review for all PRs, bug fixes (CSV path separator, newline sanitization, findById), Admin dashboard UI, README & User Manual & Demo prep |
| P2 (MeiyuFu)     | 231223748  | Auth + Profile     | Login/register system (US-A06), password recovery (RecoverServlet), TA profile management (US-TA01), CV upload with file validation (US-TA02), admin user management with suspend/activate/delete (US-A04), security hardening — strong password rules & 5-attempt login lockout |
| P3 (YuehanMeng)  | 231224033  | Access + Apply + AI | Role-based access control with AuthFilter & 403 page (US-D02), TA job application with duplicate prevention (US-TA04), application status tracking (US-TA05), AI workload balancing with rule-based engine & explainable results (US-A02), admin settings page with configurable AI provider |
| P4 (LingyueYang) | 231224044  | Job Management     | TA browse jobs with card layout (US-TA03), MO post & edit jobs with enhanced Job model (US-MO01), MO recruitment progress dashboard (US-MO05), concurrent file handling with atomic CSV writes & FileLock (US-D03), concurrency test coverage |
| P5 (ShiqiXu)     | 231223519 | MO Management      | MO view applicant CV (US-MO03), MO review applicants & update status (US-MO02/MO04), batch status update servlet, TA workload monitoring table (US-A01), MO close/reopen job workflow, auto-close expired jobs with JobDeadlineUtil |
| P6 (ChenyouXu)   | 231223782  | Testing + Notify   | Form input validation with Validator utility (US-T04), end-to-end feature test & system test (US-T01), notification system — bell icon, red badge, per-role triggers for TA/MO/Admin, mark read functionality, application history export (US-C02), final allocation export (US-A05) |

## Prototype
Figma: https://www.figma.com/make/aWKU1wtaNm8S1bYDUv7O76/University-TA-Recruitment-Prototype
