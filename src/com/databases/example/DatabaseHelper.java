package com.databases.example;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class DatabaseHelper extends SQLiteOpenHelper{

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "dbFinance";

	// Contacts table name
	private static final String TABLE_ACCOUNTS = "tblAccounts";
	private static final String TABLE_TRANSACTIONS = "tblTrans";
	private static final String TABLE_PLANNED_TRANSACTIONS = "tblPlanTrans";
	private static final String TABLE_CATEGORIES = "tblCategory";
	private static final String TABLE_SUBCATEGORIES = "tblSubCategory";
	private static final String TABLE_LINKS = "tblLinks";

	private static Context context = null;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.e("DatabaseHelper-onCreate", "Creating database...");

		String sqlCommandAccounts = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_ACCOUNTS
				+ " (AcctID INTEGER PRIMARY KEY, AcctName VARCHAR, AcctBalance VARCHAR, AcctTime VARCHAR, AcctDate VARCHAR);";

		String sqlCommandTransactions = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_TRANSACTIONS
				+ " (TransID INTEGER PRIMARY KEY, ToAcctID VARCHAR, ToPlanID VARCHAR, TransName VARCHAR, TransValue VARCHAR, TransType VARCHAR, TransCategory VARCHAR, TransCheckNum VARCHAR, TransMemo VARCHAR, TransTime VARCHAR, TransDate VARCHAR, TransCleared);";

		String sqlCommandPlannedTransactions = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_PLANNED_TRANSACTIONS
				+ " (PlanID INTEGER PRIMARY KEY, ToAcctID VARCHAR, PlanName VARCHAR, PlanValue VARCHAR, PlanType VARCHAR, PlanCategory VARCHAR, PlanMemo VARCHAR, PlanOffset VARCHAR, PlanRate VARCHAR, PlanCleared VARCHAR);";

		String sqlCommandCategory = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_CATEGORIES
				+ " (CatID INTEGER PRIMARY KEY, CatName VARCHAR, CatNote VARCHAR);";

		String sqlCommandSubCategory = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_SUBCATEGORIES
				+ " (SubCatID INTEGER PRIMARY KEY, ToCatID VARCHAR, SubCatName VARCHAR, SubCatNote VARCHAR);";

		String sqlCommandLinks = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_LINKS
				+ " (LinkID INTEGER PRIMARY KEY, ToID VARCHAR, LinkName VARCHAR, LinkMemo VARCHAR, ParentType VARCHAR);";

		db.execSQL(sqlCommandAccounts);
		db.execSQL(sqlCommandTransactions);
		db.execSQL(sqlCommandPlannedTransactions);
		db.execSQL(sqlCommandCategory);
		db.execSQL(sqlCommandSubCategory);
		db.execSQL(sqlCommandLinks);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DatabaseHelper-onUpgrade", "Upgrading database from " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANNED_TRANSACTIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBCATEGORIES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINKS);
		onCreate(db);
	}

	//Adds some basic default categories
	public void addDefaultCategories(SQLiteDatabase db){
		Log.e("DatabaseHelper-onCreate", "Adding Default Categories...");

		final String sqlDefaultCategories = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('STARTING BALANCE');";
		final String sqlDefaultCategories2 = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Utils');";
		final String sqlDefaultSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Gas',2);";
		final String sqlDefaultSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Electricty',2);";

		db.execSQL(sqlDefaultCategories);
		db.execSQL(sqlDefaultCategories2);
		db.execSQL(sqlDefaultSubCategories);
		db.execSQL(sqlDefaultSubCategories2);

	}

	//Updates balance of an account
	public void setBalance(String aID, float balance){
		SQLiteDatabase db = this.getWritableDatabase();
		String sqlCommand = "UPDATE " + TABLE_ACCOUNTS + " SET AcctBalance = " + balance + " WHERE AcctID = " + aID+ ";";
		db.execSQL(sqlCommand);
		db.close();
	}
	
	//Get all accounts
	public Cursor getAccounts(){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_ACCOUNTS, new String[] { "AcctID as _id", "AcctName", "AcctBalance", "AcctTime", "AcctDate" }, null,
				null, null, null, null);
		return cursor;
	}

	//Get single account
	public Cursor getAccount(String aID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		String sqlCommand = "SELECT * FROM " + TABLE_ACCOUNTS + 
				" WHERE AcctID = " + aID;

		cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

	//Get searched accounts
	public Cursor getSearchedAccounts(String query){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		//Command used to search
		String sqlCommand = " SELECT AcctID as _id, * FROM " + TABLE_ACCOUNTS + 
				" WHERE AcctName " + 
				" LIKE ?" + 
				" UNION " + 
				" SELECT AcctID as _id, * FROM " + TABLE_ACCOUNTS +
				" WHERE AcctBalance " + 
				" LIKE ?" + 
				" UNION " + 
				" SELECT AcctID as _id, * FROM " + TABLE_ACCOUNTS +
				" WHERE AcctDate " + 
				" LIKE ?" +
				" UNION " +
				" SELECT AcctID as _id, * FROM " + TABLE_ACCOUNTS +
				" WHERE AcctTime " + 
				" LIKE ?";

		cursor = db.rawQuery(sqlCommand, new String[] { "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%" });
		return cursor;
	}

	//Delete account (and relating transactions if specified)
	public void deleteAccount(String aID, boolean keepTransactions){
		SQLiteDatabase db = this.getWritableDatabase();

		String sqlDeleteAccount = "DELETE FROM " + TABLE_ACCOUNTS + 
				" WHERE AcctID = " + aID;
		db.execSQL(sqlDeleteAccount);

		if(keepTransactions=false){
			String sqlDeleteTransactions = "DELETE FROM " + TABLE_TRANSACTIONS + 
					" WHERE ToAcctID = " + aID;
			db.execSQL(sqlDeleteTransactions);	
		}

		db.close();
	}

	//Add account (no ID)
	public long addAccount(String name, String balance,String time,String date){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues accountValues=new ContentValues();
		accountValues.put("AcctName",name);
		accountValues.put("AcctBalance",balance);
		accountValues.put("AcctTime",time);
		accountValues.put("AcctDate",date);

		long id = db.insert(TABLE_ACCOUNTS, null, accountValues);
		db.close();
		return id; 
	}

	//Add account (ID given)
	public long addAccount(String aID, String name, String balance,String time,String date){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues accountValues=new ContentValues();
		accountValues.put("AcctID",aID);
		accountValues.put("AcctName",name);
		accountValues.put("AcctBalance",balance);
		accountValues.put("AcctTime",time);
		accountValues.put("AcctDate",date);

		long id = db.insert(TABLE_ACCOUNTS, null, accountValues);
		db.close();
		return id; 
	}

	//Get all transactions for an account
	public Cursor getTransactions(String aID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_TRANSACTIONS, new String[] { "TransID as _id", "ToAcctID", "ToPlanID", "TransName", "TransValue", "TransType", "TransCategory","TransCheckNum", "TransMemo", "TransTime", "TransDate", "TransCleared"}, "ToAcctID = " + aID,
				null, null, null, null);
		return cursor;
	}

	//Get all transactions
	public Cursor getTransactionsAll(){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_TRANSACTIONS, new String[] { "TransID as _id", "ToAcctID", "ToPlanID", "TransName", "TransValue", "TransType", "TransCategory","TransCheckNum", "TransMemo", "TransTime", "TransDate", "TransCleared"}, null,
				null, null, null, null);
		return cursor;
	}

	//Get single transaction
	public Cursor getTransaction(String tID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		String sqlCommand = "SELECT * FROM " + TABLE_TRANSACTIONS + 
				" WHERE TransID = " + tID;

		cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

	//Get searched transactions
	public Cursor getSearchedTransactions(String query){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		//Command used to search
		String sqlCommand = " SELECT TransID as _id, * FROM " + TABLE_TRANSACTIONS + 
				" WHERE TransName " + 
				" LIKE ?" +
				" UNION " +
				" SELECT TransID as _id, * FROM " + TABLE_TRANSACTIONS +
				" WHERE TransValue " + 
				" LIKE ?" +
				" UNION " +
				" SELECT TransID as _id, * FROM " + TABLE_TRANSACTIONS +
				" WHERE TransCategory " + 
				" LIKE ?" +
				" UNION " +
				" SELECT TransID as _id, * FROM " + TABLE_TRANSACTIONS +
				" WHERE TransDate " + 
				" LIKE ?" +
				" UNION " +				
				" SELECT TransID as _id, * FROM " + TABLE_TRANSACTIONS +
				" WHERE TransTime " + 
				" LIKE ?" +
				" UNION " +
				" SELECT TransID as _id, * FROM " + TABLE_TRANSACTIONS +
				" WHERE TransMemo " + 
				" LIKE ?" +
				" UNION " +
				" SELECT TransID as _id, * FROM " + TABLE_TRANSACTIONS +
				" WHERE TransCheckNum " + 
				" LIKE ?";

		cursor = db.rawQuery(sqlCommand, new String[] { "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%" });
		return cursor;
	}

	//Delete transaction (and relating transactions if specified)
	public void deleteTransaction(String id){
		SQLiteDatabase db = this.getWritableDatabase();

		String sqlDeleteTransaction = "DELETE FROM " + TABLE_TRANSACTIONS + 
				" WHERE TransID = " + id;
		db.execSQL(sqlDeleteTransaction);

	}

	//Add transaction (no ID)
	public long addTransaction(String a_id, String p_id, String name,String value,String type, String category, String checknum, String memo, String time,String date, String cleared){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues transactionValues=new ContentValues();
		transactionValues.put("ToAcctID",a_id);
		transactionValues.put("ToPlanID",p_id);
		transactionValues.put("TransName",name);
		transactionValues.put("TransValue",value);
		transactionValues.put("TransType",type);
		transactionValues.put("TransCategory",category);
		transactionValues.put("TransCheckNum",checknum);
		transactionValues.put("TransMemo",memo);
		transactionValues.put("TransTime",time);
		transactionValues.put("TransDate",date);
		transactionValues.put("TransCleared",cleared);

		long id = db.insert(TABLE_TRANSACTIONS, null, transactionValues);

		return id; 
	}

	//Add transaction (ID given)
	public long addTransaction(String t_id, String a_id, String p_id, String name,String value,String type, String category, String checknum, String memo, String time,String date, String cleared){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues transactionValues=new ContentValues();
		transactionValues.put("TransID",t_id);
		transactionValues.put("ToAcctID",a_id);
		transactionValues.put("ToPlanID",p_id);
		transactionValues.put("TransName",name);
		transactionValues.put("TransValue",value);
		transactionValues.put("TransType",type);
		transactionValues.put("TransCategory",category);
		transactionValues.put("TransCheckNum",checknum);
		transactionValues.put("TransMemo",memo);
		transactionValues.put("TransTime",time);
		transactionValues.put("TransDate",date);
		transactionValues.put("TransCleared",cleared);

		long id = db.insert(TABLE_TRANSACTIONS, null, transactionValues);

		return id; 	

	}

	//Get all categories
	public Cursor getCategories(){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_CATEGORIES, new String[] { "CatID as _id", "CatName", "CatNote" }, null,
				null, null, null, null);
		return cursor;
	}

	//Get single category
	public Cursor getCategory(String cID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		String sqlCommand = "SELECT * FROM " + TABLE_CATEGORIES + 
				" WHERE CatID = " + cID;

		cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

	//Get all categories
	public Cursor getSubCategories(){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_SUBCATEGORIES, new String[] { "SubCatID as _id", "ToCatID", "SubCatName", "SubCatNote" }, null,
				null, null, null, null);
		return cursor;
	}

	//Get single category
	public Cursor getSubCategory(String sID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		String sqlCommand = "SELECT * FROM " + TABLE_SUBCATEGORIES + 
				" WHERE SubCatID = " + sID;

		cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

}