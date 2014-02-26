/* Class that handles the Categories/SubCategories ExpandableListView seen in the Categories screen
 * Does everything from setting up the view to Add/Delete/Edit
 */

package com.databases.example;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.database.Cursor;
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

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;

public class Categories extends SherlockFragmentActivity implements OnSharedPreferenceChangeListener{
	private static DatabaseHelper dh = null;

	//NavigationDrawer
	private Drawer drawer;

	private ExpandableListView lvCategory = null;
	private static UserItemAdapter adapterCategory = null;

	//Constants for ContextMenu (Category)
	private final int CONTEXT_MENU_CATEGORY_ADD=1;
	private final int CONTEXT_MENU_CATEGORY_VIEW=2;
	private final int CONTEXT_MENU_CATEGORY_EDIT=3;
	private final int CONTEXT_MENU_CATEGORY_DELETE=4;

	//Constants for ContextMenu (SubCategory)
	private final int CONTEXT_MENU_SUBCATEGORY_VIEW=5;
	private final int CONTEXT_MENU_SUBCATEGORY_EDIT=6;
	private final int CONTEXT_MENU_SUBCATEGORY_DELETE=7;

	private ArrayList<CategoryRecord> resultsCategory = new ArrayList<CategoryRecord>();
	private ArrayList<SubCategoryRecord> resultsSubCategory = new ArrayList<SubCategoryRecord>();
	private ArrayList<Cursor> resultsCursor = new ArrayList<Cursor>();
	private Cursor cursorCategory;
	private Cursor cursorSubCategory;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		dh = new DatabaseHelper(this);

		setContentView(R.layout.categories);
		setTitle("Categories");

		//NavigationDrawer
		drawer = new Drawer(this);

		lvCategory = (ExpandableListView)this.findViewById(R.id.category_list);

		//Turn clicks on
		lvCategory.setClickable(true);
		lvCategory.setLongClickable(true);

		//Allows Context Menus for each item of the list view
		registerForContextMenu(lvCategory);

		//Set up a listener for changes in settings menu
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		categoryPopulate();
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

		cursorCategory = dh.getCategories(null,null,null,null);

		startManagingCursor(cursorCategory);
		int NameColumn = cursorCategory.getColumnIndex("CatName");
		int NoteColumn = cursorCategory.getColumnIndex("CatNote");

		cursorCategory.moveToFirst();
		if (cursorCategory != null) {
			if (cursorCategory.isFirst()) {
				do {
					String id = cursorCategory.getString(0);
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
				Log.d("Category-categoryPopulate", "No Categories found");
			}
		} 

		//Give the item adapter a list of all categories and subcategories
		adapterCategory = new UserItemAdapter(this, android.R.layout.simple_list_item_1, cursorCategory, resultsCursor);		
		lvCategory.setAdapter(adapterCategory);

	}//end of categoryPopulate

