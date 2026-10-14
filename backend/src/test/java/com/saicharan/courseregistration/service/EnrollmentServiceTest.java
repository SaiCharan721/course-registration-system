package com.saicharan.courseregistration.service;

import com.saicharan.courseregistration.domain.Course;
import com.saicharan.courseregistration.domain.Enrollment;
import com.saicharan.courseregistration.domain.EnrollmentStatus;
import com.saicharan.courseregistration.domain.Role;
import com.saicharan.courseregistration.domain.User;
import com.saicharan.courseregistration.dto.EnrollmentDtos.EnrollmentResponse;
import com.saicharan.courseregistration.exception.ConflictException;
import com.saicharan.courseregistration.repository.CourseRepository;
import com.saicharan.courseregistration.repository.EnrollmentRepository;
import com.saicharan.courseregistration.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    private static final String EMAIL = "student@test.edu";

    @Mock
    private EnrollmentRepository enrollments;
    @Mock
    private CourseRepository courses;
    @Mock
    private UserRepository users;

    private EnrollmentService service;
    private User student;

    @BeforeEach
    void setUp() {
        service = new EnrollmentService(enrollments, courses, users, 6);
        student = new User("Test Student", EMAIL, "hash", Role.STUDENT);
        when(users.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(student));
    }

    @Test
    void enrollTakesASeat() {
        Course course = new Course("CS-501", "Java", "", "Dr. P", 3, 2, "Mon");
        when(courses.findByIdForUpdate(1L)).thenReturn(Optional.of(course));
        when(enrollments.existsByStudentIdAndCourseIdAndStatus(any(), eq(1L), eq(EnrollmentStatus.ACTIVE)))
                .thenReturn(false);
        when(enrollments.findByStudentIdAndStatusOrderByEnrolledAtDesc(any(), eq(EnrollmentStatus.ACTIVE)))
                .thenReturn(List.of());
        when(enrollments.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse response = service.enroll(EMAIL, 1L);

        assertThat(response.courseCode()).isEqualTo("CS-501");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(course.getSeatsAvailable()).isEqualTo(1);
    }

    @Test
    void enrollFailsWhenCourseIsFull() {
        Course course = new Course("CS-575", "Distributed", "", "Dr. R", 3, 1, "Wed");
        course.takeSeat();
        when(courses.findByIdForUpdate(2L)).thenReturn(Optional.of(course));
        when(enrollments.existsByStudentIdAndCourseIdAndStatus(any(), eq(2L), eq(EnrollmentStatus.ACTIVE)))
                .thenReturn(false);

        assertThatThrownBy(() -> service.enroll(EMAIL, 2L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("full");
        verify(enrollments, never()).save(any());
    }

    @Test
    void enrollFailsWhenAlreadyEnrolled() {
        Course course = new Course("CS-520", "Web", "", "Prof. N", 3, 10, "Tue");
        when(courses.findByIdForUpdate(3L)).thenReturn(Optional.of(course));
        when(enrollments.existsByStudentIdAndCourseIdAndStatus(any(), eq(3L), eq(EnrollmentStatus.ACTIVE)))
                .thenReturn(true);

        assertThatThrownBy(() -> service.enroll(EMAIL, 3L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already enrolled");
        assertThat(course.getSeatsTaken()).isZero();
    }

    @Test
    void enrollFailsWhenOverCreditLimit() {
        Course current = new Course("IS-540", "DB", "", "Prof. K", 4, 10, "Tue");
        Course wanted = new Course("CS-530", "Cloud", "", "Dr. G", 3, 10, "Mon");
        when(courses.findByIdForUpdate(4L)).thenReturn(Optional.of(wanted));
        when(enrollments.existsByStudentIdAndCourseIdAndStatus(any(), anyLong(), eq(EnrollmentStatus.ACTIVE)))
                .thenReturn(false);
        when(enrollments.findByStudentIdAndStatusOrderByEnrolledAtDesc(any(), eq(EnrollmentStatus.ACTIVE)))
                .thenReturn(List.of(new Enrollment(student, current)));

        assertThatThrownBy(() -> service.enroll(EMAIL, 4L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("credit limit");
    }
}
