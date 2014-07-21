package com.databases.example.data;

//An Object Class used to hold the data of each category record
public class CategoryRecord {
    public String id;
    public String name;
    public String note;

    public CategoryRecord(String id, String name, String note) {
        this.id = id;
        this.name = name;
        this.note = note;
    }

}
