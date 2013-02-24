package com.databases.example;

import java.util.ArrayList;
import java.util.Calendar;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.databases.example.Accounts.AccountRecord;
import com.databases.example.Categories.CategoryRecord;
import com.databases.example.Categories.SubCategoryRecord;
import com.databases.example.Categories.UserItemAdapter;
import com.slidingmenu.lib.SlidingMenu;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

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

	//Constants for ContextMenu
	final int CONTEXT_MENU_OPEN=1;
	final int CONTEXT_MENU_EDIT=2;
	final int CONTEXT_MENU_DELETE=3;

	UserItemAdapter adapterPlans;

	ListView lvPlans;

	//For Memo autocomplete
	ArrayList<String> dropdownResults = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		setTitle("Schedule");
		setContentView(R.layout.schedule);

		lvPlans = (ListView)this.findViewById(R.id.schedule_list);

		//Turn clicks on
		lvPlans.setClickable(true);
		lvPlans.setLongClickable(true);

		//Allows Context Menus for each item of the list view
		registerForContextMenu(lvPlans);

		schedulePopulate();

	}//end onCreate

	//Method to list all plans
	public void schedulePopulate(){
		//A textView alerting the user if database is empty
		TextView noResult = (TextView)this.findViewById(R.id.schedule_noPlans);
		noResult.setVisibility(View.GONE);

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, this.MODE_PRIVATE, null);

		Cursor cursorPlans = myDB.query(tblPlanTrans, new String[] { "PlanID as _id", "ToAcctID", "PlanName", "PlanValue", "PlanType", "PlanCategory", "PlanMemo", "PlanOffset", "PlanRate", "PlanCleared"}, null,
				null, null, null, null);

		startManagingCursor(cursorPlans);
		int IDColumn = cursorPlans.getColumnIndex("PlanID");
		int ToIDColumn = cursorPlans.getColumnIndex("ToAcctID");
		int NameColumn = cursorPlans.getColumnIndex("PlanName");
		int ValueColumn = cursorPlans.getColumnIndex("PlanValue");
		int TypeColumn = cursorPlans.getColumnIndex("PlanType");
		int CategoryColumn = cursorPlans.getColumnIndex("PlanCategory");
		int MemoColumn = cursorPlans.getColumnIndex("PlanMemo");
		int OffsetColumn = cursorPlans.getColumnIndex("PlanOffset");
		int RateColumn = cursorPlans.getColumnIndex("PlanRate");
		int ClearedColumn = cursorPlans.getColumnIndex("PlanCleared");

		cursorPlans.moveToFirst();
		if (cursorPlans != null) {
			if (cursorPlans.isFirst()) {
				do {
					String id = cursorPlans.getString(0);
					String to_id = cursorPlans.getString(ToIDColumn);
					String name = cursorPlans.getString(NameColumn);
					String value = cursorPlans.getString(ValueColumn);
					String type = cursorPlans.getString(TypeColumn);
					String category = cursorPlans.getString(CategoryColumn);
					String memo = cursorPlans.getString(MemoColumn);
					String offset = cursorPlans.getString(OffsetColumn);
					String rate = cursorPlans.getString(RateColumn);
					String cleared = cursorPlans.getString(ClearedColumn);

					//PlanRecord entry = new PlanRecord(id, to_id, name, value, type, category, memo, offset, rate, cleared);
					//Log.d("Category", "Added Category: " + id + " " + name + " " + note);
					//resultsCategory.add(entry);

				} while (cursorPlans.moveToNext());
			}

			else {
				//No Results Found
				noResult.setVisibility(View.VISIBLE);
				Log.d("Schedule", "No Plans found");
			}
		} 

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		//Give the item adapter a list of all categories and subcategories
		adapterPlans = new UserItemAdapter(this, cursorPlans);		
		lvPlans.setAdapter(adapterPlans);

		//Log.e("Categories","out of category populate");

	}//end of categoryPopulate	

	//For Adding a Transaction
	public void schedulingAdd(){
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
				String transactionAccountID = null;
				String transactionAccount = null;
				String transactionName = null;
				String transactionValue = null;
				String transactionType = null;
				String transactionCategory = null;
				String transactionMemo = null;
				String transactionOffset = null;
				String transactionRate = null;
				String transactionCleared = null;

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
						ContentValues transactionValues=new ContentValues();
						//transactionValues.put("PlanID",1);
						transactionValues.put("ToAcctID",transactionAccountID);
						transactionValues.put("PlanName",transactionName);
						transactionValues.put("PlanValue",transactionValue);
						transactionValues.put("PlanType",transactionType);
						transactionValues.put("PlanCategory",transactionCategory);
						transactionValues.put("PlanMemo",transactionMemo);
						transactionValues.put("PlanOffset",transactionOffset);
						transactionValues.put("PlanRate",transactionRate);
						transactionValues.put("PlanCleared",transactionCleared);

						//Create database and open
						myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

						myDB.insert(tblPlanTrans, null, transactionValues);

						//Make sure Database is closed
						if (myDB != null){
							myDB.close();
						}

						//Refresh the categories list
						schedulePopulate();

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

	//Delete Category
	public void schedulingDelete(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		PlanRecord record = adapterPlans.getPlan(itemInfo.position);

		//Open Database
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		String sqlDeleteCategory = "DELETE FROM " + tblPlanTrans + 
				" WHERE PlanID = " + record.id;

		Log.d("Schedule", "Deleting " + record.name + " id:" + record.id);

		myDB.execSQL(sqlDeleteCategory);

		//Close Database if Opened
		if (myDB != null){
			myDB.close();
		}

		//Refresh the categories list
		schedulePopulate();

	}//end categoryDelete

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
			schedulingAdd();
			break;

		case R.id.account_menu_search:    
			onSearchRequested();
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	//Creates menu for long presses
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
		String name = "" + adapterPlans.getPlan(itemInfo.position).name;

		menu.setHeaderTitle(name);  
		menu.add(0, CONTEXT_MENU_OPEN, 0, "Open");
		menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit");
		menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");
	}  

	//Handles which methods are called when using the long presses menu
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		switch (item.getItemId()) {
		case CONTEXT_MENU_OPEN:
			//Log.e("Categories","Category View pressed");
			return true;

		case CONTEXT_MENU_EDIT:
			//Log.e("Categories","Category Edit pressed");
			return true;

		case CONTEXT_MENU_DELETE:
			schedulingDelete(item);
			return true;

		default:
			Log.e("Schedules", "Context Menu defualt listener fired?");
			break;
		}

		return super.onContextItemSelected(item);

	}  


	public class UserItemAdapter extends CursorAdapter {
		private Cursor plans;
		private Context context;

		public UserItemAdapter(Context context,Cursor plans) {
			super(context, plans);
			this.plans = plans;
			this.context = context;
		}

		public PlanRecord getPlan(long position){
			Cursor group = plans;

			group.moveToPosition((int) position);
			int IDColumn = group.getColumnIndex("PlanID");
			int ToIDColumn = group.getColumnIndex("ToAcctID");
			int NameColumn = group.getColumnIndex("PlanName");
			int ValueColumn = group.getColumnIndex("PlanValue");
			int TypeColumn = group.getColumnIndex("PlanType");
			int CategoryColumn = group.getColumnIndex("PlanCategory");
			int MemoColumn = group.getColumnIndex("PlanMemo");
			int OffsetColumn = group.getColumnIndex("PlanOffset");
			int RateColumn = group.getColumnIndex("PlanRate");
			int ClearedColumn = group.getColumnIndex("PlanCleared");

			String id = group.getString(0);
			String to_id = group.getString(ToIDColumn);
			String name = group.getString(NameColumn);
			String value = group.getString(ValueColumn);
			String type = group.getString(TypeColumn);
			String category = group.getString(CategoryColumn);
			String memo = group.getString(MemoColumn);
			String offset = group.getString(OffsetColumn);
			String rate = group.getString(RateColumn);
			String cleared = group.getString(ClearedColumn);

			//Log.e("HERE", "columns " + IDColumn + " " + NameColumn + " " + NoteColumn);

			PlanRecord record = new PlanRecord(id, to_id, name, value, type, category, memo, offset, rate, cleared);
			return record;
		}

		//		@Override
		//		public View getView(int position, View convertView, ViewGroup parent) {
		//			View v = convertView;
		//			Cursor user = plans;
		//
		//			//For Custom View Properties
		//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Schedule.this);
		//			boolean useDefaults = prefs.getBoolean("checkbox_default", true);
		//
		//			if (v == null) {
		//				LayoutInflater vi = (LayoutInflater)Schedule.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//				v = vi.inflate(R.layout.schedule_item, null);
		//			}
		//
		//			if (user != null) {
		//				TextView TVname = (TextView) v.findViewById(R.id.plan_name);
		//				TextView TVvalue = (TextView) v.findViewById(R.id.plan_value);
		//				TextView TVtype = (TextView) v.findViewById(R.id.plan_type);
		//				TextView TVcategory = (TextView) v.findViewById(R.id.plan_category);
		//				TextView TVmemo = (TextView) v.findViewById(R.id.plan_memo);
		//				TextView TVoffset = (TextView) v.findViewById(R.id.plan_offset);
		//				TextView TVrate = (TextView) v.findViewById(R.id.plan_rate);
		//				TextView TVcleared = (TextView) v.findViewById(R.id.plan_cleared);
		//
		//				int IDColumn = user.getColumnIndex("PlanID");
		//				int ToIDColumn = user.getColumnIndex("ToAcctID");
		//				int NameColumn = user.getColumnIndex("PlanName");
		//				int ValueColumn = user.getColumnIndex("PlanValue");
		//				int TypeColumn = user.getColumnIndex("PlanType");
		//				int CategoryColumn = user.getColumnIndex("PlanCategory");
		//				int MemoColumn = user.getColumnIndex("PlanMemo");
		//				int OffsetColumn = user.getColumnIndex("PlanOffset");
		//				int RateColumn = user.getColumnIndex("PlanRate");
		//				int ClearedColumn = user.getColumnIndex("PlanCleared");
		//
		//				user.moveToPosition(position);
		//				String id = user.getString(0);
		//				String to_id = user.getString(ToIDColumn);
		//				String name = user.getString(NameColumn);
		//				String value = user.getString(ValueColumn);
		//				String type = user.getString(TypeColumn);
		//				String category = user.getString(CategoryColumn);
		//				String memo = user.getString(MemoColumn);
		//				String offset = user.getString(OffsetColumn);
		//				String rate = user.getString(RateColumn);
		//				String cleared = user.getString(ClearedColumn);
		//
		//				if (name != null) {
		//					TVname.setText(name);
		//				}
		//				if (value != null) {
		//					TVvalue.setText(value);
		//				}
		//				if (type != null) {
		//					TVtype.setText(type);
		//				}
		//				if (category != null) {
		//					TVcategory.setText(category);
		//				}
		//				if (memo != null) {
		//					TVmemo.setText(memo);
		//				}
		//				if (offset != null) {
		//					TVoffset.setText(offset);
		//				}
		//				if (rate != null) {
		//					TVrate.setText(rate);
		//				}
		//				if (cleared != null) {
		//					TVcleared.setText(cleared);
		//				}
		//
		//			}
		//			return v;
		//		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View v = view;
			Cursor user = plans;

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Schedule.this);
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (user != null) {
				TextView TVname = (TextView) v.findViewById(R.id.plan_name);
				TextView TVvalue = (TextView) v.findViewById(R.id.plan_value);
				TextView TVtype = (TextView) v.findViewById(R.id.plan_type);
				TextView TVcategory = (TextView) v.findViewById(R.id.plan_category);
				TextView TVmemo = (TextView) v.findViewById(R.id.plan_memo);
				TextView TVoffset = (TextView) v.findViewById(R.id.plan_offset);
				TextView TVrate = (TextView) v.findViewById(R.id.plan_rate);
				TextView TVcleared = (TextView) v.findViewById(R.id.plan_cleared);

				int IDColumn = user.getColumnIndex("PlanID");
				int ToIDColumn = user.getColumnIndex("ToAcctID");
				int NameColumn = user.getColumnIndex("PlanName");
				int ValueColumn = user.getColumnIndex("PlanValue");
				int TypeColumn = user.getColumnIndex("PlanType");
				int CategoryColumn = user.getColumnIndex("PlanCategory");
				int MemoColumn = user.getColumnIndex("PlanMemo");
				int OffsetColumn = user.getColumnIndex("PlanOffset");
				int RateColumn = user.getColumnIndex("PlanRate");
				int ClearedColumn = user.getColumnIndex("PlanCleared");

				String id = user.getString(0);
				String to_id = user.getString(ToIDColumn);
				String name = user.getString(NameColumn);
				String value = user.getString(ValueColumn);
				String type = user.getString(TypeColumn);
				String category = user.getString(CategoryColumn);
				String memo = user.getString(MemoColumn);
				String offset = user.getString(OffsetColumn);
				String rate = user.getString(RateColumn);
				String cleared = user.getString(ClearedColumn);

				//Change gradient
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.plan_gradient);
					GradientDrawable defaultGradientPos = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFF00FF33,0xFF000000});

					GradientDrawable defaultGradientNeg = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFFFF0000,0xFF000000});

					if(useDefaults){
						if(Float.parseFloat((value)) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(Float.parseFloat((value)) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}
					}

				}
				catch(Exception e){
					Toast.makeText(Schedule.this, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
				}

				if (name != null) {
					TVname.setText(name);
				}
				if (value != null) {
					TVvalue.setText(value);
				}
				if (type != null) {
					TVtype.setText(type);
				}
				if (category != null) {
					TVcategory.setText(category);
				}
				if (memo != null) {
					TVmemo.setText(memo);
				}
				if (offset != null) {
					TVoffset.setText(offset);
				}
				if (rate != null) {
					TVrate.setText(rate);
				}
				if (cleared != null) {
					TVcleared.setText(cleared);
				}

			}

		}

		@Override
		public View newView(Context context, Cursor plans, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.schedule_item, parent, false);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Schedule.this);
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			//Change Background Colors
			try{
				LinearLayout l;
				l=(LinearLayout)v.findViewById(R.id.plan_layout);
				int startColor = prefs.getInt("key_account_startBackgroundColor", Color.parseColor("#E8E8E8"));
				int endColor = prefs.getInt("key_account_endBackgroundColor", Color.parseColor("#FFFFFF"));
				GradientDrawable defaultGradient = new GradientDrawable(
						GradientDrawable.Orientation.BOTTOM_TOP,
						new int[] {startColor,endColor});

				if(useDefaults){
					l.setBackgroundResource(R.drawable.account_list_style);
				}
				else{
					l.setBackgroundDrawable(defaultGradient);
				}

			}
			catch(Exception e){
				Toast.makeText(Schedule.this, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
			}

			//Change Size of main field
			try{
				String DefaultSize = prefs.getString(Schedule.this.getString(R.string.pref_key_account_nameSize), "16");
				TextView t;
				t=(TextView)v.findViewById(R.id.plan_name);

				if(useDefaults){
					t.setTextSize(16);
				}
				else{
					t.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Schedule.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_account_nameColor", Color.parseColor("#000000"));
				TextView t;
				t=(TextView)v.findViewById(R.id.plan_name);

				if(useDefaults){
					t.setTextColor(Color.parseColor("#000000"));
				}
				else{
					t.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Schedule.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				String DefaultSize = prefs.getString(Schedule.this.getString(R.string.pref_key_account_fieldSize), "10");
				TextView tmp;

				if(useDefaults){
					tmp=(TextView)v.findViewById(R.id.plan_value);
					tmp.setTextSize(10);
					tmp=(TextView)v.findViewById(R.id.plan_type);
					tmp.setTextSize(10);
					tmp=(TextView)v.findViewById(R.id.plan_category);
					tmp.setTextSize(10);
					tmp=(TextView)v.findViewById(R.id.plan_memo);
					tmp.setTextSize(10);
					tmp=(TextView)v.findViewById(R.id.plan_offset);
					tmp.setTextSize(10);
					tmp=(TextView)v.findViewById(R.id.plan_rate);
					tmp.setTextSize(10);
					tmp=(TextView)v.findViewById(R.id.plan_cleared);
					tmp.setTextSize(10);
				}
				else{
					tmp=(TextView)v.findViewById(R.id.plan_value);
					tmp.setTextSize(Integer.parseInt(DefaultSize));
					tmp=(TextView)v.findViewById(R.id.plan_type);
					tmp.setTextSize(Integer.parseInt(DefaultSize));
					tmp=(TextView)v.findViewById(R.id.plan_category);
					tmp.setTextSize(Integer.parseInt(DefaultSize));
					tmp=(TextView)v.findViewById(R.id.plan_memo);
					tmp.setTextSize(Integer.parseInt(DefaultSize));
					tmp=(TextView)v.findViewById(R.id.plan_offset);
					tmp.setTextSize(Integer.parseInt(DefaultSize));
					tmp=(TextView)v.findViewById(R.id.plan_rate);
					tmp.setTextSize(Integer.parseInt(DefaultSize));
					tmp=(TextView)v.findViewById(R.id.plan_cleared);
					tmp.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Schedule.this, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_account_fieldColor", Color.parseColor("#0099CC"));
				TextView tmp;

				if(useDefaults){
					tmp=(TextView)v.findViewById(R.id.plan_value);
					tmp.setTextColor(Color.parseColor("#0099CC"));
					tmp=(TextView)v.findViewById(R.id.plan_type);
					tmp.setTextColor(Color.parseColor("#0099CC"));
					tmp=(TextView)v.findViewById(R.id.plan_category);
					tmp.setTextColor(Color.parseColor("#0099CC"));
					tmp=(TextView)v.findViewById(R.id.plan_memo);
					tmp.setTextColor(Color.parseColor("#0099CC"));
					tmp=(TextView)v.findViewById(R.id.plan_offset);
					tmp.setTextColor(Color.parseColor("#0099CC"));
					tmp=(TextView)v.findViewById(R.id.plan_rate);
					tmp.setTextColor(Color.parseColor("#0099CC"));
					tmp=(TextView)v.findViewById(R.id.plan_cleared);
					tmp.setTextColor(Color.parseColor("#0099CC"));
				}
				else{
					tmp=(TextView)v.findViewById(R.id.plan_value);
					tmp.setTextColor(DefaultColor);
					tmp=(TextView)v.findViewById(R.id.plan_type);
					tmp.setTextColor(DefaultColor);
					tmp=(TextView)v.findViewById(R.id.plan_category);
					tmp.setTextColor(DefaultColor);
					tmp=(TextView)v.findViewById(R.id.plan_memo);
					tmp.setTextColor(DefaultColor);
					tmp=(TextView)v.findViewById(R.id.plan_offset);
					tmp.setTextColor(DefaultColor);
					tmp=(TextView)v.findViewById(R.id.plan_rate);
					tmp.setTextColor(DefaultColor);
					tmp=(TextView)v.findViewById(R.id.plan_cleared);
					tmp.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Schedule.this, "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
			}


			return v;
		}
	}//end UserItem

	//An Object Class used to hold the data of each transaction record
	public class PlanRecord {
		protected String id;
		protected String acctId;
		protected String name;
		protected String value;
		protected String type;
		protected String category;
		protected String memo;
		protected String offset;
		protected String rate;
		protected String cleared;

		public PlanRecord(String id, String acctId, String name, String value, String type, String category, String memo, String offset, String rate, String cleared) {
			this.id = id;
			this.acctId = acctId;
			this.name = name;
			this.value = value;
			this.type = type;
			this.category = category;
			this.memo = memo;
			this.offset = offset;
			this.rate = rate;
			this.cleared = cleared;
		}
	}

}//end of Schedule