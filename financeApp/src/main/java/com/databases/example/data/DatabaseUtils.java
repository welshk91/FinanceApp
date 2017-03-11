package com.databases.example.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.databases.example.model.Category;
import com.databases.example.model.Subcategory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by kwelsh on 3/7/17.
 * Utils class to add default data
 */

public class DatabaseUtils {
    /**
     * Deletes all Accounts, Transactions, and Plans
     * @param context
     * @return whether the delete was successful or not
     */
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

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     */
    public static void exportDB(Context context) {
        FileChannel src = null;
        FileChannel dst = null;

        try {
            if (!context.getExternalFilesDir(null).exists()) {
                context.getExternalFilesDir(null).mkdir();
            }

            File data = Environment.getDataDirectory();

            if (context.getExternalFilesDir(null).canWrite()) {
                String currentDBPath = "//data//" + context.getPackageName()
                        + "//databases//" + DatabaseHelper.DATABASE_NAME;
                String backupDBPath = "/" + DatabaseHelper.DATABASE_NAME + "Backup.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(context.getExternalFilesDir(null), backupDBPath);

                src = new FileInputStream(currentDB).getChannel();
                dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Timber.d("Successfully backed up database: " + backupDBPath);
                Toast.makeText(context, "Backup Successful", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Timber.e(e);
            e.printStackTrace();
            Toast.makeText(context, "Backup Error\n" + e.toString(), Toast.LENGTH_LONG).show();
        } finally {
            if (src != null) {
                try {
                    src.close();
                    dst.close();
                } catch (IOException e) {
                    Timber.e("Failed to clean up databases on backup failure! " + e);
                    e.printStackTrace();
                }
            }
        }
    }


    public static void addTestData(Context context) {
        Timber.d("Adding Test Data...");

        //CHECKING
        ContentValues accountValues = new ContentValues();
        accountValues.put(DatabaseHelper.ACCOUNT_NAME, "Checking");
        accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, "4850.50");
        accountValues.put(DatabaseHelper.ACCOUNT_TIME, "06:10");
        accountValues.put(DatabaseHelper.ACCOUNT_DATE, "2017-03-01");
        Uri checkingUri = context.getContentResolver().insert(MyContentProvider.ACCOUNTS_URI, accountValues);

        ContentValues transactionValues = new ContentValues();
        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(checkingUri.getLastPathSegment()));
        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, -1);
        transactionValues.put(DatabaseHelper.TRANS_NAME, "STARTING BALANCE");
        transactionValues.put(DatabaseHelper.TRANS_VALUE, "5000.00");
        transactionValues.put(DatabaseHelper.TRANS_TYPE, "Deposit");
        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, "STARTING BALANCE");
        transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, "");
        transactionValues.put(DatabaseHelper.TRANS_MEMO, "This is an automatically generated transaction created when you add an account");
        transactionValues.put(DatabaseHelper.TRANS_TIME, "06:10");
        transactionValues.put(DatabaseHelper.TRANS_DATE, "2017-03-01");
        transactionValues.put(DatabaseHelper.TRANS_CLEARED, true);
        context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);

        transactionValues = new ContentValues();
        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(checkingUri.getLastPathSegment()));
        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, -1);
        transactionValues.put(DatabaseHelper.TRANS_NAME, "Cash withdraw");
        transactionValues.put(DatabaseHelper.TRANS_VALUE, "149.50");
        transactionValues.put(DatabaseHelper.TRANS_TYPE, "Withdraw");
        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, "Withdraw");
        transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, "");
        transactionValues.put(DatabaseHelper.TRANS_MEMO, "Needed some extra cash...");
        transactionValues.put(DatabaseHelper.TRANS_TIME, "18:23");
        transactionValues.put(DatabaseHelper.TRANS_DATE, "2017-03-05");
        transactionValues.put(DatabaseHelper.TRANS_CLEARED, true);
        context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);


        //SAVINGS
        accountValues = new ContentValues();
        accountValues.put(DatabaseHelper.ACCOUNT_NAME, "Savings");
        accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, "10000.00");
        accountValues.put(DatabaseHelper.ACCOUNT_TIME, "18:00");
        accountValues.put(DatabaseHelper.ACCOUNT_DATE, "2017-03-07");
        Uri savingsUri = context.getContentResolver().insert(MyContentProvider.ACCOUNTS_URI, accountValues);

        transactionValues = new ContentValues();
        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(savingsUri.getLastPathSegment()));
        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, -1);
        transactionValues.put(DatabaseHelper.TRANS_NAME, "STARTING BALANCE");
        transactionValues.put(DatabaseHelper.TRANS_VALUE, "10000.00");
        transactionValues.put(DatabaseHelper.TRANS_TYPE, "Deposit");
        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, "STARTING BALANCE");
        transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, "");
        transactionValues.put(DatabaseHelper.TRANS_MEMO, "This is an automatically generated transaction created when you add an account");
        transactionValues.put(DatabaseHelper.TRANS_TIME, "18:00");
        transactionValues.put(DatabaseHelper.TRANS_DATE, "2017-03-07");
        transactionValues.put(DatabaseHelper.TRANS_CLEARED, true);
        context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
    }

    public static void addDefaultCategories(final Context context) {
        Timber.d("Adding Default Categories...");

        //Default
        ArrayList<Subcategory> subcategories = new ArrayList<>();
        subcategories.add(new Subcategory(-1, -1, true, "STARTING BALANCE", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "TRANSFER", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Default", "Default Category"), subcategories);

        //ATM
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Deposit", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Withdraw", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "ATM", "Default Category"), subcategories);

        //Car
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Road Services", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Fuel", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Maintenance", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Car", "Default Category"), subcategories);

        //Food
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Snacks", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Restaurant", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Groceries", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Food", "Default Category"), subcategories);

        //Fun
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Entertainment", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Electronics", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Shopping", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Fun", "Default Category"), subcategories);

        //Housing
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Mortgage", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Rent", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Maintenance", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Decorating", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "House", "Default Category"), subcategories);

        //Insurance
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Auto", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Health", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Dental", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Home", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Life", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Insurance", "Default Category"), subcategories);

        //Job
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Paycheck", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Tax", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Income", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Job", "Default Category"), subcategories);

        //Loans
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Auto", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Home Equity", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Mortgage", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Student", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Loans", "Default Category"), subcategories);

        //Personal
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Gift", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Donation", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Personal", "Default Category"), subcategories);

        //Random
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Interest", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Tip", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Random", "Default Category"), subcategories);

        //Travel
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Airplane", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Car Rental", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Dining", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Hotel", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Misc Expenses", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Taxi", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Travel", "Default Category"), subcategories);

        //Utils
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Gas", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Electricity", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Heat", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Water", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Air Conditioning", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Internet", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Garbage", "Default Subcategory"));
        insertCategories(context, new Category(-1, true, "Utilities", "Default Category"), subcategories);
    }

    private static void insertCategories(final Context context, final Category category, final ArrayList<Subcategory> subcategories){
        ContentValues categoryValues = new ContentValues();
        categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, category.isDefault);
        categoryValues.put(DatabaseHelper.CATEGORY_NAME, category.name);
        categoryValues.put(DatabaseHelper.CATEGORY_NOTE, category.note);
        Uri categoriesUri = context.getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);

        ContentValues subcategoryValues = new ContentValues();
        for (Subcategory subcategory : subcategories) {
            subcategoryValues.clear();
            subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, Long.parseLong(categoriesUri.getLastPathSegment()));
            subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, subcategory.isDefault);
            subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, subcategory.name);
            subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, subcategory.note);
            context.getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);
        }
    }
}
