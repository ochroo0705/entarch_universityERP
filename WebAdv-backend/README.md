# Сургууль Удирдлагын Систем – Backend

## 1. Төслийн танилцуулга

Энэхүү төсөл нь **Java Spring Boot** дээр суурилсан сургууль удирдлагын системийн backend бөгөөд **PostgreSQL** өгөгдлийн сантай ажиллана.

- **Frontend:** React/Next.js (API-р холбогдоно)
- **Backend:** Java 17+, Spring Boot, Flyway
- **Өгөгдлийн сан:** PostgreSQL, AWS RDS
- **Хэрэгсэл:** IntelliJ IDEA Ultimate, Maven

### Ажиллуулах
- PostgreSQL server бэлэн байх(local or not)
- src/main/resources/application.yml-d db хаяг, auth тохируулах
- DB дээр "schooldb" гэсэн database үүсгэх(ажиллуулахад хүснэгтүүд, жишээ өгөгдөл автоматаар үүснэ)
- terminal дээр ажиллуулах команд: **mvn spring-boot:run** (maven суулгасан байх хэрэгтэй)
- Жишээ сурагчийн хуваарь харах endpoint(postman, etc): 

- - **auth**: name = "admin" password = "admin123" 
- - http://localhost:8080/api/schedules/student/2



---

## 2. Фолдер бүтэц

`src/main/java/com/edusys/backend/` дотор:
- `model/` – JPA entity класс
- `repository/` – JPA repository интерфэйсүүд
- `service/` – Бизнес логик
- `controller/` – REST API контроллерүүд
- `bootstrap/` – Mock data initializer (DataLoader)
- `Application.java` – Spring Boot application entry point

---

## 3. Өгөгдлийн сангийн схем

### Гол хүснэгтүүд
- `users` – хэрэглэгчид (багш, оюутан, эцэг эх)
- `classes` – ангиуд
- `subjects` – хичээлүүд
- `student_enrollment` – оюутны бүртгэл
- `teaching_assignments` 
- `periods` – хичээлийн цагууд
- `schedules` – ангийн хуваарь
- `attendance` – ирц
- `homework` – даалгавар
- `homework_submissions` – даалгаврын шалгалт
- `parent_students` – эцэг эх, оюутан холбоо
- `grades` – оноо
- `announcements` – мэдэгдэл

Бүх хүснэгтүүд **foreign key** болон **CHECK constraint**-тай.

---

## 4. Java Entities (Model)

- **JPA annotations:** `@Entity`, `@Table`, `@ManyToOne`, `@OneToMany`
- ID: `@Id @GeneratedValue`
- Enum: `@Enumerated(EnumType.STRING)` + `@JsonValue`

**Жишээ Entity:** `User`, `Class`, `Subject`, `TeachingAssignment`, `StudentEnrollment`, `Period`, `Schedule`, `Homework`, `HomeworkSubmission`, `Attendance`, `Grade`, `Announcement`, `ParentStudent`

---

## 5. Repository Layer

- Өгөгдлийн сантай харьцах interface
- CRUD үйлдэл `JpaRepository`-аар дамжуулна

---

## 6. Service Layer

- Бизнес логик байрлана
- Repository-оос өгөгдөл татаж боловсруулна

---

## 7. Controller Layer

- REST API endpoints-ийг тодорхойлно
- JSON-аар өгөгдөл буцаана

**Жишээ endpoint:**
- `GET /api/homework` → бүх даалгавар авах
- `POST /api/homework` → шинэ даалгавар үүсгэх

---

## 8. Mock Data Seeder (DataLoader)

- Командны мөрөнд ажиллаж mock өгөгдөл үүсгэнэ
- Оюутан, багш, эцэг эх
- Ангийн мэдээлэл, хичээлийн хуваарь, даалгавар

---

## 9. API-н үндсэн endpoints

| Entity | GET (List) | POST (Create) |
|-------|------------|---------------|
| User | /api/users | /api/users |
| Class | /api/classes | /api/classes |
| Subject | /api/subjects | /api/subjects |
| StudentEnrollment | /api/student-enrollments | /api/student-enrollments |
| Period | /api/periods | /api/periods |
| Schedule | /api/schedules | /api/schedules |
| Attendance | /api/attendance | /api/attendance |
| ParentStudent | /api/parent-students | /api/parent-students |
| Grades | /api/grades | /api/grades |
| Announcement | /api/announcements | /api/announcements |

## Homework endpoints

