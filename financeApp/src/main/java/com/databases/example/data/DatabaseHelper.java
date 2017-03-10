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

import java.io.File;

import timber.log.Timber;

public class DatabaseHelper extends SQLiteOpenHelper {
    //Database Name
    public static final String DATABASE_NAME = "dbFinance";

    //Database Version
    public static final int DATABASE_VERSION = 1;

    //Table Names
    private static final String TABLE_ACCOUNTS = "Accounts";
    private static final String TABLE_TRANSACTIONS = "Transactions";
    private static final String TABLE_PLANS = "Plans";
    private static final String TABLE_CATEGORIES = "Category";
    private static final String TABLE_SUBCATEGORIES = "SubCategory";
    private static final String TABLE_LINKS = "Links";
    private static final String TABLE_NOTIFICATIONS = "Notifications";

    //Column Names
    public static final String ACCOUNT_ID = "_id";
    public static final String ACCOUNT_NAME = "AccountName";
    public static final String ACCOUNT_BALANCE = "AccountBalance";
    public static final String ACCOUNT_TIME = "AccountTime";
    public static final String ACCOUNT_DATE = "AccountDate";

    public static final String TRANS_ID = "_id";
    public static final String TRANS_ACCT_ID = "ToAccountId";
    public static final String TRANS_PLAN_ID = "ToPlanId";
    public static final String TRANS_NAME = "TransactionName";
    public static final String TRANS_VALUE = "TransactionValue";
    public static final String TRANS_TYPE = "TransactionType";
    public static final String TRANS_CATEGORY = "TransactionCategory";
    public static final String TRANS_CHECKNUM = "TransactionCheckNumber";
    public static final String TRANS_MEMO = "TransactionMemo";
    public static final String TRANS_TIME = "TransactionTime";
    public static final String TRANS_DATE = "TransactionDate";
    public static final String TRANS_CLEARED = "TransactionCleared";

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
    public static final String CATEGORY_IS_DEFAULT = "CategoryIsDefault";
    public static final String CATEGORY_NAME = "CategoryName";
    public static final String CATEGORY_NOTE = "CategoryNote";

    public static final String SUBCATEGORY_ID = "_id";
    public static final String SUBCATEGORY_CAT_ID = "ToCategoryId";
    public static final String SUBCATEGORY_IS_DEFAULT = "SubcategoryIsDefault";
    public static final String SUBCATEGORY_NAME = "SubcategoryName";
    public static final String SUBCATEGORY_NOTE = "SubcategoryNote";

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
        Timber.d("Creating database...");

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
                + " (" + CATEGORY_ID + " INTEGER PRIMARY KEY, " + CATEGORY_IS_DEFAULT + " VARCHAR, " + CATEGORY_NAME + " VARCHAR, " + CATEGORY_NOTE + " VARCHAR);";

        String sqlCommandSubCategory = "CREATE TABLE IF NOT EXISTS "
                + TABLE_SUBCATEGORIES
                + " (" + SUBCATEGORY_ID + " INTEGER PRIMARY KEY, " + SUBCATEGORY_CAT_ID + " VARCHAR, " + SUBCATEGORY_IS_DEFAULT + " VARCHAR, " + SUBCATEGORY_NAME + " VARCHAR, " + SUBCATEGORY_NOTE + " VARCHAR);";

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("Upgrading database from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBCATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);
    }

    //Returns Database file
    public File getDatabase() {
        File currentDB = context.getDatabasePath(DatabaseHelper.DATABASE_NAME);
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
        cursor = db.query(TABLE_CATEGORIES, new String[]{CATEGORY_ID + " as _id", CATEGORY_IS_DEFAULT, CATEGORY_NAME, CATEGORY_NOTE}, selection,
                selectionArgs, null, null, sortOrder);
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
        cursor = db.query(TABLE_SUBCATEGORIES, new String[]{SUBCATEGORY_ID + " as _id", SUBCATEGORY_CAT_ID, SUBCATEGORY_IS_DEFAULT, SUBCATEGORY_NAME, SUBCATEGORY_NOTE}, selection,
                selectionArgs, null, null, sortOrder);
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
}