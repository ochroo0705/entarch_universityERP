package com.edusys.backend.bootstrap;

import com.edusys.backend.ai.repository.StudentRiskSnapshotRepository;
import com.edusys.backend.ai.service.RiskSnapshotService;
import com.edusys.backend.model.*;
import com.edusys.backend.model.Class;
import com.edusys.backend.repository.*;
import com.edusys.backend.service.TranslationService;
import com.edusys.backend.dto.TranslationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private TeachingAssignmentRepository teachingAssignmentRepository;
    @Autowired private StudentEnrollmentRepository studentEnrollmentRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private HomeworkRepository homeworkRepository;
    @Autowired private HomeworkSubmissionRepository homeworkSubmissionRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private ExamScheduleRepository examScheduleRepository;
    @Autowired private ExamResultRepository examResultRepository;
    @Autowired private PeriodRepository periodRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ParentStudentRepository parentStudentRepository;
    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private TranslationService translationService;
    @Autowired private StudentRiskSnapshotRepository studentRiskSnapshotRepository;
    @Autowired private RiskSnapshotService riskSnapshotService;

    @Value("${app.seed.bulk-users:false}")
    private boolean seedBulkUsers;

    @Value("${app.seed.bulk-users-count:1000}")
    private int bulkUserCount;

    @Value("${app.seed.generate-risk-snapshots:true}")
    private boolean generateRiskSnapshots;

    @Value("${app.bootstrap.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.mode:none}")
    private String bootstrapMode;

    @Value("${app.bootstrap.admin.username:admin}")
    private String bootstrapAdminUsername;

    @Value("${app.bootstrap.admin.email:admin@example.com}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String bootstrapAdminPassword;

    @Value("${app.bootstrap.admin.first-name:System}")
    private String bootstrapAdminFirstName;

    @Value("${app.bootstrap.admin.last-name:Admin}")
    private String bootstrapAdminLastName;

    @Value("${app.bootstrap.admin.phone:}")
    private String bootstrapAdminPhone;

    private User createUser(String username, String email, String rawPassword, String firstName, String lastName,
                            int roleFlags, String phone, Gender gender, LocalDate dob, boolean active, String teacherSubjects) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRoleFlags(roleFlags);
        u.setPhone(phone);
        u.setGender(gender);
        u.setDateOfBirth(dob);
        u.setIsActive(active);
        u.setTeacherSubjects(teacherSubjects);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private Subject createSubject(String name, String nameMn, String code, Integer gradeLevel, Integer hoursPerWeek, boolean mandatory) {
        Subject s = new Subject();
        s.setName(name);
        s.setSubjectNameMn(nameMn);
        s.setSubjectCode(code);
        s.setGradeLevel(gradeLevel);
        s.setHoursPerWeek(hoursPerWeek);
        s.setIsMandatory(mandatory);
        s.setCreatedAt(LocalDateTime.now());
        return subjectRepository.save(s);
    }

    private Class createClass(String name, int grade, String section, User homeroom, String room) {
        Class c = new Class();
        c.setClassName(name);
        c.setGrade(grade);
        c.setSection(section);
        c.setHomeroomTeacher(homeroom);
        c.setRoomNumber(room);
        c.setAcademicYear("2025-2026");
        c.setStudentCount(0);
        c.setIsActive(true);
        c.setCreatedAt(LocalDateTime.now());
        return classRepository.save(c);
    }

    private StudentEnrollment enroll(User student, Class cls, String studentNumber) {
        StudentEnrollment se = new StudentEnrollment();
        se.setStudent(student);
        se.setClassEntity(cls);
        se.setEnrollmentDate(LocalDate.of(2025, 9, 1));
        se.setStudentNumber(studentNumber);
        se.setStatus(StudentEnrollment.Status.active);
        return studentEnrollmentRepository.save(se);
    }

    private TeachingAssignment assign(User teacher, Subject subject, Class cls) {
        TeachingAssignment ta = new TeachingAssignment();
        ta.setTeacher(teacher);
        ta.setSubject(subject);
        ta.setClassEntity(cls);
        ta.setAcademicYear("2025-2026");
        ta.setSemester(1);
        ta.setIsActive(true);
        return teachingAssignmentRepository.save(ta);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!bootstrapEnabled) {
            return;
        }

        if ("admin-only".equalsIgnoreCase(bootstrapMode)) {
            createBootstrapAdminIfNeeded();
            return;
        }

        if (!"full-demo".equalsIgnoreCase(bootstrapMode)) {
            return;
        }

        boolean shouldSeedCoreData = userRepository.count() == 0;
        boolean shouldGenerateInitialRiskSnapshots = generateRiskSnapshots && studentRiskSnapshotRepository.count() == 0;
        if (!shouldSeedCoreData && !seedBulkUsers) {
            if (shouldGenerateInitialRiskSnapshots) {
                riskSnapshotService.runScheduledRecalculation();
            }
            return;
        }
        if (!shouldSeedCoreData) {
            createBulkUsersIfNeeded(bulkUserCount);
            if (shouldGenerateInitialRiskSnapshots) {
                riskSnapshotService.runScheduledRecalculation();
            }
            return;
        }

        // ===================== USERS =====================

        // Admin
        User admin = createUser("admin", "admin@example.com", "admin123",
                "Систем", "Админ", 8, "99999999", Gender.M, LocalDate.of(1990, 1, 1), true, null);

        // Teachers (password: teacher123)
        User teacher01 = createUser("teacher01", "teacher01@example.com", "teacher123",
                "Болд", "Баатар", 2, "88888888", Gender.M, LocalDate.of(1985, 5, 10), true, "Математик, Физик");
        User teacher02 = createUser("teacher02", "teacher02@example.com", "teacher123",
                "Баяр", "Ганбаатар", 2, "88001002", Gender.M, LocalDate.of(1988, 3, 12), true, "Хими, Биологи");
        User teacher03 = createUser("teacher03", "teacher03@example.com", "teacher123",
                "Оюунаа", "Батболд", 2, "88001003", Gender.F, LocalDate.of(1990, 7, 22), true, "Англи хэл, Уран зохиол");
        User teacher04 = createUser("teacher04", "teacher04@example.com", "teacher123",
                "Мөнх", "Эрдэнэ", 2, "88001004", Gender.M, LocalDate.of(1987, 11, 5), true, "Түүх, Газарзүй");
        User teacher05 = createUser("teacher05", "teacher05@example.com", "teacher123",
                "Туяа", "Тэмүүлэн", 2, "88001005", Gender.F, LocalDate.of(1992, 1, 18), true, "Дүрслэх урлаг, Хөгжим");
        User teacher06 = createUser("teacher06", "teacher06@example.com", "teacher123",
                "Баатар", "Сүхбаатар", 2, "88001006", Gender.M, LocalDate.of(1985, 9, 30), true, "Биеийн тамир");
        User teacher07 = createUser("teacher07", "teacher07@example.com", "teacher123",
                "Сарнай", "Дашням", 2, "88001007", Gender.F, LocalDate.of(1991, 4, 14), true, "Мэдээлэл зүй");
        User teacher08 = createUser("teacher08", "teacher08@example.com", "teacher123",
                "Энхбат", "Намсрай", 2, "88001008", Gender.M, LocalDate.of(1983, 12, 1), true, "Математик, Статистик");

        // Students (password: student123)
        User s01 = createUser("student01", "student01@example.com", "student123", "Сараа", "Дорж", 1, "77777777", Gender.F, LocalDate.of(2008, 3, 15), true, null);
        User s02 = createUser("student02", "student02@example.com", "student123", "Бат", "Эрдэнэ", 1, "77001002", Gender.M, LocalDate.of(2008, 1, 20), true, null);
        User s03 = createUser("student03", "student03@example.com", "student123", "Тэмүүлэн", "Бат", 1, "77001003", Gender.M, LocalDate.of(2008, 2, 14), true, null);
        User s04 = createUser("student04", "student04@example.com", "student123", "Ариунаа", "Дорж", 1, "77001004", Gender.F, LocalDate.of(2008, 5, 22), true, null);
        User s05 = createUser("student05", "student05@example.com", "student123", "Ганболд", "Цэрэндорж", 1, "77001005", Gender.M, LocalDate.of(2007, 11, 3), true, null);
        User s06 = createUser("student06", "student06@example.com", "student123", "Номин", "Эрдэнэ", 1, "77001006", Gender.F, LocalDate.of(2008, 8, 17), true, null);
        User s07 = createUser("student07", "student07@example.com", "student123", "Билгүүн", "Мөнх", 1, "77001007", Gender.M, LocalDate.of(2007, 6, 25), true, null);
        User s08 = createUser("student08", "student08@example.com", "student123", "Солонго", "Баатар", 1, "77001008", Gender.F, LocalDate.of(2008, 1, 9), true, null);
        User s09 = createUser("student09", "student09@example.com", "student123", "Анар", "Сүхбаатар", 1, "77001009", Gender.M, LocalDate.of(2008, 12, 30), true, null);
        User s10 = createUser("student10", "student10@example.com", "student123", "Мөнхжин", "Отгон", 1, "77001010", Gender.M, LocalDate.of(2007, 9, 11), true, null);
        User s11 = createUser("student11", "student11@example.com", "student123", "Ану", "Ганбат", 1, "77001011", Gender.F, LocalDate.of(2008, 4, 5), true, null);
        User s12 = createUser("student12", "student12@example.com", "student123", "Дулгуун", "Батболд", 1, "77001012", Gender.M, LocalDate.of(2007, 7, 19), true, null);
        User s13 = createUser("student13", "student13@example.com", "student123", "Сувд", "Наран", 1, "77001013", Gender.F, LocalDate.of(2009, 1, 28), true, null);
        User s14 = createUser("student14", "student14@example.com", "student123", "Энхжин", "Болд", 1, "77001014", Gender.M, LocalDate.of(2009, 3, 16), true, null);
        User s15 = createUser("student15", "student15@example.com", "student123", "Үүрээ", "Дашдорж", 1, "77001015", Gender.F, LocalDate.of(2008, 10, 7), true, null);
        User s16 = createUser("student16", "student16@example.com", "student123", "Тулга", "Эрхэс", 1, "77001016", Gender.M, LocalDate.of(2009, 6, 14), true, null);
        User s17 = createUser("student17", "student17@example.com", "student123", "Нандин", "Оюун", 1, "77001017", Gender.F, LocalDate.of(2009, 8, 21), true, null);
        User s18 = createUser("student18", "student18@example.com", "student123", "Оргил", "Батмөнх", 1, "77001018", Gender.M, LocalDate.of(2007, 12, 25), true, null);
        User s19 = createUser("student19", "student19@example.com", "student123", "Дэлгэрмаа", "Энхтүр", 1, "77001019", Gender.F, LocalDate.of(2008, 11, 11), true, null);
        User s20 = createUser("student20", "student20@example.com", "student123", "Зул", "Гантулга", 1, "77001020", Gender.M, LocalDate.of(2009, 2, 8), true, null);
        User s21 = createUser("student21", "student21@example.com", "student123", "Саранцэцэг", "Дашзэвэг", 1, "77001021", Gender.F, LocalDate.of(2008, 9, 3), true, null);
        User s22 = createUser("student22", "student22@example.com", "student123", "Бату", "Хүрц", 1, "77001022", Gender.M, LocalDate.of(2007, 4, 17), true, null);
        User s23 = createUser("student23", "student23@example.com", "student123", "Анужин", "Цэрэн", 1, "77001023", Gender.F, LocalDate.of(2009, 5, 29), true, null);
        User s24 = createUser("student24", "student24@example.com", "student123", "Эрдэнэбат", "Сүхээ", 1, "77001024", Gender.M, LocalDate.of(2008, 7, 6), true, null);
        User s25 = createUser("student25", "student25@example.com", "student123", "Оюунгэрэл", "Мөнхбат", 1, "77001025", Gender.F, LocalDate.of(2007, 10, 13), true, null);
        User s26 = createUser("student26", "student26@example.com", "student123", "Чинзо", "Галбадрах", 1, "77001026", Gender.M, LocalDate.of(2009, 4, 22), true, null);
        User s27 = createUser("student27", "student27@example.com", "student123", "Мишээл", "Баярсайхан", 1, "77001027", Gender.F, LocalDate.of(2008, 6, 18), true, null);
        User s28 = createUser("student28", "student28@example.com", "student123", "Хулан", "Алтангэрэл", 1, "77001028", Gender.F, LocalDate.of(2007, 8, 31), true, null);
        User s29 = createUser("student29", "student29@example.com", "student123", "Отгонбаяр", "Бямбаа", 1, "77001029", Gender.M, LocalDate.of(2009, 7, 15), true, null);
        User s30 = createUser("student30", "student30@example.com", "student123", "Саруул", "Энхболд", 1, "77001030", Gender.F, LocalDate.of(2008, 3, 24), true, null);
        User s31 = createUser("student31", "student31@example.com", "student123", "Турүү", "Гансүх", 1, "77001031", Gender.M, LocalDate.of(2007, 5, 8), true, null);
        User s32 = createUser("student32", "student32@example.com", "student123", "Наранцэцэг", "Доржсүрэн", 1, "77001032", Gender.F, LocalDate.of(2009, 11, 20), true, null);

        // Parents (password: parent123)
        User p01 = createUser("parent01", "parent01@example.com", "parent123", "Дорж", "Бат", 4, "91234567", Gender.M, LocalDate.of(1980, 6, 20), true, null);
        User p02 = createUser("parent02", "parent02@example.com", "parent123", "Алтаа", "Мөнх", 4, "91234568", Gender.F, LocalDate.of(1982, 8, 15), true, null);
        User p03 = createUser("parent03", "parent03@example.com", "parent123", "Ганзориг", "Цэрэндорж", 4, "91001003", Gender.M, LocalDate.of(1978, 4, 10), true, null);
        User p04 = createUser("parent04", "parent04@example.com", "parent123", "Цэцэгмаа", "Бат", 4, "91001004", Gender.F, LocalDate.of(1981, 9, 25), true, null);
        User p05 = createUser("parent05", "parent05@example.com", "parent123", "Батсайхан", "Эрдэнэ", 4, "91001005", Gender.M, LocalDate.of(1975, 12, 8), true, null);
        User p06 = createUser("parent06", "parent06@example.com", "parent123", "Одвал", "Мөнх", 4, "91001006", Gender.F, LocalDate.of(1983, 2, 14), true, null);
        User p07 = createUser("parent07", "parent07@example.com", "parent123", "Сүхбат", "Намсрай", 4, "91001007", Gender.M, LocalDate.of(1979, 6, 30), true, null);
        User p08 = createUser("parent08", "parent08@example.com", "parent123", "Тунгалаг", "Ганбат", 4, "91001008", Gender.F, LocalDate.of(1984, 11, 17), true, null);
        User p09 = createUser("parent09", "parent09@example.com", "parent123", "Даваадорж", "Батболд", 4, "91001009", Gender.M, LocalDate.of(1976, 8, 3), true, null);
        User p10 = createUser("parent10", "parent10@example.com", "parent123", "Эрдэнэчимэг", "Наран", 4, "91001010", Gender.F, LocalDate.of(1982, 5, 21), true, null);
        User p11 = createUser("parent11", "parent11@example.com", "parent123", "Болдбаатар", "Сүхээ", 4, "91001011", Gender.M, LocalDate.of(1977, 3, 15), true, null);
        User p12 = createUser("parent12", "parent12@example.com", "parent123", "Ариунзаяа", "Галбадрах", 4, "91001012", Gender.F, LocalDate.of(1980, 10, 28), true, null);

        // ===================== PERIODS =====================
        Period[] periods = new Period[8];
        int[][] periodTimes = {
            {8,0,8,40}, {8,50,9,30}, {9,40,10,20}, {10,30,11,10},
            {11,10,11,50}, {11,50,12,30}, {12,40,13,20}, {13,30,14,10}
        };
        Period.PeriodType[] periodTypes = {
            Period.PeriodType.LESSON, Period.PeriodType.LESSON, Period.PeriodType.LESSON, Period.PeriodType.LESSON,
            Period.PeriodType.LUNCH, Period.PeriodType.LESSON, Period.PeriodType.LESSON, Period.PeriodType.LESSON
        };
        for (int i = 0; i < 8; i++) {
            Period p = new Period();
            p.setPeriodNumber(i + 1);
            p.setStartTime(LocalTime.of(periodTimes[i][0], periodTimes[i][1]));
            p.setEndTime(LocalTime.of(periodTimes[i][2], periodTimes[i][3]));
            p.setPeriodType(periodTypes[i]);
            periods[i] = periodRepository.save(p);
        }

        // ===================== SUBJECTS (Mongolian primary) =====================
        Subject math       = createSubject("Математик", "Математик", "MATH101", 10, 4, true);
        Subject physics    = createSubject("Физик", "Физик", "PHY101", 10, 3, true);
        Subject chemistry  = createSubject("Хими", "Хими", "CHEM101", 10, 3, true);
        Subject biology    = createSubject("Биологи", "Биологи", "BIO101", 10, 3, true);
        Subject english    = createSubject("Англи хэл", "Англи хэл", "ENG101", 10, 4, true);
        Subject history    = createSubject("Түүх", "Түүх", "HIS101", 10, 2, true);
        Subject geography  = createSubject("Газарзүй", "Газарзүй", "GEO101", 10, 2, true);
        Subject art        = createSubject("Дүрслэх урлаг", "Дүрслэх урлаг", "ART101", 10, 2, false);
        Subject cs         = createSubject("Мэдээлэл зүй", "Мэдээлэл зүй", "CS101", 10, 3, true);
        Subject pe         = createSubject("Биеийн тамир", "Биеийн тамир", "PE101", 10, 2, true);
        Subject literature = createSubject("Уран зохиол", "Уран зохиол", "LIT101", 11, 3, true);
        Subject stats      = createSubject("Статистик", "Статистик", "STAT101", 11, 2, false);
        Subject chem2      = createSubject("Хими", "Хими", "CHEM201", 11, 3, true);
        Subject bio2       = createSubject("Биологи", "Биологи", "BIO201", 11, 3, true);
        Subject eng2       = createSubject("Англи хэл", "Англи хэл", "ENG201", 11, 4, true);
        Subject his2       = createSubject("Түүх", "Түүх", "HIS201", 11, 2, true);

        // ===================== CLASSES =====================
        Class c10A = createClass("10-А анги", 10, "А", teacher01, "101");
        Class c10B = createClass("10-Б анги", 10, "Б", teacher01, "102");
        Class c10C = createClass("10-В анги", 10, "В", teacher02, "103");
        Class c10D = createClass("10-Г анги", 10, "Г", teacher03, "104");
        Class c11A = createClass("11-А анги", 11, "А", teacher04, "201");
        Class c11B = createClass("11-Б анги", 11, "Б", teacher05, "202");
        Class c11C = createClass("11-В анги", 11, "В", teacher06, "203");
        Class c12A = createClass("12-А анги", 12, "А", teacher07, "301");
        Class c12B = createClass("12-Б анги", 12, "Б", teacher08, "302");
        Class c9A  = createClass("9-А анги",   9, "А", teacher01, "001");

        // ===================== STUDENT ENROLLMENTS =====================
        // 10-A: s01-s08
        enroll(s01, c10A, "S001"); enroll(s02, c10A, "S002"); enroll(s03, c10A, "S003");
        enroll(s04, c10A, "S004"); enroll(s05, c10A, "S005"); enroll(s06, c10A, "S006");
        enroll(s07, c10A, "S007"); enroll(s08, c10A, "S008");
        // 10-B: s09-s14
        enroll(s09, c10B, "S009"); enroll(s10, c10B, "S010"); enroll(s11, c10B, "S011");
        enroll(s12, c10B, "S012"); enroll(s13, c10B, "S013"); enroll(s14, c10B, "S014");
        // 10-C: s15-s18
        enroll(s15, c10C, "S015"); enroll(s16, c10C, "S016");
        enroll(s17, c10C, "S017"); enroll(s18, c10C, "S018");
        // 11-A: s19-s22
        enroll(s19, c11A, "S019"); enroll(s20, c11A, "S020");
        enroll(s21, c11A, "S021"); enroll(s22, c11A, "S022");
        // 11-B: s23-s26
        enroll(s23, c11B, "S023"); enroll(s24, c11B, "S024");
        enroll(s25, c11B, "S025"); enroll(s26, c11B, "S026");
        // 12-A: s27-s30
        enroll(s27, c12A, "S027"); enroll(s28, c12A, "S028");
        enroll(s29, c12A, "S029"); enroll(s30, c12A, "S030");
        // 12-B: s31-s32
        enroll(s31, c12B, "S031"); enroll(s32, c12B, "S032");

        // Update student counts
        for (Class cls : List.of(c10A, c10B, c10C, c10D, c11A, c11B, c11C, c12A, c12B, c9A)) {
            cls.setStudentCount((int) studentEnrollmentRepository.findAll().stream()
                    .filter(e -> e.getClassEntity().getId().equals(cls.getId())).count());
            classRepository.save(cls);
        }

        // ===================== TEACHING ASSIGNMENTS =====================
        // teacher01: Math & Physics in 10-A
        TeachingAssignment ta_math_10A = assign(teacher01, math, c10A);
        TeachingAssignment ta_phys_10A = assign(teacher01, physics, c10A);
        assign(teacher01, math, c10C);
        assign(teacher01, physics, c10C);

        // teacher02: Chemistry & Biology
        TeachingAssignment ta_chem_10A = assign(teacher02, chemistry, c10A);
        assign(teacher02, chemistry, c10B);
        assign(teacher02, biology, c10C);

        // teacher03: English & Literature
        TeachingAssignment ta_t3_eng_10A = assign(teacher03, english, c10A);
        assign(teacher03, english, c10B);
        assign(teacher03, eng2, c11A);
        assign(teacher03, literature, c11B);

        // teacher04: History & Geography
        TeachingAssignment ta_his_10A = assign(teacher04, history, c10A);
        assign(teacher04, geography, c10B);
        assign(teacher04, his2, c11A);

        // teacher05: Art
        assign(teacher05, art, c10C);
        assign(teacher05, art, c11B);

        // teacher06: Physical Education
        TeachingAssignment ta_pe_10A = assign(teacher06, pe, c10A);
        assign(teacher06, pe, c10B);
        assign(teacher06, pe, c11A);

        // teacher07: Computer Science
        TeachingAssignment ta_cs_10A = assign(teacher07, cs, c10A);
        assign(teacher07, cs, c12A);

        // teacher08: Statistics
        assign(teacher08, stats, c11A);
        assign(teacher08, stats, c11B);

        // ===================== SCHEDULES =====================
        // Math 10A: Mon p1, Wed p3, Fri p2
        addSchedule(ta_math_10A, 1, 1, "101");
        addSchedule(ta_math_10A, 3, 3, "101");
        addSchedule(ta_math_10A, 5, 2, "101");
        // Physics 10A: Tue p2, Thu p4
        addSchedule(ta_phys_10A, 2, 2, "101");
        addSchedule(ta_phys_10A, 4, 4, "101");
        // English 10A: Mon p3, Wed p1
        addSchedule(ta_t3_eng_10A, 1, 3, "101");
        addSchedule(ta_t3_eng_10A, 3, 1, "101");
        // Chemistry 10A: Tue p3, Fri p4
        addSchedule(ta_chem_10A, 2, 3, "101");
        addSchedule(ta_chem_10A, 5, 4, "101");
        // History 10A: Thu p2
        addSchedule(ta_his_10A, 4, 2, "101");
        // PE 10A: Wed p6
        addSchedule(ta_pe_10A, 3, 6, "101");
        // CS 10A: Fri p3
        addSchedule(ta_cs_10A, 5, 3, "101");

        // ===================== HOMEWORK (for 10-A subjects) =====================
        // Math homework
        Homework hw_math1 = createHomework(ta_math_10A, "Алгебрын даалгавар 1", "1-10 дугаар бодлогуудыг бодох", LocalDate.of(2026, 1, 20), 100);
        Homework hw_math2 = createHomework(ta_math_10A, "Алгебрын даалгавар 2", "11-20 дугаар бодлогуудыг бодох", LocalDate.of(2026, 2, 3), 100);
        Homework hw_math3 = createHomework(ta_math_10A, "Геометрийн даалгавар", "Гурвалжны талбай, периметр бодлогууд", LocalDate.of(2026, 2, 17), 100);
        Homework hw_math4 = createHomework(ta_math_10A, "Тэгшитгэлийн даалгавар", "Квадрат тэгшитгэл бодох", LocalDate.of(2026, 3, 14), 100);
        // Physics homework
        Homework hw_phys1 = createHomework(ta_phys_10A, "Физикийн даалгавар 1", "Ньютоны хуулиудын бодлогууд", LocalDate.of(2026, 1, 25), 50);
        Homework hw_phys2 = createHomework(ta_phys_10A, "Энергийн даалгавар", "Кинетик болон потенциал энергийн бодлогууд", LocalDate.of(2026, 2, 20), 50);
        // English homework
        Homework hw_eng1 = createHomework(ta_t3_eng_10A, "Англи хэлний эссэ", "Миний өнгөрсөн зуны амралт сэдвээр эссэ бичих", LocalDate.of(2026, 2, 5), 100);
        Homework hw_eng2 = createHomework(ta_t3_eng_10A, "Дүрмийн дасгал", "Past tense дасгалууд хийх", LocalDate.of(2026, 2, 25), 50);
        // Chemistry homework
        Homework hw_chem1 = createHomework(ta_chem_10A, "Химийн даалгавар", "Менделеевийн хүснэгтийн бодлогууд", LocalDate.of(2026, 2, 10), 100);
        // History homework
        Homework hw_his1 = createHomework(ta_his_10A, "Түүхийн реферат", "Монголын эзэнт гүрний түүх", LocalDate.of(2026, 3, 1), 100);
        // CS homework
        Homework hw_cs1 = createHomework(ta_cs_10A, "Програмчлалын даалгавар", "Python хэл дээр энгийн тооцоолуур бичих", LocalDate.of(2026, 2, 15), 100);

        // ===================== STUDENT01 HOMEWORK SUBMISSIONS =====================
        createSubmission(hw_math1, s01, "Бүх бодлогуудыг бодсон", HomeworkSubmission.Status.graded, 92, "Маш сайн!", teacher01);
        createSubmission(hw_math2, s01, "11-20 бодлогууд", HomeworkSubmission.Status.graded, 88, "Сайн ажилласан", teacher01);
        createSubmission(hw_math3, s01, "Гурвалжны бодлогууд", HomeworkSubmission.Status.graded, 95, "Гайхалтай!", teacher01);
        createSubmission(hw_math4, s01, "Квадрат тэгшитгэлүүд", HomeworkSubmission.Status.submitted, null, null, null);
        createSubmission(hw_phys1, s01, "Ньютоны хуулиуд", HomeworkSubmission.Status.graded, 45, "Сайн", teacher01);
        createSubmission(hw_phys2, s01, "Энергийн бодлогууд", HomeworkSubmission.Status.graded, 42, "Идэвхтэй", teacher01);
        createSubmission(hw_eng1, s01, "My Summer Vacation essay", HomeworkSubmission.Status.graded, 85, "Good writing!", teacher03);
        createSubmission(hw_eng2, s01, "Past tense exercises", HomeworkSubmission.Status.graded, 40, "Well done", teacher03);
        createSubmission(hw_chem1, s01, "Менделеевийн хүснэгт", HomeworkSubmission.Status.graded, 90, "Маш сайн!", teacher02);
        createSubmission(hw_his1, s01, "Монголын эзэнт гүрэн", HomeworkSubmission.Status.graded, 88, "Сонирхолтой реферат", teacher04);
        createSubmission(hw_cs1, s01, "Python тооцоолуур код", HomeworkSubmission.Status.graded, 97, "Бүрэн зөв!", teacher07);

        // ===================== STUDENT01 ATTENDANCE (Jan-Mar 2026, weekdays) =====================
        // Math attendance (Mon, Wed, Fri)
        LocalDate[] mathDates = {
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 9),
            LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 14), LocalDate.of(2026, 1, 16),
            LocalDate.of(2026, 1, 19), LocalDate.of(2026, 1, 21), LocalDate.of(2026, 1, 23),
            LocalDate.of(2026, 1, 26), LocalDate.of(2026, 1, 28), LocalDate.of(2026, 1, 30),
            LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4), LocalDate.of(2026, 2, 6),
            LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 13),
            LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 20),
            LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 4), LocalDate.of(2026, 3, 6),
            LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 11)
        };
        Attendance.Status[] mathStatuses = {
            Attendance.Status.present, Attendance.Status.present, Attendance.Status.present,
            Attendance.Status.present, Attendance.Status.late, Attendance.Status.present,
            Attendance.Status.present, Attendance.Status.present, Attendance.Status.present,
            Attendance.Status.present, Attendance.Status.present, Attendance.Status.absent,
            Attendance.Status.present, Attendance.Status.present, Attendance.Status.present,
            Attendance.Status.present, Attendance.Status.present, Attendance.Status.present,
            Attendance.Status.present, Attendance.Status.sick, Attendance.Status.present,
            Attendance.Status.present, Attendance.Status.present, Attendance.Status.present,
            Attendance.Status.present, Attendance.Status.present
        };
        for (int i = 0; i < mathDates.length; i++) {
            addAttendance(s01, ta_math_10A, mathDates[i], 1, mathStatuses[i], teacher01);
        }
        // Physics attendance (Tue, Thu)
        LocalDate[] physDates = {
            LocalDate.of(2026, 1, 6), LocalDate.of(2026, 1, 8),
            LocalDate.of(2026, 1, 13), LocalDate.of(2026, 1, 15),
            LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 22),
            LocalDate.of(2026, 1, 27), LocalDate.of(2026, 1, 29),
            LocalDate.of(2026, 2, 3), LocalDate.of(2026, 2, 5),
            LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 12),
            LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 19),
            LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 5),
            LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12)
        };
        for (int i = 0; i < physDates.length; i++) {
            addAttendance(s01, ta_phys_10A, physDates[i], 2, i == 7 ? Attendance.Status.late : Attendance.Status.present, teacher01);
        }
        // English attendance (Mon, Wed)
        LocalDate[] engDates = {
            LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7),
            LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 14),
            LocalDate.of(2026, 1, 19), LocalDate.of(2026, 1, 21),
            LocalDate.of(2026, 1, 26), LocalDate.of(2026, 1, 28),
            LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4),
            LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 11),
            LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 18),
            LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 4)
        };
        for (LocalDate d : engDates) {
            addAttendance(s01, ta_t3_eng_10A, d, 3, Attendance.Status.present, teacher03);
        }

        // ===================== EXAM SCHEDULES & RESULTS =====================
        ExamSchedule mathMidterm = createExamSchedule(ta_math_10A, "Algebra Midterm", LocalDate.of(2026, 3, 20), LocalTime.of(9, 0), LocalTime.of(10, 30), "201", "Covers units 1-4", true);
        ExamSchedule physMidterm = createExamSchedule(ta_phys_10A, "Physics Midterm", LocalDate.of(2026, 3, 22), LocalTime.of(11, 0), LocalTime.of(12, 15), "Lab-1", "Mechanics and energy", true);
        ExamSchedule engSpeaking = createExamSchedule(ta_t3_eng_10A, "English Speaking Assessment", LocalDate.of(2026, 3, 25), LocalTime.of(8, 30), LocalTime.of(9, 30), "301", "Prepared presentation and Q&A", true);

        addExamResult(mathMidterm, s01, 88, 100, 40, "Strong algebra work.", "Published with class cohort", true, teacher01);
        addExamResult(physMidterm, s01, 41, 50, 35, "Good understanding of core formulas.", "Published with class cohort", true, teacher01);
        addExamResult(engSpeaking, s01, 17, 20, 25, "Confident speaking with minor grammar slips.", "Published with class cohort", true, teacher03);

        // ===================== STUDENT01 GRADES (all subjects, Q1 & Q2) =====================
        addGrade(s01, ta_math_10A, 1, 93, Grade.GradeType.QUARTER, teacher01);
        addGrade(s01, ta_math_10A, 2, 90, Grade.GradeType.QUARTER, teacher01);
        addGrade(s01, ta_math_10A, 1, 88, Grade.GradeType.MIDTERM, teacher01);
        addGrade(s01, ta_phys_10A, 1, 87, Grade.GradeType.QUARTER, teacher01);
        addGrade(s01, ta_phys_10A, 2, 91, Grade.GradeType.QUARTER, teacher01);
        addGrade(s01, ta_phys_10A, 1, 85, Grade.GradeType.MIDTERM, teacher01);
        addGrade(s01, ta_t3_eng_10A, 1, 85, Grade.GradeType.QUARTER, teacher03);
        addGrade(s01, ta_t3_eng_10A, 2, 88, Grade.GradeType.QUARTER, teacher03);
        addGrade(s01, ta_chem_10A, 1, 90, Grade.GradeType.QUARTER, teacher02);
        addGrade(s01, ta_chem_10A, 2, 92, Grade.GradeType.QUARTER, teacher02);
        addGrade(s01, ta_his_10A, 1, 88, Grade.GradeType.QUARTER, teacher04);
        addGrade(s01, ta_his_10A, 2, 86, Grade.GradeType.QUARTER, teacher04);
        addGrade(s01, ta_pe_10A, 1, 95, Grade.GradeType.QUARTER, teacher06);
        addGrade(s01, ta_pe_10A, 2, 97, Grade.GradeType.QUARTER, teacher06);
        addGrade(s01, ta_cs_10A, 1, 97, Grade.GradeType.QUARTER, teacher07);
        addGrade(s01, ta_cs_10A, 2, 95, Grade.GradeType.QUARTER, teacher07);

        // ===================== RISK DEMO PROFILES =====================
        // student02: medium risk profile with mixed attendance, repeated lateness, some missing homework, and slipping grades
        seedMediumRiskStudent(
                s02,
                teacher01, teacher02, teacher03, teacher04, teacher07,
                ta_math_10A, ta_phys_10A, ta_t3_eng_10A, ta_chem_10A, ta_his_10A, ta_cs_10A,
                hw_math1, hw_math2, hw_math3, hw_math4,
                hw_phys1, hw_phys2, hw_eng1, hw_eng2, hw_chem1, hw_his1, hw_cs1,
                mathMidterm, physMidterm, engSpeaking
        );

        // student03: high risk profile with weak attendance, many missing assignments, and consistently low grades
        seedHighRiskStudent(
                s03,
                teacher01, teacher02, teacher03, teacher04, teacher07,
                ta_math_10A, ta_phys_10A, ta_t3_eng_10A, ta_chem_10A, ta_his_10A, ta_cs_10A,
                hw_math1, hw_math2, hw_math3, hw_math4,
                hw_phys1, hw_phys2, hw_eng1, hw_eng2, hw_chem1, hw_his1, hw_cs1,
                mathMidterm, physMidterm, engSpeaking
        );

        // ===================== PARENT-STUDENT LINKS =====================
        linkParent(p01, s01, ParentStudent.Relationship.FATHER, true);
        linkParent(p01, s02, ParentStudent.Relationship.FATHER, false);
        linkParent(p02, s01, ParentStudent.Relationship.MOTHER, false);
        linkParent(p03, s03, ParentStudent.Relationship.FATHER, true);
        linkParent(p04, s04, ParentStudent.Relationship.MOTHER, true);
        linkParent(p04, s05, ParentStudent.Relationship.MOTHER, false);
        linkParent(p05, s06, ParentStudent.Relationship.FATHER, true);
        linkParent(p06, s07, ParentStudent.Relationship.MOTHER, true);
        linkParent(p07, s08, ParentStudent.Relationship.FATHER, true);
        linkParent(p07, s09, ParentStudent.Relationship.FATHER, false);
        linkParent(p08, s10, ParentStudent.Relationship.MOTHER, true);
        linkParent(p09, s11, ParentStudent.Relationship.FATHER, true);
        linkParent(p09, s12, ParentStudent.Relationship.FATHER, false);
        linkParent(p10, s13, ParentStudent.Relationship.MOTHER, true);
        linkParent(p11, s14, ParentStudent.Relationship.FATHER, true);
        linkParent(p12, s15, ParentStudent.Relationship.MOTHER, true);
        linkParent(p12, s16, ParentStudent.Relationship.MOTHER, false);
        linkParent(p03, s17, ParentStudent.Relationship.GUARDIAN, false);
        linkParent(p05, s18, ParentStudent.Relationship.FATHER, true);

        // ===================== ANNOUNCEMENTS (Mongolian primary) =====================
        Announcement ann1 = addAnnouncement("2025-2026 хичээлийн жилд тавтай морил",
                "Бүх сурагчид, багш нар, эцэг эхчүүдийг шинэ хичээлийн жилд тавтай морилно уу. Хуваарь болон ангийн хуваарилалтаа шалгана уу.",
                15, null, Announcement.Priority.high, admin);
        Announcement ann2 = addAnnouncement("Хагас жилийн шалгалтын хуваарь",
                "Хагас жилийн шалгалтууд 12 сарын 15-19-нд болно. Дэлгэрэнгүй хуваарийг анги бүрт нийтлэнэ.",
                3, null, Announcement.Priority.urgent, admin);
        Announcement ann3 = addAnnouncement("Эцэг эхийн хурал",
                "Жил бүрийн эцэг эхийн хурал 11 сарын 25-нд болно. Бүх эцэг эхчүүд оролцоно уу.",
                6, null, Announcement.Priority.high, admin);
        Announcement ann4 = addAnnouncement("10-А ангийн аялал",
                "10-А ангийн сурагчид 11 сарын 20-нд Үндэсний музейд аялна. Зөвшөөрлийн хуудсыг 11 сарын 15-ны дотор өгнө үү.",
                5, c10A, Announcement.Priority.normal, teacher01);
        Announcement ann5 = addAnnouncement("Номын сангийн цагийн хуваарь өөрчлөгдлөө",
                "Сургуулийн номын сан ажлын өдрүүдэд 18:00 цаг хүртэл ажиллана. Нэмэлт цагийг ашиглаарай.",
                1, null, Announcement.Priority.low, admin);
        Announcement ann6 = addAnnouncement("Спортын өдрийн бүртгэл",
                "Жил бүрийн спортын өдөр 12 сарын 5-нд болно. Сурагчид ангийн багшаараа дамжуулан бүртгүүлнэ.",
                3, null, Announcement.Priority.normal, teacher06);
        Announcement ann7 = addAnnouncement("Компьютерийн лабораторийн засвар",
                "11 сарын 22-23-нд компьютерийн лаборатори засварт орно. МЗ хичээлийг 105 тоотод түр явуулна.",
                2, null, Announcement.Priority.high, teacher07);
        Announcement ann8 = addAnnouncement("11 ангийн шинжлэх ухааны үзэсгэлэн",
                "11 ангийн бүх сурагчид шинжлэх ухааны үзэсгэлэнгийн төслийн саналаа 12 сарын 1-ний дотор ирүүлнэ.",
                1, null, Announcement.Priority.normal, teacher02);

        // ===================== ENGLISH TRANSLATIONS =====================
        // Subject translations
        translationService.setTranslation(new TranslationDTO(null, "subject", math.getId(), "name", "en", "Mathematics"));
        translationService.setTranslation(new TranslationDTO(null, "subject", physics.getId(), "name", "en", "Physics"));
        translationService.setTranslation(new TranslationDTO(null, "subject", chemistry.getId(), "name", "en", "Chemistry"));
        translationService.setTranslation(new TranslationDTO(null, "subject", biology.getId(), "name", "en", "Biology"));
        translationService.setTranslation(new TranslationDTO(null, "subject", english.getId(), "name", "en", "English"));
        translationService.setTranslation(new TranslationDTO(null, "subject", history.getId(), "name", "en", "History"));
        translationService.setTranslation(new TranslationDTO(null, "subject", geography.getId(), "name", "en", "Geography"));
        translationService.setTranslation(new TranslationDTO(null, "subject", art.getId(), "name", "en", "Art"));
        translationService.setTranslation(new TranslationDTO(null, "subject", cs.getId(), "name", "en", "Computer Science"));
        translationService.setTranslation(new TranslationDTO(null, "subject", pe.getId(), "name", "en", "Physical Education"));
        translationService.setTranslation(new TranslationDTO(null, "subject", literature.getId(), "name", "en", "Literature"));
        translationService.setTranslation(new TranslationDTO(null, "subject", stats.getId(), "name", "en", "Statistics"));
        translationService.setTranslation(new TranslationDTO(null, "subject", chem2.getId(), "name", "en", "Chemistry"));
        translationService.setTranslation(new TranslationDTO(null, "subject", bio2.getId(), "name", "en", "Biology"));
        translationService.setTranslation(new TranslationDTO(null, "subject", eng2.getId(), "name", "en", "English"));
        translationService.setTranslation(new TranslationDTO(null, "subject", his2.getId(), "name", "en", "History"));

        // Homework translations
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_math1.getId(), "title", "en", "Algebra Homework 1"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_math1.getId(), "description", "en", "Solve exercises 1-10"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_math2.getId(), "title", "en", "Algebra Homework 2"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_math2.getId(), "description", "en", "Solve exercises 11-20"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_math3.getId(), "title", "en", "Geometry Homework"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_math3.getId(), "description", "en", "Triangle area and perimeter problems"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_math4.getId(), "title", "en", "Equations Homework"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_math4.getId(), "description", "en", "Solve quadratic equations"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_phys1.getId(), "title", "en", "Physics Homework 1"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_phys1.getId(), "description", "en", "Newton's laws problems"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_phys2.getId(), "title", "en", "Energy Homework"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_phys2.getId(), "description", "en", "Kinetic and potential energy problems"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_eng1.getId(), "title", "en", "English Essay"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_eng1.getId(), "description", "en", "Write an essay about my summer vacation"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_eng2.getId(), "title", "en", "Grammar Exercises"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_eng2.getId(), "description", "en", "Past tense exercises"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_chem1.getId(), "title", "en", "Chemistry Homework"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_chem1.getId(), "description", "en", "Periodic table problems"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_his1.getId(), "title", "en", "History Report"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_his1.getId(), "description", "en", "History of the Mongol Empire"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_cs1.getId(), "title", "en", "Programming Assignment"));
        translationService.setTranslation(new TranslationDTO(null, "homework", hw_cs1.getId(), "description", "en", "Write a simple calculator in Python"));

        // Announcement translations
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann1.getId(), "title", "en", "Welcome to 2025-2026 Academic Year"));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann1.getId(), "content", "en", "We are excited to welcome all students, teachers, and parents to the new academic year. Please check your schedules and class assignments."));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann2.getId(), "title", "en", "Midterm Exam Schedule"));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann2.getId(), "content", "en", "Midterm examinations will be held from December 15-19. Detailed schedule will be posted in each class."));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann3.getId(), "title", "en", "Parent-Teacher Conference"));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann3.getId(), "content", "en", "Annual parent-teacher conference is scheduled for November 25. All parents are requested to attend."));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann4.getId(), "title", "en", "Grade 10-A Field Trip"));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann4.getId(), "content", "en", "Grade 10-A students will have a field trip to the National Museum on November 20. Permission slips due by November 15."));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann5.getId(), "title", "en", "Library Hours Extended"));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann5.getId(), "content", "en", "The school library will now be open until 6 PM on weekdays. Make use of this additional study time."));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann6.getId(), "title", "en", "Sports Day Registration"));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann6.getId(), "content", "en", "Annual sports day is coming up on December 5. Students can register for events through their homeroom teachers."));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann7.getId(), "title", "en", "Computer Lab Maintenance"));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann7.getId(), "content", "en", "The computer lab will be closed for maintenance on November 22-23. CS classes will be held in Room 105 temporarily."));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann8.getId(), "title", "en", "Grade 11 Science Fair"));
        translationService.setTranslation(new TranslationDTO(null, "announcement", ann8.getId(), "content", "en", "All Grade 11 students must submit their science fair project proposals by December 1."));

        if (seedBulkUsers) {
            createBulkUsersIfNeeded(bulkUserCount);
        }

        if (generateRiskSnapshots) {
            riskSnapshotService.runScheduledRecalculation();
        }
    }

    private void createBootstrapAdminIfNeeded() {
        if (userRepository.findFirstAdminUser().isPresent()) {
            return;
        }

        if (bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank()) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD must be configured for admin-only bootstrap");
        }

        createUser(
                bootstrapAdminUsername,
                bootstrapAdminEmail,
                bootstrapAdminPassword,
                bootstrapAdminFirstName,
                bootstrapAdminLastName,
                8,
                bootstrapAdminPhone,
                Gender.M,
                LocalDate.of(1990, 1, 1),
                true,
                null
        );
    }

    private void addSchedule(TeachingAssignment ta, int dayOfWeek, int periodNum, String room) {
        Period period = periodRepository.findAll().stream()
                .filter(p -> p.getPeriodNumber() == periodNum).findFirst().orElse(null);
        Schedule s = new Schedule();
        s.setTeachingAssignment(ta);
        s.setDayOfWeek(dayOfWeek);
        s.setPeriodNumber(periodNum);
        s.setStartTime(period != null ? period.getStartTime() : LocalTime.of(8, 0));
        s.setEndTime(period != null ? period.getEndTime() : LocalTime.of(8, 45));
        s.setRoomNumber(room);
        s.setIsActive(true);
        scheduleRepository.save(s);
    }

    private void linkParent(User parent, User student, ParentStudent.Relationship rel, boolean primary) {
        ParentStudent ps = new ParentStudent();
        ps.setParent(parent);
        ps.setStudent(student);
        ps.setRelationship(rel);
        ps.setIsPrimaryContact(primary);
        parentStudentRepository.save(ps);
    }

    private Homework createHomework(TeachingAssignment ta, String title, String desc, LocalDate due, int maxScore) {
        Homework hw = new Homework();
        hw.setTeachingAssignment(ta);
        hw.setTitle(title);
        hw.setDescription(desc);
        hw.setDueDate(due);
        hw.setMaxScore(maxScore);
        hw.setType(Homework.Type.HOMEWORK);
        hw.setCreatedAt(LocalDateTime.now());
        return homeworkRepository.save(hw);
    }

    private void createSubmission(Homework hw, User student, String text, HomeworkSubmission.Status status,
                                   Integer score, String feedback, User gradedBy) {
        HomeworkSubmission sub = new HomeworkSubmission();
        sub.setHomework(hw);
        sub.setStudent(student);
        sub.setSubmissionText(text);
        sub.setSubmittedAt(LocalDateTime.now().minusDays((long)(Math.random() * 30)));
        sub.setStatus(status);
        if (score != null) {
            sub.setScore(score);
            sub.setFeedback(feedback);
            sub.setGradedBy(gradedBy);
            sub.setGradedAt(LocalDateTime.now());
        }
        homeworkSubmissionRepository.save(sub);
    }

    private void addAttendance(User student, TeachingAssignment ta, LocalDate date, int period, Attendance.Status status, User markedBy) {
        Attendance att = new Attendance();
        att.setStudent(student);
        att.setTeachingAssignment(ta);
        att.setAttendanceDate(date);
        att.setPeriodNumber(period);
        att.setStatus(status);
        att.setMarkedBy(markedBy);
        att.setCreatedAt(LocalDateTime.now());
        attendanceRepository.save(att);
    }

    private void addGrade(User student, TeachingAssignment ta, int quarter, int value, Grade.GradeType type, User recordedBy) {
        Grade g = new Grade();
        g.setStudent(student);
        g.setTeachingAssignment(ta);
        g.setQuarter(quarter);
        g.setGradeValue(value);
        g.setGradeType(type);
        g.setRecordedBy(recordedBy);
        g.setRecordedAt(LocalDateTime.now());
        gradeRepository.save(g);
    }

    private void seedMediumRiskStudent(
            User student,
            User mathTeacher,
            User chemistryTeacher,
            User englishTeacher,
            User historyTeacher,
            User csTeacher,
            TeachingAssignment mathAssignment,
            TeachingAssignment physicsAssignment,
            TeachingAssignment englishAssignment,
            TeachingAssignment chemistryAssignment,
            TeachingAssignment historyAssignment,
            TeachingAssignment csAssignment,
            Homework hwMath1,
            Homework hwMath2,
            Homework hwMath3,
            Homework hwMath4,
            Homework hwPhys1,
            Homework hwPhys2,
            Homework hwEng1,
            Homework hwEng2,
            Homework hwChem1,
            Homework hwHis1,
            Homework hwCs1,
            ExamSchedule mathMidterm,
            ExamSchedule physMidterm,
            ExamSchedule engSpeaking
    ) {
        createSubmission(hwMath1, student, "Need help with a few steps", HomeworkSubmission.Status.graded, 71, "Review the final two problems.", mathTeacher);
        createSubmission(hwMath2, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwMath3, student, "Submitted after class", HomeworkSubmission.Status.late, 63, "Try to submit earlier.", mathTeacher);
        createSubmission(hwMath4, student, "Worked on most equations", HomeworkSubmission.Status.submitted, null, null, null);
        createSubmission(hwPhys1, student, "Partial answers only", HomeworkSubmission.Status.graded, 29, "Concepts need reinforcement.", mathTeacher);
        createSubmission(hwPhys2, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwEng1, student, "Essay draft", HomeworkSubmission.Status.graded, 68, "Ideas are good but grammar needs work.", englishTeacher);
        createSubmission(hwEng2, student, "Late grammar practice", HomeworkSubmission.Status.late, 32, "Watch tense consistency.", englishTeacher);
        createSubmission(hwChem1, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwHis1, student, "Short report", HomeworkSubmission.Status.graded, 74, "Add more detail next time.", historyTeacher);
        createSubmission(hwCs1, student, "Calculator mostly works", HomeworkSubmission.Status.graded, 70, "Fix edge cases and naming.", csTeacher);

        seedAttendanceSeries(student, mathAssignment, 1, mathTeacher, new Attendance.Status[] {
                Attendance.Status.present, Attendance.Status.late, Attendance.Status.present,
                Attendance.Status.present, Attendance.Status.absent, Attendance.Status.present,
                Attendance.Status.late, Attendance.Status.present, Attendance.Status.present,
                Attendance.Status.absent, Attendance.Status.present, Attendance.Status.late,
                Attendance.Status.present, Attendance.Status.present, Attendance.Status.absent,
                Attendance.Status.present, Attendance.Status.present, Attendance.Status.late
        }, new LocalDate[] {
                LocalDate.of(2026, 1, 30), LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4),
                LocalDate.of(2026, 2, 6), LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 11),
                LocalDate.of(2026, 2, 13), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 18),
                LocalDate.of(2026, 2, 20), LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 4),
                LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 11),
                LocalDate.of(2026, 3, 13), LocalDate.of(2026, 3, 14), LocalDate.of(2026, 3, 14)
        });
        seedAttendanceSeries(student, physicsAssignment, 2, mathTeacher, new Attendance.Status[] {
                Attendance.Status.present, Attendance.Status.late, Attendance.Status.present,
                Attendance.Status.absent, Attendance.Status.present, Attendance.Status.late,
                Attendance.Status.present, Attendance.Status.present, Attendance.Status.absent,
                Attendance.Status.present, Attendance.Status.present, Attendance.Status.late
        }, new LocalDate[] {
                LocalDate.of(2026, 1, 29), LocalDate.of(2026, 2, 3), LocalDate.of(2026, 2, 5),
                LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 12), LocalDate.of(2026, 2, 17),
                LocalDate.of(2026, 2, 19), LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 5),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12), LocalDate.of(2026, 3, 14)
        });
        seedAttendanceSeries(student, englishAssignment, 3, englishTeacher, new Attendance.Status[] {
                Attendance.Status.present, Attendance.Status.present, Attendance.Status.late,
                Attendance.Status.present, Attendance.Status.absent, Attendance.Status.present,
                Attendance.Status.late, Attendance.Status.present, Attendance.Status.present, Attendance.Status.absent
        }, new LocalDate[] {
                LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4), LocalDate.of(2026, 2, 9),
                LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 18),
                LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 4), LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 11)
        });

        addGrade(student, mathAssignment, 1, 72, Grade.GradeType.QUARTER, mathTeacher);
        addGrade(student, mathAssignment, 2, 68, Grade.GradeType.QUARTER, mathTeacher);
        addGrade(student, physicsAssignment, 1, 66, Grade.GradeType.QUARTER, mathTeacher);
        addGrade(student, physicsAssignment, 2, 64, Grade.GradeType.QUARTER, mathTeacher);
        addGrade(student, englishAssignment, 1, 73, Grade.GradeType.QUARTER, englishTeacher);
        addGrade(student, englishAssignment, 2, 69, Grade.GradeType.QUARTER, englishTeacher);
        addGrade(student, chemistryAssignment, 1, 67, Grade.GradeType.QUARTER, chemistryTeacher);
        addGrade(student, historyAssignment, 1, 75, Grade.GradeType.QUARTER, historyTeacher);
        addGrade(student, csAssignment, 1, 71, Grade.GradeType.QUARTER, csTeacher);

        addExamResult(mathMidterm, student, 61, 100, 40, "Performance is slipping.", "Flag for follow-up", true, mathTeacher);
        addExamResult(physMidterm, student, 24, 50, 35, "Needs support with core concepts.", "Flag for follow-up", true, mathTeacher);
        addExamResult(engSpeaking, student, 12, 20, 25, "Attendance affected preparation.", "Flag for follow-up", true, englishTeacher);
    }

    private void seedHighRiskStudent(
            User student,
            User mathTeacher,
            User chemistryTeacher,
            User englishTeacher,
            User historyTeacher,
            User csTeacher,
            TeachingAssignment mathAssignment,
            TeachingAssignment physicsAssignment,
            TeachingAssignment englishAssignment,
            TeachingAssignment chemistryAssignment,
            TeachingAssignment historyAssignment,
            TeachingAssignment csAssignment,
            Homework hwMath1,
            Homework hwMath2,
            Homework hwMath3,
            Homework hwMath4,
            Homework hwPhys1,
            Homework hwPhys2,
            Homework hwEng1,
            Homework hwEng2,
            Homework hwChem1,
            Homework hwHis1,
            Homework hwCs1,
            ExamSchedule mathMidterm,
            ExamSchedule physMidterm,
            ExamSchedule engSpeaking
    ) {
        createSubmission(hwMath1, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwMath2, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwMath3, student, "Very incomplete work", HomeworkSubmission.Status.late, 41, "Most steps missing.", mathTeacher);
        createSubmission(hwMath4, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwPhys1, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwPhys2, student, "Copied formulas only", HomeworkSubmission.Status.graded, 18, "Needs intervention.", mathTeacher);
        createSubmission(hwEng1, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwEng2, student, "Two sentences only", HomeworkSubmission.Status.late, 15, "Very incomplete.", englishTeacher);
        createSubmission(hwChem1, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwHis1, student, null, HomeworkSubmission.Status.missing, null, null, null);
        createSubmission(hwCs1, student, "Program does not run", HomeworkSubmission.Status.graded, 22, "Needs basic support.", csTeacher);

        seedAttendanceSeries(student, mathAssignment, 1, mathTeacher, new Attendance.Status[] {
                Attendance.Status.absent, Attendance.Status.absent, Attendance.Status.late,
                Attendance.Status.absent, Attendance.Status.present, Attendance.Status.absent,
                Attendance.Status.late, Attendance.Status.absent, Attendance.Status.absent,
                Attendance.Status.present, Attendance.Status.absent, Attendance.Status.late,
                Attendance.Status.absent, Attendance.Status.absent, Attendance.Status.present,
                Attendance.Status.absent, Attendance.Status.absent, Attendance.Status.late
        }, new LocalDate[] {
                LocalDate.of(2026, 1, 30), LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4),
                LocalDate.of(2026, 2, 6), LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 11),
                LocalDate.of(2026, 2, 13), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 18),
                LocalDate.of(2026, 2, 20), LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 4),
                LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 11),
                LocalDate.of(2026, 3, 13), LocalDate.of(2026, 3, 14), LocalDate.of(2026, 3, 14)
        });
        seedAttendanceSeries(student, physicsAssignment, 2, mathTeacher, new Attendance.Status[] {
                Attendance.Status.absent, Attendance.Status.late, Attendance.Status.absent,
                Attendance.Status.absent, Attendance.Status.present, Attendance.Status.absent,
                Attendance.Status.late, Attendance.Status.absent, Attendance.Status.absent,
                Attendance.Status.present, Attendance.Status.absent, Attendance.Status.absent
        }, new LocalDate[] {
                LocalDate.of(2026, 1, 29), LocalDate.of(2026, 2, 3), LocalDate.of(2026, 2, 5),
                LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 12), LocalDate.of(2026, 2, 17),
                LocalDate.of(2026, 2, 19), LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 5),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12), LocalDate.of(2026, 3, 14)
        });
        seedAttendanceSeries(student, englishAssignment, 3, englishTeacher, new Attendance.Status[] {
                Attendance.Status.absent, Attendance.Status.absent, Attendance.Status.late,
                Attendance.Status.absent, Attendance.Status.present, Attendance.Status.absent,
                Attendance.Status.late, Attendance.Status.absent, Attendance.Status.absent, Attendance.Status.present
        }, new LocalDate[] {
                LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4), LocalDate.of(2026, 2, 9),
                LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 18),
                LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 4), LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 11)
        });

        addGrade(student, mathAssignment, 1, 54, Grade.GradeType.QUARTER, mathTeacher);
        addGrade(student, mathAssignment, 2, 49, Grade.GradeType.QUARTER, mathTeacher);
        addGrade(student, physicsAssignment, 1, 52, Grade.GradeType.QUARTER, mathTeacher);
        addGrade(student, physicsAssignment, 2, 46, Grade.GradeType.QUARTER, mathTeacher);
        addGrade(student, englishAssignment, 1, 58, Grade.GradeType.QUARTER, englishTeacher);
        addGrade(student, englishAssignment, 2, 51, Grade.GradeType.QUARTER, englishTeacher);
        addGrade(student, chemistryAssignment, 1, 55, Grade.GradeType.QUARTER, chemistryTeacher);
        addGrade(student, historyAssignment, 1, 59, Grade.GradeType.QUARTER, historyTeacher);
        addGrade(student, csAssignment, 1, 48, Grade.GradeType.QUARTER, csTeacher);

        addExamResult(mathMidterm, student, 39, 100, 40, "Very weak mastery.", "Immediate support needed", true, mathTeacher);
        addExamResult(physMidterm, student, 16, 50, 35, "Large gaps in understanding.", "Immediate support needed", true, mathTeacher);
        addExamResult(engSpeaking, student, 7, 20, 25, "Student missed preparation milestones.", "Immediate support needed", true, englishTeacher);
    }

    private void seedAttendanceSeries(
            User student,
            TeachingAssignment assignment,
            int period,
            User markedBy,
            Attendance.Status[] statuses,
            LocalDate[] dates
    ) {
        for (int i = 0; i < Math.min(statuses.length, dates.length); i++) {
            addAttendance(student, assignment, dates[i], period, statuses[i], markedBy);
        }
    }

    private ExamSchedule createExamSchedule(
            TeachingAssignment ta,
            String title,
            LocalDate examDate,
            LocalTime startTime,
            LocalTime endTime,
            String roomNumber,
            String notes,
            boolean published
    ) {
        ExamSchedule examSchedule = new ExamSchedule();
        examSchedule.setTeachingAssignment(ta);
        examSchedule.setTitle(title);
        examSchedule.setExamDate(examDate);
        examSchedule.setStartTime(startTime);
        examSchedule.setEndTime(endTime);
        examSchedule.setRoomNumber(roomNumber);
        examSchedule.setNotes(notes);
        examSchedule.setPublished(published);
        examSchedule.setIsActive(true);
        examSchedule.setCreatedAt(LocalDateTime.now());
        examSchedule.setUpdatedAt(LocalDateTime.now());
        return examScheduleRepository.save(examSchedule);
    }

    private void addExamResult(
            ExamSchedule examSchedule,
            User student,
            double score,
            double totalScore,
            double weighting,
            String teacherComment,
            String remarks,
            boolean published,
            User recordedBy
    ) {
        ExamResult examResult = new ExamResult();
        examResult.setExamSchedule(examSchedule);
        examResult.setStudent(student);
        examResult.setScore(java.math.BigDecimal.valueOf(score).setScale(2, java.math.RoundingMode.HALF_UP));
        examResult.setTotalScore(java.math.BigDecimal.valueOf(totalScore).setScale(2, java.math.RoundingMode.HALF_UP));
        examResult.setPercentage(
                java.math.BigDecimal.valueOf(score)
                        .multiply(java.math.BigDecimal.valueOf(100))
                        .divide(java.math.BigDecimal.valueOf(totalScore), 2, java.math.RoundingMode.HALF_UP)
        );
        examResult.setWeighting(java.math.BigDecimal.valueOf(weighting).setScale(2, java.math.RoundingMode.HALF_UP));
        examResult.setTeacherComment(teacherComment);
        examResult.setRemarks(remarks);
        examResult.setPublished(published);
        examResult.setRecordedBy(recordedBy);
        examResult.setCreatedAt(LocalDateTime.now());
        examResult.setUpdatedAt(LocalDateTime.now());
        examResultRepository.save(examResult);
    }

    private Announcement addAnnouncement(String title, String content, int targetRoleFlags, Class targetClass,
                                  Announcement.Priority priority, User createdBy) {
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setTargetRoleFlags(targetRoleFlags);
        a.setTargetClass(targetClass);
        a.setPriority(priority);
        a.setCreatedBy(createdBy);
        a.setCreatedAt(LocalDateTime.now());
        a.setExpiresAt(LocalDateTime.now().plusDays(30));
        return announcementRepository.save(a);
    }

    private void createBulkUsersIfNeeded(int targetCount) {
        if (targetCount <= 0) {
            return;
        }

        long existingBulkUsers = userRepository.countByUsernameStartingWith("bulkuser");
        if (existingBulkUsers >= targetCount) {
            return;
        }

        createBulkUsers(targetCount - (int) existingBulkUsers);
    }

    private void createBulkUsers(int count) {
        final int batchSize = 500;
        final String encodedPassword = passwordEncoder.encode("test123");
        final LocalDateTime now = LocalDateTime.now();
        final List<Class> classes = classRepository.findAll(Sort.by("id"));

        List<User> userBatch = new ArrayList<>(batchSize);
        long existingBulkUsers = userRepository.countByUsernameStartingWith("bulkuser");

        for (int i = 0; i < count; i++) {
            long index = existingBulkUsers + i + 1;

            User user = new User();
            user.setUsername("bulkuser" + index);
            user.setEmail("bulkuser" + index + "@example.com");
            user.setPasswordHash(encodedPassword);
            user.setFirstName("Bulk");
            user.setLastName("User" + index);
            user.setRoleFlags(1);
            user.setPhone("900" + String.format("%05d", index % 100000));
            user.setGender(i % 2 == 0 ? Gender.M : Gender.F);
            user.setDateOfBirth(LocalDate.of(2008, 1, 1).plusDays(i % 365));
            user.setIsActive(i % 10 != 0);
            user.setTeacherSubjects(null);
            user.setCreatedAt(now.minusMinutes(i % 720));
            user.setUpdatedAt(now.minusMinutes(i % 720));
            userBatch.add(user);

            if (userBatch.size() == batchSize) {
                persistBulkUserBatch(userBatch, classes, existingBulkUsers + i + 1 - userBatch.size());
                userBatch.clear();
            }
        }

        if (!userBatch.isEmpty()) {
            persistBulkUserBatch(userBatch, classes, existingBulkUsers + count - userBatch.size());
        }
    }

    private void persistBulkUserBatch(List<User> userBatch, List<Class> classes, long batchStartOffset) {
        List<User> savedUsers = userRepository.saveAll(userBatch);
        if (classes.isEmpty()) {
            return;
        }

        List<StudentEnrollment> enrollments = new ArrayList<>(savedUsers.size());
        for (int i = 0; i < savedUsers.size(); i++) {
            User savedUser = savedUsers.get(i);
            long sequence = batchStartOffset + i + 1;
            Class classEntity = classes.get(i % classes.size());

            StudentEnrollment enrollment = new StudentEnrollment();
            enrollment.setStudent(savedUser);
            enrollment.setClassEntity(classEntity);
            enrollment.setEnrollmentDate(LocalDate.of(2025, 9, 1).plusDays((int) (sequence % 30)));
            enrollment.setStudentNumber(String.format("BULK%06d", sequence));
            enrollment.setStatus(StudentEnrollment.Status.active);
            enrollments.add(enrollment);
        }

        studentEnrollmentRepository.saveAll(enrollments);
    }
}
