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
import com.databases.example.app.Transactions;
import com.databases.example.data.DatabaseHelper;

/**
 * Created by kev on 10/6/14.
 */
public class TransactionSortDialogFragment extends DialogFragment {
    public static TransactionSortDialogFragment newInstance() {
        TransactionSortDialogFragment frag = new TransactionSortDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        LayoutInflater li = LayoutInflater.from(this.getActivity());
        View accountSortView = li.inflate(R.layout.sort_transactions, null);

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
                        prefs.edit().putString(getString(R.string.pref_key_transaction_sort), DatabaseHelper.TRANS_DATE + " DESC, " + DatabaseHelper.TRANS_TIME + " DESC").apply();
                        break;

                    //Oldest
                    case 1:
                        prefs.edit().putString(getString(R.string.pref_key_transaction_sort), DatabaseHelper.TRANS_DATE + " ASC, " + DatabaseHelper.TRANS_TIME + " ASC").apply();
                        break;

                    //Largest
                    case 2:
                        prefs.edit().putString(getString(R.string.pref_key_transaction_sort), DatabaseHelper.TRANS_TYPE + " ASC, CAST (" + DatabaseHelper.TRANS_VALUE + " AS INTEGER)" + " DESC").apply();
                        break;

                    //Smallest
                    case 3:
                        prefs.edit().putString(getString(R.string.pref_key_transaction_sort), DatabaseHelper.TRANS_TYPE + " ASC, CAST (" + DatabaseHelper.TRANS_VALUE + " AS INTEGER)" + " ASC").apply();
                        break;

                    //Category
                    case 4:
                        prefs.edit().putString(getString(R.string.pref_key_transaction_sort), DatabaseHelper.TRANS_CATEGORY + " ASC").apply();
                        break;

                    //Type
                    case 5:
                        prefs.edit().putString(getString(R.string.pref_key_transaction_sort), DatabaseHelper.TRANS_TYPE + " ASC").apply();
                        break;

                    //Alphabetical
                    case 6:
                        prefs.edit().putString(getString(R.string.pref_key_transaction_sort), DatabaseHelper.TRANS_NAME + " ASC").apply();
                        break;

                    //None
                    case 7:
                        prefs.edit().putString(getString(R.string.pref_key_transaction_sort), null).apply();
                        break;

                    default:
                        Log.e("Accounts-SortFragment", "Unknown Sorting Option!");
                        break;

                }//end switch

                //Restart loader with new sort order
                getParentFragment().getLoaderManager().restartLoader(Transactions.TRANS_LOADER, null, (Transactions) getParentFragment());

                getDialog().cancel();
            }
        });

        return alertDialogBuilder.create();
    }
}
