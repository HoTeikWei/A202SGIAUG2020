package com.example.assignment;

public class User {

    private String name;
    private String emailAddress;
    private String gender;
    private String imageUrl;
    private String imageName;

    public User() {
    }

    public User(String name, String emailAddress, String gender, String imageUrl, String imageName) {
        this.name = name;
        this.emailAddress = emailAddress;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gentle) {
        this.gender = gentle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

