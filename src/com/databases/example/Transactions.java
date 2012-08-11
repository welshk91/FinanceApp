package com.databases.example;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Transactions extends Activity{

	//Variables for the transaction Table
	String transactionName = null;
	String transactionTime = null;
	String transactionBalance = null;
	String transactionDate = null;

	int account_id;
	String account_name;
	String account_balance;
	String account_date;
	String account_time;

	ListView lv = null;
	ArrayAdapter<String> adapter = null;

	Cursor c = null;
	final String tblTrans = "tblTrans";
	final String dbFinance = "dbFinance";
	SQLiteDatabase myDB;
	ArrayList<String> results = new ArrayList<String>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.accounts);

		lv = (ListView)findViewById(R.id.list);
		lv.setAdapter(adapter);

		//No Parameters used in calling Intent
		if(getIntent().getExtras()==null){
			Toast.makeText(this, "Could Not Find Account Information", 5000).show();
			return;
		}

		account_id = getIntent().getExtras().getInt("ID");
		account_name = getIntent().getExtras().getString("name");
		account_balance = getIntent().getExtras().getString("balance");
		account_date = getIntent().getExtras().getString("date");
		account_time = getIntent().getExtras().getString("time");

		Toast.makeText(this, "ID: "+account_id+"\nName: "+account_name+"\nBalance: "+account_balance+"\nTime: "+account_time+"\nDate: "+account_date, 2000).show();

		populate();

	}//end onCreate


	//Populate view with all the transactions of selected account
	protected void populate(){
		//Add A back button. Might want to change this to a menu button, as you'd have to scroll up if list is big
		results = new ArrayList<String>();
		results.add(" BACK ");

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		c = myDB.query(tblTrans, new String[] { "TransID", "TransAmt", "ToAcctID"}, "ToAcctID = " + account_id,
				null, null, null, null);

		startManagingCursor(c);
		int idColumn = c.getColumnIndex("TransID");
		int amtColumn = c.getColumnIndex("TransAmt");
		int acctColumn = c.getColumnIndex("ToAcctID");

		c.moveToFirst();
		if (c != null) {
			if (c.isFirst()) {
				do {
					String Name = c.getString(idColumn);
					String Balance = c.getString(amtColumn);
					String Time = c.getString(acctColumn);
					if (Name != null && Balance != null && Time != null && 
							Name != "" && Balance != "" && Time != "") {
						results.add(Name + ", "
								+ Balance + ", " + Time);
					}
				} while (c.moveToNext());
			}
		} 

		else {
			results.add(" DATABASE EMPTY!!! ");
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		//adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, results);
		adapter = new ArrayAdapter<String>(this,R.layout.account_item, results);
		lv.setAdapter(adapter);

	}



}//end Transactions
