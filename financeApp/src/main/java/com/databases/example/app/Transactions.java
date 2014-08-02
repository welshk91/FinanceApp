/* Class that handles the Transaction Fragment seen in the Checkbook screen
 * Does everything from setting up the view to Add/Delete/Edit Transactions to calculating the balance
 */

package com.databases.example.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.Money;
import com.databases.example.data.MyContentProvider;
import com.databases.example.data.SearchWidget;
import com.databases.example.data.TransactionRecord;
import com.databases.example.data.TransactionWizardOptionalPage;
import com.databases.example.view.TransactionWizardOptionalFragment;
import com.databases.example.view.TransactionViewFragment;
import com.databases.example.view.TransactionWizard;
import com.databases.example.view.TransactionsListViewAdapter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Transactions extends SherlockFragment implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int TRANS_LOADER = 987654321;
    private static final int TRANS_SEARCH_LOADER = 98765;
    private static final int TRANS_SUBCATEGORY_LOADER = 987;

    private View myFragmentView;

    //Used to determine if fragment should show all transactions
    private boolean showAllTransactions = false;

    public static Button tTime;
    public static Button tDate;

    //ID of account transaction belongs to
    public static int account_id = 0;

    private static String sortOrder = "null";

    private ListView lv = null;

    //Constants for ContextMenu
    private final int CONTEXT_MENU_VIEW = 5;
    private final int CONTEXT_MENU_EDIT = 6;
    private final int CONTEXT_MENU_DELETE = 7;

    //ListView Adapter
    private static TransactionsListViewAdapter adapterTransactions = null;

    //For Autocomplete
    public static ArrayList<String> dropdownResults = new ArrayList<String>();

    //Adapter for category spinner
    public static SimpleCursorAdapter adapterCategory;

    //ActionMode
    private Object mActionMode = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account_id = 0;
    }//end onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.transactions, container, false);
        lv = (ListView) myFragmentView.findViewById(R.id.transaction_list);

        //Turn clicks on
        lv.setClickable(true);
        lv.setLongClickable(true);

        //Set Listener for regular mouse click
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                if (mActionMode != null) {
                    listItemChecked(position);
                } else {
                    int selectionRowID = (int) adapterTransactions.getItemId(position);
                    String item = adapterTransactions.getTransaction(position).name;

                    Toast.makeText(Transactions.this.getActivity(), "Click\nRow: " + selectionRowID + "\nEntry: " + item, Toast.LENGTH_SHORT).show();
                }
            }// end onItemClick

        }//end onItemClickListener
        );//end setOnItemClickListener

        lv.setOnItemLongClickListener(new OnItemLongClickListener() {

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);

        adapterCategory = new SimpleCursorAdapter(this.getActivity(), android.R.layout.simple_spinner_item, null, new String[]{DatabaseHelper.SUBCATEGORY_NAME}, new int[]{android.R.id.text1}, 0);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapterTransactions = new TransactionsListViewAdapter(this.getActivity(), null);
        lv.setAdapter(adapterTransactions);

        //Call Loaders to get data
        populate();

        //Arguments
        Bundle bundle = getArguments();

        //bundle is empty if from search, so don't add extra menu options
        if (bundle != null) {
            setHasOptionsMenu(true);
        }

        setRetainInstance(false);

        return myFragmentView;
    }

    //Used for ActionMode
    private void listItemChecked(int position) {
        adapterTransactions.toggleSelection(position);
        boolean hasCheckedItems = adapterTransactions.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            mActionMode = getSherlockActivity().startActionMode(new MyActionMode());
        } else if (!hasCheckedItems && mActionMode != null) {
            ((ActionMode) mActionMode).finish();
        }

        if (mActionMode != null) {
            ((ActionMode) mActionMode).invalidate();
            ((ActionMode) mActionMode).setTitle(String.valueOf(adapterTransactions.getSelectedCount()));
        }
    }

    //Populate view with all the transactions of selected account
    private void populate() {
        Bundle bundle = getArguments();
        boolean searchFragment = true;

        if (bundle != null) {
            showAllTransactions = bundle.getBoolean("showAll");
            searchFragment = bundle.getBoolean("boolSearch");

            if (!showAllTransactions && !searchFragment) {
                account_id = bundle.getInt("ID");
            }

            Log.v("Transactions-populate", "searchFragment=" + searchFragment + "\nshowAllTransactions=" + showAllTransactions + "\nAccount_id=" + account_id);
        }

        if (showAllTransactions) {
            Bundle b = new Bundle();
            b.putBoolean("boolShowAll", true);
            Log.v("Transactions-populate", "start loader (all transactions)...");
            getLoaderManager().initLoader(TRANS_LOADER, b, this);
        } else if (searchFragment) {
            String query = getActivity().getIntent().getStringExtra("query");

            try {
                Bundle b = new Bundle();
                b.putBoolean("boolSearch", true);
                b.putString("query", query);
                Log.v("Transactions-populate", "start search loader...");
                getLoaderManager().initLoader(TRANS_SEARCH_LOADER, b, this);
            } catch (Exception e) {
                Log.e("Transactions-populate", "Search Failed. Error e=" + e);
                Toast.makeText(this.getActivity(), "Search Failed\n" + e, Toast.LENGTH_SHORT).show();
                //return;
            }

        } else {
            Bundle b = new Bundle();
            b.putInt("aID", account_id);
            Log.v("Transactions-populate", "start loader (" + DatabaseHelper.TRANS_ACCT_ID + "=" + account_id + ")...");
            getLoaderManager().initLoader(TRANS_LOADER, b, this);
        }

        //Load the categories
        getLoaderManager().initLoader(TRANS_SUBCATEGORY_LOADER, null, this);
    }

    //For Adding a Transaction
    private void transactionAdd() {
        if (account_id == 0) {
            Log.e("Transaction-AddDialog", "No account selected before attempting to add transaction...");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("No Account Selected");
            alertDialogBuilder.setMessage("Please select an account before attempting to add a transaction");
            alertDialogBuilder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            alertDialogBuilder.create().show();
        } else {
            TransactionWizard frag = TransactionWizard.newInstance(null);
            frag.show(getChildFragmentManager(), "dialogAdd");
        }
    }//end of transactionAdd

    //For Sorting Transactions
    private void transactionSort() {
        DialogFragment newFragment = SortDialogFragment.newInstance();
        newFragment.show(getChildFragmentManager(), "dialogSort");
    }

    //For Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        View account_frame = getActivity().findViewById(R.id.account_frag_frame);

        if (account_frame != null) {
            SubMenu subMMenuTransaction = menu.addSubMenu("Transaction");
            subMMenuTransaction.add(com.actionbarsherlock.view.Menu.NONE, R.id.transaction_menu_add, com.actionbarsherlock.view.Menu.NONE, "Add");
            subMMenuTransaction.add(com.actionbarsherlock.view.Menu.NONE, R.id.transaction_menu_schedule, com.actionbarsherlock.view.Menu.NONE, "Schedule");
            subMMenuTransaction.add(com.actionbarsherlock.view.Menu.NONE, R.id.transaction_menu_sort, com.actionbarsherlock.view.Menu.NONE, "Sort");

            MenuItem subMenu1Item = subMMenuTransaction.getItem();
            subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        } else {
            menu.clear();
            inflater.inflate(R.layout.transaction_menu, menu);
            new SearchWidget(getSherlockActivity(), menu.findItem(R.id.transaction_menu_search).getActionView());
        }

    }

    //For Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Intent intentUp = new Intent(Transactions.this.getActivity(), Main.class);
                //intentUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intentUp);
                //menu.toggle();
                break;

            case R.id.transaction_menu_add:
                transactionAdd();
                return true;

            case R.id.transaction_menu_schedule:
                Intent intentPlans = new Intent(getActivity(), Plans.class);
                getActivity().startActivity(intentPlans);
                return true;

            case R.id.transaction_menu_sort:
                transactionSort();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    //Used after a change in settings occurs
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.e("Transactions-onSharedPreferenceChanged", "Options Changed");
        if (!isDetached()) {
            Log.e("Transactions-onSharedPreferenceChanged", "Transaction is attached");
            //Toast.makeText(this.getActivity(), "Transaction is attached", Toast.LENGTH_SHORT).show();
            //populate();
        } else {
            Log.e("Transactions-onSharedPreferenceChanged", "Transaction is detached");
            //Toast.makeText(this.getActivity(), "Transaction is detached", Toast.LENGTH_SHORT).show();
        }
    }

    //Method to help create TimePicker
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar cal = Calendar.getInstance();

            SimpleDateFormat dateFormatHour = new SimpleDateFormat("hh");
            SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");

            int hour = Integer.parseInt(dateFormatHour.format(cal.getTime()));
            int minute = Integer.parseInt(dateFormatMinute.format(cal.getTime()));

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    false);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            DateTime time = new DateTime();
            time.setStringSQL(hourOfDay + ":" + minute);

            if (tTime != null) {
                tTime.setText(time.getReadableTime());
            }

            if (TransactionWizardOptionalFragment.mPage != null) {
                TransactionWizardOptionalFragment.mPage.getData().putString(TransactionWizardOptionalPage.TIME_DATA_KEY, time.getReadableTime());
                TransactionWizardOptionalFragment.mPage.notifyDataChanged();
            }

        }
    }

    //Method to help create DatePicker
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

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            DateTime date = new DateTime();
            date.setStringSQL(year + "-" + (month + 1) + "-" + day);

            if (tDate != null) {
                tDate.setText(date.getReadableDate());
            }

            if (TransactionWizardOptionalFragment.mPage != null) {
                TransactionWizardOptionalFragment.mPage.getData().putString(TransactionWizardOptionalPage.DATE_DATA_KEY, date.getReadableDate());
                TransactionWizardOptionalFragment.mPage.notifyDataChanged();
            }

        }
    }

    //Class that handles sort dialog
    public static class SortDialogFragment extends SherlockDialogFragment {

        public static SortDialogFragment newInstance() {
            SortDialogFragment frag = new SortDialogFragment();
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
            View transactionSortView = li.inflate(R.layout.sort_transactions, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

            alertDialogBuilder.setView(transactionSortView);
            alertDialogBuilder.setTitle("Sort");
            alertDialogBuilder.setCancelable(true);

            ListView sortOptions = (ListView) transactionSortView.findViewById(R.id.sort_options);
            sortOptions.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    switch (position) {
                        //Newest
                        case 0:
                            sortOrder = DatabaseHelper.TRANS_DATE + " DESC, " + DatabaseHelper.TRANS_TIME + " DESC";
                            break;

                        //Oldest
                        case 1:
                            sortOrder = DatabaseHelper.TRANS_DATE + " ASC, " + DatabaseHelper.TRANS_TIME + " ASC";
                            break;

                        //Largest
                        case 2:
                            sortOrder = DatabaseHelper.TRANS_TYPE + " ASC, CAST (" + DatabaseHelper.TRANS_VALUE + " AS INTEGER)" + " DESC";
                            break;

                        //Smallest
                        case 3:
                            sortOrder = DatabaseHelper.TRANS_TYPE + " ASC, CAST (" + DatabaseHelper.TRANS_VALUE + " AS INTEGER)" + " ASC";
                            break;

                        //Category
                        case 4:
                            sortOrder = DatabaseHelper.TRANS_CATEGORY + " ASC";
                            break;

                        //Type
                        case 5:
                            sortOrder = DatabaseHelper.TRANS_TYPE + " ASC";
                            break;

                        //Alphabetical
                        case 6:
                            sortOrder = DatabaseHelper.TRANS_NAME + " ASC";
                            break;

                        //None
                        case 7:
                            sortOrder = null;
                            break;

                        default:
                            Log.e("Transactions-SortFragment", "Unknown Sorting Option!");
                            break;

                    }//end switch

                    getDialog().cancel();
                }
            });

            return alertDialogBuilder.create();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        switch (loaderID) {
            case TRANS_LOADER:
                if (bundle != null && bundle.getBoolean("boolShowAll")) {
                    Log.v("Transactions-onCreateLoader", "new loader (ShowAll) created");
                    return new CursorLoader(
                            getActivity(),    // Parent activity context
                            MyContentProvider.TRANSACTIONS_URI,// Table to query
                            null,                // Projection to return
                            null,                // No selection clause
                            null,                // No selection arguments
                            sortOrder            // Default sort order
                    );
                } else {
                    String selection = DatabaseHelper.TRANS_ACCT_ID + "=" + account_id;
                    Log.v("Transactions-onCreateLoader", "new loader created");
                    return new CursorLoader(
                            getActivity(),            // Parent activity context
                            MyContentProvider.TRANSACTIONS_URI,// Table to query
                            null,                        // Projection to return
                            selection,                    // No selection clause
                            null,                        // No selection arguments
                            sortOrder                    // Default sort order
                    );
                }
            case TRANS_SEARCH_LOADER:
                String query = getActivity().getIntent().getStringExtra("query");
                Log.v("Transactions-onCreateLoader", "new loader (boolSearch " + query + ") created");
                return new CursorLoader(
                        getActivity(),    // Parent activity context
                        (Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/SEARCH/" + query)),// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        sortOrder           // Default sort order
                );

            case TRANS_SUBCATEGORY_LOADER:
                Log.v("Transactions-onCreateLoader", "new category loader created");
                return new CursorLoader(
                        getActivity(),    // Parent activity context
                        MyContentProvider.SUBCATEGORIES_URI,// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        sortOrder           // Default sort order
                );

            default:
                Log.e("Transactions-onCreateLoader", "Not a valid CursorLoader ID");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        TextView footerTV = (TextView) this.myFragmentView.findViewById(R.id.transaction_footer);

        switch (loader.getId()) {
            case TRANS_LOADER:
                adapterTransactions.swapCursor(data);
                Log.v("Transactions-onLoadFinished", "loader finished. loader=" + loader.getId() + " data=" + data + " data size=" + data.getCount());

                final int valueColumn = data.getColumnIndex(DatabaseHelper.TRANS_VALUE);
                final int typeColumn = data.getColumnIndex(DatabaseHelper.TRANS_TYPE);
                BigDecimal totalBalance = BigDecimal.ZERO;
                Locale locale = getResources().getConfiguration().locale;

                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    if (data.getString(typeColumn).equals("Deposit")) {
                        totalBalance = totalBalance.add(new Money(data.getString(valueColumn)).getBigDecimal(locale));
                    } else {
                        totalBalance = totalBalance.subtract(new Money(data.getString(valueColumn)).getBigDecimal(locale));
                    }
                }

                try {
                    TextView noResult = (TextView) myFragmentView.findViewById(R.id.transaction_noTransaction);
                    lv.setEmptyView(noResult);
                    noResult.setText("No Transactions\n\n To Add A Transaction, Please Use The ActionBar On The Top");

                    footerTV.setText("Total Balance: " + new Money(totalBalance).getNumberFormat(locale));
                } catch (Exception e) {
                    Log.e("Transactions-onLoadFinished", "Error setting balance TextView. e=" + e);
                }

                if (account_id != 0) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.ACCOUNT_BALANCE, totalBalance + "");
                    getActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + account_id), values, DatabaseHelper.ACCOUNT_ID + "=" + account_id, null);
                }

                break;

            case TRANS_SEARCH_LOADER:
                adapterTransactions.swapCursor(data);
                Log.v("Transactions-onLoadFinished", "loader finished. loader=" + loader.getId() + " data=" + data + " data size=" + data.getCount());

                try {
                    TextView noResult = (TextView) myFragmentView.findViewById(R.id.transaction_noTransaction);
                    lv.setEmptyView(noResult);
                    noResult.setText("No Transactions Found");

                    footerTV.setText("Search Results");
                } catch (Exception e) {
                    Log.e("Transactions-onLoadFinished", "Error setting search TextView. e=" + e);
                }
                break;

            case TRANS_SUBCATEGORY_LOADER:
                adapterCategory.swapCursor(data);
                break;

            default:
                Log.e("Transactions-onLoadFinished", "Error. Unknown loader (" + loader.getId());
                break;
        }

        if (!getSherlockActivity().getSupportLoaderManager().hasRunningLoaders()) {
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TRANS_LOADER:
                adapterTransactions.swapCursor(null);
                Log.v("Transactions-onLoaderReset", "loader reset. loader=" + loader.getId());
                break;

            case TRANS_SEARCH_LOADER:
                adapterTransactions.swapCursor(null);
                Log.v("Transactions-onLoaderReset", "loader reset. loader=" + loader.getId());
                break;

            case TRANS_SUBCATEGORY_LOADER:
                adapterCategory.swapCursor(null);
                Log.v("Transactions-onLoaderReset", "loader reset. loader=" + loader.getId());
                break;

            default:
                Log.e("Transactions-onLoadFinished", "Error. Unknown loader (" + loader.getId());
                break;
        }
    }

    private final class MyActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(0, CONTEXT_MENU_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);
            menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            if (adapterTransactions.getSelectedCount() == 1 && mode != null) {
                menu.add(0, CONTEXT_MENU_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);
                menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
                menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
                return true;
            } else if (adapterTransactions.getSelectedCount() > 1) {
                menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
                return true;
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            SparseBooleanArray selected = adapterTransactions.getSelectedIds();

            switch (item.getItemId()) {
                case CONTEXT_MENU_VIEW:
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            DialogFragment newFragment = TransactionViewFragment.newInstance(adapterTransactions.getTransaction(selected.keyAt(i)).id);
                            newFragment.show(getChildFragmentManager(), "dialogView");
                        }
                    }

                    mode.finish();
                    return true;
                case CONTEXT_MENU_EDIT:
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            final TransactionRecord record = adapterTransactions.getTransaction(selected.keyAt(i));
                            final TransactionWizard frag = TransactionWizard.newInstance(record);
                            frag.show(getChildFragmentManager(), "dialogEdit");
                        }
                    }

                    mode.finish();
                    return true;
                case CONTEXT_MENU_DELETE:
                    TransactionRecord record;
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.valueAt(i)) {
                            record = adapterTransactions.getTransaction(selected.keyAt(i));

                            Uri uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + record.id);
                            getActivity().getContentResolver().delete(uri, DatabaseHelper.TRANS_ID + "=" + record.id, null);

                            Toast.makeText(getActivity(), "Deleted Transaction:\n" + record.name, Toast.LENGTH_SHORT).show();
                        }
                    }

                    mode.finish();
                    return true;

                default:
                    mode.finish();
                    Log.e("Transactions-onActionItemClciked", "ERROR. Clicked " + item);
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            adapterTransactions.removeSelection();
        }
    }

    @Override
    public void onDestroyView() {
        if (mActionMode != null) {
            ((ActionMode) mActionMode).finish();
        }

        super.onDestroyView();
    }

}//end Transactions