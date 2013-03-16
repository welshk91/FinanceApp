package com.databases.example;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.databases.example.Accounts.AccountRecord;
import com.databases.example.Accounts.AddDialogFragment;
import com.databases.example.Accounts.EditDialogFragment;
import com.databases.example.Accounts.UserItemAdapter;
import com.databases.example.Accounts.ViewDialogFragment;
import com.slidingmenu.lib.SlidingMenu;

public class Transactions extends SherlockFragment implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor>{

	//Used to determine if fragment should show all transactions
	boolean showAllTransactions=false;

	//Dialog for Adding Transaction
	static View promptsView;
	View myFragmentView;

	private static final int REG_LOADER = 2;

	private static DatabaseHelper dh = null;

	static Spinner tCategory;
	static Button tTime;
	static Button tDate;

	//Date Format to use for time (01:42 PM)
	final static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

	//Date Format to use for date (03-26-2013)
	final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");		

	//Variables of the Account Used
	static int account_id;

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=5;
	int CONTEXT_MENU_EDIT=6;
	int CONTEXT_MENU_DELETE=7;
	int CONTEXT_MENU_ATTACH=8;

	//ListView and Adapter
	ListView lv = null;
	static UserItemAdapter adapter = null;

	//For Autocomplete
	static ArrayList<String> dropdownResults = new ArrayList<String>();

	//Adapter for category spinner
	static SimpleCursorAdapter categorySpinnerAdapter = null;
	Cursor categoryCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getLoaderManager();

		dh = new DatabaseHelper(getActivity());

		//Arguments
		Bundle bundle=getArguments();

		//bundle is empty if from search, so don't add extra menu options
		if(bundle!=null || savedInstanceState!=null){
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

		//myFragmentView = inflater.inflate(R.layout.transactions, container, false);		
		myFragmentView = inflater.inflate(R.layout.transactions, null, false);				

		lv = (ListView)myFragmentView.findViewById(R.id.transaction_list);

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		//Arguments sent by Account Fragment
		Bundle bundle=getArguments();

		if(bundle!=null && bundle.getBoolean("showAll")){
			showAllTransactions = true;
		}
		else if(bundle!=null && showAllTransactions==false) {
			account_id = bundle.getInt("ID");
			//getActivity().setTitle("Transactions <" + account_name +">");
		}
		//Set Listener for regular mouse click
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				int selectionRowID = (int) adapter.getItemId(position);
				String item = adapter.getTransaction(position).name;

				Toast.makeText(Transactions.this.getActivity(), "Click\nRow: " + selectionRowID + "\nEntry: " + item, Toast.LENGTH_SHORT).show();

			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener


		//Allows Context Menus for each item of the list view
		registerForContextMenu(lv);

		//Set up a listener for changes in settings menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		prefs.registerOnSharedPreferenceChangeListener(this);

		TextView noResult = (TextView)myFragmentView.findViewById(R.id.transaction_noTransaction);
		lv.setEmptyView(noResult);

		adapter = new UserItemAdapter(this.getActivity(), null);
		lv.setAdapter(adapter);

		populate();

		return myFragmentView;
	}

