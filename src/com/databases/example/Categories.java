package com.databases.example;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.slidingmenu.lib.SlidingMenu;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.databases.example.Categories.SubCategoryRecord;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;


public class Categories extends SherlockActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	final String tblCategory = "tblCategory";
	final String tblSubCategory = "tblSubCategory";

	private SliderMenu menu;

	ExpandableListView lvCategory = null;
	UserItemAdapter adapterCategory = null;

	//Constant for ActionbarId
	final int ACTIONBAR_MENU_ADD_CATEGORY_ID = 8675309;

	//Constants for ContextMenu (Category)
	final int CONTEXT_MENU_CATEGORY_ADD=1;
	final int CONTEXT_MENU_CATEGORY_VIEW=2;
	final int CONTEXT_MENU_CATEGORY_EDIT=3;
	final int CONTEXT_MENU_CATEGORY_DELETE=4;

	//Constants for ContextMenu (SubCategory)
	final int CONTEXT_MENU_SUBCATEGORY_VIEW=5;
	final int CONTEXT_MENU_SUBCATEGORY_EDIT=6;
	final int CONTEXT_MENU_SUBCATEGORY_DELETE=7;

	ArrayList<CategoryRecord> resultsCategory = new ArrayList<CategoryRecord>();
	ArrayList<SubCategoryRecord> resultsSubCategory = new ArrayList<SubCategoryRecord>();
	ArrayList<Cursor> resultsCursor = new ArrayList<Cursor>();
	Cursor cursorCategory;
	Cursor cursorSubCategory;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		setTitle("Categories");
		setContentView(R.layout.categories);

		lvCategory = (ExpandableListView)this.findViewById(R.id.category_list);

		//Turn clicks on
		lvCategory.setClickable(true);
		lvCategory.setLongClickable(true);

		//Allows Context Menus for each item of the list view
		registerForContextMenu(lvCategory);

		categoryPopulate();

		//Give the item adapter a list of all categories and subcategories
		//adapterCategory = new UserItemAdapter(this, android.R.layout.simple_list_item_1, cursorCategory, resultsCursor);		
		//lvCategory.setAdapter(adapterCategory);

	}

	//Method Called to refresh the list of categories if user changes the list
	public void categoryPopulate(){
		//Reinitialize arrays to start fresh
		resultsCategory = new ArrayList<CategoryRecord>();
		resultsSubCategory = new ArrayList<SubCategoryRecord>();
		resultsCursor = new ArrayList<Cursor>();

		//A textView alerting the user if database is empty
		TextView noResult = (TextView)this.findViewById(R.id.category_noCategory);
		noResult.setVisibility(View.GONE);

		// Cursor is used to navigate the query results
		myDB = this.openOrCreateDatabase(dbFinance, this.MODE_PRIVATE, null);

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
					//Log.e("Category", "Added Category: " + id + " " + name + " " + note);
					subcategoryPopulate(id);
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

		//Give the item adapter a list of all categories and subcategories
		adapterCategory = new UserItemAdapter(this, android.R.layout.simple_list_item_1, cursorCategory, resultsCursor);		
		lvCategory.setAdapter(adapterCategory);
		
		//Log.e("Categories","out of category populate");

	}//end of categoryPopulate

	//Method for filling subcategories
	public void subcategoryPopulate(String catId){		
		//Database myDB is already open
		
		cursorSubCategory = myDB.query(tblSubCategory, new String[] { "SubCateID as _id", "ToCatID", "SubCateName", "SubCateNote"}, "ToCatID = " + catId,
				null, null, null, null);
		
		resultsCursor.add(cursorSubCategory);

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

					SubCategoryRecord entry = new SubCategoryRecord(id, to_id, name, note);
					resultsSubCategory.add(entry);
					//Log.d("Category", "Added SubCategory: " + id + " " + to_id + " " + name + " " + note);

				} while (cursorSubCategory.moveToNext());
			}

			else {
				//No Results Found
				Log.e("Category", "No Subcategories found");
			}
		} 

	}//end of subcategoryPopulate

	//Alert for adding a new category
	public void categoryAdd(android.view.MenuItem item){		
		boolean isCategory = true;
		String itemID = "0";
		
		if(item != null){
			isCategory = false;
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
			int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			itemID = adapterCategory.getCategory(groupPos).id;
			Log.e("categoryAdd", "itemID: " + itemID);
		}

		final boolean isCat = isCategory;
		final String catID = itemID;
		//Log.e("Categories","catID = " + catID);

		LayoutInflater li = LayoutInflater.from(this);
		final View categoryAddView = li.inflate(R.layout.transaction_category_add, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set account_add.xml to AlertDialog builder
		alertDialogBuilder.setView(categoryAddView);

		if(isCat){
			//set Title
			alertDialogBuilder.setTitle("Create A Category");
		}
		else{
			//set Title
			alertDialogBuilder.setTitle("Create A SubCategory");			
		}

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
					//Add a category
					if(isCat){
						//	Log.e("Category Add", "Adding a normal category : " + category);

						ContentValues categoryValues=new ContentValues();
						categoryValues.put("CateName",category);

						myDB.insert(tblCategory, null, categoryValues);

					}
					//Add a subcategory
					else{
						//	Log.e("Category Add", "Adding a subcategory : " + category + " " + catID);

						ContentValues subcategoryValues=new ContentValues();
						subcategoryValues.put("SubCateName",category);
						subcategoryValues.put("ToCatID",catID);

						myDB.insert(tblSubCategory, null, subcategoryValues);

					}

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

	//For ActionBar Menu
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

	//For ActionBar Menu Items (and home icon)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			menu.toggle();
			break;

		case ACTIONBAR_MENU_ADD_CATEGORY_ID:
			categoryAdd(null);
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
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;

		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);

		switch (type) {
		case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
			//Log.d("Categories", "Context Menu type CHILD");
			String nameSubCategory = adapterCategory.getSubCategory(groupPos,childPos).name;
			menu.setHeaderTitle(nameSubCategory);
			menu.add(0, CONTEXT_MENU_SUBCATEGORY_VIEW, 1, "View");  
			menu.add(0, CONTEXT_MENU_SUBCATEGORY_EDIT, 2, "Edit");
			menu.add(0, CONTEXT_MENU_SUBCATEGORY_DELETE, 3, "Delete");
			break;

		case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
			//Log.d("Categories", "Context Menu type GROUP");
			String nameCategory = adapterCategory.getCategory(groupPos).name;
			menu.setHeaderTitle(nameCategory);  
			menu.add(0, CONTEXT_MENU_CATEGORY_ADD, 0, "Add");
			menu.add(0, CONTEXT_MENU_CATEGORY_VIEW, 1, "View");  
			menu.add(0, CONTEXT_MENU_CATEGORY_EDIT, 2, "Edit");
			menu.add(0, CONTEXT_MENU_CATEGORY_DELETE, 3, "Delete");
			break;

		default:
			Log.e("Categories", "Context Menu type is not child or group");
			break;	

		}

	}  

	//Handles which methods are called when using the long presses menu
	@Override  
	public boolean onContextItemSelected(android.view.MenuItem item) {

		switch (item.getItemId()) {
		case CONTEXT_MENU_CATEGORY_ADD:
			Log.e("Categories","Category Add pressed") ;
			categoryAdd(item);
			return true;

		case CONTEXT_MENU_CATEGORY_VIEW:
			Log.e("Categories","Category View pressed");
			return true;

		case CONTEXT_MENU_CATEGORY_EDIT:
			Log.e("Categories","Category Edit pressed");
			return true;

		case CONTEXT_MENU_CATEGORY_DELETE:
			Log.e("Categories","Category Delete pressed");
			return true;

		case CONTEXT_MENU_SUBCATEGORY_VIEW:
			Log.e("Categories","SubCategory Delete pressed");
			return true;

		case CONTEXT_MENU_SUBCATEGORY_EDIT:
			Log.e("Categories","SubCategory Delete pressed");
			return true;

		case CONTEXT_MENU_SUBCATEGORY_DELETE:
			Log.e("Categories","SubCategory Delete pressed");
			return true;

		default:
			Log.e("Categories", "Context Menu type is not child or group");
			break;	

		}

		return super.onContextItemSelected(item);

	}  

	public class UserItemAdapter extends BaseExpandableListAdapter {
		private Cursor category;
		private ArrayList<Cursor> subcategory;
		private Context context;

		public UserItemAdapter(Context context, int textViewResourceId, Cursor cats, ArrayList<Cursor> subcats) {
			this.category = cats;
			this.subcategory = subcats;
			this.context = context;
		}

		//My method for getting a Category Record at a certain position
		public CategoryRecord getCategory(long id){
			Cursor group = category;

			group.moveToPosition((int) id);
			int IDColumn = group.getColumnIndex("CateID");
			int NameColumn = group.getColumnIndex("CateName");
			int NoteColumn = group.getColumnIndex("CateNote");

			String itemId = group.getString(IDColumn);
			String itemName = group.getString(NameColumn);
			String itemNote = group.getString(NoteColumn);

			CategoryRecord record = new CategoryRecord(itemId, itemName, itemNote);

			return record;
		}

		//My method for getting a Category Record at a certain position
		public SubCategoryRecord getSubCategory(int groupId, int childId){
			Cursor group = subcategory.get(groupId);

			int IDColumn = group.getColumnIndex("SubCateID");
			int ToIDColumn = group.getColumnIndex("ToCatID");
			int NameColumn = group.getColumnIndex("SubCateName");
			int NoteColumn = group.getColumnIndex("SubCateNote");

			group.moveToPosition(childId);
			String itemId = group.getString(0);
			String itemTo_id = group.getString(ToIDColumn);
			String itemSubname = group.getString(NameColumn);
			String itemNote = group.getString(NoteColumn);

			SubCategoryRecord record = new SubCategoryRecord(itemId, itemTo_id, itemSubname, itemNote);

			return record;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View v = convertView;
			Cursor user = category;

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
					Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
				}

				//Change Size of main field
				try{
					String DefaultSize = prefs.getString(context.getString(R.string.pref_key_account_nameSize), "16");
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
					Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}				

			}

			TextView name = (TextView) v.findViewById(R.id.category_name);
			int IDColumn = user.getColumnIndex("CateID");
			int NameColumn = user.getColumnIndex("CateName");
			int NoteColumn = user.getColumnIndex("CateNote");

			user.moveToPosition(groupPosition);
			String itemId = user.getString(0);
			String itemName = user.getString(NameColumn);
			String itemNote = user.getString(NoteColumn);
			//Log.e("getGroupView", "Found Category: " + itemName);

			if (itemName != null) {
				name.setText(itemName);
			}

			return v;
		}//end getView

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return null;
		}

		//NOT CORRECT...
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			Cursor temp = subcategory.get(groupPosition);
			temp.moveToPosition(childPosition);
			int IDColumn = temp.getColumnIndex("SubCateID");
			String itemId = temp.getString(0);

			return Long.parseLong(itemId);
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			//Log.e("Cagtegories","groupPos: " + groupPosition + " childPos: " + childPosition + " isLastChild: " + isLastChild + " parent: " + parent);

			View v = convertView;
			Cursor user = subcategory.get(groupPosition);

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean useDefaults = prefs.getBoolean("checkbox_default", true);

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.subcategory_item, null);

				//Change Background Colors
				try{
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.subcategory_item_layout);
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
					Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
				}

				//Change Size of main field
				try{
					String DefaultSize = prefs.getString(context.getString(R.string.pref_key_account_nameSize), "16");
					TextView t;
					t=(TextView)v.findViewById(R.id.subcategory_name);

					if(useDefaults){
						t.setTextSize(16);
					}
					else{
						t.setTextSize(Integer.parseInt(DefaultSize));
					}

				}
				catch(Exception e){
					Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

				try{
					int DefaultColor = prefs.getInt("key_account_nameColor", Color.parseColor("#000000"));
					TextView t;
					t=(TextView)v.findViewById(R.id.subcategory_name);

					if(useDefaults){
						t.setTextColor(Color.parseColor("#000000"));
					}
					else{
						t.setTextColor(DefaultColor);
					}

				}
				catch(Exception e){
					Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
				}

			}

			TextView name = (TextView) v.findViewById(R.id.subcategory_name);
			int IDColumn = user.getColumnIndex("SubCateID");
			int ToIDColumn = user.getColumnIndex("ToCatID");
			int NameColumn = user.getColumnIndex("SubCateName");
			int NoteColumn = user.getColumnIndex("SubCateNote");

			//user.moveToFirst();

			user.moveToPosition(childPosition);
			String itemId = user.getString(0);
			String itemTo_id = user.getString(ToIDColumn);
			String itemSubname = user.getString(NameColumn);
			String itemNote = user.getString(NoteColumn);
			Log.e("getChildView", "Found SubCategory: " + itemSubname);

			if (itemSubname != null) {
				name.setText(itemSubname);
			}

			return v;

		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			Cursor temp = subcategory.get(groupPosition);
			return temp.getCount();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return category.getCount();
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub

			return 0;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}

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

	//Close dialogs to prevent window leaks
	@Override
	public void onPause() {
//		if(alertDialogView!=null){
//			alertDialogView.dismiss();
//		}
//		if(alertDialogEdit!=null){
//			alertDialogEdit.dismiss();
//		}
//		if(alertDialogAdd!=null){
//			alertDialogAdd.dismiss();
//		}
		if(!cursorCategory.isClosed()){
			cursorCategory.close();
		}
		
		if(!cursorSubCategory.isClosed()){
			cursorSubCategory.close();
		}
		if(!resultsCursor.isEmpty()){
			resultsCursor.clear();
			resultsCursor = null;
		}
		
		//Close Database if Open
		if (myDB != null){
			myDB.close();
		}
		
		super.onPause();
	}
	
}//end category