/* Class that handles the Categories/SubCategories ExpandableListView seen in the Categories screen
 * Does everything from setting up the view to Add/Delete/Edit
 */

package com.databases.example.app;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.MyContentProvider;
import com.databases.example.data.SearchWidget;
import com.databases.example.view.CategoriesListViewAdapter;
import com.databases.example.view.CategoryAddFragment;
import com.databases.example.view.CategoryEditFragment;
import com.databases.example.view.CategoryViewFragment;
import com.databases.example.view.Drawer;

import java.util.ArrayList;

public class CategoriesActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int CATEGORIES_LOADER = 8675309;
    public static final int SUBCATEGORIES_LOADER = 867;

    //NavigationDrawer
    private Drawer drawer;

    public static CategoriesListViewAdapter adapterCategory = null;

    //Constants for ContextMenu (Category)
    private final int CONTEXT_MENU_CATEGORY_ADD = 1;
    private final int CONTEXT_MENU_CATEGORY_VIEW = 2;
    private final int CONTEXT_MENU_CATEGORY_EDIT = 3;
    private final int CONTEXT_MENU_CATEGORY_DELETE = 4;

    //Constants for ContextMenu (SubCategory)
    private final int CONTEXT_MENU_SUBCATEGORY_VIEW = 5;
    private final int CONTEXT_MENU_SUBCATEGORY_EDIT = 6;
    private final int CONTEXT_MENU_SUBCATEGORY_DELETE = 7;

    private static DatabaseHelper dh = null;
    private final ArrayList<Cursor> resultsCursor = new ArrayList<Cursor>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dh = new DatabaseHelper(this);

        setContentView(R.layout.categories);
        setTitle("CategoriesActivity");

        //NavigationDrawer
        drawer = new Drawer(this);

        ExpandableListView lvCategory = (ExpandableListView) this.findViewById(R.id.category_list);

        //Turn clicks on
        lvCategory.setClickable(true);
        lvCategory.setLongClickable(true);

        //Allows Context Menus for each item of the list view
        registerForContextMenu(lvCategory);

        //Set up a listener for changes in settings menu
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        getSupportLoaderManager().initLoader(CATEGORIES_LOADER, null, this);

        adapterCategory = new CategoriesListViewAdapter(this, 0, null, resultsCursor);
        lvCategory.setAdapter(adapterCategory);
    }

    //Method for filling subcategories
    public void subcategoryPopulate(int catId) {
        Cursor cursorSubCategory = dh.getSubCategories(null, DatabaseHelper.SUBCATEGORY_CAT_ID + "=" + catId, null, null);
        resultsCursor.add(cursorSubCategory);
    }//end of subcategoryPopulate

    //Adding a new category
    private void categoryAdd(android.view.MenuItem item) {

        //SubCategory Add
        if (item != null) {
            ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);

            DialogFragment newFragment = CategoryAddFragment.newInstance(groupPos, childPos);
            newFragment.show(getSupportFragmentManager(), "dialogAdd");

        }
        //CategoryAdd
        else {
            DialogFragment newFragment = CategoryAddFragment.newInstance();
            newFragment.show(getSupportFragmentManager(), "dialogAdd");
        }

    }//end of showCategoryAdd

    //Delete Category
    private void categoryDelete(android.view.MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
        int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int subcategoryID = adapterCategory.getSubCategory(groupPos, childPos).id;
            Uri uri = Uri.parse(MyContentProvider.SUBCATEGORIES_URI + "/" + subcategoryID);

            getContentResolver().delete(uri, DatabaseHelper.SUBCATEGORY_ID + "=" + subcategoryID, null);

            Log.d(getClass().getSimpleName(), "Deleting " + adapterCategory.getSubCategory(groupPos, childPos).name + " id:" + subcategoryID);
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            int categoryID = adapterCategory.getCategory(groupPos).id;

            //Delete category
            Uri uri = Uri.parse(MyContentProvider.CATEGORIES_URI + "/" + categoryID);
            getContentResolver().delete(uri, DatabaseHelper.CATEGORY_ID + "=" + categoryID, null);

            //Delete remaining subcategories
            uri = Uri.parse(MyContentProvider.SUBCATEGORIES_URI + "/" + 0);
            getContentResolver().delete(uri, DatabaseHelper.SUBCATEGORY_CAT_ID + "=" + categoryID, null);

            Log.d(getClass().getSimpleName(), "Deleting " + adapterCategory.getCategory(groupPos).name + " id:" + categoryID);
        }

        getSupportLoaderManager().restartLoader(CATEGORIES_LOADER, null, this);

    }//end categoryDelete

    //Edit Category
    private void categoryEdit(android.view.MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
        final int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        final int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        final int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        DialogFragment newFragment = CategoryEditFragment.newInstance(groupPos, childPos, type);
        newFragment.show(getSupportFragmentManager(), "dialogEdit");
    }

    //View Category
    private void categoryView(android.view.MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
        final int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        final int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        final int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        DialogFragment newFragment = CategoryViewFragment.newInstance(groupPos, childPos, type);
        newFragment.show(getSupportFragmentManager(), "dialogView");
    }

    //For ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //Show SearchActivity
        MenuItem menuSearch = menu.add(Menu.NONE, R.id.account_menu_search, Menu.NONE, "SearchActivity");
        menuSearch.setIcon(android.R.drawable.ic_menu_search);
        menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menuSearch.setActionView(new SearchView(getSupportActionBar().getThemedContext()));

        new SearchWidget(this, (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.account_menu_search)));

        //Show Add Icon
        MenuItem menuAdd = menu.add(Menu.NONE, R.id.account_menu_add, Menu.NONE, "Add");
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;

        int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        switch (type) {
            case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
                String nameSubCategory = adapterCategory.getSubCategory(groupPos, childPos).name;
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
                Log.e(getClass().getSimpleName(), "Context Menu type is not child or group");
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
                Log.e(getClass().getSimpleName(), "Context Menu type is not child or group");
                break;
        }

        return super.onContextItemSelected(item);
    }

    //Used after a change in settings occurs
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        //Toast.makeText(this, "Options Just Changed: CategoriesActivity.Java", Toast.LENGTH_SHORT).show();
        //categoryPopulate();
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

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        Log.d(getClass().getSimpleName(), "calling create loader...");
        switch (loaderID) {
            case CATEGORIES_LOADER:
                Log.v(getClass().getSimpleName(), "new category loader created");
                return new CursorLoader(
                        this,    // Parent activity context
                        MyContentProvider.CATEGORIES_URI,// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        null                // Default sort order-> "CAST (AcctBalance AS INTEGER)" + " DESC"
                );

            case SUBCATEGORIES_LOADER:
                Log.v(getClass().getSimpleName(), "new subcategory loader created");
                String selection = DatabaseHelper.SUBCATEGORY_CAT_ID + "=" + bundle.getString("id");
                return new CursorLoader(
                        this,    // Parent activity context
                        MyContentProvider.SUBCATEGORIES_URI,// Table to query
                        null,                // Projection to return
                        selection,            // No selection clause
                        null,                // No selection arguments
                        null                // Default sort order
                );

            default:
                Log.e(getClass().getSimpleName(), "Not a valid CursorLoader ID");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case CATEGORIES_LOADER:
                adapterCategory.swapCategoryCursor(data);
                Log.v(getClass().getSimpleName(), "loader finished. loader=" + loader.getId() + " data=" + data + " data size=" + data.getCount());

                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    //Bundle bundle = new Bundle();
                    //bundle.putString("id", data.getString(0));
                    //getSupportLoaderManager().restartLoader(SUBCATEGORIES_LOADER, bundle, this);
                    subcategoryPopulate(data.getInt(0));
                }

                break;

            case SUBCATEGORIES_LOADER:
                adapterCategory.swapSubCategoryCursor(data);
                Log.v(getClass().getSimpleName(), "loader finished. loader=" + loader.getId() + " data=" + data + " data size=" + data.getCount());
                break;

            default:
                Log.e(getClass().getSimpleName(), "Error. Unknown loader (" + loader.getId());
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case CATEGORIES_LOADER:
                adapterCategory.swapCategoryCursor(null);
                Log.v(getClass().getSimpleName(), "loader reset. loader=" + loader.getId());
                break;

            case SUBCATEGORIES_LOADER:
                adapterCategory.swapSubCategoryCursor(null);
                Log.v(getClass().getSimpleName(), "loader reset. loader=" + loader.getId());
                break;

            default:
                Log.e(getClass().getSimpleName(), "Error. Unknown loader (" + loader.getId());
                break;
        }
    }

}//end CategoriesActivity