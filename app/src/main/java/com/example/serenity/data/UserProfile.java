package com.example.serenity.data;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {
    String id;
    private String name;
    private String email;

    public UserProfile(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }


    public Map<String, Object> toFirebaseObject() {
        HashMap<String, Object> event = new HashMap<>();
        event.put("username", name);
        event.put("email", email);

        return event;
    }

}
