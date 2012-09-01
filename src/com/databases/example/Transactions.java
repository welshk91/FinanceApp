package com.databases.example;

import java.util.ArrayList;
import java.util.Calendar;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;


public class Transactions extends FragmentActivity implements OnSharedPreferenceChangeListener{

	//The View
	int page;

	//Used to keep Track of total Balance
	float totalBalance;

	//Dialog for Adding Transaction
	static View promptsView;
	View transStatsView;

	//Widgets for Adding Accounts
	EditText tName;
	EditText tValue;
	Spinner tType;	
	Spinner tCategory;
	EditText tCheckNum;
	EditText tMemo;
	static Button tTime;
	static Button tDate;
	CheckBox tCleared;

	//TextView of Statistics
	TextView statsName;
	TextView statsValue;
	TextView statsType;
	TextView statsCategory;
	TextView statsCheckNum;
	TextView statsMemo;
	TextView statsCleared;
	TextView statsDate;
	TextView statsTime;
	CheckBox chkCleared;

	//Variables of the Account Used
	int account_id;

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=1;
	int CONTEXT_MENU_EDIT=2;
	int CONTEXT_MENU_DELETE=3;

	//ListView and Adapter
	ListView lv = null;
	ArrayAdapter<TransactionRecord> adapter = null;

	//Variables needed for traversing database
	Cursor c = null;
	final String tblTrans = "tblTrans";
	final String tblAccounts = "tblAccounts";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;
	ArrayList<TransactionRecord> results = new ArrayList<TransactionRecord>();

