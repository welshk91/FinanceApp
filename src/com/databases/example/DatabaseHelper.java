/* Class that handles database operations
 * Does everything from simple insert/update/deleting of information to
 * more complex database operations like balances
 */

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
	private static final String TABLE_PLANS = "tblPlanTrans";
	private static final String TABLE_CATEGORIES = "tblCategory";
	private static final String TABLE_SUBCATEGORIES = "tblSubCategory";
	private static final String TABLE_LINKS = "tblLinks";
	private static final String TABLE_NOTIFICATIONS = "tblNotifications";
	
	private Context context = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("DatabaseHelper-onCreate", "Creating database...");

		String sqlCommandAccounts = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_ACCOUNTS
				+ " (AcctID INTEGER PRIMARY KEY, AcctName VARCHAR, AcctBalance VARCHAR, AcctTime VARCHAR, AcctDate VARCHAR);";

		String sqlCommandTransactions = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_TRANSACTIONS
				+ " (TransID INTEGER PRIMARY KEY, ToAcctID VARCHAR, ToPlanID VARCHAR, TransName VARCHAR, TransValue VARCHAR, TransType VARCHAR, TransCategory VARCHAR, TransCheckNum VARCHAR, TransMemo VARCHAR, TransTime VARCHAR, TransDate VARCHAR, TransCleared VARCHAR);";

		String sqlCommandPlannedTransactions = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_PLANS
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

		String sqlCommandNotifications = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NOTIFICATIONS
				+ " (NotificationID INTEGER PRIMARY KEY, NotificationName VARCHAR, NotificationValue VARCHAR, NotificationDate VARCHAR);";

		db.execSQL(sqlCommandAccounts);
		db.execSQL(sqlCommandTransactions);
		db.execSQL(sqlCommandPlannedTransactions);
		db.execSQL(sqlCommandCategory);
		db.execSQL(sqlCommandSubCategory);
		db.execSQL(sqlCommandLinks);
		db.execSQL(sqlCommandNotifications);

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
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBCATEGORIES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINKS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
		onCreate(db);
	}

	//Adds some basic default categories
	public void addDefaultCategories(SQLiteDatabase db){
		Log.d("DatabaseHelper-onCreate", "Adding Default Categories...");

		//Default
		final String sqlDefaultCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Default');";
		final String sqlDefaultSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('STARTING BALANCE',1);";
		final String sqlDefaultSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('TRANSFER',1);";
		 
		//ATM
		final String sqlATMCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('ATM');";
		final String sqlATMSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Deposit',2);";
		final String sqlATMSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Withdraw',2);";
		
		//Car
		final String sqlCarCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Car');";
		final String sqlCarSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Road Services',3);";
		final String sqlCarSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Fuel',3);";
		final String sqlCarSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Lease',3);";
		final String sqlCarSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Maitenance',3);";
		
		//Food
		final String sqlFoodCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Food');";
		final String sqlFoodSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Groceries',4);";
		final String sqlFoodSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Restaurant',4);";
		final String sqlFoodSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Snacks',4);";
		
		//Fun
		final String sqlFunCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Fun');";
		final String sqlFunSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Entertainment',5);";
		final String sqlFunSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Electronics',5);";
		final String sqlFunSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Shopping',5);";
		
		//Housing
		final String sqlHouseCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('House');";
		final String sqlHouseSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Rent',6);";
		final String sqlHouseSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Maintenance',6);";
		final String sqlHouseSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Decorating',6);";				
		
		//Insurance
		final String sqlInsuranceCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Insurance');";
		final String sqlInsuranceSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Auto',7);";
		final String sqlInsuranceSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Health',7);";
		final String sqlInsuranceSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Home',7);";
		final String sqlInsuranceSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Life',7);";
		
		//Job
		final String sqlJobCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Job');";
		final String sqlJobSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Paycheck',8);";
		final String sqlJobSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Tax',8);";
		final String sqlJobSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Income',8);";
		
		//Loans 
		final String sqlLoansCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Loans');";
		final String sqlLoansSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Auto',9);";
		final String sqlLoansSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Home Equity',9);";
		final String sqlLoansSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Mortgage',9);";
		final String sqlLoansSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Student',9);";
		
		//Personal
		final String sqlPersonalCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Personal');";
		final String sqlPersonalSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Gift',10);";
		final String sqlPersonalSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Donation',10);";
		
		//Random
		final String sqlRandomCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Random');";
		final String sqlRandomSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Interest',11);";
		final String sqlRandomSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Tip',11);";
		
		//Travel
		final String sqlTravelCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Travel');";
		final String sqlTravelSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Airplane',12);";
		final String sqlTravelSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Car Rental',12);";
		final String sqlTravelSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Dining',12);";
		final String sqlTravelSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Hotel',12);";
		final String sqlTravelSubCategories5 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Misc Expenses',12);";
		final String sqlTravelSubCategories6 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Taxi',12);";

		//Utilities
		final String sqlUtilitiesCategory = "INSERT INTO " + TABLE_CATEGORIES
				+ " (CatName) " + "VALUES ('Utils');";
		final String sqlUtilitiesSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Gas',13);";
		final String sqlUtilitiesSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Electricty',13);";
		final String sqlUtilitiesSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Heat',13);";
		final String sqlUtilitiesSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Water',13);";
		final String sqlUtilitiesSubCategories5 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('AC',13);";
		final String sqlUtilitiesSubCategories6 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Cable',13);";
		final String sqlUtilitiesSubCategories7 = "INSERT INTO " + TABLE_SUBCATEGORIES
				+ " (SubCatName, ToCatID) " + "VALUES ('Internet',13);";

		
		db.execSQL(sqlDefaultCategory);
		db.execSQL(sqlDefaultSubCategories);
		db.execSQL(sqlDefaultSubCategories2);
			
		db.execSQL(sqlATMCategory);
		db.execSQL(sqlATMSubCategories);
		db.execSQL(sqlATMSubCategories2);

		db.execSQL(sqlCarCategory);
		db.execSQL(sqlCarSubCategories);
		db.execSQL(sqlCarSubCategories2);
		db.execSQL(sqlCarSubCategories3);
		db.execSQL(sqlCarSubCategories4);

		db.execSQL(sqlFoodCategory);
		db.execSQL(sqlFoodSubCategories);
		db.execSQL(sqlFoodSubCategories2);
		db.execSQL(sqlFoodSubCategories3);

		db.execSQL(sqlFunCategory);
		db.execSQL(sqlFunSubCategories);
		db.execSQL(sqlFunSubCategories2);
		db.execSQL(sqlFunSubCategories3);

		db.execSQL(sqlHouseCategory);
		db.execSQL(sqlHouseSubCategories);
		db.execSQL(sqlHouseSubCategories2);
		db.execSQL(sqlHouseSubCategories3);

		db.execSQL(sqlInsuranceCategory);
		db.execSQL(sqlInsuranceSubCategories);
		db.execSQL(sqlInsuranceSubCategories2);
		db.execSQL(sqlInsuranceSubCategories3);
		db.execSQL(sqlInsuranceSubCategories4);

		db.execSQL(sqlJobCategory);
		db.execSQL(sqlJobSubCategories);
		db.execSQL(sqlJobSubCategories2);
		db.execSQL(sqlJobSubCategories3);

		db.execSQL(sqlLoansCategory);
		db.execSQL(sqlLoansSubCategories);
		db.execSQL(sqlLoansSubCategories2);
		db.execSQL(sqlLoansSubCategories3);
		db.execSQL(sqlLoansSubCategories4);

		db.execSQL(sqlPersonalCategory);
		db.execSQL(sqlPersonalSubCategories);
		db.execSQL(sqlPersonalSubCategories2);

		db.execSQL(sqlRandomCategory);
		db.execSQL(sqlRandomSubCategories);
		db.execSQL(sqlRandomSubCategories2);

		db.execSQL(sqlTravelCategory);
		db.execSQL(sqlTravelSubCategories);
		db.execSQL(sqlTravelSubCategories2);
		db.execSQL(sqlTravelSubCategories3);
		db.execSQL(sqlTravelSubCategories4);
		db.execSQL(sqlTravelSubCategories5);
		db.execSQL(sqlTravelSubCategories6);
	
		db.execSQL(sqlUtilitiesCategory);
		db.execSQL(sqlUtilitiesSubCategories);
		db.execSQL(sqlUtilitiesSubCategories2);
		db.execSQL(sqlUtilitiesSubCategories3);
		db.execSQL(sqlUtilitiesSubCategories4);
		db.execSQL(sqlUtilitiesSubCategories5);
		db.execSQL(sqlUtilitiesSubCategories6);
		db.execSQL(sqlUtilitiesSubCategories7);
		
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
	public Cursor getAccounts(String[] projection, String selection, String[] selectionArgs, String sortOrder){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_ACCOUNTS, new String[] { "AcctID as _id", "AcctName", "AcctBalance", "AcctTime", "AcctDate" }, selection,
				selectionArgs, null, null, sortOrder);
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

	//Sum up all the positive transactions 
	public Cursor sumDeposits(int AcctID){
		SQLiteDatabase db = this.getReadableDatabase();
		String sqlCommand;

		if(AcctID==0){
			sqlCommand = "SELECT SUM(TransValue) FROM " + TABLE_TRANSACTIONS + " WHERE TransType='Deposit'";
		}
		else{
			sqlCommand = "SELECT SUM(TransValue) FROM " + TABLE_TRANSACTIONS + " WHERE ToAcctID="+AcctID + " AND TransType='Deposit'";			
		}
		Cursor cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

	//Sum up all the negative transactions 
	public Cursor sumWithdraws(int AcctID){
		SQLiteDatabase db = this.getReadableDatabase();
		String sqlCommand;

		if(AcctID==0){
			sqlCommand = "SELECT SUM(TransValue) FROM " + TABLE_TRANSACTIONS + " WHERE TransType='Withdraw'";
		}
		else{
			sqlCommand = "SELECT SUM(TransValue) FROM " + TABLE_TRANSACTIONS + " WHERE ToAcctID="+AcctID + " AND TransType='Withdraw'";			
		}
		Cursor cursor = db.rawQuery(sqlCommand, null);
		return cursor;
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
	
	//Updates a category
	public int updateCategory(ContentValues values, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		db.update(TABLE_CATEGORIES, values, whereClause, whereArgs);
		return 0;
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
	
	//Updates a subcategory
	public int updateSubCategory(ContentValues values, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		db.update(TABLE_SUBCATEGORIES, values, whereClause, whereArgs);
		return 0;
	}

	//Get all planned transactions for all accounts
	public Cursor getPlans(String[] projection, String selection, String[] selectionArgs, String sortOrder){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_PLANS, new String[] { "PlanID as _id", "ToAcctID", "PlanName", "PlanValue", "PlanType", "PlanCategory", "PlanMemo", "PlanOffset", "PlanRate", "PlanCleared"}, selection,
				selectionArgs, null, null, sortOrder);
		return cursor;
	}

	//Get single planned transaction
	public Cursor getPlan(String pID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		String sqlCommand = "SELECT * FROM " + TABLE_PLANS + 
				" WHERE PlanID = " + pID;

		cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

	//Add planned transaction (no ID)
	public long addPlan(ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_PLANS, null, values);
		return id;
	}

	//Delete planned transaction
	public int deletePlan(Uri uri, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = 0;
		rowsDeleted = db.delete(TABLE_PLANS, whereClause, whereArgs);		
		return rowsDeleted;
	}
	
	//Updates a plan
	public int updatePlan(ContentValues values, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		db.update(TABLE_PLANS, values, whereClause, whereArgs);
		return 0;
	}

	//Get all notifications
	public Cursor getNotifications(String[] projection, String selection, String[] selectionArgs, String sortOrder){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_NOTIFICATIONS, new String[] { "NotificationID as _id", "NotificationName", "NotificationValue", "NotificationDate"}, selection,
				selectionArgs, null, null, sortOrder);
		return cursor;
	}

	//Get single notification
	public Cursor getNotification(String nID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();

		String sqlCommand = "SELECT * FROM " + TABLE_NOTIFICATIONS + 
				" WHERE NotificationID = " + nID;

		cursor = db.rawQuery(sqlCommand, null);
		return cursor;
	}

	//Add planned transaction (no ID)
	public long addNotification(ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_NOTIFICATIONS, null, values);
		return id;
	}

	//Delete planned transaction
	public int deleteNotification(Uri uri, String whereClause, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsDeleted = 0;
		rowsDeleted = db.delete(TABLE_NOTIFICATIONS, whereClause, whereArgs);		
		return rowsDeleted;
	}
		
}//End DatabaseHelper