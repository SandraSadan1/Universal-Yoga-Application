package com.example.uya.model;

import java.util.List;

public class YogaCourse {

    private boolean isClicked;

    // Constructor and other methods

    public boolean isClicked() {
        return isClicked;
    }

    public void setClicked(boolean clicked) {
        isClicked = clicked;
    }
    private int id;
    private String day;
    private String duration;
    private int price;
    private String timeOfCourse;
    private String yogaType;
    private String capacity;
    private String description;
    private List<YogaClass> classList;

    // Constructors
    public YogaCourse() {
        // Default constructor
    }

    public YogaCourse(int id, String day, String duration, int price, String timeOfCourse, String yogaType, String capacity, String description) {
        this.id = id;
        this.day = day;
        this.duration = duration;
        this.price = price;
        this.timeOfCourse = timeOfCourse;
        this.yogaType = yogaType;
        this.capacity = capacity;
        this.description = description;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDay() {
        return day;
    }

    public String getDuration() {
        return duration;
    }

    public int getPrice() {
        return price;
    }

    public String getTimeOfCourse() {
        return timeOfCourse;
    }

    public String getYogaType() {
        return yogaType;
    }

    public String getCapacity() {
        return capacity;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setTimeOfCourse(String timeOfCourse) {
        this.timeOfCourse = timeOfCourse;
    }

    public void setYogaType(String yogaType) {
        this.yogaType = yogaType;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setClassList(List<YogaClass> classList) {
        this.classList = classList;
    }
}
