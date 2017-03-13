/* Class that handles the Transaction Fragment seen in the Checkbook screen
 * Does everything from setting up the view to Add/Delete/Edit Transactions to calculating the balance
 */

package com.databases.example.features.checkbook.transactions;

import android.app.AlertDialog;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.app.BaseActionMode;
import com.databases.example.app.BaseActionModeInterface;
import com.databases.example.app.RecyclerViewListener;
import com.databases.example.database.DatabaseHelper;
import com.databases.example.database.MyContentProvider;
import com.databases.example.features.checkbook.CheckbookActivity;
import com.databases.example.features.checkbook.accounts.Account;
import com.databases.example.features.checkbook.accounts.AccountsFragment;
import com.databases.example.features.plans.PlansActivity;
import com.databases.example.features.search.SearchActivity;
import com.databases.example.features.search.SearchWidget;
import com.databases.example.utils.Constants;
import com.databases.example.utils.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

import timber.log.Timber;

public class TransactionsFragment extends Fragment implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor>, BaseActionModeInterface {
    public static final String TRANSACTION_FRAG_TAG = "transaction_frag_tag";

    public static final int TRANS_LOADER = 987654321;
    public static final int TRANS_SEARCH_LOADER = 98765;
    public static final int TRANS_SUBCATEGORY_LOADER = 987;

    private View myFragmentView;

    //Used to determine if fragment should show all transactions
    private boolean showAllTransactions = false;

    //ID of account transaction belongs to
    //TODO make non-static
    public static Account account;

    private RecyclerView recyclerView = null;

    //Constants for ContextMenu
    public static final int CONTEXT_MENU_VIEW = 5;
    public static final int CONTEXT_MENU_EDIT = 6;
    public static final int CONTEXT_MENU_DELETE = 7;

    //RecyclerView Adapter
    private static TransactionsRecyclerViewAdapter adapterTransactions = null;

    //For Autocomplete
    public static ArrayList<String> dropdownResults = new ArrayList<String>();

    //Adapter for category spinner
    public static SimpleCursorAdapter adapterCategory;

    //ActionMode
    private Object mActionMode = null;

    private final String SHOW_ALL_TRANSACTIONS = "boolShowAll";

    private final String ADD_FRAGMENT_TAG = "transaction_add_fragment";
    private final String EDIT_FRAGMENT_TAG = "transaction_edit_fragment";
    private final String VIEW_FRAGMENT_TAG = "transaction_view_fragment";
    private final String SORT_FRAGMENT_TAG = "transaction_sort_fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.transactions, container, false);
        recyclerView = (RecyclerView) myFragmentView.findViewById(R.id.transaction_list);

        //Turn clicks on
        recyclerView.setClickable(true);
        recyclerView.setLongClickable(true);

