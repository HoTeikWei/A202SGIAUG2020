package com.example.assignment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Recipe {
    private String imgUrl;
    private String imgName;
    private String name;
    private List<String> tag;
    private String ingredient;
    private String step;
    private Date date;
    private String user;

    public Recipe() {
    }

    public Recipe(String imgUrl, String imgName, String name, List<String> tag, String ingredient, String step, String user) {
        this.imgUrl = imgUrl;
        this.imgName = imgName;
        this.name = name;
        this.tag = tag;
        this.ingredient = ingredient;
        this.step = step;
        this.date = Calendar.getInstance().getTime();
        this.user = user;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
