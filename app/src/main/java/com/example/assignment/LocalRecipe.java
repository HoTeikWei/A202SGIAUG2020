package com.example.assignment;

import java.util.Date;
import java.util.List;

public class LocalRecipe {
    private int id;
    private String name;
    private String tag;
    private String ingredient;
    private String step;
    private String user;

    public LocalRecipe() {
    }

    public LocalRecipe(int id, String name, String tag, String ingredient, String step, String user) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.ingredient = ingredient;
        this.step = step;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
