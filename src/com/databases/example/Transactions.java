package com.databases.example;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.slidingmenu.lib.SlidingMenu;

public class Transactions extends SherlockFragment implements OnSharedPreferenceChangeListener{

	//Used to keep Track of total Balance
	float totalBalance;

	//Used to determine if fragment should show all transactions
	boolean showAllTransactions=false;

	//Dialog for Adding Transaction
	static View promptsView;
	View transStatsView;
	View myFragmentView;

	//Need to be global so I can dismiss and avoid leaks
	AlertDialog alertDialogView;
	AlertDialog alertDialogAdd;
	AlertDialog alertDialogEdit;

	//Widgets for Adding Accounts
	EditText tName;
	EditText tValue;
	Spinner tType;	
	Spinner tCategory;
	EditText tCheckNum;
	AutoCompleteTextView tMemo;
	static Button tTime;
	static Button tDate;
	Button tCategoryAdd;
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

	//Date Format to use for time (01:42 PM)
	final static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

	//Date Format to use for date (03-26-2013)
	final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");		


	//Variables of the Account Used
	int account_id;

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=4;
	int CONTEXT_MENU_EDIT=5;
	int CONTEXT_MENU_DELETE=6;

	//ListView and Adapter
	ListView lv = null;
	ArrayAdapter<TransactionRecord> adapter = null;

	//Variables needed for traversing database
	Cursor c = null;
	final String tblTrans = "tblTrans";
	final String tblAccounts = "tblAccounts";
	final String tblCategory = "tblCategory";
	final String tblSubCategory = "tblSubCategory";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;
	ArrayList<TransactionRecord> results = new ArrayList<TransactionRecord>();
	ArrayList<String> dropdownResults = new ArrayList<String>();

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

	//Adapter for category spinner
	SimpleCursorAdapter categorySpinnerAdapter = null;
	Cursor categoryCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Arguments
		Bundle bundle=getArguments();

		//bundle is empty if from search, so don't add extra menu options
		if(bundle!=null){
			setHasOptionsMenu(true);
		}

		/*
		 * Set to true if you want to keep instance during rotation change
		 * can't do it because containers are named differently for the xmls
		 */