| Name | Method | Endpoint | Description | Example curl |
|------|--------|---------|-------------|--------------|
| Get all homework for current user | GET | /api/homework | Returns all homework for the authenticated user | `curl -i -u student02:student123 http://localhost:8080/api/homework` |
| Create homework (Teacher only) | POST | /api/homework | Create a new homework | `curl -i -u teacher01:teacher123 -X POST http://localhost:8080/api/homework -H "Content-Type: application/json" -d '{"teachingAssignmentId":1,"title":"ahisan web","description":"shar nom hii","dueDate":"2025-12-30","maxScore":100,"type":"homework","attachmentUrl":""}'` |
| Get homework by ID | GET | /api/homework/{id} | Get homework details by ID | `curl -i -u teacher01:teacher123 -X GET http://localhost:8080/api/homework/3` |
| Get homework by teaching assignment | GET | /api/homework/teaching-assignment/{id} | List homework for a specific teaching assignment | `curl -i -u teacher01:teacher123 -X GET http://localhost:8080/api/homework/teaching-assignment/2` |
| Update homework by ID | PUT | /api/homework/{id} | Update homework details | `curl -i -u teacher01:teacher123 -X PUT http://localhost:8080/api/homework/1 -H "Content-Type: application/json" -d '{"teachingAssignmentId":2,"title":"Updated Homework Title","description":"Updated description","dueDate":"2025-12-31","maxScore":100,"type":"homework","attachmentUrl":null}'` |
| Delete homework by ID | DELETE | /api/homework/{id} | Delete a homework | `curl -i -u teacher01:teacher123 -X DELETE http://localhost:8080/api/homework/5` |
| Get all homework for current student | GET | /api/homework/student | List all homework assigned to the current student | `curl -i -u student02:student123 -X GET http://localhost:8080/api/homework/student` |
| Submit homework | POST | /api/homework-submissions/homework/{homeworkId} | Submit a homework | `curl -i -u student01:student123 -X POST http://localhost:8080/api/homework-submissions/homework/3 -H "Content-Type: application/json" -d '{"submissionText":"My homework submission","attachmentUrl":null}'` |
| Get my submission for a homework | GET | /api/homework-submissions/homework/{homeworkId}/me | Get current user's submission for a specific homework | `curl -i -u student01:student123 -X GET http://localhost:8080/api/homework-submissions/homework/3/me` |
| Get all submissions for a homework | GET | /api/homework-submissions/homework/{homeworkId} | List all submissions for a specific homework | `curl -i -u teacher01:teacher123 -X GET http://localhost:8080/api/homework-submissions/homework/2` |
| Get a submission by ID | GET | /api/homework-submissions/{submissionId} | Get details of a specific submission | `curl -i -u teacher01:teacher123 -X GET http://localhost:8080/api/homework-submissions/4` |
| Get all submissions of a specific student in a class | GET | /api/homework-submissions/class/{classId}/student/{studentId} | List submissions of a student in a class | `curl -i -u teacher01:teacher123 -X GET http://localhost:8080/api/homework-submissions/class/2/student/3` |
| Grade a single submission | PATCH | /api/homework-grading/submissions/{submissionId} | Grade a submission | `curl -i -u teacher01:teacher123 -X PATCH http://localhost:8080/api/homework-grading/submissions/3 -H "Content-Type: application/json" -d '{"score":85,"feedback":"Dutuu baina."}'` |
| Update grade (alias) | PATCH | /api/homework-grading/submissions/{submissionId}/update | Update the grade for a submission | `curl -i -u teacher01:teacher123 -X PATCH http://localhost:8080/api/homework-grading/submissions/3/update -H "Content-Type: application/json" -d '{"score":90,"feedback":"Saijruulsan baina"}'` |
| Bulk grading | PATCH | /api/homework-grading/homework/{homeworkId}/bulk | Grade multiple submissions at once | `curl -i -u teacher01:teacher123 -X PATCH http://localhost:8080/api/homework-grading/homework/5/bulk -H "Content-Type: application/json" -d '[{"submissionId":10,"score":80,"feedback":"Needs improvement"},{"submissionId":11,"score":95,"feedback":"Excellent"}]'` |

## Admin, teacher class management

