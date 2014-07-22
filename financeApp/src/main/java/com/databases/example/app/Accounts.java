/* Class that handles the Account Fragment seen in the Checkbook screen
 * Does everything from setting up the view to Add/Delete/Edit Accounts to calculating the balance
 */

package com.databases.example.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.SearchView;
import com.databases.example.R;
import com.databases.example.data.AccountRecord;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.Money;
import com.databases.example.data.MyContentProvider;
import com.databases.example.data.SearchWidget;
import com.databases.example.view.AccountAddFragment;
import com.databases.example.view.AccountEditFragment;
import com.databases.example.view.AccountTransferFragment;
import com.databases.example.view.AccountViewFragment;
import com.databases.example.view.AccountsListViewAdapter;

import java.math.BigDecimal;
import java.util.Locale;

public class Accounts extends SherlockFragment implements OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int ACCOUNTS_LOADER = 123456789;
    private static final int ACCOUNTS_SEARCH_LOADER = 12345;

    //Constants for ContextMenu
    final private int CONTEXT_MENU_VIEW=1;
    final private int CONTEXT_MENU_EDIT=2;
    final private int CONTEXT_MENU_DELETE=3;

    //Spinners for transfers
    private Cursor accountCursor = null;
    public static Spinner transferSpinnerTo;
    public static Spinner transferSpinnerFrom;
    public static SimpleCursorAdapter transferSpinnerAdapterFrom = null;
    public static SimpleCursorAdapter transferSpinnerAdapterTo = null;

    private View myFragmentView;
    private static String sortOrder= "null";

    private ListView lv = null;
    private static AccountsListViewAdapter adapterAccounts = null;

    public static Object mActionMode = null;
    public static int currentAccount=-1;

    //Method called upon first creation
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            currentAccount = savedInstanceState.getInt("currentAccount");
        }

    }// end onCreate

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("currentAccount", currentAccount);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.accounts, null, false);
        lv = (ListView)myFragmentView.findViewById(R.id.account_list);

        lv.setClickable(true);
        lv.setLongClickable(true);

        //Set Listener for regular mouse click
        lv.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {

                if (mActionMode != null) {
                    listItemChecked(position);
                }

                else{
                    int selectionRowID = (int) adapterAccounts.getItemId(position);
                    Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.ACCOUNTS_URI+"/"+(selectionRowID)), null, null, null, null);

                    //Just get the Account ID
                    c.moveToFirst();
                    int	entry_id = c.getInt(0);
                    c.close();

                    View checkbook_frame = getActivity().findViewById(R.id.checkbook_frag_frame);

                    if(checkbook_frame!=null){
                        Bundle args = new Bundle();
                        args.putInt("ID",entry_id);

                        //Add the fragment to the activity, pushing this transaction on to the back stack.
                        Transactions tran_frag = new Transactions();
                        tran_frag.setArguments(args);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left,android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        ft.replace(R.id.checkbook_frag_frame, tran_frag);
                        ft.addToBackStack(null);
                        ft.commit();
                        getFragmentManager().executePendingTransactions();
                    }
                    else{
                        Bundle args = new Bundle();
                        args.putBoolean("showAll", false);
                        args.putBoolean("boolSearch", false);
                        args.putInt("ID",entry_id);

                        currentAccount=position;

                        //Add the fragment to the activity
                        //NOTE: Don't add custom animation, seems to mess with onLoaderReset
                        Transactions tran_frag = new Transactions();
                        tran_frag.setArguments(args);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.replace(R.id.transaction_frag_frame, tran_frag);
                        ft.commit();
                        getFragmentManager().executePendingTransactions();
                    }
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

        adapterAccounts = new AccountsListViewAdapter(this.getActivity(), null);
        lv.setAdapter(adapterAccounts);

        populate();

        //Arguments
        Bundle bundle=getArguments();

        //bundle is empty if from search, so don't add extra menu options
        if(bundle!=null){
            setHasOptionsMenu(true);
        }

        setRetainInstance(false);

        return myFragmentView;
    }

    //Used for ActionMode
    public void listItemChecked(int position){
        adapterAccounts.toggleSelection(position);
        boolean hasCheckedItems = adapterAccounts.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null){
            // there are some selected items, start the actionMode
            mActionMode = getSherlockActivity().startActionMode(new MyActionMode());
        }
        else if (!hasCheckedItems && mActionMode != null){
            // there no selected items, finish the actionMode
            ((ActionMode) mActionMode).finish();
        }

        if(mActionMode != null){
            ((ActionMode) mActionMode).invalidate();
            ((ActionMode)mActionMode).setTitle(String.valueOf(adapterAccounts.getSelectedCount()));
        }
    }

    //Populate view with accounts
    protected void populate(){
        Bundle bundle=getArguments();
        boolean searchFragment=true;

        if(bundle!=null){
            searchFragment = bundle.getBoolean("boolSearch");
        }

        //Fragment is a search fragment
        if(searchFragment){

            //Word being searched
            String query = getActivity().getIntent().getStringExtra("query");

            try{
                Bundle b = new Bundle();
                b.putBoolean("boolSearch", true);
                b.putString("query", query);
                Log.v("Accounts-populate","start search loader...");
                getLoaderManager().initLoader(ACCOUNTS_SEARCH_LOADER, b, this);
            }
            catch(Exception e){
                Log.e("Accounts-populate","Search Failed. Error e="+e);
                Toast.makeText(this.getActivity(), "Search Failed\n"+e, Toast.LENGTH_LONG).show();
            }

        }

        //Not A Search Fragment
        else{
            Log.v("Accounts-populate","start loader...");
            getLoaderManager().initLoader(ACCOUNTS_LOADER, bundle, this);
        }

    }

    //For Attaching to an Account
    public void accountAttach(android.view.MenuItem item){
        final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final AccountRecord record = adapterAccounts.getAccount(itemInfo.position);

        Intent intentLink = new Intent(this.getActivity(), Links.class);
        intentLink.putExtra(DatabaseHelper.ACCOUNT_ID, record.id);
        intentLink.putExtra(DatabaseHelper.ACCOUNT_NAME, record.name);
        startActivityForResult(intentLink, PICKFILE_RESULT_CODE);
    }

    //For Adding an Account
    public void accountAdd(){
        DialogFragment newFragment = AccountAddFragment.newInstance();
        newFragment.show(getChildFragmentManager(), "dialogAdd");
    }

    //For Transferring from an Account
    public void accountTransfer(){
        DialogFragment newFragment = AccountTransferFragment.newInstance();
        newFragment.show(getChildFragmentManager(), "dialogTransfer");
    }

    //For Sorting Accounts
    public void accountSort(){
        DialogFragment newFragment = SortDialogFragment.newInstance();
        newFragment.show(getChildFragmentManager(), "dialogSort");
    }

    //Method to get the list of accounts for transfer spinner
    public void accountPopulate(){
        String[] from = new String[] {DatabaseHelper.ACCOUNT_ID, "_id"};
        int[] to = new int[] { android.R.id.text1};

        transferSpinnerAdapterFrom = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, accountCursor, from, to,0);
        transferSpinnerAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        transferSpinnerAdapterTo = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, accountCursor, from, to,0);
        transferSpinnerAdapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        transferSpinnerTo.setAdapter(transferSpinnerAdapterTo);
        transferSpinnerFrom.setAdapter(transferSpinnerAdapterFrom);

    }//end of accountPopulate

    //For Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        View transaction_frame = getActivity().findViewById(R.id.transaction_frag_frame);

        //Clear any leftover junk
        menu.clear();

        //If you're in dual-pane mode
        if(transaction_frame!=null){
            MenuItem menuSearch = menu.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_search, com.actionbarsherlock.view.Menu.NONE, "Search");
            menuSearch.setIcon(android.R.drawable.ic_menu_search);
            menuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            menuSearch.setActionView(new SearchView(getSherlockActivity().getSupportActionBar().getThemedContext()));

            SearchWidget searchWidget = new SearchWidget(getActivity(),menuSearch.getActionView());

            SubMenu subMenu1 = menu.addSubMenu("Account");
            subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_add, com.actionbarsherlock.view.Menu.NONE, "Add");
            subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_transfer, com.actionbarsherlock.view.Menu.NONE, "Transfer");
            subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_sort, com.actionbarsherlock.view.Menu.NONE, "Sort");
            subMenu1.add(com.actionbarsherlock.view.Menu.NONE, R.id.account_menu_unknown, com.actionbarsherlock.view.Menu.NONE, "Unknown");

            MenuItem subMenu1Item = subMenu1.getItem();
            subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        else{
            inflater.inflate(R.layout.account_menu, menu);
            SearchWidget searchWidget = new SearchWidget(getActivity(),menu.findItem(R.id.account_menu_search).getActionView());
        }

    }

    //For Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Intent intentUp = new Intent(Accounts.this.getActivity(), Main.class);
                //intentUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intentUp);
                //menu.toggle();
                break;

            case R.id.account_menu_add:
                accountAdd();
                return true;

            case R.id.account_menu_transfer:
                accountTransfer();
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
        if(!isDetached()){
            Log.d("Accounts-onSharedPreferenceChanged", "Options changed. Requery");
            //getActivity().getContentResolver().notifyChange(MyContentProvider.ACCOUNTS_URI, null);
            //getLoaderManager().restartLoader(ACCOUNTS_LOADER, null, this);
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
            View accountSortView = li.inflate(R.layout.sort_accounts, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

            alertDialogBuilder.setView(accountSortView);
            alertDialogBuilder.setTitle("Sort");
            alertDialogBuilder.setCancelable(true);

            ListView sortOptions = (ListView)accountSortView.findViewById(R.id.sort_options);
            sortOptions.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    switch (position) {
                        //Newest
                        case 0:
                            sortOrder = DatabaseHelper.ACCOUNT_DATE + " DESC, " + DatabaseHelper.ACCOUNT_TIME + " DESC";
                            break;

                        //Oldest
                        case 1:
                            //TODO Fix date so it can be sorted
                            sortOrder = DatabaseHelper.ACCOUNT_DATE + " ASC, " + DatabaseHelper.ACCOUNT_TIME + " ASC";
                            break;

                        //Largest
                        case 2:
                            sortOrder = "CAST ("+DatabaseHelper.ACCOUNT_BALANCE+" AS INTEGER)" + " DESC";
                            break;

                        //Smallest
                        case 3:
                            sortOrder = "CAST ("+DatabaseHelper.ACCOUNT_BALANCE+" AS INTEGER)" + " ASC";
                            break;

                        //Alphabetical
                        case 4:
                            sortOrder = DatabaseHelper.ACCOUNT_NAME + " ASC";
                            break;

                        //None
                        case 5:
                            sortOrder = null;
                            break;

                        default:
                            Log.e("Accounts-SortFragment","Unknown Sorting Option!");
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

        Log.d("Accounts-onCreateLoader", "calling create loader...");
        switch (loaderID) {
            case ACCOUNTS_LOADER:
                Log.v("Accounts-onCreateLoader","new loader created");
                return new CursorLoader(
                        getActivity(),   	// Parent activity context
                        MyContentProvider.ACCOUNTS_URI,// Table to query
                        null,     			// Projection to return
                        null,            	// No selection clause
                        null,            	// No selection arguments
                        sortOrder           // Default sort order-> "CAST (AcctBalance AS INTEGER)" + " DESC"
                );
            case ACCOUNTS_SEARCH_LOADER:
                String query = getActivity().getIntent().getStringExtra("query");
                Log.v("Accounts-onCreateLoader","new loader (boolSearch "+ query + ") created");
                return new CursorLoader(
                        getActivity(),   	// Parent activity context
                        (Uri.parse(MyContentProvider.ACCOUNTS_URI + "/SEARCH/" + query)),// Table to query
                        null,     			// Projection to return
                        null,            	// No selection clause
                        null,            	// No selection arguments
                        sortOrder           // Default sort order
                );

            default:
                Log.e("Accounts-onCreateLoader", "Not a valid CursorLoader ID");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        TextView footerTV = (TextView)this.myFragmentView.findViewById(R.id.account_footer);

        switch(loader.getId()){
            case ACCOUNTS_LOADER:
                adapterAccounts.swapCursor(data);
                Log.v("Accounts-onLoadFinished", "loader finished. loader="+loader.getId() + " data="+data + " data size="+data.getCount());

                int balanceColumn = data.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
                BigDecimal totalBalance = BigDecimal.ZERO;
                Locale locale=getResources().getConfiguration().locale;

                data.moveToPosition(-1);
                while(data.moveToNext()){
                    totalBalance = totalBalance.add(new Money(data.getString(balanceColumn)).getBigDecimal(locale));
                }

                try{
                    TextView noResult = (TextView)myFragmentView.findViewById(R.id.account_noTransaction);
                    noResult.setText("No Accounts\n\n To Add An Account, Please Use The ActionBar On The Top");
                    lv.setEmptyView(noResult);

                    footerTV.setText("Total Balance: " + new Money(totalBalance).getNumberFormat(locale));
                }
                catch(Exception e){
                    Log.e("Accounts-onLoadFinished", "Error setting balance TextView. e="+e);
                }

                break;

            case ACCOUNTS_SEARCH_LOADER:
                adapterAccounts.swapCursor(data);
                Log.v("Accounts-onLoadFinished", "loader finished. loader="+loader.getId() + " data="+data + " data size="+data.getCount());

                try{
                    TextView noResult = (TextView)myFragmentView.findViewById(R.id.account_noTransaction);
                    noResult.setText("No Accounts Found");
                    lv.setEmptyView(noResult);

                    footerTV.setText("Search Results");
                }
                catch(Exception e){
                    Log.e("Accounts-onLoadFinished", "Error setting search TextView. e="+e);
                }

                break;

            default:
                Log.e("Accounts-onLoadFinished", "Error. Unknown loader ("+loader.getId());
                break;
        }

        if(!getSherlockActivity().getSupportLoaderManager().hasRunningLoaders()){
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch(loader.getId()){
            case ACCOUNTS_LOADER:
                adapterAccounts.swapCursor(null);
                Log.v("Accounts-onLoaderReset", "loader reset. loader="+loader.getId());
                break;

            case ACCOUNTS_SEARCH_LOADER:
                adapterAccounts.swapCursor(null);
                Log.v("Accounts-onLoaderReset", "loader reset. loader="+loader.getId());
                break;

            default:
                Log.e("Accounts-onLoadFinished", "Error. Unknown loader ("+loader.getId());
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
            if (adapterAccounts.getSelectedCount() == 1 && mode != null) {
                menu.add(0, CONTEXT_MENU_VIEW, 0, "View").setIcon(android.R.drawable.ic_menu_view);
                menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit").setIcon(android.R.drawable.ic_menu_edit);
                menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
                return true;
            } else if (adapterAccounts.getSelectedCount() > 1) {
                menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete").setIcon(android.R.drawable.ic_menu_delete);
                return true;
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            SparseBooleanArray selected = adapterAccounts.getSelectedIds();

            switch (item.getItemId()) {
                case CONTEXT_MENU_VIEW:
                    for (int i = 0; i < selected.size(); i++){
                        if (selected.valueAt(i)) {
                            //accountOpen(adapterAccounts.getAccount(selected.keyAt(i)).id);
                            DialogFragment newFragment = AccountViewFragment.newInstance(adapterAccounts.getAccount(selected.keyAt(i)).id);
                            newFragment.show(getChildFragmentManager(), "dialogView");
                        }
                    }

                    mode.finish();
                    return true;
                case CONTEXT_MENU_EDIT:
                    for (int i = 0; i < selected.size(); i++){
                        if (selected.valueAt(i)) {
                            //accountEdit(adapterAccounts.getAccount(selected.keyAt(i)));
                            DialogFragment newFragment = AccountEditFragment.newInstance(adapterAccounts.getAccount(selected.keyAt(i)));
                            newFragment.show(getChildFragmentManager(), "dialogEdit");
                        }
                    }

                    mode.finish();
                    return true;
                case CONTEXT_MENU_DELETE:
                    AccountRecord record;
                    for (int i = 0; i < selected.size(); i++){
                        if (selected.valueAt(i)) {
                            record = adapterAccounts.getAccount(selected.keyAt(i));

                            //Delete Account
                            Uri uri = Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + record.id);
                            getActivity().getContentResolver().delete(uri,DatabaseHelper.ACCOUNT_ID+"="+record.id, null);

                            //Delete All Transactions of that account
                            uri = Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + 0);
                            getActivity().getContentResolver().delete(uri,DatabaseHelper.TRANS_ACCT_ID+"="+record.id, null);

                            Toast.makeText(getActivity(), "Deleted Account:\n" + record.name, Toast.LENGTH_SHORT).show();
                        }
                    }

                    mode.finish();
                    return true;

                default:
                    mode.finish();
                    Log.e("Accounts-onActionItemClciked","ERROR. Clicked " + item);
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode=null;
            adapterAccounts.removeSelection();
        }
    }

    @Override
    public void onDestroyView() {
        if(mActionMode!=null){
            ((ActionMode)mActionMode).finish();
        }

        super.onDestroyView();
    }

}//End Accounts