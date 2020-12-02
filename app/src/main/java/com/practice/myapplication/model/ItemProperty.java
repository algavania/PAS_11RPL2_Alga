package com.practice.myapplication.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ItemProperty extends RealmObject implements Comparable{

    @PrimaryKey
    private Integer id;
    private String team, imageUrl, description, formedYear, stadiumName, stadiumDesc, stadiumImage, stadiumLocation;
    private Boolean isFavorite;

    public ItemProperty(Integer id, String imageUrl, String team, String description, String formedYear, String stadiumName, String stadiumDesc, String stadiumImage, String stadiumLocation, Boolean isFavorite){
        this.id = id;
        this.imageUrl = imageUrl;
        this.team = team;
        this.description = description;
        this.isFavorite = isFavorite;
        this.formedYear = formedYear;
        this.stadiumName = stadiumName;
        this.stadiumDesc = stadiumDesc;
        this.stadiumImage = stadiumImage;
        this.stadiumLocation = stadiumLocation;
    }

    public ItemProperty() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTeam() {
         return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public String getFormedYear() {
        return formedYear;
    }

    public void setFormedYear(String formedYear) {
        this.formedYear = formedYear;
    }

    public String getStadiumName() {
        return stadiumName;
    }

    public void setStadiumName(String stadiumName) {
        this.stadiumName = stadiumName;
    }

    public String getStadiumDesc() {
        return stadiumDesc;
    }

    public void setStadiumDesc(String stadiumDesc) {
        this.stadiumDesc = stadiumDesc;
    }

    public String getStadiumImage() {
        return stadiumImage;
    }

    public void setStadiumImage(String stadiumImage) {
        this.stadiumImage = stadiumImage;
    }

    public String getStadiumLocation() {
        return stadiumLocation;
    }

    public void setStadiumLocation(String stadiumLocation) {
        this.stadiumLocation = stadiumLocation;
    }

    @Override
    public int compareTo(Object o) {
        int compareage=((ItemProperty)o).getId();
        /* For Ascending order*/
        return this.id-compareage;

        /* For Descending order do like this */
//        return compareage-this.id;
    }
}