| Name                                                   | Method | Endpoint                                   | Description                                                                              | Example curl                                                                                                                                                                                                                         |
| ------------------------------------------------------ | ------ | ------------------------------------------ | ---------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Assign teacher to subject (create teaching assignment) | POST   | /api/teaching-assignments/assign           | Admin assigns a teacher to a subject and class for a specific academic year and semester | `curl -i -u admin:admin123 -X POST http://localhost:8080/api/teaching-assignments/assign -H "Content-Type: application/json" -d '{"teacherId":5,"subjectId":1,"classId":1,"academicYear":"2025-2026","semester":1,"isActive":true}'` |
| Update teaching assignment                             | PUT    | /api/teaching-assignments/{id}             | Admin updates an existing teaching assignment                                            | `curl -i -u admin:admin123 -X PUT http://localhost:8080/api/teaching-assignments/1 -H "Content-Type: application/json" -d '{"teacherId":1,"subjectId":1,"classId":1,"academicYear":"2026-2027","semester":2,"isActive":true}'`       |
| Delete teaching assignment (soft delete)               | DELETE | /api/teaching-assignments/{id}             | Admin deactivates a teaching assignment (isActive = false)                               | `curl -i -u admin:admin123 -X DELETE http://localhost:8080/api/teaching-assignments/6`                                                                                                                                               |
| Enroll student to class (Teacher)                      | POST   | /api/student-enrollments/teacher/enroll    | Teacher enrolls a student into a class                                                   | `curl -i -u teacher01:teacher123 -X POST http://localhost:8080/api/student-enrollments/teacher/enroll -H "Content-Type: application/json" -d '{"studentId":3,"classId":1,"student_number":"S004","status":"active"}'`                |
| Unenroll student from class (Teacher)                  | POST   | /api/student-enrollments/teacher/unenroll  | Teacher removes a student from a class                                                   | `curl -i -u teacher01:teacher123 -X POST http://localhost:8080/api/student-enrollments/teacher/unenroll -H "Content-Type: application/json" -d '{"studentId":3,"classId":1}'`                                                        |
| Create class                                           | POST   | /api/classes                               | Admin creates a new class                                                              |                     |
| Delete class (soft delete)                             | DELETE | /api/classes/{id}                          | Admin deactivates a class (isActive = false)                                           | `curl -i -u admin:admin123 -X DELETE http://localhost:8080/api/classes/3`                                                                                                                                                      |
| Get classes taught by current teacher                  | GET    | /api/classes/my-teaching                   | Lists all classes the authenticated teacher is teaching                                  | `curl -i -u teacher01:teacher123 http://localhost:8080/api/classes/my-teaching`                                                                                                                                                      |
| Add assistant teacher to class                         | POST   | /api/classes/{id}/assistants               | Homeroom teacher assigns an assistant teacher to a class                                 | `curl -i -u teacher01:teacher123 -X POST http://localhost:8080/api/classes/1/assistants -H "Content-Type: application/json" -d '{"teacherId":5}'`                                                                                    |
| List assistant teachers                                | GET    | /api/classes/{id}/assistants               | Lists assistant teachers assigned to a class                                             | `curl -i -u teacher01:teacher123 http://localhost:8080/api/classes/1/assistants`                                                                                                                                                     |
| Remove assistant teacher                               | DELETE | /api/classes/{id}/assistants/{assistantId} | Homeroom teacher removes an assistant teacher from a class                               | `curl -i -u teacher01:teacher123 -X DELETE http://localhost:8080/api/classes/1/assistants/5`               

## Parent                                                                                             
| Name                                 | HTTP Method | Endpoint                                                              | Description                                                                              |
| ------------------------------------ | ----------- | --------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| View All Homeworks of a Child        | GET         | `/api/homework/student/{id}`                                          | Allows a parent to view all homework assignments assigned to a specific child (student). |
| View Single Homework of a Child      | GET         | `/api/homework/{id}`                                                  | Allows a parent to view the details of a specific homework assigned to their child.      |
| Create Parent–Child Link             | POST        | `/api/parent-students/link`                                           | Allows an administrator to establish a relationship between a parent and a student.      |
| View Child Submission for a Homework | GET         | `/api/homework-submissions/homework/{homeworkId}/student/{studentId}` | Allows a parent to view their child’s submission for a specific homework.                |
| View Submission by ID                | GET         | `/api/homework-submissions/{id}`                                      | Allows a parent to view a specific homework submission by submission ID.                 |
| View All Children of Parent          | GET         | `/api/parent-students/me/children`                                    | Allows a parent to retrieve a list of all students linked to their account.              |
| Submit Child Homework | POST        | `/api/homework-submissions/homework/{homeworkId}/student/{studentId}` | Allows a parent to submit a homework assignment on behalf of their child.                |
| View Child Profile    | GET         | `/api/parent-students/me/children/{id}/profile`                       | Allows a parent to view the profile details of a specific child linked to their account. |
| View Children Dashboard | GET         | `/api/parent-students/me/children/dashboard` | Allows a parent to view aggregated statistics and academic progress for all of their linked children. |


---

## 10. Санамж

- Foreign key-ууд `ON UPDATE CASCADE`, `ON DELETE CASCADE/SET NULL`
- Enum-ууд lowercase, DB check constraint-тэй нийцүүлсэн
- DTO ашиглавал frontend-д илүү цэвэр JSON өгөгдөл дамжуулна

---

## 11. Local database reseed

The current `DataLoader` only runs during backend startup, which makes realistic demo refreshes awkward. Use the standalone Python reseed script for local/dev data resets without redeploying.

### Install dependencies

```bash
python -m pip install -r scripts/reseed_requirements.txt
```

### Dry run

```bash
python scripts/reseed_school_data.py
```

This validates the generated data and prints expected row counts without changing the database.

### Apply the reseed

```bash
python scripts/reseed_school_data.py --apply --yes
```

### What it seeds

- `1` admin account: `admin / admin123`
- `36` classes from `1A` to `12C`
- `1080` students with usernames like `ST0001` and password `student123`
- parent accounts with usernames like `PT0001` and password `parent123`
- teacher accounts with usernames like `TC0001` and password `teacher123`
- subjects, periods, teaching assignments, homework, varied submissions, attendance, grades
- schedules are intentionally left empty so the backend schedule generator can populate them later

The script reads `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` from the environment or `WebAdv-backend/.env`, then falls back to the defaults in `application-dev.yml`.
