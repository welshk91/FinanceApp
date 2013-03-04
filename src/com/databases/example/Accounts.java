package com.databases.example;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class Accounts extends SherlockFragment implements OnSharedPreferenceChangeListener {

	final int PICKFILE_RESULT_CODE = 1;

	//Balance
	float totalBalance;

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=1;
	int CONTEXT_MENU_EDIT=2;
	int CONTEXT_MENU_DELETE=3;

	View myFragmentView;

	//Date Format to use for time (01:42 PM)
	final static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

	//Date Format to use for date (03-26-2013)
	final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");		

	ListView lv = null;
	static UserItemAdapter adapter = null;

	final static String tblAccounts = "tblAccounts";
	final static String tblTrans = "tblTrans";
	final static String dbFinance = "dbFinance";
	static SQLiteDatabase myDB;

	//Method called upon first creation
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Arguments
		Bundle bundle=getArguments();

		//bundle is empty if from search, so don't add extra menu options
		if(bundle!=null){
			setHasOptionsMenu(true);
		}

		setRetainInstance(false);

	}// end onCreate

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//myFragmentView = inflater.inflate(R.layout.accounts, container, false);	
		myFragmentView = inflater.inflate(R.layout.accounts, null, false);

		lv = (ListView)myFragmentView.findViewById(R.id.account_list);

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
				String sqlCommand = "SELECT * FROM " + tblAccounts + 
						" WHERE AcctID IN (SELECT AcctID FROM (SELECT AcctID FROM " + tblAccounts + 
						" LIMIT " + (selectionRowID-1) + ",1)AS tmp)";

				myDB = getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

				Cursor c = myDB.rawQuery(sqlCommand, null);

				getActivity().startManagingCursor(c);

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
					//Log.e("Here!!!", "ID: "+entry_id+"\nName: "+entry_name+"\nBalance: "+entry_balance+"\nTime: "+entry_time+"\nDate: "+entry_date);
				}while(c.moveToNext());

				//Close Database if Open
				if (myDB != null){
					myDB.close();
				}

				View checkbook_frame = getActivity().findViewById(R.id.checkbook_frag_frame);

				if(checkbook_frame!=null){

					//Data to send to transaction fragment
					Bundle args = new Bundle();
					args.putInt("ID",entry_id);
					args.putString("name", entry_name);
					args.putString("balance", entry_balance);
					args.putString("time", entry_time);
					args.putString("date", entry_date);

					// Add the fragment to the activity, pushing this transaction
					// on to the back stack.
					Transactions tran_frag = new Transactions();
					tran_frag.setArguments(args);
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.replace(R.id.checkbook_frag_frame, tran_frag);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.addToBackStack(null);
					ft.commit();
					getFragmentManager().executePendingTransactions();
				}
				else{

					//Data to send to transaction fragment
					Bundle args = new Bundle();
					args.putBoolean("showAll", false);
					args.putBoolean("boolSearch", false);

					args.putInt("ID",entry_id);
					args.putString("name", entry_name);
					args.putString("balance", entry_balance);
					args.putString("time", entry_time);
					args.putString("date", entry_date);

					// Add the fragment to the activity, pushing this transaction
					// on to the back stack.
					Transactions tran_frag = new Transactions();
					tran_frag.setArguments(args);
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.replace(R.id.transaction_frag_frame, tran_frag);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.commit();
					getFragmentManager().executePendingTransactions();

				}

			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener


		//Allows Context Menus for each item of the list view
		registerForContextMenu(lv);

		//Set up a listener for changes in settings menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		prefs.registerOnSharedPreferenceChangeListener(this);

		populate();

		return myFragmentView;
	}

	//Method called after creation, populates list with account information
	protected void populate() {
		Log.e("Accounts", "Populate");

		Cursor cursorAccounts = null;

		//A textView alerting the user if database is empty
		TextView noResult = (TextView)myFragmentView.findViewById(R.id.account_noTransaction);
		noResult.setVisibility(View.GONE);

		//Reset Balance
		totalBalance=0;

		//Arguments sent by Account Fragment
		Bundle bundle=getArguments();
		boolean searchFragment=true;

		if(bundle!=null){
			searchFragment = bundle.getBoolean("boolSearch");
		}

		// Cursor is used to navigate the query results
		myDB = this.getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

		//Fragment is a search fragment
		if(searchFragment){

			//Word being searched
			String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);			

			//Command used to search
			String sqlCommand = " SELECT AcctID as _id, * FROM " + tblAccounts + 
					" WHERE AcctName " + 
					" LIKE ?" + 
					" UNION " + 
					" SELECT AcctID as _id, * FROM " + tblAccounts +
					" WHERE AcctBalance " + 
					" LIKE ?" + 
					" UNION " + 
					" SELECT AcctID as _id, AcctName, * FROM " + tblAccounts +
					" WHERE AcctDate " + 
					" LIKE ?" +
					" UNION " +
					" SELECT AcctID as _id, AcctName, * FROM " + tblAccounts +
					" WHERE AcctTime " + 
					" LIKE ?";

			try{
				cursorAccounts = myDB.rawQuery(sqlCommand, new String[] { "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%" });
			}
			catch(Exception e){
				Toast.makeText(this.getActivity(), "Search Failed\n"+e, Toast.LENGTH_LONG).show();
				return;
			}

		}

		//Not A Search Fragment
		else{
			cursorAccounts = myDB.query(tblAccounts, new String[] { "AcctID as _id", "AcctName", "AcctBalance", "AcctTime", "AcctDate" }, null,
					null, null, null, null);			
		}

		//		getActivity().startManagingCursor(c);
		//		int IDColumn = c.getColumnIndex("AcctID");
		//		int NameColumn = c.getColumnIndex("AcctName");
		int BalanceColumn = cursorAccounts.getColumnIndex("AcctBalance");
		//		int TimeColumn = c.getColumnIndex("AcctTime");
		//		int DateColumn = c.getColumnIndex("AcctDate");

		cursorAccounts.moveToFirst();
		if (cursorAccounts != null) {
			if (cursorAccounts.isFirst()) {
				do {
					//					String id = c.getString(IDColumn);
					//					String name = c.getString(NameColumn);
					String balance = cursorAccounts.getString(BalanceColumn);
					//					String time = c.getString(TimeColumn);
					//					String date = c.getString(DateColumn);

					//Add account balance to total balance
					try{
						totalBalance = totalBalance + Float.parseFloat(balance);
					}
					catch(Exception e){
						Toast.makeText(Accounts.this.getActivity(), "Could not calculate total balance", Toast.LENGTH_SHORT).show();
					}

				} while (cursorAccounts.moveToNext());
			}

			else {
				//No Results Found
				noResult.setVisibility(View.VISIBLE);

				//No Search Results
				if(bundle==null){
					noResult.setText("Nothing Found");
				}

			}
		} 

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		adapter = new UserItemAdapter(this.getActivity(), cursorAccounts);
		lv.setAdapter(adapter);

		//Refresh Balance
		calculateBalance();

	}//end populate

	//Creates menu for long presses
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
		String name = "" + adapter.getAccount(itemInfo.position).name;

		menu.setHeaderTitle(name);  
		menu.add(0, CONTEXT_MENU_OPEN, 0, "Open");  
		menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit");
		menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");
	}  

	//Handles which methods are called when using the long presses menu
	@Override  
	public boolean onContextItemSelected(android.view.MenuItem item) {

		if(item.getItemId()==CONTEXT_MENU_OPEN){
			accountOpen(item);
			return true;
		}  
		else if(item.getItemId()==CONTEXT_MENU_EDIT){
			accountEdit(item);
			return true;
		}
		else if(item.getItemId()==CONTEXT_MENU_DELETE){
			accountDelete(item);
			return true;
		}
		else {
			//return false;
			//return super.onContextItemSelected(item);
		}  

		return super.onContextItemSelected(item);  
	}  

	//For Opening an Account
	public void accountOpen(android.view.MenuItem item){  
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		String id = adapter.getAccount(itemInfo.position).id;

		DialogFragment newFragment = ViewDialogFragment.newInstance(id);
		newFragment.show(getChildFragmentManager(), "dialogView");

	}

	//For Editing an Account
	public void accountEdit(android.view.MenuItem item){
		final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		final AccountRecord record = adapter.getAccount(itemInfo.position);

		DialogFragment newFragment = EditDialogFragment.newInstance(record);
		newFragment.show(getChildFragmentManager(), "dialogEdit");
	}

	//For Deleting an Account
	public void accountDelete(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		AccountRecord record = adapter.getAccount(itemInfo.position);

		String sqlDeleteAccount = "DELETE FROM " + tblAccounts + 
				" WHERE AcctID = " + record.id;

		//Deletes all transactions in the account
		String sqlDeleteTransactions = "DELETE FROM " + tblTrans + 
				" WHERE ToAcctID = " + record.id;

		//Open Database
		myDB = this.getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

		myDB.execSQL(sqlDeleteAccount);
		myDB.execSQL(sqlDeleteTransactions);	

		//Close Database if Opened
		if (myDB != null){
			myDB.close();
		}

		//Reload transaction fragment if shown
		View transaction_frame = getActivity().findViewById(R.id.transaction_frag_frame);

		Accounts account_frag = new Accounts();
		Transactions transaction_frag = new Transactions();

		//Bundle for Transaction fragment
		Bundle argsTran = new Bundle();
		argsTran.putBoolean("showAll", true);
		argsTran.putBoolean("boolSearch", false);

		//Bundle for Account fragment
		Bundle argsAccount = new Bundle();
		argsAccount.putBoolean("boolSearch", false);

		transaction_frag.setArguments(argsTran);
		account_frag.setArguments(argsAccount);

		if(transaction_frame!=null){
			getFragmentManager().beginTransaction()
			.replace(R.id.account_frag_frame, account_frag,"account_frag_tag").replace(R.id.transaction_frag_frame, transaction_frag, "transaction_frag_tag").commit();
		}
		else{
			getFragmentManager().beginTransaction().
			replace(R.id.checkbook_frag_frame, account_frag,"account_frag_tag").commit();
		}

		Toast.makeText(this.getActivity(), "Deleted Item:\n" + record.name, Toast.LENGTH_SHORT).show();

	}//end of accountDelete

	//For Adding an Account
	public void accountAdd(){
		DialogFragment newFragment = AddDialogFragment.newInstance();
		newFragment.show(getChildFragmentManager(), "dialogAdd");

		//		//Reload transaction fragment if shown
		//		View transaction_frame = getActivity().findViewById(R.id.transaction_frag_frame);
		//		int account_frame = R.id.account_frag_frame;
		//		View checkbook_frame = getActivity().findViewById(R.id.checkbook_frag_frame);
		//		
		//		Log.e("HERE", "Transaction_frame=" + transaction_frame);
		//		Log.e("HERE", "Account_frame=" + account_frame);
		//		Log.e("HERE", "Checkbook_frame=" + checkbook_frame);
		//		
		//		Accounts account_frag = new Accounts();
		//		Transactions transaction_frag = new Transactions();
		//
		//		//Bundle for Transaction fragment
		//		Bundle argsTran = new Bundle();
		//		argsTran.putInt("ID", 0);
		//
		//		//Bundle for Account fragment
		//		Bundle argsAccount = new Bundle();
		//		argsAccount.putBoolean("boolSearch", false);
		//
		//		transaction_frag.setArguments(argsTran);
		//		account_frag.setArguments(argsAccount);
		//		
		//		if(transaction_frame!=null){
		//			getFragmentManager().beginTransaction()
		//			.replace(account_frame, account_frag,"account_frag_tag").replace(R.id.transaction_frag_frame, transaction_frag, "transaction_frag_tag").commit();
		//			//getFragmentManager().executePendingTransactions();
		//		}
		//		else{
		//			getFragmentManager().beginTransaction().
		//			replace(R.id.checkbook_frag_frame, account_frag,"account_frag_tag").commit();
		//			//getFragmentManager().executePendingTransactions();
		//		}

	}	

	//Handle closing database properly to avoid corruption
	@Override
	public void onDestroy() {
		if (myDB != null){
			myDB.close();
		}
		super.onDestroy();
	}

	//For Menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		View transaction_frame = getActivity().findViewById(R.id.transaction_frag_frame);

		//Clear any leftover junk
		menu.clear();

		if(transaction_frame!=null){

			//Show Search
			MenuItem menuSearch = menu.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_search, com.actionbarsherlock.view.Menu.NONE, "Search");
			menuSearch.setIcon(android.R.drawable.ic_menu_search);
			menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			SubMenu subMenu1 = menu.addSubMenu("Account");
			subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_add, com.actionbarsherlock.view.Menu.NONE, "Add");
			subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_transfer, com.actionbarsherlock.view.Menu.NONE, "Transfer");
			subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_unknown, com.actionbarsherlock.view.Menu.NONE, "Unknown");

			MenuItem subMenu1Item = subMenu1.getItem();
			subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		else{
			inflater.inflate(R.layout.account_menu, menu);
		}

	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			//Intent intentUp = new Intent(Accounts.this.getActivity(), Main.class);
			//intentUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//startActivity(intentUp);
			//menu.toggle();
			break;

		case R.id.account_menu_add:    
			accountAdd();
			return true;

		case R.id.account_menu_search:    
			getActivity().onSearchRequested();
			return true;

		case R.id.account_menu_transfer:    
			//accountTransfer();
			return true;

		case R.id.account_menu_unknown:    
			//Insert Unknown Code Here
			pickFile(null);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Toast.makeText(this, "Options Just Changed: Accounts.Java", Toast.LENGTH_SHORT).show();
		populate();
	}

	//Calculates the balance
	public void calculateBalance(){
		TextView balance = (TextView)this.myFragmentView.findViewById(R.id.account_total_balance);
		balance.setText("Total Balance: " + totalBalance);
	}

	//Override default resume to also call populate in case view needs refreshing
	@Override
	public void onResume(){
		//populate();
		super.onResume();
	}

	//Method used to handle picking a file
	void pickFile(File aFile) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		startActivityForResult(intent,PICKFILE_RESULT_CODE);
	}

	//Method called after picking a file
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
		case PICKFILE_RESULT_CODE:
			if(resultCode==getActivity().RESULT_OK){
				String FilePath = data.getData().getPath();
				Toast.makeText(this.getActivity(), "File Path : " + FilePath, Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	//Close dialogs to prevent window leaks
	@Override
	public void onPause() {
		//		if(alertDialogView!=null){
		//			alertDialogView.dismiss();
		//		}
		//		if(alertDialogEdit!=null){
		//			alertDialogEdit.dismiss();
		//		}
		//		if(alertDialogAdd!=null){
		//			alertDialogAdd.dismiss();
		//		}

		//populate();

		super.onPause();
	}

	public class UserItemAdapter extends CursorAdapter {
		private Cursor accounts;
		private Context context;

		public UserItemAdapter(Context context, Cursor accounts) {
			super(context, accounts);
			this.context = context;
			this.accounts = accounts;
		}

		public AccountRecord getAccount(long position){
			Cursor group = accounts;

			group.moveToPosition((int) position);
			int IDColumn = group.getColumnIndex("AcctID");
			int NameColumn = group.getColumnIndex("AcctName");
			int BalanceColumn = group.getColumnIndex("AcctBalance");
			int TimeColumn = group.getColumnIndex("AcctTime");
			int DateColumn = group.getColumnIndex("AcctDate");

			String id = group.getString(0);
			//String id = group.getString(IDColumn);
			String name = group.getString(NameColumn);
			String balance = group.getString(BalanceColumn);
			String time = group.getString(TimeColumn);
			String date = group.getString(DateColumn);

			AccountRecord record = new AccountRecord(id, name, balance, time, date);
			return record;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View v = view;
			Cursor user = accounts;

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (user != null) {
				TextView TVname = (TextView) v.findViewById(R.id.account_name);
				TextView TVbalance = (TextView) v.findViewById(R.id.account_balance);
				TextView TVdate = (TextView) v.findViewById(R.id.account_date);
				TextView TVtime = (TextView) v.findViewById(R.id.account_time);

				int IDColumn = user.getColumnIndex("AcctID");
				int NameColumn = user.getColumnIndex("AcctName");
				int BalanceColumn = user.getColumnIndex("AcctBalance");
				int TimeColumn = user.getColumnIndex("AcctTime");
				int DateColumn = user.getColumnIndex("AcctDate");

				String id = user.getString(0);
				//String id = user.getString(IDColumn);
				String name = user.getString(NameColumn);
				String balance = user.getString(BalanceColumn);
				String time = user.getString(TimeColumn);
				String date = user.getString(DateColumn);

				//Change gradient
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.account_gradient);
					GradientDrawable defaultGradientPos = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFF00FF33,0xFF000000});

					GradientDrawable defaultGradientNeg = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFFFF0000,0xFF000000});

					if(useDefaults){
						if(Float.parseFloat(balance) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(Float.parseFloat(balance) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}
					}

				}
				catch(Exception e){
					Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
				}


				if (name != null) {
					TVname.setText(name);
				}

				if(balance != null) {
					TVbalance.setText("Balance: " + balance );
				}

				if(date != null) {
					TVdate.setText("Date: " + date );
				}

				if(time != null) {
					TVtime.setText("Time: " + time );
				}

			}

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.account_item, parent, false);

			TextView TVname = (TextView)v.findViewById(R.id.account_name);
			TextView TVbalance = (TextView)v.findViewById(R.id.account_balance);
			TextView TVtime = (TextView)v.findViewById(R.id.account_time);
			TextView TVdate = (TextView)v.findViewById(R.id.account_date);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Accounts.this.getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			//Change Background Colors
			try{
				LinearLayout l;
				l=(LinearLayout)v.findViewById(R.id.account_layout);
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
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
			}

			//Change Size of main field
			try{
				String DefaultSize = prefs.getString(Accounts.this.getString(R.string.pref_key_account_nameSize), "16");

				if(useDefaults){
					TVname.setTextSize(16);
				}
				else{
					TVname.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_account_nameColor", Color.parseColor("#000000"));

				if(useDefaults){
					TVname.setTextColor(Color.parseColor("#000000"));
				}
				else{
					TVname.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				String DefaultSize = prefs.getString(Accounts.this.getString(R.string.pref_key_account_fieldSize), "10");

				if(useDefaults){
					TVbalance.setTextSize(10);
					TVdate.setTextSize(10);
					TVtime.setTextSize(10);
				}
				else{
					TVbalance.setTextSize(Integer.parseInt(DefaultSize));
					TVdate.setTextSize(Integer.parseInt(DefaultSize));
					TVtime.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_account_fieldColor", Color.parseColor("#0099CC"));

				if(useDefaults){
					TVbalance.setTextColor(Color.parseColor("#0099CC"));
					TVdate.setTextColor(Color.parseColor("#0099CC"));
					TVtime.setTextColor(Color.parseColor("#0099CC"));
				}
				else{
					TVbalance.setTextColor(DefaultColor);
					TVdate.setTextColor(DefaultColor);
					TVtime.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
			}


			//For User-Defined Field Visibility
			if(useDefaults||prefs.getBoolean("checkbox_account_nameField", true)){
				TVname.setVisibility(View.VISIBLE);
			}
			else{
				TVname.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_account_balanceField", true)){
				TVbalance.setVisibility(View.VISIBLE);
			}
			else{
				TVbalance.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_account_dateField", true)){
				TVdate.setVisibility(View.VISIBLE);
			}
			else{
				TVdate.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_account_timeField", true)){
				TVtime.setVisibility(View.VISIBLE);
			}
			else{
				TVtime.setVisibility(View.GONE);
			}

			return v;

		}
	}

	//An Object Class used to hold the data of each account record
	
	public class AccountRecord {
		protected String id;
		protected String name;
		protected String balance;
		protected String date;
		protected String time;

		public AccountRecord(String id, String name, String balance, String date, String time) {
			this.id = id;
			this.name = name;
			this.balance = balance;
			this.date = date;
			this.time = time;
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

			String sqlCommand = "SELECT * FROM " + tblAccounts + 
					" WHERE AcctID = " + ID;

			myDB = getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

			Cursor c = myDB.rawQuery(sqlCommand, null);
			getActivity().startManagingCursor(c);

			int entry_id = 0;
			String entry_name = null;
			String entry_balance = null;
			String entry_time = null;
			String entry_date = null;

			c.moveToFirst();
			do{
				entry_id = c.getInt(c.getColumnIndex("AcctID"));
				entry_name = c.getString(c.getColumnIndex("AcctName"));
				entry_balance = c.getString(c.getColumnIndex("AcctBalance"));
				entry_time = c.getString(c.getColumnIndex("AcctTime"));
				entry_date = c.getString(c.getColumnIndex("AcctDate"));
			}while(c.moveToNext());

			//Close Database if Open
			if (myDB != null){
				myDB.close();
			}

			LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
			View accountStatsView = li.inflate(R.layout.account_stats, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this.getSherlockActivity());

			// set xml to AlertDialog builder
			alertDialogBuilder.setView(accountStatsView);

			//set Title
			alertDialogBuilder.setTitle("View Account");

			// set dialog message
			alertDialogBuilder
			.setCancelable(true);

			//Set Statistics
			TextView statsName = (TextView)accountStatsView.findViewById(R.id.TextAccountName);
			statsName.setText(entry_name);
			TextView statsValue = (TextView)accountStatsView.findViewById(R.id.TextAccountValue);
			statsValue.setText(entry_balance);
			TextView statsDate = (TextView)accountStatsView.findViewById(R.id.TextAccountDate);
			statsDate.setText(entry_date);
			TextView statsTime = (TextView)accountStatsView.findViewById(R.id.TextAccountTime);
			statsTime.setText(entry_time);

			return alertDialogBuilder.create();

		}
	}

	//Class that handles edit fragment
	
	public static class EditDialogFragment extends SherlockDialogFragment {

		public static EditDialogFragment newInstance(AccountRecord record) {
			EditDialogFragment frag = new EditDialogFragment();
			Bundle args = new Bundle();
			args.putString("id", record.id);
			args.putString("name", record.name);
			args.putString("balance", record.balance);
			args.putString("date", record.date);
			args.putString("time", record.time);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final String ID = getArguments().getString("id");
			final String name = getArguments().getString("name");
			final String balance = getArguments().getString("balance");

			LayoutInflater li = LayoutInflater.from(getActivity());
			final View promptsView = li.inflate(R.layout.account_add, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

			// set account_add.xml to AlertDialog builder
			alertDialogBuilder.setView(promptsView);

			//set Title
			alertDialogBuilder.setTitle("Edit An Account");

			//Add the previous info into the fields, remove unnecessary fields
			final EditText aName = (EditText) promptsView.findViewById(R.id.EditAccountName);
			final EditText aBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
			TextView aBalanceText = (TextView)promptsView.findViewById(R.id.BalanceTexts);
			aName.setText(name);
			aBalance.setVisibility(View.GONE);
			aBalanceText.setVisibility(View.GONE);

			// set dialog message
			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("Save",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// CODE FOR "OK"
					String accountName = null;
					String accountTime = null;
					String accountBalance = null;
					String accountDate = null;

					accountName = aName.getText().toString().trim();
					accountBalance = balance.trim();
					final Calendar c = Calendar.getInstance();
					accountTime = timeFormat.format(c.getTime());
					accountDate = dateFormat.format(c.getTime());

					try{
						String deleteCommand = "DELETE FROM " + tblAccounts + " WHERE AcctID = " + ID + ";";

						//Open Database
						myDB = getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

						//Delete Old Record
						myDB.execSQL(deleteCommand);

						//Make new record with same ID
						ContentValues accountValues=new ContentValues();
						accountValues.put("AcctID",ID);
						accountValues.put("AcctName",accountName);
						accountValues.put("AcctBalance",accountBalance);
						accountValues.put("AcctTime",accountTime);
						accountValues.put("AcctDate",accountDate);

						myDB.insert(tblAccounts, null, accountValues);

						//Close Database if Opened
						if (myDB != null){
							myDB.close();
						}

					}
					catch(Exception e){
						Toast.makeText(getActivity(), "Error Editing Account!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
					}

					//Update Accounts ListView
					((Accounts) getParentFragment()).populate();

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

	//Class that handles add fragment

	public static class AddDialogFragment extends SherlockDialogFragment {

		public static AddDialogFragment newInstance() {
			AddDialogFragment frag = new AddDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(getActivity());
			final View promptsView = li.inflate(R.layout.account_add, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

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
					String accountName = null;
					String accountTime = null;
					String accountBalance = null;
					String accountDate = null;

					//Variables for adding the account
					EditText aName = (EditText) promptsView.findViewById(R.id.EditAccountName);
					EditText aBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
					accountName = aName.getText().toString().trim();
					accountBalance = aBalance.getText().toString().trim();

					final Calendar cal = Calendar.getInstance();
					accountTime = timeFormat.format(cal.getTime());
					accountDate = dateFormat.format(cal.getTime());

					//Variables for adding Starting Balance transaction
					final String transactionName = "STARTING BALANCE";
					float transactionValue;
					final String transactionCategory = "STARTING BALANCE";
					final String transactionCheckNum = "None";
					final String transactionMemo = "This is an automatically generated transaction created when you add an account";
					final String transactionTime = accountTime;
					final String transactionDate = accountDate;
					final String transactionCleared = "true";
					String transactionType = "Unknown";

					//Check Value to see if it's valid
					try{
						transactionValue = Float.parseFloat(accountBalance);
					}
					catch(Exception e){
						transactionValue = (float) 0.00;
						accountBalance = "0";
					}				

					try{
						if(Float.parseFloat(accountBalance)>=0){
							transactionType = "Deposit";
						}
						else{
							transactionType = "Withdrawl";
							transactionValue = transactionValue * -1;
						}
					}
					catch(Exception e){
						Toast.makeText(getActivity(), "Error\nWas balance a valid format?", Toast.LENGTH_SHORT).show();
					}

					//String sqlQuery = "SELECT AcctID FROM " + tblAccounts + " WHERE AcctName='" + accountName + "' AND AcctBalance=" + accountBalance + " AND AcctTime='" + accountTime + "' AND AcctDate='" + accountDate + "';";
					String sqlQuery = "SELECT AcctID FROM " + tblAccounts + " WHERE AcctName = ? AND AcctBalance = ? AND AcctTime = ? AND AcctDate = ?;";

					int entry_id = 0;

					//Open Database
					myDB = getActivity().openOrCreateDatabase(dbFinance, getActivity().MODE_PRIVATE, null);

					try{
						if (accountName.length()>0) {

							//Insert values into accounts table
							ContentValues accountValues=new ContentValues();
							accountValues.put("AcctName",accountName);
							accountValues.put("AcctBalance",accountBalance);
							accountValues.put("AcctTime",accountTime);
							accountValues.put("AcctDate",accountDate);

							myDB.insert(tblAccounts, null, accountValues);

							//Query the Newly created account
							Cursor c = myDB.rawQuery(sqlQuery, new String[] {accountName, accountBalance, accountTime, accountDate});
							getActivity().startManagingCursor(c);

							c.moveToFirst();
							do{
								entry_id = c.getInt(0);
							}while(c.moveToNext());

							//Insert values into accounts table
							ContentValues transactionValues=new ContentValues();
							transactionValues.put("ToAcctID",entry_id);
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

							//Close Cursor object
							c.close();
						} 

						else {
							Toast.makeText(getActivity(), "Needs a Name", Toast.LENGTH_SHORT).show();
						}

					}
					catch(Exception e){
						Toast.makeText(getActivity(), "Error Adding Account!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
					}

					//Close Database if Opened
					if (myDB != null){
						myDB.close();
					}

					//Update Accounts ListView
					((Accounts) getParentFragment()).populate();					

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

}// end Accounts