	//Variables for the transaction Table
	String transactionName = null;
	String transactionValue = null;
	String transactionType = null;
	String transactionCategory = null;
	String transactionCheckNum = null;
	String transactionMemo = null;
	static String transactionTime = null;
	static String transactionDate = null;
	String transactionCleared = null;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.transactions);

		lv = (ListView)findViewById(R.id.list);

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		//No Parameters used in calling Intent
		if(getIntent().getExtras()==null){
			Toast.makeText(this, "Could Not Find Account Information", Toast.LENGTH_LONG).show();
			return;
		}

		account_id = getIntent().getExtras().getInt("ID");
		//String account_name = getIntent().getExtras().getString("name");
		//String account_balance = getIntent().getExtras().getString("balance");
		//String account_date = getIntent().getExtras().getString("date");
		//String account_time = getIntent().getExtras().getString("time");

		//Toast.makeText(this, "ID: "+account_id+"\nName: "+account_name+"\nBalance: "+account_balance+"\nTime: "+account_time+"\nDate: "+account_date, Toast.LENGTH_SHORT).show();

		//Set Listener for regular mouse click
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				int selectionRowID = (int) adapter.getItemId(position);
				String item = adapter.getItem(position).name;

				Toast.makeText(Transactions.this, "Click\nRow: " + selectionRowID + "\nEntry: " + item, Toast.LENGTH_SHORT).show();

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

		//Set up a listener for changes in settings menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		//Populate List with Entries
		populate();

	}//end onCreate


	//Populate view with all the transactions of selected account
	protected void populate(){

		results = new ArrayList<TransactionRecord>();

		//Reset totalBalance
		totalBalance = 0;

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		c = myDB.query(tblTrans, new String[] { "TransID", "ToAcctID", "TransName", "TransValue", "TransType", "TransCategory","TransCheckNum", "TransMemo", "TransTime", "TransDate", "TransCleared"}, "ToAcctID = " + account_id,
				null, null, null, null);

		startManagingCursor(c);
		int idColumn = c.getColumnIndex("TransID");
		int acctIDColumn = c.getColumnIndex("ToAcctID");
		int nameColumn = c.getColumnIndex("TransName");
		int valueColumn = c.getColumnIndex("TransValue");
		int typeColumn = c.getColumnIndex("TransType");
		int categoryColumn = c.getColumnIndex("TransCategory");
		int checknumColumn = c.getColumnIndex("TransCheckNum");
		int memoColumn = c.getColumnIndex("TransMemo");
		int timeColumn = c.getColumnIndex("TransTime");
		int dateColumn = c.getColumnIndex("TransDate");
		int clearedColumn = c.getColumnIndex("TransCleared");

		c.moveToFirst();
		if (c != null) {
			if (c.isFirst()) {
				do {
					int id = c.getInt(idColumn);
					int acctId = c.getInt(acctIDColumn);
					String name = c.getString(nameColumn);
					String value = c.getString(valueColumn);
					String type = c.getString(typeColumn);
					String category = c.getString(categoryColumn);
					String checknum = c.getString(checknumColumn);
					String memo = c.getString(memoColumn);
					String time = c.getString(timeColumn);
					String date = c.getString(dateColumn);
					String cleared = c.getString(clearedColumn);

					TransactionRecord entry = new TransactionRecord(id, acctId, name, value,type,category,checknum,memo,time,date,cleared);
					results.add(entry);

					//Add account balance to total balance
					try{
						totalBalance = totalBalance + Float.parseFloat(value);
					}
					catch(Exception e){
						Toast.makeText(Transactions.this, "Could not calculate total balance", Toast.LENGTH_SHORT).show();

					}

				} while (c.moveToNext());
			}
		} 

		else {
			TransactionRecord tmp = new TransactionRecord(0,0,"DATABASE EMPTY",null,null,null,null,null,null,null,null);
			results.add(tmp);
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		//Set up an adapter for listView
		adapter = new UserItemAdapter(this, android.R.layout.simple_list_item_1, results);
		lv.setAdapter(adapter);

		//Refresh Balance
		calculateBalance(account_id);

	}//end populate

	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
		String name = "" + adapter.getItem(itemInfo.position).name;

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
		//Object itemName = adapter.getItem(itemInfo.position);

		String sqlCommand = "SELECT * FROM " + tblTrans + " WHERE TransID IN (SELECT TransID FROM (SELECT TransID FROM " + tblTrans + " LIMIT " + (itemInfo.position) + ",1)AS tmp)";

		myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		Cursor c = myDB.rawQuery(sqlCommand, null);
		startManagingCursor(c);

		int entry_id = 0;
		int entry_acctId = 0;
		String entry_name = null;
		String entry_value = null;
		String entry_type = null;
		String entry_category = null;
		String entry_checknum = null;
		String entry_memo = null;
		String entry_time = null;
		String entry_date = null;
		String entry_cleared = null;

		c.moveToFirst();
		do{
			entry_id = c.getInt(c.getColumnIndex("TransID"));
			entry_acctId = c.getInt(c.getColumnIndex("ToAcctID"));
			entry_name = c.getString(c.getColumnIndex("TransName"));
			entry_value = c.getString(c.getColumnIndex("TransValue"));
			entry_type = c.getString(c.getColumnIndex("TransType"));
			entry_category = c.getString(c.getColumnIndex("TransCategory"));
			entry_checknum = c.getString(c.getColumnIndex("TransCheckNum"));
			entry_memo = c.getString(c.getColumnIndex("TransMemo"));
			entry_time = c.getString(c.getColumnIndex("TransTime"));
			entry_date = c.getString(c.getColumnIndex("TransDate"));
			entry_cleared = c.getString(c.getColumnIndex("TransCleared"));
			//Toast.makeText(Transactions.this, "ID: "+entry_id+"\nName: "+entry_name+"\nBalance: "+entry_value+"\nTime: "+entry_time+"\nDate: "+entry_date, Toast.LENGTH_SHORT).show();
		}while(c.moveToNext());

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		// get transaction_add.xml view
		LayoutInflater li = LayoutInflater.from(Transactions.this);
		transStatsView = li.inflate(R.layout.transaction_stats, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Transactions.this);

		// set account_add.xml to AlertDialog builder
		alertDialogBuilder.setView(transStatsView);

		//set Title
		alertDialogBuilder.setTitle("View Transaction");

		// set dialog message
		alertDialogBuilder
		.setCancelable(true);

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		//Set Statistics
		statsName = (TextView)transStatsView.findViewById(R.id.TextTransactionName);
		statsName.setText(entry_name);
		statsValue = (TextView)transStatsView.findViewById(R.id.TextTransactionValue);
		statsValue.setText(entry_value);
		statsType = (TextView)transStatsView.findViewById(R.id.TextTransactionType);
		statsType.setText(entry_type);
		statsCategory = (TextView)transStatsView.findViewById(R.id.TextTransactionCategory);
		statsCategory.setText(entry_category);
		statsCheckNum = (TextView)transStatsView.findViewById(R.id.TextTransactionCheck);
		statsCheckNum.setText(entry_checknum);
		statsMemo = (TextView)transStatsView.findViewById(R.id.TextTransactionMemo);
		statsMemo.setText(entry_memo);
		statsDate = (TextView)transStatsView.findViewById(R.id.TextTransactionDate);
		statsDate.setText(entry_date);
		statsTime = (TextView)transStatsView.findViewById(R.id.TextTransactionTime);
		statsTime.setText(entry_time);
		statsCleared = (TextView)transStatsView.findViewById(R.id.TextTransactionCleared);
		statsCleared.setText(entry_cleared);

		// show it
		alertDialog.show();

	}  

	//For Editing an Account
	public void transactionEdit(MenuItem item){
		final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		//Object itemName = adapter.getItem(itemInfo.position);
		final int tID = adapter.getItem(itemInfo.position).id;
		final int aID = adapter.getItem(itemInfo.position).acctId;
		final String name = adapter.getItem(itemInfo.position).name;
		final String value = adapter.getItem(itemInfo.position).value;
		final String type = adapter.getItem(itemInfo.position).type;
		final String category = adapter.getItem(itemInfo.position).category;
		final String checknum = adapter.getItem(itemInfo.position).checknum;
		final String memo = adapter.getItem(itemInfo.position).memo;
		final String date = adapter.getItem(itemInfo.position).date;
		final String time = adapter.getItem(itemInfo.position).time;
		final String cleared = adapter.getItem(itemInfo.position).cleared;


		// get transaction_add.xml view
		LayoutInflater li = LayoutInflater.from(Transactions.this);
		promptsView = li.inflate(R.layout.transaction_add, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				Transactions.this);

		// set account_add.xml to AlertDialog builder
		alertDialogBuilder.setView(promptsView);

		//set Title
		alertDialogBuilder.setTitle("Edit A Transaction");

		//Set fields to old values
		tName = (EditText) promptsView.findViewById(R.id.EditTransactionName);
		tValue = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
		tType = (Spinner)promptsView.findViewById(R.id.spinner_transaction_type);
		tCategory = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);
		tCheckNum = (EditText)promptsView.findViewById(R.id.EditTransactionCheck);
		tMemo = (EditText)promptsView.findViewById(R.id.EditTransactionMemo);
		tDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);
		tTime = (Button)promptsView.findViewById(R.id.ButtonTransactionTime);
		tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);

		tName.setText(name);
		tValue.setText(value);
		ArrayAdapter<String> myAdap = (ArrayAdapter<String>) tType.getAdapter();
		int spinnerPosition = myAdap.getPosition(type);
		tType.setSelection(spinnerPosition);
		ArrayAdapter<String> myAdap2 = (ArrayAdapter<String>) tCategory.getAdapter();
		int spinnerPosition2 = myAdap2.getPosition(category);
		tCategory.setSelection(spinnerPosition2);
		tCheckNum.setText(checknum);
		tMemo.setText(memo);
		tCleared.setChecked(Boolean.parseBoolean(cleared));
		tDate.setText(date);
		tTime.setText(time);

		// set dialog message
		alertDialogBuilder
		.setCancelable(false)
		.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// CODE FOR "OK"
				transactionName = tName.getText().toString().trim();
				transactionValue = tValue.getText().toString().trim();
				transactionType = tType.getSelectedItem().toString().trim();
				transactionCategory = tCategory.getSelectedItem().toString().trim();
				transactionCheckNum = tCheckNum.getText().toString().trim();
				transactionMemo = tMemo.getText().toString().trim();
				transactionCleared = tCleared.isChecked()+"";
				transactionTime = tTime.getText().toString().trim();
				transactionDate = tDate.getText().toString().trim();

				try{
					String deleteCommand = "DELETE FROM " + tblTrans + " WHERE TransID = " + tID + ";";
					String insertCommand = "INSERT INTO " + tblTrans
							+ " (TransID, ToAcctID, TransName, TransValue, TransType, TransCategory, TransCheckNum, TransMemo, TransTime, TransDate, TransCleared)" + " VALUES ('"
							+ tID + "', '" + aID + "', '" + transactionName + "', '" + transactionValue + "', '" + transactionType + "', '" + transactionCategory + "', '" + transactionCheckNum + "', '" + transactionMemo + "', '" + transactionTime + "', '" + transactionDate + "', '" + transactionCleared + "');";

					//Open Database
					myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

					//Delete Old Record
					myDB.execSQL(deleteCommand);

					//Make new record with same ID
					myDB.execSQL(insertCommand);

					//Close Database if Opened
					if (myDB != null){
						myDB.close();
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this, "Error Editing Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
				}

				page = R.layout.transactions;

				Transactions.this.populate();

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


	}

	//For Deleting an Account
	public void transactionDelete(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position).name;

		//NOTE: LIMIT *position*,*how many after*
		String sqlCommand = "DELETE FROM " + tblTrans + " WHERE TransID IN (SELECT TransID FROM (SELECT TransID FROM " + tblTrans + " LIMIT " + (itemInfo.position) + ",1)AS tmp);";

		//Open Database
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		myDB.execSQL(sqlCommand);
		//Toast.makeText(this, "SQL\n" + sqlCommand, Toast.LENGTH_LONG).show();

		//Close Database if Opened
		if (myDB != null){
			myDB.close();
		}

		Transactions.this.populate();

		Toast.makeText(this, "Deleted Item:\n" + itemName, Toast.LENGTH_SHORT).show();

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
				promptsView = li.inflate(R.layout.transaction_add, null);

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

						tName = (EditText) promptsView.findViewById(R.id.EditTransactionName);
						tValue = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
						tType = (Spinner)promptsView.findViewById(R.id.spinner_transaction_type);
						tCategory = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);
						tCheckNum = (EditText)promptsView.findViewById(R.id.EditTransactionCheck);
						tMemo = (EditText)promptsView.findViewById(R.id.EditTransactionMemo);
						tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);

						transactionName = tName.getText().toString().trim();
						transactionValue = tValue.getText().toString().trim();
						transactionType = tType.getSelectedItem().toString().trim();
						transactionCategory = tCategory.getSelectedItem().toString().trim();
						transactionCheckNum = tCheckNum.getText().toString().trim();
						transactionMemo = tMemo.getText().toString().trim();
						transactionCleared = tCleared.isChecked()+"";

						String sqlCommand = "INSERT INTO " + tblTrans
								+ " (ToAcctID, TransName, TransValue, TransType, TransCategory, TransCheckNum, TransMemo, TransTime, TransDate, TransCleared)" + " VALUES ('"
								+ account_id + "', '" + transactionName + "', '" + transactionValue + "', '" + transactionType + "', '" + transactionCategory + "', '" + transactionCheckNum + "', '" + transactionMemo + "', '" + transactionTime + "', '" + transactionDate + "', '" + transactionCleared + "');";

						//Open Database
						myDB = Transactions.this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);				

						try{
							if (transactionName != null && transactionTime != null && transactionDate != null
									&& transactionName != " " && transactionTime != " " && transactionDate != " ") {
								myDB.execSQL(sqlCommand);	
							} 

							else {
								Toast.makeText(Transactions.this, " No Nulls Allowed ", Toast.LENGTH_LONG).show();
							}
						}
						catch(Exception e){
							Toast.makeText(Transactions.this, "Error Adding Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
						}

						//Close Database if Opened
						if (myDB != null){
							myDB.close();
						}

						page = R.layout.transactions;

						Transactions.this.populate();

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
			//Toast.makeText(this, "You pressed Options!", Toast.LENGTH_SHORT).show();
			Intent v = new Intent(Transactions.this, Options.class);
			startActivity(v);
			break;

		case R.id.transaction_menu_help:

			Toast.makeText(this, "You pressed Help!", Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}

	//Method to help create TimePicker
	public static class TimePickerFragment extends DialogFragment
	implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// Do something with the time chosen by the user
			transactionTime = hourOfDay + ":" + minute;
			tTime = (Button)promptsView.findViewById(R.id.ButtonTransactionTime);
			tTime.setText(transactionTime);
		}
	}

	//Method called to show the TimePicker when adding a transaction
	public void showTimePickerDialog(View v) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), "timePicker");
	}

	//Method to help create DatePicker
	public static class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			transactionDate = (month+1) + "/" + day + "/" + year;
			tDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);
			tDate.setText(transactionDate);
		}
	}

	//Method called to show DatePicker when adding transaction
	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	public class UserItemAdapter extends ArrayAdapter<TransactionRecord> {
		private ArrayList<TransactionRecord> transaction;

		public UserItemAdapter(Context context, int textViewResourceId, ArrayList<TransactionRecord> users) {
			super(context, textViewResourceId, users);
			this.transaction = users;
		}

		//Used to Define the View of each transaction
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			TransactionRecord user = transaction.get(position);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Transactions.this);
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);


			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.transaction_item, null);

				//Change Background Colors
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.transaction_layout);
					String startColor = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_startBackgroundColor), "#E8E8E8");
					String endColor = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_endBackgroundColor), "#FFFFFF");
					GradientDrawable defaultGradient = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {Color.parseColor(startColor),Color.parseColor(endColor)});
					//gd.setCornerRadius(0f);

					if(useDefaults){
						l.setBackgroundResource(R.drawable.transaction_list_style);
					}
					else{

						l.setBackgroundDrawable(defaultGradient);
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultSize = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_nameSize), "20");
					TextView t;
					t=(TextView)v.findViewById(R.id.transaction_name);

					if(useDefaults){
						t.setTextSize(20);
					}
					else{
						t.setTextSize(Integer.parseInt(DefaultSize));
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultColor = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_nameColor), "#000000");
					TextView t;
					t=(TextView)v.findViewById(R.id.transaction_name);

					if(useDefaults){
						t.setTextColor(Color.parseColor("#000000"));
					}
					else{
						t.setTextColor(Color.parseColor(DefaultColor));
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultSize = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_fieldSize), "10");
					TextView tmp;

					if(useDefaults){
						tmp=(TextView)v.findViewById(R.id.transaction_value);
						tmp.setTextSize(10);
						tmp=(TextView)v.findViewById(R.id.transaction_date);
						tmp.setTextSize(10);
						tmp=(TextView)v.findViewById(R.id.transaction_time);
						tmp.setTextSize(10);
					}
					else{
						tmp=(TextView)v.findViewById(R.id.transaction_value);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_date);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_time);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultColor = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_fieldColor), "#0099CC");
					TextView tmp;

					if(useDefaults){
						tmp=(TextView)v.findViewById(R.id.transaction_value);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_date);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_time);
						tmp.setTextColor(Color.parseColor("#0099CC"));
					}
					else{
						tmp=(TextView)v.findViewById(R.id.transaction_value);
						tmp.setTextColor(Color.parseColor(DefaultColor));
						tmp=(TextView)v.findViewById(R.id.transaction_date);
						tmp.setTextColor(Color.parseColor(DefaultColor));
						tmp=(TextView)v.findViewById(R.id.transaction_time);
						tmp.setTextColor(Color.parseColor(DefaultColor));
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
				}

				//For User-Defined Field Visibility
				TextView name = (TextView) v.findViewById(R.id.transaction_name);
				TextView value = (TextView) v.findViewById(R.id.transaction_value);
				TextView type = (TextView) v.findViewById(R.id.transaction_type);
				TextView category = (TextView) v.findViewById(R.id.transaction_category);
				TextView checknum = (TextView) v.findViewById(R.id.transaction_checknum);
				TextView memo = (TextView) v.findViewById(R.id.transaction_memo);
				TextView date = (TextView) v.findViewById(R.id.transaction_date);
				TextView time = (TextView) v.findViewById(R.id.transaction_time);
				TextView cleared = (TextView) v.findViewById(R.id.transaction_cleared);

				if(useDefaults||prefs.getBoolean("checkbox_transaction_nameField", true)){
					name.setVisibility(View.VISIBLE);
				}
				else{
					name.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_valueField", true)){
					value.setVisibility(View.VISIBLE);
				}
				else{
					value.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_typeField", true)){
					type.setVisibility(View.VISIBLE);
				}
				else{
					type.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_categoryField", true)){
					category.setVisibility(View.VISIBLE);
				}
				else{
					category.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_checknumField", true)){
					checknum.setVisibility(View.VISIBLE);
				}
				else{
					checknum.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_memoField", true)){
					memo.setVisibility(View.VISIBLE);
				}
				else{
					memo.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_dateField", true)){
					date.setVisibility(View.VISIBLE);
				}
				else{
					date.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_timeField", true)){
					time.setVisibility(View.VISIBLE);
				}
				else{
					time.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_clearedField", true)){
					cleared.setVisibility(View.VISIBLE);
				}
				else{
					cleared.setVisibility(View.INVISIBLE);
				}


			}

			if (user != null) {
				TextView name = (TextView) v.findViewById(R.id.transaction_name);
				TextView value = (TextView) v.findViewById(R.id.transaction_value);
				TextView type = (TextView) v.findViewById(R.id.transaction_type);
				TextView category = (TextView) v.findViewById(R.id.transaction_category);
				TextView checknum = (TextView) v.findViewById(R.id.transaction_checknum);
				TextView memo = (TextView) v.findViewById(R.id.transaction_memo);
				TextView date = (TextView) v.findViewById(R.id.transaction_date);
				TextView time = (TextView) v.findViewById(R.id.transaction_time);
				TextView cleared = (TextView) v.findViewById(R.id.transaction_cleared);

				//Change gradient
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.transaction_gradient);
					GradientDrawable defaultGradientPos = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFF00FF33,0xFF000000});

					GradientDrawable defaultGradientNeg = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFFFF0000,0xFF000000});

					if(useDefaults){
						if(Float.parseFloat((user.value)) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(Float.parseFloat((user.value)) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
				}

				if (user.name != null) {
					name.setText(user.name);
				}

				if(user.value != null) {
					value.setText("Value: " + user.value );
				}

				if(user.type != null) {
					type.setText("Type: " + user.type );
				}

				if(user.category != null) {
					category.setText("Category: " + user.category );
				}

				if(user.checknum != null) {
					checknum.setText("Check Num: " + user.checknum );
				}

				if(user.memo != null) {
					memo.setText("Memo: " + user.memo );
				}

				if(user.date != null) {
					date.setText("Date: " + user.date );
				}

				if(user.time != null) {
					time.setText("Time: " + user.time );
				}

				if(user.cleared != null) {
					cleared.setText("Cleared: " + user.cleared );
				}

			}
			return v;
		}
	}

	//An Object Class used to hold the data of each transaction record
	public class TransactionRecord {
		private int id;
		private int acctId;
		private String name;
		private String value;
		private String type;
		private String category;
		private String checknum;
		private String memo;
		private String time;
		private String date;
		private String cleared;

		public TransactionRecord(int id, int acctId, String name, String value, String type, String category, String checknum, String memo, String time, String date, String cleared) {
			this.id = id;
			this.acctId = acctId;
			this.name = name;
			this.value = value;
			this.type = type;
			this.category = category;
			this.checknum = checknum;
			this.memo = memo;
			this.time = time;
			this.date = date;
			this.cleared = cleared;
		}
	}

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Toast.makeText(this, "Options Just Changed: Transactions.Java", Toast.LENGTH_SHORT).show();
		populate();
	}

	//If android version supports it, smooth gradient
	@TargetApi(5)
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);

	}

	//Calculates the balance
	public void calculateBalance(int id){
		TextView balance = (TextView)this.findViewById(R.id.transaction_total_balance);
		balance.setText("Total Balance: " + totalBalance);

		//Update account with accurate balance
		String sqlCommand = "";

		//Open Database
		myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		//Update Record
		//myDB.execSQL(sqlCommand);

		//Close Database if Opened
		if (myDB != null){
			myDB.close();
		}

		//Toast.makeText(this, "AcctID: " + id, Toast.LENGTH_SHORT).show();

	}

}//end Transactions