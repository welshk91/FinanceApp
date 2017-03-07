/* Class that handles the Scheduled Transactions seen in the Plans Screen
 * Does everything from setting up the view to Add/Delete/Edit
 * Hands over the actual scheduling to PlanReceiver Class
 */

package com.databases.example.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
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
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.MyContentProvider;
import com.databases.example.data.PlanReceiver;
import com.databases.example.fragments.PlanViewFragment;
import com.databases.example.model.Plan;
import com.databases.example.model.SearchWidget;
import com.databases.example.utils.DateTime;
import com.databases.example.view.PlansListViewAdapter;
import com.databases.example.wizard.PlanWizard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import timber.log.Timber;

public class PlansActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {
    private final int ACTIONBAR_MENU_ADD_PLAN_ID = 5882300;

    private static final int PLAN_LOADER = 5882300;
    private static final int PLAN_SUBCATEGORY_LOADER = 588;
    private static final int PLAN_ACCOUNT_LOADER = 2300;

    //NavigationDrawer
    private DrawerActivity drawerActivity;

    //Adapter for category spinner
    public static SimpleCursorAdapter categorySpinnerAdapter = null;

    //Adapter for account spinner
    public static SimpleCursorAdapter accountSpinnerAdapter = null;

    //Constants for ContextMenu
    private final int ACTION_MODE_VIEW = 1;
    private final int ACTION_MODE_EDIT = 2;
    private final int ACTION_MODE_DELETE = 3;
    private final int ACTION_MODE_TOGGLE = 4;

    public static Button datePicker;
    private PlansListViewAdapter adapterPlans;

    //ActionMode
    private Object mActionMode = null;

    //For Memo autocomplete
    public static final ArrayList<String> dropdownResults = new ArrayList<String>();

    public static final String PLAN_ID = "plan_id";
    public static final String PLAN_ACCOUNT_ID = "plan_acct_id";
    public static final String PLAN_NAME = "plan_name";
    public static final String PLAN_VALUE = "plan_value";
    public static final String PLAN_TYPE = "plan_type";
    public static final String PLAN_CATEGORY = "plan_category";
    public static final String PLAN_MEMO = "plan_memo";
    public static final String PLAN_OFFSET = "plan_offset";
    public static final String PLAN_RATE = "plan_rate";
    public static final String PLAN_NEXT = "plan_next";
    public static final String PLAN_SCHEDULED = "plan_scheduled";
    public static final String PLAN_CLEARED = "plan_cleared";

    private final String ADD_FRAGMENT_TAG = "plans_add_fragment";
    private final String EDIT_FRAGMENT_TAG = "plans_edit_fragment";
    private final String VIEW_FRAGMENT_TAG = "plans_view_fragment";
    private final String TRANSFER_FRAGMENT_TAG = "plans_transfer_fragment";
    private final String SORT_FRAGMENT_TAG = "plans_sort_fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plans);
        setTitle(getString(R.string.plans));

        //NavigationDrawer
        drawerActivity = new DrawerActivity(this);

        ListView lvPlans = (ListView) this.findViewById(R.id.plans_list);

        //Turn clicks on
        lvPlans.setClickable(true);
        lvPlans.setLongClickable(true);

