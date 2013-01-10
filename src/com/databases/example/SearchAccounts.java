package com.databases.example;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchAccounts extends Fragment {

	//View
	View myFragmentView;

	//ListView
	ListView lv = null;
	ArrayAdapter<AccountRecord> adapter = null;
	ArrayList<AccountRecord> results = new ArrayList<AccountRecord>();

	//Database
	final String tblAccounts = "tblAccounts";
	final String tblTrans = "tblTrans";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;
	
	//Statistics
	int totalRecords;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myFragmentView = inflater.inflate(R.layout.search_account, container, false);
		String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
		//Toast.makeText(getActivity(), "I'm in accounts\nSearching for " + query, Toast.LENGTH_SHORT).show();

		//Set up ListView
		lv = (ListView)myFragmentView.findViewById(R.id.search_account_list);

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
						" LIMIT " + (selectionRowID-0) + ",1)AS tmp)";

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
					//Toast.makeText(Accounts.this, "ID: "+entry_id+"\nName: "+entry_name+"\nBalance: "+entry_balance+"\nTime: "+entry_time+"\nDate: "+entry_date, Toast.LENGTH_SHORT).show();
				}while(c.moveToNext());

				//Close Database if Open
				if (myDB != null){
					myDB.close();
				}

				//Call an Intent to go to Transactions Class
				Intent i = new Intent(SearchAccounts.this.getActivity(), Transactions.class);
				i.putExtra("ID", entry_id);
				i.putExtra("name", entry_name);
				i.putExtra("balance", entry_balance);
				i.putExtra("time", entry_time);
				i.putExtra("date", entry_date);
				startActivity(i);

			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener

		//Set up an adapter for the listView
		adapter = new UserItemAdapter(this.getActivity(), android.R.layout.simple_list_item_1, results);
		lv.setAdapter(adapter);

		populate(query);

		return myFragmentView;
	}

	//Method called upon first creation
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		//populate();

	}// end onCreate
	
	//Calculates the total number of search records found that are accounts
	public void calculateRecords(){
		TextView records = (TextView)myFragmentView.findViewById(R.id.search_account_totals);
		records.setText("Account Records Found: " + totalRecords);
	}

	//Method for getting the data from database and populating listview
	public void populate(String query){
		results = new ArrayList<AccountRecord>();

		//Reset Statistics
		totalRecords = 0;
		
		String sqlCommand = " SELECT * FROM " + tblAccounts + 
				" WHERE AcctName " + 
				" LIKE ?" + 
				" UNION " + 
				" SELECT * FROM " + tblAccounts +
				" WHERE AcctBalance " + 
				" LIKE ?" + 
				" UNION " + 
				" SELECT * FROM " + tblAccounts +
				" WHERE AcctDate " + 
				" LIKE ?" +
				" UNION " +
				" SELECT * FROM " + tblAccounts +
				" WHERE AcctTime " + 
				" LIKE ?";

		myDB = this.getActivity().openOrCreateDatabase(dbFinance, this.getActivity().MODE_PRIVATE, null);
		Cursor c = null;
		try{
			c = myDB.rawQuery(sqlCommand, new String[] { "%" + query  + "%", "%" + query  + "%", "%" + query  + "%", "%" + query  + "%" });
		}
		catch(Exception e){
			Toast.makeText(this.getActivity(), "Search Failed\n"+e, Toast.LENGTH_SHORT).show();
			return;
		}

		this.getActivity().startManagingCursor(c);

		String id = null;
		String name = null;
		String balance = null;
		String time = null;
		String date = null;

		c.moveToFirst();
		if(c!=null){
			if (c.isFirst()) {
				do{
					id = c.getString(c.getColumnIndex("AcctID"));
					name = c.getString(c.getColumnIndex("AcctName"));
					balance = c.getString(c.getColumnIndex("AcctBalance"));
					time = c.getString(c.getColumnIndex("AcctTime"));
					date = c.getString(c.getColumnIndex("AcctDate"));
					//Toast.makeText(this.getActivity(), "Id: "+ id + "\nName: " + name + "\nBalance: " + balance, Toast.LENGTH_SHORT).show();					
					AccountRecord entry = new AccountRecord(id, name, balance,date,time);
					results.add(entry);
					
					totalRecords = totalRecords + 1;

				}while(c.moveToNext());
			}
			else{
				//No Results Found For Search
				TextView noResult = (TextView)myFragmentView.findViewById(R.id.search_noAccount);
				noResult.setVisibility(View.VISIBLE);
			}
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		//Close Cursor
		c.close();

		//Set up an adapter for the listView
		adapter = new UserItemAdapter(this.getActivity(), android.R.layout.simple_list_item_1, results);
		lv.setAdapter(adapter);
		
		//Refresh Balance
		calculateRecords();

		return;

	}//end populate

	public class UserItemAdapter extends ArrayAdapter<AccountRecord> {
		private ArrayList<AccountRecord> account;

		public UserItemAdapter(Context context, int textViewResourceId, ArrayList<AccountRecord> users) {
			super(context, textViewResourceId, users);
			this.account = users;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			AccountRecord user = account.get(position);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.account_item, null);
			}

			if (user != null) {
				TextView name = (TextView) v.findViewById(R.id.account_name);
				TextView balance = (TextView) v.findViewById(R.id.account_balance);
				TextView date = (TextView) v.findViewById(R.id.account_date);
				TextView time = (TextView) v.findViewById(R.id.account_time);

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
						if(Float.parseFloat((user.balance)) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(Float.parseFloat((user.balance)) >=0){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}
					}

				}
				catch(Exception e){
					Toast.makeText(this.getContext(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
				}

				if (user.name != null) {
					name.setText(user.name);
				}

				if(user.balance != null) {
					balance.setText("Balance: " + user.balance );
				}

				if(user.date != null) {
					date.setText("Date: " + user.date );
				}

				if(user.time != null) {
					time.setText("Time: " + user.time );
				}

			}
			return v;
		}//end getview

	}//end useritemclass

}//end SearchAccounts