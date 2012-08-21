package com.databases.example;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;


public class Transactions extends FragmentActivity{

	//The View
	int page;

	//Variables for the transaction Table
	String transactionName = null;
	static String transactionTime = null;
	String transactionBalance = null;
	static String transactionDate = null;

	//Dialog for Adding Transaction
	static View promptsView;
	View transStatsView;

	//Text Area for Adding Accounts
	EditText tName;
	EditText tBalance;
	static Button tDate;
	static Button tTime;

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
	String account_name;
	String account_balance;
	String account_date;
	String account_time;

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=1;
	int CONTEXT_MENU_EDIT=2;
	int CONTEXT_MENU_DELETE=3;

	//ListView and Adapter
	ListView lv = null;
	ArrayAdapter<UserRecord> adapter = null;

	//Variables needed for traversing database
	Cursor c = null;
	final String tblTrans = "tblTrans";
	final String tblAccounts = "tblAccounts";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;
	ArrayList<UserRecord> results = new ArrayList<UserRecord>();

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
		account_name = getIntent().getExtras().getString("name");
		account_balance = getIntent().getExtras().getString("balance");
		account_date = getIntent().getExtras().getString("date");
		account_time = getIntent().getExtras().getString("time");

		Toast.makeText(this, "ID: "+account_id+"\nName: "+account_name+"\nBalance: "+account_balance+"\nTime: "+account_time+"\nDate: "+account_date, Toast.LENGTH_SHORT).show();

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

