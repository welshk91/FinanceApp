/* Class that handles the Account Fragment seen in the Checkbook screen
 * Does everything from setting up the view to Add/Delete/Edit Accounts to calculating the balance
 */

package com.databases.example.features.checkbook.accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.app.BaseActionMode;
import com.databases.example.app.BaseActionModeInterface;
import com.databases.example.app.LinksActivity;
import com.databases.example.app.RecyclerViewListener;
import com.databases.example.database.DatabaseHelper;
import com.databases.example.database.MyContentProvider;
import com.databases.example.features.checkbook.CheckbookActivity;
import com.databases.example.features.checkbook.transactions.TransactionsFragment;
import com.databases.example.features.search.SearchActivity;
import com.databases.example.features.search.SearchWidget;
import com.databases.example.utils.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

import timber.log.Timber;

public class AccountsFragment extends Fragment implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor>, BaseActionModeInterface {
    public static final String ACCOUNT_FRAG_TAG = "account_frag_tag";

    private static final int PICKFILE_RESULT_CODE = 1;
    public static final int ACCOUNTS_LOADER = 123456789;
    public static final int ACCOUNTS_SEARCH_LOADER = 12345;

    public static final int CONTEXT_MENU_VIEW = 1;
    public static final int CONTEXT_MENU_EDIT = 2;
    public static final int CONTEXT_MENU_DELETE = 3;

    private View myFragmentView;

    private RecyclerView recyclerView = null;
    public static AccountsRecyclerViewAdapter adapterAccounts = null;

    public static Object mActionMode = null;

    private final String CURRENT_ACCOUNT_KEY = "currentAccount";
    public static int currentAccount = -1;

    public static final String ACCOUNT_KEY = "ID";

    private final String ADD_FRAGMENT_TAG = "account_add_fragment";
    private final String EDIT_FRAGMENT_TAG = "account_edit_fragment";
    private final String VIEW_FRAGMENT_TAG = "account_view_fragment";
    private final String TRANSFER_FRAGMENT_TAG = "account_transfer_fragment";
    private final String SORT_FRAGMENT_TAG = "account_sort_fragment";

