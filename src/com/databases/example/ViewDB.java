package com.databases.example;

import java.util.ArrayList;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ListView;

@SuppressWarnings("unused")
public class ViewDB extends ListActivity {
	Cursor c = null;
	final String MY_DB_TABLE1 = "t_Name";
	final String MY_DB_NAME = "FireCATlog";
	SQLiteDatabase myDB;
	ArrayList<String> results = new ArrayList<String>();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		open();
	}// end onCreate

	protected void open() {
		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(MY_DB_NAME, MODE_PRIVATE, null);
		c = myDB.query(MY_DB_TABLE1, new String[] { "Name", "Type", "Time", "Date" }, null,
				null, null, null, null);
		startManagingCursor(c);
		int NameColumn = c.getColumnIndex("Name");
		int TypeColumn = c.getColumnIndex("Type");
		int TimeColumn = c.getColumnIndex("Time");
		int DateColumn = c.getColumnIndex("Date");
		results.add(" BACK ");

		c.moveToFirst();
		if (c != null) {
			if (c.isFirst()) {
				int i = 0;
				do {
					i++;
					String Name = c.getString(NameColumn);
					String Type = c.getString(TypeColumn);
					String Time = c.getString(TimeColumn);
					String Date = c.getString(DateColumn);
					if (Name != null && Type != null && Time != null && Date != null && 
							Name != "" && Type != "" && Time != "" && Date != "") {
						results.add(" " + i + ": " + Name + ", "
								+ Type + ", " + Time + ", " + Date);

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

	protected void onListItemClick(ListView l, View v, int position, long id) {
		int selectionRowID = (int) this.getSelectedItemId();
		String selectedEntry = this.results.get(selectionRowID);

		if (selectedEntry.equals(" BACK ")) {
			// Refresh
			finish();
		}

	}// end onListItemClick

}// end ViewDB
