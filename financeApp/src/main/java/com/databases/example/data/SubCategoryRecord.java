package com.databases.example.data;

//An Object Class used to hold the data of each sub-category record
public class SubCategoryRecord {
    public String id;
    public String catId;
    public String name;
    public String note;

    public SubCategoryRecord(String id, String catId, String name, String note) {
        this.id = id;
        this.catId = catId;
        this.name = name;
        this.note = note;
    }
}
