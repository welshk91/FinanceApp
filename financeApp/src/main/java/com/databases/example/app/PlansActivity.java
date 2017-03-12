/* Class that handles the Scheduled Transactions seen in the Plans Screen
 * Does everything from setting up the view to Add/Delete/Edit
 * Hands over the actual scheduling to PlanReceiver Class
 */

package com.databases.example.app;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.MyContentProvider;
import com.databases.example.fragments.PlanViewFragment;
import com.databases.example.model.Plan;
import com.databases.example.model.SearchWidget;
import com.databases.example.utils.Constants;
import com.databases.example.utils.PlanUtils;
import com.databases.example.view.PlansRecyclerViewAdapter;
import com.databases.example.view.RecyclerViewListener;
import com.databases.example.wizard.PlanWizard;

import java.util.ArrayList;

import timber.log.Timber;

public class PlansActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {
    private final int ACTIONBAR_MENU_ADD_PLAN_ID = 5882300;

    private static final int PLAN_LOADER = 5882300;
    private static final int PLAN_SUBCATEGORY_LOADER = 588;
    private static final int PLAN_ACCOUNT_LOADER = 2300;

    //Adapter for category spinner
    public static SimpleCursorAdapter categorySpinnerAdapter = null;

    //Adapter for account spinner
    public static SimpleCursorAdapter accountSpinnerAdapter = null;

    //Constants for ContextMenu
    private final int ACTION_MODE_VIEW = 1;
    private final int ACTION_MODE_EDIT = 2;
    private final int ACTION_MODE_DELETE = 3;
    private final int ACTION_MODE_TOGGLE = 4;

    private PlansRecyclerViewAdapter adapterPlans;

    //ActionMode
    private Object mActionMode = null;

    //For Memo autocomplete
    public static final ArrayList<String> dropdownResults = new ArrayList<String>();

    private final String ADD_FRAGMENT_TAG = "plans_add_fragment";
    private final String EDIT_FRAGMENT_TAG = "plans_edit_fragment";
    private final String VIEW_FRAGMENT_TAG = "plans_view_fragment";
    private final String TRANSFER_FRAGMENT_TAG = "plans_transfer_fragment";
    private final String SORT_FRAGMENT_TAG = "plans_sort_fragment";

