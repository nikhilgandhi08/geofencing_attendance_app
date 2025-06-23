package com.example.app006.models;

public class Question {
    private String testTakerType;
    private String questionText;
    private String option1, option2, option3, option4;
    private int correctOption; // 1-4

    public Question() { }

    public Question(String testTakerType, String questionText,
                    String option1, String option2,
                    String option3, String option4,
                    int correctOption) {
        this.testTakerType = testTakerType;
        this.questionText  = questionText;
        this.option1       = option1;
        this.option2       = option2;
        this.option3       = option3;
        this.option4       = option4;
        this.correctOption = correctOption;
    }

    // getters & setters omitted for brevity
}
