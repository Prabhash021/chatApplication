package com.example.chatapplication;

import android.net.Uri;

public class UserDataModel {
    private String name;
    private String email;
    private String bio;
    private String dob;
    private String gender;
    private Uri profileUri;
    private String userId;

    public UserDataModel(String name, String email, String bio, String dob, String gender, Uri profileUri, String userId) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.dob = dob;
        this.gender = gender;
        this.profileUri = profileUri;
        this.userId = userId;
    }

    public UserDataModel(String name, String userId, Uri profileUri, String email) {
        this.name = name;
        this.userId = userId;
        this.profileUri = profileUri;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Uri getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(Uri profileUri) {
        this.profileUri = profileUri;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
