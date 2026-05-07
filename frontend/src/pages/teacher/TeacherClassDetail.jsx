import { useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import {
  getMyTeachingAssignments,
  getEnrollments,
  getHomeworkByTeachingAssignment,
  getSubmissionsForHomework,
  createHomeworkForClass,
  updateHomework,
  deleteHomework,
  getClassAttendanceStats,
  getClassAttendanceByDate,
  getClassAttendanceDates,
  markBulkAttendance,
  getStudentGrades,
  getStudentAttendance,
  getUserById,
  assignGrade,
  gradeSubmission,
  downloadFileAuthenticated,
} from '../../api/endpoints';
import HomeworkAttachmentViewer from '../../components/HomeworkAttachmentViewer';
import FloatingNotification from '../../components/ui/FloatingNotification';
import SearchableSelect from '../../components/ui/SearchableSelect';
import SelectMenu from '../../components/ui/SelectMenu';

const ATTENDANCE_STATUS_COLOR = {
  PRESENT: '#065F46',
  present: '#065F46',
  ABSENT: '#991B1B',
  absent: '#991B1B',
  LATE: '#92400E',
  late: '#92400E',
  EXCUSED: '#1E40AF',
  excused: '#1E40AF',
  SICK: '#6B21A8',
  sick: '#6B21A8',
};

const ATTENDANCE_STATUS_BG = {
  PRESENT: '#D1FAE5',
  present: '#D1FAE5',
  ABSENT: '#FEE2E2',
  absent: '#FEE2E2',
  LATE: '#FEF3C7',
  late: '#FEF3C7',
  EXCUSED: '#DBEAFE',
  excused: '#DBEAFE',
  SICK: '#F3E8FF',
  sick: '#F3E8FF',
};

function DetailField({ label, value, fullWidth = false }) {
  if (!value) return null;
  return (
    <div style={fullWidth ? { gridColumn: '1 / -1' } : undefined}>
      <span style={{ color: 'var(--text-muted)', fontWeight: 600 }}>{label}</span>
      <div>{value}</div>
    </div>
  );
}

function StatusBadge({ status }) {
  return (
    <span className="badge" style={{
      background: ATTENDANCE_STATUS_BG[status] || '#F3F4F6',
      color: ATTENDANCE_STATUS_COLOR[status] || 'var(--text)',
    }}>
      {(status || '').toUpperCase()}
    </span>
  );
}

export default function TeacherClassDetail() {
  const { t } = useTranslation();
  const TABS = [t('teacher.classDetail.students'), t('teacher.classDetail.homework'), t('teacher.classDetail.attendance'), t('teacher.classDetail.grades')];
  const tabOptions = TABS.map((tab) => ({ value: tab, label: tab }));
  const { classId } = useParams();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState(null);
  const [loading, setLoading] = useState(true);
  const [classInfo, setClassInfo] = useState(null);
  const [subjects, setSubjects] = useState([]); // [{subject, taId}]
  const [students, setStudents] = useState([]);

  useEffect(() => {
    Promise.all([
      getMyTeachingAssignments(),
      getEnrollments(),
    ])
      .then(([taRes, enrRes]) => {
        const myTAs = taRes.data || [];
        // Find TAs for this class
        const classAssignments = myTAs.filter(
          (a) => a.classInfo && String(a.classInfo.id) === String(classId)
        );
        if (classAssignments.length > 0) {
          setClassInfo(classAssignments[0].classInfo);
          setSubjects(
            classAssignments
              .filter((a) => a.subject)
              .map((a) => ({
                name: a.subject.name,
                code: a.subject.subjectCode,
                taId: a.id,
                hoursPerWeek: a.subject.hoursPerWeek,
                subjectId: a.subject.id,
              }))
          );
        }
        // Students enrolled in this class
        const enrolled = (enrRes.data || []).filter(
          (e) => String(e.classId || e.classEntity?.id) === String(classId) && e.status !== 'WITHDRAWN'
        );
        setStudents(
          enrolled.map((e) => ({
            id: e.studentId || e.student?.id,
            name: [e.student?.firstName, e.student?.lastName].filter(Boolean).join(' ') || e.studentUsername || e.student?.username || `Student ${e.studentId}`,
            username: e.studentUsername || e.student?.username || '',
            className: e.className || '',
            studentNumber: e.studentNumber,
          }))
        );
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [classId]);

  if (loading) return <div className="loading"><div className="spinner" />{t('teacher.classDetail.loading')}</div>;

  if (!classInfo) {
    return (
      <div>
        <div className="page-header">
          <h1>{t('teacher.classDetail.notFound')}</h1>
          <Link to="/teacher/classes" className="btn btn-secondary">{t('common.back')}</Link>
        </div>
        <div className="alert alert-error">{t('teacher.classDetail.notFoundDesc')}</div>
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{classInfo.className}</h1>
          <div className="teacher-card-meta teacher-class-detail-meta" style={{ fontSize: '0.85rem' }}>
            {t('admin.classes.gradeN', { n: classInfo.grade })} – {t('admin.createClass.section')} {classInfo.section}
            {classInfo.roomNumber ? ` • ${t('teacher.classDetail.room')} ${classInfo.roomNumber}` : ''}
            {' • '}{subjects.map((s) => s.name).join(', ')}
          </div>
        </div>
        <Link to="/teacher/classes" className="btn btn-secondary">{t('teacher.classDetail.backToClasses')}</Link>
      </div>

      <div className="teacher-tab-select">
        <SelectMenu
          options={tabOptions}
          value={activeTab || TABS[0]}
          onChange={(value) => setActiveTab(value)}
          placeholder={t('teacher.classDetail.students')}
        />
      </div>

      <div className="teacher-tabs">
        {TABS.map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`teacher-tab${activeTab === tab ? ' active' : ''}`}
          >
            {tab}
          </button>
        ))}
      </div>

      {(activeTab === TABS[0] || activeTab === null) && <StudentsTab students={students} subjects={subjects} classId={classId} />}
      {activeTab === TABS[1] && (
        <HomeworkTab subjects={subjects} classId={classId} students={students} />
      )}
      {activeTab === TABS[2] && (
        <AttendanceTab subjects={subjects} classId={classId} students={students} />
      )}
      {activeTab === TABS[3] && (
        <GradesTab subjects={subjects} classId={classId} students={students} />
      )}
    </div>
  );
}

/* ─── Students Tab ────────────────────────────────────────────── */
function StudentsTab({ students, subjects, classId }) {
  const { t } = useTranslation();
  const detailPanelRef = useRef(null);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [studentInfo, setStudentInfo] = useState(null);
  const [attendance, setAttendance] = useState([]);
  const [grades, setGrades] = useState([]);
  const [detailLoading, setDetailLoading] = useState(false);

  const loadStudent = (student) => {
    if (selectedStudent?.id === student.id) {
      setSelectedStudent(null);
      return;
    }

    setSelectedStudent(student);
    setDetailLoading(true);
    const now = new Date();
    const startOfYear = `${now.getFullYear()}-01-01`;
    const endOfYear = `${now.getFullYear()}-12-31`;
    Promise.all([
      getUserById(student.id).catch(() => ({ data: null })),
      getStudentAttendance(student.id, startOfYear, endOfYear).catch(() => ({ data: [] })),
      ...([1,2,3,4].map(q =>
        getStudentGrades(student.id, q)
          .then(r => (r.data || []).map(g => ({ ...g, quarter: q })))
          .catch(() => [])
      )),
    ]).then(([userRes, attRes, ...gradeArrays]) => {
      setStudentInfo(userRes.data);
      // Filter attendance to only this teacher's subjects
      const subjectNames = new Set(subjects.map(s => s.name));
      setAttendance((attRes.data || []).filter(a => subjectNames.has(a.subjectName)));
      // Filter grades to only this teacher's subjects
      const allGrades = gradeArrays.flat();
      setGrades(allGrades.filter(g => subjectNames.has(g.subjectName)));
      if (window.matchMedia('(max-width: 768px)').matches) {
        window.requestAnimationFrame(() => {
          detailPanelRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
      }
    }).finally(() => setDetailLoading(false));
  };

  if (students.length === 0) {
    return <div className="card"><div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>{t('teacher.classDetail.noStudents')}</div></div>;
  }

  // Attendance summary per status
  const attPresent = attendance.filter(a => a.status === 'PRESENT').length;
  const attAbsent = attendance.filter(a => a.status === 'ABSENT').length;
  const attLate = attendance.filter(a => a.status === 'LATE').length;
  const attExcused = attendance.filter(a => ['EXCUSED', 'SICK'].includes(a.status)).length;
  const attRate = attendance.length > 0 ? (attPresent * 100 / attendance.length).toFixed(1) : null;

  return (
    <div className={`grid-student-detail${selectedStudent ? ' has-detail' : ''}`}>
      <div className="card">
        <div className="table-container desktop-table">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>{t('teacher.classDetail.student')}</th>
                <th>{t('admin.users.username')}</th>
                <th>{t('teacher.classDetail.studentNo')}</th>
              </tr>
            </thead>
            <tbody>
              {students.map((s, i) => (
                <tr
                  key={s.id}
                  onClick={() => loadStudent(s)}
                  style={{
                    cursor: 'pointer',
                    background: selectedStudent?.id === s.id ? 'var(--primary-light)' : undefined,
                    transition: 'background 0.15s',
                  }}
                  onMouseEnter={(e) => { if (selectedStudent?.id !== s.id) e.currentTarget.style.background = 'rgba(26,107,92,0.04)'; }}
                  onMouseLeave={(e) => { if (selectedStudent?.id !== s.id) e.currentTarget.style.background = ''; }}
                >
                  <td>{i + 1}</td>
                  <td style={{ fontWeight: selectedStudent?.id === s.id ? 600 : 400 }}>{s.name}</td>
                  <td>{s.username ? `@${s.username}` : '-'}</td>
                  <td>{s.studentNumber || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="card-body teacher-mobile-card-list">
          {students.map((student, index) => (
            <button
              key={student.id}
              type="button"
              className="teacher-mobile-card"
              onClick={() => loadStudent(student)}
              style={{
                textAlign: 'left',
                cursor: 'pointer',
                background: selectedStudent?.id === student.id ? 'var(--primary-light)' : undefined,
              }}
            >
              <div className="teacher-mobile-card-head">
                <div>
                  <h3 className="teacher-mobile-card-title">{student.name}</h3>
                  <div className="teacher-card-meta">{student.username ? `@${student.username}` : t('teacher.classDetail.student')}</div>
                </div>
                <span className="badge badge-info">#{index + 1}</span>
              </div>

              <div className="teacher-mobile-card-grid">
                <div className="teacher-mobile-card-field">
                  <span>{t('teacher.classDetail.studentNo')}</span>
                  <strong>{student.studentNumber || '-'}</strong>
                </div>
                <div className="teacher-mobile-card-field">
                  <span>{t('common.id')}</span>
                  <strong>{student.id}</strong>
                </div>
              </div>

              <div className="teacher-mobile-card-footer">
                <span>{selectedStudent?.id === student.id ? t('common.active') : t('teacher.classDetail.student')}</span>
                <strong>{selectedStudent?.id === student.id ? 'Viewing profile' : 'Tap to view details'}</strong>
              </div>
            </button>
          ))}
        </div>
      </div>

      {selectedStudent && (
        <div ref={detailPanelRef} className="teacher-student-detail-panel" style={{ animation: 'fadeInUp 0.25s ease' }}>
          {detailLoading ? (
            <div className="card"><div className="card-body"><div className="loading"><div className="spinner" />{t('teacher.classDetail.loadingStudentData')}</div></div></div>
          ) : (
            <>
              <div className="card" style={{ marginBottom: '0.75rem' }}>
                <div style={{
                  background: 'var(--sidebar-bg)', padding: '1rem 1.25rem',
                  display: 'flex', alignItems: 'center', gap: '0.75rem',
                }}>
                  <div style={{
                    width: 44, height: 44, borderRadius: '50%',
                    background: 'linear-gradient(135deg, var(--primary), var(--sidebar-active))',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    color: 'white', fontFamily: 'var(--font-heading)', fontWeight: 800, fontSize: '1.1rem',
                  }}>
                    {(studentInfo?.firstName?.[0] || selectedStudent.name[0]).toUpperCase()}
                  </div>
                  <div>
                    <div style={{ color: 'white', fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: '1rem' }}>
                      {studentInfo ? `${studentInfo.firstName} ${studentInfo.lastName}` : selectedStudent.name}
                    </div>
                    <div style={{ color: 'var(--sidebar-text)', fontSize: '0.75rem' }}>
                      {selectedStudent.studentNumber || t('teacher.classDetail.noStudentNumber')}
                    </div>
                  </div>
                </div>
                <div className="card-body" style={{ padding: '0.75rem 1.25rem' }}>
                  <div className="teacher-responsive-meta">
                    <DetailField label={t('admin.userDetail.email')} value={studentInfo?.email} />
                    <DetailField label={t('admin.userDetail.phone')} value={studentInfo?.phone} />
                    <DetailField label={t('admin.userDetail.dateOfBirth')} value={studentInfo?.dateOfBirth ? new Date(studentInfo.dateOfBirth).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) : ''} />
                    <DetailField label={t('admin.userDetail.gender')} value={studentInfo?.gender ? <span style={{ textTransform: 'capitalize' }}>{studentInfo.gender}</span> : ''} />
                    <DetailField label={t('admin.userDetail.address')} value={studentInfo?.address} fullWidth />
                  </div>
                </div>
              </div>

              <div className="card" style={{ marginBottom: '0.75rem' }}>
                <div className="card-body" style={{ padding: '0.75rem 1.25rem' }}>
                  <h4 style={{
                    fontSize: '0.78rem', fontFamily: 'var(--font-heading)', fontWeight: 700,
                    color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em',
                    marginBottom: '0.5rem',
                  }}>
                    Attendance — My Subjects ({new Date().getFullYear()})
                  </h4>
                  {attendance.length === 0 ? (
                    <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>{t('teacher.classDetail.noAttendanceRecords')}</div>
                  ) : (
                    <>
                      <div className="teacher-stat-strip" style={{ marginBottom: '0.6rem' }}>
                        {[
                          { label: 'Rate', value: `${attRate}%`, bg: 'var(--primary-light)', color: 'var(--primary)' },
                          { label: 'Present', value: attPresent, bg: '#D1FAE5', color: '#065F46' },
                          { label: 'Absent', value: attAbsent, bg: '#FEE2E2', color: 'var(--danger)' },
                          { label: 'Late', value: attLate, bg: '#FEF3C7', color: 'var(--warning)' },
                          { label: 'Excused', value: attExcused, bg: '#E0E7FF', color: '#3730A3' },
                        ].map(s => (
                          <div key={s.label} className="teacher-stat-pill" style={{ background: s.bg }}>
                            <div style={{ fontSize: '1rem', fontWeight: 800, fontFamily: 'var(--font-heading)', color: s.color }}>{s.value}</div>
                            <div style={{ fontSize: '0.62rem', color: 'var(--text-muted)', fontWeight: 600 }}>{s.label}</div>
                          </div>
                        ))}
                      </div>
                      {attendance
                        .filter(a => ['ABSENT', 'LATE', 'SICK', 'EXCUSED'].includes(a.status))
                        .sort((a, b) => b.attendanceDate.localeCompare(a.attendanceDate))
                        .slice(0, 5).length > 0 && (
                          <div>
                            <div style={{ fontSize: '0.72rem', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '0.3rem' }}>Recent Issues</div>
                            {attendance
                              .filter(a => ['ABSENT', 'LATE', 'SICK', 'EXCUSED'].includes(a.status))
                              .sort((a, b) => b.attendanceDate.localeCompare(a.attendanceDate))
                              .slice(0, 5)
                              .map((a, i, arr) => (
                              <div key={i} style={{
                                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                                padding: '0.25rem 0', borderBottom: i < arr.length - 1 ? '1px solid var(--border)' : 'none',
                                fontSize: '0.78rem',
                              }}>
                                <span>{new Date(a.attendanceDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} — {a.subjectName}</span>
                                <span style={{
                                  fontSize: '0.68rem', fontWeight: 700, padding: '0.1rem 0.4rem', borderRadius: '4px',
                                  background: a.status === 'ABSENT' ? '#FEE2E2' : a.status === 'LATE' ? '#FEF3C7' : '#E0E7FF',
                                  color: a.status === 'ABSENT' ? 'var(--danger)' : a.status === 'LATE' ? 'var(--warning)' : '#3730A3',
                                }}>{a.status}</span>
                              </div>
                            ))}
                          </div>
                      )}
                    </>
                  )}
                </div>
              </div>

              {/* Grades card */}
              <div className="card">
                <div className="card-body" style={{ padding: '0.75rem 1.25rem' }}>
                  <h4 style={{
                    fontSize: '0.78rem', fontFamily: 'var(--font-heading)', fontWeight: 700,
                    color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em',
                    marginBottom: '0.5rem',
                  }}>
                    Grades — My Subjects
                  </h4>
                  {grades.length === 0 ? (
                    <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>{t('teacher.classDetail.noGrades')}</div>
                  ) : (
                    <>
                      <div className="table-container desktop-table">
                        <table>
                          <thead>
                            <tr>
                              <th>Subject</th>
                              <th>Q</th>
                              <th>Type</th>
                              <th>Grade</th>
                            </tr>
                          </thead>
                          <tbody>
                            {grades
                              .sort((a, b) => a.subjectName.localeCompare(b.subjectName) || a.quarter - b.quarter)
                              .map((g, i) => (
                                <tr key={i}>
                                  <td style={{ fontSize: '0.82rem' }}>{g.subjectName}</td>
                                  <td>Q{g.quarter}</td>
                                  <td>
                                    <span style={{
                                      fontSize: '0.68rem', fontWeight: 600, padding: '0.1rem 0.35rem',
                                      borderRadius: '4px', background: 'var(--primary-light)', color: 'var(--primary)',
                                    }}>{g.gradeType}</span>
                                  </td>
                                  <td>
                                    <span style={{
                                      fontWeight: 800, fontFamily: 'var(--font-heading)',
                                      color: g.gradeValue >= 70 ? 'var(--success)' : g.gradeValue >= 50 ? 'var(--warning)' : 'var(--danger)',
                                    }}>{g.gradeValue}</span>
                                  </td>
                                </tr>
                              ))}
                          </tbody>
                        </table>
                      </div>
                      <div className="teacher-mobile-card-list">
                        {grades
                          .sort((a, b) => a.subjectName.localeCompare(b.subjectName) || a.quarter - b.quarter)
                          .map((g, i) => (
                            <article key={`grade-mobile-${i}`} className="teacher-mobile-card">
                              <div className="teacher-mobile-card-head">
                                <div>
                                  <h3 className="teacher-mobile-card-title">{g.subjectName}</h3>
                                  <div className="teacher-card-meta">Q{g.quarter}</div>
                                </div>
                                <span style={{
                                  fontWeight: 800, fontFamily: 'var(--font-heading)',
                                  color: g.gradeValue >= 70 ? 'var(--success)' : g.gradeValue >= 50 ? 'var(--warning)' : 'var(--danger)',
                                }}>{g.gradeValue}</span>
                              </div>
                              <div className="teacher-mobile-card-field">
                                <span>Type</span>
                                <strong>{g.gradeType}</strong>
                              </div>
                            </article>
                          ))}
                      </div>
                    </>
                  )}
                </div>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
}

/* ─── Homework Tab ────────────────────────────────────────────── */
function HomeworkTab({ subjects, classId, students }) {
  const { t } = useTranslation();
  const [selectedSubject, setSelectedSubject] = useState(subjects[0]?.taId || '');
  const [homework, setHomework] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showCreate, setShowCreate] = useState(false);
  const [viewingHw, setViewingHw] = useState(null);
  const [editingHw, setEditingHw] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [subLoading, setSubLoading] = useState(false);

  const [form, setForm] = useState({ title: '', description: '', dueDate: '', maxScore: 100, type: 'HOMEWORK' });
  const [newFiles, setNewFiles] = useState([]);
  const [existingAttachments, setExistingAttachments] = useState([]);
  const [removeAttachmentIds, setRemoveAttachmentIds] = useState([]);
  const [formError, setFormError] = useState('');
  const [creating, setCreating] = useState(false);

  const ALLOWED_ATTACHMENT_TYPES = ['image/jpeg', 'image/png', 'application/pdf'];
  const MAX_ATTACHMENT_SIZE_BYTES = 10 * 1024 * 1024;

  useEffect(() => {
    if (!selectedSubject) return;
    setLoading(true);
    getHomeworkByTeachingAssignment(selectedSubject)
      .then((res) => setHomework(res.data || []))
      .catch(() => setHomework([]))
      .finally(() => setLoading(false));
  }, [selectedSubject]);

  const resetForm = () => {
    setForm({ title: '', description: '', dueDate: '', maxScore: 100, type: 'HOMEWORK' });
    setNewFiles([]);
    setExistingAttachments([]);
    setRemoveAttachmentIds([]);
    setFormError('');
    setEditingHw(null);
  };

  const openCreateForm = () => {
    if (showCreate && !editingHw) {
      setShowCreate(false);
      resetForm();
      return;
    }
    resetForm();
    setShowCreate(true);
  };

  const openEditForm = (hw) => {
    setEditingHw(hw);
    setForm({
      title: hw.title || '',
      description: hw.description || '',
      dueDate: hw.dueDate || '',
      maxScore: hw.maxScore ?? 100,
      type: hw.type || 'HOMEWORK',
    });
    setExistingAttachments(hw.attachments || []);
    setRemoveAttachmentIds([]);
    setNewFiles([]);
    setFormError('');
    setShowCreate(true);
  };

  const validateFiles = (files) => {
    for (const file of files) {
      if (!ALLOWED_ATTACHMENT_TYPES.includes(file.type)) {
        return `${file.name} is not supported. Please upload JPG, PNG, or PDF files only.`;
      }
      if (file.size > MAX_ATTACHMENT_SIZE_BYTES) {
        return `${file.name} is larger than 10MB.`;
      }
    }
    return '';
  };

  const handleFileSelection = (event) => {
    const selectedFiles = Array.from(event.target.files || []);
    const nextFiles = [...newFiles, ...selectedFiles];
    const validationError = validateFiles(nextFiles);
    if (validationError) {
      setFormError(validationError);
      event.target.value = '';
      return;
    }
    setFormError('');
    setNewFiles(nextFiles);
    event.target.value = '';
  };

  const removeNewFile = (fileToRemove) => {
    setNewFiles((prev) => prev.filter((file) => !(file.name === fileToRemove.name && file.size === fileToRemove.size && file.lastModified === fileToRemove.lastModified)));
  };

  const toggleExistingAttachment = (attachmentId) => {
    setRemoveAttachmentIds((prev) => (
      prev.includes(attachmentId)
        ? prev.filter((id) => id !== attachmentId)
        : [...prev, attachmentId]
    ));
  };

  const handleSave = async () => {
    if (!form.title || !form.dueDate) return;

    const validationError = validateFiles(newFiles);
    if (validationError) {
      setFormError(validationError);
      return;
    }

    setCreating(true);
    setFormError('');
    try {
      const payload = {
        title: form.title,
        description: form.description,
        dueDate: form.dueDate,
        maxScore: parseInt(form.maxScore, 10) || 100,
        type: form.type,
      };

      const res = editingHw
        ? await updateHomework(editingHw.id, { payload, files: newFiles, removeAttachmentIds })
        : await createHomeworkForClass(selectedSubject, classId, { payload, files: newFiles });

      setHomework((prev) => (
        editingHw
          ? prev.map((item) => (item.id === editingHw.id ? res.data : item))
          : [res.data, ...prev]
      ));
      setShowCreate(false);
      resetForm();
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to save homework');
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (homeworkId) => {
    if (!window.confirm('Delete this homework and all of its attachments?')) {
      return;
    }

    try {
      await deleteHomework(homeworkId);
      setHomework((prev) => prev.filter((item) => item.id !== homeworkId));
      if (viewingHw?.id === homeworkId) {
        setViewingHw(null);
      }
      if (editingHw?.id === homeworkId) {
        resetForm();
        setShowCreate(false);
      }
    } catch (err) {
      alert('Failed to delete homework: ' + (err.response?.data?.message || err.message));
    }
  };

  const viewSubmissions = (hw) => {
    setViewingHw(hw);
    setSubLoading(true);
    getSubmissionsForHomework(hw.id)
      .then((res) => setSubmissions(res.data || []))
      .catch(() => setSubmissions([]))
      .finally(() => setSubLoading(false));
  };

  const handleGrade = (submissionId, score, feedback) => {
    gradeSubmission(submissionId, { score: parseInt(score), feedback })
      .then((res) => {
        setSubmissions((prev) =>
          prev.map((s) => (s.id === submissionId ? res.data : s))
        );
      })
      .catch((err) => alert('Failed to grade: ' + (err.response?.data?.message || err.message)));
  };

  if (viewingHw) {
    return (
      <div>
        <button className="btn btn-secondary btn-sm" onClick={() => setViewingHw(null)} style={{ marginBottom: '1rem' }}>
          {t('teacher.classDetail.backToHomework')}
        </button>
        <div className="card" style={{ marginBottom: '1rem' }}>
          <div className="card-body">
            <h3 style={{ marginBottom: '0.25rem' }}>{viewingHw.title}</h3>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              {t('teacher.classDetail.due')}: {viewingHw.dueDate} • {t('teacher.classDetail.maxScore')}: {viewingHw.maxScore} • {t('teacher.classDetail.type')}: {viewingHw.type}
            </div>
            {viewingHw.description && (
              <p style={{ marginTop: '0.5rem', fontSize: '0.9rem' }}>{viewingHw.description}</p>
            )}
            <div style={{ marginTop: '1rem' }}>
              <HomeworkAttachmentViewer attachments={viewingHw.attachments || []} compact />
            </div>
          </div>
        </div>
        <div className="card">
          <div className="card-body">
            <h4 style={{ marginBottom: '1rem' }}>{t('teacher.classDetail.submissions')} ({submissions.length})</h4>
            {subLoading ? (
              <div className="loading"><div className="spinner" />{t('teacher.classDetail.loadingSubmissions')}</div>
            ) : submissions.length === 0 ? (
              <p style={{ color: 'var(--text-muted)' }}>{t('teacher.classDetail.noSubmissions')}</p>
            ) : (
              <>
                <div className="table-container desktop-table">
                  <table>
                    <thead>
                      <tr>
                        <th>{t('teacher.classDetail.studentId')}</th>
                        <th>{t('teacher.classDetail.student')}</th>
                        <th>{t('teacher.classDetail.submittedAt')}</th>
                        <th>{t('teacher.classDetail.content')}</th>
                        <th>{t('common.status')}</th>
                        <th>{t('teacher.classDetail.score')}</th>
                        <th>{t('teacher.classDetail.feedback')}</th>
                        <th>{t('common.actions')}</th>
                      </tr>
                    </thead>
                    <tbody>
                      {submissions.map((s) => (
                        <SubmissionRow
                          key={s.id}
                          submission={s}
                          studentName={students.find((student) => String(student.id) === String(s.studentId))?.name}
                          maxScore={viewingHw.maxScore}
                          onGrade={handleGrade}
                        />
                      ))}
                    </tbody>
                  </table>
                </div>
                <div className="teacher-mobile-card-list">
                  {submissions.map((s) => (
                    <SubmissionRow
                      key={`mobile-${s.id}`}
                      submission={s}
                      studentName={students.find((student) => String(student.id) === String(s.studentId))?.name}
                      maxScore={viewingHw.maxScore}
                      onGrade={handleGrade}
                      mobile
                    />
                  ))}
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="teacher-inline-controls">
        <div className="teacher-control-wide">
          <SearchableSelect
            options={subjects.map((s) => ({ value: String(s.taId), label: s.name }))}
            value={selectedSubject}
            onChange={(value) => setSelectedSubject(String(value || ''))}
            placeholder={t('teacher.classDetail.subject')}
            searchPlaceholder={t('common.search')}
            emptyLabel="No subjects found"
          />
        </div>
        <button className="btn btn-primary btn-sm" onClick={openCreateForm}>
          {showCreate && !editingHw ? t('common.cancel') : t('teacher.classDetail.createHomework')}
        </button>
      </div>

      {showCreate && (
        <div className="card" style={{ marginBottom: '1rem' }}>
          <div className="card-body">
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap' }}>
              <h4 style={{ margin: 0 }}>{editingHw ? 'Edit Homework' : t('teacher.classDetail.newHomework')}</h4>
              {editingHw && (
                <button className="btn btn-secondary btn-sm" onClick={() => { resetForm(); setShowCreate(false); }}>
                  {t('common.cancel')}
                </button>
              )}
            </div>
            {formError && <div className="alert alert-error" style={{ marginBottom: '1rem' }}>{formError}</div>}
            <div className="form-row">
              <div className="form-group">
                <label>{t('teacher.classDetail.titleLabel')}</label>
                <input className="form-control" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
              </div>
              <div className="form-group">
                <label>{t('teacher.classDetail.dueDate')}</label>
                <input type="date" className="form-control" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
              </div>
            </div>
            <div className="form-group">
              <label>{t('teacher.classDetail.description')}</label>
              <textarea className="form-control" rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>{t('teacher.classDetail.maxScore')}</label>
                <input type="number" className="form-control" value={form.maxScore} onChange={(e) => setForm({ ...form, maxScore: e.target.value })} />
              </div>
              <div className="form-group">
                <label>{t('teacher.classDetail.type')}</label>
                <SelectMenu
                  options={[
                    { value: 'HOMEWORK', label: t('teacher.classDetail.homework') },
                    { value: 'QUIZ', label: t('teacher.classDetail.quiz') },
                    { value: 'PROJECT', label: t('teacher.classDetail.project') },
                    { value: 'TEST', label: t('teacher.classDetail.test') },
                  ]}
                  value={form.type}
                  onChange={(value) => setForm({ ...form, type: value })}
                  placeholder={t('teacher.classDetail.type')}
                />
              </div>
            </div>
            <div className="form-group">
              <label>{t('teacher.classDetail.attachment')}s</label>
              <input
                type="file"
                className="form-control"
                accept=".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf"
                multiple
                onChange={handleFileSelection}
              />
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                JPG, PNG, and PDF up to 10MB each.
              </div>
            </div>
            {existingAttachments.length > 0 && (
              <div className="form-group">
                <label>Existing attachments</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {existingAttachments.map((attachment) => (
                    <label key={attachment.id ?? `${attachment.kind}-${attachment.originalFilename}`} style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      gap: '0.75rem',
                      border: '1px solid var(--border)',
                      borderRadius: '8px',
                      padding: '0.65rem 0.8rem',
                      background: attachment.id && removeAttachmentIds.includes(attachment.id) ? '#FEF2F2' : 'white',
                    }}>
                      <span style={{ display: 'flex', flexDirection: 'column', gap: '0.15rem' }}>
                        <span style={{ fontWeight: 600, wordBreak: 'break-word' }}>{attachment.originalFilename}</span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{attachment.mimeType}</span>
                      </span>
                      {attachment.id ? (
                        <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem' }}>
                          <input
                            type="checkbox"
                            checked={removeAttachmentIds.includes(attachment.id)}
                            onChange={() => toggleExistingAttachment(attachment.id)}
                          />
                          Remove
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Legacy attachment</span>
                      )}
                    </label>
                  ))}
                </div>
              </div>
            )}
            {newFiles.length > 0 && (
              <div className="form-group">
                <label>Selected files</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {newFiles.map((file) => (
                    <div key={`${file.name}-${file.size}-${file.lastModified}`} style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      gap: '0.75rem',
                      border: '1px solid var(--border)',
                      borderRadius: '8px',
                      padding: '0.65rem 0.8rem',
                      background: '#F8FAFC',
                    }}>
                      <span style={{ display: 'flex', flexDirection: 'column', gap: '0.15rem' }}>
                        <span style={{ fontWeight: 600, wordBreak: 'break-word' }}>{file.name}</span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{file.type || 'File'} • {(file.size / (1024 * 1024)).toFixed(2)} MB</span>
                      </span>
                      <button type="button" className="btn btn-secondary btn-sm" onClick={() => removeNewFile(file)}>
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
            <div className="teacher-action-row">
              <button className="btn btn-primary" onClick={handleSave} disabled={creating}>
                {creating ? (editingHw ? t('common.saving') : t('common.creating')) : (editingHw ? t('common.save') : t('teacher.classDetail.createHomework'))}
              </button>
              {editingHw && (
                <button className="btn btn-secondary" onClick={() => { resetForm(); setShowCreate(false); }}>
                  {t('common.cancel')}
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="loading"><div className="spinner" />{t('teacher.classDetail.loadingHomework')}</div>
      ) : homework.length === 0 ? (
        <div className="card"><div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>{t('teacher.classDetail.noHomework')}</div></div>
      ) : (
        <div className="card">
          <div className="table-container desktop-table">
            <table>
              <thead>
                <tr>
                  <th>{t('teacher.classDetail.titleLabel')}</th>
                  <th>{t('teacher.classDetail.type')}</th>
                  <th>{t('teacher.classDetail.dueDate')}</th>
                  <th>{t('teacher.classDetail.maxScore')}</th>
                  <th>{t('common.actions')}</th>
                </tr>
              </thead>
              <tbody>
                {homework.map((hw) => (
                  <tr key={hw.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{hw.title}</div>
                      {hw.description && (
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {hw.description.substring(0, 80)}{hw.description.length > 80 ? '...' : ''}
                        </div>
                      )}
                      {(hw.attachments?.length > 0 || hw.attachmentUrl) && (
                        <div style={{ fontSize: '0.78rem', color: 'var(--primary)', marginTop: '0.25rem' }}>
                          {(hw.attachments?.length || 1)} attachment{(hw.attachments?.length || 1) > 1 ? 's' : ''}
                        </div>
                      )}
                    </td>
                    <td><span className="badge badge-info">{hw.type}</span></td>
                    <td>{hw.dueDate}</td>
                    <td>{hw.maxScore}</td>
                    <td>
                      <div className="teacher-action-row">
                        <button className="btn btn-primary btn-sm" onClick={() => viewSubmissions(hw)}>
                          {t('teacher.classDetail.submissions')}
                        </button>
                        <button className="btn btn-secondary btn-sm" onClick={() => openEditForm(hw)}>
                          Edit
                        </button>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleDelete(hw.id)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="card-body teacher-mobile-card-list">
            {homework.map((hw) => (
              <article key={`mobile-${hw.id}`} className="teacher-mobile-card">
                <div className="teacher-mobile-card-head">
                  <div>
                    <h3 className="teacher-mobile-card-title">{hw.title}</h3>
                    <div className="teacher-card-meta">{hw.dueDate}</div>
                  </div>
                  <span className="badge badge-info">{hw.type}</span>
                </div>

                <div className="teacher-mobile-card-grid">
                  <div className="teacher-mobile-card-field">
                    <span>{t('teacher.classDetail.maxScore')}</span>
                    <strong>{hw.maxScore}</strong>
                  </div>
                  <div className="teacher-mobile-card-field">
                    <span>{t('teacher.classDetail.attachment')}</span>
                    <strong>{hw.attachments?.length || (hw.attachmentUrl ? 1 : 0)}</strong>
                  </div>
                </div>

                {hw.description ? <p className="teacher-mobile-card-copy">{hw.description}</p> : null}

                <div className="teacher-action-row">
                  <button className="btn btn-primary btn-sm" onClick={() => viewSubmissions(hw)}>
                    {t('teacher.classDetail.submissions')}
                  </button>
                  <button className="btn btn-secondary btn-sm" onClick={() => openEditForm(hw)}>
                    Edit
                  </button>
                  <button className="btn btn-secondary btn-sm" onClick={() => handleDelete(hw.id)}>
                    Delete
                  </button>
                </div>
              </article>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function SubmissionRow({ submission, studentName, maxScore, onGrade, mobile = false }) {
  const { t } = useTranslation();
  const [score, setScore] = useState(submission.score ?? '');
  const [feedback, setFeedback] = useState(submission.feedback || '');
  const isGraded = submission.score != null;

  if (mobile) {
    return (
      <article className="teacher-mobile-card">
        <div className="teacher-mobile-card-head">
          <div>
            <h3 className="teacher-mobile-card-title">{studentName || `${t('teacher.classDetail.studentId')} ${submission.studentId}`}</h3>
            <div className="teacher-card-meta">
              {t('teacher.classDetail.studentId')} {submission.studentId} • {submission.submittedAt ? new Date(submission.submittedAt).toLocaleString() : '-'}
            </div>
          </div>
          <span className={`badge ${isGraded ? 'badge-success' : 'badge-warning'}`}>
            {submission.status || (isGraded ? t('teacher.classDetail.graded') : t('teacher.classDetail.submitted'))}
          </span>
        </div>

        <div className="teacher-mobile-card-field">
          <span>{t('teacher.classDetail.content')}</span>
          {submission.submissionText ? <div className="teacher-mobile-card-copy">{submission.submissionText}</div> : null}
          {submission.attachmentUrl ? (
            <a href="#" onClick={(e) => { e.preventDefault(); downloadFileAuthenticated(submission.attachmentUrl); }} style={{ color: 'var(--primary)', cursor: 'pointer' }}>
              Download
            </a>
          ) : null}
          {!submission.submissionText && !submission.attachmentUrl ? <strong>-</strong> : null}
        </div>

        <div className="teacher-mobile-card-grid">
          <label className="teacher-mobile-card-field">
            <span>{t('teacher.classDetail.score')}</span>
            <input
              type="number"
              min={0}
              max={maxScore}
              value={score}
              onChange={(e) => setScore(e.target.value)}
              className="form-control"
            />
          </label>
          <label className="teacher-mobile-card-field">
            <span>{t('teacher.classDetail.feedback')}</span>
            <input
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              className="form-control"
              placeholder={t('teacher.classDetail.feedbackPlaceholder')}
            />
          </label>
        </div>

        <div className="teacher-action-row">
          <button
            className="btn btn-success btn-sm"
            onClick={() => onGrade(submission.id, score, feedback)}
            disabled={score === ''}
          >
            {isGraded ? t('common.update') : t('teacher.classDetail.grade')}
          </button>
        </div>
      </article>
    );
  }

  return (
    <tr>
      <td>{submission.studentId}</td>
      <td>{studentName || '-'}</td>
      <td>{submission.submittedAt ? new Date(submission.submittedAt).toLocaleString() : '-'}</td>
      <td>
        {submission.submissionText && (
          <div style={{ fontSize: '0.8rem', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={submission.submissionText}>
            {submission.submissionText}
          </div>
        )}
        {submission.attachmentUrl && (
          <a href="#" onClick={(e) => { e.preventDefault(); downloadFileAuthenticated(submission.attachmentUrl); }} style={{ fontSize: '0.8rem', color: 'var(--primary)', cursor: 'pointer' }}>📎 Download</a>
        )}
        {!submission.submissionText && !submission.attachmentUrl && '-'}
      </td>
      <td>
        <span className={`badge ${isGraded ? 'badge-success' : 'badge-warning'}`}>
          {submission.status || (isGraded ? t('teacher.classDetail.graded') : t('teacher.classDetail.submitted'))}
        </span>
      </td>
      <td>
        <input
          type="number"
          min={0}
          max={maxScore}
          value={score}
          onChange={(e) => setScore(e.target.value)}
          className="form-control"
          style={{ width: '70px', display: 'inline-block' }}
        />
        <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}> / {maxScore}</span>
      </td>
      <td>
        <input
          value={feedback}
          onChange={(e) => setFeedback(e.target.value)}
          className="form-control"
          placeholder={t('teacher.classDetail.feedbackPlaceholder')}
          style={{ minWidth: '120px' }}
        />
      </td>
      <td>
        <button
          className="btn btn-success btn-sm"
          onClick={() => onGrade(submission.id, score, feedback)}
          disabled={score === ''}
        >
          {isGraded ? t('common.update') : t('teacher.classDetail.grade')}
        </button>
      </td>
    </tr>
  );
}

/* ─── Attendance Tab ─────────────────────────────────────────── */
function AttendanceTab({ subjects, classId, students }) {
  const { t } = useTranslation();
  const [selectedSubject, setSelectedSubject] = useState(subjects[0]?.taId || '');
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [period, setPeriod] = useState(1);
  const [attendanceMap, setAttendanceMap] = useState({});
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(false);
  const [existingRecords, setExistingRecords] = useState([]);
  const [loadingExisting, setLoadingExisting] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [calendarMonth, setCalendarMonth] = useState(new Date());
  // Track dates that have attendance data
  const [attendanceDates, setAttendanceDates] = useState(new Set());
  const [statsVersion, setStatsVersion] = useState(0);

  const fmtDate = (d) => {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  };

  // Load existing attendance whenever date changes
  useEffect(() => {
    const dateStr = fmtDate(selectedDate);
    setLoadingExisting(true);
    setMessage('');
    getClassAttendanceByDate(classId, dateStr)
      .then((res) => {
        const records = res.data || [];
        setExistingRecords(records);
        if (records.length > 0) {
          // Populate map from saved data
          const map = {};
          students.forEach((s) => { map[s.id] = 'PRESENT'; });
          records.forEach((r) => {
            map[r.studentId] = r.status;
          });
          setAttendanceMap(map);
          setIsEditing(false);
        } else {
          // Fresh — default all to PRESENT
          const map = {};
          students.forEach((s) => { map[s.id] = 'PRESENT'; });
          setAttendanceMap(map);
          setIsEditing(true);
        }
      })
      .catch(() => {
        const map = {};
        students.forEach((s) => { map[s.id] = 'PRESENT'; });
        setAttendanceMap(map);
        setExistingRecords([]);
        setIsEditing(true);
      })
      .finally(() => setLoadingExisting(false));
  }, [selectedDate, classId, students]);

  // Load month stats
  useEffect(() => {
    const y = calendarMonth.getFullYear();
    const m = calendarMonth.getMonth();
    const start = fmtDate(new Date(y, m, 1));
    const end = fmtDate(new Date(y, m + 1, 0));
    setStatsLoading(true);
    getClassAttendanceStats(classId, start, end)
      .then((res) => {
        setStats(res.data);
      })
      .catch(() => setStats(null))
      .finally(() => setStatsLoading(false));
    getClassAttendanceDates(classId, start, end)
      .then((res) => setAttendanceDates(new Set(res.data || [])))
      .catch(() => setAttendanceDates(new Set()));
  }, [calendarMonth, classId, students, statsVersion]);

  const handleSubmit = () => {
    setSaving(true);
    setMessage('');
    const attendances = Object.entries(attendanceMap).map(([studentId, status]) => ({
      studentId: parseInt(studentId),
      status,
      remarks: '',
    }));
    markBulkAttendance({
      teachingAssignmentId: parseInt(selectedSubject),
      attendanceDate: fmtDate(selectedDate),
      periodNumber: period,
      attendances,
    })
      .then(() => {
        setMessage('Attendance saved successfully!');
        setIsEditing(false);
        // Refresh stats and dates
        setStatsVersion((v) => v + 1);
        getClassAttendanceByDate(classId, fmtDate(selectedDate))
          .then((res) => setExistingRecords(res.data || []))
          .catch(() => {});
      })
      .catch((err) => setMessage('Error: ' + (err.response?.data?.message || err.message)))
      .finally(() => setSaving(false));
  };

  // Calendar helpers
  const monthStart = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1);
  const monthEnd = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + 1, 0);
  const startDay = monthStart.getDay(); // 0=Sun
  const daysInMonth = monthEnd.getDate();
  const today = new Date();
  const todayStr = fmtDate(today);
  const selectedStr = fmtDate(selectedDate);

  const MONTH_NAMES = [t('days.january'), t('days.february'), t('days.march'), t('days.april'), t('days.may'), t('days.june'), t('days.july'), t('days.august'), t('days.september'), t('days.october'), t('days.november'), t('days.december')];
  const DAY_HEADERS = [t('days.sun'), t('days.mon'), t('days.tue'), t('days.wed'), t('days.thu'), t('days.fri'), t('days.sat')];

  const prevMonth = () => setCalendarMonth(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() - 1, 1));
  const nextMonth = () => setCalendarMonth(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + 1, 1));
  const goToday = () => { setCalendarMonth(new Date()); setSelectedDate(new Date()); };

  // Count for the selected date
  const presentCount = existingRecords.filter((r) => r.status === 'PRESENT' || r.status === 'present').length;
  const absentCount = existingRecords.filter((r) => r.status === 'ABSENT' || r.status === 'absent').length;
  const lateCount = existingRecords.filter((r) => r.status === 'LATE' || r.status === 'late').length;

  return (
    <div className="grid-calendar">
      {/* Left: Calendar */}
      <div>
        <div className="card" style={{ overflow: 'hidden' }}>
          <div className="teacher-calendar-header" style={{
            background: 'var(--sidebar-bg)', padding: '1rem 1.25rem',
          }}>
            <button onClick={prevMonth} style={{
              background: 'none', border: 'none', color: 'var(--sidebar-active)',
              cursor: 'pointer', fontSize: '1.1rem', padding: '0.25rem 0.5rem',
              borderRadius: '6px', transition: 'background 0.15s',
            }} onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(93,229,181,0.1)'}
              onMouseLeave={(e) => e.currentTarget.style.background = 'none'}>◀</button>
            <div style={{ textAlign: 'center' }}>
              <div style={{
                fontFamily: 'var(--font-heading)', fontWeight: 700, color: 'white',
                fontSize: '1rem', letterSpacing: '-0.02em',
              }}>
                {MONTH_NAMES[calendarMonth.getMonth()]}
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--sidebar-text)', letterSpacing: '0.04em' }}>
                {calendarMonth.getFullYear()}
              </div>
            </div>
            <button onClick={nextMonth} style={{
              background: 'none', border: 'none', color: 'var(--sidebar-active)',
              cursor: 'pointer', fontSize: '1.1rem', padding: '0.25rem 0.5rem',
              borderRadius: '6px', transition: 'background 0.15s',
            }} onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(93,229,181,0.1)'}
              onMouseLeave={(e) => e.currentTarget.style.background = 'none'}>▶</button>
          </div>
          <div className="teacher-calendar-weekdays">
            {DAY_HEADERS.map((d) => (
              <div key={d} style={{
                fontSize: '0.65rem', fontWeight: 700, color: 'var(--text-muted)',
                textTransform: 'uppercase', letterSpacing: '0.05em', padding: '0.3rem 0',
                fontFamily: 'var(--font-heading)',
              }}>{d}</div>
            ))}
          </div>
          <div className="teacher-calendar-days">
            {Array.from({ length: startDay }, (_, i) => <div key={`e${i}`} />)}
            {Array.from({ length: daysInMonth }, (_, i) => {
              const day = i + 1;
              const dateObj = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), day);
              const dateStr = fmtDate(dateObj);
              const isToday = dateStr === todayStr;
              const isSelected = dateStr === selectedStr;
              const hasData = attendanceDates.has(dateStr);
              const isWeekend = dateObj.getDay() === 0 || dateObj.getDay() === 6;

              return (
                <button
                  key={day}
                  onClick={() => setSelectedDate(dateObj)}
                  style={{
                    width: '100%', aspectRatio: '1', border: 'none',
                    borderRadius: '8px', cursor: 'pointer',
                    fontSize: '0.82rem', fontWeight: isSelected || isToday ? 700 : 500,
                    fontFamily: 'var(--font-heading)',
                    background: isSelected
                      ? 'var(--primary)'
                      : isToday
                        ? 'var(--primary-light)'
                        : 'transparent',
                    color: isSelected
                      ? 'white'
                      : isWeekend
                        ? 'var(--text-muted)'
                        : 'var(--text)',
                    position: 'relative',
                    transition: 'all 0.15s',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}
                  onMouseEnter={(e) => { if (!isSelected) e.currentTarget.style.background = 'rgba(26,107,92,0.08)'; }}
                  onMouseLeave={(e) => { if (!isSelected) e.currentTarget.style.background = isToday ? 'var(--primary-light)' : 'transparent'; }}
                >
                  {day}
                  {hasData && (
                    <span style={{
                      position: 'absolute', bottom: '3px', left: '50%',
                      transform: 'translateX(-50%)',
                      width: 5, height: 5, borderRadius: '50%',
                      background: isSelected ? 'white' : 'var(--success)',
                    }} />
                  )}
                </button>
              );
            })}
          </div>
          <div style={{ padding: '0 0.75rem 0.75rem', textAlign: 'center' }}>
            <button className="btn btn-secondary btn-sm" onClick={goToday} style={{ width: '100%', fontSize: '0.78rem' }}>
              {t('teacher.classDetail.today')}
            </button>
          </div>
        </div>

        {stats && (
          <div className="card" style={{ marginTop: '0.75rem' }}>
            <div className="card-body" style={{ padding: '1rem' }}>
              <h4 style={{
                fontSize: '0.82rem', fontFamily: 'var(--font-heading)', fontWeight: 700,
                marginBottom: '0.6rem', color: 'var(--text-muted)', textTransform: 'uppercase',
                letterSpacing: '0.04em',
              }}>
                {t('teacher.classDetail.monthOverview')}
              </h4>
              <div className="teacher-calendar-stats">
                <div style={{
                  textAlign: 'center', padding: '0.6rem 0.25rem',
                  borderRadius: '8px', background: 'var(--primary-light)',
                }}>
                  <div style={{ fontSize: '1.2rem', fontWeight: 800, fontFamily: 'var(--font-heading)', color: 'var(--primary)' }}>
                    {stats.overallStatistics?.averageAttendanceRate?.toFixed(0) ?? '—'}%
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)', fontWeight: 600 }}>{t('teacher.classDetail.attendanceStat')}</div>
                </div>
                <div style={{
                  textAlign: 'center', padding: '0.6rem 0.25rem',
                  borderRadius: '8px', background: '#FEE2E2',
                }}>
                  <div style={{ fontSize: '1.2rem', fontWeight: 800, fontFamily: 'var(--font-heading)', color: 'var(--danger)' }}>
                    {stats.overallStatistics?.totalAbsences ?? 0}
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)', fontWeight: 600 }}>{t('teacher.classDetail.absences')}</div>
                </div>
                <div style={{
                  textAlign: 'center', padding: '0.6rem 0.25rem',
                  borderRadius: '8px', background: '#FEF3C7',
                }}>
                  <div style={{ fontSize: '1.2rem', fontWeight: 800, fontFamily: 'var(--font-heading)', color: 'var(--warning)' }}>
                    {stats.overallStatistics?.totalLateArrivals ?? 0}
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)', fontWeight: 600 }}>{t('teacher.classDetail.late')}</div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Right: Attendance form */}
      <div>
        <div className="teacher-date-header" style={{ marginBottom: '0.75rem' }}>
          <div>
            <h3 style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, fontSize: '1.1rem', letterSpacing: '-0.02em' }}>
              {selectedDate.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}
            </h3>
            {existingRecords.length > 0 && !isEditing && (
              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.15rem' }}>
                {t('teacher.classDetail.savedSummary', { present: presentCount, absent: absentCount, late: lateCount })}
              </div>
            )}
          </div>
          {existingRecords.length > 0 && !isEditing && (
            <button className="btn btn-secondary btn-sm" onClick={() => setIsEditing(true)}>
              {t('teacher.classDetail.editAttendance')}
            </button>
          )}
        </div>

        {message && (
          <div className={`alert ${message.startsWith('Error') ? 'alert-error' : 'alert-success'}`}>{message}</div>
        )}

        {loadingExisting ? (
          <div className="loading"><div className="spinner" />{t('teacher.classDetail.loadingAttendance')}</div>
        ) : (
          <>
            {isEditing && (
              <div className="teacher-inline-controls">
                <div className="form-group" style={{ margin: 0 }}>
                  <label>{t('teacher.classDetail.subject')}</label>
                  <SearchableSelect
                    options={subjects.map((s) => ({ value: String(s.taId), label: s.name }))}
                    value={selectedSubject}
                    onChange={(value) => setSelectedSubject(String(value || ''))}
                    placeholder={t('teacher.classDetail.subject')}
                    searchPlaceholder={t('common.search')}
                    emptyLabel="No subjects found"
                  />
                </div>
                <div className="form-group" style={{ margin: 0 }}>
                  <label>{t('teacher.classDetail.period')}</label>
                  <input type="number" className="form-control" value={period}
                    onChange={(e) => setPeriod(parseInt(e.target.value) || 1)} min={1} max={10}
                    style={{ width: '80px' }} />
                </div>
                <button className="btn btn-sm" style={{ background: '#D1FAE5', color: '#065F46' }}
                  onClick={() => {
                    const map = {};
                    students.forEach((s) => { map[s.id] = 'PRESENT'; });
                    setAttendanceMap(map);
                  }}>{t('teacher.classDetail.allPresent')}</button>
              </div>
            )}

            <div className="card">
              {students.length === 0 ? (
                <div className="card-body" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>{t('teacher.classDetail.noStudentsEnrolled')}</div>
              ) : (
                <>
                  <div className="table-container desktop-table">
                    <table>
                      <thead>
                        <tr>
                          <th style={{ width: '40px' }}>#</th>
                          <th>{t('teacher.classDetail.student')}</th>
                          <th style={{ width: '160px' }}>{t('common.status')}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {students.map((s, i) => {
                          const status = attendanceMap[s.id] || 'PRESENT';
                          return (
                            <tr key={s.id}>
                              <td style={{ color: 'var(--text-muted)' }}>{i + 1}</td>
                              <td style={{ fontWeight: 500 }}>{s.name}</td>
                              <td>
                                {isEditing ? (
                                  <div style={{ width: '140px' }}>
                                    <SelectMenu
                                      options={[
                                        { value: 'PRESENT', label: t('teacher.classDetail.present') },
                                        { value: 'ABSENT', label: t('teacher.classDetail.absent') },
                                        { value: 'LATE', label: t('teacher.classDetail.lateStatus') },
                                        { value: 'EXCUSED', label: t('teacher.classDetail.excused') },
                                        { value: 'SICK', label: t('teacher.classDetail.sick') },
                                      ]}
                                      value={status}
                                      onChange={(value) => setAttendanceMap({ ...attendanceMap, [s.id]: value })}
                                      placeholder={t('common.status')}
                                    />
                                  </div>
                                ) : (
                                  <StatusBadge status={status} />
                                )}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                  <div className="card-body teacher-mobile-card-list">
                    {students.map((s, i) => {
                      const status = attendanceMap[s.id] || 'PRESENT';
                      return (
                        <article key={`mobile-${s.id}`} className="teacher-mobile-card">
                          <div className="teacher-mobile-card-head">
                            <div>
                              <h3 className="teacher-mobile-card-title">{s.name}</h3>
                              <div className="teacher-card-meta">#{i + 1}</div>
                            </div>
                            {!isEditing ? <StatusBadge status={status} /> : null}
                          </div>

                          {isEditing ? (
                            <label className="teacher-mobile-card-field">
                              <span>{t('common.status')}</span>
                              <SelectMenu
                                options={[
                                  { value: 'PRESENT', label: t('teacher.classDetail.present') },
                                  { value: 'ABSENT', label: t('teacher.classDetail.absent') },
                                  { value: 'LATE', label: t('teacher.classDetail.lateStatus') },
                                  { value: 'EXCUSED', label: t('teacher.classDetail.excused') },
                                  { value: 'SICK', label: t('teacher.classDetail.sick') },
                                ]}
                                value={status}
                                onChange={(value) => setAttendanceMap({ ...attendanceMap, [s.id]: value })}
                                placeholder={t('common.status')}
                              />
                            </label>
                          ) : null}
                        </article>
                      );
                    })}
                  </div>
                </>
              )}
            </div>

            {isEditing && students.length > 0 && (
              <div className="teacher-split-actions" style={{ marginTop: '0.75rem' }}>
                <button className="btn btn-primary" onClick={handleSubmit} disabled={saving}>
                  {saving ? t('common.saving') : t('teacher.classDetail.saveAttendance')}
                </button>
                {existingRecords.length > 0 && (
                  <button className="btn btn-secondary" onClick={() => {
                    // Reset to saved data
                    const map = {};
                    students.forEach((s) => { map[s.id] = 'PRESENT'; });
                    existingRecords.forEach((r) => { map[r.studentId] = r.status; });
                    setAttendanceMap(map);
                    setIsEditing(false);
                  }}>Cancel</button>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

/* ─── Grades Tab ──────────────────────────────────────────────── */
function GradesTab({ subjects, classId, students }) {
  const { t } = useTranslation();
  const [selectedSubject, setSelectedSubject] = useState(subjects[0]?.taId || '');
  const [quarter, setQuarter] = useState(1);
  const [gradeType, setGradeType] = useState('QUARTER');
  const [grades, setGrades] = useState({}); // studentId -> gradeValue
  const [saving, setSaving] = useState(false);
  const [notification, setNotification] = useState({ message: '', tone: 'success' });

  // Load existing grades for each student
  const [existingGrades, setExistingGrades] = useState([]);
  const [gradeLoading, setGradeLoading] = useState(false);

  const loadGrades = () => {
    if (students.length === 0) return;
    setGradeLoading(true);
    Promise.all(
      students.map((s) =>
        getStudentGrades(s.id, quarter)
          .then((res) => ({ studentId: s.id, grades: res.data || [] }))
          .catch(() => ({ studentId: s.id, grades: [] }))
      )
    )
      .then((results) => {
        setExistingGrades(results);
        const map = {};
        results.forEach(({ studentId, grades: gList }) => {
          const sub = subjects.find((su) => String(su.taId) === String(selectedSubject));
          const match = gList.find(
            (g) => g.subjectName === sub?.name && g.gradeType === gradeType
          );
          if (match) map[studentId] = match.gradeValue;
        });
        setGrades(map);
      })
      .finally(() => setGradeLoading(false));
  };

  useEffect(() => { loadGrades(); }, [selectedSubject, quarter, gradeType]);

  const handleSaveGrade = (studentId) => {
    const value = grades[studentId];
    if (value == null || value === '') return;
    const studentName = students.find((s) => s.id === studentId)?.name || studentId;
    setSaving(true);
    setNotification({ message: '', tone: 'success' });
    assignGrade({
      studentId: parseInt(studentId),
      teachingAssignmentId: parseInt(selectedSubject),
      quarter,
      gradeValue: parseInt(value),
      gradeType,
    })
      .then(() => setNotification({
        message: t('teacher.classDetail.gradeSaved', {
          student: studentName,
          defaultValue: `Saved grade for ${studentName}`,
        }),
        tone: 'success',
      }))
      .catch((err) => setNotification({
        message: err.response?.data?.message || err.message,
        tone: 'error',
      }))
      .finally(() => setSaving(false));
  };

  return (
    <div className="teacher-class-detail-page">
      <div className="teacher-inline-controls">
        <div className="form-group" style={{ margin: 0 }}>
          <label>{t('teacher.classDetail.subject')}</label>
          <SearchableSelect
            options={subjects.map((s) => ({ value: String(s.taId), label: s.name }))}
            value={selectedSubject}
            onChange={(value) => setSelectedSubject(String(value || ''))}
            placeholder={t('teacher.classDetail.subject')}
            searchPlaceholder={t('common.search')}
            emptyLabel="No subjects found"
          />
        </div>
        <div className="form-group" style={{ margin: 0 }}>
          <label>{t('teacher.classDetail.quarter')}</label>
          <SelectMenu
            options={[
              { value: '1', label: t('teacher.classDetail.q1') },
              { value: '2', label: t('teacher.classDetail.q2') },
              { value: '3', label: t('teacher.classDetail.q3') },
              { value: '4', label: t('teacher.classDetail.q4') },
            ]}
            value={String(quarter)}
            onChange={(value) => setQuarter(parseInt(value, 10))}
            placeholder={t('teacher.classDetail.quarter')}
          />
        </div>
        <div className="form-group" style={{ margin: 0 }}>
          <label>{t('teacher.classDetail.gradeType')}</label>
          <SelectMenu
            options={[
              { value: 'QUARTER', label: t('teacher.classDetail.quarterType') },
              { value: 'MIDTERM', label: t('teacher.classDetail.midterm') },
              { value: 'FINAL', label: t('teacher.classDetail.final') },
              { value: 'YEARLY', label: t('teacher.classDetail.yearly') },
            ]}
            value={gradeType}
            onChange={setGradeType}
            placeholder={t('teacher.classDetail.gradeType')}
          />
        </div>
      </div>

      <div className="card">
        <div className="card-body">
          <h4 style={{ marginBottom: '1rem' }}>
            {t('teacher.classDetail.assignGrades')} — {gradeType} (Q{quarter})
          </h4>
          {gradeLoading ? (
            <div className="loading"><div className="spinner" />{t('teacher.classDetail.loadingGrades')}</div>
          ) : students.length === 0 ? (
            <p style={{ color: 'var(--text-muted)' }}>{t('teacher.classDetail.noStudentsEnrolled')}</p>
          ) : (
            <>
              <div className="table-container desktop-table">
                <table>
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>{t('teacher.classDetail.student')}</th>
                      <th>{t('teacher.classDetail.gradeRange')}</th>
                      <th>{t('common.actions')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {students.map((s, i) => (
                      <tr key={s.id}>
                        <td>{i + 1}</td>
                        <td>{s.name}</td>
                        <td>
                          <input
                            type="number"
                            min={0}
                            max={100}
                            className="form-control"
                            style={{ width: '90px' }}
                            value={grades[s.id] ?? ''}
                            onChange={(e) => setGrades({ ...grades, [s.id]: e.target.value })}
                          />
                        </td>
                        <td>
                          <button
                            className="btn btn-success btn-sm"
                            onClick={() => handleSaveGrade(s.id)}
                            disabled={saving || grades[s.id] == null || grades[s.id] === ''}
                          >
                            {t('common.save')}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="teacher-mobile-card-list">
                {students.map((s, i) => (
                  <article key={`mobile-${s.id}`} className="teacher-mobile-card">
                    <div className="teacher-mobile-card-head">
                      <div>
                        <h3 className="teacher-mobile-card-title">{s.name}</h3>
                        <div className="teacher-card-meta">#{i + 1}</div>
                      </div>
                    </div>

                    <label className="teacher-mobile-card-field">
                      <span>{t('teacher.classDetail.gradeRange')}</span>
                      <input
                        type="number"
                        min={0}
                        max={100}
                        className="form-control"
                        value={grades[s.id] ?? ''}
                        onChange={(e) => setGrades({ ...grades, [s.id]: e.target.value })}
                      />
                    </label>

                    <div className="teacher-action-row">
                      <button
                        className="btn btn-success btn-sm"
                        onClick={() => handleSaveGrade(s.id)}
                        disabled={saving || grades[s.id] == null || grades[s.id] === ''}
                      >
                        {t('common.save')}
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            </>
          )}
        </div>
      </div>

      <FloatingNotification
        message={notification.message}
        tone={notification.tone}
        onClose={() => setNotification({ message: '', tone: 'success' })}
      />
    </div>
  );
}
