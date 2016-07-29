package com.example.myawesomechat;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

public class User {

    public static final String CHILD_NAME = "users";

    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String email, String displayName, String photoUrl) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public static User fromFirebaseUser(@NonNull FirebaseUser firebaseUser) {
        Uri photoUrl = firebaseUser.getPhotoUrl();
        return new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                firebaseUser.getDisplayName(),
                null == photoUrl ? null : photoUrl.toString()
        );
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
