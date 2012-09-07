package com.databases.example;

import java.util.ArrayList;

import com.databases.example.SearchAccounts.AccountRecord;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchTransactions extends Activity {
	//For Searching
	String SEARCH_CONTEXT=null;

	//ListView
	ListView lv = null;
	ArrayAdapter<TransactionRecord> adapter = null;
	ArrayList<TransactionRecord> results = new ArrayList<TransactionRecord>();

	//Database
	final String tblAccounts = "tblAccounts";
	final String tblTrans = "tblTrans";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater li = LayoutInflater.from(this);
		View searchTransactionView = li.inflate(R.layout.search_transaction, null);

		String query = getIntent().getExtras().getString("Query");
		SEARCH_CONTEXT = getIntent().getExtras().getString("Caller");

		lv = (ListView)searchTransactionView.findViewById(R.id.search_transaction_list);

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		setContentView(searchTransactionView);
		//Toast.makeText(this, "SearchTransactions Query: " + query + "\nCaller: " + SEARCH_CONTEXT, Toast.LENGTH_SHORT).show();

		//Set up an adapter for the listView
		try{
			adapter = new UserItemAdapter(this, android.R.layout.simple_list_item_1, results);
			lv.setAdapter(adapter);
		}
		catch(Exception e){
			Toast.makeText(this, "Error Here\n" + e, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		populate(query);


	}//end onCreate

	public void populate(String query){
		String sqlCommand = " SELECT * FROM " + tblTrans + 
				" WHERE TransName " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransValue " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransDate " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransTime " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransMemo " + 
				" LIKE '%" + query + "%'" +
				" UNION " +
				" SELECT * FROM " + tblTrans +
				" WHERE TransCheckNum " + 
				" LIKE '%" + query + "%'";

		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		Cursor c = myDB.rawQuery(sqlCommand, null);

		//Toast.makeText(this, "Searching From " + Boolean.toString(jargon), Toast.LENGTH_LONG).show();

		startManagingCursor(c);

		c.moveToFirst();
		if(c!=null){
			if (c.isFirst()) {
				do{
					int id = c.getInt(c.getColumnIndex("TransID"));
					int acctId = c.getInt(c.getColumnIndex("ToAcctID"));
					String name = c.getString(c.getColumnIndex("TransName"));
					String value = c.getString(c.getColumnIndex("TransValue"));
					String type = c.getString(c.getColumnIndex("TransType"));
					String category = c.getString(c.getColumnIndex("TransCategory"));
					String checknum = c.getString(c.getColumnIndex("TransCheckNum"));
					String memo = c.getString(c.getColumnIndex("TransMemo"));
					String time = c.getString(c.getColumnIndex("TransTime"));
					String date = c.getString(c.getColumnIndex("TransDate"));
					String cleared = c.getString(c.getColumnIndex("TransCleared"));
					TransactionRecord entry = new TransactionRecord(id, acctId, name, value, type, category, checknum, memo, date,time, cleared);
					results.add(entry);	
					//Toast.makeText(this, "Id: "+ id + "\nToAcctID: "+ acctId + "\nName: " + name + "\nValue: " + value, Toast.LENGTH_LONG).show();
				}while(c.moveToNext());
			}
			else{
				Toast.makeText(this, "Transactions: No Search Results for " + query, Toast.LENGTH_SHORT).show();
			}
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		return;		

	}//end populate


	public class UserItemAdapter extends ArrayAdapter<TransactionRecord> {
		private ArrayList<TransactionRecord> transaction;

		public UserItemAdapter(Context context, int textViewResourceId, ArrayList<TransactionRecord> users) {
			super(context, textViewResourceId, users);
			this.transaction = users;
		}

		//Used to Define the View of each transaction
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			TransactionRecord user = transaction.get(position);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SearchTransactions.this);
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.transaction_item, null);

			}

			if (user != null) {
				TextView name = (TextView) v.findViewById(R.id.transaction_name);
				TextView value = (TextView) v.findViewById(R.id.transaction_value);
				TextView type = (TextView) v.findViewById(R.id.transaction_type);
				TextView category = (TextView) v.findViewById(R.id.transaction_category);
				TextView checknum = (TextView) v.findViewById(R.id.transaction_checknum);
				TextView memo = (TextView) v.findViewById(R.id.transaction_memo);
				TextView date = (TextView) v.findViewById(R.id.transaction_date);
				TextView time = (TextView) v.findViewById(R.id.transaction_time);
				TextView cleared = (TextView) v.findViewById(R.id.transaction_cleared);

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
						if(user.type.contains("Deposit")){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}

					}
					else{
						if(user.type.contains("Deposit")){
							l.setBackgroundDrawable(defaultGradientPos);
						}
						else{
							l.setBackgroundDrawable(defaultGradientNeg);
						}
					}

				}
				catch(Exception e){
					Toast.makeText(SearchTransactions.this, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
				}

				if (user.name != null) {
					name.setText(user.name);
				}

				if(user.value != null) {
					value.setText("Value: " + user.value );
				}

				if(user.type != null) {
					type.setText("Type: " + user.type );
				}

				if(user.category != null) {
					category.setText("Category: " + user.category );
				}

				if(user.checknum != null) {
					checknum.setText("Check Num: " + user.checknum );
				}

				if(user.memo != null) {
					memo.setText("Memo: " + user.memo );
				}

				if(user.date != null) {
					date.setText("Date: " + user.date );
				}

				if(user.time != null) {
					time.setText("Time: " + user.time );
				}

				if(user.cleared != null) {
					cleared.setText("Cleared: " + user.cleared );
				}

			}
			return v;
		}
	}

	//An Object Class used to hold the data of each transaction record
	public class TransactionRecord {
		private int id;
		private int acctId;
		private String name;
		private String value;
		private String type;
		private String category;
		private String checknum;
		private String memo;
		private String time;
		private String date;
		private String cleared;

		public TransactionRecord(int id, int acctId, String name, String value, String type, String category, String checknum, String memo, String time, String date, String cleared) {
			this.id = id;
			this.acctId = acctId;
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

	//Override method to send the search extra data, letting it know which class called it
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString("appData.key", SEARCH_CONTEXT);
		startSearch(null, false, appData, false);
		return true;
	}



}//end SearchTransactions