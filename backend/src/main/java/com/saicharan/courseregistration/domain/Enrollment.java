package com.saicharan.courseregistration.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "enrollments", indexes = {
        @Index(name = "idx_enrollment_student", columnList = "student_id"),
        @Index(name = "idx_enrollment_course", columnList = "course_id")
})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(nullable = false)
    private Instant enrolledAt = Instant.now();

    private Instant droppedAt;

    protected Enrollment() {
    }

    public Enrollment(User student, Course course) {
        this.student = student;
        this.course = course;
    }

    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
        this.droppedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getStudent() { return student; }
    public Course getCourse() { return course; }
    public EnrollmentStatus getStatus() { return status; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public Instant getDroppedAt() { return droppedAt; }
}
