/* Class that handles the Scheduled Transactions seen in the Plans Screen
 * Does everything from setting up the view to Add/Delete/Edit
 * Hands over the actual scheduling to PlanReceiver Class
 */

package com.databases.example.app;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.MyContentProvider;
import com.databases.example.data.PlanReceiver;
import com.databases.example.data.PlanRecord;
import com.databases.example.data.PlanWizardInfo2Page;
import com.databases.example.data.SearchWidget;
import com.databases.example.view.Drawer;
import com.databases.example.view.PlanViewFragment;
import com.databases.example.view.PlanWizard;
import com.databases.example.view.PlanWizardInfo2Fragment;
import com.databases.example.view.PlansListViewAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Plans extends SherlockFragmentActivity implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {
    private final int ACTIONBAR_MENU_ADD_PLAN_ID = 5882300;

    private static final int PLAN_LOADER = 5882300;
    private static final int PLAN_SUBCATEGORY_LOADER = 588;
    private static final int PLAN_ACCOUNT_LOADER = 2300;

    //NavigationDrawer
    private Drawer drawer;

    //Adapter for category spinner
    public static SimpleCursorAdapter categorySpinnerAdapter = null;

    //Adapter for account spinner
    public static SimpleCursorAdapter accountSpinnerAdapter = null;

    //Constants for ContextMenu
    private final int ACTION_MODE_VIEW = 1;
    private final int ACTION_MODE_EDIT = 2;
    private final int ACTION_MODE_DELETE = 3;
    private final int ACTION_MODE_TOGGLE = 4;

    public static Button pDate;
    private PlansListViewAdapter adapterPlans;

    //ActionMode
    private Object mActionMode = null;

    //For Memo autocomplete
    public static final ArrayList<String> dropdownResults = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plans);
        setTitle("Plans");

        //NavigationDrawer
        drawer = new Drawer(this);

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
                    //Stuff for clicking...
                }
            }// end onItemClick

        }//end onItemClickListener
        );//end setOnItemClickListener

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

        TextView noResult = (TextView) findViewById(R.id.plans_noPlans);
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
        newFragment.show(getSupportFragmentManager(), "dialogAdd");
    }//end of planAdd

    public void schedule(PlanRecord plan) {
        Date d = null;

        try {
            DateTime test = new DateTime();
            test.setStringSQL(plan.offset);
            d = test.getYearMonthDay();
        } catch (java.text.ParseException e) {
            Log.e("Plans-schedule", "Couldn't schedule " + plan.name + "\n e:" + e);
        }

        Log.d("Plans-schedule", "d.year=" + (d.getYear() + 1900) + " d.date=" + d.getDate() + " d.month=" + d.getMonth());

        Calendar firstRun = new GregorianCalendar(d.getYear() + 1900, d.getMonth(), d.getDate());
        Log.d("Plans-schedule", "FirstRun:" + firstRun);

        Intent intent = new Intent(this, PlanReceiver.class);
        intent.putExtra("plan_id", plan.id);
        intent.putExtra("plan_acct_id", plan.acctId);
        intent.putExtra("plan_name", plan.name);
        intent.putExtra("plan_value", plan.value);
        intent.putExtra("plan_type", plan.type);
        intent.putExtra("plan_category", plan.category);
        intent.putExtra("plan_memo", plan.memo);
        intent.putExtra("plan_offset", plan.offset);
        intent.putExtra("plan_rate", plan.rate);
        intent.putExtra("plan_cleared", plan.cleared);

        //Parse Rate (token 0 is amount, token 1 is type)
        String phrase = plan.rate;
        String delims = "[ ]+";
        String[] tokens = phrase.split(delims);

        final PendingIntent sender = PendingIntent.getBroadcast(this, Integer.parseInt(plan.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        final Locale locale = getResources().getConfiguration().locale;
        final DateTime nextRun = new DateTime();

        if (tokens[1].contains("Days")) {
            Log.d("Plans-schedule", "Days");

            //If Starting Time is in the past, fire off next day(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[0]));
            }

            Log.d("Plans-schedule", "firstRun is " + firstRun);
            nextRun.setCalendar(firstRun);

            Toast.makeText(this, "Next Transaction scheduled for " + nextRun.getReadableDate(), Toast.LENGTH_SHORT).show();

            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
            getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), planValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);

            am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0]) * AlarmManager.INTERVAL_DAY), sender);
        } else if (tokens[1].contains("Weeks")) {
            Log.d("Plans-schedule", "Weeks");

            //If Starting Time is in the past, fire off next week(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(tokens[0]));
            }

            Log.d("Plans-schedule", "firstRun is " + firstRun);
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

            Log.d("Plans-schedule", "firstRun is " + firstRun);
            nextRun.setCalendar(firstRun);

            Toast.makeText(this, "Next Transaction scheduled for " + nextRun.getReadableDate(), Toast.LENGTH_SHORT).show();

            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
            getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), planValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);

            am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), cal.getTimeInMillis(), sender);
        } else {
            Log.e("Plans-schedule", "Could not set alarm; Something wrong with the rate");
        }

    }

    public void cancelPlan(PlanRecord plan) {
        Intent intent = new Intent(this, PlanReceiver.class);
        intent.putExtra("plan_id", plan.id);
        intent.putExtra("plan_acct_id", plan.acctId);
        intent.putExtra("plan_name", plan.name);
        intent.putExtra("plan_value", plan.value);
        intent.putExtra("plan_type", plan.type);
        intent.putExtra("plan_category", plan.category);
        intent.putExtra("plan_memo", plan.memo);
        intent.putExtra("plan_offset", plan.offset);
        intent.putExtra("plan_rate", plan.rate);
        intent.putExtra("plan_cleared", plan.cleared);

        // In reality, you would want to have a static variable for the request code instead of 192837
        PendingIntent sender = PendingIntent.getBroadcast(this, Integer.parseInt(plan.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        try {
            am.cancel(sender);
        } catch (Exception e) {
            Toast.makeText(this, "Error canceling plan", Toast.LENGTH_SHORT).show();
            Log.e("Plans-schedule", "AlarmManager update was not canceled. " + e.toString());
        }

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

        new SearchWidget(this, menuSearch.getActionView());

        //Add
        MenuItem subMenu1Item = menu.add(com.actionbarsherlock.view.Menu.NONE, ACTIONBAR_MENU_ADD_PLAN_ID, com.actionbarsherlock.view.Menu.NONE, "Add");
        subMenu1Item.setIcon(android.R.drawable.ic_menu_add);
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    //For ActionBar Menu Items (and home icon)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.toggle();
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
        Log.d("Plans-onSharedPreferenceChanged", "Options changed. Requery");
        //getContentResolver().notifyChange(MyContentProvider.PLANNED_TRANSACTIONS_URI, null);
        //getLoaderManager().restartLoader(PLAN_LOADER, null, this);
    }

    //Method for selecting a Date when adding a transaction
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar cal = Calendar.getInstance();

            SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
            SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");

            int year = Integer.parseInt(dateFormatYear.format(cal.getTime()));
            int month = Integer.parseInt(dateFormatMonth.format(cal.getTime())) - 1;
            int day = Integer.parseInt(dateFormatDay.format(cal.getTime()));

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            DateTime date = new DateTime();
            date.setStringSQL(year + "-" + (month + 1) + "-" + day);

            if (pDate != null) {
                pDate.setText(date.getReadableDate());
            }

            if (PlanWizardInfo2Fragment.mPage != null) {
                PlanWizardInfo2Fragment.mPage.getData().putString(PlanWizardInfo2Page.DATE_DATA_KEY, date.getReadableDate());
                PlanWizardInfo2Fragment.mPage.notifyDataChanged();
            }
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

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        switch (loaderID) {
            case PLAN_LOADER:
                if (bundle != null && bundle.getBoolean("boolSearch")) {
                    //Log.v("Plans-onCreateLoader","new loader (boolSearch "+ query + ") created");
                    String query = this.getIntent().getStringExtra("query");
                    return new CursorLoader(
                            this,    // Parent activity context
                            (Uri.parse(MyContentProvider.PLANS_ID + "/SEARCH/" + query)),// Table to query
                            null,                // Projection to return
                            null,                // No selection clause
                            null,                // No selection arguments
                            null                // Default sort order
                    );
                } else {
                    Log.v("Plans-onCreateLoader", "new loader created");
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
                Log.v("Plans-onCreateLoader", "new plan loader created");
                return new CursorLoader(
                        this,    // Parent activity context
                        MyContentProvider.ACCOUNTS_URI,// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        null           // Default sort order-> "CAST (AcctBalance AS INTEGER)" + " DESC"
                );

            case PLAN_SUBCATEGORY_LOADER:
                Log.v("Plans-onCreateLoader", "new category loader created");
                return new CursorLoader(
                        this,    // Parent activity context
                        MyContentProvider.SUBCATEGORIES_URI,// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        null           // Default sort order
                );

            default:
                Log.e("Plans-onCreateLoader", "Not a valid CursorLoader ID");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case PLAN_LOADER:
                adapterPlans.swapCursor(data);
                Log.v("Plans-onLoadFinished", "load done. loader=" + loader + " data=" + data + " data size=" + data.getCount());
                break;

            case PLAN_ACCOUNT_LOADER:
                String[] from = new String[]{DatabaseHelper.ACCOUNT_NAME, "_id"};
                int[] to = new int[]{android.R.id.text1};

                accountSpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, data, from, to, 0);
                accountSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Log.v("Plans-onLoadFinished", "load done. loader=" + loader + " data=" + data + " data size=" + data.getCount());
                break;

            case PLAN_SUBCATEGORY_LOADER:
                from = new String[]{DatabaseHelper.SUBCATEGORY_NAME};
                to = new int[]{android.R.id.text1};

                categorySpinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, data, from, to, 0);
                categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Log.v("Plans-onLoadFinished", "load done. loader=" + loader + " data=" + data + " data size=" + data.getCount());
                break;

            default:
                Log.v("Plans-onLoadFinished", "Error. Unknown loader (" + loader.getId());
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case PLAN_LOADER:
                adapterPlans.swapCursor(null);
                Log.v("Plans-onLoaderReset", "loader reset. loader=" + loader.getId());
                break;

            case PLAN_ACCOUNT_LOADER:
                Log.v("Plans-onLoaderReset", "loader reset. loader=" + loader.getId());
                break;

            case PLAN_SUBCATEGORY_LOADER:
                Log.v("Plans-onLoaderReset", "loader reset. loader=" + loader.getId());
                break;

            default:
                Log.e("Plans-onLoadFinished", "Error. Unknown loader (" + loader.getId());
                break;
        }
    }

    private final class MyActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(0, ACTION_MODE_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);
            menu.add(0, ACTION_MODE_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, ACTION_MODE_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
            menu.add(0, ACTION_MODE_TOGGLE, 3, "Toggle").setIcon(android.R.drawable.ic_menu_revert);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            if (adapterPlans.getSelectedCount() == 1 && mode != null) {
                menu.add(0, ACTION_MODE_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);
                menu.add(0, ACTION_MODE_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
                menu.add(0, ACTION_MODE_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
                menu.add(0, ACTION_MODE_TOGGLE, 3, "Toggle").setIcon(android.R.drawable.ic_menu_revert);
                return true;
            } else if (adapterPlans.getSelectedCount() > 1) {
                menu.add(0, ACTION_MODE_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
                menu.add(0, ACTION_MODE_TOGGLE, 3, "Toggle").setIcon(android.R.drawable.ic_menu_revert);
                return true;
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            SparseBooleanArray selected = adapterPlans.getSelectedIds();
            PlanRecord record;

            switch (item.getItemId()) {
                case ACTION_MODE_VIEW:
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            DialogFragment newFragment = PlanViewFragment.newInstance(adapterPlans.getPlan(selected.keyAt(i)).id);
                            newFragment.show(getSupportFragmentManager(), "dialogView");
                        }
                    }

                    mode.finish();
                    return true;
                case ACTION_MODE_EDIT:
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            record = adapterPlans.getPlan(selected.keyAt(i));
                            final PlanWizard frag = PlanWizard.newInstance(record);
                            frag.show(getSupportFragmentManager(), "dialogEdit");
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

                            Log.d("Plans", "Deleting " + record.name + " id:" + record.id);

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

                            intent = new Intent(Plans.this, PlanReceiver.class);
                            intent.putExtra("plan_id", record.id);
                            intent.putExtra("plan_acct_id", record.acctId);
                            intent.putExtra("plan_name", record.name);
                            intent.putExtra("plan_value", record.value);
                            intent.putExtra("plan_type", record.type);
                            intent.putExtra("plan_category", record.category);
                            intent.putExtra("plan_memo", record.memo);
                            intent.putExtra("plan_offset", record.offset);
                            intent.putExtra("plan_rate", record.rate);
                            intent.putExtra("plan_next", record.next);
                            intent.putExtra("plan_scheduled", record.scheduled);
                            intent.putExtra("plan_cleared", record.cleared);

                            PendingIntent sender = PendingIntent.getBroadcast(Plans.this, Integer.parseInt(record.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            try {
                                if (record.scheduled.equals("true")) {
                                    am.cancel(sender);

                                    ContentValues transactionValues = new ContentValues();
                                    transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "false");
                                    getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + record.id), transactionValues, DatabaseHelper.PLAN_ID + "=" + record.id, null);

                                    Toast.makeText(Plans.this, "Canceled plan:\n" + record.name, Toast.LENGTH_SHORT).show();
                                } else {
                                    schedule(record);

                                    ContentValues transactionValues = new ContentValues();
                                    transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "true");
                                    getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + record.id), transactionValues, DatabaseHelper.PLAN_ID + "=" + record.id, null);
                                }
                            } catch (Exception e) {
                                Toast.makeText(Plans.this, "Error toggling plan \n" + record.name, Toast.LENGTH_SHORT).show();
                                Log.e("Plans-schedule", "Error toggling a plan. " + e.toString());
                            }
                        }
                    }

                    mode.finish();
                    return true;

                default:
                    mode.finish();
                    Log.e("Plans-onActionItemClciked", "ERROR. Clicked " + item);
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

}//end of Plans