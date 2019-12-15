package com.example.cleanblog.models;

public class Like {
    private String userId;
    private String postId;
    private String timestamp;

    public Like() {
    }

    public Like(String userId, String postId, String timestamp) {
        this.userId = userId;
        this.postId = postId;
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
