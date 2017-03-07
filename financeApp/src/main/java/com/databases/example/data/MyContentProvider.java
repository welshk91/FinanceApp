/* Class that handles the content provider used
 * Lets my app not have to worry about cursors so much, 
 * though can be used to expose information in the future if need be
 */

package com.databases.example.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.databases.example.fragments.CardsFragment;

public class MyContentProvider extends ContentProvider {
    private static DatabaseHelper dh = null;

    //IDs
    private static final int DATABASE_ID = 123;
    private static final int ACCOUNTS_ID = 100;
    private static final int ACCOUNT_ID = 110;
    private static final int ACCOUNT_SEARCH_ID = 120;
    private static final int TRANSACTIONS_ID = 200;
    private static final int TRANSACTION_ID = 210;
    private static final int TRANSACTION_SEARCH_ID = 220;
    private static final int CATEGORIES_ID = 300;
    private static final int CATEGORY_ID = 310;
    private static final int SUBCATEGORIES_ID = 400;
    private static final int SUBCATEGORY_ID = 410;
    public static final int PLANS_ID = 500;
    private static final int PLAN_ID = 510;
    private static final int LINKS_ID = 600;
    private static final int LINK_ID = 610;
    private static final int NOTIFICATIONS_ID = 700;
    private static final int NOTIFICATION_ID = 710;

    private static final String AUTHORITY = "com.databases.example.provider";
    private static final String PATH_ACCOUNTS = "accounts";
    private static final String PATH_TRANSACTIONS = "transactions";
    private static final String PATH_CATEGORIES = "categories";
    private static final String PATH_SUBCATEGORIES = "subcategories";
    private static final String PATH_PLANS = "plannedTransactions";
    private static final String PATH_LINKS = "links";
    private static final String PATH_NOTIFICATIONS = "notifications";

