package com.practice.myapplication.model;

public class MatchProperty {

    private String eventName, date, img_home, img_away;
    private Boolean isFavorite;

    public MatchProperty(String eventName, String date, String img_home, String img_away){
        this.eventName = eventName;
        this.date = date;
        this.img_home = img_home;
        this.img_away = img_away;
    }

    public MatchProperty() {

    }

    public String getEventName() {
         return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImg_home() {
        return img_home;
    }

    public void setImg_home(String img_home) {
        this.img_home = img_home;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public String getImg_away() {
        return img_away;
    }

    public void setImg_away(String img_away) {
        this.img_away = img_away;
    }
}
