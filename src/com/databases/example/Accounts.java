/* Class that handles the Account Fragment seen in the Checkbook screen
 * Does everything from setting up the view to Add/Delete/Edit Accounts to calculating the balance
 */

package com.databases.example;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.SearchView;

public class Accounts extends SherlockFragment implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {
	private static final int PICKFILE_RESULT_CODE = 1;
	private static final int ACCOUNTS_LOADER = 123456789;
	private static final int ACCOUNTS_SEARCH_LOADER = 12345;

	//Constants for ContextMenu
	final private int CONTEXT_MENU_OPEN=1;
	final private int CONTEXT_MENU_EDIT=2;
	final private int CONTEXT_MENU_DELETE=3;

	//Spinners for transfers
	private Cursor accountCursor = null;
	private static Spinner transferSpinnerTo;
	private static Spinner transferSpinnerFrom;
	private static SimpleCursorAdapter transferSpinnerAdapterFrom = null;
	private static SimpleCursorAdapter transferSpinnerAdapterTo = null;

	private View myFragmentView;
	private static String sortOrder= "null";

	private ListView lv = null;
	private static UserItemAdapter adapterAccounts = null;

	protected Object mActionMode = null;
	private SparseBooleanArray mSelectedItemsIds;	

	//Method called upon first creation
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}// end onCreate

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		myFragmentView = inflater.inflate(R.layout.accounts, null, false);
		lv = (ListView)myFragmentView.findViewById(R.id.account_list);

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
					int selectionRowID = (int) adapterAccounts.getItemId(position);
					Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+(selectionRowID)), null, null, null, null);

					//Just get the Account ID
					c.moveToFirst();
					int	entry_id = c.getInt(0);
					c.close();

					View checkbook_frame = getActivity().findViewById(R.id.checkbook_frag_frame);

					if(checkbook_frame!=null){
						Bundle args = new Bundle();
						args.putInt("ID",entry_id);

						//Add the fragment to the activity, pushing this transaction on to the back stack.
						Transactions tran_frag = new Transactions();
						tran_frag.setArguments(args);
						FragmentTransaction ft = getFragmentManager().beginTransaction();
						ft.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left,android.R.anim.slide_in_left,android.R.anim.slide_out_right);
						ft.replace(R.id.checkbook_frag_frame, tran_frag);
						ft.addToBackStack(null);
						ft.commit();
						getFragmentManager().executePendingTransactions();
					}
					else{
						Bundle args = new Bundle();
						args.putBoolean("showAll", false);
						args.putBoolean("boolSearch", false);
						args.putInt("ID",entry_id);

						//Add the fragment to the activity
						//NOTE: Don't add custom animation, seems to mess with onLoaderReset
						Transactions tran_frag = new Transactions();
						tran_frag.setArguments(args);
						FragmentTransaction ft = getFragmentManager().beginTransaction();
						//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
						ft.replace(R.id.transaction_frag_frame, tran_frag);
						ft.commit();
						getFragmentManager().executePendingTransactions();
					}
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

		adapterAccounts = new UserItemAdapter(this.getActivity(), null);
		lv.setAdapter(adapterAccounts);

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
		adapterAccounts.toggleSelection(position);
		boolean hasCheckedItems = adapterAccounts.getSelectedCount() > 0;

		if (hasCheckedItems && mActionMode == null){
			// there are some selected items, start the actionMode
			mActionMode = getSherlockActivity().startActionMode(new MyActionMode());
		}
		else if (!hasCheckedItems && mActionMode != null){
			// there no selected items, finish the actionMode
			((ActionMode) mActionMode).finish();
		}

		if(mActionMode != null){
			((ActionMode) mActionMode).invalidate();
			((ActionMode)mActionMode).setTitle(String.valueOf(adapterAccounts.getSelectedCount()) + " selected");
		}
	}

	//Populate view with accounts
	protected void populate(){
		Bundle bundle=getArguments();
		boolean searchFragment=true;

		if(bundle!=null){
			searchFragment = bundle.getBoolean("boolSearch");
		}

		//Fragment is a search fragment
		if(searchFragment){

			//Word being searched
			String query = getActivity().getIntent().getStringExtra("query");			

			try{
				Bundle b = new Bundle();
				b.putBoolean("boolSearch", true);
				b.putString("query", query);
				Log.v("Accounts-populate","start search loader...");
				getLoaderManager().initLoader(ACCOUNTS_SEARCH_LOADER, b, this);
			}
			catch(Exception e){
				Log.e("Accounts-populate","Search Failed. Error e="+e);
				Toast.makeText(this.getActivity(), "Search Failed\n"+e, Toast.LENGTH_LONG).show();
			}

		}

		//Not A Search Fragment
		else{
			Log.v("Accounts-populate","start loader...");
			getLoaderManager().initLoader(ACCOUNTS_LOADER, bundle, this);
		}

	}

	//For Attaching to an Account
	public void accountAttach(android.view.MenuItem item){
		final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		final AccountRecord record = adapterAccounts.getAccount(itemInfo.position);

		Intent intentLink = new Intent(this.getActivity(), Links.class);
		intentLink.putExtra(DatabaseHelper.ACCOUNT_ID, record.id);
		intentLink.putExtra(DatabaseHelper.ACCOUNT_NAME, record.name);
		startActivityForResult(intentLink, PICKFILE_RESULT_CODE);
	}

	//For Adding an Account
	public void accountAdd(){
		DialogFragment newFragment = AddDialogFragment.newInstance();
		newFragment.show(getChildFragmentManager(), "dialogAdd");
	}	

	//For Transferring from an Account
	public void accountTransfer(){
		DialogFragment newFragment = TransferDialogFragment.newInstance();
		newFragment.show(getChildFragmentManager(), "dialogTransfer");		
	}

	//For Sorting Accounts
	public void accountSort(){
		DialogFragment newFragment = SortDialogFragment.newInstance();
		newFragment.show(getChildFragmentManager(), "dialogSort");		
	}

	//Method to get the list of accounts for transfer spinner
	public void accountPopulate(){
		String[] from = new String[] {DatabaseHelper.ACCOUNT_ID, "_id"}; 
		int[] to = new int[] { android.R.id.text1};

		transferSpinnerAdapterFrom = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, accountCursor, from, to);
		transferSpinnerAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		transferSpinnerAdapterTo = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, accountCursor, from, to);
		transferSpinnerAdapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		transferSpinnerTo.setAdapter(transferSpinnerAdapterTo);
		transferSpinnerFrom.setAdapter(transferSpinnerAdapterFrom);

	}//end of accountPopulate

	//For Menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		View transaction_frame = getActivity().findViewById(R.id.transaction_frag_frame);

		//Clear any leftover junk
		menu.clear();

		//If you're in dual-pane mode
		if(transaction_frame!=null){
			MenuItem menuSearch = menu.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_search, com.actionbarsherlock.view.Menu.NONE, "Search");
			menuSearch.setIcon(android.R.drawable.ic_menu_search);
			menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			menuSearch.setActionView(new SearchView(getSherlockActivity().getSupportActionBar().getThemedContext()));

			SearchWidget searchWidget = new SearchWidget(getActivity(),menuSearch.getActionView());

			SubMenu subMenu1 = menu.addSubMenu("Account");
			subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_add, com.actionbarsherlock.view.Menu.NONE, "Add");
			subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_transfer, com.actionbarsherlock.view.Menu.NONE, "Transfer");
			subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_sort, com.actionbarsherlock.view.Menu.NONE, "Sort");
			subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_unknown, com.actionbarsherlock.view.Menu.NONE, "Unknown");

			MenuItem subMenu1Item = subMenu1.getItem();
			subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		else{
			inflater.inflate(R.layout.account_menu, menu);
			SearchWidget searchWidget = new SearchWidget(getActivity(),menu.findItem(R.id.account_menu_search).getActionView());			
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

		case R.id.account_menu_transfer:    
			accountTransfer();
			return true;

		case R.id.account_menu_sort:    
			accountSort();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if(!isDetached()){
			Log.d("Accounts-onSharedPreferenceChanged", "Options changed. Requery");
			//getActivity().getContentResolver().notifyChange(MyContentProvider.ACCOUNTS_URI, null);
			//getLoaderManager().restartLoader(ACCOUNTS_LOADER, null, this);
		}
	}

	public class UserItemAdapter extends CursorAdapter {
		private Context context;

		public UserItemAdapter(Context context, Cursor accounts) {
			super(context, accounts);
			this.context = context;
			mSelectedItemsIds = new SparseBooleanArray();
		}

		public AccountRecord getAccount(long position){
			final Cursor group = getCursor();

			group.moveToPosition((int) position);
			final int NameColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_NAME);
			final int BalanceColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
			final int TimeColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_TIME);
			final int DateColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_DATE);

			final String id = group.getString(0);
			final String name = group.getString(NameColumn);
			final String balance = group.getString(BalanceColumn);
			final String time = group.getString(TimeColumn);
			final String date = group.getString(DateColumn);

			final AccountRecord record = new AccountRecord(id, name, balance, time, date);
			return record;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View v = view;
			Cursor user = getCursor();

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_account", true);

			if (user != null) {
				TextView tvName = (TextView) v.findViewById(R.id.account_name);
				TextView tvBalance = (TextView) v.findViewById(R.id.account_balance);
				TextView tvDate = (TextView) v.findViewById(R.id.account_date);
				TextView tvTime = (TextView) v.findViewById(R.id.account_time);

				int NameColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_NAME);
				int BalanceColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
				int TimeColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_TIME);
				int DateColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_DATE);

				String id = user.getString(0);
				String name = user.getString(NameColumn);
				Money balance = new Money(user.getString(BalanceColumn));
				String time = user.getString(TimeColumn);
				String date = user.getString(DateColumn);
				Locale locale=getResources().getConfiguration().locale;

				//Change gradient
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.account_gradient);
					//Older color to black gradient (0xFF00FF33,0xFF000000)
					GradientDrawable defaultGradientPos = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFF4ac925,0xFF4ac925});
					GradientDrawable defaultGradientNeg = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {0xFFe00707,0xFFe00707});

					if(useDefaults){
						if(balance.isPositive(locale)){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(balance.isPositive(locale)){
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
					tvName.setText(name);
				}

				if(balance != null) {
					tvBalance.setText("Balance: " + balance.getNumberFormat(locale));
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

				v.setBackgroundColor(mSelectedItemsIds.get(user.getPosition())? 0x9934B5E4: Color.TRANSPARENT);
			}

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.account_item, parent, false);

			TextView tvName = (TextView)v.findViewById(R.id.account_name);
			TextView tvBalance = (TextView)v.findViewById(R.id.account_balance);
			TextView tvTime = (TextView)v.findViewById(R.id.account_time);
			TextView tvDate = (TextView)v.findViewById(R.id.account_date);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Accounts.this.getActivity());
			boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_account", true);

			//Change Background Colors
			try{
				if(!useDefaults){
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.account_layout);
					int startColor = prefs.getInt("key_account_startBackgroundColor", Color.parseColor("#FFFFFF"));
					int endColor = prefs.getInt("key_account_endBackgroundColor", Color.parseColor("#FFFFFF"));
					GradientDrawable defaultGradient = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {startColor,endColor});
					l.setBackgroundDrawable(defaultGradient);
				}
			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
			}

			//Change Size of main field
			try{
				String DefaultSize = prefs.getString(Accounts.this.getString(R.string.pref_key_account_nameSize), "24");

				if(useDefaults){
					tvName.setTextSize(24);
				}
				else{
					tvName.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_account_nameColor", Color.parseColor("#222222"));

				if(useDefaults){
					tvName.setTextColor(Color.parseColor("#222222"));
				}
				else{
					tvName.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				String DefaultSize = prefs.getString(Accounts.this.getString(R.string.pref_key_account_fieldSize), "14");

				if(useDefaults){
					tvBalance.setTextSize(14);
					tvDate.setTextSize(14);
					tvTime.setTextSize(14);
				}
				else{
					tvBalance.setTextSize(Integer.parseInt(DefaultSize));
					tvDate.setTextSize(Integer.parseInt(DefaultSize));
					tvTime.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_account_fieldColor", Color.parseColor("#000000"));

				if(useDefaults){
					tvBalance.setTextColor(Color.parseColor("#000000"));
					tvDate.setTextColor(Color.parseColor("#000000"));
					tvTime.setTextColor(Color.parseColor("#000000"));
				}
				else{
					tvBalance.setTextColor(DefaultColor);
					tvDate.setTextColor(DefaultColor);
					tvTime.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(Accounts.this.getActivity(), "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
			}

			//For User-Defined Field Visibility
			if(useDefaults||prefs.getBoolean("checkbox_account_nameField", true)){
				tvName.setVisibility(View.VISIBLE);
			}
			else{
				tvName.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_account_balanceField", true)){
				tvBalance.setVisibility(View.VISIBLE);
			}
			else{
				tvBalance.setVisibility(View.GONE);
			}

			if(useDefaults||prefs.getBoolean("checkbox_account_dateField", true)){
				tvDate.setVisibility(View.VISIBLE);
			}
			else{
				tvDate.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_account_timeField", false) && !useDefaults){
				tvTime.setVisibility(View.VISIBLE);
			}
			else{
				tvTime.setVisibility(View.GONE);
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
			final Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+(ID)), null, null, null, null);

			int entry_id = 0;
			String entry_name = null;
			String entry_balance = null;
			String entry_time = null;
			String entry_date = null;

			c.moveToFirst();
			do{
				entry_id = c.getInt(c.getColumnIndex(DatabaseHelper.ACCOUNT_ID));
				entry_name = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));
				entry_balance = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE));
				entry_time = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_TIME));
				entry_date = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_DATE));
			}while(c.moveToNext());

			LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
			View accountStatsView = li.inflate(R.layout.account_stats, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this.getSherlockActivity());

			alertDialogBuilder.setView(accountStatsView);
			alertDialogBuilder.setTitle("View Account");
			alertDialogBuilder.setCancelable(true);

			//Set Statistics
			TextView statsName = (TextView)accountStatsView.findViewById(R.id.TextAccountName);
			statsName.setText(entry_name);
			TextView statsValue = (TextView)accountStatsView.findViewById(R.id.TextAccountValue);
			statsValue.setText(entry_balance);
			DateTime d = new DateTime();
			d.setStringSQL(entry_date);
			TextView statsDate = (TextView)accountStatsView.findViewById(R.id.TextAccountDate);
			statsDate.setText(d.getReadableDate());
			DateTime t = new DateTime();
			t.setStringSQL(entry_time);
			TextView statsTime = (TextView)accountStatsView.findViewById(R.id.TextAccountTime);
			statsTime.setText(t.getReadableTime());

			c.close();
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
			alertDialogBuilder.setView(promptsView);
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
					String accountName = null;
					String accountBalance = null;
					final Calendar c = Calendar.getInstance();
					Locale locale=getResources().getConfiguration().locale;
					DateTime accountDate = new DateTime();
					accountDate.setDate(c.getTime());

					accountName = aName.getText().toString().trim();
					accountBalance = balance.trim();

					try{
						ContentValues accountValues=new ContentValues();
						accountValues.put(DatabaseHelper.ACCOUNT_ID,ID);
						accountValues.put(DatabaseHelper.ACCOUNT_NAME,accountName);
						accountValues.put(DatabaseHelper.ACCOUNT_BALANCE,accountBalance);
						accountValues.put(DatabaseHelper.ACCOUNT_TIME,accountDate.getSQLTime(locale));
						accountValues.put(DatabaseHelper.ACCOUNT_DATE,accountDate.getSQLDate(locale));

						//Update plan
						getSherlockActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+ID), accountValues, DatabaseHelper.ACCOUNT_ID+"="+ID, null);
					}
					catch(Exception e){
						Toast.makeText(getActivity(), "Error Editing Account!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
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
			alertDialogBuilder.setView(promptsView);
			alertDialogBuilder.setTitle("Add An Account");

			//Set dialog message
			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("Save",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					final Calendar cal = Calendar.getInstance();

					String accountName = null;
					Locale locale=getResources().getConfiguration().locale;
					DateTime accountDate = new DateTime();
					accountDate.setDate(cal.getTime());

					//Variables for adding the account
					EditText aName = (EditText) promptsView.findViewById(R.id.EditAccountName);
					EditText aBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
					accountName = aName.getText().toString().trim();
					Money accountBalance = new Money(aBalance.getText().toString().trim());

					//Variables for adding Starting Balance transaction
					final String transactionName = "STARTING BALANCE";
					final String transactionPlanId = "0";
					Money transactionValue=null;
					final String transactionCategory = "STARTING BALANCE";
					final String transactionCheckNum = "None";
					final String transactionMemo = "This is an automatically generated transaction created when you add an account";
					final String transactionTime = accountDate.getSQLTime(locale);
					final String transactionDate = accountDate.getSQLDate(locale);
					final String transactionCleared = "true";
					String transactionType = "Unknown";

					//Check Value to see if it's valid
					try{
						transactionValue = new Money(Float.parseFloat(accountBalance.getBigDecimal(locale)+""));
					}
					catch(Exception e){
						transactionValue = new Money("0.00");
						accountBalance = new Money("0.00");
					}				

					try{
						if(accountBalance.isPositive(locale)){
							transactionType = "Deposit";
						}
						else{
							transactionType = "Withdraw";
							transactionValue = new Money (transactionValue.getBigDecimal(locale).multiply(new BigDecimal(-1)));
						}
					}
					catch(Exception e){
						Toast.makeText(getActivity(), "Error\nWas balance a valid format?", Toast.LENGTH_SHORT).show();
					}

					try{
						if (accountName.length()>0) {

							ContentValues accountValues=new ContentValues();
							accountValues.put(DatabaseHelper.ACCOUNT_NAME,accountName);
							accountValues.put(DatabaseHelper.ACCOUNT_BALANCE,accountBalance.getBigDecimal(locale)+"");
							accountValues.put(DatabaseHelper.ACCOUNT_TIME,accountDate.getSQLTime(locale));
							accountValues.put(DatabaseHelper.ACCOUNT_DATE,accountDate.getSQLDate(locale));

							//Insert values into accounts table
							Uri u = getActivity().getContentResolver().insert(MyContentProvider.ACCOUNTS_URI, accountValues);

							ContentValues transactionValues=new ContentValues();
							transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(u.getLastPathSegment()));
							transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, transactionPlanId);
							transactionValues.put(DatabaseHelper.TRANS_NAME, transactionName);
							transactionValues.put(DatabaseHelper.TRANS_VALUE, transactionValue.getBigDecimal(locale)+"");
							transactionValues.put(DatabaseHelper.TRANS_TYPE, transactionType);
							transactionValues.put(DatabaseHelper.TRANS_CATEGORY, transactionCategory);
							transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, transactionCheckNum);
							transactionValues.put(DatabaseHelper.TRANS_MEMO, transactionMemo);
							transactionValues.put(DatabaseHelper.TRANS_TIME, transactionTime);
							transactionValues.put(DatabaseHelper.TRANS_DATE, transactionDate);
							transactionValues.put(DatabaseHelper.TRANS_CLEARED, transactionCleared);

							//Insert values into accounts table
							getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
						} 

						else {
							Toast.makeText(getActivity(), "Needs a Name", Toast.LENGTH_SHORT).show();
						}

					}
					catch(Exception e){
						Log.e("Accounts-AddDialog", "Exception e="+e);
						Toast.makeText(getActivity(), "Error Adding Account!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
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

	//Class that handles transfers fragment
	public static class TransferDialogFragment extends SherlockDialogFragment {

		public static TransferDialogFragment newInstance() {
			TransferDialogFragment frag = new TransferDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final LayoutInflater li = LayoutInflater.from(getActivity());
			final View promptsView = li.inflate(R.layout.account_transfer, null);
			final EditText tAmount = (EditText) promptsView.findViewById(R.id.EditAccountAmount);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setView(promptsView);
			alertDialogBuilder.setTitle("Transfer Money");

			transferSpinnerFrom = (Spinner)promptsView.findViewById(R.id.SpinnerAccountFrom);
			transferSpinnerTo = (Spinner)promptsView.findViewById(R.id.SpinnerAccountTo);

			//Populate Account Drop-down List
			((Accounts) getParentFragment()).accountPopulate();

			//Set dialog message
			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("Transfer",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {

					//Needed to get account's name from DB-populated spinner
					int accountPosition1 = transferSpinnerFrom.getSelectedItemPosition();
					Cursor cursorAccount1 = (Cursor) transferSpinnerAdapterFrom.getItem(accountPosition1);				

					int accountPosition2 = transferSpinnerTo.getSelectedItemPosition();
					Cursor cursorAccount2 = (Cursor) transferSpinnerAdapterTo.getItem(accountPosition2);

					String transferAmount = tAmount.getText().toString().trim();
					String transferFrom = null;
					String transferTo = null;
					String transferToID = null;
					String transferFromID = null;

					try{
						transferFrom = cursorAccount1.getString(cursorAccount1.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));
						transferFromID = cursorAccount1.getString(cursorAccount1.getColumnIndex("_id"));
						transferTo = cursorAccount2.getString(cursorAccount2.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));
						transferToID = cursorAccount2.getString(cursorAccount2.getColumnIndex("_id"));
					}
					catch(Exception e){
						Log.e("Account-transferDialog","No Accounts? Exception e=" + e);
						dialog.cancel();
						Toast.makeText(getActivity(), "No Accounts \n\nUse The ActionBar To Create Accounts", Toast.LENGTH_LONG).show();
						return;
					}					

					Log.d("Account-Transfer", "From:"+transferFrom + " To:"+transferTo + " Amount:" + transferAmount);

					//Transfer From
					final Calendar cal = Calendar.getInstance();
					Locale locale=getResources().getConfiguration().locale;
					DateTime transferDate = new DateTime();
					transferDate.setDate(cal.getTime());

					float tAmount;
					final String transferName = "TRANSFER";
					final String transferPlanId = "0";
					final String transferCategory = "TRANSFER";
					final String transferCheckNum = "None";
					final String transferMemo = "This is an automatically generated transaction created when you transfer money";
					final String transferCleared = "true";
					String transferType = "Withdraw";

					//Check Value to see if it's valid
					try{
						tAmount = Float.parseFloat(transferAmount);
					}
					catch(Exception e){
						Log.e("Accounts-transfer", "Invalid amount? Error e="+e);
						return;
					}				

					ContentValues transferValues=new ContentValues();
					
					try{
						transferValues.put(DatabaseHelper.TRANS_ACCT_ID, transferFromID);
						transferValues.put(DatabaseHelper.TRANS_PLAN_ID, transferPlanId);
						transferValues.put(DatabaseHelper.TRANS_NAME, transferName);
						transferValues.put(DatabaseHelper.TRANS_VALUE, tAmount);
						transferValues.put(DatabaseHelper.TRANS_TYPE, transferType);
						transferValues.put(DatabaseHelper.TRANS_CATEGORY, transferCategory);
						transferValues.put(DatabaseHelper.TRANS_CHECKNUM, transferCheckNum);
						transferValues.put(DatabaseHelper.TRANS_MEMO, transferMemo);
						transferValues.put(DatabaseHelper.TRANS_TIME, transferDate.getSQLTime(locale));
						transferValues.put(DatabaseHelper.TRANS_DATE, transferDate.getSQLDate(locale));
						transferValues.put(DatabaseHelper.TRANS_CLEARED, transferCleared);

						//Insert values into transaction table
						getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transferValues);

						//Update Account Info
						ContentValues accountValues=new ContentValues();

						Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+transferFromID), null, null, null, null);

						int entry_id = 0;
						String entry_name = null;
						String entry_balance = null;
						String entry_time = null;
						String entry_date = null;

						c.moveToFirst();
						do{
							entry_id = c.getInt(c.getColumnIndex(DatabaseHelper.ACCOUNT_ID));
							entry_name = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));
							entry_balance = Float.parseFloat(c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE)))-tAmount+"";
							entry_time = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_TIME));
							entry_date = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_DATE));
						}while(c.moveToNext());

						accountValues.put(DatabaseHelper.ACCOUNT_ID,entry_id);
						accountValues.put(DatabaseHelper.ACCOUNT_NAME,entry_name);
						accountValues.put(DatabaseHelper.ACCOUNT_BALANCE,entry_balance);
						accountValues.put(DatabaseHelper.ACCOUNT_TIME,entry_time);
						accountValues.put(DatabaseHelper.ACCOUNT_DATE,entry_date);

						getActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+transferFromID), accountValues,DatabaseHelper.ACCOUNT_ID+"="+transferFromID, null);
						c.close();

					} catch(Exception e){
						Log.e("Accounts-transferDialog", "Transfer From failed. Exception e="+e);
						Toast.makeText(getActivity(), "Error Transferring!\n Did you enter valid input? ", Toast.LENGTH_SHORT).show();
						return;
					}

					//Transfer To
					transferType = "Deposit";

					try{
						transferValues.clear();
						transferValues.put(DatabaseHelper.TRANS_ACCT_ID, transferToID);
						transferValues.put(DatabaseHelper.TRANS_PLAN_ID, transferPlanId);
						transferValues.put(DatabaseHelper.TRANS_NAME, transferName);
						transferValues.put(DatabaseHelper.TRANS_VALUE, tAmount);
						transferValues.put(DatabaseHelper.TRANS_TYPE, transferType);
						transferValues.put(DatabaseHelper.TRANS_CATEGORY, transferCategory);
						transferValues.put(DatabaseHelper.TRANS_CHECKNUM, transferCheckNum);
						transferValues.put(DatabaseHelper.TRANS_MEMO, transferMemo);
						transferValues.put(DatabaseHelper.TRANS_TIME, transferDate.getSQLTime(locale));
						transferValues.put(DatabaseHelper.TRANS_DATE, transferDate.getSQLDate(locale));
						transferValues.put(DatabaseHelper.TRANS_CLEARED, transferCleared);

						//Insert values into transaction table
						getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transferValues);

						//Update Account Info
						ContentValues accountValues=new ContentValues();

						Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+transferToID), null, null, null, null);

						int entry_id = 0;
						String entry_name = null;
						String entry_balance = null;
						String entry_time = null;
						String entry_date = null;

						c.moveToFirst();
						do{
							entry_id = c.getInt(c.getColumnIndex(DatabaseHelper.ACCOUNT_ID));
							entry_name = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));
							entry_balance = Float.parseFloat(c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE)))+tAmount+"";
							entry_time = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_TIME));
							entry_date = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_DATE));
						}while(c.moveToNext());

						accountValues.put(DatabaseHelper.ACCOUNT_ID,entry_id);
						accountValues.put(DatabaseHelper.ACCOUNT_NAME,entry_name);
						accountValues.put(DatabaseHelper.ACCOUNT_BALANCE,entry_balance);
						accountValues.put(DatabaseHelper.ACCOUNT_TIME,entry_time);
						accountValues.put(DatabaseHelper.ACCOUNT_DATE,entry_date);

						getActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+transferToID), accountValues,DatabaseHelper.ACCOUNT_ID+"="+transferToID, null);
						c.close();

					} catch(Exception e){
						Log.e("Accounts-transferDialog", "Transfer To failed. Exception e="+e);
						Toast.makeText(getActivity(), "Error Transferring!\n Did you enter valid input? ", Toast.LENGTH_SHORT).show();
						return;
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
			View accountSortView = li.inflate(R.layout.sort_accounts, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

			alertDialogBuilder.setView(accountSortView);
			alertDialogBuilder.setTitle("Sort");
			alertDialogBuilder.setCancelable(true);

			ListView sortOptions = (ListView)accountSortView.findViewById(R.id.sort_options);
			sortOptions.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					switch (position) {
					//Newest
					case 0:
						sortOrder = DatabaseHelper.ACCOUNT_DATE + " DESC, " + DatabaseHelper.ACCOUNT_TIME + " DESC";
						break;

						//Oldest
					case 1:
						//TODO Fix date so it can be sorted
						sortOrder = DatabaseHelper.ACCOUNT_DATE + " ASC, " + DatabaseHelper.ACCOUNT_TIME + " ASC";
						break;

						//Largest
					case 2:
						sortOrder = "CAST ("+DatabaseHelper.ACCOUNT_BALANCE+" AS INTEGER)" + " DESC";
						break;

						//Smallest	
					case 3:
						sortOrder = "CAST ("+DatabaseHelper.ACCOUNT_BALANCE+" AS INTEGER)" + " ASC";
						break;

						//Alphabetical	
					case 4:
						sortOrder = DatabaseHelper.ACCOUNT_NAME + " ASC";
						break;

						//None	
					case 5:
						sortOrder = null;
						break;

					default:
						Log.e("Accounts-SortFragment","Unknown Sorting Option!");
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

		Log.d("Accounts-onCreateLoader", "calling create loader...");
		switch (loaderID) {
		case ACCOUNTS_LOADER:
			Log.v("Accounts-onCreateLoader","new loader created");
			return new CursorLoader(
					getActivity(),   	// Parent activity context
					MyContentProvider.ACCOUNTS_URI,// Table to query
					null,     			// Projection to return
					null,            	// No selection clause
					null,            	// No selection arguments
					sortOrder           // Default sort order-> "CAST (AcctBalance AS INTEGER)" + " DESC"
					);
		case ACCOUNTS_SEARCH_LOADER:
			String query = getActivity().getIntent().getStringExtra("query");
			Log.v("Accounts-onCreateLoader","new loader (boolSearch "+ query + ") created");
			return new CursorLoader(
					getActivity(),   	// Parent activity context
					(Uri.parse(MyContentProvider.ACCOUNTS_URI + "/SEARCH/" + query)),// Table to query
					null,     			// Projection to return
					null,            	// No selection clause
					null,            	// No selection arguments
					sortOrder           // Default sort order
					);

		default:
			Log.e("Accounts-onCreateLoader", "Not a valid CursorLoader ID");
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		TextView footerTV = (TextView)this.myFragmentView.findViewById(R.id.account_footer);

		switch(loader.getId()){
		case ACCOUNTS_LOADER:
			adapterAccounts.swapCursor(data);
			Log.v("Accounts-onLoadFinished", "loader finished. loader="+loader.getId() + " data="+data + " data size="+data.getCount());

			int balanceColumn = data.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
			BigDecimal totalBalance = BigDecimal.ZERO;
			Locale locale=getResources().getConfiguration().locale;

			data.moveToPosition(-1);
			while(data.moveToNext()){
				totalBalance = totalBalance.add(new Money(data.getString(balanceColumn)).getBigDecimal(locale));
			}

			try{
				TextView noResult = (TextView)myFragmentView.findViewById(R.id.account_noTransaction);
				noResult.setText("No Accounts\n\n To Add An Account, Please Use The ActionBar On The Top");
				lv.setEmptyView(noResult);

				footerTV.setText("Total Balance: " + new Money(totalBalance).getNumberFormat(locale));
			}
			catch(Exception e){
				Log.e("Accounts-onLoadFinished", "Error setting balance TextView. e="+e);
			}

			break;

		case ACCOUNTS_SEARCH_LOADER:
			adapterAccounts.swapCursor(data);
			Log.v("Accounts-onLoadFinished", "loader finished. loader="+loader.getId() + " data="+data + " data size="+data.getCount());

			try{
				TextView noResult = (TextView)myFragmentView.findViewById(R.id.account_noTransaction);
				noResult.setText("No Accounts Found");
				lv.setEmptyView(noResult);

				footerTV.setText("Search Results");
			}
			catch(Exception e){
				Log.e("Accounts-onLoadFinished", "Error setting search TextView. e="+e);
			}

			break;

		default:
			Log.e("Accounts-onLoadFinished", "Error. Unknown loader ("+loader.getId());
			break;
		}

		if(!getSherlockActivity().getSupportLoaderManager().hasRunningLoaders()){
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);			
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch(loader.getId()){
		case ACCOUNTS_LOADER:
			adapterAccounts.swapCursor(null);
			Log.v("Accounts-onLoaderReset", "loader reset. loader="+loader.getId());
			break;

		case ACCOUNTS_SEARCH_LOADER:
			adapterAccounts.swapCursor(null);
			Log.v("Accounts-onLoaderReset", "loader reset. loader="+loader.getId());
			break;

		default:
			Log.e("Accounts-onLoadFinished", "Error. Unknown loader ("+loader.getId());
			break;
		}
	}

	private final class MyActionMode implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add(0, CONTEXT_MENU_OPEN, 0, "Open");  
			menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit");
			menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.clear();
			if (adapterAccounts.getSelectedCount() == 1 && mode != null) {
				menu.add(0, CONTEXT_MENU_OPEN, 0, "Open");  
				menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit");
				menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");				
				return true;
			} else if (adapterAccounts.getSelectedCount() > 1) {
				menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");
				return true;
			}

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			SparseBooleanArray selected = adapterAccounts.getSelectedIds();

			switch (item.getItemId()) {
			case CONTEXT_MENU_OPEN:
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						//accountOpen(adapterAccounts.getAccount(selected.keyAt(i)).id);
						DialogFragment newFragment = ViewDialogFragment.newInstance(adapterAccounts.getAccount(selected.keyAt(i)).id);
						newFragment.show(getChildFragmentManager(), "dialogView");
					}
				}

				mode.finish();
				return true;
			case CONTEXT_MENU_EDIT:
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						//accountEdit(adapterAccounts.getAccount(selected.keyAt(i)));
						DialogFragment newFragment = EditDialogFragment.newInstance(adapterAccounts.getAccount(selected.keyAt(i)));
						newFragment.show(getChildFragmentManager(), "dialogEdit");
					}
				}

				mode.finish();
				return true;
			case CONTEXT_MENU_DELETE:
				AccountRecord record;
				for (int i = 0; i < selected.size(); i++){				
					if (selected.valueAt(i)) {
						record = adapterAccounts.getAccount(selected.keyAt(i));

						//Delete Account
						Uri uri = Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + record.id);
						getActivity().getContentResolver().delete(uri,DatabaseHelper.ACCOUNT_ID+"="+record.id, null);

						//Delete All Transactions of that account
						uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + 0);
						getActivity().getContentResolver().delete(uri,DatabaseHelper.TRANS_ACCT_ID+"="+record.id, null);

						Toast.makeText(getActivity(), "Deleted Account:\n" + record.name, Toast.LENGTH_SHORT).show();
					}
				}

				mode.finish();
				return true;

			default:
				mode.finish();
				Log.e("Accounts-onActionItemClciked","ERROR. Clicked " + item);
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode=null;
			adapterAccounts.removeSelection();
		}
	}

	@Override
	public void onDestroyView() {
		if(mActionMode!=null){
			((ActionMode)mActionMode).finish();		
		}
				
		super.onDestroyView();
	}

}//End Accounts