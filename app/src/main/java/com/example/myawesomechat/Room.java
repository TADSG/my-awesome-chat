package com.example.myawesomechat;

import java.util.ArrayList;
import java.util.List;

public class Room {

    public static final String CHILD_NAME = "rooms";

    private List<Message> messages = new ArrayList<>();

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessages() {
        this.messages = messages;
    }
}
