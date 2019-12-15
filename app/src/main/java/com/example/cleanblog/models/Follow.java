package com.example.cleanblog.models;

public class Follow {
    private String userId;
    private String followingUserId;

    public Follow() {
    }

    public Follow(String userId, String followerUserId) {
        this.userId = userId;
        this.followingUserId = followerUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFollowingUserId() {
        return followingUserId;
    }

    public void setFollowingUserId(String followingUserId) {
        this.followingUserId = followingUserId;
    }
}
