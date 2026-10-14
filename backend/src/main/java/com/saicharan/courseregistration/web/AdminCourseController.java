package com.saicharan.courseregistration.web;

import com.saicharan.courseregistration.dto.CourseDtos.CourseRequest;
import com.saicharan.courseregistration.dto.CourseDtos.CourseResponse;
import com.saicharan.courseregistration.dto.EnrollmentDtos.RosterEntry;
import com.saicharan.courseregistration.service.CourseService;
import com.saicharan.courseregistration.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-only endpoints; access is restricted to ROLE_ADMIN in SecurityConfig. */
@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public AdminCourseController(CourseService courseService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return courseService.create(request);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }

    @GetMapping("/{id}/enrollments")
    public List<RosterEntry> roster(@PathVariable Long id) {
        return enrollmentService.roster(id);
    }
}
