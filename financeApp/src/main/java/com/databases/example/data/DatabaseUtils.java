package com.databases.example.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import timber.log.Timber;

/**
 * Created by kwelsh on 3/7/17.
 * Utils class to add default data
 */

public class DatabaseUtils {
    public static boolean deleteDatabase(Context context) {
        Timber.d("Deleting database...");

        try {
            Uri uri = Uri.parse(MyContentProvider.ACCOUNTS_URI + "/");
            context.getContentResolver().delete(uri, null, null);

            uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/");
            context.getContentResolver().delete(uri, null, null);

            uri = Uri.parse(MyContentProvider.PLANS_URI + "/");
            context.getContentResolver().delete(uri, null, null);

            return true;
        } catch (Exception e) {
            Timber.e("Couldn't delete database. Error e=" + e);
        }

        return false;
    }

    public static void addTestData(Context context) {
        Timber.d("Adding Test Data...");

        //CHECKING
        ContentValues accountValues = new ContentValues();
        accountValues.put(DatabaseHelper.ACCOUNT_NAME, "Checking");
        accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, "4850");
        accountValues.put(DatabaseHelper.ACCOUNT_TIME, "06:10 PM");
        accountValues.put(DatabaseHelper.ACCOUNT_DATE, "03-01-2017");
        Uri checkingUri = context.getContentResolver().insert(MyContentProvider.ACCOUNTS_URI, accountValues);

        ContentValues transactionValues = new ContentValues();
        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(checkingUri.getLastPathSegment()));
        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, -1);
        transactionValues.put(DatabaseHelper.TRANS_NAME, "STARTING BALANCE");
        transactionValues.put(DatabaseHelper.TRANS_VALUE, "5000");
        transactionValues.put(DatabaseHelper.TRANS_TYPE, "Deposit");
        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, "STARTING BALANCE");
        transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, "");
        transactionValues.put(DatabaseHelper.TRANS_MEMO, "This is an automatically generated transaction created when you add an account");
        transactionValues.put(DatabaseHelper.TRANS_TIME, "06:10 PM");
        transactionValues.put(DatabaseHelper.TRANS_DATE, "03-01-2017");
        transactionValues.put(DatabaseHelper.TRANS_CLEARED, true);
        context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);

        transactionValues = new ContentValues();
        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(checkingUri.getLastPathSegment()));
        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, -1);
        transactionValues.put(DatabaseHelper.TRANS_NAME, "Cash withdraw");
        transactionValues.put(DatabaseHelper.TRANS_VALUE, "150");
        transactionValues.put(DatabaseHelper.TRANS_TYPE, "Withdraw");
        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, "Withdraw");
        transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, "");
        transactionValues.put(DatabaseHelper.TRANS_MEMO, "Needed some extra cash...");
        transactionValues.put(DatabaseHelper.TRANS_TIME, "09:00 PM");
        transactionValues.put(DatabaseHelper.TRANS_DATE, "03-05-2017");
        transactionValues.put(DatabaseHelper.TRANS_CLEARED, true);
        context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);


        //SAVINGS
        accountValues = new ContentValues();
        accountValues.put(DatabaseHelper.ACCOUNT_NAME, "Savings");
        accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, "10000");
        accountValues.put(DatabaseHelper.ACCOUNT_TIME, "05:00 PM");
        accountValues.put(DatabaseHelper.ACCOUNT_DATE, "03-07-2017");
        Uri savingsUri = context.getContentResolver().insert(MyContentProvider.ACCOUNTS_URI, accountValues);

        transactionValues = new ContentValues();
        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(savingsUri.getLastPathSegment()));
        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, -1);
        transactionValues.put(DatabaseHelper.TRANS_NAME, "STARTING BALANCE");
        transactionValues.put(DatabaseHelper.TRANS_VALUE, "10000");
        transactionValues.put(DatabaseHelper.TRANS_TYPE, "Deposit");
        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, "STARTING BALANCE");
        transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, "");
        transactionValues.put(DatabaseHelper.TRANS_MEMO, "This is an automatically generated transaction created when you add an account");
        transactionValues.put(DatabaseHelper.TRANS_TIME, "05:00 PM");
        transactionValues.put(DatabaseHelper.TRANS_DATE, "03-07-2017");
        transactionValues.put(DatabaseHelper.TRANS_CLEARED, true);
        context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
    }

    public static void addDefaultCategories(Context context) {
        Timber.d("Adding Default CategoriesActivity...");

        //Default
        ContentValues categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Default");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        Uri categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        ContentValues subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "STARTING BALANCE");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "TRANSFER");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //ATM
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "ATM");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Deposit");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Withdraw");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Car
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Car");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Road Services");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Fuel");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Maintenance");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Food
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Food");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Groceries");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Restaurant");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Snacks");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Fun
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Fun");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Entertainment");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Electronics");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Shopping");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Housing
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "House");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Rent");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Maintenance");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Decorating");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Insurance
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Insurance");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Auto");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Health");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Home");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Life");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Job
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Job");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Paycheck");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Tax");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Income");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Loans
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Loans");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Auto");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Home Equity");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Mortgage");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Student");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Personal
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Personal");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Gift");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Donation");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Random
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Random");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Interest");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Tip");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Travel
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Travel");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Airplane");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Car Rental");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Dining");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Hotel");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Misc Expenses");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Taxi");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        //Utils
        categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "true");
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, "Utilities");
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, "Default Category");
        categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Gas");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Electricity");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Heat");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Water");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Air Conditioning");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Internet");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);

        subcategoryValues = new ContentValues();
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "true");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, "Garbage");
        subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, "Default Subcategory");
        context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);
    }
}
