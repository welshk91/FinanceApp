/* Class that handles the Scheduled Transactions seen in the Plans Screen
 * Does everything from setting up the view to Add/Delete/Edit
 * Hands over the actual scheduling to PlanReceiver Class
 */

package com.databases.example;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
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

public class Plans extends SherlockFragmentActivity implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor>{
	private final int ACTIONBAR_MENU_ADD_PLAN_ID = 5882300;

	private static final int PLAN_LOADER = 5882300;
	private static final int PLAN_SUBCATEGORY_LOADER = 588;
	private static final int PLAN_ACCOUNT_LOADER = 2300;	
	
	//NavigationDrawer
	private Drawer drawer;

	//Adapter for category spinner
	private static SimpleCursorAdapter categorySpinnerAdapter = null;
	private static Spinner categorySpinner;

	//Adapter for category spinner
	private static SimpleCursorAdapter accountSpinnerAdapter = null;
	private static Spinner accountSpinner;

	//Constants for ContextMenu
	private final int CONTEXT_MENU_OPEN=1;
	private final int CONTEXT_MENU_EDIT=2;
	private final int CONTEXT_MENU_DELETE=3;
	private final int CONTEXT_MENU_CANCEL=4;

	//Dialog for Adding Transaction
	private static View promptsView;

	private static Button pDate;
	private UserItemAdapter adapterPlans;
	private ListView lvPlans;

