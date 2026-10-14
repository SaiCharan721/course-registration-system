package com.saicharan.courseregistration.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saicharan.courseregistration.domain.Role;
import com.saicharan.courseregistration.domain.User;
import com.saicharan.courseregistration.repository.CourseRepository;
import com.saicharan.courseregistration.repository.EnrollmentRepository;
import com.saicharan.courseregistration.repository.UserRepository;
import com.saicharan.courseregistration.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationFlowTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private UserRepository users;
    @Autowired
    private CourseRepository courses;
    @Autowired
    private EnrollmentRepository enrollments;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private EnrollmentService enrollmentService;

    @BeforeEach
    void cleanDatabase() {
        enrollments.deleteAll();
        courses.deleteAll();
        users.deleteAll();
        users.save(new User("Admin", "admin@test.edu", encoder.encode("Admin@1234"), Role.ADMIN));
    }

    @Test
    void studentCanRegisterBrowseEnrollAndDrop() throws Exception {
        String adminToken = login("admin@test.edu", "Admin@1234");
        long courseId = createCourse(adminToken, "CS-501", 2);

        String studentToken = register("Sam Student", "sam@test.edu");

        mvc.perform(get("/api/courses").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].seatsAvailable").value(2));

        String body = mvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("CS-501"))
                .andReturn().getResponse().getContentAsString();
        long enrollmentId = json.readTree(body).get("id").asLong();

        mvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/enrollments/me").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(delete("/api/enrollments/" + enrollmentId).header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/courses/" + courseId))
                .andExpect(jsonPath("$.seatsAvailable").value(2));
    }

    @Test
    void securityRulesAreEnforced() throws Exception {
        String studentToken = register("Pat Student", "pat@test.edu");

        mvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson("CS-999", 10)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pat@test.edu\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidInputReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void lastSeatCannotBeTakenTwice() throws Exception {
        String adminToken = login("admin@test.edu", "Admin@1234");
        long courseId = createCourse(adminToken, "CS-575", 3);

        int students = 10;
        List<String> emails = new ArrayList<>();
        for (int i = 0; i < students; i++) {
            String email = "load" + i + "@test.edu";
            users.save(new User("Load " + i, email, "x", Role.STUDENT));
            emails.add(email);
        }

        ExecutorService pool = Executors.newFixedThreadPool(students);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        for (String email : emails) {
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    enrollmentService.enroll(email, courseId);
                    succeeded.incrementAndGet();
                } catch (RuntimeException expected) {
                    // course full
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertThat(succeeded.get()).isEqualTo(3);
        assertThat(courses.findById(courseId).orElseThrow().getSeatsTaken()).isEqualTo(3);
    }

    private String register(String name, String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + email
                                + "\",\"password\":\"Student@1234\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private long createCourse(String adminToken, String code, int capacity) throws Exception {
        String body = mvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson(code, capacity)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(body);
        return node.get("id").asLong();
    }

    private static String courseJson(String code, int capacity) {
        return "{\"code\":\"" + code + "\",\"title\":\"Advanced Java Programming\",\"description\":\"Streams\","
                + "\"instructor\":\"Dr. Patel\",\"credits\":3,\"capacity\":" + capacity
                + ",\"schedule\":\"Mon 10:00\"}";
    }
}
