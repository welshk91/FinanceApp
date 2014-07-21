package com.databases.example.data;

//An Object Class used to hold the data of each transaction record
public class TransactionRecord {

    public int id;
    public int acctId;
    public int planId;
    public String name;
    public String value;
    public String type;
    public String category;
    public String checknum;
    public String memo;
    public String time;
    public String date;
    public String cleared;

    public TransactionRecord(int id, int acctId, int planId, String name, String value, String type, String category, String checknum, String memo, String time, String date, String cleared) {
        this.id = id;
        this.acctId = acctId;
        this.planId = planId;
        this.name = name;
        this.value = value;
        this.type = type;
        this.category = category;
        this.checknum = checknum;
        this.memo = memo;
        this.time = time;
        this.date = date;
        this.cleared = cleared;
    }
}
