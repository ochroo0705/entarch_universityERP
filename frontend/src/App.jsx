import { Routes, Route, Navigate } from 'react-router-dom';
import { lazy } from 'react';
import { useAuth } from './context/AuthContext';
import Login from './pages/Login';

// Eagerly loaded layouts (keep sidebar always visible during navigation)
import AdminLayout from './components/AdminLayout';
import TeacherLayout from './components/TeacherLayout';
import StudentLayout from './components/StudentLayout';
import StaffLayout from './components/StaffLayout';

// Lazy-loaded admin pages
const Dashboard = lazy(() => import('./pages/admin/Dashboard'));
const Users = lazy(() => import('./pages/admin/Users'));
const CreateUser = lazy(() => import('./pages/admin/CreateUser'));
const StaffPermissions = lazy(() => import('./pages/admin/StaffPermissions'));
const Classes = lazy(() => import('./pages/admin/Classes'));
const CreateClass = lazy(() => import('./pages/admin/CreateClass'));
const Subjects = lazy(() => import('./pages/admin/Subjects'));
const CreateSubject = lazy(() => import('./pages/admin/CreateSubject'));
const TeachingAssignments = lazy(() => import('./pages/admin/TeachingAssignments'));
const CreateTeachingAssignment = lazy(() => import('./pages/admin/CreateTeachingAssignment'));
const Schedules = lazy(() => import('./pages/admin/Schedules'));
const CreateSchedule = lazy(() => import('./pages/admin/CreateSchedule'));
const ExamSchedules = lazy(() => import('./pages/admin/ExamSchedules'));
const CreateExamSchedule = lazy(() => import('./pages/admin/CreateExamSchedule'));
const Announcements = lazy(() => import('./pages/admin/Announcements'));
const CreateAnnouncement = lazy(() => import('./pages/admin/CreateAnnouncement'));
const AnnouncementDetail = lazy(() => import('./pages/admin/AnnouncementDetail'));
const Enrollments = lazy(() => import('./pages/admin/Enrollments'));
const UserDetail = lazy(() => import('./pages/admin/UserDetail'));
const AdminFinanceCafeteria = lazy(() => import('./pages/admin/FinanceCafeteria'));
const AdminAiRiskDashboard = lazy(() => import('./pages/admin/AdminAiRiskDashboard'));
const AdminAiRiskConfig = lazy(() => import('./pages/admin/AdminAiRiskConfig'));
const AdminAiAuditLog = lazy(() => import('./pages/admin/AdminAiAuditLog'));
const UniversityErpBlueprint = lazy(() => import('./pages/admin/UniversityErpBlueprint'));
const UniversityErpModuleDemo = lazy(() => import('./pages/admin/UniversityErpModuleDemo'));

// Lazy-loaded teacher pages
const TeacherDashboard = lazy(() => import('./pages/teacher/TeacherDashboard'));
const TeacherClasses = lazy(() => import('./pages/teacher/TeacherClasses'));
const TeacherClassDetail = lazy(() => import('./pages/teacher/TeacherClassDetail'));
const TeacherSchedule = lazy(() => import('./pages/teacher/TeacherSchedule'));
const TeacherExams = lazy(() => import('./pages/teacher/TeacherExams'));
const TeacherAnnouncements = lazy(() => import('./pages/teacher/TeacherAnnouncements'));
const TeacherAiRiskDashboard = lazy(() => import('./pages/teacher/TeacherAiRiskDashboard'));

// Lazy-loaded student pages
const StudentDashboard = lazy(() => import('./pages/student/StudentDashboard'));
const StudentSchedule = lazy(() => import('./pages/student/StudentSchedule'));
const StudentExams = lazy(() => import('./pages/student/StudentExams'));
const StudentSubjects = lazy(() => import('./pages/student/StudentSubjects'));
const StudentSubjectDetail = lazy(() => import('./pages/student/StudentSubjectDetail'));
const StudentHomework = lazy(() => import('./pages/student/StudentHomework'));
const StudentGrades = lazy(() => import('./pages/student/StudentGrades'));
const StudentAttendance = lazy(() => import('./pages/student/StudentAttendance'));
const StudentAnnouncements = lazy(() => import('./pages/student/StudentAnnouncements'));
const StudentFinanceCafeteria = lazy(() => import('./pages/student/StudentFinanceCafeteria'));

