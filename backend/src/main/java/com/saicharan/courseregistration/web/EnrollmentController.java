package com.saicharan.courseregistration.web;

import com.saicharan.courseregistration.dto.EnrollmentDtos.EnrollRequest;
import com.saicharan.courseregistration.dto.EnrollmentDtos.EnrollmentResponse;
import com.saicharan.courseregistration.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(Authentication auth, @Valid @RequestBody EnrollRequest request) {
        return enrollmentService.enroll(auth.getName(), request.courseId());
    }

    @GetMapping("/me")
    public List<EnrollmentResponse> mine(Authentication auth) {
        return enrollmentService.myEnrollments(auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void drop(Authentication auth, @PathVariable Long id) {
        enrollmentService.drop(auth.getName(), id);
    }
}
