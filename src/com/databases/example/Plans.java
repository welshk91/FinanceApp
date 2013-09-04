package com.databases.example;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Plans extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	private SliderMenu menu;

	final int ACTIONBAR_MENU_ADD_PLAN_ID = 5882300;

	//Adapter for category spinner
	SimpleCursorAdapter categorySpinnerAdapter = null;
	Spinner categorySpinner;

	//Alert Dialogs (Need to be closed properly)
	AlertDialog alertDialogView;
	AlertDialog alertDialogAdd;
	AlertDialog alertDialogEdit;

	private static final int PLAN_LOADER = 5882300;

	//Cursor (Need to be closed properly)
	Cursor cursorPlans;

	//Adapter for category spinner
	SimpleCursorAdapter accountSpinnerAdapter = null;
	Spinner accountSpinner;

	//Date Format to use for time (01:42 PM)
	final static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

	//Date Format to use for date (03-26-2013)
	final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");		

	//Variables for the transaction Table
	static String transactionDate = null;

	//Constants for ContextMenu
	final int CONTEXT_MENU_OPEN=1;
	final int CONTEXT_MENU_EDIT=2;
	final int CONTEXT_MENU_DELETE=3;
	final int CONTEXT_MENU_CANCEL=4;

	//Dialog for Adding Transaction
	static View promptsView;

	static Button pDate;

	UserItemAdapter adapterPlans;

	ListView lvPlans;

	//For Memo autocomplete
	ArrayList<String> dropdownResults = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setTitle("Plans");
		setContentView(R.layout.plans);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		lvPlans = (ListView)this.findViewById(R.id.plans_list);

		//Turn clicks on
		lvPlans.setClickable(true);
		lvPlans.setLongClickable(true);

		//Allows Context Menus for each item of the list view
		registerForContextMenu(lvPlans);

		TextView noResult = (TextView)findViewById(R.id.plans_noPlans);
		lvPlans.setEmptyView(noResult);
		
		adapterPlans = new UserItemAdapter(this, cursorPlans);		
		lvPlans.setAdapter(adapterPlans);

		plansPopulate();

	}//end onCreate

	//Method to list all plans
	public void plansPopulate(){
		getSupportLoaderManager().initLoader(PLAN_LOADER, null, this);
	}

	//For Scheduling a Transaction
	public void schedulingAdd(){
		LayoutInflater li = LayoutInflater.from(this);
		promptsView = li.inflate(R.layout.plan_add, null);

		final EditText tName = (EditText) promptsView.findViewById(R.id.EditTransactionName);
		final EditText tValue = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
		final Spinner tType = (Spinner)promptsView.findViewById(R.id.spinner_transaction_type);
		categorySpinner = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);
		accountSpinner = (Spinner)promptsView.findViewById(R.id.spinner_transaction_account);
		final AutoCompleteTextView tMemo = (AutoCompleteTextView)promptsView.findViewById(R.id.EditTransactionMemo);
		final EditText tRate = (EditText) promptsView.findViewById(R.id.EditRate);
		final Spinner rateSpinner = (Spinner)promptsView.findViewById(R.id.spinner_rate_type);
		final CheckBox tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);

		final Calendar c = Calendar.getInstance();
		pDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);
		pDate.setText(dateFormat.format(c.getTime()));

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
					transactionAccount = cursorAccount.getString(cursorAccount.getColumnIndex("AcctName"));
					transactionAccountID = cursorAccount.getString(cursorAccount.getColumnIndex("_id"));
				}
				catch(Exception e){
					//Usually caused if no category exists
					Log.e("transactionAdd","No Account? Exception e:" + e);
					dialog.cancel();
					Toast.makeText(Plans.this, "Needs An Account \n\nUse The Side Menu->Checkbook To Create Accounts", Toast.LENGTH_LONG).show();
					return;
				}

				try{
					//	transactionCategoryID = cursorCategory.getString(cursorCategory.getColumnIndex("ToCatId"));
					transactionCategory = cursorCategory.getString(cursorCategory.getColumnIndex("SubCatName"));
				}
				catch(Exception e){
					//Usually caused if no category exists
					Log.e("transactionAdd","No Category? Exception e:" + e);
					dialog.cancel();
					Toast.makeText(Plans.this, "Needs A Category \n\nUse The Side Menu->Categories To Create Categories", Toast.LENGTH_LONG).show();
					return;
				}

				transactionMemo = tMemo.getText().toString().trim();

				//Set Time
				transactionOffset = pDate.getText().toString().trim();
				transactionRate = tRate.getText().toString().trim() + " " + rateSpinner.getSelectedItem().toString().trim();
				transactionCleared = tCleared.isChecked()+"";

				//Check to see if value is a number
				boolean validValue=false;
				try{
					Float.parseFloat(transactionValue);
					validValue=true;
				}
				catch(Exception e){
					Log.e("Plans","Value not valid; transactionValue=" + transactionValue);
					validValue=false;
				}

				//Check to see if value is a number
				boolean validRate=false;
				try{
					Integer.parseInt(tRate.getText().toString().trim());
					validRate=true;
				}
				catch(Exception e){
					Log.e("Plans","Rate not valid; Edit Text rate=" + tRate.getText().toString().trim());
					validRate=false;
				}

				try{
					if (transactionName.length()>0 && validRate && validValue) {
						Log.d("Plans", transactionAccountID + transactionAccount + transactionName + transactionValue + transactionType + transactionCategory + transactionMemo + transactionOffset + transactionRate + transactionCleared);

						ContentValues transactionValues = new ContentValues();
						transactionValues.put("ToAcctID", transactionAccountID);
						transactionValues.put("PlanName", transactionName);
						transactionValues.put("PlanValue", transactionValue);
						transactionValues.put("PlanType", transactionType);
						transactionValues.put("PlanCategory", transactionCategory);
						transactionValues.put("PlanMemo", transactionMemo);
						transactionValues.put("PlanOffset", transactionOffset);
						transactionValues.put("PlanRate", transactionRate);
						transactionValues.put("PlanCleared", transactionCleared);

						Uri u = getContentResolver().insert(MyContentProvider.PLANNED_TRANSACTIONS_URI, transactionValues);

						PlanRecord record = new PlanRecord(u.getLastPathSegment(), transactionAccountID, transactionName, transactionValue, transactionType, transactionCategory, transactionMemo, transactionOffset, transactionRate, transactionCleared);
						schedule(record);

						//Refresh the plans list
						plansPopulate();
					} 

					else {
						Toast.makeText(Plans.this, "Transactions need a Name, Value, and Rate", Toast.LENGTH_LONG).show();
					}
				}
				catch(Exception e){
					Log.e("Plans-Edit", "e = " + e);
					Toast.makeText(Plans.this, "Error Adding Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
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

	//Delete Plan
	public void schedulingDelete(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		PlanRecord record = adapterPlans.getPlan(itemInfo.position);

		Uri uri = Uri.parse(MyContentProvider.PLANNED_TRANSACTIONS_URI + "/" + record.id);
		this.getContentResolver().delete(uri, "PlanID="+record.id, null);

		Log.d("Plans", "Deleting " + record.name + " id:" + record.id);

		//Cancel all upcoming notifications
		cancelPlan(record);

		//Refresh the categories list
		plansPopulate();

	}//end categoryDelete

	//For Editing a Scheduled Transaction
	public void schedulingEdit(final android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		PlanRecord record = adapterPlans.getPlan(itemInfo.position);

		LayoutInflater li = LayoutInflater.from(this);
		promptsView = li.inflate(R.layout.plan_add, null);

		final EditText tName = (EditText) promptsView.findViewById(R.id.EditTransactionName);
		final EditText tValue = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
		final Spinner tType = (Spinner)promptsView.findViewById(R.id.spinner_transaction_type);
		categorySpinner = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);
		accountSpinner = (Spinner)promptsView.findViewById(R.id.spinner_transaction_account);
		final AutoCompleteTextView tMemo = (AutoCompleteTextView)promptsView.findViewById(R.id.EditTransactionMemo);
		final EditText tRate = (EditText) promptsView.findViewById(R.id.EditRate);
		final Spinner rateSpinner = (Spinner)promptsView.findViewById(R.id.spinner_rate_type);
		final CheckBox tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);
		pDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);

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

		tName.setText(record.name);
		tValue.setText(record.value);
		ArrayAdapter<String> typeAdap = (ArrayAdapter<String>) tType.getAdapter();
		int spinnerPosition = typeAdap.getPosition(record.type);
		tType.setSelection(spinnerPosition);

		//Used to find correct category to select
		for (int i = 0; i < categorySpinner.getCount(); i++) {
			Cursor value = (Cursor) categorySpinner.getItemAtPosition(i);
			String name = value.getString(value.getColumnIndex("SubCatName"));
			if (name.contentEquals(record.category)) {
				categorySpinner.setSelection(i);
				break;
			}
		}

		//Used to find correct account to select
		for (int i = 0; i < accountSpinner.getCount(); i++) {
			Cursor value = (Cursor) accountSpinner.getItemAtPosition(i);
			String id = value.getString(value.getColumnIndex("_id"));
			if (id.contentEquals(record.acctId)) {
				accountSpinner.setSelection(i);
				break;
			}
		}

		tMemo.setText(record.memo);
		pDate.setText(record.offset);

		//Parse Rate (token 0 is amount, token 1 is type)
		String phrase = record.rate;
		String delims = "[ ]+";
		String[] tokens = phrase.split(delims);

		tRate.setText(tokens[0]);

		ArrayAdapter<String> rateAdap = (ArrayAdapter<String>) rateSpinner.getAdapter();
		int spinnerPosition4 = rateAdap.getPosition(tokens[1]);
		rateSpinner.setSelection(spinnerPosition4);

		tCleared.setChecked(Boolean.parseBoolean(record.cleared));

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set account_add.xml to AlertDialog builder
		alertDialogBuilder.setView(promptsView);

		//set Title
		alertDialogBuilder.setTitle("Editing A Scheduled Transaction");

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
					transactionAccount = cursorAccount.getString(cursorAccount.getColumnIndex("AcctName"));
					transactionAccountID = cursorAccount.getString(cursorAccount.getColumnIndex("_id"));
				}
				catch(Exception e){
					//Usually caused if no category exists
					Log.e("transactionAdd","No Account? Exception e:" + e);
					dialog.cancel();
					Toast.makeText(Plans.this, "Needs An Account \n\nUse The Side Menu->Checkbook To Create Accounts", Toast.LENGTH_LONG).show();
					return;
				}

				try{
					//	transactionCategoryID = cursorCategory.getString(cursorCategory.getColumnIndex("ToCatId"));
					transactionCategory = cursorCategory.getString(cursorCategory.getColumnIndex("SubCatName"));
				}
				catch(Exception e){
					//Usually caused if no category exists
					Log.e("transactionAdd","No Category? Exception e:" + e);
					dialog.cancel();
					Toast.makeText(Plans.this, "Needs A Category \n\nUse The Side Menu->Categories To Create Categories", Toast.LENGTH_LONG).show();
					return;
				}

				transactionMemo = tMemo.getText().toString().trim();

				//Set Time
				transactionOffset = pDate.getText().toString().trim();
				transactionRate = tRate.getText().toString().trim() + " " + rateSpinner.getSelectedItem().toString().trim();
				transactionCleared = tCleared.isChecked()+"";

				//Check to see if value is a number
				boolean validValue=false;
				try{
					Float.parseFloat(transactionValue);
					validValue=true;
				}
				catch(Exception e){
					Log.e("Plans-Edit","Value not valid; transactionValue=" + transactionValue);
					validValue=false;
				}

				//Check to see if value is a number
				boolean validRate=false;
				try{
					Integer.parseInt(tRate.getText().toString().trim());
					validRate=true;
				}
				catch(Exception e){
					Log.e("Plans-Edit","Rate not valid; Edit Text rate=" + tRate.getText().toString().trim());
					validRate=false;
				}

				try{
					if (transactionName.length()>0 && validRate && validValue) {

						Log.d("Plans-Edit", transactionAccountID + transactionAccount + transactionName + transactionValue + transactionType + transactionCategory + transactionMemo + transactionOffset + transactionRate + transactionCleared);

						schedulingDelete(item);

						ContentValues transactionValues=new ContentValues();
						transactionValues.put("ToAcctID", transactionAccountID);
						transactionValues.put("PlanName", transactionName);
						transactionValues.put("PlanValue", transactionValue);
						transactionValues.put("PlanType", transactionType);
						transactionValues.put("PlanCategory", transactionCategory);
						transactionValues.put("PlanMemo", transactionMemo);
						transactionValues.put("PlanOffset", transactionOffset);
						transactionValues.put("PlanRate", transactionRate);
						transactionValues.put("PlanCleared", transactionCleared);

						Uri u = getContentResolver().insert(MyContentProvider.PLANNED_TRANSACTIONS_URI, transactionValues);

						PlanRecord record = new PlanRecord(u.getLastPathSegment(), transactionAccountID, transactionName, transactionValue, transactionType, transactionCategory, transactionMemo, transactionOffset, transactionRate, transactionCleared);
						schedule(record);

						//Refresh the schedule list
						plansPopulate();
					} 

					else {
						Toast.makeText(Plans.this, "Transactions need a Name, Value, and Rate", Toast.LENGTH_LONG).show();
					}
				}
				catch(Exception e){
					Log.e("Plans-Edit", "e = ");
					Toast.makeText(Plans.this, "Error Adding Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
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

	}//end of transactionAdd

	//View Plan
	public void planView(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		PlanRecord record = adapterPlans.getPlan(itemInfo.position);

		LayoutInflater li = LayoutInflater.from(this);
		final View planStatsView = li.inflate(R.layout.plan_stats, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);

		// set xml to AlertDialog builder
		alertDialogBuilder.setView(planStatsView);

		//set Title
		alertDialogBuilder.setTitle("View Plan");

		// set dialog message
		alertDialogBuilder
		.setCancelable(true);

		// create alert dialog
		alertDialogView = alertDialogBuilder.create();

		//Set Statistics
		TextView statsName = (TextView)planStatsView.findViewById(R.id.TextTransactionName);
		statsName.setText(record.name);
		TextView statsAccount = (TextView)planStatsView.findViewById(R.id.TextTransactionAccount);
		statsAccount.setText(record.acctId);
		TextView statsValue = (TextView)planStatsView.findViewById(R.id.TextTransactionValue);
		statsValue.setText(record.value);
		TextView statsType = (TextView)planStatsView.findViewById(R.id.TextTransactionType);
		statsType.setText(record.type);
		TextView statsCategory = (TextView)planStatsView.findViewById(R.id.TextTransactionCategory);
		statsCategory.setText(record.category);
		TextView statsMemo = (TextView)planStatsView.findViewById(R.id.TextTransactionMemo);
		statsMemo.setText(record.memo);
		TextView statsOffset = (TextView)planStatsView.findViewById(R.id.TextTransactionOffset);
		statsOffset.setText(record.offset);
		TextView statsRate = (TextView)planStatsView.findViewById(R.id.TextTransactionRate);
		statsRate.setText(record.rate);
		TextView statsCleared = (TextView)planStatsView.findViewById(R.id.TextTransactionCleared);
		statsCleared.setText(record.cleared);

		// show it
		alertDialogView.show();

	}

	//Method to get the list of categories for spinner
	public void categoryPopulate(){
		Cursor categoryCursor = this.getContentResolver().query(MyContentProvider.SUBCATEGORIES_URI, null, null, null, null);
		startManagingCursor(categoryCursor);
		String[] from = new String[] {"SubCatName"}; 
		int[] to = new int[] { android.R.id.text1 };

		categorySpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, categoryCursor, from, to);
		categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(categorySpinnerAdapter);
	}//end of categoryPopulate

	//Method to get the list of accounts for spinner
	public void accountPopulate(){
		Cursor accountCursor = this.getContentResolver().query(MyContentProvider.ACCOUNTS_URI, null, null, null, null);
		startManagingCursor(accountCursor);
		String[] from = new String[] {"AcctName", "_id"}; 
		int[] to = new int[] { android.R.id.text1};

		accountSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, accountCursor, from, to);
		accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accountSpinner.setAdapter(accountSpinnerAdapter);
	}//end of accountPopulate

	private void schedule(PlanRecord plan) {
		PlanRecord record = plan;
		Date d = null;

		try {
			d = dateFormat.parse(record.offset);
		}catch (java.text.ParseException e) {
			Log.e("Plans-schedule", "Couldn't schedule " + record.name + "\n e:"+e);
		}

		Log.d("Plans-schedule", "d.year=" + (d.getYear()+1900) + " d.date=" + d.getDate() + " d.month=" + d.getMonth());

		Calendar firstRun = new GregorianCalendar(d.getYear()+1900,d.getMonth(),d.getDate());
		Log.d("Plans-schedule", "FirstRun:" + firstRun);

		Intent intent = new Intent(this, PlanReceiver.class);
		intent.putExtra("plan_id", record.id);
		intent.putExtra("plan_acct_id",record.acctId);
		intent.putExtra("plan_name",record.name);
		intent.putExtra("plan_value",record.value);
		intent.putExtra("plan_type",record.type);
		intent.putExtra("plan_category",record.category);
		intent.putExtra("plan_memo",record.memo);
		intent.putExtra("plan_offset",record.offset);
		intent.putExtra("plan_rate",record.rate);
		intent.putExtra("plan_cleared",record.cleared);

		//Parse Rate (token 0 is amount, token 1 is type)
		String phrase = record.rate;
		String delims = "[ ]+";
		String[] tokens = phrase.split(delims);

		// In reality, you would want to have a static variable for the request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(this, Integer.parseInt(record.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

		if(tokens[1].contains("Days")){
			Log.d("Plans-schedule", "Days");

			//If Starting Time is in the past, fire off next month(s)
			while (firstRun.before(Calendar.getInstance())) {
				firstRun.add(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[0]));
			}

			Log.d("Plans-schedule", "firstRun is " + firstRun);
			Toast.makeText(this, "Next Transaction scheduled for " + dateFormat.format(firstRun.getTime()), Toast.LENGTH_SHORT).show();

			//am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_DAY), sender);
			am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_DAY), sender);
		}
		else if(tokens[1].contains("Weeks")){
			Log.d("Plans-schedule", "Weeks");

			//If Starting Time is in the past, fire off next month(s)
			while (firstRun.before(Calendar.getInstance())) {
				firstRun.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(tokens[0]));
			}

			Log.d("Plans-schedule", "firstRun is " + firstRun);
			Toast.makeText(this, "Next Transaction scheduled for " + dateFormat.format(firstRun.getTime()), Toast.LENGTH_SHORT).show();

			//am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_DAY)*7, sender);
			am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_DAY)*7, sender);
		}
		else if(tokens[1].contains("Months")){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(cal.getTimeInMillis());
			cal.add(Calendar.MONTH, Integer.parseInt(tokens[0]));

			//If Starting Time is in the past, fire off next month(s)
			while (firstRun.before(Calendar.getInstance())) {
				firstRun.add(Calendar.MONTH, Integer.parseInt(tokens[0]));
			}

			Log.d("Plans-schedule", "firstRun is " + firstRun);
			Toast.makeText(this, "Next Transaction scheduled for " + dateFormat.format(firstRun.getTime()), Toast.LENGTH_SHORT).show();

			am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), cal.getTimeInMillis(), sender);
			//am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_FIFTEEN_MINUTES), sender);
			//am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 1000*30, sender);
		}
		else{
			Log.e("Plans-schedule", "Could not set alarm; Something wrong with the rate");
		}

		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
		//am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), 1000*20, sender);
		//am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 1000*6, sender);
	}

	private void cancelPlan(PlanRecord plan) {
		PlanRecord record = plan;

		Intent intent = new Intent(this, PlanReceiver.class);
		intent.putExtra("plan_id", record.id);
		intent.putExtra("plan_acct_id",record.acctId);
		intent.putExtra("plan_name",record.name);
		intent.putExtra("plan_value",record.value);
		intent.putExtra("plan_type",record.type);
		intent.putExtra("plan_category",record.category);
		intent.putExtra("plan_memo",record.memo);
		intent.putExtra("plan_offset",record.offset);
		intent.putExtra("plan_rate",record.rate);
		intent.putExtra("plan_cleared",record.cleared);

		// In reality, you would want to have a static variable for the request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(this, Integer.parseInt(record.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

		try {
			am.cancel(sender);
		} catch (Exception e) {
			Log.e("Plans-schedule", "AlarmManager update was not canceled. " + e.toString());
		}

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
			//PlanRecord testRecord = new PlanRecord(1+"", 1+"", "test record", "50", "Deposit", "Electrical", "Memo things", "02-25-2013", "3 Months", "false");
			//schedule(testRecord);
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
		menu.add(0, CONTEXT_MENU_CANCEL, 3, "Cancel");
	}  

	//Handles which methods are called when using the long presses menu
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		switch (item.getItemId()) {
		case CONTEXT_MENU_OPEN:
			planView(item);
			return true;

		case CONTEXT_MENU_EDIT:
			schedulingEdit(item);
			return true;

		case CONTEXT_MENU_DELETE:
			schedulingDelete(item);
			return true;

		case CONTEXT_MENU_CANCEL:
			AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			PlanRecord record = adapterPlans.getPlan(itemInfo.position);
			cancelPlan(record);
			return true;

		default:
			Log.e("Plans-onContextItemSelected", "Context Menu defualt listener fired?");
			break;
		}

		return super.onContextItemSelected(item);
	}  

	//Method for selecting a Date when adding a transaction
	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}

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
		super.onPause();
	}

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

			pDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);
			pDate.setText(transactionDate);
		}
	}

	public class UserItemAdapter extends CursorAdapter {
		private Context context;

		public UserItemAdapter(Context context,Cursor plans) {
			super(context, plans);
			this.context = context;
		}

		public PlanRecord getPlan(long position){
			Cursor group = getCursor();

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

			PlanRecord record = new PlanRecord(id, to_id, name, value, type, category, memo, offset, rate, cleared);
			return record;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View v = view;
			Cursor user = getCursor();

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Plans.this);
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (user != null) {
				TextView TVname = (TextView) v.findViewById(R.id.plan_name);
				TextView TVaccount = (TextView) v.findViewById(R.id.plan_account);
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
						if(type.contains("Deposit")){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(type.contains("Deposit")){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}
					}

				}
				catch(Exception e){
					Toast.makeText(Plans.this, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
				}

				if (name != null) {
					TVname.setText(name);
				}
				if (to_id != null) {
					TVaccount.setText("Account ID: " + to_id);
				}
				if (value != null) {
					TVvalue.setText("Value: " + value);
				}
				if (type != null) {
					TVtype.setText("Type: " + type);
				}
				if (category != null) {
					TVcategory.setText("Category: " + category);
				}
				if (memo != null) {
					TVmemo.setText("Memo: " + memo);
				}
				if (offset != null) {
					TVoffset.setText("Offset: " + offset);
				}
				if (rate != null) {
					TVrate.setText("Rate: " + rate);
				}
				if (cleared != null) {
					TVcleared.setText("Cleared: " + cleared);
				}

			}

		}

		@Override
		public View newView(Context context, Cursor plans, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.plan_item, parent, false);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Plans.this);
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
				Toast.makeText(Plans.this, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
			}

			//Change Size of main field
			try{
				String DefaultSize = prefs.getString(Plans.this.getString(R.string.pref_key_account_nameSize), "16");
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
				Toast.makeText(Plans.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
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
				Toast.makeText(Plans.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				String DefaultSize = prefs.getString(Plans.this.getString(R.string.pref_key_account_fieldSize), "10");
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
				Toast.makeText(Plans.this, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
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
				Toast.makeText(Plans.this, "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
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

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {		
		switch (loaderID) {
		case PLAN_LOADER:
			if(bundle!=null && bundle.getBoolean("boolSearch")){
				String query = this.getIntent().getStringExtra(SearchManager.QUERY);
				return new CursorLoader(
						this,   	// Parent activity context
						(Uri.parse(MyContentProvider.PLANNED_TRANSACTIONS_ID + "/SEARCH/" + query)),// Table to query
						null,     			// Projection to return
						null,            	// No selection clause
						null,            	// No selection arguments
						null             	// Default sort order
						);
			}
			else{
				return new CursorLoader(
						this,   	// Parent activity context
						MyContentProvider.PLANNED_TRANSACTIONS_URI,// Table to query
						null,     			// Projection to return
						null,            	// No selection clause
						null,            	// No selection arguments
						null             	// Default sort order
						);				
			}
		default:
			Log.e("Plans-onCreateLoader", "Not a valid CursorLoader ID");
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.e("Plans-onLoadFinished", "load done. loader="+loader + " data="+data + " data size="+data.getCount());
		adapterPlans.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		loader = null; //Possible Solution????	
	}

}//end of Plans