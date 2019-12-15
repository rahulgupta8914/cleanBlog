package com.example.cleanblog.models;

public class UserProfileImage {
    private String profileImageUrl;

    public UserProfileImage() {
    }

    public UserProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