        //Set Listener for regular mouse click
        lvPlans.setOnItemClickListener(new OnItemClickListener() {
                                           @Override
                                           public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                                               if (mActionMode != null) {
                                                   listItemChecked(position);
                                               } else {
                                                   //TODO Stuff for clicking...
                                               }
                                           }// end onItemClick

                                       }
        );

        lvPlans.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                listItemChecked(position);
                return true;
            }
        });

        //Set up a listener for changes in settings menu
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        TextView noResult = (TextView) findViewById(R.id.plans_empty);
        lvPlans.setEmptyView(noResult);

        adapterPlans = new PlansListViewAdapter(this, null);
        lvPlans.setAdapter(adapterPlans);

        getSupportLoaderManager().initLoader(PLAN_LOADER, null, this);
        getSupportLoaderManager().initLoader(PLAN_SUBCATEGORY_LOADER, null, this);
        getSupportLoaderManager().initLoader(PLAN_ACCOUNT_LOADER, null, this);

    }//end onCreate

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

    public void schedule(Plan plan) {
        Date d = null;

        try {
            DateTime test = new DateTime();
            test.setStringSQL(plan.offset);
            d = test.getYearMonthDay();
        } catch (java.text.ParseException e) {
            Timber.e("Couldn't schedule " + plan.name + "\n e:" + e);
        }

        Timber.d("d.year=" + (d.getYear() + 1900) + " d.date=" + d.getDate() + " d.month=" + d.getMonth());

        Calendar firstRun = new GregorianCalendar(d.getYear() + 1900, d.getMonth(), d.getDate());
        Timber.d("FirstRun:" + firstRun);

        Intent intent = new Intent(this, PlanReceiver.class);
        intent.putExtra(PLAN_ID, plan.id);
        intent.putExtra(PLAN_ACCOUNT_ID, plan.acctId);
        intent.putExtra(PLAN_NAME, plan.name);
        intent.putExtra(PLAN_VALUE, plan.value);
        intent.putExtra(PLAN_TYPE, plan.type);
        intent.putExtra(PLAN_CATEGORY, plan.category);
        intent.putExtra(PLAN_MEMO, plan.memo);
        intent.putExtra(PLAN_OFFSET, plan.offset);
        intent.putExtra(PLAN_RATE, plan.rate);
        intent.putExtra(PLAN_CLEARED, plan.cleared);

        //Parse Rate (token 0 is amount, token 1 is type)
        String phrase = plan.rate;
        String delims = "[ ]+";
        String[] tokens = phrase.split(delims);

        final PendingIntent sender = PendingIntent.getBroadcast(this, plan.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        final Locale locale = getResources().getConfiguration().locale;
        final DateTime nextRun = new DateTime();

        if (tokens[1].contains("Days")) {
            Timber.v("Days");

            //If Starting Time is in the past, fire off next day(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[0]));
            }

            Timber.d("firstRun is " + firstRun);
            nextRun.setCalendar(firstRun);

            Toast.makeText(this, "Next Transaction scheduled for " + nextRun.getReadableDate(), Toast.LENGTH_SHORT).show();

            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
            getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), planValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);

            am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0]) * AlarmManager.INTERVAL_DAY), sender);
        } else if (tokens[1].contains("Weeks")) {
            Timber.v("Weeks");

            //If Starting Time is in the past, fire off next week(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(tokens[0]));
            }

            Timber.d("firstRun is " + firstRun);
            nextRun.setCalendar(firstRun);

            Toast.makeText(this, "Next Transaction scheduled for " + nextRun.getReadableDate(), Toast.LENGTH_SHORT).show();

            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
            getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), planValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);

            am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0]) * AlarmManager.INTERVAL_DAY) * 7, sender);
        } else if (tokens[1].contains("Months")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(cal.getTimeInMillis());
            cal.add(Calendar.MONTH, Integer.parseInt(tokens[0]));

            //If Starting Time is in the past, fire off next month(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.MONTH, Integer.parseInt(tokens[0]));
            }

            Timber.d("firstRun is " + firstRun);
            nextRun.setCalendar(firstRun);

            Toast.makeText(this, "Next Transaction scheduled for " + nextRun.getReadableDate(), Toast.LENGTH_SHORT).show();

            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
            getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), planValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);

            am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), cal.getTimeInMillis(), sender);
        } else {
            Timber.e("Could not set alarm; Something wrong with the rate");
        }

    }

    public void cancelPlan(Plan plan) {
        Intent intent = new Intent(this, PlanReceiver.class);
        intent.putExtra(PLAN_ID, plan.id);
        intent.putExtra(PLAN_ACCOUNT_ID, plan.acctId);
        intent.putExtra(PLAN_NAME, plan.name);
        intent.putExtra(PLAN_VALUE, plan.value);
        intent.putExtra(PLAN_TYPE, plan.type);
        intent.putExtra(PLAN_CATEGORY, plan.category);
        intent.putExtra(PLAN_MEMO, plan.memo);
        intent.putExtra(PLAN_OFFSET, plan.offset);
        intent.putExtra(PLAN_RATE, plan.rate);
        intent.putExtra(PLAN_CLEARED, plan.cleared);

        // In reality, you would want to have a static variable for the request code instead of 192837
        PendingIntent sender = PendingIntent.getBroadcast(this, plan.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        try {
            am.cancel(sender);
        } catch (Exception e) {
            Toast.makeText(this, "Error canceling plan", Toast.LENGTH_SHORT).show();
            Timber.e("AlarmManager update was not canceled. " + e.toString());
        }
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
            case android.R.id.home:
                drawerActivity.toggle();
                break;

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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerActivity.getDrawerToggle().syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerActivity.getDrawerToggle().onConfigurationChanged(newConfig);
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
                adapterPlans.swapCursor(data);
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
                adapterPlans.swapCursor(null);
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

                            Uri uri = Uri.parse(MyContentProvider.PLANS_URI + "/" + record.id);
                            getContentResolver().delete(uri, DatabaseHelper.PLAN_ID + "=" + record.id, null);

                            Timber.d("Deleting " + record.name + " id:" + record.id);

                            //Cancel all upcoming notifications
                            cancelPlan(record);
                        }
                    }

                    mode.finish();
                    return true;

                case ACTION_MODE_TOGGLE:
                    Intent intent;
                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            record = adapterPlans.getPlan(selected.keyAt(i));

                            intent = new Intent(PlansActivity.this, PlanReceiver.class);
                            intent.putExtra(PLAN_ID, record.id);
                            intent.putExtra(PLAN_ACCOUNT_ID, record.acctId);
                            intent.putExtra(PLAN_NAME, record.name);
                            intent.putExtra(PLAN_VALUE, record.value);
                            intent.putExtra(PLAN_TYPE, record.type);
                            intent.putExtra(PLAN_CATEGORY, record.category);
                            intent.putExtra(PLAN_MEMO, record.memo);
                            intent.putExtra(PLAN_OFFSET, record.offset);
                            intent.putExtra(PLAN_RATE, record.rate);
                            intent.putExtra(PLAN_NEXT, record.next);
                            intent.putExtra(PLAN_SCHEDULED, record.scheduled);
                            intent.putExtra(PLAN_CLEARED, record.cleared);

                            PendingIntent sender = PendingIntent.getBroadcast(PlansActivity.this, record.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            try {
                                if (record.scheduled.equals("true")) {
                                    am.cancel(sender);

                                    ContentValues transactionValues = new ContentValues();
                                    transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "false");
                                    getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + record.id), transactionValues, DatabaseHelper.PLAN_ID + "=" + record.id, null);

                                    Toast.makeText(PlansActivity.this, "Canceled plan:\n" + record.name, Toast.LENGTH_SHORT).show();
                                } else {
                                    schedule(record);

                                    ContentValues transactionValues = new ContentValues();
                                    transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "true");
                                    getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + record.id), transactionValues, DatabaseHelper.PLAN_ID + "=" + record.id, null);
                                }
                            } catch (Exception e) {
                                Toast.makeText(PlansActivity.this, "Error toggling plan \n" + record.name, Toast.LENGTH_SHORT).show();
                                Timber.e("Error toggling a plan. " + e.toString());
                            }
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