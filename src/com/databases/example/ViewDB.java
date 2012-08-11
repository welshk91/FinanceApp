package com.databases.example;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListView;

public class ViewDB extends Activity {

	int page;

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=1;
	int CONTEXT_MENU_EDIT=2;
	int CONTEXT_MENU_DELETE=3;

	//Adding
	Button StartDone_Button;
	Button StartBack_Button;
	EditText startName;
	EditText startTime;
	//Variables for the Account Table
	String accountName = null;
	String accountTime = null;
	String accountBalance = null;
	String accountDate = null;

	ListView lv = null;
	ArrayAdapter<String> adapter = null;

	Cursor c = null;
	final String tblAccounts = "tblAccounts";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;
	ArrayList<String> results = new ArrayList<String>();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.accounts);
		page = R.layout.accounts;

		lv = (ListView)findViewById(R.id.list);
		lv.setAdapter(adapter);

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		//Set Listener for regular mouse click
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				int selectionRowID = (int) adapter.getItemId(position);
				String item = (String) adapter.getItem(position);

				//Toast.makeText(ViewDB.this, "Click\nRow: " + selectionRowID + "\nEntry: " + item, 4000).show();
				if (item.contains("BACK")) {
					// Refresh
					Toast.makeText(ViewDB.this, " Going Back... ", 3000).show();
					finish();
				}

				else{

					//NOTE: LIMIT *position*,*how many after*
					String sqlCommand = "SELECT * FROM " + tblAccounts + " WHERE ID IN (SELECT ID FROM (SELECT ID FROM " + tblAccounts + " LIMIT " + (selectionRowID-1) + ",1)AS tmp)";

					myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

					Cursor c = myDB.rawQuery(sqlCommand, null);
					startManagingCursor(c);
					
					int entry_id = 0;
					String entry_name = null;
					String entry_balance = null;
					String entry_time = null;
					String entry_date = null;

					c.moveToFirst();
					do{
						entry_id = c.getInt(0);
						entry_name = c.getString(1);
						entry_balance = c.getString(2);
						entry_time = c.getString(3);
						entry_date = c.getString(4);
						Toast.makeText(ViewDB.this, "ID: "+entry_id+"\nName: "+entry_name+"\nBalance: "+entry_balance+"\nTime: "+entry_time+"\nDate: "+entry_date, 2000).show();
					}while(c.moveToNext());

					//Close Database if Open
					if (myDB != null){
						myDB.close();
					}

					//Call an Intent to go to Transactions Class
					Intent i = new Intent(ViewDB.this, Transactions.class);
					i.putExtra("ID", entry_id);
					i.putExtra("name", entry_name);
					i.putExtra("balance", entry_balance);
					i.putExtra("time", entry_time);
					i.putExtra("date", entry_date);
					startActivity(i);
				}

			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener


		//Allows Context Menus for each item of the list view
		registerForContextMenu(lv);

		//Buttons
		Button addAccount = (Button)findViewById(R.id.footerAdd); 
		addAccount.setOnClickListener(buttonListener);

		populate();

	}// end onCreate

	//Method called after creation, populates list with account information
	protected void populate() {
		//Add A back button. Might want to change this to a menu button, as you'd have to scroll up if list is big
		results = new ArrayList<String>();
		results.add(" BACK ");

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		c = myDB.query(tblAccounts, new String[] { "Name", "Balance", "Time", "Date" }, null,
				null, null, null, null);
		startManagingCursor(c);
		int NameColumn = c.getColumnIndex("Name");
		int BalanceColumn = c.getColumnIndex("Balance");
		int TimeColumn = c.getColumnIndex("Time");
		int DateColumn = c.getColumnIndex("Date");

		c.moveToFirst();
		if (c != null) {
			if (c.isFirst()) {
				do {
					String Name = c.getString(NameColumn);
					String Balance = c.getString(BalanceColumn);
					String Time = c.getString(TimeColumn);
					String Date = c.getString(DateColumn);
					if (Name != null && Balance != null && Time != null && Date != null && 
							Name != "" && Balance != "" && Time != "" && Date != "") {
						results.add(Name + ", "
								+ Balance + ", " + Time + ", " + Date);
					}
				} while (c.moveToNext());
			}
		} 

		else {
			results.add(" DATABASE EMPTY!!! ");
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		//adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, results);
		adapter = new ArrayAdapter<String>(this,R.layout.account_item, results);
		lv.setAdapter(adapter);
	}

	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
		String name = "" + adapter.getItem(itemInfo.position);

		menu.setHeaderTitle(name);  
		menu.add(0, CONTEXT_MENU_OPEN, 0, "Open");  
		menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit");
		menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");
	}  

	@Override  
	public boolean onContextItemSelected(MenuItem item) {

		if(item.getTitle()=="Open"){
			accountOpen(item);
		}  
		else if(item.getTitle()=="Edit"){
			accountEdit(item);
		}
		else if(item.getTitle()=="Delete"){
			accountDelete(item);
		}
		else {
			System.out.print("ERROR on ContextMenu; function not found");
			return false;
		}  

		return true;  
	}  

	//For Opening an Account
	public void accountOpen(MenuItem item){  
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		Toast.makeText(this, "Opened Item:\n" + itemName, Toast.LENGTH_SHORT).show();  
	}  

	//For Editing an Account
	public void accountEdit(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		Toast.makeText(this, "Editing Item:\n" + itemName, Toast.LENGTH_SHORT).show();  
	}

	//For Deleting an Account
	public void accountDelete(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		//Needed to skip code if you try to delete "BACK" entry
		if(itemInfo.position>=1){

			//NOTE: LIMIT *position*,*how many after*
			String sqlCommand = "DELETE FROM " + tblAccounts + " WHERE ID IN (SELECT ID FROM (SELECT ID FROM " + tblAccounts + " LIMIT " + (itemInfo.position-1) + ",1)AS tmp);";

			//Open Database
			myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

			myDB.execSQL(sqlCommand);
			//Toast.makeText(this, "SQL\n" + sqlCommand, 5000).show();

			//Close Database if Opened
			if (myDB != null){
				myDB.close();
			}

			results.remove(itemInfo.position);
			adapter.notifyDataSetChanged();

			Toast.makeText(this, "Deleted Item:\n" + itemName, Toast.LENGTH_SHORT).show();
		}

	}//end of accountDelete

	//Method for handling Button 'mouse-clicks'
	public OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {
			//If the Add button on the Account list is pressed
			case R.id.footerAdd:
				//code here for add button
				page = R.layout.start;
				break;

				//If the Transfer button on the Account list is pressed
			case R.id.footerTransfer:
				//code here for transfer button
				Toast.makeText(ViewDB.this, "Transfer Pressed", Toast.LENGTH_SHORT).show();
				break;

				//If the unknown button on the Account list is pressed
			case R.id.footerUnknown:
				//code here for unknown button
				Toast.makeText(ViewDB.this, "Unknown Pressed", Toast.LENGTH_SHORT).show();
				break;

				//If The Done button is pressed on the Add Account page
			case R.id.StartDone:
				accountName = startName.getText().toString().trim();
				accountBalance = startTime.getText().toString().trim();

				//Open Database
				myDB = ViewDB.this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

				if(Calendar.getInstance().get(Calendar.AM_PM)==1){
					accountTime = Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+ " PM";
				}
				else{
					accountTime = Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+ " AM";
				}				

				accountDate = Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR);
				if (accountName != null && accountTime != null && accountDate != null
						&& accountName != " " && accountTime != " " && accountDate != " ") {
					myDB.execSQL("INSERT INTO " + tblAccounts
							+ " (Name, Balance, Time, Date)" + " VALUES ('"
							+ accountName + "', '" + accountBalance + "', '" + accountTime + "', '"
							+ accountDate + "');");
					page = R.layout.accounts;
				} 

				else {
					Toast.makeText(ViewDB.this, " No Nulls Allowed ", 3000).show();
				}

				//Close Database if Opened
				if (myDB != null){
					myDB.close();
				}

				break;

				//If Back pressed on Start
			case R.id.StartBack:
				page = R.layout.accounts;
				break;

			}

			switch (page) {

			//Going to Accounts
			case R.layout.accounts:
				ViewDB.this.onCreate(null);

				break;

				//Going to Add Account
			case R.layout.start:
				setContentView(R.layout.start);
				StartDone_Button = (Button) findViewById(R.id.StartDone);
				StartDone_Button.setOnClickListener(buttonListener);
				StartBack_Button = (Button) findViewById(R.id.StartBack);
				StartBack_Button.setOnClickListener(buttonListener);
				startName = (EditText) findViewById(R.id.EditTextName);
				startTime = (EditText) findViewById(R.id.EditTextStart);

				break;
			}
		}
	};//end of buttonListener

	/*
	 * Handle closing database properly to avoid corruption
	 * */
	@Override
	public void onDestroy() {
		if (myDB != null){
			myDB.close();
		}
		super.onDestroy();
	}


	//For Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.account_menu, menu);
		return true;
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.account_menu_logout:     
			Toast.makeText(this, "You pressed Logout!", Toast.LENGTH_LONG).show();
			this.finish();
			this.moveTaskToBack(true);
			super.onDestroy();
			break;

		case R.id.account_menu_options:    
			Toast.makeText(this, "You pressed Options!", Toast.LENGTH_LONG).show();
			break;

		case R.id.account_menu_help:    
			Toast.makeText(this, "You pressed Help!", Toast.LENGTH_LONG).show();
			break;
		}
		return true;
	}

}// end ViewDB
