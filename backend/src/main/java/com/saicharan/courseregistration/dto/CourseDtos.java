package com.saicharan.courseregistration.dto;

import com.saicharan.courseregistration.domain.Course;
import jakarta.validation.constraints.*;

public final class CourseDtos {

    private CourseDtos() {
    }

    public record CourseRequest(
            @NotBlank @Size(max = 20) @Pattern(regexp = "^[A-Z]{2,5}-?\\d{3,4}$",
                    message = "must look like CS-501 or CS501") String code,
            @NotBlank @Size(max = 150) String title,
            @Size(max = 2000) String description,
            @NotBlank @Size(max = 100) String instructor,
            @Min(1) @Max(6) int credits,
            @Min(1) @Max(500) int capacity,
            @Size(max = 100) String schedule) {
    }

    public record CourseResponse(Long id, String code, String title, String description, String instructor,
                                 int credits, int capacity, int seatsTaken, int seatsAvailable, String schedule) {

        public static CourseResponse from(Course c) {
            return new CourseResponse(c.getId(), c.getCode(), c.getTitle(), c.getDescription(), c.getInstructor(),
                    c.getCredits(), c.getCapacity(), c.getSeatsTaken(), c.getSeatsAvailable(), c.getSchedule());
        }
    }
}
