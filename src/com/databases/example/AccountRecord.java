package com.databases.example;

//An Object Class used to hold the data of each account record
public class AccountRecord {
	protected String id;
	protected String name;
	protected String balance;
	protected String date;
	protected String time;

	public AccountRecord(String id, String name, String balance, String date, String time) {
		this.id = id;
		this.name = name;
		this.balance = balance;
		this.date = date;
		this.time = time;
	}
}
