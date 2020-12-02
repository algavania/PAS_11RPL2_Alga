package com.practice.myapplication.model;

public class UploadProfile {
    private String name, email, about, location, image;

    public UploadProfile() {

    }

    public UploadProfile(String name, String email, String about, String location, String image) {
        this.name = name;
        this.email = email;
        this.about = about;
        this.location = location;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
