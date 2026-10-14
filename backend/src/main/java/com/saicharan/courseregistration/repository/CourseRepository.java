package com.saicharan.courseregistration.repository;

import com.saicharan.courseregistration.domain.Course;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
            select c from Course c
            where lower(c.title) like lower(concat('%', :q, '%'))
               or lower(c.code) like lower(concat('%', :q, '%'))
               or lower(c.instructor) like lower(concat('%', :q, '%'))
            """)
    Page<Course> search(@Param("q") String query, Pageable pageable);

    /**
     * Locks the course row for the rest of the transaction so two students
     * cannot take the last seat at the same time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findByIdForUpdate(@Param("id") Long id);

    boolean existsByCodeIgnoreCase(String code);
}
