import api from './axios';

let aiAccessibleStudentsCache = null;
let aiAccessibleStudentsPromise = null;

const normalizePaginatedArrayResponse = async (path, params) => {
  if (params && Object.keys(params).length > 0) {
    return api.get(path, { params });
  }

  const firstPage = await api.get(path, { params: { page: 1, pageSize: 100 } });
  const payload = firstPage.data ?? {};
  if (!Array.isArray(payload.items)) {
    return firstPage;
  }

  const totalPages = Number(payload.totalPages) || 1;
  if (totalPages <= 1) {
    return { ...firstPage, data: payload.items };
  }

  const responses = await Promise.all(
    Array.from({ length: totalPages - 1 }, (_, index) =>
      api.get(path, { params: { page: index + 2, pageSize: 100 } })
    )
  );

  const items = [
    ...payload.items,
    ...responses.flatMap((response) => (Array.isArray(response.data?.items) ? response.data.items : [])),
  ];

  return { ...firstPage, data: items };
};

export const login = (username, password) =>
  api.post('/public/auth/login', { username, password });

export const getStats = () => api.get('/admin/stats');

// Finance and cafeteria
export const getFeeItems = () => api.get('/finance/fee-items');
export const createFeeItem = (data) => api.post('/finance/fee-items', data);
export const getFeeInvoices = (params) => api.get('/finance/invoices', { params });
export const createFeeInvoice = (data) => api.post('/finance/invoices', data);
export const updateFeeInvoice = (invoiceId, data) => api.put(`/finance/invoices/${invoiceId}`, data);
export const cancelFeeInvoice = (invoiceId, data = {}) => api.post(`/finance/invoices/${invoiceId}/cancel`, data);
export const waiveFeeInvoice = (invoiceId, data = {}) => api.post(`/finance/invoices/${invoiceId}/waive`, data);
export const recordFeePayment = (data) => api.post('/finance/payments', data);
export const getMealPlans = () => api.get('/finance/meal-plans');
export const createMealPlan = (data) => api.post('/finance/meal-plans', data);
export const getMealItems = () => api.get('/finance/meal-items');
export const createMealItem = (data) => api.post('/finance/meal-items', data);
export const getMealPurchases = (params) => api.get('/finance/meal-purchases', { params });
export const getMealPurchaseDailySummary = (params) => api.get('/finance/meal-purchases/daily-summary', { params });
export const recordMealPurchase = (data) => api.post('/finance/meal-purchases', data);
export const getStudentFinanceSummary = (studentId) => api.get(`/finance/students/${studentId}/summary`);

