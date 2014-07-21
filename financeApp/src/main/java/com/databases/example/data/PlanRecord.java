package com.databases.example.data;

//An Object Class used to hold the data of each transaction record
public class PlanRecord {
    public String id;
    public String acctId;
    public String name;
    public String value;
    public String type;
    public String category;
    public String memo;
    public String offset;
    public String rate;
    public String next;
    public String scheduled;
    public String cleared;

    public PlanRecord(String id, String acctId, String name, String value, String type, String category, String memo, String offset, String rate, String next, String scheduled, String cleared) {
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
