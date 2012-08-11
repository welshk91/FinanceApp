package com.databases.example;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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

	//Constants for ContextMenu
	int CONTEXT_MENU_OPEN=1;
	int CONTEXT_MENU_EDIT=2;
	int CONTEXT_MENU_DELETE=3;

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

		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);

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

		//Set Listener for regular mouse click
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				int selectionRowID = (int) adapter.getItemId(position);
				String item = (String) adapter.getItem(position);

				Toast.makeText(Transactions.this, "Click\nRow: " + selectionRowID + "\nEntry: " + item, 4000).show();
				if (item.contains("BACK")) {
					// Refresh
					Toast.makeText(Transactions.this, " Going Back... ", 3000).show();
					finish();
				}

			}// end onItemClick

		}//end onItemClickListener
				);//end setOnItemClickListener


		//Allows Context Menus for each item of the list view
		registerForContextMenu(lv);

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

	}//end populate

	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
		String name = "" + adapter.getItem(itemInfo.position);

		menu.setHeaderTitle(name);  
		menu.add(0, CONTEXT_MENU_OPEN, 0, "Open");  
		menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit");
		menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");
	}  

	@Override  
	public boolean onContextItemSelected(MenuItem item) {

		if(item.getTitle()=="Open"){
			transactionOpen(item);
		}  
		else if(item.getTitle()=="Edit"){
			transactionEdit(item);
		}
		else if(item.getTitle()=="Delete"){
			transactionDelete(item);
		}
		else {
			System.out.print("ERROR on ContextMenu; function not found");
			return false;
		}  

		return true;  
	}

	//For Opening an Account
	public void transactionOpen(MenuItem item){  
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		Toast.makeText(this, "Opened Item:\n" + itemName, Toast.LENGTH_SHORT).show();  
	}  

	//For Editing an Account
	public void transactionEdit(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		Toast.makeText(this, "Editing Item:\n" + itemName, Toast.LENGTH_SHORT).show();  
	}

	//For Deleting an Account
	public void transactionDelete(MenuItem item){
		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Object itemName = adapter.getItem(itemInfo.position);

		//Needed to skip code if you try to delete "BACK" entry
		if(itemInfo.position>=1){

			//NOTE: LIMIT *position*,*how many after*
			//NOT PERFECT!!! HAD SOME CASES WHERE DELETE DIDNT KILL IT
			String sqlCommand = "DELETE FROM " + tblTrans + " WHERE TransID IN (SELECT TransID FROM (SELECT TransID FROM " + tblTrans + " LIMIT " + (itemInfo.position-1) + ",1)AS tmp);";

			//Open Database
			myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

			myDB.execSQL(sqlCommand);
			Toast.makeText(this, "SQL\n" + sqlCommand, 5000).show();

			//Close Database if Opened
			if (myDB != null){
				myDB.close();
			}

			results.remove(itemInfo.position);
			adapter.notifyDataSetChanged();

			Toast.makeText(this, "Deleted Item:\n" + itemName, Toast.LENGTH_SHORT).show();
		}

	}//end of accountDelete

	//For Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.transaction_menu, menu);
		return true;
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.transaction_menu_logout:     
			Toast.makeText(this, "You pressed Logout!", Toast.LENGTH_LONG).show();
			this.finish();
			this.moveTaskToBack(true);
			super.onDestroy();
			break;

		case R.id.transaction_menu_options:    
			Toast.makeText(this, "You pressed Options!", Toast.LENGTH_LONG).show();
			break;

		case R.id.transaction_menu_help:    
			Toast.makeText(this, "You pressed Help!", Toast.LENGTH_LONG).show();
			break;
		}
		return true;
	}

}//end Transactions
