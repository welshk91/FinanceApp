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

	//The Method that handles the Searching
	private void doSearch(String queryStr) { 

		if(SEARCH_CONTEXT.contains("ViewDB")){
			Toast.makeText(this, "Searching From " + SEARCH_CONTEXT, Toast.LENGTH_LONG).show();
		}

		else if(SEARCH_CONTEXT.contains("Transactions")){
			Toast.makeText(this, "Searching From " + SEARCH_CONTEXT, Toast.LENGTH_LONG).show();
		}

		else{
			Toast.makeText(this, "Searching From " + SEARCH_CONTEXT, Toast.LENGTH_LONG).show();
		}









		String sqlCommand = " SELECT AcctID,AcctName,AcctBalance FROM " + tblAccounts + 
				" WHERE AcctName " + 
				" LIKE '%" + queryStr + "%'" + 
				" UNION " + 
				" SELECT TransID,TransName,TransValue FROM " + tblTrans +
				" WHERE TransName " + 
				" LIKE '%" + queryStr + "%'";

		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		Cursor c = myDB.rawQuery(sqlCommand, null);

		//Toast.makeText(this, "Searching From " + Boolean.toString(jargon), Toast.LENGTH_LONG).show();

		startManagingCursor(c);

		int entry_id = 0;
		String entry_name = null;
		String entry_balance = null;
		String entry_time = null;
		String entry_date = null;

		c.moveToFirst();
		if(c!=null){
			if (c.isFirst()) {
				do{
					entry_id = c.getInt(0);
					entry_name = c.getString(1);
					entry_balance = c.getString(2);
					//entry_time = c.getString(c.getColumnIndex("AcctTime"));
					//entry_date = c.getString(c.getColumnIndex("AcctDate"));
					Toast.makeText(this, "Id: "+ entry_id + "\nName: " + entry_name + "\nBalance: " + entry_balance, Toast.LENGTH_LONG).show();
				}while(c.moveToNext());
			}
			else{
				Toast.makeText(this, "No Search Results for " + queryStr, Toast.LENGTH_SHORT).show();
			}
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
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

}//end SearchTime
