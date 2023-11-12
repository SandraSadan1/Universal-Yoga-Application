package com.example.uya.model;

public class TimeSlot {
    private int id;
    private String timeOfCourse;

    public TimeSlot(int id, String courseTime) {
        this.id = id;
        this.timeOfCourse = courseTime;
    }

    public int getId() {
        return id;
    }

    public String getCourseTime() {
        return timeOfCourse;
    }

    // Override toString to provide a meaningful representation
    @Override
    public String toString() {
        return "ID: " + id + ", Time: " + timeOfCourse;
    }
}
