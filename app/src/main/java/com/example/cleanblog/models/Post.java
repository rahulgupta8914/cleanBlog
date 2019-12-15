package com.example.cleanblog.models;

import java.io.Serializable;

public class Post implements Serializable {
    private String postId;
    private String fileName;
    private String imageUrl;
    private String description;
    private User user;
    private String timeStamp;
    private String tags;

    public Post() {
    }

    public Post(String fileName, String imageUrl, String description, User user, String timeStamp, String tags) {
        this.fileName = fileName;
        this.imageUrl = imageUrl;
        this.description = description;
        this.user = user;
        this.timeStamp = timeStamp;
        this.tags = tags;
    }

    public Post(String postId, String fileName, String imageUrl, String description, User user, String timeStamp, String tags) {
        this.postId = postId;
        this.fileName = fileName;
        this.imageUrl = imageUrl;
        this.description = description;
        this.user = user;
        this.timeStamp = timeStamp;
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", user=" + user +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
