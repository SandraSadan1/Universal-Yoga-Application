package com.example.uya.model;

public class YogaClass {
    private int id;
    private String date;
    private int courseId;
    private String teacher;
    private String comments;

    // Constructors
    public YogaClass() {
        // Default constructor
    }

    public YogaClass(int id, String date, int courseId, String teacher, String comments) {
        this.id = id;
        this.date = date;
        this.courseId = courseId;
        this.teacher = teacher;
        this.comments = comments;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getCourseId() {
        return courseId;
    }

    public String  getTeacherName() {
        return teacher;
    }
    public String  getComments() {
        return comments;
    }


    // Setters
    public void setId(int id) {
        this.id = id;
    }

}
