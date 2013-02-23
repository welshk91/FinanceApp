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
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.slidingmenu.lib.SlidingMenu;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
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

	//Need to be global so I can dismiss and avoid leaks
	AlertDialog alertDialogView;
	AlertDialog alertDialogAdd;
	AlertDialog alertDialogEdit;

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

		cursorCategory = myDB.query(tblCategory, new String[] { "CatID", "CatName", "CatNote"}, null,
				null, null, null, null);

		startManagingCursor(cursorCategory);
		int IDColumn = cursorCategory.getColumnIndex("CatID");
		int NameColumn = cursorCategory.getColumnIndex("CatName");
		int NoteColumn = cursorCategory.getColumnIndex("CatNote");

		cursorCategory.moveToFirst();
		if (cursorCategory != null) {
			if (cursorCategory.isFirst()) {
				do {
					String id = cursorCategory.getString(IDColumn);
					String name = cursorCategory.getString(NameColumn);
					String note = cursorCategory.getString(NoteColumn);

					CategoryRecord entry = new CategoryRecord(id, name, note);
					//Log.d("Category", "Added Category: " + id + " " + name + " " + note);
					subcategoryPopulate(id);
					resultsCategory.add(entry);

				} while (cursorCategory.moveToNext());
			}

			else {
				//No Results Found
				noResult.setVisibility(View.VISIBLE);
				Log.d("Category", "No Categories found");
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

		cursorSubCategory = myDB.query(tblSubCategory, new String[] { "SubCatID", "ToCatID", "SubCatName", "SubCatNote"}, "ToCatID = " + catId,
				null, null, null, null);

		resultsCursor.add(cursorSubCategory);

		startManagingCursor(cursorSubCategory);
		int IDColumn = cursorSubCategory.getColumnIndex("SubCatID");
		int ToIDColumn = cursorSubCategory.getColumnIndex("ToCatID");
		int NameColumn = cursorSubCategory.getColumnIndex("SubCatName");
		int NoteColumn = cursorSubCategory.getColumnIndex("SubCatNote");

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
				Log.d("Category", "No Subcategories found");
			}
		} 

	}//end of subcategoryPopulate

	//Adding a new category
	public void categoryAdd(android.view.MenuItem item){			
		boolean isCategory = true;
		String itemID = "0";
		CategoryRecord catRecord;

		if(item != null){
			isCategory = false;
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
			int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
			catRecord = adapterCategory.getCategory(groupPos);
			itemID = catRecord.id;
			//Log.e("categoryAdd", "itemID: " + catRecord.id);
		}

		final boolean isCat = isCategory;
		final String catID = itemID;

		LayoutInflater li = LayoutInflater.from(this);
		final View categoryAddView = li.inflate(R.layout.category_add, null);

		final EditText editName = (EditText)categoryAddView.findViewById(R.id.EditCategoryName);
		final EditText editNote = (EditText)categoryAddView.findViewById(R.id.EditCategoryNote);

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
				String name = editName.getText().toString().trim();
				String note = editNote.getText().toString().trim();

				//Create database and open
				myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

				try{
					//Add a category
					if(isCat){
						//Log.e("Category Add", "Adding a normal category : " + category);

						ContentValues categoryValues=new ContentValues();
						categoryValues.put("CatName",name);
						categoryValues.put("CatNote",note);

						myDB.insert(tblCategory, null, categoryValues);

					}
					//Add a subcategory
					else{
						//Log.e("Category Add", "Adding a subcategory : " + catID + name + note);

						ContentValues subcategoryValues=new ContentValues();
						subcategoryValues.put("ToCatID",catID);
						subcategoryValues.put("SubCatName",name);
						subcategoryValues.put("SubCatNote",note);

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
		alertDialogAdd = alertDialogBuilder.create();

		// show it
		alertDialogAdd.show();

	}//end of showCategoryAdd

	//Delete Category
	public void categoryDelete(android.view.MenuItem item){
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);

		//Open Database
		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		if(type==ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			String subcategoryID = adapterCategory.getSubCategory(groupPos, childPos).id;
			String sqlDeleteSubCategory = "DELETE FROM " + tblSubCategory + 
					" WHERE SubCatID = " + subcategoryID;

			Log.d("categoryDelete", "Deleting " + adapterCategory.getSubCategory(groupPos, childPos).name + " id:" + subcategoryID);

			myDB.execSQL(sqlDeleteSubCategory);

		}
		else if(type==ExpandableListView.PACKED_POSITION_TYPE_GROUP){
			String categoryID = adapterCategory.getCategory(groupPos).id;
			String sqlDeleteCategory = "DELETE FROM " + tblCategory + 
					" WHERE CatID = " + categoryID;
			String sqlDeleteSubCategories = "DELETE FROM " + tblSubCategory + 
					" WHERE ToCatID = " + categoryID;

			Log.d("categoryDelete", "Deleting " + adapterCategory.getCategory(groupPos).name + " id:" + categoryID);

			myDB.execSQL(sqlDeleteCategory);
			myDB.execSQL(sqlDeleteSubCategories);	

		}

		//Close Database if Opened
		if (myDB != null){
			myDB.close();
		}

		//Refresh the categories list
		categoryPopulate();

	}//end categoryDelete


	//Edit Category
	public void categoryEdit(android.view.MenuItem item){
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		final int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		final int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		final int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		SubCategoryRecord subrecord = null;
		CategoryRecord record = null;

		LayoutInflater li = LayoutInflater.from(this);
		final View categoryAddView = li.inflate(R.layout.category_add, null);

		final EditText editName = (EditText)categoryAddView.findViewById(R.id.EditCategoryName);
		final EditText editNote = (EditText)categoryAddView.findViewById(R.id.EditCategoryNote);

		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);


		if(type==ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			subrecord = adapterCategory.getSubCategory(groupPos, childPos);
			alertDialogBuilder.setTitle("Editing " + subrecord.name);
			editName.setText(subrecord.name);
			editNote.setText(subrecord.note);
		}
		else if(type==ExpandableListView.PACKED_POSITION_TYPE_GROUP){
			record = adapterCategory.getCategory(groupPos);
			alertDialogBuilder.setTitle("Editing " + record.name);
			editName.setText(record.name);
			editNote.setText(record.note);			
		}

		// set account_add.xml to AlertDialog builder
		alertDialogBuilder.setView(categoryAddView);

		// set dialog message
		alertDialogBuilder
		.setCancelable(true)
		.setPositiveButton("Done",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				String newName = editName.getText().toString().trim();
				String newNote = editNote.getText().toString().trim();

				try{
					myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

					if(type==ExpandableListView.PACKED_POSITION_TYPE_CHILD){
						SubCategoryRecord oldRecord = adapterCategory.getSubCategory(groupPos, childPos);
						String sqlDeleteSubCategory = "DELETE FROM " + tblSubCategory + 
								" WHERE SubCatID = " + oldRecord.id;

						Log.d("categoryEdit", "Deleting " + oldRecord.name + " id:" + oldRecord.id);
						myDB.execSQL(sqlDeleteSubCategory);

						//Make new record with same ID
						ContentValues valuesSubCategory=new ContentValues();
						valuesSubCategory.put("SubCatID",oldRecord.id);
						valuesSubCategory.put("ToCatID",oldRecord.catId);
						valuesSubCategory.put("SubCatName",newName);
						valuesSubCategory.put("SubCatNote",newNote);

						myDB.insert(tblSubCategory, null, valuesSubCategory);

					}
					else if(type==ExpandableListView.PACKED_POSITION_TYPE_GROUP){
						CategoryRecord oldRecord = adapterCategory.getCategory(groupPos);
						String sqlDeleteCategory = "DELETE FROM " + tblCategory + 
								" WHERE CatID = " + oldRecord.id;

						Log.d("categoryEdit", "Deleting " + oldRecord.name + " id:" + oldRecord.id);
						myDB.execSQL(sqlDeleteCategory);

						//Make new record with same ID
						ContentValues valuesCategory=new ContentValues();
						valuesCategory.put("CatID",oldRecord.id);
						valuesCategory.put("CatName",newName);
						valuesCategory.put("CatNote",newNote);

						myDB.insert(tblCategory, null, valuesCategory);

					}

					//Make sure Database is closed
					if (myDB != null){
						myDB.close();
					}

				}
				catch(Exception e){
					Log.e("Categories", "Error editing Categories");
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
		alertDialogEdit = alertDialogBuilder.create();

		// show it
		alertDialogEdit.show();

		//Refresh the categories list
		categoryPopulate();

	}

	//View Category
	public void categoryView(android.view.MenuItem item){
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		final int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		final int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		final int type = ExpandableListView.getPackedPositionType(info.packedPosition);

		LayoutInflater li = LayoutInflater.from(this);
		final View categoryStatsView = li.inflate(R.layout.category_stats, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);

		// set xml to AlertDialog builder
		alertDialogBuilder.setView(categoryStatsView);

		//set Title
		alertDialogBuilder.setTitle("View Category");

		// set dialog message
		alertDialogBuilder
		.setCancelable(true);

		// create alert dialog
		alertDialogView = alertDialogBuilder.create();

		if(type==ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			SubCategoryRecord record = adapterCategory.getSubCategory(groupPos, childPos);

			//Set Statistics
			TextView statsName = (TextView)categoryStatsView.findViewById(R.id.TextCategoryName);
			statsName.setText(record.name);
			TextView statsValue = (TextView)categoryStatsView.findViewById(R.id.TextCategoryParent);
			statsValue.setText(record.catId);
			TextView statsDate = (TextView)categoryStatsView.findViewById(R.id.TextCategoryNote);
			statsDate.setText(record.note);			

		}
		else if(type==ExpandableListView.PACKED_POSITION_TYPE_GROUP){
			CategoryRecord record = adapterCategory.getCategory(groupPos);

			//Set Statistics
			TextView statsName = (TextView)categoryStatsView.findViewById(R.id.TextCategoryName);
			statsName.setText(record.name);
			TextView statsValue = (TextView)categoryStatsView.findViewById(R.id.TextCategoryParent);
			statsValue.setText("None");
			TextView statsDate = (TextView)categoryStatsView.findViewById(R.id.TextCategoryNote);
			statsDate.setText(record.note);			

		}

		// show it
		alertDialogView.show();

	}



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
			String nameSubCategory = adapterCategory.getSubCategory(groupPos,childPos).name;
			menu.setHeaderTitle(nameSubCategory);
			menu.add(0, CONTEXT_MENU_SUBCATEGORY_VIEW, 1, "View");  
			menu.add(0, CONTEXT_MENU_SUBCATEGORY_EDIT, 2, "Edit");
			menu.add(0, CONTEXT_MENU_SUBCATEGORY_DELETE, 3, "Delete");
			break;

		case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
			String nameCategory = adapterCategory.getCategory(groupPos).name;
			menu.add(1, CONTEXT_MENU_CATEGORY_ADD, 0, "Add");
			menu.setHeaderTitle(nameCategory);
			menu.add(1, CONTEXT_MENU_CATEGORY_VIEW, 1, "View");  
			menu.add(1, CONTEXT_MENU_CATEGORY_EDIT, 2, "Edit");
			menu.add(1, CONTEXT_MENU_CATEGORY_DELETE, 3, "Delete");
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
			//Log.e("Categories","Category Add pressed");
			categoryAdd(item);
			return true;

		case CONTEXT_MENU_CATEGORY_VIEW:
			//Log.e("Categories","Category View pressed");
			categoryView(item);
			return true;

		case CONTEXT_MENU_CATEGORY_EDIT:
			//Log.e("Categories","Category Edit pressed");
			categoryEdit(item);
			return true;

		case CONTEXT_MENU_CATEGORY_DELETE:
			categoryDelete(item);
			return true;

		case CONTEXT_MENU_SUBCATEGORY_VIEW:
			//Log.e("Categories","SubCategory View pressed");
			categoryView(item);
			return true;

		case CONTEXT_MENU_SUBCATEGORY_EDIT:
			//Log.e("Categories","SubCategory Edit pressed");
			categoryEdit(item);
			return true;

		case CONTEXT_MENU_SUBCATEGORY_DELETE:
			categoryDelete(item);
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
			int IDColumn = group.getColumnIndex("CatID");
			int NameColumn = group.getColumnIndex("CatName");
			int NoteColumn = group.getColumnIndex("CatNote");

			//Log.e("HERE", "columns " + IDColumn + " " + NameColumn + " " + NoteColumn);
			String itemId = group.getString(IDColumn);
			String itemName = group.getString(NameColumn);
			String itemNote = group.getString(NoteColumn);

			CategoryRecord record = new CategoryRecord(itemId, itemName, itemNote);
			//group.close();

			return record;
		}

		//My method for getting a Category Record at a certain position
		public SubCategoryRecord getSubCategory(int groupId, int childId){
			Cursor group = subcategory.get(groupId);

			group.moveToPosition(childId);
			int IDColumn = group.getColumnIndex("SubCatID");
			int ToIDColumn = group.getColumnIndex("ToCatID");
			int NameColumn = group.getColumnIndex("SubCatName");
			int NoteColumn = group.getColumnIndex("SubCatNote");

			Log.e("HERE", "columns " + IDColumn + " " + ToIDColumn + " " + NameColumn + " " + NoteColumn);
			String itemId = group.getString(0);
			String itemTo_id = group.getString(ToIDColumn);
			String itemSubname = group.getString(NameColumn);
			String itemNote = group.getString(NoteColumn);

			SubCategoryRecord record = new SubCategoryRecord(itemId, itemTo_id, itemSubname, itemNote);
			//group.close();

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
			int IDColumn = user.getColumnIndex("CatID");
			int NameColumn = user.getColumnIndex("CatName");
			int NoteColumn = user.getColumnIndex("CatNote");

			user.moveToPosition(groupPosition);
			String itemId = user.getString(IDColumn);
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

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			Cursor temp = subcategory.get(groupPosition);
			temp.moveToPosition(childPosition);
			int IDColumn = temp.getColumnIndex("SubCatID");
			String itemId = temp.getString(0);

			//Log.e("getChildID", "returning " + Long.parseLong(itemId));

			return Long.parseLong(itemId);

		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			//Log.d("Cagtegories","groupPos: " + groupPosition + " childPos: " + childPosition + " isLastChild: " + isLastChild + " parent: " + parent);

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
			int IDColumn = user.getColumnIndex("SubCatID");
			int ToIDColumn = user.getColumnIndex("ToCatID");
			int NameColumn = user.getColumnIndex("SubCatName");
			int NoteColumn = user.getColumnIndex("SubCatNote");

			user.moveToPosition(childPosition);
			String itemId = user.getString(IDColumn);
			String itemTo_id = user.getString(ToIDColumn);
			String itemSubname = user.getString(NameColumn);
			String itemNote = user.getString(NoteColumn);
			//Log.d("getChildView", "Found SubCategory: " + itemSubname);

			if (itemSubname != null) {
				name.setText(itemSubname);
			}

			//user.close();
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
		if(alertDialogView!=null){
			alertDialogView.dismiss();
		}
		if(alertDialogEdit!=null){
			alertDialogEdit.dismiss();
		}
		if(alertDialogAdd!=null){
			alertDialogAdd.dismiss();
		}

		//if(!cursorCategory.isClosed()){
		//	cursorCategory.close();
		//}
		//if(!cursorSubCategory.isClosed()){
		//	cursorSubCategory.close();
		//}
		//if(!resultsCursor.isEmpty()){
		//	resultsCursor.clear();
		//	resultsCursor = null;
		//}

		super.onPause();
	}

}//end category