package com.example.serenity.data;

import java.util.ArrayList;
import java.util.HashMap;

public class TodoListModel {

    public enum STATE {
        CLOSED,
        OPENED
    }



    String id;
    String name;
    String message;
    int level;
    STATE state = STATE.CLOSED;
    ArrayList<TodoListModel> models = new ArrayList<>();

    public TodoListModel(String id, String name, String message, int level) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<TodoListModel> getModels() {
        return models;
    }

    public int getLevel() {
        return level;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public HashMap<String, String> toFireBaseObject() {
        HashMap<String, String> todo = new HashMap<>();
        todo.put("Task", name);
        todo.put("Message", message);
        //todo.put("Level", level);
        return todo;
    }
}