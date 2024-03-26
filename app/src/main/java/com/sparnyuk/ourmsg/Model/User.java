package com.sparnyuk.ourmsg.Model;

public class User {
    private String imageURL, username, id;
    private long time;

    public User(String imageURL, String username, String id,long time) {
        this.imageURL = imageURL;
        this.username = username;
        this.id = id;
        this.time=time;
    }

    public User() {
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}