package com.databases.example;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
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
	public int deleteAccount(Uri uri,String selection, String[] selectionArgs){
		SQLiteDatabase db = this.getWritableDatabase();
	    String id = uri.getLastPathSegment();
	    int rowsDeleted = 0;
		rowsDeleted = db.delete(TABLE_ACCOUNTS, "AcctID = " + id, null);		
		return rowsDeleted;
	}

	//Add account
	public long addAccount(ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		long id = db.insert(TABLE_ACCOUNTS, null, values);
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

	//Get all transactions for all accounts
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

	//Add category (no ID)
	public long addCategory(String name, String note){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues categoryValues=new ContentValues();
		categoryValues.put("CatName",name);
		categoryValues.put("CatNote",note);

		long id = db.insert(TABLE_CATEGORIES, null, categoryValues);
		db.close();
		return id; 
	}

	//Add category (ID given)
	public long addCategory(String cID, String name, String note){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues categoryValues=new ContentValues();
		categoryValues.put("CatID",cID);
		categoryValues.put("CatName",name);
		categoryValues.put("CatNote",note);

		long id = db.insert(TABLE_CATEGORIES, null, categoryValues);
		db.close();
		return id; 
	}

	//Delete category (and relating subcategories if specified)
	public void deleteCategory(String cID, boolean keepSubCategories){
		SQLiteDatabase db = this.getWritableDatabase();

		String sqlDeleteCategory = "DELETE FROM " + TABLE_CATEGORIES + 
				" WHERE CatID = " + cID;

		db.execSQL(sqlDeleteCategory);

		if(keepSubCategories=false){
			String sqlDeleteSubCategories = "DELETE FROM " + TABLE_SUBCATEGORIES + 
					" WHERE ToCatID = " + cID;
			db.execSQL(sqlDeleteSubCategories);	
		}

		db.close();
	}

	//Get subcategories for all categories
	public Cursor getSubCategoriesAll(){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_SUBCATEGORIES, new String[] { "SubCatID as _id", "ToCatID", "SubCatName", "SubCatNote" }, null,
				null, null, null, null);
		return cursor;
	}

	//Get subcategories for a category
	public Cursor getSubCategories(String cID){
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		cursor = db.query(TABLE_SUBCATEGORIES, new String[] { "SubCatID as _id", "ToCatID", "SubCatName", "SubCatNote"}, "ToCatID = " + cID,
				null, null, null, null);
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

	//Add subcategory (no ID)
	public long addSubCategory(String cID, String name, String note){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues subcategoryValues=new ContentValues();
		subcategoryValues.put("ToCatID",cID);
		subcategoryValues.put("SubCatName",name);
		subcategoryValues.put("SubCatNote",note);

		long id = db.insert(TABLE_SUBCATEGORIES, null, subcategoryValues);
		db.close();
		return id; 
	}

	//Add subcategory (ID given)
	public long addSubCategory(String sID, String cID, String name, String note){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues subcategoryValues=new ContentValues();
		subcategoryValues.put("SubCatID",sID);
		subcategoryValues.put("ToCatID",cID);
		subcategoryValues.put("SubCatName",name);
		subcategoryValues.put("SubCatNote",note);

		long id = db.insert(TABLE_SUBCATEGORIES, null, subcategoryValues);
		db.close();
		return id; 
	}

	//Delete subcategory
	public void deleteSubCategory(String cID){
		SQLiteDatabase db = this.getWritableDatabase();
		String sqlDeleteSubCategory = "DELETE FROM " + TABLE_SUBCATEGORIES + 
				" WHERE SubCatID = " + cID;
		db.execSQL(sqlDeleteSubCategory);
		db.close();
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
	public long addPlannedTransaction(String aID, String name,String value,String type, String category, String memo, String offset, String rate, String cleared){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues transactionValues=new ContentValues();
		transactionValues.put("ToAcctID",aID);
		transactionValues.put("PlanName",name);
		transactionValues.put("PlanValue",value);
		transactionValues.put("PlanType",type);
		transactionValues.put("PlanCategory",category);
		transactionValues.put("PlanMemo",memo);
		transactionValues.put("PlanOffset",offset);
		transactionValues.put("PlanRate",rate);
		transactionValues.put("PlanCleared",cleared);

		long id = db.insert(TABLE_PLANNED_TRANSACTIONS, null, transactionValues);

		return id; 
	}

	//Add planned transaction (ID given)
	public long addPlannedTransaction(String pID, String aID, String name,String value,String type, String category, String memo, String offset, String rate, String cleared){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues transactionValues=new ContentValues();
		transactionValues.put("PlansID",pID);
		transactionValues.put("ToAcctID",aID);
		transactionValues.put("PlanName",name);
		transactionValues.put("PlanValue",value);
		transactionValues.put("PlanType",type);
		transactionValues.put("PlanCategory",category);
		transactionValues.put("PlanMemo",memo);
		transactionValues.put("PlanOffset",offset);
		transactionValues.put("PlanRate",rate);
		transactionValues.put("PlanCleared",cleared);

		long id = db.insert(TABLE_PLANNED_TRANSACTIONS, null, transactionValues);

		return id; 	

	}
	
	//Delete planned transaction
	public void deletePlannedTransaction(String pID){
		SQLiteDatabase db = this.getWritableDatabase();
		String sqlDeleteTransaction = "DELETE FROM " + TABLE_PLANNED_TRANSACTIONS + 
				" WHERE PlanID = " + pID;
		db.execSQL(sqlDeleteTransaction);
		db.close();
	}

}