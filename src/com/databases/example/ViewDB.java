package com.databases.example;

import java.util.ArrayList;
import android.app.ListActivity;
//import android.content.ContentValues;
//import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ListView;

public class ViewDB extends ListActivity {

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=1;
	int CONTEXT_MENU_EDIT=2;
	int CONTEXT_MENU_DELETE=3;

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

		//Allows Context Menus for each item of the list view
		registerForContextMenu(lv);

		start();
	}// end onCreate

	//Method called after creation
	protected void start() {
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
		} 

		else {
			results.add(" DATABASE EMPTY!!! ");
		}

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, results);
		this.setListAdapter(adapter);

	}

	//Method for Click
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		int selectionRowID = (int) getListAdapter().getItemId(position);
		String item = (String) getListAdapter().getItem(position);

		Toast.makeText(ViewDB.this, "Click\nRow: " + selectionRowID + "\nEntry: " + item, 4000).show();

		if (item.contains("BACK")) {
			// Refresh
			Toast.makeText(ViewDB.this, " Going Back... ", 3000).show();
			finish();
		}

	}// end onListItemClick

	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
		String name = "" + getListAdapter().getItem(itemInfo.position);

		menu.setHeaderTitle(name);  
		menu.add(0, CONTEXT_MENU_OPEN, 0, "Open");  
		menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit");
		menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");
	}  

	@Override  
	public boolean onContextItemSelected(MenuItem item) {

		if(item.getTitle()=="Open"){
			accountOpen(item);
		}  
		else if(item.getTitle()=="Edit"){
			accountEdit(item);
		}
		else if(item.getTitle()=="Delete"){
			accountDelete(item);
		}
		else {
			System.out.print("ERROR on ContextMenu; function not found");
			return false;
		}  

		return true;  
	}  

	//For Opening an Account
	public void accountOpen(MenuItem item){  
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = getListAdapter().getItem(itemInfo.position);

		Toast.makeText(this, "Opened Item:\n" + itemName, Toast.LENGTH_SHORT).show();  
	}  

	//For Editing an Account
	public void accountEdit(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = getListAdapter().getItem(itemInfo.position);

		Toast.makeText(this, "Editing Item:\n" + itemName, Toast.LENGTH_SHORT).show();  
	}

	//For Deleting an Account
	public void accountDelete(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = getListAdapter().getItem(itemInfo.position);

		//Need to skip code if you try to delete "BACK" entry
		if(itemInfo.position>=1){

			//NEEDS ACCOUNT ID else it will delete multiple accounts of same name!
			//NOTE: LIMIT *position*,*how many after*
			String sqlCommand = "DELETE FROM " + tblAccounts + " WHERE Name IN (SELECT Name FROM (SELECT Name FROM " + tblAccounts + " LIMIT " + (itemInfo.position-1) + ",1)AS tmp);";

			//Open Database
			myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

			myDB.execSQL(sqlCommand);
			Toast.makeText(this, "SQL\n" + sqlCommand, 5000).show();

			//Close Database if Opened
			if (myDB != null){
				myDB.close();
			}

			Toast.makeText(this, "Deleted Item:\n" + itemName, Toast.LENGTH_SHORT).show();
		}

	}

}// end ViewDB
