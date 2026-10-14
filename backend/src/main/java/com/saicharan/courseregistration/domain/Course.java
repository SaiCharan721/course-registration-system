package com.saicharan.courseregistration.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 100)
    private String instructor;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int seatsTaken;

    @Column(length = 100)
    private String schedule;

    protected Course() {
    }

    public Course(String code, String title, String description, String instructor,
                  int credits, int capacity, String schedule) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.credits = credits;
        this.capacity = capacity;
        this.schedule = schedule;
    }

    public boolean hasOpenSeat() {
        return seatsTaken < capacity;
    }

    public void takeSeat() {
        if (!hasOpenSeat()) {
            throw new IllegalStateException("Course " + code + " is full");
        }
        seatsTaken++;
    }

    public void releaseSeat() {
        if (seatsTaken > 0) {
            seatsTaken--;
        }
    }

    public void update(String title, String description, String instructor,
                       int credits, int capacity, String schedule) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.credits = credits;
        this.capacity = capacity;
        this.schedule = schedule;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstructor() { return instructor; }
    public int getCredits() { return credits; }
    public int getCapacity() { return capacity; }
    public int getSeatsTaken() { return seatsTaken; }
    public int getSeatsAvailable() { return capacity - seatsTaken; }
    public String getSchedule() { return schedule; }
}
