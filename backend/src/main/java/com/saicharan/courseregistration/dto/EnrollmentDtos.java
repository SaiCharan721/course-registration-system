package com.saicharan.courseregistration.dto;

import com.saicharan.courseregistration.domain.Enrollment;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class EnrollmentDtos {

    private EnrollmentDtos() {
    }

    public record EnrollRequest(@NotNull Long courseId) {
    }

    public record EnrollmentResponse(Long id, Long courseId, String courseCode, String courseTitle,
                                     int credits, String schedule, String status, Instant enrolledAt) {

        public static EnrollmentResponse from(Enrollment e) {
            return new EnrollmentResponse(e.getId(), e.getCourse().getId(), e.getCourse().getCode(),
                    e.getCourse().getTitle(), e.getCourse().getCredits(), e.getCourse().getSchedule(),
                    e.getStatus().name(), e.getEnrolledAt());
        }
    }

    public record RosterEntry(Long enrollmentId, String studentName, String studentEmail, Instant enrolledAt) {

        public static RosterEntry from(Enrollment e) {
            return new RosterEntry(e.getId(), e.getStudent().getFullName(), e.getStudent().getEmail(),
                    e.getEnrolledAt());
        }
    }
}
