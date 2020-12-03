package com.example.assignment;

import java.util.Date;

public class Comment {
    String emailAddress;
    String text;
    Date date;

    public Comment() {
    }

    public Comment(String emailAddress, String text) {
        this.emailAddress = emailAddress;
        this.text = text;
        this.date = new Date();
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
