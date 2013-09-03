package com.databases.example;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class MyContentProvider extends ContentProvider{

	// Contacts table name
	private static DatabaseHelper dh = null;

	//IDs
	public static final int ACCOUNTS_ID = 100;
	public static final int ACCOUNT_ID = 110;
	public static final int ACCOUNT_SEARCH_ID = 120;
	public static final int TRANSACTIONS_ID = 200;
	public static final int TRANSACTION_ID = 210;
	public static final int TRANSACTION_SEARCH_ID = 220;
	public static final int CATEGORIES_ID = 300;
	public static final int CATEGORY_ID = 310;
	public static final int SUBCATEGORIES_ID = 400;
	public static final int SUBCATEGORY_ID = 410;
	public static final int PLANNED_TRANSACTIONS_ID = 500;
	public static final int PLANNED_TRANSACTION_ID = 510;
	public static final int LINKS_ID = 600;
	public static final int LINK_ID = 610;

	private static final String AUTHORITY = "com.databases.example.provider";
	private static final String PATH_ACCOUNTS = "accounts";
	private static final String PATH_TRANSACTIONS = "transactions";
	private static final String PATH_CATEGORIES = "categories";
	private static final String PATH_SUBCATEGORIES = "subcategories";
	private static final String PATH_PLANNED_TRANSACTIONS = "plannedTransactions";
	private static final String PATH_LINKS = "links";

	public static final Uri ACCOUNTS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + PATH_ACCOUNTS);
	public static final Uri TRANSACTIONS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + PATH_TRANSACTIONS);
	public static final Uri CATEGORIES_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + PATH_CATEGORIES);
	public static final Uri SUBCATEGORIES_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + PATH_SUBCATEGORIES);
	public static final Uri PLANNED_TRANSACTIONS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + PATH_PLANNED_TRANSACTIONS);
	public static final Uri LINKS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + PATH_LINKS);

	//public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
	//        + "/mt-tutorial";
	//public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
	//       + "/mt-tutorial";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static{
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
		sURIMatcher.addURI(AUTHORITY, PATH_PLANNED_TRANSACTIONS, PLANNED_TRANSACTIONS_ID);
		sURIMatcher.addURI(AUTHORITY, PATH_PLANNED_TRANSACTIONS + "/#", PLANNED_TRANSACTION_ID);
		sURIMatcher.addURI(AUTHORITY, PATH_LINKS, LINKS_ID);
		sURIMatcher.addURI(AUTHORITY, PATH_LINKS + "/#", LINK_ID);
	}

	@Override
	public boolean onCreate(){
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
			cursor = dh.getAccounts();
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
		case PLANNED_TRANSACTIONS_ID:
			cursor = dh.getPlannedTransactionsAll();
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case PLANNED_TRANSACTION_ID:
			cursor = dh.getPlannedTransaction(uri.getLastPathSegment());
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case LINKS_ID:
			cursor = dh.getAccounts();
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case LINK_ID:
			cursor = dh.getAccount(uri.getLastPathSegment());
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}

	}

	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		int uriType = sURIMatcher.match(uri);
		int rowsDeleted = 0;

		switch (uriType) {
		case ACCOUNT_ID:
			rowsDeleted = dh.deleteAccount(uri, whereClause, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case TRANSACTION_ID:
			rowsDeleted = dh.deleteTransaction(uri,whereClause,whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case CATEGORY_ID:
			rowsDeleted = dh.deleteCategory(uri,whereClause,whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case SUBCATEGORY_ID:
			rowsDeleted = dh.deleteSubCategory(uri,whereClause,whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case PLANNED_TRANSACTION_ID:
			rowsDeleted = dh.deletePlannedTransaction(uri,whereClause,whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case LINK_ID:
			dh.getAccount(uri.getLastPathSegment());
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}		

		return rowsDeleted;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		long id = 0;
		switch (uriType) {
		case ACCOUNTS_ID:
			id = dh.addAccount(values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(PATH_ACCOUNTS + "/" + id);
		case TRANSACTIONS_ID:
			id = dh.addTransaction(values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(PATH_TRANSACTIONS + "/" + id);
		case CATEGORIES_ID:
			id = dh.addCategory(values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(PATH_CATEGORIES + "/" + id);
		case SUBCATEGORIES_ID:
			id = dh.addSubCategory(values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(PATH_SUBCATEGORIES + "/" + id);
		case PLANNED_TRANSACTIONS_ID:
			id = dh.addPlannedTransaction(values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(PATH_PLANNED_TRANSACTIONS + "/" + id);
		case LINK_ID:
			dh.getAccount(uri.getLastPathSegment());
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse(PATH_LINKS + "/" + id);
		default:
			throw new IllegalArgumentException("Unknown URI");
		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause,
			String[] whereArgs) {
		int uriType = sURIMatcher.match(uri);
		int rowsUpdated = 0;

		switch (uriType) {
		case TRANSACTION_ID:
			Log.e("MyContentProvider-update", "Updating transaction & account information");
			rowsUpdated = dh.updateAccount(values,whereClause,whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			getContext().getContentResolver().notifyChange(ACCOUNTS_URI, null);
			break;
		case ACCOUNT_ID:
			Log.e("MyContentProvider-update", "Updating account information");
			rowsUpdated = dh.updateAccount(values,whereClause,whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			//getContext().getContentResolver().notifyChange(ACCOUNTS_URI, null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}

		return rowsUpdated;

	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		Log.e("MyContentProvider-getType", "Tried to use getType method, but I didn't do anything but return null here...");
		return null;
	}

}