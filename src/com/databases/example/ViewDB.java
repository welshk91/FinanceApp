package com.databases.example;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;

public class ViewDB extends Activity implements OnSharedPreferenceChangeListener {

	int page;

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=1;
	int CONTEXT_MENU_EDIT=2;
	int CONTEXT_MENU_DELETE=3;

	//Text Area for Adding Accounts
	EditText aName;
	EditText aBalance;

	View accountStatsView;

	//Variables for the Account Table
	String accountName = null;
	String accountTime = null;
	String accountBalance = null;
	String accountDate = null;

	//TextView of Statistics
	TextView statsName;
	TextView statsValue;
	TextView statsDate;
	TextView statsTime;


	ListView lv = null;
	ArrayAdapter<AccountRecord> adapter = null;

	Cursor c = null;
	final String tblAccounts = "tblAccounts";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;
	ArrayList<AccountRecord> results = new ArrayList<AccountRecord>();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.accounts);
		page = R.layout.accounts;

		lv = (ListView)findViewById(R.id.list);

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		//Set Listener for regular mouse click
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				int selectionRowID = (int) adapter.getItemId(position);
				//String item = (String) adapter.getItem(position).name;

				//NOTE: LIMIT *position*,*how many after*
				String sqlCommand = "SELECT * FROM " + tblAccounts + " WHERE ID IN (SELECT ID FROM (SELECT ID FROM " + tblAccounts + " LIMIT " + (selectionRowID-0) + ",1)AS tmp)";

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
					//Toast.makeText(ViewDB.this, "ID: "+entry_id+"\nName: "+entry_name+"\nBalance: "+entry_balance+"\nTime: "+entry_time+"\nDate: "+entry_date, Toast.LENGTH_SHORT).show();
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

			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener


		//Allows Context Menus for each item of the list view
		registerForContextMenu(lv);

		//Footer Buttons 
		Button addAccount = (Button)findViewById(R.id.account_footer_Add); 
		addAccount.setOnClickListener(buttonListener);
		Button transferAccount = (Button)findViewById(R.id.account_footer_Transfer); 
		transferAccount.setOnClickListener(buttonListener);
		Button unknownAccount = (Button)findViewById(R.id.account_footer_Unknown); 
		unknownAccount.setOnClickListener(buttonListener);

		//Set up an adapter for the listView
		adapter = new UserItemAdapter(this, android.R.layout.simple_list_item_1, results);
		lv.setAdapter(adapter);

		//Set up a listener for changes in settings menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		populate();

	}// end onCreate

	//Method called after creation, populates list with account information
	protected void populate() {
		results = new ArrayList<AccountRecord>();

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
					String name = c.getString(NameColumn);
					String balance = c.getString(BalanceColumn);
					String time = c.getString(TimeColumn);
					String date = c.getString(DateColumn);

					AccountRecord entry = new AccountRecord(name, balance,date,time);
					results.add(entry);

				} while (c.moveToNext());
			}
		} 

		else {
			AccountRecord tmp = new AccountRecord("DATABASE EMPTY",null,null,null);
			results.add(tmp);		
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		adapter = new UserItemAdapter(this, android.R.layout.simple_list_item_1, results);
		lv.setAdapter(adapter);
	}

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

		String sqlCommand = "SELECT * FROM " + tblAccounts + " WHERE ID IN (SELECT ID FROM (SELECT ID FROM " + tblAccounts + " LIMIT " + (itemInfo.position-0) + ",1)AS tmp)";
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
			entry_id = c.getInt(c.getColumnIndex("ID"));
			entry_name = c.getString(c.getColumnIndex("Name"));
			entry_balance = c.getString(c.getColumnIndex("Balance"));
			entry_time = c.getString(c.getColumnIndex("Time"));
			entry_date = c.getString(c.getColumnIndex("Date"));
			//Toast.makeText(ViewDB.this, "ID: "+entry_id+"\nName: "+entry_name+"\nBalance: "+entry_balance+"\nTime: "+entry_time+"\nDate: "+entry_date, Toast.LENGTH_SHORT).show();
		}while(c.moveToNext());

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}


		LayoutInflater li = LayoutInflater.from(ViewDB.this);
		accountStatsView = li.inflate(R.layout.account_stats, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				ViewDB.this);

		// set account_add.xml to AlertDialog builder
		alertDialogBuilder.setView(accountStatsView);

		//set Title
		alertDialogBuilder.setTitle("View Account");

		// set dialog message
		alertDialogBuilder
		.setCancelable(true);

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

		//Set Statistics
		statsName = (TextView)accountStatsView.findViewById(R.id.TextAccountName);
		statsName.setText(entry_name);
		statsValue = (TextView)accountStatsView.findViewById(R.id.TextAccountValue);
		statsValue.setText(entry_balance);
		statsDate = (TextView)accountStatsView.findViewById(R.id.TextAccountDate);
		statsDate.setText(entry_date);
		statsTime = (TextView)accountStatsView.findViewById(R.id.TextAccountTime);
		statsTime.setText(entry_date);

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


		//NOTE: LIMIT *position*,*how many after*
		String sqlCommand = "DELETE FROM " + tblAccounts + " WHERE ID IN (SELECT ID FROM (SELECT ID FROM " + tblAccounts + " LIMIT " + (itemInfo.position-0) + ",1)AS tmp);";

		//Open Database
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		myDB.execSQL(sqlCommand);
		//Toast.makeText(this, "SQL\n" + sqlCommand, 5000).show();

		//Close Database if Opened
		if (myDB != null){
			myDB.close();
		}

		//results.remove(itemInfo.position);
		//adapter.notifyDataSetChanged();

		ViewDB.this.populate();

		Toast.makeText(this, "Deleted Item:\n" + itemName, Toast.LENGTH_SHORT).show();

	}//end of accountDelete

	//Method for handling Button 'mouse-clicks'
	public OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {
			//If the Add button on the Account list is pressed
			case R.id.account_footer_Add:
				//code here for add button
				page = R.layout.account_add;
				break;

				//If the Transfer button on the Account list is pressed
			case R.id.account_footer_Transfer:
				//code here for transfer button
				Toast.makeText(ViewDB.this, "Transfer Pressed", Toast.LENGTH_SHORT).show();
				break;

				//If the unknown button on the Account list is pressed
			case R.id.account_footer_Unknown:
				//code here for unknown button
				Toast.makeText(ViewDB.this, "Unknown Pressed", Toast.LENGTH_SHORT).show();
				break;

			}//end Switch ViewByID

			switch (page) {

			//Going to Accounts
			case R.layout.accounts:
				ViewDB.this.onCreate(null);

				break;

				//Going to Add Account
			case R.layout.account_add:

				// get account_add.xml view
				LayoutInflater li = LayoutInflater.from(ViewDB.this);
				final View promptsView = li.inflate(R.layout.account_add, null);

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						ViewDB.this);

				// set account_add.xml to AlertDialog builder
				alertDialogBuilder.setView(promptsView);

				//set Title
				alertDialogBuilder.setTitle("Add An Account");

				// set dialog message
				alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// CODE FOR "OK"
						aName = (EditText) promptsView.findViewById(R.id.EditAccountName);
						aBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
						accountName = aName.getText().toString().trim();
						accountBalance = aBalance.getText().toString().trim();

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
							Toast.makeText(ViewDB.this, " No Nulls Allowed ", Toast.LENGTH_SHORT).show();
						}

						//Close Database if Opened
						if (myDB != null){
							myDB.close();
						}

						ViewDB.this.populate();

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
			Toast.makeText(this, "You pressed Logout!", Toast.LENGTH_SHORT).show();
			this.finish();
			this.moveTaskToBack(true);
			super.onDestroy();
			break;

		case R.id.account_menu_options:    
			//Toast.makeText(this, "You pressed Options!", Toast.LENGTH_SHORT).show();
			Intent v = new Intent(ViewDB.this, Options.class);
			startActivity(v);
			break;

		case R.id.account_menu_help:    
			Toast.makeText(this, "You pressed Help!", Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}

	public class UserItemAdapter extends ArrayAdapter<AccountRecord> {
		private ArrayList<AccountRecord> account;

		public UserItemAdapter(Context context, int textViewResourceId, ArrayList<AccountRecord> users) {
			super(context, textViewResourceId, users);
			this.account = users;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			AccountRecord user = account.get(position);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ViewDB.this);
			boolean useDefaults = prefs.getBoolean("checkbox_default", false);


			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.account_item, null);

				//Change Background Colors
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.account_layout);
					String startColor = prefs.getString(ViewDB.this.getString(R.string.pref_key_account_startBackgroundColor), "#E8E8E8");
					String endColor = prefs.getString(ViewDB.this.getString(R.string.pref_key_account_endBackgroundColor), "#FFFFFF");
					GradientDrawable defaultGradient = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {Color.parseColor(startColor),Color.parseColor(endColor)});
					//gd.setCornerRadius(0f);

					if(useDefaults){
						l.setBackgroundResource(R.drawable.account_background_gradient);
					}
					else{
						l.setBackgroundDrawable(defaultGradient);
					}

				}
				catch(Exception e){
					Toast.makeText(ViewDB.this, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultSize = prefs.getString(ViewDB.this.getString(R.string.pref_key_account_nameSize), "16");
					TextView t;
					t=(TextView)v.findViewById(R.id.account_name);

					if(useDefaults){
						t.setTextSize(16);
					}
					else{
						t.setTextSize(Integer.parseInt(DefaultSize));
					}

				}
				catch(Exception e){
					Toast.makeText(ViewDB.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultColor = prefs.getString(ViewDB.this.getString(R.string.pref_key_account_nameColor), "#000000");
					TextView t;
					t=(TextView)v.findViewById(R.id.account_name);

					if(useDefaults){
						t.setTextColor(Color.parseColor("#000000"));
					}
					else{
						t.setTextColor(Color.parseColor(DefaultColor));
					}

				}
				catch(Exception e){
					Toast.makeText(ViewDB.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultSize = prefs.getString(ViewDB.this.getString(R.string.pref_key_account_fieldSize), "10");
					TextView tmp;

					if(useDefaults){
						tmp=(TextView)v.findViewById(R.id.account_balance);
						tmp.setTextSize(10);
						tmp=(TextView)v.findViewById(R.id.account_date);
						tmp.setTextSize(10);
						tmp=(TextView)v.findViewById(R.id.account_time);
						tmp.setTextSize(10);
					}
					else{
						tmp=(TextView)v.findViewById(R.id.account_balance);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.account_date);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.account_time);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
					}

				}
				catch(Exception e){
					Toast.makeText(ViewDB.this, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultColor = prefs.getString(ViewDB.this.getString(R.string.pref_key_account_fieldColor), "#0099CC");
					TextView tmp;

					if(useDefaults){
						tmp=(TextView)v.findViewById(R.id.account_balance);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.account_date);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.account_time);
						tmp.setTextColor(Color.parseColor("#0099CC"));
					}
					else{
						tmp=(TextView)v.findViewById(R.id.account_balance);
						tmp.setTextColor(Color.parseColor(DefaultColor));
						tmp=(TextView)v.findViewById(R.id.account_date);
						tmp.setTextColor(Color.parseColor(DefaultColor));
						tmp=(TextView)v.findViewById(R.id.account_time);
						tmp.setTextColor(Color.parseColor(DefaultColor));
					}

				}
				catch(Exception e){
					Toast.makeText(ViewDB.this, "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
				}


				//For User-Defined Field Visibility
				if(useDefaults||prefs.getBoolean("checkbox_account_nameField", true)){
					TextView name = (TextView) v.findViewById(R.id.account_name);
					name.setVisibility(View.VISIBLE);
				}
				else{
					TextView name = (TextView) v.findViewById(R.id.account_name);
					name.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_account_balanceField", true)){
					TextView balance = (TextView) v.findViewById(R.id.account_balance);
					balance.setVisibility(View.VISIBLE);
				}
				else{
					TextView balance = (TextView) v.findViewById(R.id.account_balance);
					balance.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_account_dateField", true)){
					TextView date = (TextView) v.findViewById(R.id.account_date);
					date.setVisibility(View.VISIBLE);
				}
				else{
					TextView date = (TextView) v.findViewById(R.id.account_date);
					date.setVisibility(View.INVISIBLE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_account_timeField", true)){
					TextView time = (TextView) v.findViewById(R.id.account_time);
					time.setVisibility(View.VISIBLE);
				}
				else{
					TextView time = (TextView) v.findViewById(R.id.account_time);
					time.setVisibility(View.INVISIBLE);
				}


			}

			if (user != null) {
				TextView name = (TextView) v.findViewById(R.id.account_name);
				TextView balance = (TextView) v.findViewById(R.id.account_balance);
				TextView date = (TextView) v.findViewById(R.id.account_date);
				TextView time = (TextView) v.findViewById(R.id.account_time);

				//Change gradient
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.account_gradient);
					GradientDrawable defaultGradientPos = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFF00FF33,0xFF000000});
					//gd.setCornerRadius(0f);

					GradientDrawable defaultGradientNeg = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFFFF0000,0xFF000000});
					//gd.setCornerRadius(0f);

					if(useDefaults){
						if(Float.parseFloat((user.balance)) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(Float.parseFloat((user.balance)) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}
					}

				}
				catch(Exception e){
					Toast.makeText(ViewDB.this, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
				}


				if (user.name != null) {
					name.setText(user.name);
				}

				if(user.balance != null) {
					balance.setText("Balance: " + user.balance );
				}

				if(user.date != null) {
					date.setText("Date: " + user.date );
				}

				if(user.time != null) {
					time.setText("Time: " + user.time );
				}

			}
			return v;
		}
	}

	//An Object Class used to hold the data of each account record
	public class AccountRecord {
		private String name;
		private String balance;
		private String date;
		private String time;

		public AccountRecord(String name, String balance, String date, String time) {
			this.name = name;
			this.balance = balance;
			this.date = date;
			this.time = time;
		}
	}

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Toast.makeText(this, "Options Just Changed: ViewDB.Java", Toast.LENGTH_SHORT).show();
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

}// end ViewDB
