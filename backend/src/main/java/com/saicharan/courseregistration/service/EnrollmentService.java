package com.saicharan.courseregistration.service;

import com.saicharan.courseregistration.domain.Course;
import com.saicharan.courseregistration.domain.Enrollment;
import com.saicharan.courseregistration.domain.EnrollmentStatus;
import com.saicharan.courseregistration.domain.User;
import com.saicharan.courseregistration.dto.EnrollmentDtos.EnrollmentResponse;
import com.saicharan.courseregistration.dto.EnrollmentDtos.RosterEntry;
import com.saicharan.courseregistration.exception.ConflictException;
import com.saicharan.courseregistration.exception.NotFoundException;
import com.saicharan.courseregistration.repository.CourseRepository;
import com.saicharan.courseregistration.repository.EnrollmentRepository;
import com.saicharan.courseregistration.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollments;
    private final CourseRepository courses;
    private final UserRepository users;
    private final int maxCredits;

    public EnrollmentService(EnrollmentRepository enrollments, CourseRepository courses, UserRepository users,
                             @Value("${app.enrollment.max-credits:18}") int maxCredits) {
        this.enrollments = enrollments;
        this.courses = courses;
        this.users = users;
        this.maxCredits = maxCredits;
    }

    /**
     * Enrolls the student in a course. The course row is locked for the length
     * of the transaction, so the seat check and the seat update cannot race.
     */
    @Transactional
    public EnrollmentResponse enroll(String studentEmail, Long courseId) {
        User student = findStudent(studentEmail);
        Course course = courses.findByIdForUpdate(courseId)
                .orElseThrow(() -> new NotFoundException("Course " + courseId + " not found"));

        if (enrollments.existsByStudentIdAndCourseIdAndStatus(student.getId(), courseId, EnrollmentStatus.ACTIVE)) {
            throw new ConflictException("You are already enrolled in " + course.getCode());
        }
        if (!course.hasOpenSeat()) {
            throw new ConflictException(course.getCode() + " is full");
        }
        int currentCredits = enrollments
                .findByStudentIdAndStatusOrderByEnrolledAtDesc(student.getId(), EnrollmentStatus.ACTIVE)
                .stream().mapToInt(e -> e.getCourse().getCredits()).sum();
        if (currentCredits + course.getCredits() > maxCredits) {
            throw new ConflictException("Enrolling would put you over the " + maxCredits + "-credit limit");
        }

        course.takeSeat();
        Enrollment saved = enrollments.save(new Enrollment(student, course));
        log.info("Student {} enrolled in {} ({} seats left)", student.getId(), course.getCode(),
                course.getSeatsAvailable());
        return EnrollmentResponse.from(saved);
    }

    @Transactional
    public void drop(String studentEmail, Long enrollmentId) {
        User student = findStudent(studentEmail);
        Enrollment enrollment = enrollments.findByIdAndStudentId(enrollmentId, student.getId())
                .orElseThrow(() -> new NotFoundException("Enrollment " + enrollmentId + " not found"));
        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new ConflictException("This enrollment was already dropped");
        }
        Course course = courses.findByIdForUpdate(enrollment.getCourse().getId())
                .orElseThrow(() -> new NotFoundException("Course not found"));
        enrollment.drop();
        course.releaseSeat();
        log.info("Student {} dropped {}", student.getId(), course.getCode());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> myEnrollments(String studentEmail) {
        User student = findStudent(studentEmail);
        return enrollments.findByStudentIdAndStatusOrderByEnrolledAtDesc(student.getId(), EnrollmentStatus.ACTIVE)
                .stream().map(EnrollmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RosterEntry> roster(Long courseId) {
        if (!courses.existsById(courseId)) {
            throw new NotFoundException("Course " + courseId + " not found");
        }
        return enrollments.findByCourseIdAndStatusOrderByEnrolledAtAsc(courseId, EnrollmentStatus.ACTIVE)
                .stream().map(RosterEntry::from).toList();
    }

    private User findStudent(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User " + email + " not found"));
    }
}
