/* Class that handles the Transaction Fragment seen in the Checkbook screen
 * Does everything from setting up the view to Add/Delete/Edit Transactions to calculating the balance
 */

package com.databases.example;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.wizardpager.wizard.WizardDialogFragment;
import com.wizardpager.wizard.model.AbstractWizardModel;
import com.wizardpager.wizard.model.ModelCallbacks;
import com.wizardpager.wizard.model.Page;
import com.wizardpager.wizard.model.PageList;
import com.wizardpager.wizard.model.ReviewItem;
import com.wizardpager.wizard.ui.PageFragmentCallbacks;
import com.wizardpager.wizard.ui.StepPagerStrip;

public class Transactions extends SherlockFragment implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor>{
	private static final int TRANS_LOADER = 987654321;
	private static final int TRANS_SEARCH_LOADER = 98765;
	private static final int TRANS_SUBCATEGORY_LOADER = 987;

	private View myFragmentView;
	
	//Used to determine if fragment should show all transactions
	private boolean showAllTransactions=false;

	private static Button tTime;
	private static Button tDate;

	//ID of account transaction belongs to
	private static int account_id=0;

	private static String sortOrder = "null";

	private ListView lv = null;

	//Constants for ContextMenu
	private final int CONTEXT_MENU_VIEW=5;
	private final int CONTEXT_MENU_EDIT=6;
	private final int CONTEXT_MENU_DELETE=7;

	//ListView Adapter
	private static UserItemAdapter adapterTransactions = null;

	//For Autocomplete
	private static ArrayList<String> dropdownResults = new ArrayList<String>();

	//Adapter for category spinner
	private static SimpleCursorAdapter adapterCategory;