		//Populate List with Entries
		populate();

	}//end onCreate


	//Populate view with all the transactions of selected account
	protected void populate(){

		results = new ArrayList<UserRecord>();

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		c = myDB.query(tblTrans, new String[] { "TransDesc", "TransAmt", "TransDate"}, "ToAcctID = " + account_id,
				null, null, null, null);

		startManagingCursor(c);
		int descColumn = c.getColumnIndex("TransDesc");
		int amtColumn = c.getColumnIndex("TransAmt");
		int dateColumn = c.getColumnIndex("TransDate");

		c.moveToFirst();
		if (c != null) {
			if (c.isFirst()) {
				do {
					String name = c.getString(descColumn);
					String balance = c.getString(amtColumn);
					String date = c.getString(dateColumn); 

					UserRecord entry = new UserRecord(name, balance,null,null,null,null,date,null,null);
					results.add(entry);

				} while (c.moveToNext());
			}
		} 

		else {
			UserRecord tmp = new UserRecord("DATABASE EMPTY", null,null,null,null,null,null,null,null);
			results.add(tmp);
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		adapter = new UserItemAdapter(this, android.R.layout.simple_list_item_1, results);
		lv.setAdapter(adapter);

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
		//Toast.makeText(this, "Opened Item:\n" + itemName, Toast.LENGTH_SHORT).show();

		String sqlCommand = "SELECT * FROM " + tblTrans + " WHERE TransID IN (SELECT TransID FROM (SELECT TransID FROM " + tblTrans + " LIMIT " + (itemInfo.position) + ",1)AS tmp)";
		//Toast.makeText(this, "SQL\n" + sqlCommand, Toast.LENGTH_LONG).show();

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
			entry_id = c.getInt(c.getColumnIndex("TransID"));
			entry_name = c.getString(c.getColumnIndex("TransDesc"));
			entry_balance = c.getString(c.getColumnIndex("TransAmt"));
			entry_time = c.getString(c.getColumnIndex("TransDate"));
			entry_date = c.getString(c.getColumnIndex("TransDate"));
			Toast.makeText(Transactions.this, "ID: "+entry_id+"\nName: "+entry_name+"\nBalance: "+entry_balance+"\nTime: "+entry_time+"\nDate: "+entry_date, Toast.LENGTH_SHORT).show();
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
		statsValue.setText(entry_balance);
		statsType = (TextView)transStatsView.findViewById(R.id.TextTransactionType);
		statsType.setText(entry_date);
		statsCategory = (TextView)transStatsView.findViewById(R.id.TextTransactionCategory);
		statsCategory.setText(entry_date);
		statsCheckNum = (TextView)transStatsView.findViewById(R.id.TextTransactionCheck);
		statsCheckNum.setText(entry_date);
		statsMemo = (TextView)transStatsView.findViewById(R.id.TextTransactionMemo);
		statsMemo.setText(entry_date);
		statsDate = (TextView)transStatsView.findViewById(R.id.TextTransactionDate);
		statsDate.setText(entry_date);
		statsTime = (TextView)transStatsView.findViewById(R.id.TextTransactionTime);
		statsTime.setText(entry_date);

		//chkCleared = (CheckBox)this.findViewById(R.id.CheckTransactionCleared);
		//String cleared = Boolean.valueOf(chkCleared.isChecked()).toString();
		statsCleared = (TextView)transStatsView.findViewById(R.id.TextTransactionCleared);
		//statsCleared.setText(cleared);

		// show it
		alertDialog.show();

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

		//NOTE: LIMIT *position*,*how many after*
		String sqlCommand = "DELETE FROM " + tblTrans + " WHERE TransID IN (SELECT TransID FROM (SELECT TransID FROM " + tblTrans + " LIMIT " + (itemInfo.position) + ",1)AS tmp);";

		//Open Database
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		myDB.execSQL(sqlCommand);
		Toast.makeText(this, "SQL\n" + sqlCommand, Toast.LENGTH_LONG).show();

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
						tBalance = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
						transactionName = tName.getText().toString().trim();
						transactionBalance = tBalance.getText().toString().trim();

						//Open Database
						myDB = Transactions.this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);				

						if (transactionName != null && transactionTime != null && transactionDate != null
								&& transactionName != " " && transactionTime != " " && transactionDate != " ") {
							myDB.execSQL("INSERT INTO " + tblTrans
									+ " (TransDesc, ToAcctID, TransAmt, TransDate)" + " VALUES ('"
									+ transactionName + "', '" + account_id + "', '" + transactionBalance + "', '" + transactionDate + "');");
							page = R.layout.transactions;
						} 

						else {
							Toast.makeText(Transactions.this, " No Nulls Allowed ", Toast.LENGTH_LONG).show();
						}

						//Close Database if Opened
						if (myDB != null){
							myDB.close();
						}

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
			Toast.makeText(this, "You pressed Options!", Toast.LENGTH_SHORT).show();
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

	public class UserItemAdapter extends ArrayAdapter<UserRecord> {
		private ArrayList<UserRecord> users;

		public UserItemAdapter(Context context, int textViewResourceId, ArrayList<UserRecord> users) {
			super(context, textViewResourceId, users);
			this.users = users;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.transaction_item, null);
			}

			UserRecord user = users.get(position);
			if (user != null) {
				TextView name = (TextView) v.findViewById(R.id.name);
				TextView value = (TextView) v.findViewById(R.id.value);
				TextView type = (TextView) v.findViewById(R.id.type);
				TextView category = (TextView) v.findViewById(R.id.category);
				TextView checknum = (TextView) v.findViewById(R.id.checknum);
				TextView memo = (TextView) v.findViewById(R.id.memo);
				TextView date = (TextView) v.findViewById(R.id.date);
				TextView time = (TextView) v.findViewById(R.id.time);
				TextView cleared = (TextView) v.findViewById(R.id.cleared);

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

	public class UserRecord {
		private String name;
		private String value;
		private String type;
		private String category;
		private String checknum;
		private String memo;
		private String date;
		private String time;
		private String cleared;

		public UserRecord(String name, String value, String type, String category, String checknum, String memo, String date, String time, String cleared) {
			this.name = name;
			this.value = value;
			this.type = type;
			this.category = category;
			this.checknum = checknum;
			this.memo = memo;
			this.date = date;
			this.time = time;
			this.cleared = cleared;
		}
	}

}//end Transactions