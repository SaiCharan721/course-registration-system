package com.saicharan.courseregistration.service;

import com.saicharan.courseregistration.domain.Course;
import com.saicharan.courseregistration.domain.EnrollmentStatus;
import com.saicharan.courseregistration.dto.CourseDtos.CourseRequest;
import com.saicharan.courseregistration.dto.CourseDtos.CourseResponse;
import com.saicharan.courseregistration.dto.PageResponse;
import com.saicharan.courseregistration.exception.ConflictException;
import com.saicharan.courseregistration.exception.NotFoundException;
import com.saicharan.courseregistration.repository.CourseRepository;
import com.saicharan.courseregistration.repository.EnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CourseService {

    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public CourseService(CourseRepository courses, EnrollmentRepository enrollments) {
        this.courses = courses;
        this.enrollments = enrollments;
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> search(String query, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("code"));
        Page<Course> result = StringUtils.hasText(query)
                ? courses.search(query.trim(), pageable)
                : courses.findAll(pageable);
        return PageResponse.from(result.map(CourseResponse::from));
    }

    @Transactional(readOnly = true)
    public CourseResponse get(Long id) {
        return CourseResponse.from(find(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        if (courses.existsByCodeIgnoreCase(request.code())) {
            throw new ConflictException("Course code " + request.code() + " already exists");
        }
        Course course = new Course(request.code().toUpperCase(), request.title(), request.description(),
                request.instructor(), request.credits(), request.capacity(), request.schedule());
        return CourseResponse.from(courses.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courses.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Course " + id + " not found"));
        if (request.capacity() < course.getSeatsTaken()) {
            throw new ConflictException("Capacity cannot be lower than the " + course.getSeatsTaken()
                    + " students already enrolled");
        }
        course.update(request.title(), request.description(), request.instructor(),
                request.credits(), request.capacity(), request.schedule());
        return CourseResponse.from(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = find(id);
        if (enrollments.countByCourseIdAndStatus(id, EnrollmentStatus.ACTIVE) > 0) {
            throw new ConflictException("Course has active enrollments; drop them before deleting");
        }
        courses.delete(course);
    }

    private Course find(Long id) {
        return courses.findById(id).orElseThrow(() -> new NotFoundException("Course " + id + " not found"));
    }
}
