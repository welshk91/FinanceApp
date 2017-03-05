package com.databases.example.model;

//An Object Class used to hold the data of each category record
public class Category {
    public final String id;
    public final String name;
    public final String note;

    public Category(String id, String name, String note) {
        this.id = id;
        this.name = name;
        this.note = note;
    }

}
