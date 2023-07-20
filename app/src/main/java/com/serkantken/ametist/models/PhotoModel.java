package com.serkantken.ametist.models;

import java.io.Serializable;

public class PhotoModel implements Serializable {
    String photoId;
    String link, squareLink;
    long date;

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSquareLink() {
        return squareLink;
    }

    public void setSquareLink(String squareLink) {
        this.squareLink = squareLink;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
