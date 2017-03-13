package com.databases.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.databases.example.features.categories.Category;
import com.databases.example.features.categories.Subcategory;
import com.databases.example.features.checkbook.accounts.Account;
import com.databases.example.features.checkbook.transactions.Transaction;
import com.databases.example.features.plans.Plan;
import com.databases.example.features.plans.PlanUtils;

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
     *
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

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(-1, -1, -1, "STARTING BALANCE", "5000.00", "Deposit", "STARTING BALANCE", "", "This is an automatically generated transaction created when you add an account", "2017-03-01", "06:10", "true"));
        transactions.add(new Transaction(-1, -1, -1, "Cash withdraw", "160.00", "Withdraw", "Withdraw", "", "Need some extra cash", "2017-03-05", "18:23", "true"));
        transactions.add(new Transaction(-1, -1, -1, "Devon's Wedding Gift", "250.00", "Withdraw", "Gift", "8675309", "Check for Devon's Wedding", "2017-03-08", "9:17", "false"));
        long checkingId = insertAccount(context, new Account(-1, "Checking", "4590.00", "2017-03-01", "06:10"), transactions);

        transactions.clear();
        transactions.add(new Transaction(-1, -1, -1, "STARTING BALANCE", "10000.00", "Deposit", "STARTING BALANCE", "", "This is an automatically generated transaction created when you add an account", "2017-03-07", "18:30", "true"));
        long savingsId = insertAccount(context, new Account(-1, "Savings", "10000.00", "2017-03-07", "18:30"), transactions);

        transactions.clear();
        transactions.add(new Transaction(-1, -1, -1, "STARTING BALANCE", "100.00", "Deposit", "STARTING BALANCE", "", "This is an automatically generated transaction created when you add an account", "2017-03-11", "12:45", "true"));
        transactions.add(new Transaction(-1, -1, -1, "Hotdog Bet", "5.00", "Deposit", "Personal", "", "Tyler finally paid me for eating 7 hotdogs", "2017-03-12", "16:57", "true"));
        long cashId = insertAccount(context, new Account(-1, "Cash", "105.00", "2017-03-11", "12:45"), transactions);

        insertPlan(context, new Plan(1, (int) checkingId, "Paycheck", "2200.00", "Deposit",
                "Paycheck", "Time to get paid!", "2017-03-10", "2 Weeks", "2017-03-24", "true", "true"), checkingId);

        //Annoying Transaction
        insertPlan(context, new Plan(2, (int) cashId, "Annoying Transaction", "50.00", "Deposit",
                "Gift", "This is an annoying test plan...", "2017-03-09", "1 Days", "2017-03-13", "true", "true"), cashId);
    }


    public static void addDefaultCategories(final Context context) {
        Timber.d("Adding Default Categories...");

        //Default
        ArrayList<Subcategory> subcategories = new ArrayList<>();
        subcategories.add(new Subcategory(-1, -1, true, "STARTING BALANCE", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "TRANSFER", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Default", "Default Category"), subcategories);

        //ATM
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Deposit", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Withdraw", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "ATM", "Default Category"), subcategories);

        //Car
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Road Services", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Fuel", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Maintenance", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Car", "Default Category"), subcategories);

        //Food
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Snacks", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Restaurant", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Groceries", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Food", "Default Category"), subcategories);

        //Fun
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Entertainment", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Electronics", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Shopping", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Fun", "Default Category"), subcategories);

        //Housing
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Mortgage", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Rent", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Maintenance", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Decorating", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "House", "Default Category"), subcategories);

        //Insurance
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Auto", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Health", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Dental", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Home", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Life", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Insurance", "Default Category"), subcategories);

        //Job
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Paycheck", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Tax", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Income", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Job", "Default Category"), subcategories);

        //Loans
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Auto", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Home Equity", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Mortgage", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Student", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Loans", "Default Category"), subcategories);

        //Personal
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Gift", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Donation", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Personal", "Default Category"), subcategories);

        //Random
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Interest", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Tip", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Random", "Default Category"), subcategories);

        //Travel
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Airplane", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Car Rental", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Dining", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Hotel", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Misc Expenses", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Taxi", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Travel", "Default Category"), subcategories);

        //Utils
        subcategories.clear();
        subcategories.add(new Subcategory(-1, -1, true, "Gas", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Electricity", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Heat", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Water", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Air Conditioning", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Internet", "Default Subcategory"));
        subcategories.add(new Subcategory(-1, -1, true, "Garbage", "Default Subcategory"));
        insertCategory(context, new Category(-1, true, "Utilities", "Default Category"), subcategories);
    }

    private static long insertCategory(final Context context, final Category category, final ArrayList<Subcategory> subcategories) {
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

        return Long.parseLong(categoriesUri.getLastPathSegment());
    }

    private static long insertAccount(final Context context, final Account account, final ArrayList<Transaction> transactions) {
        ContentValues accountValues = new ContentValues();
        accountValues.put(DatabaseHelper.ACCOUNT_NAME, account.name);
        accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, account.balance);
        accountValues.put(DatabaseHelper.ACCOUNT_TIME, account.time);
        accountValues.put(DatabaseHelper.ACCOUNT_DATE, account.date);
        Uri accountUri = context.getContentResolver().insert(MyContentProvider.ACCOUNTS_URI, accountValues);

        ContentValues transactionValues = new ContentValues();
        for (Transaction transaction : transactions) {
            transactionValues.clear();
            transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(accountUri.getLastPathSegment()));
            transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, -1);
            transactionValues.put(DatabaseHelper.TRANS_NAME, transaction.name);
            transactionValues.put(DatabaseHelper.TRANS_VALUE, transaction.value);
            transactionValues.put(DatabaseHelper.TRANS_TYPE, transaction.type);
            transactionValues.put(DatabaseHelper.TRANS_CATEGORY, transaction.category);
            transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, transaction.checknum);
            transactionValues.put(DatabaseHelper.TRANS_MEMO, transaction.memo);
            transactionValues.put(DatabaseHelper.TRANS_TIME, transaction.time);
            transactionValues.put(DatabaseHelper.TRANS_DATE, transaction.date);
            transactionValues.put(DatabaseHelper.TRANS_CLEARED, transaction.cleared);
            context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
        }

        return Long.parseLong(accountUri.getLastPathSegment());
    }

    private static long insertPlan(final Context context, final Plan plan, long accountId) {
        if (PlanUtils.schedule(context, plan)) {
            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_ID, plan.id);
            planValues.put(DatabaseHelper.PLAN_ACCT_ID, accountId);
            planValues.put(DatabaseHelper.PLAN_NAME, plan.name);
            planValues.put(DatabaseHelper.PLAN_VALUE, plan.value);
            planValues.put(DatabaseHelper.PLAN_TYPE, plan.type);
            planValues.put(DatabaseHelper.PLAN_CATEGORY, plan.category);
            planValues.put(DatabaseHelper.PLAN_MEMO, plan.memo);
            planValues.put(DatabaseHelper.PLAN_OFFSET, plan.offset);
            planValues.put(DatabaseHelper.PLAN_RATE, plan.rate);
            planValues.put(DatabaseHelper.PLAN_NEXT, plan.next);
            planValues.put(DatabaseHelper.PLAN_SCHEDULED, plan.scheduled);
            planValues.put(DatabaseHelper.PLAN_CLEARED, plan.cleared);
            Uri planUri = context.getContentResolver().insert(MyContentProvider.PLANS_URI, planValues);

            return Long.parseLong(planUri.getLastPathSegment());
        }

        return -1;
    }
}