const normalizeRole = (role) => String(role || '').replace(/^ROLE_/, '');

function userHasRole(user, role) {
  const expected = normalizeRole(role);
  const roles = Array.isArray(user?.roles) && user.roles.length > 0 ? user.roles : [user?.role];
  return roles.map(normalizeRole).includes(expected);
}

function ProtectedRoute({ children, role, roles }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;
  const allowedRoles = roles || (role ? [role] : []);
  if (allowedRoles.length > 0 && !allowedRoles.some((item) => userHasRole(user, item))) return <Navigate to="/login" />;
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/admin"
        element={
          <ProtectedRoute role="ADMIN">
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="users" element={<Users />} />
        <Route path="users/:id" element={<UserDetail />} />
        <Route path="users/create" element={<CreateUser />} />
        <Route path="staff-permissions" element={<StaffPermissions />} />
        <Route path="classes" element={<Classes />} />
        <Route path="classes/create" element={<CreateClass />} />
        <Route path="subjects" element={<Subjects />} />
        <Route path="subjects/create" element={<CreateSubject />} />
        <Route path="teaching-assignments" element={<TeachingAssignments />} />
        <Route path="teaching-assignments/create" element={<CreateTeachingAssignment />} />
        <Route path="schedules" element={<Schedules />} />
        <Route path="schedules/create" element={<CreateSchedule />} />
        <Route path="exam-schedules" element={<ExamSchedules />} />
        <Route path="exam-schedules/create" element={<CreateExamSchedule />} />
        <Route path="announcements" element={<Announcements />} />
        <Route path="announcements/:id" element={<AnnouncementDetail />} />
        <Route path="announcements/create" element={<CreateAnnouncement />} />
        <Route path="parent-students" element={<Navigate to="/admin/users" replace />} />
        <Route path="parent-students/link" element={<Navigate to="/admin/users" replace />} />
        <Route path="enrollments" element={<Enrollments />} />
        <Route path="finance-cafeteria" element={<AdminFinanceCafeteria />} />
        <Route path="ai/risk-dashboard" element={<AdminAiRiskDashboard />} />
        <Route path="ai/risk-config" element={<AdminAiRiskConfig />} />
        <Route path="ai/drafts" element={<Navigate to="/admin/ai/risk-dashboard" replace />} />
        <Route path="ai/audit" element={<AdminAiAuditLog />} />
        <Route path="university-erp" element={<UniversityErpBlueprint />} />
        <Route path="university-erp/:moduleKey" element={<UniversityErpModuleDemo />} />
      </Route>
      <Route
        path="/staff"
        element={
          <ProtectedRoute roles={['FINANCE_STAFF']}>
            <StaffLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/staff/finance-cafeteria" replace />} />
        <Route path="finance-cafeteria" element={<AdminFinanceCafeteria />} />
      </Route>
      <Route
        path="/teacher"
        element={
          <ProtectedRoute role="TEACHER">
            <TeacherLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<TeacherDashboard />} />
        <Route path="classes" element={<TeacherClasses />} />
        <Route path="classes/:classId" element={<TeacherClassDetail />} />
        <Route path="schedule" element={<TeacherSchedule />} />
        <Route path="exams" element={<TeacherExams />} />
        <Route path="announcements" element={<TeacherAnnouncements />} />
        <Route path="ai/risk-dashboard" element={<TeacherAiRiskDashboard />} />
        <Route path="ai/drafts" element={<Navigate to="/teacher/ai/risk-dashboard" replace />} />
      </Route>
      <Route
        path="/student"
        element={
          <ProtectedRoute role="STUDENT">
            <StudentLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<StudentDashboard />} />
        <Route path="schedule" element={<StudentSchedule />} />
        <Route path="exams" element={<StudentExams />} />
        <Route path="subjects" element={<StudentSubjects />} />
        <Route path="subjects/:subjectSlug" element={<StudentSubjectDetail />} />
        <Route path="homework" element={<StudentHomework />} />
        <Route path="grades" element={<StudentGrades />} />
        <Route path="attendance" element={<StudentAttendance />} />
        <Route path="finance-cafeteria" element={<StudentFinanceCafeteria />} />
        <Route path="announcements" element={<StudentAnnouncements />} />
      </Route>
      <Route path="/parent/*" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" />} />
    </Routes>
  );
}
