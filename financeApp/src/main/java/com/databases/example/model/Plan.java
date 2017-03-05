package com.databases.example.model;

//An Object Class used to hold the data of each transaction record
public class Plan {
    public final String id;
    public final String acctId;
    public final String name;
    public final String value;
    public final String type;
    public final String category;
    public final String memo;
    public final String offset;
    public final String rate;
    public final String next;
    public final String scheduled;
    public final String cleared;

    public Plan(String id, String acctId, String name, String value, String type, String category, String memo, String offset, String rate, String next, String scheduled, String cleared) {
        this.id = id;
        this.acctId = acctId;
        this.name = name;
        this.value = value;
        this.type = type;
        this.category = category;
        this.memo = memo;
        this.offset = offset;
        this.rate = rate;
        this.next = next;
        this.scheduled = scheduled;
        this.cleared = cleared;
    }


}
