package com.databases.example;

//An Object Class used to hold the data of each transaction record
public class TransactionRecord {
	protected int id;
	protected int acctId;
	protected String name;
	protected String value;
	protected String type;
	protected String category;
	protected String checknum;
	protected String memo;
	protected String time;
	protected String date;
	protected String cleared;

	public TransactionRecord(int id, int acctId, String name, String value, String type, String category, String checknum, String memo, String time, String date, String cleared) {
		this.id = id;
		this.acctId = acctId;
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