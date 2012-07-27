package com.databases.example;

import java.util.ArrayList;
import android.app.ListActivity;
//import android.content.ContentValues;
//import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ListView;

public class ViewDB extends ListActivity {
	Cursor c = null;
	final String tblAccounts = "t_Name";
	final String dbFinance = "Financelog";
	SQLiteDatabase myDB;
	ArrayList<String> results = new ArrayList<String>();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		ListView lv = getListView();

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

		//Listener for Long Presses 
		lv.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener 
				(){ 
			@Override 
			public boolean onItemLongClick(AdapterView<?> av, View v, int 
					pos, long id) { 
				onLongListItemClick(v,pos,id); 
				return true; 
			} 
		}); 

		open();
	}// end onCreate

	//Method called after creation
	protected void open() {
		//Add A back button. Might want to change this to a menu button, as you'd have to scroll up if list is big
		results.add(" BACK ");

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
		c = myDB.query(tblAccounts, new String[] { "Name", "Balance", "Time", "Date" }, null,
				null, null, null, null);
		startManagingCursor(c);
		int NameColumn = c.getColumnIndex("Name");
		int BalanceColumn = c.getColumnIndex("Balance");
		int TimeColumn = c.getColumnIndex("Time");
		int DateColumn = c.getColumnIndex("Date");

		c.moveToFirst();
		if (c != null) {
			if (c.isFirst()) {
				int i = 0;
				do {
					i++;
					String Name = c.getString(NameColumn);
					String Balance = c.getString(BalanceColumn);
					String Time = c.getString(TimeColumn);
					String Date = c.getString(DateColumn);
					if (Name != null && Balance != null && Time != null && Date != null && 
							Name != "" && Balance != "" && Time != "" && Date != "") {
						results.add(" " + i + ": " + Name + ", "
								+ Balance + ", " + Time + ", " + Date);

					}
				} while (c.moveToNext());
			}
		} else {
			results.add(" DATABASE EMPTY!!! ");
		}
		if (myDB != null)
			myDB.close();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, results);
		this.setListAdapter(adapter);

	}

	//Method for Click
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		int selectionRowID = (int) getListAdapter().getItemId(position);
		String item = (String) getListAdapter().getItem(position);

		Toast.makeText(ViewDB.this, "Click\nRow: " + selectionRowID + "\nEntry: " + item, 4000)
		.show();

		if (item.contains("BACK")) {
			// Refresh
			Toast.makeText(ViewDB.this, " Going Back... ", 3000)
			.show();
			finish();
		}

	}// end onListItemClick

	//Method for Handling Long Press 
	protected void onLongListItemClick(View v, int position, long id) { 
		int selectionRowID = (int) getListAdapter().getItemId(position);
		String item = (String) getListAdapter().getItem(position);

		Toast.makeText(ViewDB.this, "Long Press\nRow: " + selectionRowID + "\nEntry: " + item, 4000)
		.show();
	} 

}// end ViewDB
