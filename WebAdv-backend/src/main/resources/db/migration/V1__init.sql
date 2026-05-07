-- USERS
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       role_flags INTEGER,
                       phone VARCHAR(20),
                       address TEXT,
                       date_of_birth DATE,
                       gender VARCHAR(10) CHECK (gender IN ('M','F','Other')),
                       profile_picture VARCHAR(500),
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CLASSES
CREATE TABLE classes (
                         id BIGSERIAL PRIMARY KEY,
                         class_name VARCHAR(50) NOT NULL,
                         grade INTEGER NOT NULL,
                         section VARCHAR(10),
                         homeroom_teacher_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
                         room_number VARCHAR(50),
                         academic_year VARCHAR(20),
                         student_count INTEGER DEFAULT 0,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SUBJECTS
CREATE TABLE subjects (
                          id BIGSERIAL PRIMARY KEY,
                          subject_name VARCHAR(100) NOT NULL,
                          subject_name_mn VARCHAR(100),
                          subject_code VARCHAR(20) UNIQUE,
                          grade_level INTEGER,
                          hours_per_week INTEGER,
                          is_mandatory BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- STUDENT ENROLLMENT
CREATE TABLE student_enrollment (
                                    id BIGSERIAL PRIMARY KEY,
                                    student_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
                                    class_id BIGINT REFERENCES classes(id) ON UPDATE CASCADE ON DELETE CASCADE,
                                    enrollment_date DATE,
                                    student_number VARCHAR(20) UNIQUE,
                                    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active','graduated','transferred','dropped'))
);

-- TEACHING ASSIGNMENTS
CREATE TABLE teaching_assignments (
                                      id BIGSERIAL PRIMARY KEY,
                                      teacher_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
                                      subject_id BIGINT REFERENCES subjects(id) ON UPDATE CASCADE ON DELETE CASCADE,
                                      class_id BIGINT REFERENCES classes(id) ON UPDATE CASCADE ON DELETE CASCADE,
                                      academic_year VARCHAR(20),
                                      semester INTEGER,
                                      is_active BOOLEAN DEFAULT TRUE
);

-- PERIODS
CREATE TABLE periods (
                         id BIGSERIAL PRIMARY KEY,
                         period_number INTEGER UNIQUE,
                         start_time TIME,
                         end_time TIME,
                         period_type VARCHAR(10) CHECK (period_type IN ('lesson','break','lunch'))
);

-- SCHEDULES
CREATE TABLE schedules (
                           id BIGSERIAL PRIMARY KEY,
                           teaching_assignment_id BIGINT REFERENCES teaching_assignments(id) ON UPDATE CASCADE ON DELETE CASCADE,
                           day_of_week INTEGER CHECK (day_of_week BETWEEN 1 AND 7),
                           period_number INTEGER REFERENCES periods(period_number) ON UPDATE CASCADE ON DELETE SET NULL,
                           start_time TIME,
                           end_time TIME,
                           room_number VARCHAR(50),
                           is_active BOOLEAN DEFAULT TRUE
);

-- ATTENDANCE
CREATE TABLE attendance (
                            id BIGSERIAL PRIMARY KEY,
                            student_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
                            teaching_assignment_id BIGINT REFERENCES teaching_assignments(id) ON UPDATE CASCADE ON DELETE CASCADE,
                            attendance_date DATE NOT NULL,
                            period_number INTEGER,
                            status VARCHAR(10) CHECK (status IN ('present','absent','late','excused','sick')),
                            remarks TEXT,
                            marked_by BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- HOMEWORK
CREATE TABLE homework (
                          id BIGSERIAL PRIMARY KEY,
                          teaching_assignment_id BIGINT REFERENCES teaching_assignments(id) ON UPDATE CASCADE ON DELETE CASCADE,
                          title VARCHAR(255),
                          description TEXT,
                          due_date DATE,
                          max_score INTEGER,
                          type VARCHAR(10) CHECK (type IN ('homework','project','quiz','test')),
                          attachment_url VARCHAR(500),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- HOMEWORK SUBMISSIONS
CREATE TABLE homework_submissions (
                                      id BIGSERIAL PRIMARY KEY,
                                      homework_id BIGINT REFERENCES homework(id) ON UPDATE CASCADE ON DELETE CASCADE,
                                      student_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
                                      submission_text TEXT,
                                      attachment_url VARCHAR(500),
                                      submitted_at TIMESTAMP,
                                      score INTEGER,
                                      feedback TEXT,
                                      graded_by BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
                                      graded_at TIMESTAMP,
                                      status VARCHAR(10) DEFAULT 'assigned' CHECK (status IN ('assigned','submitted','late','graded','missing'))
);

-- PARENT-STUDENT LINK
CREATE TABLE parent_students (
                                 id BIGSERIAL PRIMARY KEY,
                                 parent_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
                                 student_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
                                 relationship VARCHAR(10) CHECK (relationship IN ('father','mother','guardian','other')),
                                 is_primary_contact BOOLEAN DEFAULT FALSE
);

-- GRADES
CREATE TABLE grades (
                        id BIGSERIAL PRIMARY KEY,
                        student_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
                        teaching_assignment_id BIGINT REFERENCES teaching_assignments(id) ON UPDATE CASCADE ON DELETE CASCADE,
                        quarter INTEGER,
                        grade_value INTEGER,
                        grade_type VARCHAR(10) CHECK (grade_type IN ('quarter','midterm','final','yearly')),
                        recorded_by BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
                        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ANNOUNCEMENTS
CREATE TABLE announcements (
                               id BIGSERIAL PRIMARY KEY,
                               title VARCHAR(255),
                               content TEXT,
                               target_role_flags INTEGER,
                               target_class_id BIGINT REFERENCES classes(id) ON UPDATE CASCADE ON DELETE SET NULL,
                               priority VARCHAR(10) DEFAULT 'normal' CHECK (priority IN ('low','normal','high','urgent')),
                               created_by BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               expires_at TIMESTAMP
);