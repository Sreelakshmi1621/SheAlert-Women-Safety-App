package com.example.shealert;

public class FAQModel {

    public String question;
    public String answer;
    public boolean isExpanded;

    public FAQModel(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.isExpanded = false;
    }
}