	//For Memo autocomplete
	private static ArrayList<String> dropdownResults = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.plans);
		setTitle("Plans");

		//NavigationDrawer
		drawer = new Drawer(this);

		lvPlans = (ListView)this.findViewById(R.id.plans_list);

		//Turn clicks on
		lvPlans.setClickable(true);
		lvPlans.setLongClickable(true);

		//Allows Context Menus for each item of the list view
		registerForContextMenu(lvPlans);

		//Set up a listener for changes in settings menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		TextView noResult = (TextView)findViewById(R.id.plans_noPlans);
		lvPlans.setEmptyView(noResult);

		adapterPlans = new UserItemAdapter(this, null);		
		lvPlans.setAdapter(adapterPlans);

		plansPopulate();

	}//end onCreate

	//Method to list all plans
	public void plansPopulate(){
		getSupportLoaderManager().initLoader(PLAN_LOADER, null, this);
	}

	//For Scheduling a Transaction
	public void planAdd(){
		DialogFragment newFragment = AddDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialogAdd");		
	}//end of planAdd

	//Delete Plan
	public void planDelete(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		PlanRecord record = adapterPlans.getPlan(itemInfo.position);

		Uri uri = Uri.parse(MyContentProvider.PLANS_URI + "/" + record.id);
		this.getContentResolver().delete(uri, "PlanID="+record.id, null);

		Log.d("Plans", "Deleting " + record.name + " id:" + record.id);

		//Cancel all upcoming notifications
		cancelPlan(record);

		//Refresh the plans list
		plansPopulate();

	}//end planDelete

	//For Editing a Scheduled Transaction
	public void planEdit(final android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		PlanRecord record = adapterPlans.getPlan(itemInfo.position);

		DialogFragment newFragment = EditDialogFragment.newInstance(record);
		newFragment.show(getSupportFragmentManager(), "dialogEdit");
	}//end of planEdit

	//View Plan
	public void planView(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		String id = adapterPlans.getPlan(itemInfo.position).id;

		DialogFragment newFragment = ViewDialogFragment.newInstance(id);
		newFragment.show(getSupportFragmentManager(), "dialogView");
	}

	private void schedule(PlanRecord plan) {
		PlanRecord record = plan;
		Date d = null;

		try {
			DateTime test = new DateTime();
			test.setStringSQL(record.offset);
			d = test.getYearMonthDay();
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

			//If Starting Time is in the past, fire off next day(s)
			while (firstRun.before(Calendar.getInstance())) {
				firstRun.add(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[0]));
			}

			Log.d("Plans-schedule", "firstRun is " + firstRun);
			DateTime fRun = new DateTime(); 
			fRun.setCalendar(firstRun);

			Toast.makeText(this, "Next Transaction scheduled for " + fRun.getReadableDate(), Toast.LENGTH_SHORT).show();

			am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_DAY), sender);
		}
		else if(tokens[1].contains("Weeks")){
			Log.d("Plans-schedule", "Weeks");

			//If Starting Time is in the past, fire off next week(s)
			while (firstRun.before(Calendar.getInstance())) {
				firstRun.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(tokens[0]));
			}

			Log.d("Plans-schedule", "firstRun is " + firstRun);
			DateTime fRun = new DateTime(); 
			fRun.setCalendar(firstRun);

			Toast.makeText(this, "Next Transaction scheduled for " + fRun.getReadableDate(), Toast.LENGTH_SHORT).show();

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
			DateTime fRun = new DateTime(); 
			fRun.setCalendar(firstRun);

			Toast.makeText(this, "Next Transaction scheduled for " + fRun.getReadableDate(), Toast.LENGTH_SHORT).show();

			am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), cal.getTimeInMillis(), sender);
		}
		else{
			Log.e("Plans-schedule", "Could not set alarm; Something wrong with the rate");
		}

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
			Toast.makeText(this, "Error canceling plan", Toast.LENGTH_SHORT).show();
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
		menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menuSearch.setActionView(new SearchView(getSupportActionBar().getThemedContext()));
		
		SearchWidget searchWidget = new SearchWidget(this,menuSearch.getActionView());
		
		//Add
		MenuItem subMenu1Item = menu.add(com.actionbarsherlock.view.Menu.NONE, ACTIONBAR_MENU_ADD_PLAN_ID, com.actionbarsherlock.view.Menu.NONE, "Add");
		subMenu1Item.setIcon(android.R.drawable.ic_menu_add);
		subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	//For ActionBar Menu Items (and home icon)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			drawer.toggle();
			break;

		case ACTIONBAR_MENU_ADD_PLAN_ID:
			planAdd();
			break;
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
			planEdit(item);
			return true;

		case CONTEXT_MENU_DELETE:
			planDelete(item);
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

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d("Plans-onSharedPreferenceChanged", "Options changed. Requery");
		//getContentResolver().notifyChange(MyContentProvider.PLANNED_TRANSACTIONS_URI, null);
		//getLoaderManager().restartLoader(PLAN_LOADER, null, this);
	}


	//Method for selecting a Date when adding a transaction
	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
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
			DateTime date = new DateTime();
			date.setStringSQL(year + "-" + (month+1) + "-" + day);
			pDate.setText(date.getReadableDate());

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
			boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_plan", true);

			if (user != null) {
				TextView tvName = (TextView) v.findViewById(R.id.plan_name);
				TextView tvAccount = (TextView) v.findViewById(R.id.plan_account);
				TextView tvValue = (TextView) v.findViewById(R.id.plan_value);
				TextView tvType = (TextView) v.findViewById(R.id.plan_type);
				TextView tvCategory = (TextView) v.findViewById(R.id.plan_category);
				TextView tvMemo = (TextView) v.findViewById(R.id.plan_memo);
				TextView tvOffset = (TextView) v.findViewById(R.id.plan_offset);
				TextView tvRate = (TextView) v.findViewById(R.id.plan_rate);
				TextView tvCleared = (TextView) v.findViewById(R.id.plan_cleared);

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
				Money value = new Money(user.getString(ValueColumn));
				String type = user.getString(TypeColumn);
				String category = user.getString(CategoryColumn);
				String memo = user.getString(MemoColumn);
				String offset = user.getString(OffsetColumn);
				String rate = user.getString(RateColumn);
				String cleared = user.getString(ClearedColumn);

				Locale locale=getResources().getConfiguration().locale;


				//Change gradient
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.plan_gradient);
					GradientDrawable defaultGradientPos = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFF4ac925,0xFF4ac925});

					GradientDrawable defaultGradientNeg = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFFe00707,0xFFe00707});

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
					tvName.setText(name);
				}
				if (to_id != null) {
					tvAccount.setText("Account ID: " + to_id);
				}
				if (value != null) {
					tvValue.setText("Value: " + value.getNumberFormat(locale));
				}
				if (type != null) {
					tvType.setText("Type: " + type);
				}
				if (category != null) {
					tvCategory.setText("Category: " + category);
				}
				if (memo != null) {
					tvMemo.setText("Memo: " + memo);
				}
				if (offset != null) {
					DateTime o = new DateTime();
					o.setStringSQL(offset);
					tvOffset.setText("Offset: " + o.getReadableDate());
				}
				if (rate != null) {
					tvRate.setText("Rate: " + rate);
				}
				if (cleared != null) {
					tvCleared.setText("Cleared: " + cleared);
				}

			}

		}

		@Override
		public View newView(Context context, Cursor plans, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.plan_item, parent, false);

			TextView tvName=(TextView)v.findViewById(R.id.plan_name);
			TextView tvAccount=(TextView)v.findViewById(R.id.plan_account);
			TextView tvValue=(TextView)v.findViewById(R.id.plan_value);
			TextView tvType=(TextView)v.findViewById(R.id.plan_type);
			TextView tvCategory=(TextView)v.findViewById(R.id.plan_category);
			TextView tvMemo=(TextView)v.findViewById(R.id.plan_memo);
			TextView tvOffset=(TextView)v.findViewById(R.id.plan_offset);
			TextView tvRate=(TextView)v.findViewById(R.id.plan_rate);
			TextView tvCleared=(TextView)v.findViewById(R.id.plan_cleared);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Plans.this);
			boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_plan", true);

			//Change Background Colors
			try{
				if(!useDefaults){
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.plan_layout);
					int startColor = prefs.getInt("key_plan_startBackgroundColor", Color.parseColor("#FFFFFF"));
					int endColor = prefs.getInt("key_plan_endBackgroundColor", Color.parseColor("#FFFFFF"));
					GradientDrawable customGradient = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {startColor,endColor});
					l.setBackgroundDrawable(customGradient);
				}
			}
			catch(Exception e){
				Toast.makeText(Plans.this, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
			}

			//Change Size of main field
			try{
				String customSize = prefs.getString(Plans.this.getString(R.string.pref_key_plan_nameSize), "24");

				if(useDefaults){
					tvName.setTextSize(24);
				}
				else{
					tvName.setTextSize(Integer.parseInt(customSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Plans.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int customColor = prefs.getInt("key_plan_nameColor", Color.parseColor("#222222"));

				if(useDefaults){
					tvName.setTextColor(Color.parseColor("#222222"));
				}
				else{
					tvName.setTextColor(customColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Plans.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				String defaultSize = prefs.getString(Plans.this.getString(R.string.pref_key_plan_fieldSize), "14");
				int customSize = Integer.parseInt(defaultSize);

				if(useDefaults){
					tvAccount.setTextSize(14);
					tvValue.setTextSize(14);
					tvType.setTextSize(14);
					tvCategory.setTextSize(14);
					tvMemo.setTextSize(14);
					tvOffset.setTextSize(14);
					tvRate.setTextSize(14);
					tvCleared.setTextSize(14);
				}
				else{
					tvAccount.setTextSize(customSize);
					tvValue.setTextSize(customSize);
					tvType.setTextSize(customSize);
					tvCategory.setTextSize(customSize);
					tvMemo.setTextSize(customSize);
					tvOffset.setTextSize(customSize);
					tvRate.setTextSize(customSize);
					tvCleared.setTextSize(customSize);
				}

			}
			catch(Exception e){
				Toast.makeText(Plans.this, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_plan_fieldColor", Color.parseColor("#000000"));

				if(useDefaults){
					tvAccount.setTextColor(Color.parseColor("#000000"));
					tvValue.setTextColor(Color.parseColor("#000000"));
					tvType.setTextColor(Color.parseColor("#000000"));
					tvCategory.setTextColor(Color.parseColor("#000000"));
					tvMemo.setTextColor(Color.parseColor("#000000"));
					tvOffset.setTextColor(Color.parseColor("#000000"));
					tvRate.setTextColor(Color.parseColor("#000000"));
					tvCleared.setTextColor(Color.parseColor("#000000"));
				}
				else{
					tvAccount.setTextColor(DefaultColor);
					tvValue.setTextColor(DefaultColor);
					tvType.setTextColor(DefaultColor);
					tvCategory.setTextColor(DefaultColor);
					tvMemo.setTextColor(DefaultColor);
					tvOffset.setTextColor(DefaultColor);
					tvRate.setTextColor(DefaultColor);
					tvCleared.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Plans.this, "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
			}

			//For User-Defined Field Visibility
			if(useDefaults||prefs.getBoolean("checkbox_plan_nameField", true)){
				tvName.setVisibility(View.VISIBLE);
			}
			else{
				tvName.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_plan_accountField", true)){
				tvAccount.setVisibility(View.VISIBLE);
			}
			else{
				tvAccount.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_plan_valueField", true)){
				tvValue.setVisibility(View.VISIBLE);
			}
			else{
				tvValue.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_plan_typeField", false) && !useDefaults){
				tvType.setVisibility(View.VISIBLE);
			}
			else{
				tvType.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_plan_categoryField", true)){
				tvCategory.setVisibility(View.VISIBLE);
			}
			else{
				tvCategory.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_plan_memoField", false) && !useDefaults){
				tvMemo.setVisibility(View.VISIBLE);
			}
			else{
				tvMemo.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_plan_offsetField", true)){
				tvOffset.setVisibility(View.VISIBLE);
			}
			else{
				tvOffset.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_plan_rateField", true)){
				tvRate.setVisibility(View.VISIBLE);
			}
			else{
				tvRate.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_plan_clearedField", false) && !useDefaults){
				tvCleared.setVisibility(View.VISIBLE);
			}
			else{
				tvCleared.setVisibility(View.GONE);
			}

			return v;
		}
	}//end UserItem

	//An Object Class used to hold the data of each transaction record
	public static class PlanRecord {
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

	//Class that handles view fragment
	public static class ViewDialogFragment extends SherlockDialogFragment {

		public static ViewDialogFragment newInstance(String id) {
			ViewDialogFragment frag = new ViewDialogFragment();
			Bundle args = new Bundle();
			args.putString("id", id);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final String ID = getArguments().getString("id");
			Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.PLANS_URI+"/"+(ID)), null, null, null, null);

			int entry_id = 0;
			String entry_acctId = null;
			String entry_name = null;
			String entry_value = null;
			String entry_type = null;
			String entry_category = null;
			String entry_memo = null;
			String entry_offset = null;
			String entry_rate = null;
			String entry_cleared = null;

			c.moveToFirst();
			do{
				entry_id = c.getInt(c.getColumnIndex("PlanID"));
				entry_acctId = c.getString(c.getColumnIndex("ToAcctID"));
				entry_name = c.getString(c.getColumnIndex("PlanName"));
				entry_value = c.getString(c.getColumnIndex("PlanValue"));
				entry_type = c.getString(c.getColumnIndex("PlanType"));
				entry_category = c.getString(c.getColumnIndex("PlanCategory"));
				entry_memo = c.getString(c.getColumnIndex("PlanMemo"));
				entry_offset = c.getString(c.getColumnIndex("PlanOffset"));
				entry_rate = c.getString(c.getColumnIndex("PlanRate"));
				entry_cleared = c.getString(c.getColumnIndex("PlanCleared"));
			}while(c.moveToNext());

			LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
			final View planStatsView = li.inflate(R.layout.plan_stats, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());
			alertDialogBuilder.setView(planStatsView);
			alertDialogBuilder.setTitle("View Plan");
			alertDialogBuilder.setCancelable(true);

			//Set Statistics
			TextView statsName = (TextView)planStatsView.findViewById(R.id.TextTransactionName);
			statsName.setText(entry_name);
			TextView statsAccount = (TextView)planStatsView.findViewById(R.id.TextTransactionAccount);
			statsAccount.setText(entry_acctId);
			TextView statsValue = (TextView)planStatsView.findViewById(R.id.TextTransactionValue);
			statsValue.setText(entry_value);
			TextView statsType = (TextView)planStatsView.findViewById(R.id.TextTransactionType);
			statsType.setText(entry_type);
			TextView statsCategory = (TextView)planStatsView.findViewById(R.id.TextTransactionCategory);
			statsCategory.setText(entry_category);
			TextView statsMemo = (TextView)planStatsView.findViewById(R.id.TextTransactionMemo);
			statsMemo.setText(entry_memo);
			TextView statsOffset = (TextView)planStatsView.findViewById(R.id.TextTransactionOffset);
			DateTime o = new DateTime();
			o.setStringSQL(entry_offset);
			statsOffset.setText(o.getReadableDate());
			TextView statsRate = (TextView)planStatsView.findViewById(R.id.TextTransactionRate);
			statsRate.setText(entry_rate);
			TextView statsCleared = (TextView)planStatsView.findViewById(R.id.TextTransactionCleared);
			statsCleared.setText(entry_cleared);

			return alertDialogBuilder.create();
		}
	}

	public static class AddDialogFragment extends SherlockDialogFragment {

		public static AddDialogFragment newInstance() {
			AddDialogFragment frag = new AddDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
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
			//pDate.setText(dateFormat.format(c.getTime()));
			DateTime d = new DateTime();
			d.setCalendar(c);
			pDate.setText(d.getReadableDate());

			//Adapter for memo's autocomplete
			ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this.getSherlockActivity(), android.R.layout.simple_dropdown_item_1line, dropdownResults);
			tMemo.setAdapter(dropdownAdapter);

			//Add dictionary back to autocomplete
			TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
			tMemo.setKeyListener(input);

			//Populate Category Drop-down List
			getLoaderManager().initLoader(PLAN_SUBCATEGORY_LOADER, null, ((Plans) getSherlockActivity()));

			//Populate Account Drop-down List
			getLoaderManager().initLoader(PLAN_ACCOUNT_LOADER, null, ((Plans) getSherlockActivity()));

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

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
					Money transactionValue = null;
					String transactionType = null;
					String transactionCategory = null;
					String transactionMemo = null;
					DateTime transactionOffset = new DateTime();
					String transactionRate = null;
					String transactionCleared = null;
					Locale locale=getResources().getConfiguration().locale;

					//Needed to get category's name from DB-populated spinner
					int categoryPosition = categorySpinner.getSelectedItemPosition();
					Cursor cursorCategory = (Cursor) categorySpinnerAdapter.getItem(categoryPosition);	

					//Needed to get account's name from DB-populated spinner
					int accountPosition = accountSpinner.getSelectedItemPosition();
					Cursor cursorAccount = (Cursor) accountSpinnerAdapter.getItem(accountPosition);				

					transactionName = tName.getText().toString().trim();
					try{
						transactionValue = new Money(tValue.getText().toString().trim());	
					}
					catch(Exception e){
						Log.e("Plans-Add","Invalid Value? Exception e:" + e);
						dialog.cancel();
						Toast.makeText(getSherlockActivity(), "Invalid Value", Toast.LENGTH_LONG).show();
						return;
					}

					transactionType = tType.getSelectedItem().toString().trim();

					try{
						transactionAccount = cursorAccount.getString(cursorAccount.getColumnIndex("AcctName"));
						transactionAccountID = cursorAccount.getString(cursorAccount.getColumnIndex("_id"));
					}
					catch(Exception e){
						//Usually caused if no account exists
						Log.e("Plans-Add","No Account? Exception e:" + e);
						dialog.cancel();
						Toast.makeText(getSherlockActivity(), "Needs An Account \n\nUse The Side Menu->Checkbook To Create Accounts", Toast.LENGTH_LONG).show();
						return;
					}

					try{
						//	transactionCategoryID = cursorCategory.getString(cursorCategory.getColumnIndex("ToCatId"));
						transactionCategory = cursorCategory.getString(cursorCategory.getColumnIndex("SubCatName"));
					}
					catch(Exception e){
						//Usually caused if no category exists
						Log.e("Plans-Add","No Category? Exception e:" + e);
						dialog.cancel();
						Toast.makeText(getSherlockActivity(), "Needs A Category \n\nUse The Side Menu->Categories To Create Categories", Toast.LENGTH_LONG).show();
						return;
					}

					transactionMemo = tMemo.getText().toString().trim();

					//Set Time
					transactionOffset.setStringReadable(pDate.getText().toString().trim());

					transactionRate = tRate.getText().toString().trim() + " " + rateSpinner.getSelectedItem().toString().trim();
					transactionCleared = tCleared.isChecked()+"";

					//Check to see if value is a number
					boolean validRate=false;
					try{
						Integer.parseInt(tRate.getText().toString().trim());
						validRate=true;
					}
					catch(Exception e){
						Log.e("Plans-Add","Rate not valid; Edit Text rate=" + tRate.getText().toString().trim());
						validRate=false;
					}

					try{
						if (transactionName.length()>0 && validRate) {
							Log.d("Plans-Add", transactionAccountID + transactionAccount + transactionName + transactionValue + transactionType + transactionCategory + transactionMemo + transactionOffset + transactionRate + transactionCleared);

							ContentValues transactionValues = new ContentValues();
							transactionValues.put("ToAcctID", transactionAccountID);
							transactionValues.put("PlanName", transactionName);
							transactionValues.put("PlanValue", transactionValue.getBigDecimal(locale)+"");
							transactionValues.put("PlanType", transactionType);
							transactionValues.put("PlanCategory", transactionCategory);
							transactionValues.put("PlanMemo", transactionMemo);
							transactionValues.put("PlanOffset", transactionOffset.getSQLDate(locale));
							transactionValues.put("PlanRate", transactionRate);
							transactionValues.put("PlanCleared", transactionCleared);

							Uri u = getSherlockActivity().getContentResolver().insert(MyContentProvider.PLANS_URI, transactionValues);

							PlanRecord record = new PlanRecord(u.getLastPathSegment(), transactionAccountID, transactionName, transactionValue.getBigDecimal(locale)+"", transactionType, transactionCategory, transactionMemo, transactionOffset.getSQLDate(locale), transactionRate, transactionCleared);
							((Plans) getSherlockActivity()).schedule(record);

							//Refresh the plans list
							((Plans) getSherlockActivity()).plansPopulate();
							//plansPopulate();
						} 

						else {
							Toast.makeText(getSherlockActivity(), "Transactions need a Name, Value, and Rate", Toast.LENGTH_LONG).show();
						}
					}
					catch(Exception e){
						Log.e("Plans-Add", "e = " + e);
						Toast.makeText(getSherlockActivity(), "Error Adding Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
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

			return alertDialogBuilder.create();			
		}
	}

	public static class EditDialogFragment extends SherlockDialogFragment {

		public static EditDialogFragment newInstance(PlanRecord record) {
			EditDialogFragment frag = new EditDialogFragment();
			Bundle args = new Bundle();
			args.putString("id", record.id);
			args.putString("acct_id", record.acctId);
			args.putString("name", record.name);
			args.putString("value", record.value);
			args.putString("type", record.type);
			args.putString("category", record.category);
			args.putString("memo", record.memo);
			args.putString("rate", record.rate);
			args.putString("offset", record.offset);
			args.putString("cleared", record.cleared);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final String ID = getArguments().getString("id");
			final String aID = getArguments().getString("acct_id");
			final String name = getArguments().getString("name");
			final String value = getArguments().getString("value");
			final String type = getArguments().getString("type");
			final String category = getArguments().getString("category");
			final String memo = getArguments().getString("memo");
			final String offset = getArguments().getString("offset");
			final String rate = getArguments().getString("rate");
			final String cleared = getArguments().getString("cleared");
			final PlanRecord oldRecord = new PlanRecord(ID, aID, name, value, type, category, memo, offset, rate, cleared);

			LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
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
			ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this.getSherlockActivity(), android.R.layout.simple_dropdown_item_1line, dropdownResults);
			tMemo.setAdapter(dropdownAdapter);

			//Add dictionary back to autocomplete
			TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
			tMemo.setKeyListener(input);

			//Populate Category Drop-down List
			getLoaderManager().initLoader(PLAN_SUBCATEGORY_LOADER, null, ((Plans) getSherlockActivity()));

			//Populate Account Drop-down List
			getLoaderManager().initLoader(PLAN_ACCOUNT_LOADER, null, ((Plans) getSherlockActivity()));

			tName.setText(name);
			tValue.setText(value);
			ArrayAdapter<String> typeAdap = (ArrayAdapter<String>) tType.getAdapter();
			int spinnerPosition = typeAdap.getPosition(type);
			tType.setSelection(spinnerPosition);

			//Used to find correct category to select
			for (int i = 0; i < categorySpinner.getCount(); i++) {
				Cursor cursorValue = (Cursor) categorySpinner.getItemAtPosition(i);
				String cursorName = cursorValue.getString(cursorValue.getColumnIndex("SubCatName"));
				if (cursorName.contentEquals(category)) {
					categorySpinner.setSelection(i);
					break;
				}
			}

			//Used to find correct account to select
			for (int i = 0; i < accountSpinner.getCount(); i++) {
				Cursor cursorValue = (Cursor) accountSpinner.getItemAtPosition(i);
				String cursorID = cursorValue.getString(cursorValue.getColumnIndex("_id"));
				if (cursorID.contentEquals(aID)) {
					accountSpinner.setSelection(i);
					break;
				}
			}

			tMemo.setText(memo);
			DateTime d = new DateTime();
			d.setStringSQL(offset);
			pDate.setText(d.getReadableDate());

			//Parse Rate (token 0 is amount, token 1 is type)
			String phrase = rate;
			String delims = "[ ]+";
			String[] tokens = phrase.split(delims);

			tRate.setText(tokens[0]);

			ArrayAdapter<String> rateAdap = (ArrayAdapter<String>) rateSpinner.getAdapter();
			int spinnerPosition4 = rateAdap.getPosition(tokens[1]);
			rateSpinner.setSelection(spinnerPosition4);

			tCleared.setChecked(Boolean.parseBoolean(cleared));

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

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
					Money transactionValue = null;
					String transactionType = null;
					String transactionCategory = null;
					String transactionMemo = null;
					DateTime transactionOffset = new DateTime();
					String transactionRate = null;
					String transactionCleared = null;
					Locale locale=getResources().getConfiguration().locale;

					//Needed to get category's name from DB-populated spinner
					int categoryPosition = categorySpinner.getSelectedItemPosition();
					Cursor cursorCategory = (Cursor) categorySpinnerAdapter.getItem(categoryPosition);	

					//Needed to get account's name from DB-populated spinner
					int accountPosition = accountSpinner.getSelectedItemPosition();
					Cursor cursorAccount = (Cursor) accountSpinnerAdapter.getItem(accountPosition);				

					transactionName = tName.getText().toString().trim();

					try{
						transactionValue = new Money(tValue.getText().toString().trim());	
					}
					catch(Exception e){
						Log.e("Plans-schedulingEdit","Invalid Value? Exception e:" + e);
						dialog.cancel();
						Toast.makeText(getSherlockActivity(), "Invalid Value", Toast.LENGTH_LONG).show();
						return;
					}


					transactionType = tType.getSelectedItem().toString().trim();

					try{
						transactionAccount = cursorAccount.getString(cursorAccount.getColumnIndex("AcctName"));
						transactionAccountID = cursorAccount.getString(cursorAccount.getColumnIndex("_id"));
					}
					catch(Exception e){
						//Usually caused if no account exists
						Log.e("Plans-schedulingEdit","No Account? Exception e:" + e);
						dialog.cancel();
						Toast.makeText(getSherlockActivity(), "Needs An Account \n\nUse The Side Menu->Checkbook To Create Accounts", Toast.LENGTH_LONG).show();
						return;
					}

					try{
						//	transactionCategoryID = cursorCategory.getString(cursorCategory.getColumnIndex("ToCatId"));
						transactionCategory = cursorCategory.getString(cursorCategory.getColumnIndex("SubCatName"));
					}
					catch(Exception e){
						//Usually caused if no category exists
						Log.e("Plans-schedulingEdit","No Category? Exception e:" + e);
						dialog.cancel();
						Toast.makeText(getSherlockActivity(), "Needs A Category \n\nUse The Side Menu->Categories To Create Categories", Toast.LENGTH_LONG).show();
						return;
					}

					transactionMemo = tMemo.getText().toString().trim();

					//Set Time
					transactionOffset.setStringReadable(pDate.getText().toString().trim());
					transactionRate = tRate.getText().toString().trim() + " " + rateSpinner.getSelectedItem().toString().trim();
					transactionCleared = tCleared.isChecked()+"";

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
						if (transactionName.length()>0 && validRate) {
							Log.d("Plans-Edit", transactionAccountID + transactionAccount + transactionName + transactionValue + transactionType + transactionCategory + transactionMemo + transactionOffset + transactionRate + transactionCleared);

							ContentValues transactionValues=new ContentValues();
							transactionValues.put("PlanID", ID);
							transactionValues.put("ToAcctID", transactionAccountID);
							transactionValues.put("PlanName", transactionName);
							transactionValues.put("PlanValue", transactionValue.getBigDecimal(locale)+"");
							transactionValues.put("PlanType", transactionType);
							transactionValues.put("PlanCategory", transactionCategory);
							transactionValues.put("PlanMemo", transactionMemo);
							transactionValues.put("PlanOffset", transactionOffset.getSQLDate(locale));
							transactionValues.put("PlanRate", transactionRate);
							transactionValues.put("PlanCleared", transactionCleared);

							//Cancel old plan
							((Plans) getSherlockActivity()).cancelPlan(oldRecord);

							//Update plan
							getSherlockActivity().getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI+"/"+ID), transactionValues, "PlanID ="+ID, null);

							//Reschedule plan
							PlanRecord record = new PlanRecord(ID, transactionAccountID, transactionName, transactionValue.getBigDecimal(locale)+"", transactionType, transactionCategory, transactionMemo, transactionOffset.getSQLDate(locale), transactionRate, transactionCleared);
							((Plans) getSherlockActivity()).schedule(record);;
							((Plans) getSherlockActivity()).plansPopulate();;
						} 

						else {
							Toast.makeText(getSherlockActivity(), "Transactions need a Name, Value, and Rate", Toast.LENGTH_LONG).show();
						}
					}
					catch(Exception e){
						Log.e("Plans-Edit", "e = "+e);
						Toast.makeText(getSherlockActivity(), "Error Adding Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
					}

				}//end onClick "OK"
			})
			.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			return alertDialogBuilder.create();			
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawer.getDrawerToggle().syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawer.getDrawerToggle().onConfigurationChanged(newConfig);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {		
		switch (loaderID) {
		case PLAN_LOADER:
			if(bundle!=null && bundle.getBoolean("boolSearch")){
				//Log.v("Plans-onCreateLoader","new loader (boolSearch "+ query + ") created");
				String query = this.getIntent().getStringExtra("query");
				return new CursorLoader(
						this,   	// Parent activity context
						(Uri.parse(MyContentProvider.PLANS_ID + "/SEARCH/" + query)),// Table to query
						null,     			// Projection to return
						null,            	// No selection clause
						null,            	// No selection arguments
						null             	// Default sort order
						);
			}
			else{
				Log.v("Plans-onCreateLoader","new loader created");
				return new CursorLoader(
						this,   	// Parent activity context
						MyContentProvider.PLANS_URI,// Table to query
						null,     			// Projection to return
						null,            	// No selection clause
						null,            	// No selection arguments
						null             	// Default sort order
						);				
			}
			
		case PLAN_ACCOUNT_LOADER:
			Log.v("Plans-onCreateLoader","new plan loader created");
			return new CursorLoader(
					this,   	// Parent activity context
					MyContentProvider.ACCOUNTS_URI,// Table to query
					null,     			// Projection to return
					null,            	// No selection clause
					null,            	// No selection arguments
					null           // Default sort order-> "CAST (AcctBalance AS INTEGER)" + " DESC"
					);

		case PLAN_SUBCATEGORY_LOADER:
			Log.v("Plans-onCreateLoader","new category loader created");
			return new CursorLoader(
					this,   	// Parent activity context
					MyContentProvider.SUBCATEGORIES_URI,// Table to query
					null,     			// Projection to return
					null,            	// No selection clause
					null,            	// No selection arguments
					null           // Default sort order
					);			
						
		default:
			Log.e("Plans-onCreateLoader", "Not a valid CursorLoader ID");
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch(loader.getId()){
		case PLAN_LOADER:			
			adapterPlans.swapCursor(data);
			Log.v("Plans-onLoadFinished", "load done. loader="+loader + " data="+data + " data size="+data.getCount());
			break;
			
		case PLAN_ACCOUNT_LOADER:
			String[] from = new String[] {"AcctName", "_id"}; 
			int[] to = new int[] { android.R.id.text1};

			accountSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, data, from, to);
			accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			accountSpinner.setAdapter(accountSpinnerAdapter);
			Log.v("Plans-onLoadFinished", "load done. loader="+loader + " data="+data + " data size="+data.getCount());
			break;
			
		case PLAN_SUBCATEGORY_LOADER:
			from = new String[] {"SubCatName"}; 
			to = new int[] { android.R.id.text1 };

			categorySpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, data, from, to);
			categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			categorySpinner.setAdapter(categorySpinnerAdapter);
			Log.v("Plans-onLoadFinished", "load done. loader="+loader + " data="+data + " data size="+data.getCount());
			break;
			
		default:
			Log.v("Plans-onLoadFinished", "Error. Unknown loader ("+loader.getId());			
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch(loader.getId()){
		case PLAN_LOADER:
			adapterPlans.swapCursor(null);
			Log.v("Plans-onLoaderReset", "loader reset. loader="+loader.getId());
			break;

		case PLAN_ACCOUNT_LOADER:
			Log.v("Plans-onLoaderReset", "loader reset. loader="+loader.getId());
			break;
			
		case PLAN_SUBCATEGORY_LOADER:
			Log.v("Plans-onLoaderReset", "loader reset. loader="+loader.getId());
			break;

		default:
			Log.e("Plans-onLoadFinished", "Error. Unknown loader ("+loader.getId());
			break;
		}		
	}
	
}//end of Plans