package com.example.cleanblog.models;

public class Comment {
    private String userId;
    private String postId;
    private String comment;
    private String timeStamp;

    public Comment() {
    }

    public Comment(String userId, String postId, String comment, String timeStamp) {
        this.userId = userId;
        this.postId = postId;
        this.comment = comment;
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
