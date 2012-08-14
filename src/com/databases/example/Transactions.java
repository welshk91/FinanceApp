package com.databases.example;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Transactions extends Activity{

	int page;

	//Variables for the transaction Table
	String transactionName = null;
	String transactionTime = null;
	String transactionBalance = null;
	String transactionDate = null;

	//Text Area for Adding Accounts
	EditText tName;
	EditText tBalance;

	//Variables for the Transactions Table
	String transName = null;
	String accountTime = null;
	String transBalance = null;
	String accountDate = null;

	int account_id;
	String account_name;
	String account_balance;
	String account_date;
	String account_time;

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=1;
	int CONTEXT_MENU_EDIT=2;
	int CONTEXT_MENU_DELETE=3;

	ListView lv = null;
	ArrayAdapter<String> adapter = null;

	Cursor c = null;
	final String tblTrans = "tblTrans";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;
	ArrayList<String> results = new ArrayList<String>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.transactions);

		lv = (ListView)findViewById(R.id.list);
		lv.setAdapter(adapter);

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		//No Parameters used in calling Intent
		if(getIntent().getExtras()==null){
			Toast.makeText(this, "Could Not Find Account Information", 5000).show();
			return;
		}

		account_id = getIntent().getExtras().getInt("ID");
		account_name = getIntent().getExtras().getString("name");
		account_balance = getIntent().getExtras().getString("balance");
		account_date = getIntent().getExtras().getString("date");
		account_time = getIntent().getExtras().getString("time");

		Toast.makeText(this, "ID: "+account_id+"\nName: "+account_name+"\nBalance: "+account_balance+"\nTime: "+account_time+"\nDate: "+account_date, 2000).show();

		//Set Listener for regular mouse click
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				int selectionRowID = (int) adapter.getItemId(position);
				String item = (String) adapter.getItem(position);

				Toast.makeText(Transactions.this, "Click\nRow: " + selectionRowID + "\nEntry: " + item, 4000).show();
				if (item.contains("BACK")) {
					// Refresh
					Toast.makeText(Transactions.this, " Going Back... ", 3000).show();
					finish();
				}

			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener


		//Allows Context Menus for each item of the list view
		registerForContextMenu(lv);

		//Buttons
		Button addTransaction = (Button)findViewById(R.id.transaction_footer_Add); 
		addTransaction.setOnClickListener(buttonListener);
		Button scheduleTransaction = (Button)findViewById(R.id.transaction_footer_Schedule); 
		scheduleTransaction.setOnClickListener(buttonListener);
		Button unknownTransaction = (Button)findViewById(R.id.transaction_footer_Unknown); 
		unknownTransaction.setOnClickListener(buttonListener);

		populate();

	}//end onCreate


	//Populate view with all the transactions of selected account
	protected void populate(){
		//Add A back button. Might want to change this to a menu button, as you'd have to scroll up if list is big
		results = new ArrayList<String>();
		results.add(" BACK ");

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		c = myDB.query(tblTrans, new String[] { "TransID", "TransAmt", "ToAcctID"}, "ToAcctID = " + account_id,
				null, null, null, null);

		startManagingCursor(c);
		int idColumn = c.getColumnIndex("TransID");
		int amtColumn = c.getColumnIndex("TransAmt");
		int acctColumn = c.getColumnIndex("ToAcctID");

		c.moveToFirst();
		if (c != null) {
			if (c.isFirst()) {
				do {
					String Name = c.getString(idColumn);
					String Balance = c.getString(amtColumn);
					String Time = c.getString(acctColumn);
					if (Name != null && Balance != null && Time != null && 
							Name != "" && Balance != "" && Time != "") {
						results.add(Name + ", "
								+ Balance + ", " + Time);
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
		adapter = new ArrayAdapter<String>(this,R.layout.transaction_item, results);
		lv.setAdapter(adapter);

	}//end populate

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
			transactionOpen(item);
		}  
		else if(item.getTitle()=="Edit"){
			transactionEdit(item);
		}
		else if(item.getTitle()=="Delete"){
			transactionDelete(item);
		}
		else {
			System.out.print("ERROR on ContextMenu; function not found");
			return false;
		}  

		return true;  
	}

	//For Opening an Account
	public void transactionOpen(MenuItem item){  
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		Toast.makeText(this, "Opened Item:\n" + itemName, Toast.LENGTH_SHORT).show();  
	}  

	//For Editing an Account
	public void transactionEdit(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		Toast.makeText(this, "Editing Item:\n" + itemName, Toast.LENGTH_SHORT).show();  
	}

	//For Deleting an Account
	public void transactionDelete(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		//Needed to skip code if you try to delete "BACK" entry
		if(itemInfo.position>=1){

			//NOTE: LIMIT *position*,*how many after*
			//NOT PERFECT!!! HAD SOME CASES WHERE DELETE DIDNT KILL IT
			String sqlCommand = "DELETE FROM " + tblTrans + " WHERE TransID IN (SELECT TransID FROM (SELECT TransID FROM " + tblTrans + " LIMIT " + (itemInfo.position-1) + ",1)AS tmp);";

			//Open Database
			myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

			myDB.execSQL(sqlCommand);
			Toast.makeText(this, "SQL\n" + sqlCommand, 3000).show();

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
			case R.id.transaction_footer_Add:
				//code here for add button
				page = R.layout.transaction_add;
				break;

				//If the Transfer button on the Account list is pressed
			case R.id.transaction_footer_Schedule:
				//code here for transfer button
				Toast.makeText(Transactions.this, "Schedule Pressed", Toast.LENGTH_SHORT).show();
				break;

				//If the unknown button on the Account list is pressed
			case R.id.transaction_footer_Unknown:
				//code here for unknown button
				Toast.makeText(Transactions.this, "Unknown Pressed", Toast.LENGTH_SHORT).show();
				break;

			}//end Switch ViewByID

			switch (page) {

			//Going to Accounts
			case R.layout.transactions:
				Transactions.this.onCreate(null);

				break;

				//Going to Add Account
			case R.layout.transaction_add:

				// get transaction_add.xml view
				LayoutInflater li = LayoutInflater.from(Transactions.this);
				final View promptsView = li.inflate(R.layout.transaction_add, null);

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						Transactions.this);

				// set account_add.xml to AlertDialog builder
				alertDialogBuilder.setView(promptsView);

				//set Title
				alertDialogBuilder.setTitle("Add A Transaction");

				// set dialog message
				alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// CODE FOR "OK"

						tName = (EditText) promptsView.findViewById(R.id.EditAccountName);
						tBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
						transName = tName.getText().toString().trim();
						transBalance = tBalance.getText().toString().trim();

						//						//Open Database
						//						myDB = Transactions.this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
						//
						//						if(Calendar.getInstance().get(Calendar.AM_PM)==1){
						//							accountTime = Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+ " PM";
						//						}
						//						else{
						//							accountTime = Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+ " AM";
						//						}				
						//
						//						accountDate = Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR);
						//						if (accountName != null && accountTime != null && accountDate != null
						//								&& accountName != " " && accountTime != " " && accountDate != " ") {
						//							myDB.execSQL("INSERT INTO " + tblTrans
						//									+ " (Name, Balance, Time, Date)" + " VALUES ('"
						//									+ accountName + "', '" + accountBalance + "', '" + accountTime + "', '"
						//									+ accountDate + "');");
						//							page = R.layout.accounts;
						//						} 
						//
						//						else {
						//							Toast.makeText(Transactions.this, " No Nulls Allowed ", 3000).show();
						//						}
						//
						//						//Close Database if Opened
						//						if (myDB != null){
						//							myDB.close();
						//						}
						//
						//						Transactions.this.populate();

					}//end onClick "OK"
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// CODE FOR "Cancel"
						dialog.cancel();
					}
				});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();

				break;
			}
		}
	};//end of buttonListener

	//For Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.transaction_menu, menu);
		return true;
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.transaction_menu_logout:     
			Toast.makeText(this, "You pressed Logout!", Toast.LENGTH_SHORT).show();
			this.finish();
			this.moveTaskToBack(true);
			super.onDestroy();
			break;

		case R.id.transaction_menu_options:    
			Toast.makeText(this, "You pressed Options!", Toast.LENGTH_SHORT).show();
			break;

		case R.id.transaction_menu_help:    
			Toast.makeText(this, "You pressed Help!", Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}

}//end Transactions
