package com.edusys.backend.service;

import com.edusys.backend.dto.*;
import com.edusys.backend.model.Class;
import com.edusys.backend.model.Period;
import com.edusys.backend.model.Schedule;
import com.edusys.backend.model.TeachingAssignment;
import com.edusys.backend.repository.PeriodRepository;
import com.edusys.backend.repository.ScheduleRepository;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import com.edusys.backend.repository.TeachingAssignmentRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final PeriodRepository periodRepository;

    public ScheduleService(ScheduleRepository scheduleRepository,
                          TeachingAssignmentRepository teachingAssignmentRepository,
                          StudentEnrollmentRepository studentEnrollmentRepository,
                          PeriodRepository periodRepository) {
        this.scheduleRepository = scheduleRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.periodRepository = periodRepository;
    }

    @Transactional
    public ScheduleResponseDTO createSchedule(ScheduleCreateDTO dto) {
        // Validate teaching assignment exists
        TeachingAssignment teachingAssignment = teachingAssignmentRepository.findById(dto.teachingAssignmentId())
                .orElseThrow(() -> new RuntimeException("Teaching assignment not found"));

        // Check for conflicts
        checkForConflicts(teachingAssignment, dto.dayOfWeek(), dto.periodNumber(), dto.roomNumber(), null);

        // Create new schedule
        Schedule schedule = new Schedule();
        schedule.setTeachingAssignment(teachingAssignment);
        schedule.setDayOfWeek(dto.dayOfWeek());
        schedule.setPeriodNumber(dto.periodNumber());
        schedule.setStartTime(LocalTime.parse(dto.startTime()));
        schedule.setEndTime(LocalTime.parse(dto.endTime()));
        schedule.setRoomNumber(dto.roomNumber());
        schedule.setIsActive(true);

        Schedule saved = scheduleRepository.save(schedule);
        return mapToResponseDTO(saved);
    }

    public List<ScheduleResponseDTO> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .sorted(Comparator.comparing(ScheduleResponseDTO::dayOfWeek)
                        .thenComparing(ScheduleResponseDTO::periodNumber))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDTO<ScheduleResponseDTO> listSchedules(ScheduleListQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        Page<Schedule> schedulePage = scheduleRepository.findAll(
                buildListSpecification(query),
                PageRequest.of(page - 1, pageSize, buildListSort(query.getSortBy(), query.getSortOrder()))
        );

        List<ScheduleResponseDTO> items = schedulePage.getContent().stream()
                .map(this::mapToResponseDTO)
                .toList();

        return new PaginatedResponseDTO<>(
                items,
                schedulePage.getNumber() + 1,
                schedulePage.getSize(),
                schedulePage.getTotalElements(),
                schedulePage.getTotalPages()
        );
    }

    public ScheduleResponseDTO getScheduleById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        return mapToResponseDTO(schedule);
    }

    @Transactional
    public ScheduleResponseDTO updateSchedule(Long id, ScheduleCreateDTO dto) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Validate teaching assignment exists
        TeachingAssignment teachingAssignment = teachingAssignmentRepository.findById(dto.teachingAssignmentId())
                .orElseThrow(() -> new RuntimeException("Teaching assignment not found"));

        // Check for conflicts (excluding current schedule)
        checkForConflicts(teachingAssignment, dto.dayOfWeek(), dto.periodNumber(), dto.roomNumber(), id);

        schedule.setTeachingAssignment(teachingAssignment);
        schedule.setDayOfWeek(dto.dayOfWeek());
        schedule.setPeriodNumber(dto.periodNumber());
        schedule.setStartTime(LocalTime.parse(dto.startTime()));
        schedule.setEndTime(LocalTime.parse(dto.endTime()));
        schedule.setRoomNumber(dto.roomNumber());

        Schedule updated = scheduleRepository.save(schedule);
        return mapToResponseDTO(updated);
    }

    private void checkForConflicts(TeachingAssignment ta, Integer dayOfWeek, Integer periodNumber, String roomNumber, Long excludeId) {
        java.util.function.Predicate<Schedule> notSelf = s -> excludeId == null || !s.getId().equals(excludeId);

        // 1. Teacher conflict: same teacher already has a class at this day+period
        List<Schedule> teacherConflicts = scheduleRepository.findTeacherConflicts(
                ta.getTeacher().getId(), dayOfWeek, periodNumber
        ).stream().filter(notSelf).toList();
        if (!teacherConflicts.isEmpty()) {
            String existing = teacherConflicts.get(0).getTeachingAssignment().getClassEntity().getClassName();
            throw new RuntimeException("Teacher conflict: " + ta.getTeacher().getFirstName() + " " +
                    ta.getTeacher().getLastName() + " already teaches class " + existing +
                    " on day " + dayOfWeek + " period " + periodNumber);
        }

        // 2. Class conflict: same class already has a subject at this day+period
        List<Schedule> classConflicts = scheduleRepository.findClassConflicts(
                ta.getClassEntity().getId(), dayOfWeek, periodNumber
        ).stream().filter(notSelf).toList();
        if (!classConflicts.isEmpty()) {
            String existing = classConflicts.get(0).getTeachingAssignment().getSubject().getName();
            throw new RuntimeException("Class conflict: " + ta.getClassEntity().getClassName() +
                    " already has " + existing + " on day " + dayOfWeek + " period " + periodNumber);
        }

        // 3. Room conflict: same room already booked at this day+period
        if (roomNumber != null && !roomNumber.isBlank()) {
            List<Schedule> roomConflicts = scheduleRepository.findRoomConflicts(
                    roomNumber, dayOfWeek, periodNumber
            ).stream().filter(notSelf).toList();
            if (!roomConflicts.isEmpty()) {
                String existing = roomConflicts.get(0).getTeachingAssignment().getSubject().getName();
                throw new RuntimeException("Room conflict: Room " + roomNumber +
                        " is already booked for " + existing + " on day " + dayOfWeek + " period " + periodNumber);
            }
        }
    }

    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("Schedule not found");
        }
        scheduleRepository.deleteById(id);
    }

    public List<StudentScheduleDTO> getStudentSchedule(Long studentId) {
        return scheduleRepository.findByStudentId(studentId).stream()
                .map(s -> new StudentScheduleDTO(
                        s.getDayOfWeek(),
                        s.getPeriodNumber(),
                        s.getStartTime().toString(),
                        s.getEndTime().toString(),
                        s.getRoomNumber(),
                        s.getTeachingAssignment().getSubject().getName(),
                        s.getTeachingAssignment().getTeacher().getFirstName() + " " + 
                                s.getTeachingAssignment().getTeacher().getLastName(),
                        s.getTeachingAssignment().getClassEntity().getClassName()
                ))
                .sorted(Comparator.comparing(StudentScheduleDTO::dayOfWeek)
                        .thenComparing(StudentScheduleDTO::periodNumber))
                .toList();
    }

    public List<TeacherScheduleDTO> getTeacherSchedule(Long teacherId) {
        return scheduleRepository.findByTeacherId(teacherId).stream()
                .map(s -> new TeacherScheduleDTO(
                        s.getId(),
                        s.getDayOfWeek(),
                        s.getPeriodNumber(),
                        s.getStartTime().toString(),
                        s.getEndTime().toString(),
                        s.getRoomNumber(),
                        s.getTeachingAssignment().getSubject().getName(),
                        s.getTeachingAssignment().getClassEntity().getClassName(),
                        s.getTeachingAssignment().getClassEntity().getGrade()
                ))
                .sorted(Comparator.comparing(TeacherScheduleDTO::dayOfWeek)
                        .thenComparing(TeacherScheduleDTO::periodNumber))
                .toList();
    }

    private ScheduleResponseDTO mapToResponseDTO(Schedule schedule) {
        return new ScheduleResponseDTO(
                schedule.getId(),
                schedule.getTeachingAssignment().getId(),
                schedule.getDayOfWeek(),
                schedule.getPeriodNumber(),
                schedule.getStartTime().toString(),
                schedule.getEndTime().toString(),
                schedule.getRoomNumber(),
                schedule.getTeachingAssignment().getSubject().getName(),
                schedule.getTeachingAssignment().getTeacher().getFirstName() + " " + 
                        schedule.getTeachingAssignment().getTeacher().getLastName(),
                schedule.getTeachingAssignment().getClassEntity().getClassName(),
                schedule.getIsActive()
        );
    }

    // New endpoints implementation
    public List<TeacherClassDTO> getTeacherClasses(Long teacherId) {
        List<Class> classes = scheduleRepository.findClassesByTeacherId(teacherId);
        
        // Load ALL teacher schedules once, not once per class
        List<Schedule> allSchedules = scheduleRepository.findByTeacherId(teacherId);
        
        return classes.stream()
                .map(c -> {
                    String subject = allSchedules.stream()
                            .filter(s -> s.getTeachingAssignment().getClassEntity().getId().equals(c.getId()))
                            .findFirst()
                            .map(s -> s.getTeachingAssignment().getSubject().getName())
                            .orElse("");
                    
                    // Count students
                    Long studentCount = studentEnrollmentRepository.countByClassEntityId(c.getId());
                    
                    return new TeacherClassDTO(
                            c.getId(),
                            c.getClassName(),
                            c.getGrade(),
                            subject,
                            studentCount.intValue()
                    );
                })
                .sorted(Comparator.comparing(TeacherClassDTO::gradeLevel)
                        .thenComparing(TeacherClassDTO::className))
                .toList();
    }

    public CalendarScheduleDTO getTeacherCalendar(Long teacherId) {
        List<Schedule> schedules = scheduleRepository.findByTeacherId(teacherId);
        
        Map<Integer, List<CalendarSlotDTO>> scheduleByDay = schedules.stream()
                .collect(Collectors.groupingBy(
                        Schedule::getDayOfWeek,
                        Collectors.mapping(s -> new CalendarSlotDTO(
                                s.getId(),
                                s.getPeriodNumber(),
                                s.getStartTime().toString(),
                                s.getEndTime().toString(),
                                s.getRoomNumber(),
                                s.getTeachingAssignment().getSubject().getName(),
                                s.getTeachingAssignment().getClassEntity().getClassName(),
                                s.getTeachingAssignment().getTeacher().getFirstName() + " " + 
                                        s.getTeachingAssignment().getTeacher().getLastName(),
                                s.getTeachingAssignment().getClassEntity().getGrade()
                        ), Collectors.toList())
                ));
        
        // Sort each day's schedules by period number
        scheduleByDay.values().forEach(daySchedules -> 
                daySchedules.sort(Comparator.comparing(CalendarSlotDTO::periodNumber))
        );
        
        return new CalendarScheduleDTO(scheduleByDay);
    }

    public CalendarScheduleDTO getStudentCalendar(Long studentId) {
        List<Schedule> schedules = scheduleRepository.findByStudentId(studentId);
        
        Map<Integer, List<CalendarSlotDTO>> scheduleByDay = schedules.stream()
                .collect(Collectors.groupingBy(
                        Schedule::getDayOfWeek,
                        Collectors.mapping(s -> new CalendarSlotDTO(
                                s.getId(),
                                s.getPeriodNumber(),
                                s.getStartTime().toString(),
                                s.getEndTime().toString(),
                                s.getRoomNumber(),
                                s.getTeachingAssignment().getSubject().getName(),
                                s.getTeachingAssignment().getClassEntity().getClassName(),
                                s.getTeachingAssignment().getTeacher().getFirstName() + " " + 
                                        s.getTeachingAssignment().getTeacher().getLastName(),
                                s.getTeachingAssignment().getClassEntity().getGrade()
                        ), Collectors.toList())
                ));
        
        // Sort each day's schedules by period number
        scheduleByDay.values().forEach(daySchedules -> 
                daySchedules.sort(Comparator.comparing(CalendarSlotDTO::periodNumber))
        );
        
        return new CalendarScheduleDTO(scheduleByDay);
    }

    // ──── Configuration constants ────
    private static final int MAX_SAME_SUBJECT_PER_CLASS_PER_DAY = 2;  // hard cap
    private static final int MAX_LESSONS_PER_CLASS_PER_DAY = 7;
    private static final int MAX_LESSONS_PER_TEACHER_PER_DAY = 6;
    /** Max days the same subject may sit in the exact same period slot for a class */
    private static final int MAX_SAME_PERIOD_SLOT_ACROSS_DAYS = 2;
    /** Ideal max lessons per teacher per day — above this gets penalized */
    private static final int IDEAL_TEACHER_LESSONS_PER_DAY = 4;
    /** Ideal balanced lessons per class per day */
    private static final int IDEAL_CLASS_LESSONS_PER_DAY = 5;

    @Transactional
    public List<ScheduleResponseDTO> generateSchedule(boolean clearExisting) {
        // 1. Optionally clear all existing schedules
        if (clearExisting) {
            scheduleRepository.deleteAll();
        }

        // 2. Load ALL periods sorted by period_number
        List<Period> allPeriods = periodRepository.findAll().stream()
                .sorted(Comparator.comparing(Period::getPeriodNumber))
                .toList();
        if (allPeriods.isEmpty()) {
            throw new RuntimeException("No periods defined. Please create periods first.");
        }

        // Separate lesson-type periods from break/lunch
        List<Period> lessonPeriods = allPeriods.stream()
                .filter(p -> p.getPeriodType() == Period.PeriodType.LESSON)
                .toList();
        if (lessonPeriods.isEmpty()) {
            throw new RuntimeException("No lesson periods defined. Please create lesson-type periods.");
        }

        // 3. Load all active teaching assignments (filter out incomplete ones)
        List<TeachingAssignment> assignments = teachingAssignmentRepository.findAllByIsActiveTrue().stream()
                .filter(ta -> ta.getSubject() != null && ta.getTeacher() != null && ta.getClassEntity() != null)
                .collect(Collectors.toList());
        if (assignments.isEmpty()) {
            throw new RuntimeException("No active teaching assignments found (or all are missing subject/teacher/class).");
        }

        // 4. School days (Mon–Fri)
        int[] schoolDays = {1, 2, 3, 4, 5};
        Random rng = new Random();

        // 5. Build occupancy trackers
        Map<String, Set<Long>> teacherSlots  = new HashMap<>();  // "day-periodNum" → teacher IDs
        Map<String, Set<Long>> classSlots    = new HashMap<>();  // "day-periodNum" → class IDs
        Map<String, Map<Long, Integer>> subjectPerClassDay = new HashMap<>(); // "day-classId" → {subjectId→count}
        Map<String, Integer> classLessonsPerDay   = new HashMap<>();  // "day-classId" → count
        Map<String, Integer> teacherLessonsPerDay = new HashMap<>();  // "day-tTeacherId" → count
        // "classId-subjectId-periodNum" → count of days using this exact slot
        Map<String, Integer> subjectPeriodRepeat  = new HashMap<>();

        // Pre-populate from existing schedules if not cleared
        if (!clearExisting) {
            List<Schedule> existing = scheduleRepository.findAll().stream()
                    .filter(s -> s.getIsActive() != null && s.getIsActive())
                    .toList();
            for (Schedule s : existing) {
                String slotKey = s.getDayOfWeek() + "-" + s.getPeriodNumber();
                Long tId = s.getTeachingAssignment().getTeacher().getId();
                Long cId = s.getTeachingAssignment().getClassEntity().getId();
                Long sId = s.getTeachingAssignment().getSubject().getId();

                teacherSlots.computeIfAbsent(slotKey, k -> new HashSet<>()).add(tId);
                classSlots.computeIfAbsent(slotKey, k -> new HashSet<>()).add(cId);

                String classDayKey   = s.getDayOfWeek() + "-" + cId;
                String teacherDayKey = s.getDayOfWeek() + "-t" + tId;
                subjectPerClassDay.computeIfAbsent(classDayKey, k -> new HashMap<>())
                        .merge(sId, 1, Integer::sum);
                classLessonsPerDay.merge(classDayKey, 1, Integer::sum);
                teacherLessonsPerDay.merge(teacherDayKey, 1, Integer::sum);

                String repeatKey = cId + "-" + sId + "-" + s.getPeriodNumber();
                subjectPeriodRepeat.merge(repeatKey, 1, Integer::sum);
            }
        }

        // 6. Sort assignments: subjects with more hours first (harder to place)
        //    Shuffle first for randomness, then stable-sort by hours descending
        Collections.shuffle(assignments, rng);
        assignments.sort((a, b) -> {
            int hA = a.getSubject().getHoursPerWeek() != null ? a.getSubject().getHoursPerWeek() : 2;
            int hB = b.getSubject().getHoursPerWeek() != null ? b.getSubject().getHoursPerWeek() : 2;
            return Integer.compare(hB, hA);
        });

        List<Schedule> generated = new ArrayList<>();
        List<String> warnings  = new ArrayList<>();

        // 7. Place each assignment across the week using scored candidate selection
        for (TeachingAssignment ta : assignments) {
            int hoursNeeded = ta.getSubject().getHoursPerWeek() != null ? ta.getSubject().getHoursPerWeek() : 2;
            Long teacherId  = ta.getTeacher().getId();
            Long classId    = ta.getClassEntity().getId();
            Long subjectId  = ta.getSubject().getId();
            String roomNumber = ta.getClassEntity().getRoomNumber();
            int placed = 0;

            // Build list of all candidate (day, periodIndex) pairs and shuffle
            List<int[]> candidates = new ArrayList<>();
            for (int dayIdx = 0; dayIdx < schoolDays.length; dayIdx++) {
                for (int pIdx = 0; pIdx < lessonPeriods.size(); pIdx++) {
                    candidates.add(new int[]{dayIdx, pIdx});
                }
            }
            Collections.shuffle(candidates, rng);

            // Track which days and periods THIS assignment has already used
            Set<Integer> daysUsed = new HashSet<>();
            Set<Integer> periodsUsed = new HashSet<>();

            for (int round = 0; round < hoursNeeded && !candidates.isEmpty(); round++) {
                int bestIdx = -1;
                int bestScore = Integer.MAX_VALUE;

                for (int i = 0; i < candidates.size(); i++) {
                    int day = schoolDays[candidates.get(i)[0]];
                    int pIdx = candidates.get(i)[1];
                    Period period = lessonPeriods.get(pIdx);
                    String slotKey       = day + "-" + period.getPeriodNumber();
                    String classDayKey   = day + "-" + classId;
                    String teacherDayKey = day + "-t" + teacherId;

                    // ═══ Hard constraints (skip if violated) ═══
                    if (teacherSlots.getOrDefault(slotKey, Collections.emptySet()).contains(teacherId))
                        continue;
                    if (classSlots.getOrDefault(slotKey, Collections.emptySet()).contains(classId))
                        continue;
                    int subjectCountToday = subjectPerClassDay
                            .getOrDefault(classDayKey, Collections.emptyMap())
                            .getOrDefault(subjectId, 0);
                    if (subjectCountToday >= MAX_SAME_SUBJECT_PER_CLASS_PER_DAY)
                        continue;
                    if (classLessonsPerDay.getOrDefault(classDayKey, 0) >= MAX_LESSONS_PER_CLASS_PER_DAY)
                        continue;
                    if (teacherLessonsPerDay.getOrDefault(teacherDayKey, 0) >= MAX_LESSONS_PER_TEACHER_PER_DAY)
                        continue;

                    // ═══ Soft scoring (lower = better) ═══
                    int score = 0;

                    // (A) SAME SUBJECT ON SAME DAY — strong penalty to avoid doubles
                    //     Already having 1 of this subject today → heavy cost
                    if (subjectCountToday >= 1) score += 60;

                    // (B) SPREAD ACROSS DAYS — penalize reusing a day this assignment
                    //     already occupies (grows with how many lessons already on that day)
                    if (daysUsed.contains(day)) score += 40;

                    // (C) PERIOD VARIETY — avoid always landing in the same period slot
                    if (periodsUsed.contains(period.getPeriodNumber())) score += 25;

                    // (D) PERIOD SLOT REPETITION — penalize the same class+subject always
                    //     being in the same period number across the whole week
                    String repeatKey = classId + "-" + subjectId + "-" + period.getPeriodNumber();
                    int repeatCount = subjectPeriodRepeat.getOrDefault(repeatKey, 0);
                    if (repeatCount >= MAX_SAME_PERIOD_SLOT_ACROSS_DAYS) score += 80;
                    else score += repeatCount * 20;

                    // (E) CLASS DAILY BALANCE — penalize days that are already heavy
                    int classLoad = classLessonsPerDay.getOrDefault(classDayKey, 0);
                    if (classLoad >= IDEAL_CLASS_LESSONS_PER_DAY) score += (classLoad - IDEAL_CLASS_LESSONS_PER_DAY + 1) * 12;
                    // Also penalize if this day is much heavier than the lightest day
                    int minClassLoad = Integer.MAX_VALUE;
                    for (int d : schoolDays) {
                        minClassLoad = Math.min(minClassLoad, classLessonsPerDay.getOrDefault(d + "-" + classId, 0));
                    }
                    if (minClassLoad < Integer.MAX_VALUE) {
                        int imbalance = classLoad - minClassLoad;
                        if (imbalance >= 2) score += imbalance * 8;
                    }

                    // (F) TEACHER DAILY BALANCE — penalize overloading a teacher on one day
                    int teacherLoad = teacherLessonsPerDay.getOrDefault(teacherDayKey, 0);
                    if (teacherLoad >= IDEAL_TEACHER_LESSONS_PER_DAY) score += (teacherLoad - IDEAL_TEACHER_LESSONS_PER_DAY + 1) * 10;
                    // Balance across the week: penalize if this day is much heavier than teacher's lightest
                    int minTeacherLoad = Integer.MAX_VALUE;
                    for (int d : schoolDays) {
                        minTeacherLoad = Math.min(minTeacherLoad, teacherLessonsPerDay.getOrDefault(d + "-t" + teacherId, 0));
                    }
                    if (minTeacherLoad < Integer.MAX_VALUE) {
                        int tImbalance = teacherLoad - minTeacherLoad;
                        if (tImbalance >= 2) score += tImbalance * 6;
                    }

                    // (G) CONSECUTIVE DAYS — slight penalty for same subject on adjacent days
                    for (int usedDay : daysUsed) {
                        if (Math.abs(usedDay - day) == 1) score += 8;
                    }

                    // (H) PERIOD POSITION — mild preference for mid-morning/early slots
                    //     Penalize very late periods more, and first period slightly
                    //     (to avoid everything piling into period 1)
                    if (pIdx == 0) score += 3;        // slight penalty for always-period-1
                    else if (pIdx <= 3) score += 0;   // periods 2-4 are ideal
                    else score += (pIdx - 3) * 2;     // later periods get a small penalty

                    // (I) TIE-BREAKER — small random jitter to prevent deterministic ties
                    score += rng.nextInt(4);

                    if (score < bestScore) {
                        bestScore = score;
                        bestIdx = i;
                    }
                }

                if (bestIdx == -1) break; // no valid slot found

                // Place the best candidate
                int[] pick = candidates.remove(bestIdx);
                int day = schoolDays[pick[0]];
                Period period = lessonPeriods.get(pick[1]);
                String slotKey       = day + "-" + period.getPeriodNumber();
                String classDayKey   = day + "-" + classId;
                String teacherDayKey = day + "-t" + teacherId;

                Schedule schedule = new Schedule();
                schedule.setTeachingAssignment(ta);
                schedule.setDayOfWeek(day);
                schedule.setPeriodNumber(period.getPeriodNumber());
                schedule.setStartTime(period.getStartTime());
                schedule.setEndTime(period.getEndTime());
                schedule.setRoomNumber(roomNumber);
                schedule.setIsActive(true);
                generated.add(schedule);

                // Update all trackers
                teacherSlots.computeIfAbsent(slotKey, k -> new HashSet<>()).add(teacherId);
                classSlots.computeIfAbsent(slotKey, k -> new HashSet<>()).add(classId);
                subjectPerClassDay.computeIfAbsent(classDayKey, k -> new HashMap<>())
                        .merge(subjectId, 1, Integer::sum);
                classLessonsPerDay.merge(classDayKey, 1, Integer::sum);
                teacherLessonsPerDay.merge(teacherDayKey, 1, Integer::sum);
                String repeatKey = classId + "-" + subjectId + "-" + period.getPeriodNumber();
                subjectPeriodRepeat.merge(repeatKey, 1, Integer::sum);
                daysUsed.add(day);
                periodsUsed.add(period.getPeriodNumber());
                placed++;
            }

            if (placed < hoursNeeded) {
                warnings.add(ta.getTeacher().getFirstName() + " " + ta.getTeacher().getLastName() +
                        " - " + ta.getSubject().getName() + " (" + ta.getClassEntity().getClassName() +
                        "): placed " + placed + "/" + hoursNeeded);
            }
        }

        if (!warnings.isEmpty()) {
            System.out.println("Schedule generation warnings — could not fully place:");
            warnings.forEach(w -> System.out.println("  • " + w));
        }

        // 8. Save all generated schedules
        List<Schedule> saved = scheduleRepository.saveAll(generated);

        return saved.stream()
                .map(this::mapToResponseDTO)
                .sorted(Comparator.comparing(ScheduleResponseDTO::dayOfWeek)
                        .thenComparing(ScheduleResponseDTO::periodNumber))
                .toList();
    }

    private Sort buildListSort(String sortBy, String sortOrder) {
        String requestedSort = sortBy == null || sortBy.isBlank() ? "dayOfWeek" : sortBy.trim();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (requestedSort) {
            case "id" -> Sort.by(direction, "id");
            case "dayOfWeek" -> Sort.by(direction, "dayOfWeek", "periodNumber", "id");
            case "periodNumber" -> Sort.by(direction, "periodNumber", "dayOfWeek", "id");
            case "startTime" -> Sort.by(direction, "startTime").and(Sort.by(direction, "id"));
            case "subject" -> Sort.by(direction, "teachingAssignment.subject.subjectName").and(Sort.by(direction, "id"));
            case "teacher" -> Sort.by(direction, "teachingAssignment.teacher.lastName", "teachingAssignment.teacher.firstName", "id");
            case "className" -> Sort.by(direction, "teachingAssignment.classEntity.className").and(Sort.by(direction, "id"));
            case "roomNumber" -> Sort.by(direction, "roomNumber").and(Sort.by(direction, "id"));
            default -> throw new IllegalArgumentException("Unsupported sortBy value");
        };
    }

    private Specification<Schedule> buildListSpecification(ScheduleListQueryDTO query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Join<Schedule, TeachingAssignment> assignmentJoin = root.join("teachingAssignment", JoinType.LEFT);
            Join<TeachingAssignment, com.edusys.backend.model.User> teacherJoin = assignmentJoin.join("teacher", JoinType.LEFT);
            Join<TeachingAssignment, com.edusys.backend.model.Subject> subjectJoin = assignmentJoin.join("subject", JoinType.LEFT);
            Join<TeachingAssignment, Class> classJoin = assignmentJoin.join("classEntity", JoinType.LEFT);

            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (query.getSearch() != null && !query.getSearch().isBlank()) {
                String term = "%" + query.getSearch().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(subjectJoin.get("subjectName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("firstName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("lastName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(classJoin.get("className")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("roomNumber")), term)
                ));
            }

            if (query.getDayOfWeek() != null) {
                predicates.add(criteriaBuilder.equal(root.get("dayOfWeek"), query.getDayOfWeek()));
            }
            if (query.getClassId() != null) {
                predicates.add(criteriaBuilder.equal(classJoin.get("id"), query.getClassId()));
            }
            if (query.getTeacherId() != null) {
                predicates.add(criteriaBuilder.equal(teacherJoin.get("id"), query.getTeacherId()));
            }
            if (query.getSubjectId() != null) {
                predicates.add(criteriaBuilder.equal(subjectJoin.get("id"), query.getSubjectId()));
            }
            if (query.getGrade() != null) {
                predicates.add(criteriaBuilder.equal(classJoin.get("grade"), query.getGrade()));
            }
            if (query.getRoomNumber() != null && !query.getRoomNumber().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("roomNumber")),
                        query.getRoomNumber().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (query.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), query.getIsActive()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
