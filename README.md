# Smart Campus Analytics

An analytics dashboard and Spring Boot API for organizing student indicators and helping faculty decide where follow-up may be useful.

The Student and Faculty portals share a blush-pink, burgundy, and rose theme (`#FDF0F4`, `#5A1A32`, and `#B24C63`), including dashboard cards, forms, navigation, and charts.

## Unified student view

The student record and CSV template can hold:

- **Academic:** CGPA, internal-assessment average, backlogs, and subject-performance average.
- **Attendance:** overall and subject-wise average attendance.
- **LMS:** activity hours per week, logins per week, and assignment-completion percentage.
- **Engagement:** events, clubs, hackathons, and certifications.
- **Placement:** aptitude, coding, and mock-interview scores.
- **Skills:** technical and soft-skill scores.
- **Feedback:** student-satisfaction and faculty-feedback scores.

Use the **Saved Student Details** page to download the CSV template and import rows. Student ID, name, department, and semester are required. Indicator values may be left blank when unavailable; numeric values are range-checked before import. The Add Student Profile form accepts the same indicators.

## Transparent decision-support scoring

The API calculates seven category scores and a 0–100 Student Success Score using these category weights:

| Category | Weight |
| --- | ---: |
| Academic | 30% |
| Attendance | 15% |
| LMS | 10% |
| Engagement | 10% |
| Placement | 15% |
| Skills | 10% |
| Feedback | 10% |

Available indicators are averaged within their category. CGPA is converted from a 0–10 scale; backlog count is converted to a score with a 20-point reduction per backlog; LMS hours, logins, and engagement counts are normalized against documented caps in `ScoringEngineService`. When a category has no data, its weight is excluded and the remaining weights are proportionally rebalanced. Data coverage reports the proportion of the seven categories with at least one indicator.

Overall and academic risk flags use score thresholds; two or more backlogs and attendance below 65% trigger a high academic/overall risk flag. Placement risk is based on the average of available aptitude, coding, and mock-interview scores. The API also returns suggested follow-up text based on low category indicators.

These are transparent rule-based indicators for human review, **not a trained or validated machine-learning prediction**. CSV upload consolidates data into one student record; connectors to external college attendance, LMS, placement, or feedback systems are not included.

## Individual course grades

Use **Individual Course Grades** in the sidebar to select a student and semester. Semesters 1–2 show the common courses supplied for all departments. For IT students, semesters 3–8 show the supplied IT curriculum. Other departments show suggested department-specific courses; unlisted courses can be entered manually. Select a grade from the grade list or enter a custom grade. Course-grade records (including semester) are stored separately and linked to the student ID, and can be reviewed or deleted.

## Student portal

The Streamlit app offers separate Student and Faculty portals. Students sign in with their registration number and DOB in `YYYY-MM-DD` format. The DOB is the permanent password as requested, but only a BCrypt hash is stored. Faculty sign in with a faculty ID and a provisioned password. Five consecutive failed attempts lock either account for 15 minutes; successful sessions expire after eight hours and raw session tokens are not stored in the database. Student sessions can access only the student's own portal endpoints; faculty sessions can access the analytics and student-management APIs. A student session cannot access the faculty dashboard or its APIs.

Provision accounts from the saved student list using a server-side CSV with the `studentId,dateOfBirth` header (template: `database/student_roster_template.csv`). Do not paste DOBs into chat or commit a populated roster. Set a long random `STUDENT_ROSTER_IMPORT_TOKEN` environment variable and POST the CSV to `http://localhost:8081/api/v1/student-portal/auth/roster` with the same value in the `X-Roster-Import-Token` header and form field `file`. Import replaces the selected students' DOB password hashes and clears their lockout counters. The imported DOB values are not retained as plaintext.

Provision faculty accounts with a server-side CSV using the `facultyId,password` header (template: `database/faculty_roster_template.csv`). Use a unique password of at least 12 characters for each account, keep the populated roster private, and do not commit it or paste it into chat. Set a separate long random `FACULTY_ROSTER_IMPORT_TOKEN` environment variable and POST the roster to `http://localhost:8081/api/v1/faculty-portal/auth/roster` with that value in the `X-Roster-Import-Token` header and the roster in multipart field `file`. Imported passwords are BCrypt-hashed; re-importing an ID resets its password and lockout state.

For local UI testing only, `database/seed_local_demo_accounts.sql` creates a synthetic student and one faculty account with public demo credentials. Run it only against a local development database; never run it in production. The demo student logs in with ID `DEMO-STU-001` and DOB/password `2004-01-15`; the faculty test login is ID `DEMO-FAC-001` and password `FacultyTest@2026!`. These accounts are not created automatically.

Students can log hackathons, events, and clubs; upload distinct course/event/activity certificates; and submit PDF/JPG/PNG assignments by semester and subject. Activity totals are derived from saved entries, and the certificate count is derived from uploaded files (identical file contents for one student count once). Uploads are capped at 10 MB and saved under `STUDENT_UPLOAD_DIR` (default `backend-springboot/uploads/student-portal`); keep that directory private and back it up with the database.

The faculty **Weekly Student Activity** page shows each student's successful portal logins and estimated active portal hours over the last 7 days. Active time is recorded while the student portal tab is visible and has recent mouse, keyboard, touch, or scroll activity; idle/background time is excluded. This is an estimate of portal use, not a measure of offline study time.

DOB-only passwords remain guessable even when stored as hashes. Use HTTPS outside localhost and do not deploy this sign-in design for real student records without stronger authentication such as a student-chosen password or second factor. No default student accounts are created; the saved student register numbers must be matched to an authorized DOB roster before login works.

## Run locally

- Create the MySQL database using `database/create_smart_campus_db.sql` (for example, `mysql -u root -p < database/create_smart_campus_db.sql`).
- Configure the database URL/user/password in `backend-springboot/src/main/resources/application.properties`. The default schema is `smart_campus_db` on localhost port `3306`.
- Start the Spring Boot backend in `backend-springboot` on port `8081`. Hibernate updates existing tables with newly added student fields.
- Start the Streamlit app in `frontend-python` on port `8501`.

## Protected API

The Spring Boot backend exposes the following student-record APIs to authenticated faculty sessions only. Student sessions cannot access these endpoints:

- `GET /api/v1/students` — list student records.
- `GET /api/v1/students/{studentId}` — get one student by student ID.
- `POST /api/v1/students` — create a record and calculate scores/risk flags (`201 Created`).
- `PUT /api/v1/students/{studentId}` — replace a record and recalculate its scores/risk flags.
- `DELETE /api/v1/students/{studentId}` — delete a record (`204 No Content`).
- `GET /api/v1/students/{studentId}/courses` — list that student's course grades.
- `POST /api/v1/students/{studentId}/courses` — add a course grade (`201 Created`); JSON fields: `courseName`, optional `courseCode`, and `grade`.
- `PUT /api/v1/students/{studentId}/courses/{courseGradeId}` — update a course grade.
- `DELETE /api/v1/students/{studentId}/courses/{courseGradeId}` — delete a course grade (`204 No Content`).

Student IDs are unique. Duplicate creates return `409 Conflict`; missing records return `404 Not Found`; invalid values return `400 Bad Request`. Risk scores are recalculated on create/update and are not accepted as authoritative client input.