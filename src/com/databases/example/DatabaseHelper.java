package com.databases.example;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


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

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_PH_NO = "phone_number";

	private static Context context = null;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
        
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.e("DatabaseHelper-onCreate", "Creating database...");
		
		String sqlCommandAccounts = "CREATE TABLE "
				+ TABLE_ACCOUNTS
				+ " (AcctID INTEGER PRIMARY KEY, AcctName VARCHAR, AcctBalance VARCHAR, AcctTime VARCHAR, AcctDate VARCHAR);";

		String sqlCommandTransactions = "CREATE TABLE "
				+ TABLE_TRANSACTIONS
				+ " (TransID INTEGER PRIMARY KEY, ToAcctID VARCHAR, ToPlanID VARCHAR, TransName VARCHAR, TransValue VARCHAR, TransType VARCHAR, TransCategory VARCHAR, TransCheckNum VARCHAR, TransMemo VARCHAR, TransTime VARCHAR, TransDate VARCHAR, TransCleared);";

		String sqlCommandPlannedTransactions = "CREATE TABLE "
				+ TABLE_PLANNED_TRANSACTIONS
				+ " (PlanID INTEGER PRIMARY KEY, ToAcctID VARCHAR, PlanName VARCHAR, PlanValue VARCHAR, PlanType VARCHAR, PlanCategory VARCHAR, PlanMemo VARCHAR, PlanOffset VARCHAR, PlanRate VARCHAR, PlanCleared VARCHAR);";

		String sqlCommandCategory = "CREATE TABLE "
				+ TABLE_CATEGORIES
				+ " (CatID INTEGER PRIMARY KEY, CatName VARCHAR, CatNote VARCHAR);";

		String sqlCommandSubCategory = "CREATE TABLE "
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
		Log.d("DatabaseHelper-onCreate", "Adding Default Categories...");
		
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

	

}
