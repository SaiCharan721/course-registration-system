package com.saicharan.courseregistration.config;

import com.saicharan.courseregistration.domain.Course;
import com.saicharan.courseregistration.domain.Role;
import com.saicharan.courseregistration.domain.User;
import com.saicharan.courseregistration.repository.CourseRepository;
import com.saicharan.courseregistration.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Loads demo users and courses on an empty database so the app is usable right away. */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final CourseRepository courses;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, CourseRepository courses, PasswordEncoder encoder) {
        this.users = users;
        this.courses = courses;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() == 0) {
            users.save(new User("Admin User", "admin@demo.edu", encoder.encode("Admin@1234"), Role.ADMIN));
            users.save(new User("Demo Student", "student@demo.edu", encoder.encode("Student@1234"), Role.STUDENT));
            log.info("Seeded demo users admin@demo.edu and student@demo.edu");
        }
        if (courses.count() == 0) {
            courses.saveAll(List.of(
                    new Course("CS-501", "Advanced Java Programming",
                            "Collections, streams, concurrency and JVM internals.", "Dr. Patel", 3, 40,
                            "Mon/Wed 10:00-11:15"),
                    new Course("CS-520", "Web Application Development",
                            "Building full stack apps with Spring Boot and React.", "Prof. Nguyen", 3, 35,
                            "Tue/Thu 13:00-14:15"),
                    new Course("CS-530", "Cloud Computing with AWS",
                            "EC2, S3, containers and CI/CD pipelines.", "Dr. Garcia", 3, 30,
                            "Mon/Wed 14:30-15:45"),
                    new Course("IS-540", "Database Systems",
                            "Relational design, SQL tuning and indexing.", "Prof. Kim", 3, 45,
                            "Tue/Thu 09:00-10:15"),
                    new Course("IS-560", "Information Security",
                            "Authentication, authorization and secure coding.", "Dr. Okafor", 3, 25,
                            "Fri 09:00-11:45"),
                    new Course("CS-575", "Distributed Systems",
                            "Microservices, messaging with Kafka and fault tolerance.", "Dr. Rossi", 3, 3,
                            "Wed 18:00-20:45")));
            log.info("Seeded demo courses");
        }
    }
}
