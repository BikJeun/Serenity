package com.example.serenity;

import java.util.ArrayList;
import java.util.HashMap;

public class TodoListModel {

    public enum STATE {
        CLOSED,
        OPENED
    }

    String name;
    String message;
    int level;
    STATE state = STATE.CLOSED;
    ArrayList<TodoListModel> models = new ArrayList<>();

    public TodoListModel(String name, String message, int level) {
        this.name = name;
        this.message = message;
        this.level = level;
    }

    public HashMap<String, String> toFireBaseObject() {
        HashMap<String, String> todo = new HashMap<>();
        todo.put("Task", name);
        todo.put("Message", message);
        //todo.put("Level", level);
        return todo;
    }
}