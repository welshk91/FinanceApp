package com.databases.example;

import android.app.TabActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class SearchMain extends TabActivity {

	//Used in searching to id the last activity
	private String SEARCH_CONTEXT = "SearchTime.java";
	private String query;

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
	//	public void onListItemClick(ListView l, 
	//			View v, int position, long id) { 
	//		// call detail activity for clicked entry 
	//	} 
	private void handleIntent(Intent intent) { 

		Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
			SEARCH_CONTEXT = appData.getString("appData.key");
		}

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
			query = intent.getStringExtra(SearchManager.QUERY);
			setTitle("Search <" + query + ">");
			makeView();
		}

	}    

	//Method that handles setting up the Tabs
	public void makeView(){
		setContentView(R.layout.search);
		//Toast.makeText(this, "SearchTime Query: " + query + "\nCaller: " + SEARCH_CONTEXT, Toast.LENGTH_SHORT).show();
		TabHost tabHost = new TabHost(this);
		tabHost = getTabHost();
		tabHost.setCurrentTab(0);
		tabHost.clearAllTabs();

		//The Intents called for tab content
		Intent intentAccounts = new Intent().setClass(this, SearchAccounts.class);
		Intent intentTransactions = new Intent().setClass(this, SearchTransactions.class);

		intentAccounts.putExtra("Query", query);
		intentTransactions.putExtra("Query", query);
		intentAccounts.putExtra("Caller", SEARCH_CONTEXT);
		intentTransactions.putExtra("Caller", SEARCH_CONTEXT);
		intentAccounts.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intentTransactions.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Accounts tab
		TabSpec tabSpecAccounts = null;
		tabSpecAccounts = tabHost
				.newTabSpec("Accounts")
				.setIndicator("Accounts")
				.setContent(intentAccounts);

		// Transactions tab
		TabSpec tabSpecTransactions = null;
		tabSpecTransactions = tabHost
				.newTabSpec("Transactions")
				.setIndicator("Transactions")
				.setContent(
						new Intent().setClass(this, SearchTransactions.class)
						.putExtra("Query", query)
						.putExtra("Caller", SEARCH_CONTEXT)
						);

		// add all tabs
		tabHost.setup();
		tabHost.addTab(tabSpecAccounts);
		tabHost.addTab(tabSpecTransactions);

		//set Windows tab as default (zero based)
		if(SEARCH_CONTEXT.contains("Account")){
			tabHost.setCurrentTab(0);
		}

		else if(SEARCH_CONTEXT.contains("Transaction")){
			tabHost.setCurrentTab(1);
		}

		else{
			tabHost.setCurrentTab(0);
		}

	}

	//Override method to send the search extra data, letting it know which class called it
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString("appData.key", SEARCH_CONTEXT);
		startSearch(null, false, appData, false);
		return true;
	}
	//Override method to send the search extra data, letting it know which class called it




}//end SearchTime