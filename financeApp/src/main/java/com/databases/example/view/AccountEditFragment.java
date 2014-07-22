package com.databases.example.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.databases.example.R;
import com.databases.example.data.AccountRecord;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.MyContentProvider;

import java.util.Calendar;
import java.util.Locale;

//Class that handles edit fragment
public class AccountEditFragment extends SherlockDialogFragment{
    public static AccountEditFragment newInstance(AccountRecord record) {
        AccountEditFragment frag = new AccountEditFragment();
        Bundle args = new Bundle();
        args.putString("id", record.id);
        args.putString("name", record.name);
        args.putString("balance", record.balance);
        args.putString("date", record.date);
        args.putString("time", record.time);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String ID = getArguments().getString("id");
        final String name = getArguments().getString("name");
        final String balance = getArguments().getString("balance");

        LayoutInflater li = LayoutInflater.from(getActivity());
        final View promptsView = li.inflate(R.layout.account_add, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle("Edit An Account");

        //Add the previous info into the fields, remove unnecessary fields
        final EditText aName = (EditText) promptsView.findViewById(R.id.EditAccountName);
        final EditText aBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
        TextView aBalanceText = (TextView)promptsView.findViewById(R.id.BalanceTexts);
        aName.setText(name);
        aBalance.setVisibility(View.GONE);
        aBalanceText.setVisibility(View.GONE);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String accountName = null;
                                String accountBalance = null;
                                final Calendar c = Calendar.getInstance();
                                Locale locale=getResources().getConfiguration().locale;
                                DateTime accountDate = new DateTime();
                                accountDate.setDate(c.getTime());

                                accountName = aName.getText().toString().trim();
                                accountBalance = balance.trim();

                                try{
                                    ContentValues accountValues=new ContentValues();
                                    accountValues.put(DatabaseHelper.ACCOUNT_ID,ID);
                                    accountValues.put(DatabaseHelper.ACCOUNT_NAME,accountName);
                                    accountValues.put(DatabaseHelper.ACCOUNT_BALANCE,accountBalance);
                                    accountValues.put(DatabaseHelper.ACCOUNT_TIME,accountDate.getSQLTime(locale));
                                    accountValues.put(DatabaseHelper.ACCOUNT_DATE,accountDate.getSQLDate(locale));

                                    //Update plan
                                    getSherlockActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + ID), accountValues, DatabaseHelper.ACCOUNT_ID+"="+ID, null);
                                }
                                catch(Exception e){
                                    Toast.makeText(getActivity(), "Error Editing Account!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
                                }

                            }//end onClick "OK"
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        return alertDialogBuilder.create();
    }
}
