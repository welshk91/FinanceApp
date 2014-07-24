package com.databases.example.data;

//An Object Class used to hold the data of each account record
public class AccountRecord {

    public final String id;
    public final String name;
    public final String balance;
    public final String date;
    public final String time;

    public AccountRecord(String id, String name, String balance, String date, String time) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.date = date;
        this.time = time;
    }

}