        //Set up a listener for changes in settings menu
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);

        adapterCategory = new SimpleCursorAdapter(this.getActivity(), android.R.layout.simple_spinner_item, null, new String[]{DatabaseHelper.SUBCATEGORY_NAME}, new int[]{android.R.id.text1}, 0);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapterTransactions = new TransactionsRecyclerViewAdapter(getActivity(), null, new RecyclerViewListener() {
            @Override
            public void onItemClick(Object model, int position) {
                if (mActionMode != null) {
                    listItemChecked(position);
                } else {
                    String item = adapterTransactions.getTransaction(position).name;
                    Toast.makeText(TransactionsFragment.this.getActivity(), "Click\nRow: " + position + "\nEntry: " + item, Toast.LENGTH_SHORT).show();
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
        recyclerView.setAdapter(adapterTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

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
            BaseActionMode baseActionMode = new BaseActionMode();
            baseActionMode.setBaseActionModeInterface(this);
            mActionMode = getActivity().startActionMode(baseActionMode);
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
            showAllTransactions = bundle.getBoolean(CheckbookActivity.SHOW_ALL_KEY);
            searchFragment = bundle.getBoolean(SearchActivity.BOOLEAN_SEARCH_KEY);

            if (!showAllTransactions && !searchFragment) {
                account = bundle.getParcelable(AccountsFragment.ACCOUNT_KEY);
            }

            Timber.v("searchFragment=" + searchFragment + "\nshowAllTransactions=" + showAllTransactions + "\nAccount_id=" + account);
        }

        if (showAllTransactions) {
            Bundle b = new Bundle();
            b.putBoolean(SHOW_ALL_TRANSACTIONS, true);
            Timber.v("start loader (all transactions)...");
            getLoaderManager().initLoader(TRANS_LOADER, b, this);
        } else if (searchFragment) {
            String query = getActivity().getIntent().getStringExtra(SearchActivity.QUERY_KEY);

            try {
                Bundle b = new Bundle();
                b.putBoolean(SearchActivity.BOOLEAN_SEARCH_KEY, true);
                b.putString(SearchActivity.QUERY_KEY, query);
                Timber.v("start search loader...");
                getLoaderManager().initLoader(TRANS_SEARCH_LOADER, b, this);
            } catch (Exception e) {
                Timber.e("SearchActivity Failed. Error e=" + e);
                Toast.makeText(getActivity(), "SearchActivity Failed\n" + e, Toast.LENGTH_SHORT).show();
                //return;
            }

        } else {
            Bundle b = new Bundle();
            b.putParcelable("aID", account);    //Not sure if we need this, "aID" doesn't seem to be used anywhere...
            Timber.v("start loader (" + DatabaseHelper.TRANS_ACCT_ID + "=" + account.id + ")...");
            getLoaderManager().initLoader(TRANS_LOADER, b, this);
        }

        //Load the categories
        getLoaderManager().initLoader(TRANS_SUBCATEGORY_LOADER, null, this);
    }

    //For Adding a Transaction
    private void transactionAdd() {
        if (account == null) {
            Timber.w("No account selected before attempting to add transaction...");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(R.string.no_account_selected);
            alertDialogBuilder.setMessage(R.string.select_an_account);
            alertDialogBuilder.setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            alertDialogBuilder.create().show();
        } else {
            TransactionWizard frag = TransactionWizard.newInstance(null);
            frag.show(getChildFragmentManager(), ADD_FRAGMENT_TAG);
        }
    }

    //For Sorting TransactionsFragment
    private void transactionSort() {
        DialogFragment newFragment = TransactionSortDialogFragment.newInstance();
        newFragment.show(getChildFragmentManager(), SORT_FRAGMENT_TAG);
    }

    //For Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        View account_frame = getActivity().findViewById(R.id.account_frag_frame);

        if (account_frame != null) {
            SubMenu subMMenuTransaction = menu.addSubMenu(R.string.transaction);
            subMMenuTransaction.add(Menu.NONE, R.id.transaction_menu_add, Menu.NONE, R.string.add);
            subMMenuTransaction.add(Menu.NONE, R.id.transaction_menu_schedule, Menu.NONE, R.string.schedule);
            subMMenuTransaction.add(Menu.NONE, R.id.transaction_menu_sort, Menu.NONE, R.string.sort);

            MenuItem subMenu1Item = subMMenuTransaction.getItem();
            subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        } else {
            menu.clear();
            inflater.inflate(R.menu.transaction_menu, menu);
            new SearchWidget(getActivity(), (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.transaction_menu_search)));
        }
    }

    //For Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.transaction_menu_add:
                transactionAdd();
                return true;

            case R.id.transaction_menu_schedule:
                Intent intentPlans = new Intent(getActivity(), PlansActivity.class);
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
        Timber.v("OptionsActivity Changed");
        if (!isDetached()) {
            Timber.d("Transaction is attached");
            //Toast.makeText(this.getActivity(), "Transaction is attached", Toast.LENGTH_SHORT).show();
            //populate();
        } else {
            Timber.d("Transaction is detached");
            //Toast.makeText(this.getActivity(), "Transaction is detached", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        getActivity().setProgressBarIndeterminateVisibility(true);

        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_key_transaction_sort), null);

        switch (loaderID) {
            case TRANS_LOADER:
                if (bundle != null && bundle.getBoolean(SHOW_ALL_TRANSACTIONS)) {
                    Timber.v("new loader (ShowAll) created");
                    return new CursorLoader(
                            getActivity(),    // Parent activity context
                            MyContentProvider.TRANSACTIONS_URI,// Table to query
                            null,                // Projection to return
                            null,                // No selection clause
                            null,                // No selection arguments
                            sortOrder            // Default sort order
                    );
                } else {
                    String selection = DatabaseHelper.TRANS_ACCT_ID + "=" + account.id;
                    Timber.v("new loader created");
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
                String query = getActivity().getIntent().getStringExtra(SearchActivity.QUERY_KEY);
                Timber.v("new loader (boolSearch " + query + ") created");
                return new CursorLoader(
                        getActivity(),    // Parent activity context
                        (Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/SEARCH/" + query)),// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        sortOrder           // Default sort order
                );

            case TRANS_SUBCATEGORY_LOADER:
                Timber.v("new category loader created");
                return new CursorLoader(
                        getActivity(),    // Parent activity context
                        MyContentProvider.SUBCATEGORIES_URI,// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        sortOrder           // Default sort order
                );

            default:
                Timber.e("Not a valid CursorLoader ID");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        TextView footerTV = (TextView) this.myFragmentView.findViewById(R.id.transaction_footer);

        switch (loader.getId()) {
            case TRANS_LOADER:
                ArrayList<Transaction> transactions = Transaction.getTransactions(data);
                adapterTransactions.setTransactions(transactions);
                Timber.v("loader finished. loader=" + loader.getId() + " data=" + data + " data size=" + data.getCount());

                BigDecimal totalBalance = BigDecimal.ZERO;
                Locale locale = getResources().getConfiguration().locale;

                for (Transaction transaction : transactions) {
                    if (transaction.type.equals(Constants.DEPOSIT)) {
                        totalBalance = totalBalance.add(new Money(transaction.value).getBigDecimal(locale));
                    } else {
                        totalBalance = totalBalance.subtract(new Money(transaction.value).getBigDecimal(locale));
                    }
                }

                try {
                    footerTV.setText("Total Balance: " + new Money(totalBalance).getNumberFormat(locale));
                } catch (Exception e) {
                    Timber.e("Error setting balance TextView. e=" + e);
                }

                if (account != null) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.ACCOUNT_BALANCE, totalBalance + "");
                    getActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + account.id), values, DatabaseHelper.ACCOUNT_ID + "=" + account.id, null);
                }

                break;

            case TRANS_SEARCH_LOADER:
                adapterTransactions.setTransactions(Transaction.getTransactions(data));
                Timber.v("loader finished. loader=" + loader.getId() + " data=" + data + " data size=" + data.getCount());

                try {
                    footerTV.setText("SearchActivity Results");
                } catch (Exception e) {
                    Timber.e("Error setting search TextView. e=" + e);
                }
                break;

            case TRANS_SUBCATEGORY_LOADER:
                adapterCategory.swapCursor(data);
                break;

            default:
                Timber.e("Error. Unknown loader (" + loader.getId());
                break;
        }

        if (!getActivity().getSupportLoaderManager().hasRunningLoaders()) {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TRANS_LOADER:
                adapterTransactions.setTransactions(null);
                Timber.v("loader reset. loader=" + loader.getId());
                break;

            case TRANS_SEARCH_LOADER:
                adapterTransactions.setTransactions(null);
                Timber.v("loader reset. loader=" + loader.getId());
                break;

            case TRANS_SUBCATEGORY_LOADER:
                adapterCategory.swapCursor(null);
                Timber.v("loader reset. loader=" + loader.getId());
                break;

            default:
                Timber.e("Error. Unknown loader (" + loader.getId());
                break;
        }
    }

    @Override
    public void onDestroyView() {
        if (mActionMode != null) {
            ((ActionMode) mActionMode).finish();
        }

        super.onDestroyView();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.add(0, CONTEXT_MENU_VIEW, 0, R.string.view).setIcon(android.R.drawable.ic_menu_view);
        menu.add(0, CONTEXT_MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
        menu.add(0, CONTEXT_MENU_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu.clear();
        if (adapterTransactions.getSelectedCount() == 1 && mode != null) {
            menu.add(0, CONTEXT_MENU_VIEW, 0, R.string.view).setIcon(android.R.drawable.ic_menu_view);
            menu.add(0, CONTEXT_MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, CONTEXT_MENU_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
            return true;
        } else if (adapterTransactions.getSelectedCount() > 1) {
            menu.add(0, CONTEXT_MENU_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
            return true;
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        adapterTransactions.removeSelection();
    }

    @Override
    public boolean viewClicked(ActionMode mode, MenuItem item, SparseBooleanArray selectedIds) {
        for (int i = 0; i < selectedIds.size(); i++) {
            if (selectedIds.valueAt(i)) {
                DialogFragment newFragment = TransactionViewFragment.newInstance(adapterTransactions.getTransaction(selectedIds.keyAt(i)).id);
                newFragment.show(getChildFragmentManager(), VIEW_FRAGMENT_TAG);
            }
        }

        mode.finish();
        return true;
    }

    @Override
    public boolean editClicked(ActionMode mode, MenuItem item, SparseBooleanArray selectedIds) {
        for (int i = 0; i < selectedIds.size(); i++) {
            if (selectedIds.valueAt(i)) {
                final Transaction record = adapterTransactions.getTransaction(selectedIds.keyAt(i));
                final TransactionWizard frag = TransactionWizard.newInstance(record);
                frag.show(getChildFragmentManager(), EDIT_FRAGMENT_TAG);
            }
        }

        mode.finish();
        return true;
    }

    @Override
    public boolean deleteClicked(ActionMode mode, MenuItem item, SparseBooleanArray selectedIds) {
        Transaction record;
        for (int i = 0; i < selectedIds.size(); i++) {
            if (selectedIds.valueAt(i)) {
                record = adapterTransactions.getTransaction(selectedIds.keyAt(i));

                Uri uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + record.id);
                getActivity().getContentResolver().delete(uri, DatabaseHelper.TRANS_ID + "=" + record.id, null);

                Toast.makeText(getActivity(), "Deleted Transaction:\n" + record.name, Toast.LENGTH_SHORT).show();
            }
        }

        mode.finish();
        return true;
    }

    @Override
    public SparseBooleanArray getSelectedIds() {
        return adapterTransactions.getSelectedIds();
    }
}