	//Method for filling subcategories
	public void subcategoryPopulate(String catId){		
		cursorSubCategory = dh.getSubCategories(null,"ToCatID="+catId,null,null);
		resultsCursor.add(cursorSubCategory);

		startManagingCursor(cursorSubCategory);
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
				Log.d("Category-subcategoryPopulate", "No Subcategories found");
			}
		} 

	}//end of subcategoryPopulate

	//Adding a new category
	public void categoryAdd(android.view.MenuItem item){			

		//SubCategory Add
		if(item != null){
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
			int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);

			DialogFragment newFragment = AddDialogFragment.newInstance(groupPos,childPos);
			newFragment.show(getSupportFragmentManager(), "dialogAdd");

		}
		//CategoryAdd
		else{

			DialogFragment newFragment = AddDialogFragment.newInstance();
			newFragment.show(getSupportFragmentManager(), "dialogAdd");
		}

	}//end of showCategoryAdd

	//Delete Category
	public void categoryDelete(android.view.MenuItem item){
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);

		if(type==ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			String subcategoryID = adapterCategory.getSubCategory(groupPos, childPos).id;
			Uri uri = Uri.parse(MyContentProvider.SUBCATEGORIES_URI + "/" + subcategoryID);

			getContentResolver().delete(uri,"SubCatID="+subcategoryID, null);

			Log.d("Categories-categoryDelete", "Deleting " + adapterCategory.getSubCategory(groupPos, childPos).name + " id:" + subcategoryID);
		}
		else if(type==ExpandableListView.PACKED_POSITION_TYPE_GROUP){
			String categoryID = adapterCategory.getCategory(groupPos).id;

			//Delete category
			Uri uri = Uri.parse(MyContentProvider.CATEGORIES_URI + "/" + categoryID);
			getContentResolver().delete(uri,"CatID="+categoryID, null);

			//Delete remaining subcategories
			uri = Uri.parse(MyContentProvider.SUBCATEGORIES_URI + "/" + 0);
			getContentResolver().delete(uri,"ToCatID="+categoryID, null);

			Log.d("Categories-categoryDelete", "Deleting " + adapterCategory.getCategory(groupPos).name + " id:" + categoryID);
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

		DialogFragment newFragment = EditDialogFragment.newInstance(groupPos,childPos,type);
		newFragment.show(getSupportFragmentManager(), "dialogEdit");
	}

	//View Category
	public void categoryView(android.view.MenuItem item){
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		final int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		final int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		final int type = ExpandableListView.getPackedPositionType(info.packedPosition);

		DialogFragment newFragment = ViewDialogFragment.newInstance(groupPos,childPos,type);
		newFragment.show(getSupportFragmentManager(), "dialogView");
	}

	//For ActionBar Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		//Show Search
		MenuItem menuSearch = menu.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_search, com.actionbarsherlock.view.Menu.NONE, "Search");
		menuSearch.setIcon(android.R.drawable.ic_menu_search);
		menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menuSearch.setActionView(new SearchView(getSupportActionBar().getThemedContext()));
		
		SearchWidget searchWidget = new SearchWidget(this,menuSearch.getActionView());

		//Show Add Icon
		MenuItem menuAdd = menu.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_add, com.actionbarsherlock.view.Menu.NONE, "Add");
		menuAdd.setIcon(android.R.drawable.ic_menu_add);
		menuAdd.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	//For ActionBar Menu Items (and home icon)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			drawer.toggle();
			break;
			
		case R.id.account_menu_add:    
			categoryAdd(null);
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
			Log.e("Categories-onCreateContextMenu", "Context Menu type is not child or group");
			break;	
		}

	}

	//Handles which methods are called when using the long presses menu
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		switch (item.getItemId()) {
		case CONTEXT_MENU_CATEGORY_ADD:
			categoryAdd(item);
			return true;

		case CONTEXT_MENU_CATEGORY_VIEW:
			categoryView(item);
			return true;

		case CONTEXT_MENU_CATEGORY_EDIT:
			categoryEdit(item);
			return true;

		case CONTEXT_MENU_CATEGORY_DELETE:
			categoryDelete(item);
			return true;

		case CONTEXT_MENU_SUBCATEGORY_VIEW:
			categoryView(item);
			return true;

		case CONTEXT_MENU_SUBCATEGORY_EDIT:
			categoryEdit(item);
			return true;

		case CONTEXT_MENU_SUBCATEGORY_DELETE:
			categoryDelete(item);
			return true;

		default:
			Log.e("Categories-onContextItemSelected", "Context Menu type is not child or group");
			break;
		}

		return super.onContextItemSelected(item);
	}  

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Toast.makeText(this, "Options Just Changed: Categories.Java", Toast.LENGTH_SHORT).show();
		//categoryPopulate();
	}

	public class UserItemAdapter extends BaseExpandableListAdapter{
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
			int NameColumn = group.getColumnIndex("CatName");
			int NoteColumn = group.getColumnIndex("CatNote");

			String itemId = group.getString(0);
			String itemName = group.getString(NameColumn);
			String itemNote = group.getString(NoteColumn);

			CategoryRecord record = new CategoryRecord(itemId, itemName, itemNote);
			return record;
		}

		//My method for getting a Category Record at a certain position
		public SubCategoryRecord getSubCategory(int groupId, int childId){
			Cursor group = subcategory.get(groupId);

			group.moveToPosition(childId);
			int ToIDColumn = group.getColumnIndex("ToCatID");
			int NameColumn = group.getColumnIndex("SubCatName");
			int NoteColumn = group.getColumnIndex("SubCatNote");

			//Log.e("HERE", "columns " + IDColumn + " " + ToIDColumn + " " + NameColumn + " " + NoteColumn);
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

			CategoryViewHolder viewHolder;

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_category", true);

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.category_item, null);

				viewHolder = new CategoryViewHolder();
				viewHolder.tvName = (TextView) v.findViewById(R.id.category_name);				
				viewHolder.tvNote = (TextView) v.findViewById(R.id.category_note);

				v.setTag(viewHolder);
			}
			else {
				viewHolder = (CategoryViewHolder) v.getTag();
			}

			//Change Background Colors
			try{
				if(!useDefaults){
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.category_layout);
					int startColor = prefs.getInt("key_category_startBackgroundColor", Color.parseColor("#FFFFFF"));
					int endColor = prefs.getInt("key_category_endBackgroundColor", Color.parseColor("#FFFFFF"));
					GradientDrawable defaultGradient = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {startColor,endColor});
					l.setBackgroundDrawable(defaultGradient);
				}


			}
			catch(Exception e){
				Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
			}

			//Change Size/Color of main field
			try{
				String DefaultSize = prefs.getString(context.getString(R.string.pref_key_category_nameSize), "24");

				if(useDefaults){
					viewHolder.tvName.setTextSize(24);
				}
				else{
					viewHolder.tvName.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_category_nameColor", Color.parseColor("#222222"));

				if(useDefaults){
					viewHolder.tvName.setTextColor(Color.parseColor("#222222"));
				}
				else{
					viewHolder.tvName.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			int NameColumn = user.getColumnIndex("CatName");
			int NoteColumn = user.getColumnIndex("CatNote");

			user.moveToPosition(groupPosition);
			String itemId = user.getString(0);
			String itemName = user.getString(NameColumn);
			String itemNote = user.getString(NoteColumn);
			//Log.d("getGroupView", "Found Category: " + itemName);

			//For User-Defined Field Visibility
			if(useDefaults||prefs.getBoolean("checkbox_category_nameField", true)){
				viewHolder.tvName.setVisibility(View.VISIBLE);
				viewHolder.tvName.setText(itemName);
			}
			else{
				viewHolder.tvName.setVisibility(View.GONE);
			}

			if(prefs.getBoolean("checkbox_category_noteField", false) && !useDefaults && itemNote!=null){
				viewHolder.tvNote.setVisibility(View.VISIBLE);
				viewHolder.tvNote.setText(itemNote);
			}
			else{
				viewHolder.tvNote.setVisibility(View.GONE);
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
			Cursor temp = subcategory.get(groupPosition);
			temp.moveToPosition(childPosition);
			String itemId = temp.getString(0);

			//Log.d("getChildID", "returning " + Long.parseLong(itemId));

			return Long.parseLong(itemId);
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			//Log.d("Cagtegories","groupPos: " + groupPosition + " childPos: " + childPosition + " isLastChild: " + isLastChild + " parent: " + parent);

			View v = convertView;
			Cursor user = subcategory.get(groupPosition);
			SubCategoryViewHolder viewHolder;

			//For Custom View Properties
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_subcategory", true);

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.subcategory_item, null);

				viewHolder = new SubCategoryViewHolder();
				viewHolder.tvName = (TextView) v.findViewById(R.id.subcategory_name);
				viewHolder.tvNote = (TextView) v.findViewById(R.id.subcategory_note);

				v.setTag(viewHolder);
			}

			else {
				viewHolder = (SubCategoryViewHolder) v.getTag();
			}

			//Change Background Colors
			try{
				if(!useDefaults){
					LinearLayout l;
					l=(LinearLayout)v.findViewById(R.id.subcategory_item_layout);
					int startColor = prefs.getInt("key_subcategory_startBackgroundColor", Color.parseColor("#FFFFFF"));
					int endColor = prefs.getInt("key_subcategory_endBackgroundColor", Color.parseColor("#FFFFFF"));
					GradientDrawable defaultGradient = new GradientDrawable(
							GradientDrawable.Orientation.BOTTOM_TOP,
							new int[] {startColor,endColor});
					l.setBackgroundDrawable(defaultGradient);
				}
			}
			catch(Exception e){
				Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
			}

			//Change Size/color of main field
			try{
				String DefaultSize = prefs.getString(context.getString(R.string.pref_key_subcategory_nameSize), "24");

				if(useDefaults){
					viewHolder.tvName.setTextSize(24);
				}
				else{
					viewHolder.tvName.setTextSize(Integer.parseInt(DefaultSize));
				}

			}
			catch(Exception e){
				Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			try{
				int DefaultColor = prefs.getInt("key_subcategory_nameColor", Color.parseColor("#000000"));

				if(useDefaults){
					viewHolder.tvName.setTextColor(Color.parseColor("#000000"));
				}
				else{
					viewHolder.tvName.setTextColor(DefaultColor);
				}

			}
			catch(Exception e){
				Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
			}

			int ToIDColumn = user.getColumnIndex("ToCatID");
			int NameColumn = user.getColumnIndex("SubCatName");
			int NoteColumn = user.getColumnIndex("SubCatNote");

			user.moveToPosition(childPosition);
			String itemId = user.getString(0);
			String itemTo_id = user.getString(ToIDColumn);
			String itemSubname = user.getString(NameColumn);
			String itemNote = user.getString(NoteColumn);
			//Log.d("getChildView", "Found SubCategory: " + itemSubname);

			if(useDefaults||prefs.getBoolean("checkbox_subcategory_nameField", true)){
				viewHolder.tvName.setVisibility(View.VISIBLE);
				viewHolder.tvName.setText(itemSubname);
			}
			else{
				viewHolder.tvName.setVisibility(View.GONE);
			}
			if(prefs.getBoolean("checkbox_subcategory_noteField", false) && !useDefaults && itemNote!=null){
				viewHolder.tvNote.setVisibility(View.VISIBLE);
				viewHolder.tvNote.setText(itemNote);
			}
			else{
				viewHolder.tvNote.setVisibility(View.GONE);
			}

			return v;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
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
			return true;
		}

	} //end of UserItemAdapter

	//ViewHolder for Categories
	static class CategoryViewHolder{
		TextView tvName;
		TextView tvNote;
	}

	//ViewHolder for SubCategories
	static class SubCategoryViewHolder{
		TextView tvName;
		TextView tvCategory;
		TextView tvNote;
	}

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

	@Override
	public void onDestroy() {
		if(dh!=null){
			dh.close();
		}
		super.onDestroy();
	}

	//Class that handles view fragment
	public static class ViewDialogFragment extends SherlockDialogFragment {
		public static ViewDialogFragment newInstance(int gPos, int cPos,int t) {
			ViewDialogFragment frag = new ViewDialogFragment();
			Bundle args = new Bundle();
			args.putInt("group", gPos);
			args.putInt("child", cPos);
			args.putInt("type", t);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(this.getActivity());
			final View categoryStatsView = li.inflate(R.layout.category_stats, null);

			final int type = getArguments().getInt("type");
			final int groupPos = getArguments().getInt("group");
			final int childPos = getArguments().getInt("child");

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setView(categoryStatsView);
			alertDialogBuilder.setTitle("View Category");
			alertDialogBuilder.setCancelable(true);

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

			return alertDialogBuilder.create();
		}

	}

	//Class that handles edit fragment
	public static class EditDialogFragment extends SherlockDialogFragment {
		public static EditDialogFragment newInstance(int gPos, int cPos,int t) {
			EditDialogFragment frag = new EditDialogFragment();
			Bundle args = new Bundle();
			args.putInt("group", gPos);
			args.putInt("child", cPos);
			args.putInt("type", t);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(this.getActivity());
			final View categoryAddView = li.inflate(R.layout.category_add, null);

			final int type = getArguments().getInt("type");
			final int groupPos = getArguments().getInt("group");
			final int childPos = getArguments().getInt("child");

			SubCategoryRecord subrecord = null;
			CategoryRecord record = null;

			final EditText editName = (EditText)categoryAddView.findViewById(R.id.EditCategoryName);
			final EditText editNote = (EditText)categoryAddView.findViewById(R.id.EditCategoryNote);

			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());

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

			alertDialogBuilder.setView(categoryAddView);
			alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Done",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					String newName = editName.getText().toString().trim();
					String newNote = editNote.getText().toString().trim();

					try{
						if(type==ExpandableListView.PACKED_POSITION_TYPE_CHILD){
							SubCategoryRecord oldRecord = adapterCategory.getSubCategory(groupPos, childPos);

							ContentValues subcategoryValues=new ContentValues();
							subcategoryValues.put("SubCatID",oldRecord.id);
							subcategoryValues.put("ToCatID",oldRecord.catId);
							subcategoryValues.put("SubCatName",newName);
							subcategoryValues.put("SubCatNote",newNote);
							getActivity().getContentResolver().update(Uri.parse(MyContentProvider.SUBCATEGORIES_URI+"/"+oldRecord.id), subcategoryValues,"SubCatID ="+oldRecord.id,null);
						}
						else if(type==ExpandableListView.PACKED_POSITION_TYPE_GROUP){
							CategoryRecord oldRecord = adapterCategory.getCategory(groupPos);

							ContentValues categoryValues=new ContentValues();
							categoryValues.put("CatID",oldRecord.id);
							categoryValues.put("SubCatName",newName);
							categoryValues.put("SubCatNote",newNote);
							getActivity().getContentResolver().update(Uri.parse(MyContentProvider.CATEGORIES_URI+"/"+oldRecord.id), categoryValues,"CatID ="+oldRecord.id,null);
						}
					}
					catch(Exception e){
						Log.e("Categories-EditDialog", "Error editing Categories");
					}

					//Refresh the categories list
					((Categories) getActivity()).categoryPopulate();

				}
			})
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});		

			return alertDialogBuilder.create();
		}

	}

	//Class that handles add fragment
	public static class AddDialogFragment extends SherlockDialogFragment {

		public static AddDialogFragment newInstance() {
			AddDialogFragment frag = new AddDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		public static AddDialogFragment newInstance(int gPos, int cPos) {
			AddDialogFragment frag = new AddDialogFragment();
			Bundle args = new Bundle();
			args.putInt("group", gPos);
			args.putInt("child", cPos);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			boolean isCategory = true;
			String itemID = "0";
			CategoryRecord catRecord;

			if(!this.getArguments().isEmpty()){
				isCategory = false;
				int groupPos = getArguments().getInt("group");
				int childPos = getArguments().getInt("child");
				catRecord = adapterCategory.getCategory(groupPos);
				itemID = catRecord.id;
				//Log.e("categoryAdd", "itemID: " + catRecord.id);
			}

			final boolean isCat = isCategory;
			final String catID = itemID;

			LayoutInflater li = LayoutInflater.from(this.getActivity());
			final View categoryAddView = li.inflate(R.layout.category_add, null);

			final EditText editName = (EditText)categoryAddView.findViewById(R.id.EditCategoryName);
			final EditText editNote = (EditText)categoryAddView.findViewById(R.id.EditCategoryNote);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
			alertDialogBuilder.setView(categoryAddView);

			//Set title
			if(isCat){
				alertDialogBuilder.setTitle("Create A Category");
			}
			else{
				alertDialogBuilder.setTitle("Create A SubCategory");			
			}

			//Set dialog message
			alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Add",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					String name = editName.getText().toString().trim();
					String note = editNote.getText().toString().trim();

					try{
						//Add a category
						if(isCat){
							ContentValues categoryValues=new ContentValues();
							categoryValues.put("CatName",name);
							categoryValues.put("CatNote",note);
							getActivity().getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);
						}
						//Add a subcategory
						else{
							ContentValues subcategoryValues=new ContentValues();
							subcategoryValues.put("ToCatID",catID);
							subcategoryValues.put("SubCatName",name);
							subcategoryValues.put("SubCatNote",note);
							getActivity().getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);
						}

					}
					catch(Exception e){
						Log.e("Categories-AddDialog", "Error adding Categories. e = " + e);
					}

					//Refresh the categories list
					((Categories) getActivity()).categoryPopulate();

				}
			})
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			return alertDialogBuilder.create();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawer.getDrawerToggle().syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawer.getDrawerToggle().onConfigurationChanged(newConfig);
	}
	
}//end Categories