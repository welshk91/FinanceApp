package com.databases.example.data;

//An Object Class used to hold the data of each sub-category record
public class SubCategoryRecord {
    public final String id;
    public final String catId;
    public final String name;
    public final String note;

    public SubCategoryRecord(String id, String catId, String name, String note) {
        this.id = id;
        this.catId = catId;
        this.name = name;
        this.note = note;
    }
}
