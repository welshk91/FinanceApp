package com.databases.example;

//An Object Class used to hold the data of each account record
public class AccountRecord {
	public String id;
	public String name;
	public String balance;
	public String date;
	public String time;

	public AccountRecord(String id, String name, String balance, String date, String time) {
		this.id = id;
		this.name = name;
		this.balance = balance;
		this.date = date;
		this.time = time;
	}
}