// University ERP phase 1
export const getUniversityApplicants = (params) => api.get('/university-erp/admissions/applicants', { params });
export const createUniversityApplicant = (data) => api.post('/university-erp/admissions/applicants', data);
export const screenUniversityApplicant = (id, data = {}) => api.post(`/university-erp/admissions/applicants/${id}/screen`, data);
export const acceptUniversityApplicant = (id, data = {}) => api.post(`/university-erp/admissions/applicants/${id}/accept`, data);
export const rejectUniversityApplicant = (id, data = {}) => api.post(`/university-erp/admissions/applicants/${id}/reject`, data);
export const getUniversityCourses = () => api.get('/university-erp/course-selection/courses');
export const getUniversityCourseSelections = (params) => api.get('/university-erp/course-selection/selections', { params });
export const createUniversityCourseSelection = (data) => api.post('/university-erp/course-selection/selections', data);
export const getUniversityCoursePrerequisites = (params) => api.get('/university-erp/course-selection/prerequisites', { params });
export const createUniversityCoursePrerequisite = (data) => api.post('/university-erp/course-selection/prerequisites', data);
export const getUniversityCourseCorequisites = (params) => api.get('/university-erp/course-selection/corequisites', { params });
export const createUniversityCourseCorequisite = (data) => api.post('/university-erp/course-selection/corequisites', data);
export const getUniversityAcademicRecords = (params) => api.get('/university-erp/course-selection/academic-records', { params });
export const createUniversityAcademicRecord = (data) => api.post('/university-erp/course-selection/academic-records', data);
export const getUniversityAcademicPolicy = () => api.get('/university-erp/course-selection/academic-policy');
export const updateUniversityAcademicPolicy = (data) => api.put('/university-erp/course-selection/academic-policy', data);
export const getUniversityProgramRequirements = (params) => api.get('/university-erp/course-selection/program-requirements', { params });
export const createUniversityProgramRequirement = (data) => api.post('/university-erp/course-selection/program-requirements', data);
export const getUniversityDegreeAudit = (params) => api.get('/university-erp/course-selection/degree-audit', { params });
export const getUniversityFacultyProfiles = () => api.get('/university-erp/hr-faculty/profiles');
export const upsertUniversityFacultyProfile = (data) => api.post('/university-erp/hr-faculty/profiles', data);
export const getUniversityDepartments = () => api.get('/university-erp/hr-faculty/departments');
export const createUniversityDepartment = (data) => api.post('/university-erp/hr-faculty/departments', data);
export const getUniversityFacultyWorkloads = (params) => api.get('/university-erp/hr-faculty/workloads', { params });
export const createUniversityFacultyWorkload = (data) => api.post('/university-erp/hr-faculty/workloads', data);
export const getUniversityFacultyLeaveRequests = (params) => api.get('/university-erp/hr-faculty/leave-requests', { params });
export const createUniversityFacultyLeaveRequest = (data) => api.post('/university-erp/hr-faculty/leave-requests', data);
export const decideUniversityFacultyLeaveRequest = (id, data) => api.post(`/university-erp/hr-faculty/leave-requests/${id}/decision`, data);
export const getUniversityServiceTypes = (params) => api.get('/university-erp/student-services/types', { params });
export const createUniversityServiceType = (data) => api.post('/university-erp/student-services/types', data);
export const updateUniversityServiceType = (id, data) => api.put(`/university-erp/student-services/types/${id}`, data);
export const getUniversityServiceRequests = (params) => api.get('/university-erp/student-services/requests', { params });
export const getUniversityServiceQueues = () => api.get('/university-erp/student-services/queues');
export const createUniversityServiceRequest = (data) => api.post('/university-erp/student-services/requests', data);
export const updateUniversityServiceRequestStatus = (id, data) => api.post(`/university-erp/student-services/requests/${id}/status`, data);
export const assignUniversityServiceRequest = (id, data) => api.post(`/university-erp/student-services/requests/${id}/assignment`, data);
export const evaluateUniversityGraduationClearance = (id, data) => api.post(`/university-erp/student-services/requests/${id}/graduation-clearance`, data);
export const getUniversityServiceRequestDetail = (id) => api.get(`/university-erp/student-services/requests/${id}`);
export const addUniversityServiceRequestComment = (id, data) => api.post(`/university-erp/student-services/requests/${id}/comments`, data);
export const uploadUniversityServiceRequestAttachments = (id, files = []) => {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  return api.post(`/university-erp/student-services/requests/${id}/attachments`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const getUniversityReportSummary = () => api.get('/university-erp/reporting/summary');
export const getUniversityAuditEvents = () => api.get('/university-erp/reporting/audit-events');
export const getUniversityReportDefinitions = () => api.get('/university-erp/reporting/report-definitions');
export const getUniversityReportRuns = () => api.get('/university-erp/reporting/report-runs');
export const runUniversityReport = (reportKey) => api.post(`/university-erp/reporting/report-definitions/${reportKey}/run`);
export const exportUniversityReportCsv = (reportKey, params = {}) =>
  api.get(`/university-erp/reporting/report-definitions/${reportKey}/csv`, { params, responseType: 'blob' });
export const getUniversityReportDetails = (reportKey, params = {}) =>
  api.get(`/university-erp/reporting/report-definitions/${reportKey}/details`, { params });
export const getUniversityIntegrations = () => api.get('/university-erp/integrations');
export const getUniversityIntegrationRuns = () => api.get('/university-erp/integrations/runs');
export const getUniversityIntegrationConnections = () => api.get('/university-erp/integrations/connections');
export const saveUniversityIntegrationConnection = (data) => api.post('/university-erp/integrations/connections', data);
export const smokeTestUniversityIntegrations = () => api.post('/university-erp/integrations/smoke-test');
export const runUniversityIntegration = (key) => api.post(`/university-erp/integrations/${key}/run`);
export const failUniversityIntegration = (key) => api.post(`/university-erp/integrations/${key}/fail`);
export const retryUniversityIntegrationRun = (id) => api.post(`/university-erp/integrations/runs/${id}/retry`);
export const simulateUniversityBankPaymentCallback = (data = {}) => api.post('/university-erp/integrations/bank/payment-callback', data);
export const exportUniversityLmsRoster = () => api.post('/university-erp/integrations/lms/roster-export');
export const dispatchUniversityNotifications = () => api.post('/university-erp/integrations/notification/dispatch');
export const exportUniversityGovernmentReport = () => api.post('/university-erp/integrations/government/report-export');
export const seedUniversityErpDemoData = () => api.post('/university-erp/demo/seed');

// AI foundation
export const getAiAccessibleStudents = async ({ forceRefresh = false } = {}) => {
  if (!forceRefresh && aiAccessibleStudentsCache) {
    return { data: aiAccessibleStudentsCache };
  }

  if (!forceRefresh && aiAccessibleStudentsPromise) {
    return aiAccessibleStudentsPromise;
  }

  aiAccessibleStudentsPromise = api.get('/ai/students/access-list')
    .then((response) => {
      aiAccessibleStudentsCache = response.data || [];
      return { ...response, data: aiAccessibleStudentsCache };
    })
    .finally(() => {
      aiAccessibleStudentsPromise = null;
    });

  return aiAccessibleStudentsPromise;
};

export const clearAiAccessibleStudentsCache = () => {
  aiAccessibleStudentsCache = null;
  aiAccessibleStudentsPromise = null;
};

export const getAiRiskSnapshots = (params) => api.get('/ai/risk-snapshots', { params });
export const createAiRiskSnapshot = (data) => api.post('/ai/risk-snapshots', data);
export const getAiRiskSnapshotById = (id) => api.get(`/ai/risk-snapshots/${id}`);
export const getAiStudentRiskSnapshots = (studentId) => api.get(`/ai/students/${studentId}/risk-snapshots`);
export const getAiRiskDashboard = (params) => api.get('/ai/risk-dashboard', { params });
export const getAiAdminRiskDashboardBundle = (params) => api.get('/ai/risk-dashboard/admin-bundle', { params });
export const getAiRiskDashboardDetail = (studentId) => api.get(`/ai/risk-dashboard/${studentId}`);
export const getAiRiskSummary = (params) => api.get('/ai/risk-dashboard/summary', { params });
export const recalculateAiRiskStudent = (studentId) => api.post(`/ai/risk-dashboard/${studentId}/recalculate`);
export const recalculateAiRiskScope = (data = {}) => api.post('/ai/risk-dashboard/recalculate', data);
export const getAiRiskAccessScope = () => api.get('/ai/risk-dashboard/filters/access-scope');
export const getAiRiskConfig = () => api.get('/ai/risk-config');
export const updateAiRiskConfig = (configKey, data) => api.put(`/ai/risk-config/${configKey}`, data);
export const getAiMessageDrafts = (params) => api.get('/ai/message-drafts', { params });
export const createAiMessageDraft = (data) => api.post('/ai/message-drafts', data);
export const getAiMessageDraftById = (id) => api.get(`/ai/message-drafts/${id}`);
export const updateAiMessageDraft = (id, data) => api.put(`/ai/message-drafts/${id}/content`, data);
export const approveAiMessageDraft = (id, data = {}) => api.post(`/ai/message-drafts/${id}/approve`, data);
export const rejectAiMessageDraft = (id, data = {}) => api.post(`/ai/message-drafts/${id}/reject`, data);
export const retryAiMessageDraftGeneration = (id) => api.post(`/ai/message-drafts/${id}/retry-generation`);
export const getAiMessageDraftAuditLogs = (id, params) => api.get(`/ai/message-drafts/${id}/audit-logs`, { params });
export const getAiAuditLogs = (params) => api.get('/ai/audit-logs', { params });
export const getAiAnalyticsSummaries = (params) => api.get('/ai/analytics-summaries', { params });
export const getAiAnalyticsSummaryById = (id) => api.get(`/ai/analytics-summaries/${id}`);
export const generateAiAnalyticsSummary = (data) => api.post('/ai/analytics-summaries/generate', data);
export const refreshAiAnalyticsSummary = (id, params) => api.post(`/ai/analytics-summaries/${id}/refresh`, null, { params });
export const getAiTeacherAnalyticsSummary = (params) => api.get('/ai/analytics-summaries/teacher-overview', { params });
export const getAiAdminAnalyticsSummary = (params) => api.get('/ai/analytics-summaries/admin-overview', { params });
export const getAiAnalyticsSummaryAuditLogs = (id, params) => api.get(`/ai/analytics-summaries/${id}/audit-logs`, { params });

// Users
export const getAllUsers = (params) => api.get('/users', { params });
export const getRoleOptions = () => api.get('/users/roles');
export const updateUserRoles = (id, data) => api.put(`/users/${id}/roles`, data);
export const getTeachers = () => api.get('/users/teachers');
export const getUserById = (id) => api.get(`/users/${id}`);
export const createTeacher = (data) => api.post('/users/teacher', data);
export const createStudent = (data) => api.post('/users/student', data);
export const createParent = (data) => api.post('/users/parent', data);

// Classes
export const getClasses = () => api.get('/classes');
export const createClass = (data) => api.post('/classes', data);
export const deactivateClass = (id) => api.delete(`/classes/${id}`);

// Subjects
export const getSubjects = (params) => normalizePaginatedArrayResponse('/subjects', params);
export const createSubject = (data) => api.post('/subjects', data);

// Teaching Assignments
export const getTeachingAssignments = (params) => normalizePaginatedArrayResponse('/teaching-assignments', params);
export const assignTeaching = (data) => api.post('/teaching-assignments/assign', data);
export const updateTeachingAssignment = (id, data) => api.put(`/teaching-assignments/${id}`, data);
export const deactivateTeachingAssignment = (id) => api.delete(`/teaching-assignments/${id}`);

// Schedules
export const getSchedules = (params) => normalizePaginatedArrayResponse('/schedules', params);
export const getScheduleById = (id) => api.get(`/schedules/${id}`);
export const createSchedule = (data) => api.post('/schedules', data);
export const updateSchedule = (id, data) => api.put(`/schedules/${id}`, data);
export const deleteSchedule = (id) => api.delete(`/schedules/${id}`);
export const generateSchedule = (clearExisting = true) =>
  api.post(`/schedules/generate?clearExisting=${clearExisting}`);

// Exam schedules
export const getExamSchedules = (params) => api.get('/exam-schedules', { params });
export const getExamScheduleById = (id) => api.get(`/exam-schedules/${id}`);
export const createExamSchedule = (data) => api.post('/exam-schedules', data);
export const updateExamSchedule = (id, data) => api.put(`/exam-schedules/${id}`, data);
export const deleteExamSchedule = (id) => api.delete(`/exam-schedules/${id}`);
export const getTeacherExamSchedules = (teacherId) => api.get(`/exam-schedules/teacher/${teacherId}`);
export const getStudentExamSchedules = (studentId) => api.get(`/exam-schedules/student/${studentId}`);
export const getAllExamResults = () => api.get('/grades/exams/results');
export const getExamResultById = (examResultId) => api.get(`/grades/exams/results/${examResultId}`);
export const upsertExamResult = (data) => api.post('/grades/exams/results', data);
export const updateExamResultPublishStatus = (examResultId, published) =>
  api.post(`/grades/exams/results/${examResultId}/publish`, { published });
export const getExamResultsByExamSchedule = (examScheduleId) => api.get(`/grades/exams/exam-schedule/${examScheduleId}`);
export const getExamScheduleRoster = (examScheduleId) => api.get(`/grades/exams/exam-schedule/${examScheduleId}/roster`);
export const getStudentExamResults = (studentId) => api.get(`/grades/exams/student/${studentId}`);
export const getTeacherExamResults = (teacherId) => api.get(`/grades/exams/teacher/${teacherId}`);
export const getTeachingAssignmentExamRoster = (teachingAssignmentId) =>
  api.get(`/grades/exams/teaching-assignment/${teachingAssignmentId}/roster`);
export const getClassExamRoster = (classId) => api.get(`/grades/exams/class/${classId}/roster`);

// Announcements
export const getAnnouncements = () => api.get('/announcements');
export const getAnnouncementById = (id) => api.get(`/announcements/${id}`);
export const createAnnouncement = (data) => api.post('/announcements', data);
export const deleteAnnouncement = (id) => api.delete(`/announcements/${id}`);

// Parent-Student
export const getParentStudents = (params) => api.get('/parent-students', { params });
export const linkParentStudent = (data) => api.post('/parent-students/link', data);

// Parent portal
export const getMyChildren = () => api.get('/parent-students/me/children');
export const getMyChildrenDashboard = () => api.get('/parent-students/me/children/dashboard');
export const getChildProfile = (studentId) => api.get(`/parent-students/me/children/${studentId}/profile`);
export const getParentWarning = (studentId, startDate, endDate) =>
  api.get(`/attendance/parent-warning/${studentId}?startDate=${startDate}&endDate=${endDate}`);

// Student Enrollments
export const getEnrollments = (params) => normalizePaginatedArrayResponse('/student-enrollments', params);
export const createEnrollment = (data) => api.post('/student-enrollments', data);

// Schedules (teacher/student views)
export const getTeacherSchedule = (teacherId) => api.get(`/schedules/teacher/${teacherId}`);
export const getStudentSchedule = (studentId) => api.get(`/schedules/student/${studentId}`);

// Periods
export const getPeriods = () => api.get('/periods');

// Homework Submissions (admin can view all)
export const getAllSubmissions = () => api.get('/homework-submissions');

// Teacher-specific endpoints
export const getMyTeachingAssignments = () => api.get('/teaching-assignments/me');
export const getTeachingAssignmentGroup = (taId) => api.get(`/teaching-assignments/${taId}`);
export const getTeachingAssignmentClassDetail = (taId, classId) =>
  api.get(`/teaching-assignments/${taId}/${classId}`);

export const getMyTeachingClasses = () => api.get('/classes/my-teaching');
export const getTeacherCalendar = (teacherId) => api.get(`/schedules/teacher/${teacherId}/calendar`);
export const getTeacherClasses = (teacherId) => api.get(`/schedules/teacher/${teacherId}/classes`);

export const getHomeworkForCurrentUser = () => api.get('/homework');
export const getHomeworkById = (id) => api.get(`/homework/${id}`);
export const getHomeworkByTeachingAssignment = (taId) => api.get(`/homework/teaching-assignment/${taId}`);
const buildHomeworkFormData = ({ payload, files = [], removeAttachmentIds = [] }) => {
  const formData = new FormData();
  formData.append('payload', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
  files.forEach((file) => formData.append('files', file));
  removeAttachmentIds.forEach((id) => formData.append('removeAttachmentIds', id));
  return formData;
};

export const createHomework = ({ payload, files = [] }) =>
  api.post('/homework', buildHomeworkFormData({ payload, files }), {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const createHomeworkForClass = (taId, classId, { payload, files = [] }) =>
  api.post(`/teaching-assignments/${taId}/${classId}/homework`, buildHomeworkFormData({ payload, files }), {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const updateHomework = (id, { payload, files = [], removeAttachmentIds = [] }) =>
  api.put(`/homework/${id}`, buildHomeworkFormData({ payload, files, removeAttachmentIds }), {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
export const deleteHomework = (id) => api.delete(`/homework/${id}`);

export const getSubmissionsForHomework = (homeworkId) =>
  api.get(`/homework-submissions/homework/${homeworkId}`);

export const submitHomework = (homeworkId, { submissionText, file }) => {
  const formData = new FormData();
  if (submissionText) formData.append('submissionText', submissionText);
  if (file) formData.append('file', file);
  return api.post(`/homework-submissions/homework/${homeworkId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const uploadFile = (file, subfolder = 'general') => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('subfolder', subfolder);
  return api.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const getFileDownloadUrl = (filePath) => `/api/files/download/${filePath}`;

export const downloadFileAuthenticated = async (url) => {
  const path = url.startsWith('/api/') ? url.replace('/api/', '/') : url;
  const res = await api.get(path, { responseType: 'blob' });
  const contentDisposition = res.headers['content-disposition'];
  let filename = 'download';
  if (contentDisposition) {
    const match = contentDisposition.match(/filename="?([^"]+)"?/);
    if (match) filename = match[1];
  }
  const blob = new Blob([res.data]);
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(link.href);
};

export const fetchAuthenticatedFileBlobUrl = async (url) => {
  const path = url.startsWith('/api/') ? url.replace('/api/', '/') : url;
  const res = await api.get(path, { responseType: 'blob' });
  return {
    blobUrl: URL.createObjectURL(res.data),
    contentType: res.data?.type || res.headers['content-type'] || 'application/octet-stream',
  };
};

export const gradeSubmission = (submissionId, data) =>
  api.patch(`/homework-grading/submissions/${submissionId}`, data);
export const gradeSubmissionsBulk = (homeworkId, data) =>
  api.patch(`/homework-grading/homework/${homeworkId}/bulk`, data);

export const markAttendance = (data) => api.post('/attendance/mark', data);
export const markBulkAttendance = (data) => api.post('/attendance/mark-bulk', data);
export const getStudentAttendance = (studentId, startDate, endDate) =>
  api.get(`/attendance/student/${studentId}?startDate=${startDate}&endDate=${endDate}`);
export const getClassAttendanceStats = (classId, startDate, endDate) =>
  api.get(`/attendance/statistics/class/${classId}?startDate=${startDate}&endDate=${endDate}`);
export const getClassAttendanceByDate = (classId, date) =>
  api.get(`/attendance/class/${classId}/date?date=${date}`);
export const getClassAttendanceDates = (classId, startDate, endDate) =>
  api.get(`/attendance/class/${classId}/dates?startDate=${startDate}&endDate=${endDate}`);

export const assignGrade = (data) => api.post('/grades/assign', data);
export const updateGrade = (gradeId, data) => api.put(`/grades/${gradeId}`, data);
export const getStudentGrades = (studentId, quarter) =>
  api.get(`/grades/student/${studentId}${quarter ? `?quarter=${quarter}` : ''}`);
export const getStudentGPA = (studentId, quarter) =>
  api.get(`/grades/student/${studentId}/gpa${quarter ? `?quarter=${quarter}` : ''}`);
export const getGradeTrends = (studentId) =>
  api.get(`/grades/student/${studentId}/trends`);

export const getHomeworkForStudent = () => api.get('/homework/student');
export const getHomeworkForStudentId = (studentId) => api.get(`/homework/student/${studentId}`);

export const getMySubmission = (homeworkId) =>
  api.get(`/homework-submissions/homework/${homeworkId}/me`);
export const getSubmissionForStudent = (homeworkId, studentId) =>
  api.get(`/homework-submissions/homework/${homeworkId}/student/${studentId}`);
export const getSubmissionsForStudentInClass = (classId, studentId) =>
  api.get(`/homework-submissions/class/${classId}/student/${studentId}`);

export const getStudentCalendar = (studentId) =>
  api.get(`/schedules/student/${studentId}/calendar`);

export const getTranslations = (entityType, entityId, locale) =>
  api.get(`/translations/${entityType}/${entityId}${locale ? `?locale=${locale}` : ''}`);
export const getTranslationsBatch = (entityType, ids) =>
  api.get(`/translations/${entityType}/batch?ids=${ids.join(',')}`);
export const setTranslation = (data) => api.put('/translations', data);
export const setTranslationsBulk = (translations) =>
  api.put('/translations/bulk', { translations });
export const deleteTranslations = (entityType, entityId) =>
  api.delete(`/translations/${entityType}/${entityId}`);
