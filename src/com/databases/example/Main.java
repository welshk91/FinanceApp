package com.databases.example;

import java.util.ArrayList;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Main extends SherlockActivity {	
	//Variables for the Views
	Button Checkbook_Button;
	Button Manage_Button;
	Button Exit_Button;

	//Variables for the Database
	public final String tblAccounts = "tblAccounts";
	final String tblTrans = "tblTrans";
	final String tblCategory = "tblCategory";
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
				createDatabase();
				Intent intentSchedules = new Intent(Main.this, Accounts.class);
				startActivity(intentSchedules);
				break;

			case R.id.dashboard_statistics:
				createDatabase();
				Intent intentStats = new Intent(Main.this, Accounts.class);
				startActivity(intentStats);
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
		setContentView(R.layout.main);
		Checkbook_Button = (Button) findViewById(R.id.dashboard_checkbook);
		Checkbook_Button.setOnClickListener(buttonListener);
		Manage_Button = (Button) findViewById(R.id.dashboard_manage);
		Manage_Button.setOnClickListener(buttonListener);
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

				//Create database and open
				myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

				//Create Accounts table
				myDB.execSQL(sqlCommandAccounts);

				//Create Transactions table
				myDB.execSQL(sqlCommandTransactions);

				//Create Category table
				myDB.execSQL(sqlCommandCategory);

				//Add some default categories
				final String sqlDefaultCategories = "INSERT INTO " + tblCategory
						+ " (CateName)" + " VALUES ('Gas');";

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

		case R.id.main_menu_logout:
			Toast.makeText(this, "You pressed Logout!", Toast.LENGTH_SHORT).show();
			this.finish();
			this.moveTaskToBack(true);
			super.onDestroy();
			break;

		case R.id.main_menu_options:    
			//Toast.makeText(this, "You pressed Options!", Toast.LENGTH_SHORT).show();
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

}// end Main