    private DrawerActivity drawerActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plans);
        setTitle(getString(R.string.plans));

        //NavigationDrawer
        drawerActivity = new DrawerActivity(this, Constants.ActivityTag.PLANS, null);
        drawerActivity.initialize();

        RecyclerView recyclerViewPlans = (RecyclerView) findViewById(R.id.plans_list);

        //Turn clicks on
        recyclerViewPlans.setClickable(true);
        recyclerViewPlans.setLongClickable(true);

        //Set up a listener for changes in settings menu
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        adapterPlans = new PlansRecyclerViewAdapter(this, null, new RecyclerViewListener() {
            @Override
            public void onItemClick(Object model, int position) {
                if (mActionMode != null) {
                    listItemChecked(position);
                } else {
                    //TODO Stuff for clicking...
                }
            }

            @Override
            public boolean onItemLongClick(Object model, int position) {
                if (mActionMode != null) {
                    return false;
                }

                listItemChecked(position);
                return true;
            }
        });

        recyclerViewPlans.setAdapter(adapterPlans);
        recyclerViewPlans.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        getSupportLoaderManager().initLoader(PLAN_LOADER, null, this);
        getSupportLoaderManager().initLoader(PLAN_SUBCATEGORY_LOADER, null, this);
        getSupportLoaderManager().initLoader(PLAN_ACCOUNT_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawerActivity.setActivityTag();
    }

    //Used for ActionMode
    private void listItemChecked(int position) {
        adapterPlans.toggleSelection(position);
        boolean hasCheckedItems = adapterPlans.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            mActionMode = this.startActionMode(new MyActionMode());
        } else if (!hasCheckedItems && mActionMode != null) {
            ((ActionMode) mActionMode).finish();
        }

        if (mActionMode != null) {
            ((ActionMode) mActionMode).invalidate();
            ((ActionMode) mActionMode).setTitle(String.valueOf(adapterPlans.getSelectedCount()));
        }
    }

    //For Scheduling a Transaction
    private void planAdd() {
        PlanWizard newFragment = PlanWizard.newInstance(null);
        newFragment.show(getSupportFragmentManager(), ADD_FRAGMENT_TAG);
    }


    //For ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //Show SearchActivity
        MenuItem menuSearch = menu.add(Menu.NONE, R.id.account_menu_search, Menu.NONE, R.string.search);
        menuSearch.setIcon(android.R.drawable.ic_menu_search);
        menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menuSearch.setActionView(new SearchView(getSupportActionBar().getThemedContext()));

        new SearchWidget(this, (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.account_menu_search)));

        //Add
        MenuItem subMenu1Item = menu.add(Menu.NONE, ACTIONBAR_MENU_ADD_PLAN_ID, Menu.NONE, R.string.add);
        subMenu1Item.setIcon(android.R.drawable.ic_menu_add);
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    //For ActionBar Menu Items (and home icon)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ACTIONBAR_MENU_ADD_PLAN_ID:
                planAdd();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //Used after a change in settings occurs
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Timber.d("OptionsActivity changed. Requery");
        //getContentResolver().notifyChange(MyContentProvider.PLANNED_TRANSACTIONS_URI, null);
        //getLoaderManager().restartLoader(PLAN_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        switch (loaderID) {
            case PLAN_LOADER:
                if (bundle != null && bundle.getBoolean(SearchActivity.BOOLEAN_SEARCH_KEY)) {
                    //Timber.v("new loader (boolSearch "+ query + ") created");
                    String query = this.getIntent().getStringExtra(SearchActivity.QUERY_KEY);
                    return new CursorLoader(
                            this,    // Parent activity context
                            (Uri.parse(MyContentProvider.PLANS_ID + "/SEARCH/" + query)),// Table to query
                            null,                // Projection to return
                            null,                // No selection clause
                            null,                // No selection arguments
                            null                // Default sort order
                    );
                } else {
                    Timber.v("new loader created");
                    return new CursorLoader(
                            this,    // Parent activity context
                            MyContentProvider.PLANS_URI,// Table to query
                            null,                // Projection to return
                            null,                // No selection clause
                            null,                // No selection arguments
                            null                // Default sort order
                    );
                }

            case PLAN_ACCOUNT_LOADER:
                Timber.v("new plan loader created");
                return new CursorLoader(
                        this,    // Parent activity context
                        MyContentProvider.ACCOUNTS_URI,// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        null           // Default sort order-> "CAST (AcctBalance AS INTEGER)" + " DESC"
                );

            case PLAN_SUBCATEGORY_LOADER:
                Timber.v("new category loader created");
                return new CursorLoader(
                        this,    // Parent activity context
                        MyContentProvider.SUBCATEGORIES_URI,// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        null           // Default sort order
                );

            default:
                Timber.e("Not a valid CursorLoader ID");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case PLAN_LOADER:
                adapterPlans.setPlans(Plan.getPlans(data));
                Timber.v("load done. loader=" + loader + " data=" + data + " data size=" + data.getCount());
                break;

            case PLAN_ACCOUNT_LOADER:
                String[] from = new String[]{DatabaseHelper.ACCOUNT_NAME, "_id"};
                int[] to = new int[]{android.R.id.text1};

                accountSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, data, from, to, 0);
                accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Timber.v("load done. loader=" + loader + " data=" + data + " data size=" + data.getCount());
                break;

            case PLAN_SUBCATEGORY_LOADER:
                from = new String[]{DatabaseHelper.SUBCATEGORY_NAME};
                to = new int[]{android.R.id.text1};

                categorySpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, data, from, to, 0);
                categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Timber.v("load done. loader=" + loader + " data=" + data + " data size=" + data.getCount());
                break;

            default:
                Timber.v("Error. Unknown loader (" + loader.getId());
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case PLAN_LOADER:
                adapterPlans.setPlans(null);
                Timber.v("loader reset. loader=" + loader.getId());
                break;

            case PLAN_ACCOUNT_LOADER:
                Timber.v("loader reset. loader=" + loader.getId());
                break;

            case PLAN_SUBCATEGORY_LOADER:
                Timber.v("loader reset. loader=" + loader.getId());
                break;

            default:
                Timber.e("Error. Unknown loader (" + loader.getId());
                break;
        }
    }

    private final class MyActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(0, ACTION_MODE_VIEW, 0, R.string.view).setIcon(android.R.drawable.ic_menu_view);
            menu.add(0, ACTION_MODE_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, ACTION_MODE_DELETE, 2, R.string.edit).setIcon(android.R.drawable.ic_menu_delete);
            menu.add(0, ACTION_MODE_TOGGLE, 3, R.string.toggle).setIcon(android.R.drawable.ic_menu_revert);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            if (adapterPlans.getSelectedCount() == 1 && mode != null) {
                menu.add(0, ACTION_MODE_VIEW, 0, R.string.view).setIcon(android.R.drawable.ic_menu_view);
                menu.add(0, ACTION_MODE_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
                menu.add(0, ACTION_MODE_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
                menu.add(0, ACTION_MODE_TOGGLE, 3, R.string.toggle).setIcon(android.R.drawable.ic_menu_revert);
                return true;
            } else if (adapterPlans.getSelectedCount() > 1) {
                menu.add(0, ACTION_MODE_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
                menu.add(0, ACTION_MODE_TOGGLE, 3, R.string.toggle).setIcon(android.R.drawable.ic_menu_revert);
                return true;
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            SparseBooleanArray selected = adapterPlans.getSelectedIds();
            Plan record;

            switch (item.getItemId()) {
                case ACTION_MODE_VIEW:
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            DialogFragment newFragment = PlanViewFragment.newInstance(adapterPlans.getPlan(selected.keyAt(i)).id);
                            newFragment.show(getSupportFragmentManager(), VIEW_FRAGMENT_TAG);
                        }
                    }

                    mode.finish();
                    return true;
                case ACTION_MODE_EDIT:
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            record = adapterPlans.getPlan(selected.keyAt(i));
                            final PlanWizard frag = PlanWizard.newInstance(record);
                            frag.show(getSupportFragmentManager(), EDIT_FRAGMENT_TAG);
                        }
                    }

                    mode.finish();
                    return true;
                case ACTION_MODE_DELETE:
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            record = adapterPlans.getPlan(selected.keyAt(i));

                            if (PlanUtils.cancelPlan(PlansActivity.this, record)) {
                                Uri uri = Uri.parse(MyContentProvider.PLANS_URI + "/" + record.id);
                                getContentResolver().delete(uri, DatabaseHelper.PLAN_ID + "=" + record.id, null);
                                Timber.d("Deleting " + record.name + " id:" + record.id);
                            }
                        }
                    }

                    mode.finish();
                    return true;

                case ACTION_MODE_TOGGLE:
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            record = adapterPlans.getPlan(selected.keyAt(i));
                            PlanUtils.togglePlan(PlansActivity.this, record);
                        }
                    }

                    mode.finish();
                    return true;

                default:
                    mode.finish();
                    Timber.e("ERROR. Clicked " + item);
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            adapterPlans.removeSelection();
        }
    }

    @Override
    public void onDestroy() {
        if (mActionMode != null) {
            ((ActionMode) mActionMode).finish();
        }

        super.onDestroy();
    }
}