	//Populate view with all the transactions of selected account
	protected void populate(){
		dropdownResults = new ArrayList<String>();

		//Arguments for fragment
		Bundle bundle=getArguments();
		boolean searchFragment=true;

		if(bundle!=null){
			searchFragment = bundle.getBoolean("boolSearch");
		}

		if(showAllTransactions){
			Bundle b = new Bundle();
			b.putBoolean("boolShowAll", true);
			getLoaderManager().restartLoader(REG_LOADER, bundle, this);
		}
		else if(searchFragment){
			//Word being searched
			String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);			

			try{
				Bundle b = new Bundle();
				b.putBoolean("boolSearch", true);
				b.putString("query", query);
				getLoaderManager().restartLoader(REG_LOADER, b, this);
			}
			catch(Exception e){
				Log.e("Transactions-populate", "Search Failed. Error e=" + e);
				Toast.makeText(this.getActivity(), "Search Failed\n"+e, Toast.LENGTH_SHORT).show();
				return;
			}

		}
		else{
			Bundle b = new Bundle();
			b.putInt("aID", account_id);
			getLoaderManager().restartLoader(REG_LOADER, bundle, this);
		}

	}//end populate

	//Creates menu for long presses
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
		String name = adapter.getTransaction(itemInfo.position).name;

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
		int id = adapter.getTransaction(itemInfo.position).id;

		DialogFragment newFragment = ViewDialogFragment.newInstance(id);
		newFragment.show(getChildFragmentManager(), "dialogView");

	}  

	//For Adding a Transaction
	public void transactionAdd(){
		DialogFragment newFragment = AddDialogFragment.newInstance();
		newFragment.show(getChildFragmentManager(), "dialogAdd");
	}//end of transactionAdd

	//For Editing an Transaction
	public void transactionEdit(android.view.MenuItem item){
		final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		final TransactionRecord record = adapter.getTransaction(itemInfo.position);

		DialogFragment newFragment = EditDialogFragment.newInstance(record);
		newFragment.show(getChildFragmentManager(), "dialogEdit");	
	}

	//For Deleting an Transaction
	public void transactionDelete(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		TransactionRecord record = adapter.getTransaction(itemInfo.position);

		Uri uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + record.id);
		getActivity().getContentResolver().delete(uri, "TransID="+record.id, null);

		//((Transactions) getParentFragment()).calculateBalance();
		calculateBalance();
		Toast.makeText(this.getActivity(), "Deleted Item:\n" + record.name, Toast.LENGTH_SHORT).show();
	}//end of transactionDelete

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

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Toast.makeText(this, "Options Just Changed: Transactions.Java", Toast.LENGTH_SHORT).show();
		populate();
	}

	//Calculates the balance
	public void calculateBalance(){
		float totalBalance = 0;

		Cursor cursor = getActivity().getContentResolver().query(MyContentProvider.TRANSACTIONS_URI, null, "ToAcctID="+account_id, null, null);
					
		cursor.moveToFirst();
		if (cursor != null) {
			if (cursor.isFirst()) {
				do {
					String value = cursor.getString(cursor.getColumnIndex("TransValue"));
					String type = cursor.getString(cursor.getColumnIndex("TransType"));

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
						Log.e("Transactions-calculateBalance", "Could not calculate total balance. Error e=" + e);
					}

				} while (cursor.moveToNext());
			}

			else {
				Log.e("Transactions-calculateBalance", "No results found/Cursor empty");
			}
		}

		ContentValues values = new ContentValues();
		values.put("AcctBalance", totalBalance);		
		getActivity().getContentResolver().update(Uri.parse(MyContentProvider.TRANSACTIONS_URI+"/"+account_id), values,"AcctID ="+account_id, null);
		
		TextView balance = (TextView)this.myFragmentView.findViewById(R.id.transaction_total_balance);
		balance.setText("Total Balance: " + totalBalance);
	}

	//Method Called to refresh the list of categories if user changes the list
	public void categoryPopulate(){
		categoryCursor = dh.getSubCategoriesAll();

		getActivity().startManagingCursor(categoryCursor);
		String[] from = new String[] {"SubCatName"}; 
		int[] to = new int[] { android.R.id.text1 };

		categorySpinnerAdapter = new SimpleCursorAdapter(this.getActivity(), android.R.layout.simple_spinner_item, categoryCursor, from, to);
		categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tCategory.setAdapter(categorySpinnerAdapter);

	}//end of categoryPopulate

	//Close dialogs to prevent window leaks
	@Override
	public void onPause() {
		if(dh!=null){
			dh.close();
		}

		if(categoryCursor!=null){
			categoryCursor.close();
		}

		super.onPause();
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

			String transactionTime = hourOfDay + ":" + minute + " " + ampm;
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
			String transactionDate = null;
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

	public class UserItemAdapter extends CursorAdapter {
		private Context context;

		public UserItemAdapter(Context context, Cursor transactions) {
			super(context, transactions);
		}

		public TransactionRecord getTransaction(long position){
			Cursor group = getCursor();

			group.moveToPosition((int) position);
			int idColumn = group.getColumnIndex("TransID");
			int acctIDColumn = group.getColumnIndex("ToAcctID");
			int planIDColumn = group.getColumnIndex("ToPlanID");
			int nameColumn = group.getColumnIndex("TransName");
			int valueColumn = group.getColumnIndex("TransValue");
			int typeColumn = group.getColumnIndex("TransType");
			int categoryColumn = group.getColumnIndex("TransCategory");
			int checknumColumn = group.getColumnIndex("TransCheckNum");
			int memoColumn = group.getColumnIndex("TransMemo");
			int timeColumn = group.getColumnIndex("TransTime");
			int dateColumn = group.getColumnIndex("TransDate");
			int clearedColumn = group.getColumnIndex("TransCleared");

			//int id = group.getInt(idColumn);
			int id = group.getInt(0);
			int acctId = group.getInt(acctIDColumn);
			int planId = group.getInt(planIDColumn);
			String name = group.getString(nameColumn);
			String value = group.getString(valueColumn);
			String type = group.getString(typeColumn);
			String category = group.getString(categoryColumn);
			String checknum = group.getString(checknumColumn);
			String memo = group.getString(memoColumn);
			String time = group.getString(timeColumn);
			String date = group.getString(dateColumn);
			String cleared = group.getString(clearedColumn);

			TransactionRecord record = new TransactionRecord(id, acctId, planId, name, value,type,category,checknum,memo,time,date,cleared);
			return record;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View v = view;
			Cursor user = getCursor();

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (user != null) {
				TextView TVname = (TextView) v.findViewById(R.id.transaction_name);
				TextView TVvalue = (TextView) v.findViewById(R.id.transaction_value);
				TextView TVtype = (TextView) v.findViewById(R.id.transaction_type);
				TextView TVcategory = (TextView) v.findViewById(R.id.transaction_category);
				TextView TVchecknum = (TextView) v.findViewById(R.id.transaction_checknum);
				TextView TVmemo = (TextView) v.findViewById(R.id.transaction_memo);
				TextView TVdate = (TextView) v.findViewById(R.id.transaction_date);
				TextView TVtime = (TextView) v.findViewById(R.id.transaction_time);
				TextView TVcleared = (TextView) v.findViewById(R.id.transaction_cleared);

				int idColumn = user.getColumnIndex("TransID");
				int acctIDColumn = user.getColumnIndex("ToAcctID");
				int planIDColumn = user.getColumnIndex("ToPlanID");
				int nameColumn = user.getColumnIndex("TransName");
				int valueColumn = user.getColumnIndex("TransValue");
				int typeColumn = user.getColumnIndex("TransType");
				int categoryColumn = user.getColumnIndex("TransCategory");
				int checknumColumn = user.getColumnIndex("TransCheckNum");
				int memoColumn = user.getColumnIndex("TransMemo");
				int timeColumn = user.getColumnIndex("TransTime");
				int dateColumn = user.getColumnIndex("TransDate");
				int clearedColumn = user.getColumnIndex("TransCleared");

				int id = user.getInt(0);
				//int id = user.getInt(idColumn);
				int acctId = user.getInt(acctIDColumn);
				int planId = user.getInt(planIDColumn);
				String name = user.getString(nameColumn);
				String value = user.getString(valueColumn);
				String type = user.getString(typeColumn);
				String category = user.getString(categoryColumn);
				String checknum = user.getString(checknumColumn);
				String memo = user.getString(memoColumn);
				String time = user.getString(timeColumn);
				String date = user.getString(dateColumn);
				String cleared = user.getString(clearedColumn);

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
					TVname.setText(name);

					if(planId!=0){
						TVname.setTextColor(Color.parseColor("#FF9933"));
					}
					else{
						TVname.setTextColor(Color.parseColor("#000000"));
					}

				}

				if(value != null) {
					TVvalue.setText("Value: " + value );
				}

				if(type != null) {
					TVtype.setText("Type: " + type );
				}

				if(category != null) {
					TVcategory.setText("Category: " + category );
				}

				if(checknum != null) {
					TVchecknum.setText("Check Num: " + checknum );
				}

				if(memo != null) {
					TVmemo.setText("Memo: " + memo );
				}

				if(date != null) {
					TVdate.setText("Date: " + date );
				}

				if(time != null) {
					TVtime.setText("Time: " + time );
				}

				if(cleared != null) {
					TVcleared.setText("Cleared: " + cleared );
				}

			}			

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = vi.inflate(R.layout.transaction_item, null);

			LinearLayout l=(LinearLayout)v.findViewById(R.id.transaction_layout);
			TextView TVname = (TextView)v.findViewById(R.id.transaction_name);
			TextView TVvalue = (TextView)v.findViewById(R.id.transaction_value);
			TextView TVtype = (TextView)v.findViewById(R.id.transaction_type);
			TextView TVcategory = (TextView)v.findViewById(R.id.transaction_category);
			TextView TVchecknum = (TextView)v.findViewById(R.id.transaction_checknum);
			TextView TVmemo = (TextView)v.findViewById(R.id.transaction_memo);
			TextView TVtime = (TextView)v.findViewById(R.id.transaction_time);
			TextView TVdate = (TextView)v.findViewById(R.id.transaction_date);
			TextView TVcleared = (TextView)v.findViewById(R.id.transaction_cleared);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Transactions.this.getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			//Change Background Colors
			try{
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

				if(useDefaults){
					TVname.setTextSize(18);
				}
				else{
					TVname.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_transaction_nameColor", Color.parseColor("#000000"));

				if(useDefaults){
					TVname.setTextColor(Color.parseColor("#000000"));
				}
				else{
					TVname.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				String DefaultSize = prefs.getString(Transactions.this.getString(R.string.pref_key_transaction_fieldSize), "10");

				if(useDefaults){
					TVvalue.setTextSize(10);
					TVdate.setTextSize(10);
					TVtime.setTextSize(10);
					TVcategory.setTextSize(10);
					TVmemo.setTextSize(10);
					TVchecknum.setTextSize(10);
					TVcleared.setTextSize(10);
					TVtype.setTextSize(10);
				}
				else{
					TVvalue.setTextSize(Integer.parseInt(DefaultSize));
					TVtype.setTextSize(Integer.parseInt(DefaultSize));
					TVcategory.setTextSize(Integer.parseInt(DefaultSize));
					TVchecknum.setTextSize(Integer.parseInt(DefaultSize));
					TVmemo.setTextSize(Integer.parseInt(DefaultSize));
					TVtime.setTextSize(Integer.parseInt(DefaultSize));
					TVdate.setTextSize(Integer.parseInt(DefaultSize));
					TVcleared.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_transaction_fieldColor", Color.parseColor("#0099CC"));

				if(useDefaults){
					TVvalue.setTextColor(Color.parseColor("#0099CC"));
					TVtype.setTextColor(Color.parseColor("#0099CC"));
					TVcategory.setTextColor(Color.parseColor("#0099CC"));
					TVchecknum.setTextColor(Color.parseColor("#0099CC"));
					TVmemo.setTextColor(Color.parseColor("#0099CC"));
					TVtime.setTextColor(Color.parseColor("#0099CC"));
					TVdate.setTextColor(Color.parseColor("#0099CC"));
					TVcleared.setTextColor(Color.parseColor("#0099CC"));
				}
				else{
					TVvalue.setTextColor(DefaultColor);
					TVtype.setTextColor(DefaultColor);
					TVcategory.setTextColor(DefaultColor);
					TVchecknum.setTextColor(DefaultColor);
					TVmemo.setTextColor(DefaultColor);
					TVtime.setTextColor(DefaultColor);
					TVdate.setTextColor(DefaultColor);
					TVcleared.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Transactions.this.getActivity(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_nameField", true)){
				TVname.setVisibility(View.VISIBLE);
			}
			else{
				TVname.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_valueField", true)){
				TVvalue.setVisibility(View.VISIBLE);
			}
			else{
				TVvalue.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_transaction_typeField", false)){
				TVtype.setVisibility(View.VISIBLE);
			}
			else{
				TVtype.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_categoryField", true)){
				TVcategory.setVisibility(View.VISIBLE);
			}
			else{
				TVcategory.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_checknumField", true)){
				TVchecknum.setVisibility(View.VISIBLE);
			}
			else{
				TVchecknum.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_transaction_memoField", false)){
				TVmemo.setVisibility(View.VISIBLE);
			}
			else{
				TVmemo.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_dateField", true)){
				TVdate.setVisibility(View.VISIBLE);
			}
			else{
				TVdate.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_transaction_timeField", true)){
				TVtime.setVisibility(View.VISIBLE);
			}
			else{
				TVtime.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_transaction_clearedField", false)){
				TVcleared.setVisibility(View.VISIBLE);
			}
			else{
				TVcleared.setVisibility(View.GONE);
			}

			return v;
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
			int id = getArguments().getInt("id");

			Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.TRANSACTIONS_URI+"/"+id), null, null, null, null);

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
				entry_id = c.getInt(c.getColumnIndex("TransID"));
				entry_acctId = c.getInt(c.getColumnIndex("ToAcctID"));
				entry_planId = c.getInt(c.getColumnIndex("ToPlanID"));
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

			// get transaction_stats.xml view
			LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
			View transStatsView = li.inflate(R.layout.transaction_stats, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

			// set xml to AlertDialog builder
			alertDialogBuilder.setView(transStatsView);

			//set Title
			alertDialogBuilder.setTitle("View Transaction");

			// set dialog message
			alertDialogBuilder
			.setCancelable(true);

			//Set Statistics
			TextView statsName = (TextView)transStatsView.findViewById(R.id.TextTransactionName);
			statsName.setText(entry_name);
			TextView statsValue = (TextView)transStatsView.findViewById(R.id.TextTransactionValue);
			statsValue.setText(entry_value);
			TextView statsType = (TextView)transStatsView.findViewById(R.id.TextTransactionType);
			statsType.setText(entry_type);
			TextView statsCategory = (TextView)transStatsView.findViewById(R.id.TextTransactionCategory);
			statsCategory.setText(entry_category);
			TextView statsCheckNum = (TextView)transStatsView.findViewById(R.id.TextTransactionCheck);
			statsCheckNum.setText(entry_checknum);
			TextView statsMemo = (TextView)transStatsView.findViewById(R.id.TextTransactionMemo);
			statsMemo.setText(entry_memo);
			TextView statsDate = (TextView)transStatsView.findViewById(R.id.TextTransactionDate);
			statsDate.setText(entry_date);
			TextView statsTime = (TextView)transStatsView.findViewById(R.id.TextTransactionTime);
			statsTime.setText(entry_time);
			TextView statsCleared = (TextView)transStatsView.findViewById(R.id.TextTransactionCleared);
			statsCleared.setText(entry_cleared);

			c.close();
			return alertDialogBuilder.create();
		}
	}

	public static class EditDialogFragment extends SherlockDialogFragment {

		public static EditDialogFragment newInstance(TransactionRecord record) {
			EditDialogFragment frag = new EditDialogFragment();
			Bundle args = new Bundle();
			args.putInt("id", record.id);
			args.putInt("acct_id", record.acctId);
			args.putInt("plan_id", record.planId);
			args.putString("name", record.name);
			args.putString("value", record.value);
			args.putString("type", record.type);
			args.putString("category", record.category);
			args.putString("checknum", record.checknum);
			args.putString("memo", record.memo);
			args.putString("date", record.date);
			args.putString("time", record.time);
			args.putString("cleared", record.cleared);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int tID = getArguments().getInt("id");
			final int aID = getArguments().getInt("acct_id");
			final int pID = getArguments().getInt("plan_id");
			final String name = getArguments().getString("name");
			final String value = getArguments().getString("value");
			final String type = getArguments().getString("type");
			final String category = getArguments().getString("category");
			final String checknum = getArguments().getString("checknum");
			final String memo = getArguments().getString("memo");
			final String date = getArguments().getString("date");
			final String time = getArguments().getString("time");
			final String cleared = getArguments().getString("cleared");

			// get transaction_add.xml view
			LayoutInflater li = LayoutInflater.from(getActivity());
			promptsView = li.inflate(R.layout.transaction_add, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

			// set account_add.xml to AlertDialog builder
			alertDialogBuilder.setView(promptsView);

			//set Title
			alertDialogBuilder.setTitle("Edit A Transaction");

			//Set fields to old values
			final EditText tName = (EditText) promptsView.findViewById(R.id.EditTransactionName);
			final EditText tValue = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
			final Spinner tType = (Spinner)promptsView.findViewById(R.id.spinner_transaction_type);
			tCategory = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);
			final EditText tCheckNum = (EditText)promptsView.findViewById(R.id.EditTransactionCheck);
			final AutoCompleteTextView tMemo = (AutoCompleteTextView)promptsView.findViewById(R.id.EditTransactionMemo);
			final Button tDate = (Button)promptsView.findViewById(R.id.ButtonTransactionDate);
			final Button tTime = (Button)promptsView.findViewById(R.id.ButtonTransactionTime);
			final CheckBox tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);

			//Set the adapter for memo's autocomplete
			ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, dropdownResults);
			tMemo.setAdapter(dropdownAdapter);

			//Add dictionary back to autocomplete
			TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
			tMemo.setKeyListener(input);

			//Populate Category Spinner			
			((Transactions) getParentFragment()).categoryPopulate();					

			tName.setText(name);
			tValue.setText(value);
			ArrayAdapter<String> myAdap = (ArrayAdapter<String>) tType.getAdapter();
			int spinnerPosition = myAdap.getPosition(type);
			tType.setSelection(spinnerPosition);

			//Used to find correct category to select
			for (int i = 0; i < tCategory.getCount(); i++) {
				Cursor c = (Cursor) tCategory.getItemAtPosition(i);
				String catName = c.getString(c.getColumnIndex("SubCatName"));
				if (catName.contentEquals(category)) {
					tCategory.setSelection(i);
					break;
				}
			}

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

					String transactionName = tName.getText().toString().trim();
					String transactionValue = tValue.getText().toString().trim();
					String transactionType = tType.getSelectedItem().toString().trim();
					String transactionCategory = cursor.getString(cursor.getColumnIndex("CateName"));
					String transactionCheckNum = tCheckNum.getText().toString().trim();
					String transactionMemo = tMemo.getText().toString().trim();
					String transactionCleared = tCleared.isChecked()+"";
					String transactionTime = tTime.getText().toString().trim();
					String transactionDate = tDate.getText().toString().trim();

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

							Uri uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + tID);
							getActivity().getContentResolver().delete(uri, "TransID="+tID, null);

							ContentValues transactionValues=new ContentValues();
							transactionValues.put("TransID", tID);
							transactionValues.put("ToAcctID", aID);
							transactionValues.put("ToPlanID", pID);
							transactionValues.put("TransName", transactionName);
							transactionValues.put("TransValue", transactionValue);
							transactionValues.put("TransType", transactionType);
							transactionValues.put("TransCategory", transactionCategory);
							transactionValues.put("TransCheckNum", transactionCheckNum);
							transactionValues.put("TransMemo", transactionMemo);
							transactionValues.put("TransTime", transactionTime);
							transactionValues.put("TransDate", transactionDate);
							transactionValues.put("TransCleared", transactionCleared);

							//Make new record with same ID
							getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
							
							((Transactions) getParentFragment()).calculateBalance();					
						}

						else{
							Toast.makeText(getActivity(), "Needs a Name", Toast.LENGTH_SHORT).show();
						}

					}
					catch(Exception e){
						Log.e("here...", "Error e="+e);
						Toast.makeText(getActivity(), "Error Editing Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
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

	public static class AddDialogFragment extends SherlockDialogFragment {

		public static AddDialogFragment newInstance() {
			AddDialogFragment frag = new AddDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if(account_id==0){
				Toast.makeText(getActivity(), "Please Select an Account First", Toast.LENGTH_LONG).show();
				return null;
			}

			// get transaction_add.xml view
			LayoutInflater li = LayoutInflater.from(getActivity());
			promptsView = li.inflate(R.layout.transaction_add, null);

			final EditText tName = (EditText) promptsView.findViewById(R.id.EditTransactionName);
			final EditText tValue = (EditText) promptsView.findViewById(R.id.EditTransactionValue);
			final Spinner tType = (Spinner)promptsView.findViewById(R.id.spinner_transaction_type);
			tCategory = (Spinner)promptsView.findViewById(R.id.spinner_transaction_category);
			final EditText tCheckNum = (EditText)promptsView.findViewById(R.id.EditTransactionCheck);
			final AutoCompleteTextView tMemo = (AutoCompleteTextView)promptsView.findViewById(R.id.EditTransactionMemo);
			final CheckBox tCleared = (CheckBox)promptsView.findViewById(R.id.CheckTransactionCleared);
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

			//Populate Category Drop-down List
			//categoryPopulate();
			((Transactions) getParentFragment()).categoryPopulate();					

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

			// set account_add.xml to AlertDialog builder
			alertDialogBuilder.setView(promptsView);

			//set Title
			alertDialogBuilder.setTitle("Add A Transaction");

			// set dialog message
			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("Add",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					//Needed to get category's name from DB-populated spinner
					int categoryPosition = tCategory.getSelectedItemPosition();
					Cursor cursor = (Cursor) categorySpinnerAdapter.getItem(categoryPosition);

					String transactionName = tName.getText().toString().trim();
					String transactionValue = tValue.getText().toString().trim();
					String transactionType = tType.getSelectedItem().toString().trim();

					String transactionCategory = null;

					try{
						transactionCategory = cursor.getString(cursor.getColumnIndex("SubCatName"));
					}
					catch(Exception e){
						Log.e("Transaction-addDialog","No Category? Exception e=" + e);
						dialog.cancel();
						Toast.makeText(getActivity(), "Needs A Category \n\nUse The Side Menu To Create Categories", Toast.LENGTH_LONG).show();
						return;
					}

					String transactionCheckNum = tCheckNum.getText().toString().trim();
					String transactionMemo = tMemo.getText().toString().trim();
					String transactionCleared = tCleared.isChecked()+"";

					//Set Time
					String transactionTime = tTime.getText().toString().trim();
					String transactionDate = tDate.getText().toString().trim();

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

							ContentValues transactionValues=new ContentValues();
							transactionValues.put("ToAcctID", account_id);
							transactionValues.put("ToPlanID", 0);
							transactionValues.put("TransName", transactionName);
							transactionValues.put("TransValue", transactionValue);
							transactionValues.put("TransType", transactionType);
							transactionValues.put("TransCategory", transactionCategory);
							transactionValues.put("TransCheckNum", transactionCheckNum);
							transactionValues.put("TransMemo", transactionMemo);
							transactionValues.put("TransTime", transactionTime);
							transactionValues.put("TransDate", transactionDate);
							transactionValues.put("TransCleared", transactionCleared);

							getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
							
							((Transactions) getParentFragment()).calculateBalance();
						} 

						else {
							Toast.makeText(getActivity(), "Needs a Name", Toast.LENGTH_LONG).show();
						}
					}
					catch(Exception e){
						Log.e("here...", "Error e="+e);
						Toast.makeText(getActivity(), "Error Adding Transaction!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
					}

					cursor.close();

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

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		switch (loaderID) {
		case REG_LOADER:
			if(bundle!=null && bundle.getBoolean("boolSearch")){
				Log.e("Here1", "boolSearch="+bundle.getBoolean("boolSearch"));
				String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
				return new CursorLoader(
						getActivity(),   	// Parent activity context
						(Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/SEARCH/" + query)),// Table to query
						null,     			// Projection to return
						null,            	// No selection clause
						null,            	// No selection arguments
						null             	// Default sort order
						);
			}
			else if(bundle!=null && bundle.getBoolean("boolShowAll")){
				Log.e("Here2", "boolShowAll="+bundle.getBoolean("boolShowAll"));
				return new CursorLoader(
						getActivity(),   	// Parent activity context
						MyContentProvider.TRANSACTIONS_URI,// Table to query
						null,     			// Projection to return
						null,            	// No selection clause
						null,            	// No selection arguments
						null             	// Default sort order
						);
			}
			else{
				Log.e("Here3","accout_id="+account_id);
				//String selection = "stitchlevel='" + args.getString("Level") + "'";
				String[] projection = new String[]{ "TransID as _id", "ToAcctID", "ToPlanID", "TransName", "TransValue", "TransType", "TransCategory","TransCheckNum", "TransMemo", "TransTime", "TransDate", "TransCleared"};
				String selection = "ToAcctID=" + account_id;
				return new CursorLoader(
						getActivity(),   	// Parent activity context
						MyContentProvider.TRANSACTIONS_URI,// Table to query
						projection,     			// Projection to return
						selection,					// No selection clause
						null,						// No selection arguments
						null             			// Default sort order
						);				
			}
		default:
			Log.e("Transactions-onCreateLoader", "Not a valid CursorLoader ID");
			return null;
		}

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);	
	}


}//end Transactions