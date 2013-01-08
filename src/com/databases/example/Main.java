package com.databases.example;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import java.util.ArrayList;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Main extends SherlockActivity {	
	// this is your preferred flag
	private static final int _ReqCreatePattern = 0;
	// this is your preferred flag
	private static final int _ReqSignIn = 1;
	//String savedPattern = null;

	//Variables for the Views
	Button Checkbook_Button;
	Button Manage_Button;
	Button Stats_Button;
	Button Schedule_Button;
	Button Exit_Button;

	//Variables for the Database
	public final String tblAccounts = "tblAccounts";
	final String tblTrans = "tblTrans";
	final String tblCategory = "tblCategory";
	final String tblLinks = "tblLinks";
	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;

	//Variables for the Account Table
	String accountName = null;
	String accountTime = null;
	String accountBalance = null;
	String accountDate = null;

	//Variables for the ListView
	public ArrayList<String> results = new ArrayList<String>();

	//Method handling 'mouse-click'
	public OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {

			case R.id.dashboard_checkbook:
				createDatabase();
				Intent intentCheckbook = new Intent(Main.this, Accounts.class);
				startActivity(intentCheckbook);
				break;

			case R.id.dashboard_manage:
				//	createDatabase();
				Intent intentManage = new Intent(Main.this, Manage.class);
				startActivity(intentManage);
				break;

			case R.id.dashboard_schedules:
				//	createDatabase();
				//Intent intentSchedules = new Intent(Main.this, Accounts.class);
				//startActivity(intentSchedules);
				confirmPattern();
				break;

			case R.id.dashboard_statistics:
				//	createDatabase();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();
				break;

			case R.id.dashboard_exit:
				if (myDB != null){
					myDB.close();
				}

				Main.this.finish();
				onDestroy();
				break;

			}

		}// end onClick
	};// end onClickListener

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//For Clear preferences!!!!!! REMOVE EVENTUALLY
		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		//Editor editor = settings.edit();
		//editor.clear();
		//editor.commit();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
		boolean lockEnabled = prefs.getBoolean("checkbox_lock_enabled", false);
		
		if(lockEnabled){
			confirmPattern();
		}

		setContentView(R.layout.main);

		Checkbook_Button = (Button) findViewById(R.id.dashboard_checkbook);
		Checkbook_Button.setOnClickListener(buttonListener);
		Manage_Button = (Button) findViewById(R.id.dashboard_manage);
		Manage_Button.setOnClickListener(buttonListener);
		Schedule_Button = (Button) findViewById(R.id.dashboard_schedules);
		Schedule_Button.setOnClickListener(buttonListener);
		Stats_Button = (Button) findViewById(R.id.dashboard_statistics);
		Stats_Button.setOnClickListener(buttonListener);
		Exit_Button = (Button) findViewById(R.id.dashboard_exit);
		Exit_Button.setOnClickListener(buttonListener);

	}// end onCreate

	//Over-rode method to handle database closing, prevent corruption
	@Override
	public void onDestroy() {

		//Close database to avoid corruption/leaks
		if (myDB != null){
			myDB.close();
		}

		//Exit
		super.onDestroy();
	}

	//Method for Creating Database
	public void createDatabase(){
		/*
		 * Initialize database and tables
		 */

		//If this is the first time running program...
		if(true){
			try {

				String sqlCommandAccounts = "CREATE TABLE IF NOT EXISTS "
						+ tblAccounts
						+ " (AcctID INTEGER PRIMARY KEY, AcctName VARCHAR, AcctBalance VARCHAR, AcctTime VARCHAR, AcctDate VARCHAR);";

				String sqlCommandTransactions = "CREATE TABLE IF NOT EXISTS "
						+ tblTrans
						+ " (TransID INTEGER PRIMARY KEY, ToAcctID VARCHAR, TransName VARCHAR, TransValue VARCHAR, TransType VARCHAR, TransCategory VARCHAR, TransCheckNum VARCHAR, TransMemo VARCHAR, TransTime VARCHAR, TransDate VARCHAR, TransCleared);";

				String sqlCommandCategory = "CREATE TABLE IF NOT EXISTS "
						+ tblCategory
						+ " (CateID INTEGER PRIMARY KEY, CateName VARCHAR);";

				String sqlCommandLinks = "CREATE TABLE IF NOT EXISTS "
						+ tblLinks
						+ " (LinkID INTEGER PRIMARY KEY, ToID VARCHAR, LinkName VARCHAR, LinkMemo VARCHAR, ParentType VARCHAR);";

				//Create database and open
				myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

				//Create Accounts table
				myDB.execSQL(sqlCommandAccounts);

				//Create Transactions table
				myDB.execSQL(sqlCommandTransactions);

				//Create Category table
				myDB.execSQL(sqlCommandCategory);

				//Create Category table
				myDB.execSQL(sqlCommandLinks);

				//Add some default categories
				final String sqlDefaultCategories = "INSERT INTO " + tblCategory
						+ " (CateName)" + " VALUES ('STARTING BALANCE', 'Gift',  Gas', 'Rent', 'Utilities');";

				//myDB.execSQL(sqlDefaultCategories);

			} 
			catch (Exception e) {
				Toast.makeText(this, "Error Creating Database!!!\n\n" + e, Toast.LENGTH_LONG).show();
			}

		}//end if

		//Make sure Database is closed even if try-catch fails
		if (myDB != null){
			myDB.close();
		}

	}//end createDatabase

	//For Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);
		return true;
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main_menu_search:    
			onSearchRequested();
			break;

		case R.id.main_menu_links:    
			//	Toast.makeText(this, "You pressed Links!", Toast.LENGTH_SHORT).show();
			Intent intentLinks = new Intent(Main.this, Links.class);
			startActivity(intentLinks);			
			break;

		case R.id.main_menu_logout:
			Toast.makeText(this, "You pressed Logout!", Toast.LENGTH_SHORT).show();
			this.finish();
			this.moveTaskToBack(true);
			super.onDestroy();
			break;

		case R.id.main_menu_options:    
			Intent v = new Intent(Main.this, Options.class);
			startActivity(v);
			break;

		case R.id.main_menu_help:    
			Toast.makeText(this, "You pressed Help!", Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}

	//Override method to send the search extra data, letting it know which class called it
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		startSearch(null, false, appData, false);
		return true;
	}

	//Confirm Lockscreen
	public void confirmPattern(){
		Intent intent = new Intent(Main.this, LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.ComparePattern);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
		String savedPattern = prefs.getString("myPattern", null);
		
		if(savedPattern!=null){
			intent.putExtra(LockPatternActivity._Pattern, savedPattern);
			startActivityForResult(intent, _ReqSignIn);
		}
		else{
			Toast.makeText(Main.this, "Cannot Use Lockscreen\nNo Pattern Set Yet", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case _ReqSignIn:
			if (resultCode == RESULT_OK) {
				// signing in ok
				Toast.makeText(Main.this, "Sign In\nAccepted", Toast.LENGTH_SHORT).show();
			} else {
				// signing in failed
				Toast.makeText(Main.this, "Sign In\nFailed", Toast.LENGTH_SHORT).show();
				this.finish();
				this.moveTaskToBack(true);
				super.onDestroy();
			}
			break;

		}

	}

}// end Main 