package com.example.chatapplication;

import android.net.Uri;

public class UserDataModel {
    private String name;
    private String email;
    private String bio;
    private String dob;
    private String gender;
    private Uri profileUri;

    public UserDataModel(String name, String email, String bio, String dob, String gender, Uri profileUri) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.dob = dob;
        this.gender = gender;
        this.profileUri = profileUri;
    }

    public UserDataModel(String userName, String userMail, Uri imageLink) {
        name = userName;
        email = userMail;
        profileUri = imageLink;
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
}
