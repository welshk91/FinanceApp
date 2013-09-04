package com.databases.example;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{

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

	private Context context = null;

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

		addDefaultCategories(db);
	}

	public int deleteDatabase(){
		Log.d("DatabaseHelper-deleteDatabase","Deleting database...");

		try{			
			context.deleteDatabase(DATABASE_NAME);
			return 1;
		}
		catch(Exception e){
			Log.e("DatabaseHelper-deleteDatabase", "Couldn't delete database. Error e="+e);
		}

		return 0;
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
				+ " (CatName) " + "VALUES ('Default');";
		final String sqlDefaultCategories2 = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Utils');";
		final String sqlDefaultSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Gas',2);";
		final String sqlDefaultSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Electricty',2);";
		final String sqlDefaultSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Heat',2);";
		final String sqlDefaultSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Water',2);";
		final String sqlDefaultSubCategories5 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('AC',2);";
		final String sqlDefaultSubCategories6 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('STARTING BALANCE',1);";
		final String sqlDefaultSubCategories7 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('TRANSFER',1);";

		db.execSQL(sqlDefaultCategories);
		db.execSQL(sqlDefaultCategories2);
		db.execSQL(sqlDefaultSubCategories);
		db.execSQL(sqlDefaultSubCategories2);
		db.execSQL(sqlDefaultSubCategories3);
		db.execSQL(sqlDefaultSubCategories4);
		db.execSQL(sqlDefaultSubCategories5);
		db.execSQL(sqlDefaultSubCategories6);
		db.execSQL(sqlDefaultSubCategories7);
	}

	//Updates balance of an account
	public void setBalance(String aID, float balance){
		SQLiteDatabase db = this.getWritableDatabase();
		String sqlCommand = "UPDATE " + TABLE_ACCOUNTS + " SET AcctBalance = " + balance + " WHERE AcctID = " + aID+ ";";
		db.execSQL(sqlCommand);
	}

	//Returns Database file
	public File getDatabase(){
		File currentDB = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
		//Log.d("DatabaseHelper-getDatabase", "currentDB="+currentDB.getAbsolutePath());
		return currentDB;
	}

	//Sum up all the account balances
	public Cursor sumAccounts(){
		SQLiteDatabase db = this.getReadableDatabase();
		String sqlCommand = "SELECT SUM(AcctBalance) FROM " + TABLE_ACCOUNTS;
		Cursor cursor = db.rawQuery(sqlCommand, null);
		return cursor;
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
	public int deleteAccount(Uri uri, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = 0;
		rowsDeleted = db.delete(TABLE_ACCOUNTS, whereClause, whereArgs);		
		return rowsDeleted;
	}

	//Add account
	public long addAccount(ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_ACCOUNTS, null, values);
		return id; 
	}

	//Updates an account
	public int updateAccount(ContentValues values, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		db.update(TABLE_ACCOUNTS, values, whereClause, whereArgs);
		return 0;
	}

	//Get all transactions for an account
	public Cursor getTransactions(String[] projection, String selection, String[] selectionArgs, String sortOrder){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_TRANSACTIONS, new String[] { "TransID as _id", "ToAcctID", "ToPlanID", "TransName", "TransValue", "TransType", "TransCategory","TransCheckNum", "TransMemo", "TransTime", "TransDate", "TransCleared"}, selection,
				selectionArgs, null, null, sortOrder);
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
	public int deleteTransaction(Uri uri, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = 0;
		rowsDeleted = db.delete(TABLE_TRANSACTIONS, whereClause, whereArgs);		
		return rowsDeleted;
	}

	//Add transaction
	public long addTransaction(ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_TRANSACTIONS, null, values);
		return id; 
	}

	//Get all categories
	public Cursor getCategories(String[] projection, String selection, String[] selectionArgs, String sortOrder){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_CATEGORIES, new String[] { "CatID as _id", "CatName", "CatNote" }, selection,
				selectionArgs, null, null, sortOrder);
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

	//Add category
	public long addCategory(ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_CATEGORIES, null, values);
		return id; 
	}

	//Delete category (and relating subcategories if specified)
	public int deleteCategory(Uri uri, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = 0;
		rowsDeleted = db.delete(TABLE_CATEGORIES, whereClause, whereArgs);		
		return rowsDeleted;
	}

	//Get subcategories for a category
	public Cursor getSubCategories(String[] projection, String selection, String[] selectionArgs, String sortOrder){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_SUBCATEGORIES, new String[] { "SubCatID as _id", "ToCatID", "SubCatName", "SubCatNote"}, selection,
				selectionArgs, null, null, sortOrder);
		return cursor;
	}

	//Get single subcategory
	public Cursor getSubCategory(String sID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		String sqlCommand = "SELECT * FROM " + TABLE_SUBCATEGORIES + 
				" WHERE SubCatID = " + sID;

		cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

	//Add subcategory (ID given)
	public long addSubCategory(ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_SUBCATEGORIES, null, values);
		return id; 	
	}

	//Delete subcategory
	public int deleteSubCategory(Uri uri, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = 0;
		rowsDeleted = db.delete(TABLE_SUBCATEGORIES, whereClause, whereArgs);		
		return rowsDeleted;
	}

	//Get all planned transactions for all accounts
	public Cursor getPlannedTransactionsAll(){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_PLANNED_TRANSACTIONS, new String[] { "PlanID as _id", "ToAcctID", "PlanName", "PlanValue", "PlanType", "PlanCategory", "PlanMemo", "PlanOffset", "PlanRate", "PlanCleared"}, null,
				null, null, null, null);
		return cursor;
	}

	//Get single planned transaction
	public Cursor getPlannedTransaction(String pID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		String sqlCommand = "SELECT * FROM " + TABLE_PLANNED_TRANSACTIONS + 
				" WHERE PlanID = " + pID;

		cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

	//Add planned transaction (no ID)
	public long addPlannedTransaction(ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_PLANNED_TRANSACTIONS, null, values);
		return id;
	}

	//Delete planned transaction
	public int deletePlannedTransaction(Uri uri, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = 0;
		rowsDeleted = db.delete(TABLE_PLANNED_TRANSACTIONS, whereClause, whereArgs);		
		return rowsDeleted;
	}

}