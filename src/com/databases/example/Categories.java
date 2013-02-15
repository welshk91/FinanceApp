package com.databases.example;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.slidingmenu.lib.SlidingMenu;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.slidingmenu.lib.SlidingMenu;
import com.tjerkw.slideexpandable.library.ActionSlideExpandableListView;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;


public class Categories extends SherlockActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	final String tblCategory = "tblCategory";
	final String tblSubCategory = "tblSubCategory";

	private SliderMenu menu;

	ListView lvCategory = null;
	ListView lvSubCategory = null;
	ArrayAdapter<CategoryRecord> adapterCategory = null;
	//ArrayAdapter<SubCategoryRecord> adapterSubCategory = null;
	SimpleCursorAdapter adapterSubCategory = null;

	//Constant for ActionbarId
	final int ACTIONBAR_MENU_ADD_CATEGORY_ID = 8675309;
	
	//Constants for ContextMenu
	int CONTEXT_MENU_ADD=1;
	int CONTEXT_MENU_VIEW=2;
	int CONTEXT_MENU_EDIT=3;
	int CONTEXT_MENU_DELETE=4;

	ArrayList<CategoryRecord> resultsCategory = new ArrayList<CategoryRecord>();
	ArrayList<SubCategoryRecord> resultsSubCategory = new ArrayList<SubCategoryRecord>();
	Cursor cursorCategory;
	Cursor cursorSubCategory;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//Add Sliding Menu
		//menu = new SliderMenu(this);
		//menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		setTitle("Categories");
		setContentView(R.layout.categories);

		lvCategory = (ListView)this.findViewById(R.id.category_list);

		//Turn clicks on
		lvCategory.setClickable(true);
		lvCategory.setLongClickable(true);

		categoryPopulate();

	}

	//Method Called to refresh the list of categories if user changes the list
	public void categoryPopulate(){
		resultsCategory = new ArrayList<CategoryRecord>();

		//A textView alerting the user if database is empty
		TextView noResult = (TextView)this.findViewById(R.id.category_noCategory);
		noResult.setVisibility(View.GONE);

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, this.MODE_PRIVATE, null);

		//categoryCursor = myDB.rawQuery(sqlCategoryPopulate, null);
		cursorCategory = myDB.query(tblCategory, new String[] { "CateID", "CateName", "CateNote"}, null,
				null, null, null, null);

		startManagingCursor(cursorCategory);
		int IDColumn = cursorCategory.getColumnIndex("CateID");
		int NameColumn = cursorCategory.getColumnIndex("CateName");
		int NoteColumn = cursorCategory.getColumnIndex("CateNote");

		cursorCategory.moveToFirst();
		if (cursorCategory != null) {
			if (cursorCategory.isFirst()) {
				do {
					String id = cursorCategory.getString(IDColumn);
					String name = cursorCategory.getString(NameColumn);
					String note = cursorCategory.getString(NoteColumn);

					CategoryRecord entry = new CategoryRecord(id, name, note);
					resultsCategory.add(entry);

				} while (cursorCategory.moveToNext());
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

		//Allows Context Menus for each item of the list view
		registerForContextMenu(lvCategory);
		
		adapterCategory = new UserItemAdapter(this, android.R.layout.simple_list_item_1, resultsCategory);		

		lvCategory.setAdapter(new SlideExpandableListAdapter(
				adapterCategory,
				R.id.expandable_toggle_button,
				R.id.expandable
				));

		//		// listen for events in the two buttons for every list item.
		//		// the 'position' var will tell which list item is clicked
		//		lvCategory.setItemActionListener(new ActionSlideExpandableListView.OnActionClickListener() {
		//
		//			@Override
		//			public void onClick(View listView, View buttonview, int position) {
		//
		//				/**
		//				 * Normally you would put a switch
		//				 * statement here, and depending on
		//				 * view.getId() you would perform a
		//				 * different action.
		//				 */
		//				String actionName = "";
		//				if(buttonview.getId()==R.id.ButtonA) {
		//					actionName = "buttonA";
		//				} else {
		//					actionName = "ButtonB";
		//				}
		//
		//				Log.e("Categories", "Clicked Action: "+actionName+" in list item "+position);
		//
		//			}
		//
		//			// note that we also add 1 or more ids to the setItemActionListener
		//			// this is needed in order for the listview to discover the buttons
		//		}, R.id.ButtonA, R.id.ButtonB);



		//categoryCursor.close();

		//Close Database
		if (myDB != null){
			myDB.close();
		}

		Log.e("Categories","out of category populate");

	}//end of categoryPopulate

	//Method for filling subcategories
	public void subcategoryPopulate(View v, String catId){
		Log.e("Categories","In subcategoryPopulate(" + catId +")");
		resultsSubCategory = new ArrayList<SubCategoryRecord>();

		//A textView alerting the user if database is empty
		TextView noResult = (TextView)v.findViewById(R.id.subcategory_noSubCategory);
		noResult.setVisibility(View.GONE);

		lvSubCategory = (ListView)v.findViewById(R.id.subcategory_list);

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, this.MODE_PRIVATE, null);

		//cursorSubCategory = myDB.rawQuery(sqlSubCategoryPopulate, null);
		cursorSubCategory = myDB.query(tblSubCategory, new String[] { "SubCateID as _id", "ToCatID", "SubCateName", "SubCateNote"}, "ToCatID = " + catId,
				null, null, null, null);

		startManagingCursor(cursorSubCategory);
		int IDColumn = cursorSubCategory.getColumnIndex("SubCateID");
		int ToIDColumn = cursorSubCategory.getColumnIndex("ToCatID");
		int NameColumn = cursorSubCategory.getColumnIndex("SubCateName");
		int NoteColumn = cursorSubCategory.getColumnIndex("SubCateNote");

		cursorSubCategory.moveToFirst();
		if (cursorSubCategory != null) {
			if (cursorSubCategory.isFirst()) {
				do {
					String id = cursorSubCategory.getString(0);
					String to_id = cursorSubCategory.getString(ToIDColumn);
					String name = cursorSubCategory.getString(NameColumn);
					String note = cursorSubCategory.getString(NoteColumn);

					SubCategoryRecord entry = new SubCategoryRecord(id, catId, name, note);
					resultsSubCategory.add(entry);
					Log.e("Category", "Added SubCategory: " + id + " " + catId + " " + name + " " + note);

				} while (cursorSubCategory.moveToNext());
			}

			else {
				//No Results Found
				Log.e("Category", "No Subcategories found");
				noResult.setVisibility(View.VISIBLE);
			}
		} 

		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}

		String[] from = new String[] {"SubCateName"}; 
		int[] to = new int[] { android.R.id.text1 };

		adapterSubCategory = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursorSubCategory, from, to);		

		lvSubCategory.setAdapter(adapterSubCategory);

		//Log.e("Categories","Out of subcategoryPopulate(" + catId +")");

	}//end of subcategoryPopulate

	//For Menu Items

	//Alert for adding a new category
	public void categoryAdd(int catId){		
		LayoutInflater li = LayoutInflater.from(this);
		final View categoryAddView = li.inflate(R.layout.transaction_category_add, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set account_add.xml to AlertDialog builder
		alertDialogBuilder.setView(categoryAddView);

		//set Title
		alertDialogBuilder.setTitle("Create A Category");

		// set dialog message
		alertDialogBuilder
		.setCancelable(true)
		.setPositiveButton("Add",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				EditText categorySpinner = (EditText)categoryAddView.findViewById(R.id.EditCategoryName);
				String category = categorySpinner.getText().toString().trim();

				//Create database and open
				myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

				try{
					//Insert values into accounts table
					ContentValues categoryValues=new ContentValues();
					categoryValues.put("CateName",category);

					myDB.insert(tblCategory, null, categoryValues);
					
				}
				catch(Exception e){
					Log.e("Categories", "Error adding Categories");
				}

				//Make sure Database is closed
				if (myDB != null){
					myDB.close();
				}

				//Refresh the categories list
				categoryPopulate();

			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}//end of showCategoryAdd

	//For Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		//Show Search
		MenuItem menuSearch = menu.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_search, com.actionbarsherlock.view.Menu.NONE, "Search");
		menuSearch.setIcon(android.R.drawable.ic_menu_search);
		menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		SubMenu subMenu1 = menu.addSubMenu("Categories");
		subMenu1.add(com.actionbarsherlock.view.Menu.NONE, ACTIONBAR_MENU_ADD_CATEGORY_ID, com.actionbarsherlock.view.Menu.NONE, "Add");

		MenuItem subMenu1Item = subMenu1.getItem();
		subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			menu.toggle();
			break;

		case ACTIONBAR_MENU_ADD_CATEGORY_ID:
			categoryAdd(0);
			break;
			
		case R.id.account_menu_search:    
			onSearchRequested();
			return true;

		}

		return super.onOptionsItemSelected(item);
	}
	
	//Creates menu for long presses
		@Override  
		public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
			super.onCreateContextMenu(menu, v, menuInfo);

			AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
			String name = "" + (itemInfo.position);

			menu.setHeaderTitle(name);  
			menu.add(0, CONTEXT_MENU_ADD, 0, "Add");
			menu.add(0, CONTEXT_MENU_VIEW, 1, "View");  
			menu.add(0, CONTEXT_MENU_EDIT, 2, "Edit");
			menu.add(0, CONTEXT_MENU_DELETE, 3, "Delete");
		}  

		//Handles which methods are called when using the long presses menu
		/* NOTE: Not sure whether to use return false/true or return super.onContextItemSelected(item)
		 * Using 'super' causes a bug that performs an action twice if you single pane->dual pane->context menu
		 */
		@Override  
		public boolean onContextItemSelected(android.view.MenuItem item) {

			if(item.getItemId()==CONTEXT_MENU_ADD){
				Log.e("Categories","Context Menu ADD pressed");
				return true;
			}
			else if(item.getItemId()==CONTEXT_MENU_VIEW){
				Log.e("Categories","Context Menu View pressed");
				return true;
			}
			else if(item.getItemId()==CONTEXT_MENU_EDIT){
				Log.e("Categories","Context Menu Edit pressed");
				return true;
			}
			else if(item.getItemId()==CONTEXT_MENU_DELETE){
				Log.e("Categories","Context Menu Delete pressed");
				return true;
			}
			else {
				//return false;
				//return super.onContextItemSelected(item);
			}  

			return super.onContextItemSelected(item);  
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

				//Log.e("Categories","id: " + user.id);
				
				//Populate SubCategory List
				subcategoryPopulate(v, user.id);

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