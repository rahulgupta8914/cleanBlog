package com.example.cleanblog.models;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String userEmail;

    public User() {
    }

    public User(String userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
