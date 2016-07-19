package com.example.myawesomechat;

public class Message {

    public static final String CHILD_NAME = "messages";

    private String text;
    private String name;
    private String photoUrl;

    public Message() {
        //重要！空的 constructor 為 Firebase Realtime Database 必須要有的。
    }

    public Message(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}