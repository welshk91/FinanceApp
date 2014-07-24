/* Class that handles database operations
 * Does everything from simple insert/update/deleting of information to
 * more complex database operations like balances
 */

package com.databases.example.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "dbFinance";

    //Table Names
    private static final String TABLE_ACCOUNTS = "tblAccounts";
    private static final String TABLE_TRANSACTIONS = "tblTrans";
    private static final String TABLE_PLANS = "tblPlanTrans";
    private static final String TABLE_CATEGORIES = "tblCategory";
    private static final String TABLE_SUBCATEGORIES = "tblSubCategory";
    private static final String TABLE_LINKS = "tblLinks";
    private static final String TABLE_NOTIFICATIONS = "tblNotifications";

    //Column Names
    public static final String ACCOUNT_ID = "_id";
    public static final String ACCOUNT_NAME = "AcctName";
    public static final String ACCOUNT_BALANCE = "AcctBalance";
    public static final String ACCOUNT_TIME = "AcctTime";
    public static final String ACCOUNT_DATE = "AcctDate";

    public static final String TRANS_ID = "_id";
    public static final String TRANS_ACCT_ID = "ToAcctID";
    public static final String TRANS_PLAN_ID = "ToPlanID";
    public static final String TRANS_NAME = "TransName";
    public static final String TRANS_VALUE = "TransValue";
    public static final String TRANS_TYPE = "TransType";
    public static final String TRANS_CATEGORY = "TransCategory";
    public static final String TRANS_CHECKNUM = "TransCheckNum";
    public static final String TRANS_MEMO = "TransMemo";
    public static final String TRANS_TIME = "TransTime";
    public static final String TRANS_DATE = "TransDate";
    public static final String TRANS_CLEARED = "TransCleared";

    public static final String PLAN_ID = "_id";
    public static final String PLAN_ACCT_ID = TRANS_ACCT_ID;
    public static final String PLAN_NAME = "PlanName";
    public static final String PLAN_VALUE = "PlanValue";
    public static final String PLAN_TYPE = "PlanType";
    public static final String PLAN_CATEGORY = "PlanCategory";
    public static final String PLAN_MEMO = "PlanMemo";
    public static final String PLAN_OFFSET = "PlanOffset";
    public static final String PLAN_RATE = "PlanRate";
    public static final String PLAN_SCHEDULED = "PlanScheduled";
    public static final String PLAN_NEXT = "PlanNext";
    public static final String PLAN_CLEARED = "PlanCleared";

    public static final String CATEGORY_ID = "_id";
    public static final String CATEGORY_NAME = "CatName";
    public static final String CATEGORY_NOTE = "CatNote";

    public static final String SUBCATEGORY_ID = "_id";
    public static final String SUBCATEGORY_CAT_ID = "ToCatID";
    public static final String SUBCATEGORY_NAME = "SubCatName";
    public static final String SUBCATEGORY_NOTE = "SubCatNote";

    public static final String NOT_ID = "_id";
    public static final String NOT_NAME = "NotificationName";
    public static final String NOT_VALUE = "NotificationValue";
    public static final String NOT_DATE = "NotificationDate";

    private Context context = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper-onCreate", "Creating database...");

        String sqlCommandAccounts = "CREATE TABLE IF NOT EXISTS "
                + TABLE_ACCOUNTS
                + " (" + ACCOUNT_ID + " INTEGER PRIMARY KEY, " + ACCOUNT_NAME + " VARCHAR, " + ACCOUNT_BALANCE + " VARCHAR, " + ACCOUNT_TIME + " VARCHAR, " + ACCOUNT_DATE + " VARCHAR);";

        String sqlCommandTransactions = "CREATE TABLE IF NOT EXISTS "
                + TABLE_TRANSACTIONS
                + " (" + TRANS_ID + " INTEGER PRIMARY KEY, " + TRANS_ACCT_ID + " VARCHAR, " + TRANS_PLAN_ID + " VARCHAR, " + TRANS_NAME + " VARCHAR, " + TRANS_VALUE + " VARCHAR, " + TRANS_TYPE + " VARCHAR, " + TRANS_CATEGORY + " VARCHAR, " + TRANS_CHECKNUM + " VARCHAR, " + TRANS_MEMO + " VARCHAR, " + TRANS_TIME + " VARCHAR, " + TRANS_DATE + " VARCHAR, " + TRANS_CLEARED + " VARCHAR);";

        String sqlCommandPlans = "CREATE TABLE IF NOT EXISTS "
                + TABLE_PLANS
                + " (" + PLAN_ID + " INTEGER PRIMARY KEY, " + PLAN_ACCT_ID + " VARCHAR, " + PLAN_NAME + " VARCHAR, " + PLAN_VALUE + " VARCHAR, " + PLAN_TYPE + " VARCHAR, " + PLAN_CATEGORY + " VARCHAR, " + PLAN_MEMO + " VARCHAR, " + PLAN_OFFSET + " VARCHAR, " + PLAN_RATE + " VARCHAR, " + PLAN_NEXT + " VARCHAR, " + PLAN_SCHEDULED + " VARCHAR, " + PLAN_CLEARED + " VARCHAR);";

        String sqlCommandCategory = "CREATE TABLE IF NOT EXISTS "
                + TABLE_CATEGORIES
                + " (" + CATEGORY_ID + " INTEGER PRIMARY KEY, " + CATEGORY_NAME + " VARCHAR, " + CATEGORY_NOTE + " VARCHAR);";

        String sqlCommandSubCategory = "CREATE TABLE IF NOT EXISTS "
                + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_ID + " INTEGER PRIMARY KEY, " + SUBCATEGORY_CAT_ID + " VARCHAR, " + SUBCATEGORY_NAME + " VARCHAR, " + SUBCATEGORY_NOTE + " VARCHAR);";

        String sqlCommandLinks = "CREATE TABLE IF NOT EXISTS "
                + TABLE_LINKS
                + " (LinkID INTEGER PRIMARY KEY, ToID VARCHAR, LinkName VARCHAR, LinkMemo VARCHAR, ParentType VARCHAR);";

        String sqlCommandNotifications = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NOTIFICATIONS
                + " (" + NOT_ID + " INTEGER PRIMARY KEY, " + NOT_NAME + " VARCHAR, " + NOT_VALUE + " VARCHAR, " + NOT_DATE + " VARCHAR);";


        db.execSQL(sqlCommandAccounts);
        db.execSQL(sqlCommandTransactions);
        db.execSQL(sqlCommandPlans);
        db.execSQL(sqlCommandCategory);
        db.execSQL(sqlCommandSubCategory);
        db.execSQL(sqlCommandLinks);
        db.execSQL(sqlCommandNotifications);

        addDefaultCategories(db);
    }

    public boolean deleteDatabase() {
        Log.d("DatabaseHelper-deleteDatabase", "Deleting database...");

        try {
            return context.deleteDatabase(DATABASE_NAME);
        } catch (Exception e) {
            Log.e("DatabaseHelper-deleteDatabase", "Couldn't delete database. Error e=" + e);
        }

        return false;
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
    private void addDefaultCategories(SQLiteDatabase db) {
        Log.d("DatabaseHelper-onCreate", "Adding Default Categories...");

        //Default
        final String sqlDefaultCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Default');";
        final String sqlDefaultSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('STARTING BALANCE',1);";
        final String sqlDefaultSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('TRANSFER',1);";

        //ATM
        final String sqlATMCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('ATM');";
        final String sqlATMSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Deposit',2);";
        final String sqlATMSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Withdraw',2);";

        //Car
        final String sqlCarCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Car');";
        final String sqlCarSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Road Services',3);";
        final String sqlCarSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Fuel',3);";
        final String sqlCarSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Lease',3);";
        final String sqlCarSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Maitenance',3);";

        //Food
        final String sqlFoodCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Food');";
        final String sqlFoodSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Groceries',4);";
        final String sqlFoodSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Restaurant',4);";
        final String sqlFoodSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Snacks',4);";

        //Fun
        final String sqlFunCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Fun');";
        final String sqlFunSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Entertainment',5);";
        final String sqlFunSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Electronics',5);";
        final String sqlFunSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Shopping',5);";

        //Housing
        final String sqlHouseCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('House');";
        final String sqlHouseSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Rent',6);";
        final String sqlHouseSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Maintenance',6);";
        final String sqlHouseSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Decorating',6);";

        //Insurance
        final String sqlInsuranceCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Insurance');";
        final String sqlInsuranceSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Auto',7);";
        final String sqlInsuranceSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Health',7);";
        final String sqlInsuranceSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Home',7);";
        final String sqlInsuranceSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Life',7);";

        //Job
        final String sqlJobCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Job');";
        final String sqlJobSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Paycheck',8);";
        final String sqlJobSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Tax',8);";
        final String sqlJobSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Income',8);";

        //Loans
        final String sqlLoansCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Loans');";
        final String sqlLoansSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Auto',9);";
        final String sqlLoansSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Home Equity',9);";
        final String sqlLoansSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Mortgage',9);";
        final String sqlLoansSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Student',9);";

        //Personal
        final String sqlPersonalCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Personal');";
        final String sqlPersonalSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Gift',10);";
        final String sqlPersonalSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Donation',10);";

        //Random
        final String sqlRandomCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Random');";
        final String sqlRandomSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Interest',11);";
        final String sqlRandomSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Tip',11);";

        //Travel
        final String sqlTravelCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Travel');";
        final String sqlTravelSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Airplane',12);";
        final String sqlTravelSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Car Rental',12);";
        final String sqlTravelSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Dining',12);";
        final String sqlTravelSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Hotel',12);";
        final String sqlTravelSubCategories5 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Misc Expenses',12);";
        final String sqlTravelSubCategories6 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Taxi',12);";

        //Utilities
        final String sqlUtilitiesCategory = "INSERT INTO " + TABLE_CATEGORIES
                + " (" + CATEGORY_NAME + ") " + "VALUES ('Utils');";
        final String sqlUtilitiesSubCategories = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Gas',13);";
        final String sqlUtilitiesSubCategories2 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Electricty',13);";
        final String sqlUtilitiesSubCategories3 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Heat',13);";
        final String sqlUtilitiesSubCategories4 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Water',13);";
        final String sqlUtilitiesSubCategories5 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('AC',13);";
        final String sqlUtilitiesSubCategories6 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Cable',13);";
        final String sqlUtilitiesSubCategories7 = "INSERT INTO " + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_NAME + ", " + SUBCATEGORY_CAT_ID + ") " + "VALUES ('Internet',13);";


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

    //Returns Database file
    public File getDatabase() {
        File currentDB = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
        //Log.d("DatabaseHelper-getDatabase", "currentDB="+currentDB.getAbsolutePath());
        return currentDB;
    }

    //Get all accounts
    public Cursor getAccounts(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.query(TABLE_ACCOUNTS, new String[]{ACCOUNT_ID + " as _id", ACCOUNT_NAME, ACCOUNT_BALANCE, ACCOUNT_TIME, ACCOUNT_DATE}, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    //Get single account
    public Cursor getAccount(String aID) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlCommand = "SELECT * FROM " + TABLE_ACCOUNTS +
                " WHERE " + ACCOUNT_ID + " = " + aID;

        cursor = db.rawQuery(sqlCommand, null);
        return cursor;
    }

    //Get searched accounts
    public Cursor getSearchedAccounts(String query) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();

        //Command used to search
        String sqlCommand = " SELECT " + ACCOUNT_ID + " as _id, * FROM " + TABLE_ACCOUNTS +
                " WHERE " + ACCOUNT_NAME +
                " LIKE ?" +
                " UNION " +
                " SELECT " + ACCOUNT_ID + " as _id, * FROM " + TABLE_ACCOUNTS +
                " WHERE " + ACCOUNT_BALANCE +
                " LIKE ?" +
                " UNION " +
                " SELECT " + ACCOUNT_ID + " as _id, * FROM " + TABLE_ACCOUNTS +
                " WHERE " + ACCOUNT_TIME +
                " LIKE ?" +
                " UNION " +
                " SELECT " + ACCOUNT_ID + " as _id, * FROM " + TABLE_ACCOUNTS +
                " WHERE " + ACCOUNT_DATE +
                " LIKE ?";

        cursor = db.rawQuery(sqlCommand, new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%"});
        return cursor;
    }

    //Delete account (and relating transactions if specified)
    public int deleteAccount(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ACCOUNTS, whereClause, whereArgs);
    }

    //Add account
    public long addAccount(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_ACCOUNTS, null, values);
    }

    //Updates an account
    public int updateAccount(ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_ACCOUNTS, values, whereClause, whereArgs);
    }

    //Get all transactions for an account
    public Cursor getTransactions(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.query(TABLE_TRANSACTIONS, new String[]{TRANS_ID + " as _id", TRANS_ACCT_ID, TRANS_PLAN_ID, TRANS_NAME, TRANS_VALUE, TRANS_TYPE, TRANS_CATEGORY, TRANS_CHECKNUM, TRANS_MEMO, TRANS_TIME, TRANS_DATE, TRANS_CLEARED}, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    //Get single transaction
    public Cursor getTransaction(String tID) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlCommand = "SELECT * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANS_ID + " = " + tID;

        cursor = db.rawQuery(sqlCommand, null);
        return cursor;
    }

    //Get searched transactions
    public Cursor getSearchedTransactions(String query) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();

        //Command used to search
        String sqlCommand = " SELECT " + TRANS_ID + " as _id, * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANS_NAME +
                " LIKE ?" +
                " UNION " +
                " SELECT " + TRANS_ID + " as _id, * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANS_VALUE +
                " LIKE ?" +
                " UNION " +
                " SELECT " + TRANS_ID + " as _id, * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANS_CATEGORY +
                " LIKE ?" +
                " UNION " +
                " SELECT " + TRANS_ID + " as _id, * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANS_DATE +
                " LIKE ?" +
                " UNION " +
                " SELECT " + TRANS_ID + " as _id, * FROM " + TABLE_TRANSACTIONS +
                " WHERE  " + TRANS_TIME +
                " LIKE ?" +
                " UNION " +
                " SELECT " + TRANS_ID + " as _id, * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANS_MEMO +
                " LIKE ?" +
                " UNION " +
                " SELECT " + TRANS_ID + " as _id, * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + TRANS_CHECKNUM +
                " LIKE ?";

        cursor = db.rawQuery(sqlCommand, new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%"});
        return cursor;
    }

    //Delete transaction (and relating transactions if specified)
    public int deleteTransaction(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TRANSACTIONS, whereClause, whereArgs);
    }

    //Add transaction
    public long addTransaction(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_TRANSACTIONS, null, values);
    }

    //Updates a transaction
    public int updateTransaction(ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_TRANSACTIONS, values, whereClause, whereArgs);
    }

    //Get all categories
    public Cursor getCategories(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.query(TABLE_CATEGORIES, new String[]{CATEGORY_ID + " as _id", CATEGORY_NAME, CATEGORY_NOTE}, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    //Get single category
    public Cursor getCategory(String cID) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlCommand = "SELECT * FROM " + TABLE_CATEGORIES +
                " WHERE " + CATEGORY_ID + " = " + cID;

        cursor = db.rawQuery(sqlCommand, null);
        return cursor;
    }

    //Add category
    public long addCategory(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    //Delete category (and relating subcategories if specified)
    public int deleteCategory(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CATEGORIES, whereClause, whereArgs);
    }

    //Updates a category
    public int updateCategory(ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_CATEGORIES, values, whereClause, whereArgs);
    }

    //Get subcategories for a category
    public Cursor getSubCategories(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.query(TABLE_SUBCATEGORIES, new String[]{SUBCATEGORY_ID + " as _id", SUBCATEGORY_CAT_ID, SUBCATEGORY_NAME, SUBCATEGORY_NOTE}, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    //Get single subcategory
    public Cursor getSubCategory(String sID) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlCommand = "SELECT * FROM " + TABLE_SUBCATEGORIES +
                " WHERE " + SUBCATEGORY_ID + " = " + sID;

        cursor = db.rawQuery(sqlCommand, null);
        return cursor;
    }

    //Add subcategory (ID given)
    public long addSubCategory(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_SUBCATEGORIES, null, values);
    }

    //Delete subcategory
    public int deleteSubCategory(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SUBCATEGORIES, whereClause, whereArgs);
    }

    //Updates a subcategory
    public int updateSubCategory(ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_SUBCATEGORIES, values, whereClause, whereArgs);
    }

    //Get all planned transactions for all accounts
    public Cursor getPlans(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.query(TABLE_PLANS, new String[]{PLAN_ID + " as _id", PLAN_ACCT_ID, PLAN_NAME, PLAN_VALUE, PLAN_TYPE, PLAN_CATEGORY, PLAN_MEMO, PLAN_OFFSET, PLAN_RATE, PLAN_NEXT, PLAN_SCHEDULED, PLAN_CLEARED}, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    //Get single planned transaction
    public Cursor getPlan(String pID) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlCommand = "SELECT * FROM " + TABLE_PLANS +
                " WHERE " + PLAN_ID + " = " + pID;

        cursor = db.rawQuery(sqlCommand, null);
        return cursor;
    }

    //Add planned transaction (no ID)
    public long addPlan(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_PLANS, null, values);
    }

    //Delete planned transaction
    public int deletePlan(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PLANS, whereClause, whereArgs);
    }

    //Updates a plan
    public int updatePlan(ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TABLE_PLANS, values, whereClause, whereArgs);
    }

    //Get all notifications
    public Cursor getNotifications(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        cursor = db.query(TABLE_NOTIFICATIONS, new String[]{NOT_ID + " as _id", NOT_NAME, NOT_VALUE, NOT_DATE}, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    //Get single notification
    public Cursor getNotification(String nID) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();

        String sqlCommand = "SELECT * FROM " + TABLE_NOTIFICATIONS +
                " WHERE " + NOT_ID + " = " + nID;

        cursor = db.rawQuery(sqlCommand, null);
        return cursor;
    }

    //Add planned transaction (no ID)
    public long addNotification(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    //Delete planned transaction
    public int deleteNotification(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOTIFICATIONS, whereClause, whereArgs);
    }

}//End DatabaseHelper