    //Method called upon first creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            currentAccount = savedInstanceState.getInt(CURRENT_ACCOUNT_KEY);
        }

    }// end onCreate

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(CURRENT_ACCOUNT_KEY, currentAccount);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.accounts, container, false);
        recyclerView = (RecyclerView) myFragmentView.findViewById(R.id.account_list);

        recyclerView.setClickable(true);
        recyclerView.setLongClickable(true);

        //Set up a listener for changes in settings menu
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);

        adapterAccounts = new AccountsRecyclerViewAdapter(getActivity(), null, new RecyclerViewListener() {
            @Override
            public void onItemClick(final Object object, int position) {
                if (mActionMode != null) {
                    listItemChecked(position);
                } else {
                    Account account = (Account) object;
                    View checkbook_frame = getActivity().findViewById(R.id.checkbook_frag_frame);

                    if (checkbook_frame != null) {
                        Bundle args = new Bundle();
                        args.putParcelable(ACCOUNT_KEY, account);

                        //Add the fragment to the activity, pushing this transaction on to the back stack.
                        TransactionsFragment tran_frag = new TransactionsFragment();
                        tran_frag.setArguments(args);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                        ft.replace(R.id.checkbook_frag_frame, tran_frag);
                        ft.addToBackStack(null);
                        ft.commit();
                        getFragmentManager().executePendingTransactions();
                    } else {
                        Bundle args = new Bundle();
                        args.putBoolean(CheckbookActivity.SHOW_ALL_KEY, false);
                        args.putBoolean(SearchActivity.BOOLEAN_SEARCH_KEY, false);
                        args.putParcelable(ACCOUNT_KEY, account);

                        currentAccount = position;

                        //Add the fragment to the activity
                        //NOTE: Don't add custom animation, seems to mess with onLoaderReset
                        TransactionsFragment tran_frag = new TransactionsFragment();
                        tran_frag.setArguments(args);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.replace(R.id.transaction_frag_frame, tran_frag);
                        ft.commit();
                        getFragmentManager().executePendingTransactions();
                    }
                }
            }

            @Override
            public boolean onItemLongClick(Object account, int position) {
                if (mActionMode != null) {
                    return false;
                }

                listItemChecked(position);
                return true;
            }
        });

        recyclerView.setAdapter(adapterAccounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

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

    private void listItemChecked(int position) {
        adapterAccounts.toggleSelection(position);
        boolean hasCheckedItems = adapterAccounts.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            BaseActionMode baseActionMode = new BaseActionMode();
            baseActionMode.setBaseActionModeInterface(this);
            mActionMode = getActivity().startActionMode(baseActionMode);
        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the actionMode
            ((ActionMode) mActionMode).finish();
        }

        if (mActionMode != null) {
            ((ActionMode) mActionMode).invalidate();
            ((ActionMode) mActionMode).setTitle(String.valueOf(adapterAccounts.getSelectedCount()));
        }
    }

    //Populate view with accounts
    private void populate() {
        Bundle bundle = getArguments();
        boolean searchFragment = true;

        if (bundle != null) {
            searchFragment = bundle.getBoolean(SearchActivity.BOOLEAN_SEARCH_KEY);
        }

        //Fragment is a search fragment
        if (searchFragment) {

            //Word being searched
            String query = getActivity().getIntent().getStringExtra(SearchActivity.QUERY_KEY);

            try {
                Bundle b = new Bundle();
                b.putBoolean(SearchActivity.BOOLEAN_SEARCH_KEY, true);
                b.putString(SearchActivity.QUERY_KEY, query);
                Timber.v("start search loader...");
                getLoaderManager().initLoader(ACCOUNTS_SEARCH_LOADER, b, this);
            } catch (Exception e) {
                Timber.e("SearchActivity Failed. Error e=" + e);
                Toast.makeText(this.getActivity(), "SearchActivity Failed\n" + e, Toast.LENGTH_LONG).show();
            }

        }

        //Not A SearchActivity Fragment
        else {
            Timber.v("start loader...");
            getLoaderManager().initLoader(ACCOUNTS_LOADER, bundle, this);
        }

    }

    //For Attaching to an Account
    public void accountAttach(android.view.MenuItem item) {
        final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Account record = adapterAccounts.getAccount(itemInfo.position);

        Intent intentLink = new Intent(this.getActivity(), LinksActivity.class);
        intentLink.putExtra(DatabaseHelper.ACCOUNT_ID, record.id);
        intentLink.putExtra(DatabaseHelper.ACCOUNT_NAME, record.name);
        startActivityForResult(intentLink, PICKFILE_RESULT_CODE);
    }

    //For Adding an Account
    private void accountAdd() {
        AccountWizard newFragment = AccountWizard.newInstance(null);
        newFragment.show(getChildFragmentManager(), ADD_FRAGMENT_TAG);
    }

    //For Transferring from an Account
    private void accountTransfer() {
        DialogFragment newFragment = AccountTransferFragment.newInstance();
        newFragment.show(getChildFragmentManager(), TRANSFER_FRAGMENT_TAG);
    }

    //For Sorting AccountsFragment
    private void accountSort() {
        DialogFragment newFragment = AccountSortDialogFragment.newInstance();
        newFragment.show(getChildFragmentManager(), SORT_FRAGMENT_TAG);
    }

    //For Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        View transaction_frame = getActivity().findViewById(R.id.transaction_frag_frame);

        //Clear any leftover junk
        menu.clear();

        //If you're in dual-pane mode
        if (transaction_frame != null) {
            MenuItem menuSearch = menu.add(Menu.NONE, R.id.account_menu_search, Menu.NONE, R.string.search);
            menuSearch.setIcon(android.R.drawable.ic_menu_search);
            menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            menuSearch.setActionView(new SearchView(((AppCompatActivity) getActivity()).getSupportActionBar().getThemedContext()));

            //Create SearchWidget
            new SearchWidget(getActivity(), (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.account_menu_search)));

            SubMenu subMenu1 = menu.addSubMenu(R.string.account);
            subMenu1.add(Menu.NONE, R.id.account_menu_add, Menu.NONE, R.string.add);
            subMenu1.add(Menu.NONE, R.id.account_menu_transfer, Menu.NONE, R.string.transfer);
            subMenu1.add(Menu.NONE, R.id.account_menu_sort, Menu.NONE, R.string.sort);

            MenuItem subMenu1Item = subMenu1.getItem();
            subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        } else {
            inflater.inflate(R.menu.account_menu, menu);

            //Create SearchWidget
            new SearchWidget(getActivity(), (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.account_menu_search)));
        }

    }

    //For Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Intent intentUp = new Intent(AccountsFragment.this.getActivity(), MainActivity.class);
                //intentUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intentUp);
                //menu.toggle();
                break;

            case R.id.account_menu_add:
                accountAdd();
                return true;

            case R.id.account_menu_transfer:
                if (adapterAccounts.getItemCount() < 2) {
                    Toast.makeText(getActivity(), "Not Enough Accounts For Transfer \n\nUse The ActionBar To Create Accounts", Toast.LENGTH_LONG).show();
                } else {
                    accountTransfer();
                }
                return true;

            case R.id.account_menu_sort:
                accountSort();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Used after a change in settings occurs
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (!isDetached()) {
            Timber.d("Settings changed. Requery");
            //getActivity().getContentResolver().notifyChange(MyContentProvider.ACCOUNTS_URI, null);
            //getLoaderManager().restartLoader(ACCOUNTS_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        getActivity().setProgressBarIndeterminateVisibility(true);
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_key_account_sort), null);

        Timber.d("calling create loader...");
        switch (loaderID) {
            case ACCOUNTS_LOADER:
                Timber.v("new loader created");
                return new CursorLoader(
                        getActivity(),    // Parent activity context
                        MyContentProvider.ACCOUNTS_URI,// Table to query
                        null,                // Projection to return
                        null,                // No selection clause
                        null,                // No selection arguments
                        sortOrder           // Default sort order-> "CAST (AcctBalance AS INTEGER)" + " DESC"
                );
            case ACCOUNTS_SEARCH_LOADER:
                String query = getActivity().getIntent().getStringExtra(SearchActivity.QUERY_KEY);
                Timber.v("new loader (boolSearch " + query + ") created");
                return new CursorLoader(
                        getActivity(),    // Parent activity context
                        (Uri.parse(MyContentProvider.ACCOUNTS_URI + "/SEARCH/" + query)),// Table to query
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
        TextView footerTV = (TextView) this.myFragmentView.findViewById(R.id.account_footer);

        switch (loader.getId()) {
            case ACCOUNTS_LOADER:
                ArrayList<Account> accounts = Account.getAccounts(data);
                adapterAccounts.setAccounts(accounts);
                Timber.v("loader finished. loader=" + loader.getId() + " data=" + data + " data size=" + data.getCount());

                BigDecimal totalBalance = BigDecimal.ZERO;
                Locale locale = getResources().getConfiguration().locale;

                for (Account account : accounts) {
                    totalBalance = totalBalance.add(new Money(account.balance).getBigDecimal(locale));
                }

                try {
                    footerTV.setText("Total Balance: " + new Money(totalBalance).getNumberFormat(locale));
                } catch (Exception e) {
                    Timber.e("Error setting balance TextView. e=" + e);
                }

                break;

            case ACCOUNTS_SEARCH_LOADER:
                adapterAccounts.setAccounts(Account.getAccounts(data));
                Timber.v("loader finished. loader=" + loader.getId() + " data=" + data + " data size=" + data.getCount());
                footerTV.setText(R.string.search_results);
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
            case ACCOUNTS_LOADER:
                adapterAccounts.setAccounts(null);
                Timber.v("loader reset. loader=" + loader.getId());
                break;

            case ACCOUNTS_SEARCH_LOADER:
                adapterAccounts.setAccounts(null);
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
        if (adapterAccounts.getSelectedCount() == 1 && mode != null) {
            menu.add(0, CONTEXT_MENU_VIEW, 0, R.string.view).setIcon(android.R.drawable.ic_menu_view);
            menu.add(0, CONTEXT_MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, CONTEXT_MENU_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
        } else if (adapterAccounts.getSelectedCount() > 1) {
            menu.add(0, CONTEXT_MENU_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        adapterAccounts.removeSelection();
    }

    @Override
    public boolean viewClicked(ActionMode mode, MenuItem item, SparseBooleanArray selectedIds) {
        for (int i = 0; i < selectedIds.size(); i++) {
            if (selectedIds.valueAt(i)) {
                DialogFragment newFragment = AccountViewFragment.newInstance(adapterAccounts.getAccount(selectedIds.keyAt(i)));
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
                final Account record = adapterAccounts.getAccount(selectedIds.keyAt(i));
                AccountWizard newFragment = AccountWizard.newInstance(record);
                newFragment.show(getChildFragmentManager(), EDIT_FRAGMENT_TAG);
            }
        }

        mode.finish();
        return true;
    }

    @Override
    public boolean deleteClicked(ActionMode mode, MenuItem item, SparseBooleanArray selectedIds) {
        Account record;
        for (int i = 0; i < selectedIds.size(); i++) {
            if (selectedIds.valueAt(i)) {
                record = adapterAccounts.getAccount(selectedIds.keyAt(i));

                //Delete Account
                Uri uri = Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + record.id);
                getActivity().getContentResolver().delete(uri, DatabaseHelper.ACCOUNT_ID + "=" + record.id, null);

                //Delete All TransactionsFragment of that account
                uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + 0);
                getActivity().getContentResolver().delete(uri, DatabaseHelper.TRANS_ACCT_ID + "=" + record.id, null);

                Toast.makeText(getActivity(), "Deleted Account:\n" + record.name, Toast.LENGTH_SHORT).show();
            }
        }

        mode.finish();
        return true;
    }

    @Override
    public SparseBooleanArray getSelectedIds(){
        return adapterAccounts.getSelectedIds();
    }
}