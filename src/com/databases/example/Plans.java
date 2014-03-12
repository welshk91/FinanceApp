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
import com.actionbarsherlock.view.ActionMode;
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
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

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
	private final int CONTEXT_MENU_VIEW=1;
	private final int CONTEXT_MENU_EDIT=2;
	private final int CONTEXT_MENU_DELETE=3;
	private final int CONTEXT_MENU_TOGGLE=4;

	//Dialog for Adding Transaction
	private static View promptsView;

	private static Button pDate;
	private UserItemAdapter adapterPlans;
	private ListView lvPlans;

	//ActionMode
	protected Object mActionMode = null;
	private SparseBooleanArray mSelectedItemsIds;

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

		//Set Listener for regular mouse click
		lvPlans.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				if (mActionMode != null) {
					listItemChecked(position);
				}
				else{
					//Stuff for clicking...
				}
			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener

		lvPlans.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mActionMode != null) {
					return false;
				}

				listItemChecked(position);
				return true;
			}
		});

		//Set up a listener for changes in settings menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		TextView noResult = (TextView)findViewById(R.id.plans_noPlans);
		lvPlans.setEmptyView(noResult);

		adapterPlans = new UserItemAdapter(this, null);		
		lvPlans.setAdapter(adapterPlans);

		getSupportLoaderManager().initLoader(PLAN_LOADER, null, this);

	}//end onCreate

	//Used for ActionMode
	public void listItemChecked(int position){
		adapterPlans.toggleSelection(position);
		boolean hasCheckedItems = adapterPlans.getSelectedCount() > 0;

		if (hasCheckedItems && mActionMode == null){
			mActionMode = this.startActionMode(new MyActionMode());
		}
		else if (!hasCheckedItems && mActionMode != null){
			((ActionMode) mActionMode).finish();
		}

		if(mActionMode != null){
			((ActionMode) mActionMode).invalidate();
			((ActionMode)mActionMode).setTitle(String.valueOf(adapterPlans.getSelectedCount()));
		}
	}

	//For Scheduling a Transaction
	public void planAdd(){
		DialogFragment newFragment = AddDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialogAdd");		
	}//end of planAdd

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
			final Calendar cal = Calendar.getInstance();

			SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
			SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
			SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");

			int year = Integer.parseInt(dateFormatYear.format(cal.getTime()));
			int month = Integer.parseInt(dateFormatMonth.format(cal.getTime()))-1;
			int day = Integer.parseInt(dateFormatDay.format(cal.getTime()));

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
			mSelectedItemsIds = new SparseBooleanArray();
		}

		public PlanRecord getPlan(long position){
			final Cursor group = getCursor();

			group.moveToPosition((int) position);
			final int IDColumn = group.getColumnIndex(DatabaseHelper.PLAN_ID);
			final int ToIDColumn = group.getColumnIndex(DatabaseHelper.PLAN_ACCT_ID);
			final int NameColumn = group.getColumnIndex(DatabaseHelper.PLAN_NAME);
			final int ValueColumn = group.getColumnIndex(DatabaseHelper.PLAN_VALUE);
			final int TypeColumn = group.getColumnIndex(DatabaseHelper.PLAN_TYPE);
			final int CategoryColumn = group.getColumnIndex(DatabaseHelper.PLAN_CATEGORY);
			final int MemoColumn = group.getColumnIndex(DatabaseHelper.PLAN_MEMO);
			final int OffsetColumn = group.getColumnIndex(DatabaseHelper.PLAN_OFFSET);
			final int RateColumn = group.getColumnIndex(DatabaseHelper.PLAN_RATE);
			final int ScheduledColumn = group.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED);
			final int ClearedColumn = group.getColumnIndex(DatabaseHelper.PLAN_CLEARED);

			final String id = group.getString(0);
			final String to_id = group.getString(ToIDColumn);
			final String name = group.getString(NameColumn);
			final String value = group.getString(ValueColumn);
			final String type = group.getString(TypeColumn);
			final String category = group.getString(CategoryColumn);
			final String memo = group.getString(MemoColumn);
			final String offset = group.getString(OffsetColumn);
			final String rate = group.getString(RateColumn);
			final String scheduled = group.getString(ScheduledColumn);
			final String cleared = group.getString(ClearedColumn);

			final PlanRecord record = new PlanRecord(id, to_id, name, value, type, category, memo, offset, rate, scheduled, cleared);
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

				final int IDColumn = user.getColumnIndex(DatabaseHelper.PLAN_ID);
				final int ToIDColumn = user.getColumnIndex(DatabaseHelper.PLAN_ACCT_ID);
				final int NameColumn = user.getColumnIndex(DatabaseHelper.PLAN_NAME);
				final int ValueColumn = user.getColumnIndex(DatabaseHelper.PLAN_VALUE);
				final int TypeColumn = user.getColumnIndex(DatabaseHelper.PLAN_TYPE);
				final int CategoryColumn = user.getColumnIndex(DatabaseHelper.PLAN_CATEGORY);
				final int MemoColumn = user.getColumnIndex(DatabaseHelper.PLAN_MEMO);
				final int OffsetColumn = user.getColumnIndex(DatabaseHelper.PLAN_OFFSET);
				final int RateColumn = user.getColumnIndex(DatabaseHelper.PLAN_RATE);
				final int ScheduledColumn = user.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED);
				final int ClearedColumn = user.getColumnIndex(DatabaseHelper.PLAN_CLEARED);

				String id = user.getString(0);
				String to_id = user.getString(ToIDColumn);
				String name = user.getString(NameColumn);
				Money value = new Money(user.getString(ValueColumn));
				String type = user.getString(TypeColumn);
				String category = user.getString(CategoryColumn);
				String memo = user.getString(MemoColumn);
				String offset = user.getString(OffsetColumn);
				String rate = user.getString(RateColumn);
				String scheduled = user.getString(ScheduledColumn);
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

				if(scheduled.equals("false")){
					v.setAlpha(.5f);
				}
				else{
					v.setAlpha(1.0f);
				}

				v.setBackgroundColor(mSelectedItemsIds.get(user.getPosition())? 0x9934B5E4: Color.TRANSPARENT);
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

		public void toggleSelection(int position)
		{
			selectView(position, !mSelectedItemsIds.get(position));
		}

		public void removeSelection() {
			mSelectedItemsIds = new SparseBooleanArray();
			notifyDataSetChanged();
		}

		public void selectView(int position, boolean value)
		{
			if(value)
				mSelectedItemsIds.put(position, value);
			else
				mSelectedItemsIds.delete(position);

			notifyDataSetChanged();
		}

		public int getSelectedCount() {
			return mSelectedItemsIds.size();// mSelectedCount;
		}

		public SparseBooleanArray getSelectedIds() {
			return mSelectedItemsIds;
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
		protected String scheduled;
		protected String cleared;

		public PlanRecord(String id, String acctId, String name, String value, String type, String category, String memo, String offset, String rate, String scheduled, String cleared) {
			this.id = id;
			this.acctId = acctId;
			this.name = name;
			this.value = value;
			this.type = type;
			this.category = category;
			this.memo = memo;
			this.offset = offset;
			this.rate = rate;
			this.scheduled = scheduled;
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
			Cursor cursor = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.PLANS_URI+"/"+(ID)), null, null, null, null);

			int entry_id = 0;
			String entry_acctId = null;
			String entry_name = null;
			String entry_value = null;
			String entry_type = null;
			String entry_category = null;
			String entry_memo = null;
			String entry_offset = null;
			String entry_rate = null;
			String entry_scheduled = null;
			String entry_cleared = null;

			cursor.moveToFirst();
			do{
				entry_id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.PLAN_ID));
				entry_acctId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_ACCT_ID));
				entry_name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_NAME));
				entry_value = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_VALUE));
				entry_type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_TYPE));
				entry_category = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_CATEGORY));
				entry_memo = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_MEMO));
				entry_offset = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_OFFSET));
				entry_rate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_RATE));
				entry_scheduled = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED));
				entry_cleared = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_CLEARED));
			}while(cursor.moveToNext());

			final LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
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
			TextView statsScheduled = (TextView)planStatsView.findViewById(R.id.TextTransactionScheduled);
			statsScheduled.setText(entry_scheduled);
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
						transactionAccount = cursorAccount.getString(cursorAccount.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));
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
						transactionCategory = cursorCategory.getString(cursorCategory.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME));
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
							transactionValues.put(DatabaseHelper.PLAN_ACCT_ID, transactionAccountID);
							transactionValues.put(DatabaseHelper.PLAN_NAME, transactionName);
							transactionValues.put(DatabaseHelper.PLAN_VALUE, transactionValue.getBigDecimal(locale)+"");
							transactionValues.put(DatabaseHelper.PLAN_TYPE, transactionType);
							transactionValues.put(DatabaseHelper.PLAN_CATEGORY, transactionCategory);
							transactionValues.put(DatabaseHelper.PLAN_MEMO, transactionMemo);
							transactionValues.put(DatabaseHelper.PLAN_OFFSET, transactionOffset.getSQLDate(locale));
							transactionValues.put(DatabaseHelper.PLAN_RATE, transactionRate);
							transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "true");
							transactionValues.put(DatabaseHelper.PLAN_CLEARED, transactionCleared);

							Uri u = getSherlockActivity().getContentResolver().insert(MyContentProvider.PLANS_URI, transactionValues);

							PlanRecord record = new PlanRecord(u.getLastPathSegment(), transactionAccountID, transactionName, transactionValue.getBigDecimal(locale)+"", transactionType, transactionCategory, transactionMemo, transactionOffset.getSQLDate(locale), transactionRate, "true", transactionCleared);
							((Plans) getSherlockActivity()).schedule(record);
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
			args.putString("scheduled", record.scheduled);
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
			final String scheduled = getArguments().getString("scheduled");
			final String cleared = getArguments().getString("cleared");

			final PlanRecord oldRecord = new PlanRecord(ID, aID, name, value, type, category, memo, offset, rate, scheduled, cleared);

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
				String cursorName = cursorValue.getString(cursorValue.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME));
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
						transactionAccount = cursorAccount.getString(cursorAccount.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));
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
						transactionCategory = cursorCategory.getString(cursorCategory.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME));
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
							transactionValues.put(DatabaseHelper.PLAN_ID, ID);
							transactionValues.put(DatabaseHelper.PLAN_ACCT_ID, transactionAccountID);
							transactionValues.put(DatabaseHelper.PLAN_NAME, transactionName);
							transactionValues.put(DatabaseHelper.PLAN_VALUE, transactionValue.getBigDecimal(locale)+"");
							transactionValues.put(DatabaseHelper.PLAN_TYPE, transactionType);
							transactionValues.put(DatabaseHelper.PLAN_CATEGORY, transactionCategory);
							transactionValues.put(DatabaseHelper.PLAN_MEMO, transactionMemo);
							transactionValues.put(DatabaseHelper.PLAN_OFFSET, transactionOffset.getSQLDate(locale));
							transactionValues.put(DatabaseHelper.PLAN_RATE, transactionRate);
							transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "true");
							transactionValues.put(DatabaseHelper.PLAN_CLEARED, transactionCleared);

							//Cancel old plan
							((Plans) getSherlockActivity()).cancelPlan(oldRecord);

							//Update plan
							getSherlockActivity().getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI+"/"+ID), transactionValues, DatabaseHelper.PLAN_ID+"="+ID, null);

							//Reschedule plan
							final PlanRecord record = new PlanRecord(ID, transactionAccountID, transactionName, transactionValue.getBigDecimal(locale)+"", transactionType, transactionCategory, transactionMemo, transactionOffset.getSQLDate(locale), transactionRate, "true", transactionCleared);
							((Plans) getSherlockActivity()).schedule(record);
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
			String[] from = new String[] {DatabaseHelper.ACCOUNT_NAME, "_id"}; 
			int[] to = new int[] { android.R.id.text1};

			accountSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, data, from, to);
			accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			accountSpinner.setAdapter(accountSpinnerAdapter);
			Log.v("Plans-onLoadFinished", "load done. loader="+loader + " data="+data + " data size="+data.getCount());
			break;

		case PLAN_SUBCATEGORY_LOADER:
			from = new String[] {DatabaseHelper.SUBCATEGORY_NAME}; 
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

	private final class MyActionMode implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add(0, CONTEXT_MENU_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);  
			menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
			menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
			menu.add(0, CONTEXT_MENU_TOGGLE, 3, "Toggle").setIcon(android.R.drawable.ic_menu_revert);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.clear();
			if (adapterPlans.getSelectedCount() == 1 && mode != null) {
				menu.add(0, CONTEXT_MENU_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);  
				menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
				menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);				
				menu.add(0, CONTEXT_MENU_TOGGLE, 3, "Toggle").setIcon(android.R.drawable.ic_menu_revert);
				return true;
			} else if (adapterPlans.getSelectedCount() > 1) {
				menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
				menu.add(0, CONTEXT_MENU_TOGGLE, 3, "Toggle").setIcon(android.R.drawable.ic_menu_revert);
				return true;
			}

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			SparseBooleanArray selected = adapterPlans.getSelectedIds();
			PlanRecord record;

			switch (item.getItemId()) {
			case CONTEXT_MENU_VIEW:
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						DialogFragment newFragment = ViewDialogFragment.newInstance(adapterPlans.getPlan(selected.keyAt(i)).id);
						newFragment.show(getSupportFragmentManager(), "dialogView");
					}
				}

				mode.finish();
				return true;
			case CONTEXT_MENU_EDIT:
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						DialogFragment newFragment = EditDialogFragment.newInstance(adapterPlans.getPlan(selected.keyAt(i)));
						newFragment.show(getSupportFragmentManager(), "dialogEdit");
					}
				}

				mode.finish();
				return true;
			case CONTEXT_MENU_DELETE:				
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						record = adapterPlans.getPlan(selected.keyAt(i));

						Uri uri = Uri.parse(MyContentProvider.PLANS_URI + "/" + record.id);
						getContentResolver().delete(uri, DatabaseHelper.PLAN_ID+"="+record.id, null);

						Log.d("Plans", "Deleting " + record.name + " id:" + record.id);

						//Cancel all upcoming notifications
						cancelPlan(record);
					}
				}

				mode.finish();
				return true;

			case CONTEXT_MENU_TOGGLE:
				Intent intent;
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						record = adapterPlans.getPlan(selected.keyAt(i));

						intent = new Intent(Plans.this, PlanReceiver.class);
						intent.putExtra("plan_id", record.id);
						intent.putExtra("plan_acct_id",record.acctId);
						intent.putExtra("plan_name",record.name);
						intent.putExtra("plan_value",record.value);
						intent.putExtra("plan_type",record.type);
						intent.putExtra("plan_category",record.category);
						intent.putExtra("plan_memo",record.memo);
						intent.putExtra("plan_offset",record.offset);
						intent.putExtra("plan_rate",record.rate);
						intent.putExtra("plan_scheduled",record.scheduled);
						intent.putExtra("plan_cleared",record.cleared);

						PendingIntent sender = PendingIntent.getBroadcast(Plans.this, Integer.parseInt(record.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);

						try {
							if(record.scheduled.equals("true")){
								am.cancel(sender);

								ContentValues transactionValues=new ContentValues();
								transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "false");
								getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI+"/"+record.id), transactionValues, DatabaseHelper.PLAN_ID+"="+record.id, null);

								Toast.makeText(Plans.this, "Canceled plan:\n"+record.name, Toast.LENGTH_SHORT).show();
							}
							else{
								schedule(record);

								ContentValues transactionValues=new ContentValues();
								transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "true");
								getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI+"/"+record.id), transactionValues, DatabaseHelper.PLAN_ID+"="+record.id, null);
							}
						} catch (Exception e) {
							Toast.makeText(Plans.this, "Error toggling plan \n"+record.name, Toast.LENGTH_SHORT).show();
							Log.e("Plans-schedule", "Error toggling a plan. " + e.toString());
						}
					}
				}

				mode.finish();
				return true;

			default:
				mode.finish();
				Log.e("Plans-onActionItemClciked","ERROR. Clicked " + item);
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode=null;
			adapterPlans.removeSelection();
		}
	}

	@Override
	public void onDestroy() {
		if(mActionMode!=null){
			((ActionMode)mActionMode).finish();		
		}

		super.onDestroy();
	}

}//end of Plans