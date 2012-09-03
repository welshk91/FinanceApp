package com.databases.example;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class SearchTime extends ListActivity {

	//Used in searching to id the last activity
	private String SEARCH_CONTEXT = "SearchTime.java";

	//Variables for the Database
	public final String tblAccounts = "tblAccounts";
	final String tblTrans = "tblTrans";
	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		handleIntent(getIntent()); 
	} 

	public void onNewIntent(Intent intent) { 
		setIntent(intent); 
		handleIntent(intent); 
	} 
	public void onListItemClick(ListView l, 
			View v, int position, long id) { 
		// call detail activity for clicked entry 
	} 
	private void handleIntent(Intent intent) { 

		Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
			SEARCH_CONTEXT = appData.getString("appData.key");
		}

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
			String query = 
					intent.getStringExtra(SearchManager.QUERY); 
			doSearch(query); 
		} 
	}    

	//The Method that determines which searching methods to call
	private void doSearch(String query) { 

		Toast.makeText(this, "Searching From " + SEARCH_CONTEXT, Toast.LENGTH_SHORT).show();

		if(SEARCH_CONTEXT.contains("ViewDB")){
			searchAccounts(query);
		}

		else if(SEARCH_CONTEXT.contains("Transactions")){
			searchTransactions(query);
		}

		else{
			searchAccounts(query);
			searchTransactions(query);
		}

	}//end doSearch

	//Override method to send the search extra data, letting it know which class called it
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString("appData.key", SEARCH_CONTEXT);
		startSearch(null, false, appData, false);
		return true;
	}

	//Method that searches for Accounts
	public void searchAccounts(String query){

		String sqlCommand = " SELECT * FROM " + tblAccounts + 
				" WHERE AcctName " + 
				" LIKE '%" + query + "%'" + 
				" UNION " + 
				" SELECT * FROM " + tblAccounts +
				" WHERE AcctBalance " + 
				" LIKE '%" + query + "%'" + 
				" UNION " + 
				" SELECT * FROM " + tblAccounts +
				" WHERE AcctDate " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblAccounts +
				" WHERE AcctTime " + 
				" LIKE '%" + query + "%'";

		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		Cursor c = myDB.rawQuery(sqlCommand, null);

		startManagingCursor(c);

		int id = 0;
		String name = null;
		String balance = null;
		String time = null;
		String date = null;

		c.moveToFirst();
		if(c!=null){
			if (c.isFirst()) {
				do{
					id = c.getInt(c.getColumnIndex("AcctID"));
					name = c.getString(c.getColumnIndex("AcctName"));
					balance = c.getString(c.getColumnIndex("AcctBalance"));
					time = c.getString(c.getColumnIndex("AcctTime"));
					date = c.getString(c.getColumnIndex("AcctDate"));
					Toast.makeText(this, "Id: "+ id + "\nName: " + name + "\nBalance: " + balance, Toast.LENGTH_LONG).show();
				}while(c.moveToNext());
			}
			else{
				Toast.makeText(this, "No Search Results for " + query, Toast.LENGTH_SHORT).show();
			}
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}


		return;
	}//end searchAccounts


	//Method that searches for Transactions
	public void searchTransactions(String query){

		String sqlCommand = " SELECT * FROM " + tblTrans + 
				" WHERE TransName " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransValue " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransDate " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransTime " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransMemo " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransCheckNum " + 
				" LIKE '%" + query + "%'";

		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		Cursor c = myDB.rawQuery(sqlCommand, null);

		//Toast.makeText(this, "Searching From " + Boolean.toString(jargon), Toast.LENGTH_LONG).show();

		startManagingCursor(c);

		c.moveToFirst();
		if(c!=null){
			if (c.isFirst()) {
				do{
					int id = c.getInt(c.getColumnIndex("TransID"));
					int acctId = c.getInt(c.getColumnIndex("ToAcctID"));
					String name = c.getString(c.getColumnIndex("TransName"));
					String value = c.getString(c.getColumnIndex("TransValue"));
					String type = c.getString(c.getColumnIndex("TransType"));
					String category = c.getString(c.getColumnIndex("TransCategory"));
					String checknum = c.getString(c.getColumnIndex("TransCheckNum"));
					String memo = c.getString(c.getColumnIndex("TransMemo"));
					String time = c.getString(c.getColumnIndex("TransTime"));
					String date = c.getString(c.getColumnIndex("TransDate"));
					String cleared = c.getString(c.getColumnIndex("TransCleared"));

					Toast.makeText(this, "Id: "+ id + "\nToAcctID: "+ acctId + "\nName: " + name + "\nValue: " + value, Toast.LENGTH_LONG).show();
				}while(c.moveToNext());
			}
			else{
				Toast.makeText(this, "No Search Results for " + query, Toast.LENGTH_SHORT).show();
			}
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		return;
	}//end searchTransactions

}//end SearchTime
