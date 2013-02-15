package com.databases.example;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockActivity;
import com.slidingmenu.lib.SlidingMenu;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.databases.example.Transactions.DatePickerFragment;
import com.databases.example.Transactions.TimePickerFragment;
import com.slidingmenu.lib.SlidingMenu;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class Categories extends SherlockActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	final String tblCategory = "tblCategory";
	
	private SliderMenu menu;
	
	ListView lv = null;

	//Adapter for category spinner
	SimpleCursorAdapter categoryAdapter = null;
	Cursor categoryCursor;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Categories");
		setContentView(R.layout.categories);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		
		lv = (ListView)this.findViewById(R.id.category_list);        
		
		//Turn clicks on
		lv.setClickable(true);
		lv.setLongClickable(true);
		
		categoryPopulate();

	}
	
	//Method Called to refresh the list of categories if user changes the list
	public void categoryPopulate(){

		String[] from = new String[] {"CateName"}; 
		int[] to = new int[] { android.R.id.text1 };
		
		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, this.MODE_PRIVATE, null);

		final String sqlCategoryPopulate = "SELECT CateID as _id,CateName FROM " + tblCategory
				+ ";";
		
		categoryCursor = myDB.rawQuery(sqlCategoryPopulate, null);
		
		categoryAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, categoryCursor, from, to);
		
		lv.setAdapter(categoryAdapter);
		
		//categoryCursor.close();
		
		//Close Database
		if (myDB != null){
			myDB.close();
		}
		
	}//end of categoryPopulate

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			menu.toggle();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}//end class