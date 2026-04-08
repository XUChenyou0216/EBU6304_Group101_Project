# TA Recruitment System

## Group Name-list

- ZhaoWantong: 2025110949  (Support TA)
- XUChenyou0216: 231223782 (Lead in Phase1 / Member)
- NancyLis1: 231223793 (Member / Lead in Phase2)
- Aurora050214: 231223519 (Member)
- Lar-me-s: 231223748 (Member)
- Valentina_Yang: 231224044 (Member)
- lylybay: 231224033 (Member)

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

### MO (Module Organiser)
- Post new TA job vacancies with details and deadline
- Edit existing job postings
- View applicant list per job
- Review applicant profiles and CVs
- Update application status (accept/reject)
- Track recruitment progress

### Admin (Administrator)
- View system statistics (total users, TAs, MOs)
- Manage user accounts
- Monitor TA workload

### Security
- Role-based access control (TA/MO/Admin)
- 403 error page for unauthorized access
- Unauthenticated users redirected to login
- Password recovery via security questions

## Technology Stack
- **Backend**: Java 11, Servlet 4.0, JSP
- **Frontend**: HTML, CSS, JavaScript
- **Data Storage**: CSV files (no database, as per project requirements)
- **Build**: Maven, Embedded Tomcat 7
- **Version Control**: Git, GitHub

## Project Structure
```
ta-recruitment/
├── pom.xml
├── src/main/java/com/ta/
│   ├── model/        User, Job, Application, TAProfile
│   ├── dao/          UserDAO, JobDAO, ApplicationDAO, TAProfileDAO
│   ├── servlet/      Login, Register, Recover, AuthFilter, Profile,
│   │                 UploadCv, Apply, PostJob, EditJob,
│   │                 MoApplicants, MoUpdateStatus, CvServe, AppInitializer
│   └── util/         FileManager, PasswordUtil, SessionUtil, Validator
├── src/main/webapp/
│   ├── css/          style.css, applicants-review.css
│   ├── ta/           dashboard, jobs, profile, applications
│   ├── mo/           dashboard, jobs, post-job, edit-job, applicants
│   ├── admin/        dashboard
│   ├── data/         CSV files (users, jobs, applications, profiles)
│   └── uploads/      CV files
└── src/test/java/    FullIterationFeatureTest, ValidatorTest
```

## Data Files (CSV)
- `users.csv` — user accounts with hashed passwords
- `jobs.csv` — TA job postings
- `applications.csv` — job applications with status
- `profiles.csv` — TA personal profiles with CV file paths

## Version History
- **v1.0** (2026-03-30) — Sprint 1: Project scaffold, login/register, role-based access, job listing, form validation
- **v2.0** (2026-04-08) — Sprint 2: TA profile/CV upload, job application, MO post/edit jobs, applicant review, status management, end-to-end testing

## Team — Group 101
| Member           | QM ID |Role | Key Contributions                                                                             |
|------------------|-------|------|-----------------------------------------------------------------------------------------------|
| P1 (LeyanLi)     | 231223793  | Lead | Project architecture, integration, code review, demo prep (US-D01 and  US-C01)                |
| P2 (fmy)         |231223748  |Auth + Profile | Login, register, password recovery,TA profile, CV upload  <br/>(US-A06, US-TA01 and US-TA02）  |
| P3 (yuehanMeng)  | 231224033  |Access + Apply | Role permission control, 403 page, TA apply, application status (US-D02, US-TA04 and US-TA05） |
| P4 (LingyueYang) | 231224044  |Job Management | TA job listing, MO post/edit jobs, job detail page <br/> (US-TA03 and US-MO01）                |
| P5 (Shiqi-Xu)    | 231223519  |MO Management | CV viewer, applicant review, status update servlets <br/> (US-MO03, US-MO02 and US-MO04       |
| P6 (XuChenyou)   | 231223782 |Testing | Form validation, end-to-end feature test <br/> (US-T04, US-T04 and US-C02                     |

## Prototype
Figma: https://www.figma.com/make/aWKU1wtaNm8S1bYDUv7O76/University-TA-Recruitment-Prototype