	//ActionMode
	protected Object mActionMode = null;
	private SparseBooleanArray mSelectedItemsIds;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		account_id=0;
	}//end onCreate

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		myFragmentView = inflater.inflate(R.layout.transactions, null, false);
		lv = (ListView)myFragmentView.findViewById(R.id.transaction_list);

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		//Set Listener for regular mouse click
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				if (mActionMode != null) {
					listItemChecked(position);
				}
				else{
					int selectionRowID = (int) adapterTransactions.getItemId(position);
					String item = adapterTransactions.getTransaction(position).name;

					Toast.makeText(Transactions.this.getActivity(), "Click\nRow: " + selectionRowID + "\nEntry: " + item, Toast.LENGTH_SHORT).show();
				}
			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		prefs.registerOnSharedPreferenceChangeListener(this);

		adapterCategory = new SimpleCursorAdapter(this.getActivity(), android.R.layout.simple_spinner_item, null, new String[] {DatabaseHelper.SUBCATEGORY_NAME}, new int[] { android.R.id.text1 });
		adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		adapterTransactions = new UserItemAdapter(this.getActivity(), null);
		lv.setAdapter(adapterTransactions);

		//Call Loaders to get data
		populate();

		//Arguments
		Bundle bundle=getArguments();

		//bundle is empty if from search, so don't add extra menu options
		if(bundle!=null){
			setHasOptionsMenu(true);
		}

		setRetainInstance(false);

		return myFragmentView;
	}

	//Used for ActionMode
	public void listItemChecked(int position){
		adapterTransactions.toggleSelection(position);
		boolean hasCheckedItems = adapterTransactions.getSelectedCount() > 0;

		if (hasCheckedItems && mActionMode == null){
			mActionMode = getSherlockActivity().startActionMode(new MyActionMode());
		}
		else if (!hasCheckedItems && mActionMode != null){
			((ActionMode) mActionMode).finish();
		}

		if(mActionMode != null){
			((ActionMode) mActionMode).invalidate();
			((ActionMode)mActionMode).setTitle(String.valueOf(adapterTransactions.getSelectedCount()));
		}
	}

	//Populate view with all the transactions of selected account
	protected void populate(){
		Bundle bundle=getArguments();
		boolean searchFragment=true;

		if(bundle!=null){
			if(bundle.getBoolean("showAll")){
				showAllTransactions = true;				
			}
			else{
				showAllTransactions = false;
			}

			if(bundle.getBoolean("boolSearch")){
				searchFragment = true;
			}
			else{
				searchFragment = false;
			}

			if(!showAllTransactions && !searchFragment){
				account_id = bundle.getInt("ID");				
			}

			Log.v("Transactions-populate","searchFragment="+searchFragment+"\nshowAllTransactions="+showAllTransactions+"\nAccount_id="+account_id);
		}

		if(showAllTransactions){
			Bundle b = new Bundle();
			b.putBoolean("boolShowAll", true);
			Log.v("Transactions-populate","start loader (all transactions)...");
			getLoaderManager().initLoader(TRANS_LOADER, b, this);
		}
		else if(searchFragment){
			String query = getActivity().getIntent().getStringExtra("query");

			try{
				Bundle b = new Bundle();
				b.putBoolean("boolSearch", true);
				b.putString("query", query);
				Log.v("Transactions-populate","start search loader...");
				getLoaderManager().initLoader(TRANS_SEARCH_LOADER, b, this);
			}
			catch(Exception e){
				Log.e("Transactions-populate", "Search Failed. Error e=" + e);
				Toast.makeText(this.getActivity(), "Search Failed\n"+e, Toast.LENGTH_SHORT).show();
				//return;
			}

		}
		else{
			Bundle b = new Bundle();
			b.putInt("aID", account_id);
			Log.v("Transactions-populate","start loader ("+DatabaseHelper.TRANS_ACCT_ID+"="+ account_id + ")...");
			getLoaderManager().initLoader(TRANS_LOADER, b, this);
		}

		//Load the categories
		getLoaderManager().initLoader(TRANS_SUBCATEGORY_LOADER, null, this);
	}

	//For Adding a Transaction
	public void transactionAdd(){
		if(account_id==0){
			Log.e("Transaction-AddDialog", "No account selected before attempting to add transaction...");
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setTitle("No Account Selected");
			alertDialogBuilder.setMessage("Please select an account before attempting to add a transaction");
			alertDialogBuilder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			alertDialogBuilder.create().show();
		}
		else{
			DialogExample frag = DialogExample.newInstance(null);
			frag.show(getChildFragmentManager(), "dialogAdd");
		}
	}//end of transactionAdd

	//For Sorting Transactions
	public void transactionSort(){
		DialogFragment newFragment = SortDialogFragment.newInstance();
		newFragment.show(getChildFragmentManager(), "dialogSort");		
	}
	//For Menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		View account_frame = getActivity().findViewById(R.id.account_frag_frame);

		if(account_frame!=null){
			SubMenu subMMenuTransaction = menu.addSubMenu("Transaction");
			subMMenuTransaction.add(com.actionbarsherlock.view.Menu.NONE, R.id.transaction_menu_add, com.actionbarsherlock.view.Menu.NONE, "Add");
			subMMenuTransaction.add(com.actionbarsherlock.view.Menu.NONE, R.id.transaction_menu_schedule, com.actionbarsherlock.view.Menu.NONE, "Schedule");
			subMMenuTransaction.add(com.actionbarsherlock.view.Menu.NONE, R.id.transaction_menu_sort, com.actionbarsherlock.view.Menu.NONE, "Sort");

			MenuItem subMenu1Item = subMMenuTransaction.getItem();
			subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		else{
			menu.clear();
			inflater.inflate(R.layout.transaction_menu, menu);
			SearchWidget searchWidget = new SearchWidget(getSherlockActivity(),menu.findItem(R.id.transaction_menu_search).getActionView());
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

		case R.id.transaction_menu_schedule:    
			Intent intentPlans = new Intent(getActivity(), Plans.class);
			getActivity().startActivity(intentPlans);
			return true;

		case R.id.transaction_menu_sort:    
			transactionSort();
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.e("Transactions-onSharedPreferenceChanged","Options Changed");
		if(!isDetached()){
			Log.e("Transactions-onSharedPreferenceChanged","Transaction is attached");
			//Toast.makeText(this.getActivity(), "Transaction is attached", Toast.LENGTH_SHORT).show();
			//populate();
		}
		else{
			Log.e("Transactions-onSharedPreferenceChanged","Transaction is detached");
			//Toast.makeText(this.getActivity(), "Transaction is detached", Toast.LENGTH_SHORT).show();			
		}
	}

	//Method to help create TimePicker
	public static class TimePickerFragment extends DialogFragment
	implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar cal = Calendar.getInstance();

			SimpleDateFormat dateFormatHour = new SimpleDateFormat("hh");
			SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");

			int hour = Integer.parseInt(dateFormatHour.format(cal.getTime()));
			int minute = Integer.parseInt(dateFormatMinute.format(cal.getTime()));

			return new TimePickerDialog(getActivity(), this, hour, minute,
					false);
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			DateTime time = new DateTime();
			time.setStringSQL(hourOfDay + ":" + minute);

			if(tTime!=null){
				tTime.setText(time.getReadableTime());
			}

			if(TransactionOptionalFragment.mPage!=null){
				TransactionOptionalFragment.mPage.getData().putString(TransactionOptionalPage.TIME_DATA_KEY, time.getReadableTime());
				TransactionOptionalFragment.mPage.notifyDataChanged();
			}

		}
	}

	//Method to help create DatePicker
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

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			DateTime date = new DateTime();
			date.setStringSQL(year + "-" + (month+1) + "-" + day);

			if(tDate!=null){
				tDate.setText(date.getReadableDate());
			}

			if(TransactionOptionalFragment.mPage!=null){
				TransactionOptionalFragment.mPage.getData().putString(TransactionOptionalPage.DATE_DATA_KEY, date.getReadableDate());
				TransactionOptionalFragment.mPage.notifyDataChanged();
			}

		}
	}

	public class UserItemAdapter extends CursorAdapter {
		public UserItemAdapter(Context context, Cursor transactions) {
			super(context, transactions);
			mSelectedItemsIds = new SparseBooleanArray();
		}

		public TransactionRecord getTransaction(long position){
			final Cursor group = getCursor();

			group.moveToPosition((int) position);
			final int idColumn = group.getColumnIndex(DatabaseHelper.TRANS_ID);
			final int acctIDColumn = group.getColumnIndex(DatabaseHelper.TRANS_ACCT_ID);
			final int planIDColumn = group.getColumnIndex(DatabaseHelper.TRANS_PLAN_ID);
			final int nameColumn = group.getColumnIndex(DatabaseHelper.TRANS_NAME);
			final int valueColumn = group.getColumnIndex(DatabaseHelper.TRANS_VALUE);
			final int typeColumn = group.getColumnIndex(DatabaseHelper.TRANS_TYPE);
			final int categoryColumn = group.getColumnIndex(DatabaseHelper.TRANS_CATEGORY);
			final int checknumColumn = group.getColumnIndex(DatabaseHelper.TRANS_CHECKNUM);
			final int memoColumn = group.getColumnIndex(DatabaseHelper.TRANS_MEMO);
			final int timeColumn = group.getColumnIndex(DatabaseHelper.TRANS_TIME);
			final int dateColumn = group.getColumnIndex(DatabaseHelper.TRANS_DATE);
			final int clearedColumn = group.getColumnIndex(DatabaseHelper.TRANS_CLEARED);

			//int id = group.getInt(idColumn);
			final int id = group.getInt(0);
			final int acctId = group.getInt(acctIDColumn);
			final int planId = group.getInt(planIDColumn);
			final String name = group.getString(nameColumn);
			final String value = group.getString(valueColumn);
			final String type = group.getString(typeColumn);
			final String category = group.getString(categoryColumn);
			final String checknum = group.getString(checknumColumn);
			final String memo = group.getString(memoColumn);
			final String time = group.getString(timeColumn);
			final String date = group.getString(dateColumn);
			final String cleared = group.getString(clearedColumn);

			final TransactionRecord record = new TransactionRecord(id, acctId, planId, name, value,type,category,checknum,memo,time,date,cleared);
			return record;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View v = view;
			Cursor user = cursor;

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_transaction", true);

			if (user != null) {
				TextView tvName = (TextView) v.findViewById(R.id.transaction_name);
				TextView tvValue = (TextView) v.findViewById(R.id.transaction_value);
				TextView tvType = (TextView) v.findViewById(R.id.transaction_type);
				TextView tvCategory = (TextView) v.findViewById(R.id.transaction_category);
				TextView tvChecknum = (TextView) v.findViewById(R.id.transaction_checknum);
				TextView tvMemo = (TextView) v.findViewById(R.id.transaction_memo);
				TextView tvDate = (TextView) v.findViewById(R.id.transaction_date);
				TextView tvTime = (TextView) v.findViewById(R.id.transaction_time);
				TextView tvCleared = (TextView) v.findViewById(R.id.transaction_cleared);

				int idColumn = user.getColumnIndex(DatabaseHelper.TRANS_ID);
				int acctIDColumn = user.getColumnIndex(DatabaseHelper.TRANS_ACCT_ID);
				int planIDColumn = user.getColumnIndex(DatabaseHelper.TRANS_PLAN_ID);
				int nameColumn = user.getColumnIndex(DatabaseHelper.TRANS_NAME);
				int valueColumn = user.getColumnIndex(DatabaseHelper.TRANS_VALUE);
				int typeColumn = user.getColumnIndex(DatabaseHelper.TRANS_TYPE);
				int categoryColumn = user.getColumnIndex(DatabaseHelper.TRANS_CATEGORY);
				int checknumColumn = user.getColumnIndex(DatabaseHelper.TRANS_CHECKNUM);
				int memoColumn = user.getColumnIndex(DatabaseHelper.TRANS_MEMO);
				int timeColumn = user.getColumnIndex(DatabaseHelper.TRANS_TIME);
				int dateColumn = user.getColumnIndex(DatabaseHelper.TRANS_DATE);
				int clearedColumn = user.getColumnIndex(DatabaseHelper.TRANS_CLEARED);

				int id = user.getInt(0);
				int acctId = user.getInt(acctIDColumn);
				int planId = user.getInt(planIDColumn);
				String name = user.getString(nameColumn);
				Money value = new Money(user.getString(valueColumn));
				String type = user.getString(typeColumn);
				String category = user.getString(categoryColumn);
				String checknum = user.getString(checknumColumn);
				String memo = user.getString(memoColumn);
				String time = user.getString(timeColumn);
				String date = user.getString(dateColumn);
				String cleared = user.getString(clearedColumn);
				Locale locale=getResources().getConfiguration().locale;

				//Change gradient
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.transaction_gradient);
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
					Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
				}

				if (name != null) {
					tvName.setText(name);

					if(planId!=0){
						tvName.setTextColor(Color.parseColor("#FF9933"));
					}
					else{
						tvName.setTextColor(Color.parseColor("#000000"));
					}

				}

				if(value != null) {
					tvValue.setText("Value: " + value.getNumberFormat(locale));
				}

				if(type != null) {
					tvType.setText("Type: " + type );
				}

				if(category != null) {
					tvCategory.setText("Category: " + category );
				}

				if(checknum != null) {
					tvChecknum.setText("Check Num: " + checknum );
				}

				if(memo != null) {
					tvMemo.setText("Memo: " + memo );
				}

				if(date != null) {
					DateTime d = new DateTime();
					d.setStringSQL(date);
					tvDate.setText("Date: " + d.getReadableDate());
				}

				if(time != null) {
					DateTime t = new DateTime();
					t.setStringSQL(time);
					tvTime.setText("Time: " + t.getReadableTime());
				}

				if(cleared != null) {
					tvCleared.setText("Cleared: " + cleared );
				}

				v.setBackgroundColor(mSelectedItemsIds.get(user.getPosition())? 0x9934B5E4: Color.TRANSPARENT);				
			}			

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = vi.inflate(R.layout.transaction_item, null);

			LinearLayout l=(LinearLayout)v.findViewById(R.id.transaction_layout);
			TextView tvName = (TextView)v.findViewById(R.id.transaction_name);
			TextView tvValue = (TextView)v.findViewById(R.id.transaction_value);
			TextView tvType = (TextView)v.findViewById(R.id.transaction_type);
			TextView tvCategory = (TextView)v.findViewById(R.id.transaction_category);
			TextView tvChecknum = (TextView)v.findViewById(R.id.transaction_checknum);
			TextView tvMemo = (TextView)v.findViewById(R.id.transaction_memo);
			TextView tvTime = (TextView)v.findViewById(R.id.transaction_time);
			TextView tvDate = (TextView)v.findViewById(R.id.transaction_date);
			TextView tvCleared = (TextView)v.findViewById(R.id.transaction_cleared);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Transactions.this.getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_transaction", true);

			//Change Background Colors
			try{
				if(!useDefaults){
					int startColor = prefs.getInt("key_transaction_startBackgroundColor", Color.parseColor("#FFFFFF"));
					int endColor = prefs.getInt("key_transaction_endBackgroundColor", Color.parseColor("#FFFFFF"));

					GradientDrawable defaultGradient = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {startColor,endColor});
					l.setBackgroundDrawable(defaultGradient);
				}
			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
			}

			try{
				String DefaultSize = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_nameSize), "24");

				if(useDefaults){
					tvName.setTextSize(24);
				}
				else{
					tvName.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_transaction_nameColor", Color.parseColor("#222222"));

				if(useDefaults){
					tvName.setTextColor(Color.parseColor("#222222"));
				}
				else{
					tvName.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				String DefaultSize = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_fieldSize), "14");

				if(useDefaults){
					tvValue.setTextSize(14);
					tvDate.setTextSize(14);
					tvTime.setTextSize(14);
					tvCategory.setTextSize(14);
					tvMemo.setTextSize(14);
					tvChecknum.setTextSize(14);
					tvCleared.setTextSize(14);
					tvType.setTextSize(14);
				}
				else{
					tvValue.setTextSize(Integer.parseInt(DefaultSize));
					tvType.setTextSize(Integer.parseInt(DefaultSize));
					tvCategory.setTextSize(Integer.parseInt(DefaultSize));
					tvChecknum.setTextSize(Integer.parseInt(DefaultSize));
					tvMemo.setTextSize(Integer.parseInt(DefaultSize));
					tvTime.setTextSize(Integer.parseInt(DefaultSize));
					tvDate.setTextSize(Integer.parseInt(DefaultSize));
					tvCleared.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_transaction_fieldColor", Color.parseColor("#000000"));

				if(useDefaults){
					tvValue.setTextColor(Color.parseColor("#000000"));
					tvType.setTextColor(Color.parseColor("#000000"));
					tvCategory.setTextColor(Color.parseColor("#000000"));
					tvChecknum.setTextColor(Color.parseColor("#000000"));
					tvMemo.setTextColor(Color.parseColor("#000000"));
					tvTime.setTextColor(Color.parseColor("#000000"));
					tvDate.setTextColor(Color.parseColor("#000000"));
					tvCleared.setTextColor(Color.parseColor("#000000"));
				}
				else{
					tvValue.setTextColor(DefaultColor);
					tvType.setTextColor(DefaultColor);
					tvCategory.setTextColor(DefaultColor);
					tvChecknum.setTextColor(DefaultColor);
					tvMemo.setTextColor(DefaultColor);
					tvTime.setTextColor(DefaultColor);
					tvDate.setTextColor(DefaultColor);
					tvCleared.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_nameField", true)){
				tvName.setVisibility(View.VISIBLE);
			}
			else{
				tvName.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_valueField", true)){
				tvValue.setVisibility(View.VISIBLE);
			}
			else{
				tvValue.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_transaction_typeField", false) && !useDefaults){
				tvType.setVisibility(View.VISIBLE);
			}
			else{
				tvType.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_categoryField", true)){
				tvCategory.setVisibility(View.VISIBLE);
			}
			else{
				tvCategory.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_transaction_checknumField", false) && !useDefaults){
				tvChecknum.setVisibility(View.VISIBLE);
			}
			else{
				tvChecknum.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_transaction_memoField", false) && !useDefaults){
				tvMemo.setVisibility(View.VISIBLE);
			}
			else{
				tvMemo.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_dateField", true)){
				tvDate.setVisibility(View.VISIBLE);
			}
			else{
				tvDate.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_transaction_timeField", false) && !useDefaults){
				tvTime.setVisibility(View.VISIBLE);
			}
			else{
				tvTime.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_transaction_clearedField", false) && !useDefaults){
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

	}

	//An Object Class used to hold the data of each transaction record
	public class TransactionRecord {
		protected int id;
		protected int acctId;
		protected int planId;
		protected String name;
		protected String value;
		protected String type;
		protected String category;
		protected String checknum;
		protected String memo;
		protected String time;
		protected String date;
		protected String cleared;

		public TransactionRecord(int id, int acctId, int planId, String name, String value, String type, String category, String checknum, String memo, String time, String date, String cleared) {
			this.id = id;
			this.acctId = acctId;
			this.planId = planId;
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

	public static class ViewDialogFragment extends SherlockDialogFragment {

		public static ViewDialogFragment newInstance(int id) {
			ViewDialogFragment frag = new ViewDialogFragment();
			Bundle args = new Bundle();
			args.putInt("id", id);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int id = getArguments().getInt("id");
			final Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.TRANSACTIONS_URI+"/"+id), null, null, null, null);

			int entry_id = 0;
			int entry_acctId = 0;
			int entry_planId = 0;
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
				entry_id = c.getInt(c.getColumnIndex(DatabaseHelper.TRANS_ID));
				entry_acctId = c.getInt(c.getColumnIndex(DatabaseHelper.TRANS_ACCT_ID));
				entry_planId = c.getInt(c.getColumnIndex(DatabaseHelper.TRANS_PLAN_ID));
				entry_name = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_NAME));
				entry_value = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_VALUE));
				entry_type = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_TYPE));
				entry_category = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_CATEGORY));
				entry_checknum = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_CHECKNUM));
				entry_memo = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_MEMO));
				entry_time = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_TIME));
				entry_date = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_DATE));
				entry_cleared = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_CLEARED));
			}while(c.moveToNext());

			final LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
			final View transStatsView = li.inflate(R.layout.transaction_item, null);

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			final boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_account", true);

			final Locale locale=getResources().getConfiguration().locale;
			final Money value = new Money(entry_value);

			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

			alertDialogBuilder.setView(transStatsView);
			alertDialogBuilder.setCancelable(true);

			//Change gradient
			try{
				LinearLayout l;
				l=(LinearLayout)transStatsView.findViewById(R.id.transaction_gradient);
				GradientDrawable defaultGradientPos = new GradientDrawable(
						GradientDrawable.Orientation.BOTTOM_TOP,
						new int[] {0xFF4ac925,0xFF4ac925});

				GradientDrawable defaultGradientNeg = new GradientDrawable(
						GradientDrawable.Orientation.BOTTOM_TOP,
						new int[] {0xFFe00707,0xFFe00707});

				if(useDefaults){
					if(entry_type.contains("Deposit")){
						l.setBackgroundDrawable(defaultGradientPos);
					}
					else{
						l.setBackgroundDrawable(defaultGradientNeg);
					}

				}
				else{
					if(entry_type.contains("Deposit")){
						l.setBackgroundDrawable(defaultGradientPos);
					}
					else{
						l.setBackgroundDrawable(defaultGradientNeg);
					}
				}

			}
			catch(Exception e){
				Toast.makeText(getActivity(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
			}

			//Set Statistics
			TextView statsName = (TextView)transStatsView.findViewById(R.id.transaction_name);
			statsName.setText(entry_name);
			TextView statsValue = (TextView)transStatsView.findViewById(R.id.transaction_value);
			statsValue.setText("Value: " + value.getNumberFormat(locale));
			TextView statsType = (TextView)transStatsView.findViewById(R.id.transaction_type);
			statsType.setText("Type: " + entry_type);
			TextView statsCategory = (TextView)transStatsView.findViewById(R.id.transaction_category);
			statsCategory.setText("Category: " + entry_category);
			TextView statsCheckNum = (TextView)transStatsView.findViewById(R.id.transaction_checknum);
			statsCheckNum.setText("Check Num: " + entry_checknum);
			TextView statsMemo = (TextView)transStatsView.findViewById(R.id.transaction_memo);
			statsMemo.setText("Memo: " + entry_memo);
			DateTime d = new DateTime();
			d.setStringSQL(entry_date);
			TextView statsDate = (TextView)transStatsView.findViewById(R.id.transaction_date);
			statsDate.setText("Date: " + d.getReadableDate());
			DateTime t = new DateTime();
			t.setStringSQL(entry_time);
			TextView statsTime = (TextView)transStatsView.findViewById(R.id.transaction_time);
			statsTime.setText("Time: " + t.getReadableTime());
			TextView statsCleared = (TextView)transStatsView.findViewById(R.id.transaction_cleared);
			statsCleared.setText("Cleared: " + entry_cleared);

			//c.close();
			return alertDialogBuilder.create();
		}
	}

	//Class that handles sort dialog
	public static class SortDialogFragment extends SherlockDialogFragment {

		public static SortDialogFragment newInstance() {
			SortDialogFragment frag = new SortDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
			View transactionSortView = li.inflate(R.layout.sort_transactions, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

			alertDialogBuilder.setView(transactionSortView);
			alertDialogBuilder.setTitle("Sort");
			alertDialogBuilder.setCancelable(true);

			ListView sortOptions = (ListView)transactionSortView.findViewById(R.id.sort_options);
			sortOptions.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					switch (position) {
					//Newest
					case 0:
						sortOrder = DatabaseHelper.TRANS_DATE + " DESC, " + DatabaseHelper.TRANS_TIME + " DESC";						
						break;

						//Oldest
					case 1:
						sortOrder = DatabaseHelper.TRANS_DATE + " ASC, " + DatabaseHelper.TRANS_TIME + " ASC";
						break;

						//Largest
					case 2:
						sortOrder = DatabaseHelper.TRANS_TYPE+" ASC, CAST ("+DatabaseHelper.TRANS_VALUE+" AS INTEGER)" + " DESC";
						break;

						//Smallest
					case 3:
						sortOrder = DatabaseHelper.TRANS_TYPE+" ASC, CAST ("+DatabaseHelper.TRANS_VALUE+" AS INTEGER)" + " ASC";
						break;

						//Category	
					case 4:
						sortOrder = DatabaseHelper.TRANS_CATEGORY + " ASC";
						break;

						//Type
					case 5:
						sortOrder = DatabaseHelper.TRANS_TYPE + " ASC";
						break;

						//Alphabetical
					case 6:
						sortOrder = DatabaseHelper.TRANS_NAME + " ASC";
						break;

						//None
					case 7:
						sortOrder = null;
						break;

					default:
						Log.e("Transactions-SortFragment","Unknown Sorting Option!");
						break;

					}//end switch

					getDialog().cancel();
				}
			});			

			return alertDialogBuilder.create();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

		switch (loaderID) {
		case TRANS_LOADER:
			if(bundle!=null && bundle.getBoolean("boolShowAll")){
				Log.v("Transactions-onCreateLoader","new loader (ShowAll) created");
				return new CursorLoader(
						getActivity(),   	// Parent activity context
						MyContentProvider.TRANSACTIONS_URI,// Table to query
						null,     			// Projection to return
						null,            	// No selection clause
						null,            	// No selection arguments
						sortOrder          	// Default sort order
						);
			}
			else{
				String selection = DatabaseHelper.TRANS_ACCT_ID+"=" + account_id;
				Log.v("Transactions-onCreateLoader","new loader created");
				return new CursorLoader(
						getActivity(),   			// Parent activity context
						MyContentProvider.TRANSACTIONS_URI,// Table to query
						null,     					// Projection to return
						selection,					// No selection clause
						null,						// No selection arguments
						sortOrder             		// Default sort order
						);				
			}
		case TRANS_SEARCH_LOADER:
			String query = getActivity().getIntent().getStringExtra("query");
			Log.v("Transactions-onCreateLoader","new loader (boolSearch "+ query + ") created");
			return new CursorLoader(
					getActivity(),   	// Parent activity context
					(Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/SEARCH/" + query)),// Table to query
					null,     			// Projection to return
					null,            	// No selection clause
					null,            	// No selection arguments
					sortOrder           // Default sort order
					);			

		case TRANS_SUBCATEGORY_LOADER:
			Log.v("Transactions-onCreateLoader","new category loader created");
			return new CursorLoader(
					getActivity(),   	// Parent activity context
					MyContentProvider.SUBCATEGORIES_URI,// Table to query
					null,     			// Projection to return
					null,            	// No selection clause
					null,            	// No selection arguments
					sortOrder           // Default sort order
					);			

		default:
			Log.e("Transactions-onCreateLoader", "Not a valid CursorLoader ID");
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		TextView footerTV = (TextView)this.myFragmentView.findViewById(R.id.transaction_footer);

		switch(loader.getId()){
		case TRANS_LOADER:
			adapterTransactions.swapCursor(data);
			Log.v("Transactions-onLoadFinished", "loader finished. loader="+loader.getId() + " data="+data + " data size="+data.getCount());

			final int valueColumn = data.getColumnIndex(DatabaseHelper.TRANS_VALUE);
			final int typeColumn = data.getColumnIndex(DatabaseHelper.TRANS_TYPE);
			BigDecimal totalBalance = BigDecimal.ZERO;
			Locale locale=getResources().getConfiguration().locale;

			data.moveToPosition(-1);
			while(data.moveToNext()){
				if(data.getString(typeColumn).equals("Deposit")){
					totalBalance = totalBalance.add(new Money(data.getString(valueColumn)).getBigDecimal(locale));					
				}
				else{
					totalBalance = totalBalance.subtract(new Money(data.getString(valueColumn)).getBigDecimal(locale));					
				}
			}

			try{
				TextView noResult = (TextView)myFragmentView.findViewById(R.id.transaction_noTransaction);
				lv.setEmptyView(noResult);
				noResult.setText("No Transactions\n\n To Add A Transaction, Please Use The ActionBar On The Top");

				footerTV.setText("Total Balance: " + new Money(totalBalance).getNumberFormat(locale));
			}
			catch(Exception e){
				Log.e("Transactions-onLoadFinished", "Error setting balance TextView. e="+e);
			}

			if(account_id!=0){
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.ACCOUNT_BALANCE, totalBalance+"");		
				getActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+account_id), values,"AcctID ="+account_id, null);
			}

			break;

		case TRANS_SEARCH_LOADER:
			adapterTransactions.swapCursor(data);
			Log.v("Transactions-onLoadFinished", "loader finished. loader="+loader.getId() + " data="+data + " data size="+data.getCount());

			try{
				TextView noResult = (TextView)myFragmentView.findViewById(R.id.transaction_noTransaction);
				lv.setEmptyView(noResult);
				noResult.setText("No Transactions Found");

				footerTV.setText("Search Results");
			}
			catch(Exception e){
				Log.e("Transactions-onLoadFinished", "Error setting search TextView. e="+e);
			}
			break;

		case TRANS_SUBCATEGORY_LOADER:
			adapterCategory.swapCursor(data);
			break;

		default:
			Log.e("Transactions-onLoadFinished", "Error. Unknown loader ("+loader.getId());
			break;
		}

		if(!getSherlockActivity().getSupportLoaderManager().hasRunningLoaders()){
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);			
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch(loader.getId()){
		case TRANS_LOADER:
			adapterTransactions.swapCursor(null);
			Log.v("Transactions-onLoaderReset", "loader reset. loader="+loader.getId());
			break;

		case TRANS_SEARCH_LOADER:
			adapterTransactions.swapCursor(null);
			Log.v("Transactions-onLoaderReset", "loader reset. loader="+loader.getId());
			break;

		case TRANS_SUBCATEGORY_LOADER:
			adapterCategory.swapCursor(null);
			Log.v("Transactions-onLoaderReset", "loader reset. loader="+loader.getId());
			break;

		default:
			Log.e("Transactions-onLoadFinished", "Error. Unknown loader ("+loader.getId());
			break;
		}	
	}

	private final class MyActionMode implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add(0, CONTEXT_MENU_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);  
			menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
			menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.clear();
			if (adapterTransactions.getSelectedCount() == 1 && mode != null) {
				menu.add(0, CONTEXT_MENU_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);  
				menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
				menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);				
				return true;
			} else if (adapterTransactions.getSelectedCount() > 1) {
				menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
				return true;
			}

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			SparseBooleanArray selected = adapterTransactions.getSelectedIds();

			switch (item.getItemId()) {
			case CONTEXT_MENU_VIEW:
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						DialogFragment newFragment = ViewDialogFragment.newInstance(adapterTransactions.getTransaction(selected.keyAt(i)).id);
						newFragment.show(getChildFragmentManager(), "dialogView");
					}
				}

				mode.finish();
				return true;
			case CONTEXT_MENU_EDIT:
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						//DialogFragment newFragment = EditDialogFragment.newInstance(adapterTransactions.getTransaction(selected.keyAt(i)));
						//newFragment.show(getChildFragmentManager(), "dialogEdit");

						final TransactionRecord record = adapterTransactions.getTransaction(selected.keyAt(i));

						final Bundle bundle = new Bundle();

						final Bundle bdl1 = new Bundle();
						bdl1.putInt("id", record.id);
						bdl1.putInt("acct_id", record.acctId);
						bdl1.putInt("plan_id", record.planId);
						bdl1.putString("name",record.name);
						bdl1.putString("value",record.value);
						bdl1.putString("type",record.type);
						bdl1.putString("category",record.category);
						bundle.putBundle("Transaction Info",bdl1);

						final Bundle bdl2 = new Bundle();
						bdl2.putString("checknum",record.checknum);
						bdl2.putString("memo", record.memo);
						bdl2.putString("date",record.date);
						bdl2.putString("time",record.time);
						bdl2.putString("cleared",record.cleared);
						bundle.putBundle("Optional",bdl2);

						final DialogExample frag = DialogExample.newInstance(bundle);
						frag.show(getChildFragmentManager(), "dialogEdit");						
					}
				}

				mode.finish();
				return true;
			case CONTEXT_MENU_DELETE:
				TransactionRecord record;
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						record = adapterTransactions.getTransaction(selected.keyAt(i));

						Uri uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + record.id);
						getActivity().getContentResolver().delete(uri, DatabaseHelper.TRANS_ID+"="+record.id, null);

						Toast.makeText(getActivity(), "Deleted Transaction:\n" + record.name, Toast.LENGTH_SHORT).show();
					}
				}

				mode.finish();
				return true;

			default:
				mode.finish();
				Log.e("Transactions-onActionItemClciked","ERROR. Clicked " + item);
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode=null;
			adapterTransactions.removeSelection();
		}
	}

	@Override
	public void onDestroyView() {
		if(mActionMode!=null){
			((ActionMode)mActionMode).finish();		
		}

		super.onDestroyView();
	}

	public static class DialogExample extends WizardDialogFragment{

		private AbstractWizardModel mWizardModel = new AddWizardModel(getActivity());	

		public static DialogExample newInstance(Bundle bundle) {
			DialogExample frag = new DialogExample();
			frag.setArguments(bundle);			
			return frag;
		}

		//Set Style & Theme of Dialog
		@SuppressLint("InlinedApi")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if(android.os.Build.VERSION.SDK_INT>14){
				setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog);
			}
			else{
				setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Dialog);
			}

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			View myFragmentView = inflater.inflate(R.layout.wizard, null, false);

			ViewPager mPager = (ViewPager) myFragmentView.findViewById(R.id.pager);
			mPager.setOffscreenPageLimit(5);
			StepPagerStrip mStepPagerStrip = (StepPagerStrip) myFragmentView.findViewById(R.id.strip);
			Button mNextButton = (Button) myFragmentView.findViewById(R.id.next_button);
			Button mPrevButton = (Button) myFragmentView.findViewById(R.id.prev_button);
			setControls(mPager, mStepPagerStrip, mNextButton, mPrevButton);

			//Load Data into Wizard
			final Bundle bundle = getArguments();
			if(bundle!=null){
				mWizardModel.load(bundle);
			}

			return myFragmentView;
		}

		//Create Wizard
		@Override
		public AbstractWizardModel onCreateModel() {
			return mWizardModel;
		}

		//Method that runs after wizard is finished
		@Override
		public void onSubmit() {
			final Bundle bundleInfo = mWizardModel.findByKey("Transaction Info").getData();
			final Bundle bundleOptional = mWizardModel.findByKey("Optional").getData();
			final Locale locale=getResources().getConfiguration().locale;

			String value="";
			final DateTime transactionDate = new DateTime();
			transactionDate.setStringReadable(bundleOptional.getString(TransactionOptionalPage.DATE_DATA_KEY).trim());
			final DateTime transactionTime = new DateTime();
			transactionTime.setStringReadable(bundleOptional.getString(TransactionOptionalPage.TIME_DATA_KEY).trim());

			//Check to see if value is a number
			boolean validValue = false;
			try{
				Money transactionValue = new Money(bundleInfo.getString(TransactionInfoPage.VALUE_DATA_KEY).trim());
				value = transactionValue.getBigDecimal(locale)+"";
				validValue=true;
			}
			catch(Exception e){
				validValue=false;
				Toast.makeText(getActivity(), "Please enter a valid value", Toast.LENGTH_SHORT).show();
			}

			if(validValue){
				getDialog().cancel();

				if(getArguments()!=null){					
					ContentValues transactionValues=new ContentValues();
					transactionValues.put(DatabaseHelper.TRANS_ID, bundleInfo.getInt(TransactionInfoPage.ID_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, bundleInfo.getInt(TransactionInfoPage.ACCOUNT_ID_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, bundleInfo.getInt(TransactionInfoPage.PLAN_ID_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_NAME, bundleInfo.getString(TransactionInfoPage.NAME_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_VALUE, value);
					transactionValues.put(DatabaseHelper.TRANS_TYPE, bundleInfo.getString(TransactionInfoPage.TYPE_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_CATEGORY, bundleInfo.getString(TransactionInfoPage.CATEGORY_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, bundleOptional.getString(TransactionOptionalPage.CHECKNUM_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_MEMO, bundleOptional.getString(TransactionOptionalPage.MEMO_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_TIME, transactionTime.getSQLTime(locale));
					transactionValues.put(DatabaseHelper.TRANS_DATE, transactionDate.getSQLDate(locale));
					transactionValues.put(DatabaseHelper.TRANS_CLEARED, bundleOptional.getString(TransactionOptionalPage.CLEARED_DATA_KEY));
					
					getActivity().getContentResolver().update(Uri.parse(MyContentProvider.TRANSACTIONS_URI+"/"+bundleInfo.getInt(TransactionInfoPage.ID_DATA_KEY)), transactionValues, DatabaseHelper.TRANS_ID+"="+bundleInfo.getInt(TransactionInfoPage.ID_DATA_KEY), null);
				}
				else{
					ContentValues transactionValues=new ContentValues();
					transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, account_id);
					transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, 0);
					transactionValues.put(DatabaseHelper.TRANS_NAME, bundleInfo.getString(TransactionInfoPage.NAME_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_VALUE, value);
					transactionValues.put(DatabaseHelper.TRANS_TYPE, bundleInfo.getString(TransactionInfoPage.TYPE_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_CATEGORY, bundleInfo.getString(TransactionInfoPage.CATEGORY_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, bundleOptional.getString(TransactionOptionalPage.CHECKNUM_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_MEMO, bundleOptional.getString(TransactionOptionalPage.MEMO_DATA_KEY));
					transactionValues.put(DatabaseHelper.TRANS_TIME, transactionTime.getSQLTime(locale));
					transactionValues.put(DatabaseHelper.TRANS_DATE, transactionDate.getSQLDate(locale));
					transactionValues.put(DatabaseHelper.TRANS_CLEARED, bundleOptional.getString(TransactionOptionalPage.CLEARED_DATA_KEY));

					getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
				}
				
			}

		}

		//Allow back button to be used to go back a step in the wizard
		@Override
		public boolean useBackForPrevious() {
			return true;
		}

	}

	public static class AddWizardModel extends AbstractWizardModel {
		public AddWizardModel(Context context) {
			super(context);
		}

		@Override
		protected PageList onNewRootPageList() {
			return new PageList(

					new TransactionInfoPage(this, "Transaction Info")
					.setRequired(true),

					new TransactionOptionalPage(this, "Optional")
					);
		}

	}

	public static class TransactionInfoFragment extends SherlockFragment {
		private static final String ARG_KEY = "transaction_info_key";

		private PageFragmentCallbacks mCallbacks;
		private String mKey;
		private TransactionInfoPage mPage;
		private EditText mNameView;
		private EditText mValueView;
		private Spinner mTypeView;
		private Spinner mCategoryView;

		public static TransactionInfoFragment create(String key) {
			Bundle args = new Bundle();
			args.putString(ARG_KEY, key);

			TransactionInfoFragment fragment = new TransactionInfoFragment();
			fragment.setArguments(args);
			return fragment;
		}

		public TransactionInfoFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			Bundle args = getArguments();
			mKey = args.getString(ARG_KEY);
			mPage = (TransactionInfoPage) mCallbacks.onGetPage(mKey);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.transaction_page_info, container, false);
			((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

			mNameView = ((EditText) rootView.findViewById(R.id.transaction_name));
			mNameView.setText(mPage.getData().getString(TransactionInfoPage.NAME_DATA_KEY));

			mValueView = ((EditText) rootView.findViewById(R.id.transaction_value));
			mValueView.setText(mPage.getData().getString(TransactionInfoPage.VALUE_DATA_KEY));

			mTypeView = (Spinner) rootView.findViewById(R.id.spinner_transaction_type);
			if(mPage.getData().getString(TransactionInfoPage.TYPE_DATA_KEY)==null || mPage.getData().getString(TransactionInfoPage.TYPE_DATA_KEY).equals("Withdraw")){
				mTypeView.setSelection(0);				
			}
			else{
				mTypeView.setSelection(1);
			}

			mCategoryView = (Spinner) rootView.findViewById(R.id.spinner_transaction_category);
			mCategoryView.setAdapter(adapterCategory);

			String category = mPage.getData().getString(TransactionInfoPage.CATEGORY_DATA_KEY); 
			final int count = adapterCategory.getCount();
			String catName;
			Cursor cursor;

			if(category!=null){
				for (int i = 0; i < count; i++) {
					cursor = (Cursor) mCategoryView.getItemAtPosition(i);
					catName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME));
					if (catName.contentEquals(category)) {
						mCategoryView.setSelection(i);
						break;
					}
				}
			}

			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);

			if (!(activity instanceof PageFragmentCallbacks)) {
				mCallbacks = (PageFragmentCallbacks) getParentFragment();
			}
			else{
				mCallbacks = (PageFragmentCallbacks) activity;
			}
		}

		@Override
		public void onDetach() {
			super.onDetach();
			mCallbacks = null;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			mNameView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1,
						int i2) {
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				}

				@Override
				public void afterTextChanged(Editable editable) {
					mPage.getData().putString(TransactionInfoPage.NAME_DATA_KEY,
							(editable != null) ? editable.toString() : null);
					mPage.notifyDataChanged();
				}
			});

			mValueView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1,
						int i2) {
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				}

				@Override
				public void afterTextChanged(Editable editable) {
					mPage.getData().putString(TransactionInfoPage.VALUE_DATA_KEY,
							(editable != null) ? editable.toString() : null);
					mPage.notifyDataChanged();
				}
			});

			mTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					Object item = parent.getItemAtPosition(pos);
					mPage.getData().putString(TransactionInfoPage.TYPE_DATA_KEY,item.toString());
					mPage.notifyDataChanged();
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

			mCategoryView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					Cursor cursor = (Cursor) adapterCategory.getItem(pos);
					String category = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME));

					mPage.getData().putString(TransactionInfoPage.CATEGORY_DATA_KEY,category);			    	
					mPage.notifyDataChanged();
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

		}

		@Override
		public void setMenuVisibility(boolean menuVisible) {
			super.setMenuVisibility(menuVisible);

			// In a future update to the support library, this should override setUserVisibleHint
			// instead of setMenuVisibility.
			if (mNameView != null) {
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				if (!menuVisible) {
					imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
				}
			}
		}
	}

	public static class TransactionOptionalFragment extends SherlockFragment {
		private static final String ARG_KEY = "transaction_optional_key";

		private PageFragmentCallbacks mCallbacks;
		private String mKey;
		private static TransactionOptionalPage mPage;
		private EditText mCheckNumView;
		private AutoCompleteTextView mMemoView;
		private CheckBox mClearedView;

		public static TransactionOptionalFragment create(String key) {
			Bundle args = new Bundle();
			args.putString(ARG_KEY, key);

			TransactionOptionalFragment fragment = new TransactionOptionalFragment();
			fragment.setArguments(args);
			return fragment;
		}

		public TransactionOptionalFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			Bundle args = getArguments();
			mKey = args.getString(ARG_KEY);
			mPage = (TransactionOptionalPage) mCallbacks.onGetPage(mKey);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			final Bundle data = mPage.getData();

			View rootView = inflater.inflate(R.layout.transaction_page_optional, container, false);
			((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

			mCheckNumView = ((EditText) rootView.findViewById(R.id.transaction_checknum));
			mCheckNumView.setText(data.getString(TransactionOptionalPage.CHECKNUM_DATA_KEY));

			mMemoView = ((AutoCompleteTextView) rootView.findViewById(R.id.transaction_memo));
			mMemoView.setText(data.getString(TransactionOptionalPage.MEMO_DATA_KEY));

			//Adapter for memo's autocomplete
			ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, dropdownResults);
			mMemoView.setAdapter(dropdownAdapter);			

			//Add dictionary back to autocomplete
			TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
			mMemoView.setKeyListener(input);

			tTime = (Button)rootView.findViewById(R.id.transaction_time);
			tDate = (Button)rootView.findViewById(R.id.transaction_date);

			if(data.getString(TransactionOptionalPage.DATE_DATA_KEY)!=null && data.getString(TransactionOptionalPage.DATE_DATA_KEY).length()>0){
				final DateTime date = new DateTime();
				date.setStringSQL(data.getString(TransactionOptionalPage.DATE_DATA_KEY));
				tDate.setText(date.getReadableDate());
				mPage.getData().putString(TransactionOptionalPage.DATE_DATA_KEY, date.getReadableDate());
			}
			if(data.getString(TransactionOptionalPage.TIME_DATA_KEY)!=null && data.getString(TransactionOptionalPage.TIME_DATA_KEY).length()>0){
				final DateTime time = new DateTime();
				time.setStringSQL(data.getString(TransactionOptionalPage.TIME_DATA_KEY));
				tTime.setText(time.getReadableTime());
				mPage.getData().putString(TransactionOptionalPage.TIME_DATA_KEY, time.getReadableTime());
			}
			else if(data.getString(TransactionOptionalPage.DATE_DATA_KEY)==null && data.getString(TransactionOptionalPage.TIME_DATA_KEY)==null){
				final Calendar c = Calendar.getInstance();
				final DateTime date = new DateTime();
				date.setCalendar(c);				

				tDate.setText(date.getReadableDate());
				tTime.setText(date.getReadableTime());
				mPage.getData().putString(TransactionOptionalPage.DATE_DATA_KEY, date.getReadableDate());
				mPage.getData().putString(TransactionOptionalPage.TIME_DATA_KEY, date.getReadableTime());
			}

			mClearedView = (CheckBox) rootView.findViewById(R.id.transaction_cleared);
			if(mPage.getData().getString(TransactionOptionalPage.CLEARED_DATA_KEY)!=null){				
				mClearedView.setChecked(Boolean.parseBoolean(mPage.getData().getString(TransactionOptionalPage.CLEARED_DATA_KEY)));				
			}
			else{
				mClearedView.setChecked(true);				
				mPage.getData().putString(TransactionOptionalPage.CLEARED_DATA_KEY, "true");
			}

			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);

			if (!(activity instanceof PageFragmentCallbacks)) {
				mCallbacks = (PageFragmentCallbacks) getParentFragment();
			}
			else{
				mCallbacks = (PageFragmentCallbacks) activity;
			}
		}

		@Override
		public void onDetach() {
			super.onDetach();
			mCallbacks = null;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			mCheckNumView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1,
						int i2) {
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				}

				@Override
				public void afterTextChanged(Editable editable) {
					mPage.getData().putString(TransactionOptionalPage.CHECKNUM_DATA_KEY,
							(editable != null) ? editable.toString() : null);
					mPage.notifyDataChanged();
				}
			});

			mMemoView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1,
						int i2) {
				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				}

				@Override
				public void afterTextChanged(Editable editable) {
					mPage.getData().putString(TransactionOptionalPage.MEMO_DATA_KEY,
							(editable != null) ? editable.toString() : null);
					mPage.notifyDataChanged();
				}
			});

			mClearedView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (mClearedView.isChecked()) {
						mPage.getData().putString(TransactionOptionalPage.CLEARED_DATA_KEY, "true");
					}
					else
					{
						mPage.getData().putString(TransactionOptionalPage.CLEARED_DATA_KEY, "false");
					}

					mPage.notifyDataChanged();
				}
			});

		}

		@Override
		public void setMenuVisibility(boolean menuVisible) {
			super.setMenuVisibility(menuVisible);

			// In a future update to the support library, this should override setUserVisibleHint
			// instead of setMenuVisibility.
			if (mCheckNumView != null) {
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				if (!menuVisible) {
					imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
				}
			}
		}
	}

	public static class TransactionInfoPage extends Page {
		public static final String ID_DATA_KEY = "id";
		public static final String ACCOUNT_ID_DATA_KEY = "acct_id";
		public static final String PLAN_ID_DATA_KEY = "plan_id";

		public static final String NAME_DATA_KEY = "name";
		public static final String VALUE_DATA_KEY = "value";
		public static final String TYPE_DATA_KEY = "type";
		public static final String CATEGORY_DATA_KEY = "category";

		public TransactionInfoPage(ModelCallbacks callbacks, String title) {
			super(callbacks, title);
		}

		@Override
		public Fragment createFragment() {
			return TransactionInfoFragment.create(getKey());
		}

		@Override
		public void getReviewItems(ArrayList<ReviewItem> dest) {        
			dest.add(new ReviewItem("Name", mData.getString(NAME_DATA_KEY), getKey(), -1));
			dest.add(new ReviewItem("Value", mData.getString(VALUE_DATA_KEY), getKey(), -1));
			dest.add(new ReviewItem("Type", mData.getString(TYPE_DATA_KEY), getKey(), -1));
			dest.add(new ReviewItem("Category", mData.getString(CATEGORY_DATA_KEY), getKey(), -1));
		}

		@Override
		public boolean isCompleted() {
			return !TextUtils.isEmpty(mData.getString(NAME_DATA_KEY)) && !TextUtils.isEmpty(mData.getString(VALUE_DATA_KEY));
		}   
	}

	public static class TransactionOptionalPage extends Page {
		public static final String CHECKNUM_DATA_KEY = "checknum";
		public static final String MEMO_DATA_KEY = "memo";
		public static final String DATE_DATA_KEY = "date";
		public static final String TIME_DATA_KEY = "time";
		public static final String CLEARED_DATA_KEY = "cleared";

		public TransactionOptionalPage(ModelCallbacks callbacks, String title) {
			super(callbacks, title);
		}

		@Override
		public Fragment createFragment() {
			return TransactionOptionalFragment.create(getKey());
		}

		@Override
		public void getReviewItems(ArrayList<ReviewItem> dest) {
			dest.add(new ReviewItem("Check Number", mData.getString(CHECKNUM_DATA_KEY), getKey(), -1));
			dest.add(new ReviewItem("Memo", mData.getString(MEMO_DATA_KEY), getKey(), -1));
			dest.add(new ReviewItem("Date", mData.getString(DATE_DATA_KEY), getKey(), -1));
			dest.add(new ReviewItem("Time", mData.getString(TIME_DATA_KEY), getKey(), -1));
			dest.add(new ReviewItem("Cleared", mData.getString(CLEARED_DATA_KEY), getKey(), -1));
		}
	}

}//end Transactions