		setRetainInstance(false);

	}//end onCreate

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		myFragmentView = inflater.inflate(R.layout.transactions, container, false);		

		lv = (ListView)myFragmentView.findViewById(R.id.transaction_list);

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		//Arguments sent by Account Fragment
		Bundle bundle=getArguments();

		if(bundle!=null && bundle.getBoolean("showAll")){
			//Toast.makeText(this.getActivity(), "Could Not Find Account Information", Toast.LENGTH_SHORT).show();
			showAllTransactions = true;
		}
		else if(bundle!=null && showAllTransactions==false) {
			account_id = bundle.getInt("ID");

			//getActivity().setTitle("Transactions <" + account_name +">");

			//Toast.makeText(this.getActivity(), "ID: "+account_id+"\nName: "+account_name+"\nBalance: "+account_balance+"\nTime: "+account_time+"\nDate: "+account_date, Toast.LENGTH_SHORT).show();
		}
		//Set Listener for regular mouse click
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				int selectionRowID = (int) adapter.getItemId(position);
				String item = adapter.getItem(position).name;

				Toast.makeText(Transactions.this.getActivity(), "Click\nRow: " + selectionRowID + "\nEntry: " + item, Toast.LENGTH_SHORT).show();

			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener


		//Allows Context Menus for each item of the list view
		registerForContextMenu(lv);

		//Set up a listener for changes in settings menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		prefs.registerOnSharedPreferenceChangeListener(this);

		//Populate List with Entries
		populate();

		return myFragmentView;
	}

	//Populate view with all the transactions of selected account
	protected void populate(){
		results = new ArrayList<TransactionRecord>();
		dropdownResults = new ArrayList<String>();

		//TextView instructing user if database is empty
		TextView noResult = (TextView)myFragmentView.findViewById(R.id.transaction_noTransaction);
		noResult.setVisibility(View.GONE);

		//Reset totalBalance
		totalBalance = 0;

		//Arguments for fragment
		Bundle bundle=getArguments();
		boolean searchFragment=true;

		if(bundle!=null){
			searchFragment = bundle.getBoolean("boolSearch");
		}

		// Cursor is used to navigate the query results
		myDB = this.getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

		if(showAllTransactions){
			c = myDB.query(tblTrans, new String[] { "TransID", "ToAcctID", "TransName", "TransValue", "TransType", "TransCategory","TransCheckNum", "TransMemo", "TransTime", "TransDate", "TransCleared"}, null,
					null, null, null, null);
		}
		else if(searchFragment){
			//Word being searched
			String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);			

			//SQL for searching
			String sqlCommand = " SELECT * FROM " + tblTrans + 
					" WHERE TransName " + 
					" LIKE ?" +
					" UNION " +
					" SELECT * FROM " + tblTrans +
					" WHERE TransValue " + 
					" LIKE ?" +
					" UNION " +
					" SELECT * FROM " + tblTrans +
					" WHERE TransCategory " + 
					" LIKE ?" +
					" UNION " +
					" SELECT * FROM " + tblTrans +
					" WHERE TransDate " + 
					" LIKE ?" +
					" UNION " +				
					" SELECT * FROM " + tblTrans +
					" WHERE TransTime " + 
					" LIKE ?" +
					" UNION " +
					" SELECT * FROM " + tblTrans +
					" WHERE TransMemo " + 
					" LIKE ?" +
					" UNION " +
					" SELECT * FROM " + tblTrans +
					" WHERE TransCheckNum " + 
					" LIKE ?";

			try{
				c = myDB.rawQuery(sqlCommand, new String[] { "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%" });
			}
			catch(Exception e){
				Toast.makeText(this.getActivity(), "Search Failed\n"+e, Toast.LENGTH_SHORT).show();
				return;
			}

		}
		else{
			c = myDB.query(tblTrans, new String[] { "TransID", "ToAcctID", "TransName", "TransValue", "TransType", "TransCategory","TransCheckNum", "TransMemo", "TransTime", "TransDate", "TransCleared"}, "ToAcctID = " + account_id,
					null, null, null, null);
		}

		//"ToAcctID = " + account_id

		getActivity().startManagingCursor(c);
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
					dropdownResults.add(memo);

					//Add account balance to total balance
					try{

						//Withdraws should subtract totalBalance
						if(type.contains("Withdrawl")){
							totalBalance = totalBalance - (Float.parseFloat(value));
						}
						//Deposit should add to totalBalance
						else{
							totalBalance = totalBalance + Float.parseFloat(value);
						}

					}
					catch(Exception e){
						Toast.makeText(Transactions.this.getActivity(), "Could not calculate total balance", Toast.LENGTH_SHORT).show();
					}

				} while (c.moveToNext());
			}

			else {
				//No Results Found
				noResult.setVisibility(View.VISIBLE);

				//No Search Result
				if(bundle==null){
					noResult.setText("Nothing Found");
				}

			}
		} 

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		//Set up an adapter for listView
		adapter = new UserItemAdapter(this.getActivity(), android.R.layout.simple_list_item_1, results);
		lv.setAdapter(adapter);

		//Refresh Balance
		calculateBalance(account_id);

	}//end populate

	//Creates menu for long presses
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

	//Handles which methods are called when using the long presses menu
	@Override  
	public boolean onContextItemSelected(android.view.MenuItem item) {

		if(item.getItemId()==CONTEXT_MENU_OPEN){
			//Toast.makeText(Transactions.this.getActivity(), "Open in trans", Toast.LENGTH_SHORT).show();
			transactionOpen(item);
			return true;
		}  
		else if(item.getItemId()==CONTEXT_MENU_EDIT){
			transactionEdit(item);
			return true;
		}
		else if(item.getItemId()==CONTEXT_MENU_DELETE){
			transactionDelete(item);
			return true;
		}
		else {
			//return super.onContextItemSelected(item);
		}  

		return super.onContextItemSelected(item);  
	}

	//For Opening a Transaction
	public void transactionOpen(android.view.MenuItem item){  
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		String sqlCommand = "SELECT * FROM " + tblTrans + 
				" WHERE TransID = " + adapter.getItem(itemInfo.position).id;

		myDB = getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

		Cursor c = myDB.rawQuery(sqlCommand, null);
		getActivity().startManagingCursor(c);

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

		// get transaction_stats.xml view
		LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
		transStatsView = li.inflate(R.layout.transaction_stats, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

		// set xml to AlertDialog builder
		alertDialogBuilder.setView(transStatsView);

		//set Title
		alertDialogBuilder.setTitle("View Transaction");

		// set dialog message
		alertDialogBuilder
		.setCancelable(true);

		// create alert dialog
		alertDialogView = alertDialogBuilder.create();

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
		alertDialogView.show();

	}  

	//For Adding a Transaction
	public void transactionAdd(){
		if(account_id==0){
			Toast.makeText(Transactions.this.getActivity(), "Please Select an Account First", Toast.LENGTH_LONG).show();
			return;
		}

		// get transaction_add.xml view
		LayoutInflater li = LayoutInflater.from(Transactions.this.getActivity());
		promptsView = li.inflate(R.layout.transaction_add, null);

		tName = (EditText) promptsView.findViewById(R.id.EditTransactionName);
		tValue = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
		tType = (Spinner)promptsView.findViewById(R.id.spinner_transaction_type);
		tCategory = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);
		tCheckNum = (EditText)promptsView.findViewById(R.id.EditTransactionCheck);
		tMemo = (AutoCompleteTextView)promptsView.findViewById(R.id.EditTransactionMemo);
		tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);
		tTime = (Button)promptsView.findViewById(R.id.ButtonTransactionTime);
		tDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);

		//Adapter for memo's autocomplete
		ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, dropdownResults);
		tMemo.setAdapter(dropdownAdapter);

		//Add dictionary back to autocomplete
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
		tMemo.setKeyListener(input);

		final Calendar c = Calendar.getInstance();
		
		tDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);
		tDate.setText(dateFormat.format(c.getTime()));

		tTime = (Button)promptsView.findViewById(R.id.ButtonTransactionTime);
		tTime.setText(timeFormat.format(c.getTime()));

		tCategory = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);

		//Populate List
		categoryPopulate();

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				Transactions.this.getActivity());

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

				//Needed to get category's name from DB-populated spinner
				int categoryPosition = tCategory.getSelectedItemPosition();
				Cursor cursor = (Cursor) categorySpinnerAdapter.getItem(categoryPosition);

				transactionName = tName.getText().toString().trim();
				transactionValue = tValue.getText().toString().trim();
				transactionType = tType.getSelectedItem().toString().trim();

				try{
					transactionCategory = cursor.getString(cursor.getColumnIndex("CateName"));
				}
				catch(Exception e){
					//Usually caused if no category exists

				}

				transactionCheckNum = tCheckNum.getText().toString().trim();
				transactionMemo = tMemo.getText().toString().trim();
				transactionCleared = tCleared.isChecked()+"";

				//Set Time
				transactionTime = tTime.getText().toString().trim();
				transactionDate = tDate.getText().toString().trim();

				//Check to see if value is a number
				boolean validValue=false;
				try{
					Float.parseFloat(transactionValue);
					validValue=true;
				}
				catch(Exception e){
					validValue=false;
				}

				//Open Database
				myDB = Transactions.this.getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

				try{
					if (transactionName.length()>0) {

						if(!validValue){
							transactionValue = "0";
						}

						//Insert values into accounts table
						ContentValues transactionValues=new ContentValues();
						transactionValues.put("ToAcctID",account_id);
						transactionValues.put("TransName",transactionName);
						transactionValues.put("TransValue",transactionValue);
						transactionValues.put("TransType",transactionType);
						transactionValues.put("TransCategory",transactionCategory);
						transactionValues.put("TransCheckNum",transactionCheckNum);
						transactionValues.put("TransMemo",transactionMemo);
						transactionValues.put("TransTime",transactionTime);
						transactionValues.put("TransDate",transactionDate);
						transactionValues.put("TransCleared",transactionCleared);

						myDB.insert(tblTrans, null, transactionValues);

					} 

					else {
						Toast.makeText(Transactions.this.getActivity(), "Needs a Name", Toast.LENGTH_LONG).show();
					}
				}
				catch(Exception e){
					Toast.makeText(Transactions.this.getActivity(), "Error Adding Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
				}

				//Close cursor
				cursor.close();

				//Close Database if Opened
				if (myDB != null){
					myDB.close();
				}

				Transactions.this.populate();

				//Reload account fragment if shown
				View account_frame = getActivity().findViewById(R.id.account_frag_frame);

				if(account_frame!=null){
					Accounts account_frag = new Accounts();

					//Bundle for Account fragment
					Bundle argsAccount = new Bundle();
					argsAccount.putBoolean("boolSearch", false);

					account_frag.setArguments(argsAccount);

					getFragmentManager().beginTransaction()
					.replace(R.id.account_frag_frame, account_frag, "account_frag_tag").commit();

					getFragmentManager().executePendingTransactions();

				}

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
		alertDialogAdd = alertDialogBuilder.create();

		// show it
		alertDialogAdd.show();

	}//end of transactionAdd

	//For Editing an Transaction
	public void transactionEdit(android.view.MenuItem item){
		final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
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
		LayoutInflater li = LayoutInflater.from(Transactions.this.getActivity());
		promptsView = li.inflate(R.layout.transaction_add, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				Transactions.this.getActivity());

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
		tMemo = (AutoCompleteTextView)promptsView.findViewById(R.id.EditTransactionMemo);
		tDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);
		tTime = (Button)promptsView.findViewById(R.id.ButtonTransactionTime);
		tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);

		//Set the adapter for memo's autocomplete
		ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, dropdownResults);
		tMemo.setAdapter(dropdownAdapter);

		//Add dictionary back to autocomplete
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
		tMemo.setKeyListener(input);

		tName.setText(name);
		tValue.setText(value);
		ArrayAdapter<String> myAdap = (ArrayAdapter<String>) tType.getAdapter();
		int spinnerPosition = myAdap.getPosition(type);
		tType.setSelection(spinnerPosition);

		//Populate Category Spinner
		categoryPopulate();

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

				//Needed to get category's name from DB-populated spinner
				int categoryPosition = tCategory.getSelectedItemPosition();
				Cursor cursor = (Cursor) categorySpinnerAdapter.getItem(categoryPosition);

				transactionName = tName.getText().toString().trim();
				transactionValue = tValue.getText().toString().trim();
				transactionType = tType.getSelectedItem().toString().trim();
				transactionCategory = cursor.getString(cursor.getColumnIndex("CateName"));
				transactionCheckNum = tCheckNum.getText().toString().trim();
				transactionMemo = tMemo.getText().toString().trim();
				transactionCleared = tCleared.isChecked()+"";
				transactionTime = tTime.getText().toString().trim();
				transactionDate = tDate.getText().toString().trim();

				//Check to see if value is a number
				boolean validValue=false;
				try{
					Float.parseFloat(transactionValue);
					validValue=true;
				}
				catch(Exception e){
					validValue=false;
				}

				try{
					if(transactionName.length()>0){

						if(!validValue){
							transactionValue = "0";
						}

						String deleteCommand = "DELETE FROM " + tblTrans + " WHERE TransID = " + tID + ";";

						//Open Database
						myDB = getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

						//Delete Old Record
						myDB.execSQL(deleteCommand);

						//Make new record with same ID
						ContentValues transactionValues=new ContentValues();
						transactionValues.put("TransID",tID);
						transactionValues.put("ToAcctID",aID);
						transactionValues.put("TransName",transactionName);
						transactionValues.put("TransValue",transactionValue);
						transactionValues.put("TransType",transactionType);
						transactionValues.put("TransCategory",transactionCategory);
						transactionValues.put("TransCheckNum",transactionCheckNum);
						transactionValues.put("TransMemo",transactionMemo);
						transactionValues.put("TransTime",transactionTime);
						transactionValues.put("TransDate",transactionDate);
						transactionValues.put("TransCleared",transactionCleared);

						myDB.insert(tblTrans, null, transactionValues);

						//Close Database if Opened
						if (myDB != null){
							myDB.close();
						}
					}

					else{
						Toast.makeText(Transactions.this.getActivity(), "Needs a Name", Toast.LENGTH_SHORT).show();
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this.getActivity(), "Error Editing Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
				}

				Transactions.this.populate();

				//Reload account fragment if shown
				View account_frame = getActivity().findViewById(R.id.account_frag_frame);

				if(account_frame!=null){
					Accounts account_frag = new Accounts();

					//Bundle for Account fragment
					Bundle argsAccount = new Bundle();
					argsAccount.putBoolean("boolSearch", false);

					account_frag.setArguments(argsAccount);

					getFragmentManager().beginTransaction()
					.replace(R.id.account_frag_frame, account_frag, "account_frag_tag").commit();

					getFragmentManager().executePendingTransactions();

				}

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
		alertDialogEdit = alertDialogBuilder.create();

		// show it
		alertDialogEdit.show();

	}

	//For Deleting an Transaction
	public void transactionDelete(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position).name;

		//NOTE: LIMIT *position*,*how many after*
		//String sqlCommand = "DELETE FROM " + tblTrans + 
		//		" WHERE TransID IN (SELECT TransID FROM (SELECT TransID FROM " + tblTrans + 
		//		" LIMIT " + (itemInfo.position-0) + ",1)AS tmp);";

		String sqlCommand = "DELETE FROM " + tblTrans + 
				" WHERE TransID = " + adapter.getItem(itemInfo.position).id;

		//Open Database
		myDB = this.getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

		myDB.execSQL(sqlCommand);

		//Close Database if Opened
		if (myDB != null){
			myDB.close();
		}

		Transactions.this.populate();

		Toast.makeText(this.getActivity(), "Deleted Item:\n" + itemName, Toast.LENGTH_SHORT).show();

	}//end of accountDelete

	//For Menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		View account_frame = getActivity().findViewById(R.id.account_frag_frame);

		if(account_frame!=null){
			SubMenu subMMenuTransaction = menu.addSubMenu("Transaction");
			subMMenuTransaction.add(com.actionbarsherlock.view.Menu.NONE, R.id.transaction_menu_add, com.actionbarsherlock.view.Menu.NONE, "Add");
			subMMenuTransaction.add(com.actionbarsherlock.view.Menu.NONE, R.id.transaction_menu_schedule, com.actionbarsherlock.view.Menu.NONE, "Schedule");

			MenuItem subMenu1Item = subMMenuTransaction.getItem();
			subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		else{
			menu.clear();
			inflater.inflate(R.layout.transaction_menu, menu);
		}

	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			//Intent intentUp = new Intent(Transactions.this.getActivity(), Main.class);
			//intentUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//startActivity(intentUp);
			//menu.toggle();
			break;

		case R.id.transaction_menu_add:    
			transactionAdd();
			return true;

		case R.id.transaction_menu_search:    
			getActivity().onSearchRequested();
			return true;

		case R.id.transaction_menu_schedule:    
			//transactionSchedule
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//Method to help create TimePicker
	public static class TimePickerFragment extends DialogFragment
	implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();

			SimpleDateFormat dateFormatHour = new SimpleDateFormat("hh");
			SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");

			int hour = Integer.parseInt(dateFormatHour.format(c.getTime()));
			int minute = Integer.parseInt(dateFormatMinute.format(c.getTime()));

			return new TimePickerDialog(getActivity(), this, hour, minute,
					false);
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			String ampm = "";
			if(hourOfDay >=12){
				ampm = "PM";
			}
			else{
				ampm = "AM";
			}

			if(hourOfDay==0){
				hourOfDay=12;
			}
			else if (hourOfDay>12){
				hourOfDay=hourOfDay-12;
			}

			transactionTime = hourOfDay + ":" + minute + " " + ampm;
			tTime = (Button)promptsView.findViewById(R.id.ButtonTransactionTime);
			tTime.setText(transactionTime);

		}
	}

	//Method to help create DatePicker
	public static class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();

			SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
			SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
			SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");
			
			int year = Integer.parseInt(dateFormatYear.format(c.getTime()));
			int month = Integer.parseInt(dateFormatMonth.format(c.getTime()))-1;
			int day = Integer.parseInt(dateFormatDay.format(c.getTime()));

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			if(month<10){
				transactionDate = "0"+(month+1) + "-" + day + "-" + year;
			}
			else{
				transactionDate = (month+1) + "-" + day + "-" + year;
			}
			
			tDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);
			tDate.setText(transactionDate);
		}
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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Transactions.this.getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);


			if (v == null) {
				LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.transaction_item, null);

				//Change Background Colors
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.transaction_layout);
					int startColor = prefs.getInt("key_transaction_startBackgroundColor", Color.parseColor("#E8E8E8"));
					int endColor = prefs.getInt("key_transaction_endBackgroundColor", Color.parseColor("#FFFFFF"));

					GradientDrawable defaultGradient = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {startColor,endColor});

					if(useDefaults){
						l.setBackgroundResource(R.drawable.transaction_list_style);
					}
					else{

						l.setBackgroundDrawable(defaultGradient);
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
				}

				try{
					String DefaultSize = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_nameSize), "18");
					TextView t;

					t=(TextView)v.findViewById(R.id.transaction_name);

					if(useDefaults){
						t.setTextSize(18);
					}
					else{
						t.setTextSize(Integer.parseInt(DefaultSize));
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

				try{
					int DefaultColor = prefs.getInt("key_transaction_nameColor", Color.parseColor("#000000"));
					TextView t;
					t=(TextView)v.findViewById(R.id.transaction_name);

					if(useDefaults){
						t.setTextColor(Color.parseColor("#000000"));
					}
					else{
						t.setTextColor(DefaultColor);
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
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
						tmp=(TextView)v.findViewById(R.id.transaction_category);
						tmp.setTextSize(10);
						tmp=(TextView)v.findViewById(R.id.transaction_memo);
						tmp.setTextSize(10);
						tmp=(TextView)v.findViewById(R.id.transaction_checknum);
						tmp.setTextSize(10);
						tmp=(TextView)v.findViewById(R.id.transaction_cleared);
						tmp.setTextSize(10);
						tmp=(TextView)v.findViewById(R.id.transaction_type);
						tmp.setTextSize(10);
					}
					else{
						tmp=(TextView)v.findViewById(R.id.transaction_value);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_date);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_time);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_category);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_memo);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_checknum);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_cleared);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
						tmp=(TextView)v.findViewById(R.id.transaction_type);
						tmp.setTextSize(Integer.parseInt(DefaultSize));
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
				}

				try{
					int DefaultColor = prefs.getInt("key_transaction_fieldColor", Color.parseColor("#0099CC"));
					TextView tmp;

					if(useDefaults){
						tmp=(TextView)v.findViewById(R.id.transaction_value);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_date);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_time);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_category);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_memo);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_checknum);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_cleared);
						tmp.setTextColor(Color.parseColor("#0099CC"));
						tmp=(TextView)v.findViewById(R.id.transaction_type);
						tmp.setTextColor(Color.parseColor("#0099CC"));
					}
					else{
						tmp=(TextView)v.findViewById(R.id.transaction_value);
						tmp.setTextColor(DefaultColor);
						tmp=(TextView)v.findViewById(R.id.transaction_date);
						tmp.setTextColor(DefaultColor);
						tmp=(TextView)v.findViewById(R.id.transaction_time);
						tmp.setTextColor(DefaultColor);
						tmp=(TextView)v.findViewById(R.id.transaction_category);
						tmp.setTextColor(DefaultColor);
						tmp=(TextView)v.findViewById(R.id.transaction_memo);
						tmp.setTextColor(DefaultColor);
						tmp=(TextView)v.findViewById(R.id.transaction_checknum);
						tmp.setTextColor(DefaultColor);
						tmp=(TextView)v.findViewById(R.id.transaction_cleared);
						tmp.setTextColor(DefaultColor);
						tmp=(TextView)v.findViewById(R.id.transaction_type);
						tmp.setTextColor(DefaultColor);
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
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
					name.setVisibility(View.GONE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_valueField", true)){
					value.setVisibility(View.VISIBLE);
				}
				else{
					value.setVisibility(View.GONE);
				}

				if(prefs.getBoolean("checkbox_transaction_typeField", false)){
					type.setVisibility(View.VISIBLE);
				}
				else{
					type.setVisibility(View.GONE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_categoryField", true)){
					category.setVisibility(View.VISIBLE);
				}
				else{
					category.setVisibility(View.GONE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_checknumField", true)){
					checknum.setVisibility(View.VISIBLE);
				}
				else{
					checknum.setVisibility(View.GONE);
				}

				if(prefs.getBoolean("checkbox_transaction_memoField", false)){
					memo.setVisibility(View.VISIBLE);
				}
				else{
					memo.setVisibility(View.GONE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_dateField", true)){
					date.setVisibility(View.VISIBLE);
				}
				else{
					date.setVisibility(View.GONE);
				}

				if(useDefaults||prefs.getBoolean("checkbox_transaction_timeField", true)){
					time.setVisibility(View.VISIBLE);
				}
				else{
					time.setVisibility(View.GONE);
				}

				if(prefs.getBoolean("checkbox_transaction_clearedField", false)){
					cleared.setVisibility(View.VISIBLE);
				}
				else{
					cleared.setVisibility(View.GONE);
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
						if(user.type.contains("Deposit")){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(user.type.contains("Deposit")){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}
					}

				}
				catch(Exception e){
					Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
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

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Toast.makeText(this, "Options Just Changed: Transactions.Java", Toast.LENGTH_SHORT).show();
		populate();
	}

	//Calculates the balance
	public void calculateBalance(int id){
		TextView balance = (TextView)this.myFragmentView.findViewById(R.id.transaction_total_balance);
		balance.setText("Total Balance: " + totalBalance);

		//Update account with accurate balance
		String sqlCommand = "UPDATE " + tblAccounts + " SET AcctBalance = " + totalBalance + " WHERE AcctID = " + id + ";";

		//Open Database
		myDB = getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

		//Update Record
		myDB.execSQL(sqlCommand);

		//Close Database if Opened
		if (myDB != null){
			myDB.close();
		}

	}

	//Override default resume to also call populate in case view needs refreshing
	@Override
	public void onResume(){
		populate();
		super.onResume();
	}

	//Method Called to refresh the list of categories if user changes the list
	public void categoryPopulate(){

		// Cursor is used to navigate the query results
		myDB = this.getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

		final String sqlCategoryPopulate = "SELECT ToCatID as _id,SubCatName as CatName FROM " + tblSubCategory
				+ " ORDER BY _id;";

		//Can use this to combine category/subcategories
		//		String sqlCategoryPopulate = " SELECT CatID as _id,CatName FROM " + tblCategory + 
		//				" UNION " + 
		//				" SELECT ToCatID as _id, SubCatName FROM " + tblSubCategory + " ORDER BY _id";

		categoryCursor = myDB.rawQuery(sqlCategoryPopulate, null);
		getActivity().startManagingCursor(categoryCursor);
		String[] from = new String[] {"CatName"}; 
		int[] to = new int[] { android.R.id.text1 };

		categorySpinnerAdapter = new SimpleCursorAdapter(this.getActivity(), android.R.layout.simple_spinner_item, categoryCursor, from, to);
		categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tCategory.setAdapter(categorySpinnerAdapter);

		//Close Database
		if (myDB != null){
			myDB.close();
		}

	}//end of categoryPopulate

	//Close dialogs to prevent window leaks
	@Override
	public void onPause() {
		if(alertDialogView!=null){
			alertDialogView.dismiss();
		}
		if(alertDialogEdit!=null){
			alertDialogEdit.dismiss();
		}
		if(alertDialogAdd!=null){
			alertDialogAdd.dismiss();
		}
		if(categoryCursor!=null){
			categoryCursor.close();
		}

		super.onPause();
	}

}//end Transactions