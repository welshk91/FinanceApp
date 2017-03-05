package com.databases.example.view;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.databases.example.R;
import com.databases.example.app.AccountsFragment;
import com.databases.example.data.DatabaseHelper;

/**
 * Created by kev on 10/6/14.
 */
public class AccountSortDialogFragment extends DialogFragment {
    public static AccountSortDialogFragment newInstance() {
        AccountSortDialogFragment frag = new AccountSortDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        LayoutInflater li = LayoutInflater.from(this.getActivity());
        View accountSortView = li.inflate(R.layout.sort_accounts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());

        alertDialogBuilder.setView(accountSortView);
        alertDialogBuilder.setTitle("Sort");
        alertDialogBuilder.setCancelable(true);

        ListView sortOptions = (ListView) accountSortView.findViewById(R.id.sort_options);
        sortOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                switch (position) {
                    //Newest
                    case 0:
                        prefs.edit().putString(getString(R.string.pref_key_account_sort), DatabaseHelper.ACCOUNT_DATE + " DESC, " + DatabaseHelper.ACCOUNT_TIME + " DESC").apply();
                        break;

                    //Oldest
                    case 1:
                        prefs.edit().putString(getString(R.string.pref_key_account_sort), DatabaseHelper.ACCOUNT_DATE + " ASC, " + DatabaseHelper.ACCOUNT_TIME + " ASC").apply();
                        break;

                    //Largest
                    case 2:
                        prefs.edit().putString(getString(R.string.pref_key_account_sort), "CAST (" + DatabaseHelper.ACCOUNT_BALANCE + " AS INTEGER)" + " DESC").apply();
                        break;

                    //Smallest
                    case 3:
                        prefs.edit().putString(getString(R.string.pref_key_account_sort), "CAST (" + DatabaseHelper.ACCOUNT_BALANCE + " AS INTEGER)" + " ASC").apply();
                        break;

                    //Alphabetical
                    case 4:
                        prefs.edit().putString(getString(R.string.pref_key_account_sort), DatabaseHelper.ACCOUNT_NAME + " ASC").apply();
                        break;

                    //None
                    case 5:
                        prefs.edit().putString(getString(R.string.pref_key_account_sort), null).apply();
                        break;

                    default:
                        Log.e(getClass().getSimpleName(), "Unknown Sorting Option!");
                        break;

                }//end switch

                //Restart loader with new sort order
                getParentFragment().getLoaderManager().restartLoader(AccountsFragment.ACCOUNTS_LOADER, null, (AccountsFragment) getParentFragment());

                getDialog().cancel();
            }
        });

        return alertDialogBuilder.create();
    }
}
