📅 Schedule API – Short Documentation
Overview

Сургуулийн хуваарийн системийн API.
Админ хуваарь удирдана, багш ба сурагч өөрийн хуваарийг харна.

🔐 Authorization
Role	Эрх
ADMIN	Бүх CRUD
TEACHER	Өөрийн хуваарь, анги
STUDENT	Өөрийн хуваарь
PARENT	Хүүхдийн хуваарь
📌 Endpoints Summary
🛠 Admin
Method	Endpoint	Description
POST	/api/schedules	Хуваарь үүсгэх
GET	/api/schedules	Бүх хуваарь
GET	/api/schedules/{id}	Хуваарийн дэлгэрэнгүй
PUT	/api/schedules/{id}	Хуваарь засах
DELETE	/api/schedules/{id}	Хуваарь устгах (soft delete)
🎓 Student
Method	Endpoint	Description
GET	/api/schedules/student/{studentId}	Хуваарь (list)
GET	/api/schedules/student/{studentId}/calendar	Хуваарь (calendar)
👨‍🏫 Teacher
Method	Endpoint	Description
GET	/api/schedules/teacher/{teacherId}	Хуваарь (list)
GET	/api/schedules/teacher/{teacherId}/calendar	Хуваарь (calendar)
GET	/api/schedules/teacher/{teacherId}/classes	Заадаг ангиуд
👪 Parent
Method	Endpoint	Description
GET	/api/schedules/student/{studentId}	Хүүхдийн хуваарь (list)
GET	/api/schedules/student/{studentId}/calendar	Хүүхдийн хуваарь (calendar)

💡 Тэмдэглэл: Parent нь student endpoint-үүдийг ашиглан өөрийн хүүхдийн хуваарийг харна.

🧾 Schedule Create / Update Payload
{
  "teachingAssignmentId": 1,
  "dayOfWeek": 1,
  "periodNumber": 1,
  "startTime": "08:00",
  "endTime": "08:45",
  "roomNumber": "101"
}

Fields

dayOfWeek: 1–7 (Даваа–Ням)

startTime, endTime: HH:mm

roomNumber: optional

📤 Common Response Fields
{
  "dayOfWeek": 1,
  "periodNumber": 1,
  "startTime": "08:00",
  "endTime": "08:45",
  "subject": "Mathematics",
  "teacher": "John Doe",
  "className": "10A"
}

📆 Calendar Response Format
{
  "scheduleByDay": {
    "1": [ { "periodNumber": 1, "subject": "Math" } ],
    "2": [ { "periodNumber": 2, "subject": "Physics" } ]
  }
}

⚠️ Error Codes
Code	Meaning
400	Буруу өгөгдөл
403	Эрхгүй
404	Олдсонгүй
409	Хуваарь давхцаж байна
📏 Business Rules

Нэг багш/анги → нэг цагт 1 хичээл

Устгах → isActive = false

Зөвхөн идэвхтэй хуваарь харагдана

Эрэмбэлэлт: dayOfWeek → periodNumber

🗓 Day of Week

1=Даваа · 2=Мягмар · 3=Лхагва · 4=Пүрэв · 5=Баасан · 6=Бямба · 7=Ням
Endpoint	Төлөв	Тайлбар
GET /api/schedules	✅	Бүх schedule-үүдийг буцаана (3 schedule)
GET /api/schedules/teacher/{id}/classes	✅	Багшийн анги нарыг жагсаана (2 анги, оюутны тоо орсон)
GET /api/schedules/teacher/{id}/calendar	✅	Багшийн хуваарийг өдрөөр нь grouping хийсэн (Day 1: 2 хичээл, Day 2: 1 хичээл)
GET /api/schedules/student/{id}/calendar	✅	Оюутны хуваарийг өдрөөр нь grouping хийсэн
