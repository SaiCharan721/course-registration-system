package com.saicharan.courseregistration.repository;

import com.saicharan.courseregistration.domain.Enrollment;
import com.saicharan.courseregistration.domain.EnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, EnrollmentStatus status);

    @EntityGraph(attributePaths = "course")
    List<Enrollment> findByStudentIdAndStatusOrderByEnrolledAtDesc(Long studentId, EnrollmentStatus status);

    @EntityGraph(attributePaths = "student")
    List<Enrollment> findByCourseIdAndStatusOrderByEnrolledAtAsc(Long courseId, EnrollmentStatus status);

    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findByIdAndStudentId(Long id, Long studentId);

    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
}