    public static final Uri DATABASE_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri ACCOUNTS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_ACCOUNTS);
    public static final Uri TRANSACTIONS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_TRANSACTIONS);
    public static final Uri CATEGORIES_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_CATEGORIES);
    public static final Uri SUBCATEGORIES_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_SUBCATEGORIES);
    public static final Uri PLANS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_PLANS);
    public static final Uri LINKS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_LINKS);
    public static final Uri NOTIFICATIONS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_NOTIFICATIONS);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, null, DATABASE_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_ACCOUNTS, ACCOUNTS_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_ACCOUNTS + "/#", ACCOUNT_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_ACCOUNTS + "/SEARCH/*", ACCOUNT_SEARCH_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS, TRANSACTIONS_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS + "/#", TRANSACTION_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS + "/SEARCH/*", TRANSACTION_SEARCH_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_CATEGORIES, CATEGORIES_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_CATEGORIES + "/#", CATEGORY_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_SUBCATEGORIES, SUBCATEGORIES_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_SUBCATEGORIES + "/#", SUBCATEGORY_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_PLANS, PLANS_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_PLANS + "/#", PLAN_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_LINKS, LINKS_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_LINKS + "/#", LINK_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_NOTIFICATIONS, NOTIFICATIONS_ID);
        sURIMatcher.addURI(AUTHORITY, PATH_NOTIFICATIONS + "/#", NOTIFICATION_ID);
    }

    @Override
    public boolean onCreate() {
        dh = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case ACCOUNTS_ID:
                cursor = dh.getAccounts(projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case ACCOUNT_ID:
                cursor = dh.getAccount(uri.getLastPathSegment());
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case ACCOUNT_SEARCH_ID:
                cursor = dh.getSearchedAccounts(uri.getLastPathSegment());
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case TRANSACTIONS_ID:
                cursor = dh.getTransactions(projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case TRANSACTION_ID:
                cursor = dh.getTransaction(uri.getLastPathSegment());
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case TRANSACTION_SEARCH_ID:
                cursor = dh.getSearchedTransactions(uri.getLastPathSegment());
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case CATEGORIES_ID:
                cursor = dh.getCategories(projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case CATEGORY_ID:
                cursor = dh.getCategory(uri.getLastPathSegment());
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case SUBCATEGORIES_ID:
                cursor = dh.getSubCategories(projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case SUBCATEGORY_ID:
                cursor = dh.getSubCategory(uri.getLastPathSegment());
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case PLANS_ID:
                cursor = dh.getPlans(projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case PLAN_ID:
                cursor = dh.getPlan(uri.getLastPathSegment());
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case LINKS_ID:
                // TODO Need to handle LinksActivity eventually
            case LINK_ID:
                // TODO Need to handle LinksActivity eventually
            case NOTIFICATIONS_ID:
                cursor = dh.getNotifications(projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case NOTIFICATION_ID:
                cursor = dh.getNotification(uri.getLastPathSegment());
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new IllegalArgumentException("MyContentProvider-query: Unknown URI");
        }

    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsDeleted = 0;

        switch (uriType) {
            case DATABASE_ID:
                dh.deleteDatabase();
                Log.d(getClass().getSimpleName(), "URI=" + ACCOUNTS_URI);
                getContext().getContentResolver().notifyChange(ACCOUNTS_URI, null);
                getContext().getContentResolver().notifyChange(TRANSACTIONS_URI, null);
                getContext().getContentResolver().notifyChange(CATEGORIES_URI, null);
                getContext().getContentResolver().notifyChange(SUBCATEGORIES_URI, null);
                getContext().getContentResolver().notifyChange(LINKS_URI, null);
                getContext().getContentResolver().notifyChange(PLANS_URI, null);
                break;
            case ACCOUNT_ID:
                rowsDeleted = dh.deleteAccount(uri, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                CardsFragment.accountChanged = true;
                break;
            case TRANSACTION_ID:
                rowsDeleted = dh.deleteTransaction(uri, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                CardsFragment.transactionChanged = true;
                break;
            case CATEGORY_ID:
                rowsDeleted = dh.deleteCategory(uri, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case SUBCATEGORY_ID:
                rowsDeleted = dh.deleteSubCategory(uri, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case PLAN_ID:
                rowsDeleted = dh.deletePlan(uri, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                CardsFragment.planChanged = true;
                break;
            case LINK_ID:
                // TODO Need to handle LinksActivity eventually
                break;
            case NOTIFICATION_ID:
                rowsDeleted = dh.deleteNotification(uri, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                throw new IllegalArgumentException("MyContentProvider-delete: Unknown URI");
        }

        return rowsDeleted;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        long id;
        switch (uriType) {
            case ACCOUNTS_ID:
                id = dh.addAccount(values);
                getContext().getContentResolver().notifyChange(uri, null);
                CardsFragment.accountChanged = true;
                return Uri.parse(PATH_ACCOUNTS + "/" + id);
            case TRANSACTIONS_ID:
                id = dh.addTransaction(values);
                getContext().getContentResolver().notifyChange(uri, null);
                CardsFragment.transactionChanged = true;
                return Uri.parse(PATH_TRANSACTIONS + "/" + id);
            case CATEGORIES_ID:
                id = dh.addCategory(values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(PATH_CATEGORIES + "/" + id);
            case SUBCATEGORIES_ID:
                id = dh.addSubCategory(values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(PATH_SUBCATEGORIES + "/" + id);
            case PLANS_ID:
                id = dh.addPlan(values);
                getContext().getContentResolver().notifyChange(uri, null);
                CardsFragment.planChanged = true;
                return Uri.parse(PATH_PLANS + "/" + id);
            case NOTIFICATIONS_ID:
                id = dh.addNotification(values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(PATH_NOTIFICATIONS + "/" + id);
            case LINK_ID:
                // TODO Need to handle LinksActivity eventually
            default:
                throw new IllegalArgumentException("MyContentProvider-insert: Unknown URI");
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause,
                      String[] whereArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsUpdated;

        switch (uriType) {
            case TRANSACTION_ID:
                Log.d(getClass().getSimpleName(), "Updating transaction & account information");
                //rowsUpdated = dh.updateAccount(values,whereClause,whereArgs);
                rowsUpdated = dh.updateTransaction(values, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                getContext().getContentResolver().notifyChange(ACCOUNTS_URI, null);
                CardsFragment.transactionChanged = true;
                break;
            case ACCOUNT_ID:
                Log.d(getClass().getSimpleName(), "Updating account information");
                rowsUpdated = dh.updateAccount(values, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                CardsFragment.accountChanged = true;
                break;
            case CATEGORY_ID:
                Log.d(getClass().getSimpleName(), "Updating category information");
                rowsUpdated = dh.updateCategory(values, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case SUBCATEGORY_ID:
                Log.d(getClass().getSimpleName(), "Updating subcategory information");
                rowsUpdated = dh.updateSubCategory(values, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case PLAN_ID:
                Log.d(getClass().getSimpleName(), "Updating plan information");
                rowsUpdated = dh.updatePlan(values, whereClause, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                CardsFragment.planChanged = true;
                break;
            default:
                throw new IllegalArgumentException("MyContentProvider-update: Unknown URI");
        }

        return rowsUpdated;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        Log.d(getClass().getSimpleName(), "Tried to use getType method, but I didn't do anything but return null here...");
        return null;
    }

}//End MyContentProvider