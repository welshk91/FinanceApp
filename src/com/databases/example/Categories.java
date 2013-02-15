package com.databases.example;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.slidingmenu.lib.SlidingMenu;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.databases.example.Accounts.UserItemAdapter;
import com.databases.example.Transactions.DatePickerFragment;
import com.databases.example.Transactions.TimePickerFragment;
import com.slidingmenu.lib.SlidingMenu;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListView;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class Categories extends SherlockActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	final String tblCategory = "tblCategory";

	private SliderMenu menu;

	ActionSlideExpandableListView lv = null;
	ArrayAdapter<CategoryRecord> adapter = null;
	//ListAdapter adapter = null;

	//Adapter for category spinner
	SimpleCursorAdapter categoryAdapter = null;
	ArrayList<CategoryRecord> results = new ArrayList<CategoryRecord>();
	Cursor categoryCursor;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		
		setTitle("Categories");
		setContentView(R.layout.categories);

		lv = (ActionSlideExpandableListView)this.findViewById(R.id.category_list);        

		//Turn clicks on
		//lv.setClickable(true);
		//lv.setLongClickable(true);

		categoryPopulate();

	}

	//Method Called to refresh the list of categories if user changes the list
	public void categoryPopulate(){
		results = new ArrayList<CategoryRecord>();

		//A textView alerting the user if database is empty
		TextView noResult = (TextView)this.findViewById(R.id.category_noCategory);
		noResult.setVisibility(View.GONE);

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, this.MODE_PRIVATE, null);

		final String sqlCategoryPopulate = "SELECT CateID as _id,CateName FROM " + tblCategory
				+ ";";

		//categoryCursor = myDB.rawQuery(sqlCategoryPopulate, null);
		categoryCursor = myDB.query(tblCategory, new String[] { "CateID", "CateName", "CateNote"}, null,
				null, null, null, null);

		startManagingCursor(categoryCursor);
		int IDColumn = categoryCursor.getColumnIndex("CateID");
		int NameColumn = categoryCursor.getColumnIndex("CateName");
		int NoteColumn = categoryCursor.getColumnIndex("CateNote");

		categoryCursor.moveToFirst();
		if (categoryCursor != null) {
			if (categoryCursor.isFirst()) {
				do {
					String id = categoryCursor.getString(IDColumn);
					String name = categoryCursor.getString(NameColumn);
					String note = categoryCursor.getString(NoteColumn);

					CategoryRecord entry = new CategoryRecord(id, name, note);
					results.add(entry);

				} while (categoryCursor.moveToNext());
			}

			else {
				//No Results Found
				noResult.setVisibility(View.VISIBLE);
			}
		} 

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		adapter = new UserItemAdapter(this, android.R.layout.simple_list_item_1, results);		

		lv.setAdapter(new SlideExpandableListAdapter(
				adapter,
				R.id.expandable_toggle_button,
				R.id.expandable
				));

		// listen for events in the two buttons for every list item.
		// the 'position' var will tell which list item is clicked
		lv.setItemActionListener(new ActionSlideExpandableListView.OnActionClickListener() {

			@Override
			public void onClick(View listView, View buttonview, int position) {

				/**
				 * Normally you would put a switch
				 * statement here, and depending on
				 * view.getId() you would perform a
				 * different action.
				 */
				String actionName = "";
				if(buttonview.getId()==R.id.ButtonA) {
					actionName = "buttonA";
				} else {
					actionName = "ButtonB";
				}
				/**
				 * For testing sake we just show a toast
				 */

				Log.e("Categories", "Clicked Action: "+actionName+" in list item "+position);

			}

			// note that we also add 1 or more ids to the setItemActionListener
			// this is needed in order for the listview to discover the buttons
		}, R.id.ButtonA, R.id.ButtonB);


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

	public class UserItemAdapter extends ArrayAdapter<CategoryRecord> {
		private ArrayList<CategoryRecord> category;

		public UserItemAdapter(Context context, int textViewResourceId, ArrayList<CategoryRecord> users) {
			super(context, textViewResourceId, users);
			this.category = users;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			CategoryRecord user = category.get(position);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.category_item, null);

				//Change Background Colors
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.category_layout);
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
					Toast.makeText(this.getContext(), "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
				}

				//Change Size of main field
				try{
					String DefaultSize = prefs.getString(this.getContext().getString(R.string.pref_key_account_nameSize), "16");
					TextView t;
					t=(TextView)v.findViewById(R.id.category_name);

					if(useDefaults){
						t.setTextSize(16);
					}
					else{
						t.setTextSize(Integer.parseInt(DefaultSize));
					}

				}
				catch(Exception e){
					Toast.makeText(this.getContext(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

				try{
					int DefaultColor = prefs.getInt("key_account_nameColor", Color.parseColor("#000000"));
					TextView t;
					t=(TextView)v.findViewById(R.id.category_name);

					if(useDefaults){
						t.setTextColor(Color.parseColor("#000000"));
					}
					else{
						t.setTextColor(DefaultColor);
					}

				}
				catch(Exception e){
					Toast.makeText(this.getContext(), "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

				//				try{
				//					String DefaultSize = prefs.getString(this.getContext().getString(R.string.pref_key_account_fieldSize), "10");
				//					TextView tmp;
				//
				//					if(useDefaults){
				//						tmp=(TextView)v.findViewById(R.id.account_balance);
				//						tmp.setTextSize(10);
				//						tmp=(TextView)v.findViewById(R.id.account_date);
				//						tmp.setTextSize(10);
				//						tmp=(TextView)v.findViewById(R.id.account_time);
				//						tmp.setTextSize(10);
				//					}
				//					else{
				//						tmp=(TextView)v.findViewById(R.id.account_balance);
				//						tmp.setTextSize(Integer.parseInt(DefaultSize));
				//						tmp=(TextView)v.findViewById(R.id.account_date);
				//						tmp.setTextSize(Integer.parseInt(DefaultSize));
				//						tmp=(TextView)v.findViewById(R.id.account_time);
				//						tmp.setTextSize(Integer.parseInt(DefaultSize));
				//					}
				//
				//				}
				//				catch(Exception e){
				//					Toast.makeText(this.getContext(), "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
				//				}
				//
				//				try{
				//					int DefaultColor = prefs.getInt("key_account_fieldColor", Color.parseColor("#0099CC"));
				//					TextView tmp;
				//
				//					if(useDefaults){
				//						tmp=(TextView)v.findViewById(R.id.account_balance);
				//						tmp.setTextColor(Color.parseColor("#0099CC"));
				//						tmp=(TextView)v.findViewById(R.id.account_date);
				//						tmp.setTextColor(Color.parseColor("#0099CC"));
				//						tmp=(TextView)v.findViewById(R.id.account_time);
				//						tmp.setTextColor(Color.parseColor("#0099CC"));
				//					}
				//					else{
				//						tmp=(TextView)v.findViewById(R.id.account_balance);
				//						tmp.setTextColor(DefaultColor);
				//						tmp=(TextView)v.findViewById(R.id.account_date);
				//						tmp.setTextColor(DefaultColor);
				//						tmp=(TextView)v.findViewById(R.id.account_time);
				//						tmp.setTextColor(DefaultColor);
				//					}
				//
				//				}
				//				catch(Exception e){
				//					Toast.makeText(this.getContext(), "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
				//				}


				//For User-Defined Field Visibility
				//				if(useDefaults||prefs.getBoolean("checkbox_account_nameField", true)){
				//					TextView name = (TextView) v.findViewById(R.id.account_name);
				//					name.setVisibility(View.VISIBLE);
				//				}
				//				else{
				//					TextView name = (TextView) v.findViewById(R.id.account_name);
				//					name.setVisibility(View.GONE);
				//				}
				//
				//				if(useDefaults||prefs.getBoolean("checkbox_account_balanceField", true)){
				//					TextView balance = (TextView) v.findViewById(R.id.account_balance);
				//					balance.setVisibility(View.VISIBLE);
				//				}
				//				else{
				//					TextView balance = (TextView) v.findViewById(R.id.account_balance);
				//					balance.setVisibility(View.GONE);
				//				}
				//
				//				if(useDefaults||prefs.getBoolean("checkbox_account_dateField", true)){
				//					TextView date = (TextView) v.findViewById(R.id.account_date);
				//					date.setVisibility(View.VISIBLE);
				//				}
				//				else{
				//					TextView date = (TextView) v.findViewById(R.id.account_date);
				//					date.setVisibility(View.GONE);
				//				}
				//
				//				if(useDefaults||prefs.getBoolean("checkbox_account_timeField", true)){
				//					TextView time = (TextView) v.findViewById(R.id.account_time);
				//					time.setVisibility(View.VISIBLE);
				//				}
				//				else{
				//					TextView time = (TextView) v.findViewById(R.id.account_time);
				//					time.setVisibility(View.GONE);
				//				}

			}

			if (user != null) {
				TextView name = (TextView) v.findViewById(R.id.category_name);
				//				TextView balance = (TextView) v.findViewById(R.id.account_balance);

				if (user.name != null) {
					name.setText(user.name);
				}

				//				if(user.balance != null) {
				//					balance.setText("Balance: " + user.balance );
				//				}

			}
			return v;
		}//end getView

	} //end of UserItemAdapter

	//An Object Class used to hold the data of each category record
	public class CategoryRecord {
		protected String id;
		protected String name;
		protected String note;

		public CategoryRecord(String id, String name, String note) {
			this.id = id;
			this.name = name;
			this.note = note;
		}
	}

	//An Object Class used to hold the data of each sub-category record
	public class SubCategoryRecord {
		protected String id;
		protected String catId;
		protected String name;
		protected String note;

		public SubCategoryRecord(String id, String catId, String name, String note) {
			this.id = id;
			this.catId = catId;
			this.name = name;
			this.note = note;
		}
	}

}//end category