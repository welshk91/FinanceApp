package com.databases.example;

import java.util.ArrayList;
import java.util.Calendar;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.slidingmenu.lib.SlidingMenu;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class Schedule extends SherlockActivity{

	public final String dbFinance = "dbFinance";
	final String tblPlanTrans = "tblPlanTrans";
	final String tblAccounts = "tblAccounts";
	final String tblSubCategory = "tblSubCategory";
	public SQLiteDatabase myDB = null;
	private SliderMenu menu;

	final int ACTIONBAR_MENU_ADD_PLAN_ID = 5882300;

	//Adapter for category spinner
	SimpleCursorAdapter categorySpinnerAdapter = null;
	Spinner categorySpinner;

	//Adapter for category spinner
	SimpleCursorAdapter accountSpinnerAdapter = null;
	Spinner accountSpinner;

	ArrayList<String> dropdownResults = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}//end onCreate

	//For Adding a Transaction
	public void transactionAdd(){
		AlertDialog alertDialogAdd;

		// get transaction_add.xml view
		LayoutInflater li = LayoutInflater.from(this);
		final View promptsView = li.inflate(R.layout.schedule_transaction_add, null);

		final EditText tName = (EditText) promptsView.findViewById(R.id.EditTransactionName);
		final EditText tValue = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
		final Spinner tType = (Spinner)promptsView.findViewById(R.id.spinner_transaction_type);
		categorySpinner = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);
		accountSpinner = (Spinner)promptsView.findViewById(R.id.spinner_transaction_account);
		final AutoCompleteTextView tMemo = (AutoCompleteTextView)promptsView.findViewById(R.id.EditTransactionMemo);
		final CheckBox tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);

		//Adapter for memo's autocomplete
		ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, dropdownResults);
		tMemo.setAdapter(dropdownAdapter);

		//Add dictionary back to autocomplete
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
		tMemo.setKeyListener(input);

		//Populate Category Drop-down List
		categoryPopulate();

		//Populate Account Drop-down List
		accountPopulate();

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set account_add.xml to AlertDialog builder
		alertDialogBuilder.setView(promptsView);

		//set Title
		alertDialogBuilder.setTitle("Schedule A Transaction");

		// set dialog message
		alertDialogBuilder
		.setCancelable(false)
		.setPositiveButton("Add",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				//Variables for the transaction Table
				String transactionAccount = null;
				String transactionName = null;
				String transactionValue = null;
				String transactionType = null;
				String transactionCategory = null;
				String transactionCheckNum = null;
				String transactionMemo = null;
				String transactionCleared = null;
				String transactionCategoryID = null;
				String transactionAccountID = null;

				//Needed to get category's name from DB-populated spinner
				int categoryPosition = categorySpinner.getSelectedItemPosition();
				Cursor cursorCategory = (Cursor) categorySpinnerAdapter.getItem(categoryPosition);	

				//Needed to get account's name from DB-populated spinner
				int accountPosition = accountSpinner.getSelectedItemPosition();
				Cursor cursorAccount = (Cursor) accountSpinnerAdapter.getItem(accountPosition);				

				transactionName = tName.getText().toString().trim();
				transactionValue = tValue.getText().toString().trim();
				transactionType = tType.getSelectedItem().toString().trim();

				try{
					//	transactionCategoryID = cursorCategory.getString(cursorCategory.getColumnIndex("ToCatId"));
					transactionCategory = cursorCategory.getString(cursorCategory.getColumnIndex("SubCatName"));
				}
				catch(Exception e){
					//Usually caused if no category exists
					Log.e("transactionAdd","No Category? Exception e:" + e);
					dialog.cancel();
					Toast.makeText(Schedule.this, "Needs A Category \n\nUse The Side Menu->Categories To Create Categories", Toast.LENGTH_LONG).show();
				}

				try{
					transactionAccount = cursorAccount.getString(cursorAccount.getColumnIndex("AcctName"));
					transactionAccountID = cursorCategory.getString(cursorCategory.getColumnIndex("_id"));
				}
				catch(Exception e){
					//Usually caused if no category exists
					Log.e("transactionAdd","No Account? Exception e:" + e);
					dialog.cancel();
					Toast.makeText(Schedule.this, "Needs An Account \n\nUse The Side Menu->Checkbook To Create Accounts", Toast.LENGTH_LONG).show();
				}

				transactionMemo = tMemo.getText().toString().trim();
				transactionCleared = tCleared.isChecked()+"";

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
					if (transactionName.length()>0) {

						if(!validValue){
							transactionValue = "0";
						}

						/****CALL INTENT TO SCHEDULE HERE*****/
						//schedule();

						Log.e("Schedule", transactionAccountID + transactionAccount + transactionName + transactionValue + transactionType + transactionCategory + transactionMemo + transactionCleared);

						//Insert values into accounts table
						//ContentValues transactionValues=new ContentValues();
						//transactionValues.put("ToAcctID",accounnt_id);
						//transactionValues.put("ToPlanID",0);
						//transactionValues.put("TransName",transactionName);
						//transactionValues.put("TransValue",transactionValue);
						//transactionValues.put("TransType",transactionType);
						//transactionValues.put("TransCategory",transactionCategory);
						//transactionValues.put("TransCheckNum",transactionCheckNum);
						//transactionValues.put("TransMemo",transactionMemo);
						//transactionValues.put("TransCleared",transactionCleared);

					} 

					else {
						Toast.makeText(Schedule.this, "Needs a Name", Toast.LENGTH_LONG).show();
					}
				}
				catch(Exception e){
					Toast.makeText(Schedule.this, "Error Adding Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
				}

				//Close cursor
				cursorCategory.close();
				cursorAccount.close();

				//Transactions.this.populate();

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

	//Method to get the list of categories for spinner
	public void categoryPopulate(){
		// Cursor is used to navigate the query results
		myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		final String sqlCategoryPopulate = "SELECT ToCatID as _id, SubCatName FROM " + tblSubCategory
				+ " ORDER BY _id;";

		Cursor categoryCursor = myDB.rawQuery(sqlCategoryPopulate, null);
		startManagingCursor(categoryCursor);
		String[] from = new String[] {"SubCatName"}; 
		int[] to = new int[] { android.R.id.text1 };

		categorySpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, categoryCursor, from, to);
		categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(categorySpinnerAdapter);

		//Close Database
		if (myDB != null){
			myDB.close();
		}

	}//end of categoryPopulate

	//Method to get the list of accounts for spinner
	public void accountPopulate(){

		// Cursor is used to navigate the query results
		myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		final String sqlAccountPopulate = "SELECT AcctID as _id,AcctName FROM " + tblAccounts
				+ " ;";

		Cursor accountCursor = myDB.rawQuery(sqlAccountPopulate, null);
		startManagingCursor(accountCursor);
		String[] from = new String[] {"AcctName", "_id"}; 
		int[] to = new int[] { android.R.id.text1};

		accountSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, accountCursor, from, to);
		accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accountSpinner.setAdapter(accountSpinnerAdapter);

		//Close Database
		if (myDB != null){
			myDB.close();
		}

	}//end of accountPopulate

	private void schedule() {
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		// add 5 minutes to the calendar object
		cal.add(Calendar.SECOND, 10);
		Intent intent = new Intent(this, PlanReceiver.class);
		intent.putExtra("alarm_message", "O'Doyle Rules!");
		// In reality, you would want to have a static variable for the request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(this, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
		am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000*8, sender);
	}

	//For ActionBar Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		//Show Search
		MenuItem menuSearch = menu.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_search, com.actionbarsherlock.view.Menu.NONE, "Search");
		menuSearch.setIcon(android.R.drawable.ic_menu_search);
		menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		MenuItem subMenu1Item = menu.add(com.actionbarsherlock.view.Menu.NONE, ACTIONBAR_MENU_ADD_PLAN_ID, com.actionbarsherlock.view.Menu.NONE, "Add");
		subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;

	}

	//For ActionBar Menu Items (and home icon)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			menu.toggle();
			break;

		case ACTIONBAR_MENU_ADD_PLAN_ID:
			transactionAdd();
			break;

		case R.id.account_menu_search:    
			onSearchRequested();
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

}//end of PlanReceiver