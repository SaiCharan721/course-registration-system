package com.saicharan.courseregistration.web;

import com.saicharan.courseregistration.dto.CourseDtos.CourseResponse;
import com.saicharan.courseregistration.dto.PageResponse;
import com.saicharan.courseregistration.service.CourseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public PageResponse<CourseResponse> list(@RequestParam(required = false) String q,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "12") int size) {
        return courseService.search(q, page, size);
    }

    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable Long id) {
        return courseService.get(id);
    }
}
