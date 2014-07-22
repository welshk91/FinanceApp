package com.databases.example.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.Money;
import com.databases.example.data.MyContentProvider;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;

//Class that handles add fragment
public class AccountAddFragment extends SherlockDialogFragment {
    public static AccountAddFragment newInstance() {
        AccountAddFragment frag = new AccountAddFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(getActivity());
        final View promptsView = li.inflate(R.layout.account_add, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle("Add An Account");

        //Set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                final Calendar cal = Calendar.getInstance();

                                String accountName = null;
                                Locale locale=getResources().getConfiguration().locale;
                                DateTime accountDate = new DateTime();
                                accountDate.setDate(cal.getTime());

                                //Variables for adding the account
                                EditText aName = (EditText) promptsView.findViewById(R.id.EditAccountName);
                                EditText aBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
                                accountName = aName.getText().toString().trim();
                                Money accountBalance = new Money(aBalance.getText().toString().trim());

                                //Variables for adding Starting Balance transaction
                                final String transactionName = "STARTING BALANCE";
                                final String transactionPlanId = "0";
                                Money transactionValue=null;
                                final String transactionCategory = "STARTING BALANCE";
                                final String transactionCheckNum = "None";
                                final String transactionMemo = "This is an automatically generated transaction created when you add an account";
                                final String transactionTime = accountDate.getSQLTime(locale);
                                final String transactionDate = accountDate.getSQLDate(locale);
                                final String transactionCleared = "true";
                                String transactionType = "Unknown";

                                //Check Value to see if it's valid
                                try{
                                    transactionValue = new Money(Float.parseFloat(accountBalance.getBigDecimal(locale)+""));
                                }
                                catch(Exception e){
                                    transactionValue = new Money("0.00");
                                    accountBalance = new Money("0.00");
                                }

                                try{
                                    if(accountBalance.isPositive(locale)){
                                        transactionType = "Deposit";
                                    }
                                    else{
                                        transactionType = "Withdraw";
                                        transactionValue = new Money (transactionValue.getBigDecimal(locale).multiply(new BigDecimal(-1)));
                                    }
                                }
                                catch(Exception e){
                                    Toast.makeText(getActivity(), "Error\nWas balance a valid format?", Toast.LENGTH_SHORT).show();
                                }

                                try{
                                    if (accountName.length()>0) {

                                        ContentValues accountValues=new ContentValues();
                                        accountValues.put(DatabaseHelper.ACCOUNT_NAME,accountName);
                                        accountValues.put(DatabaseHelper.ACCOUNT_BALANCE,accountBalance.getBigDecimal(locale)+"");
                                        accountValues.put(DatabaseHelper.ACCOUNT_TIME,accountDate.getSQLTime(locale));
                                        accountValues.put(DatabaseHelper.ACCOUNT_DATE,accountDate.getSQLDate(locale));

                                        //Insert values into accounts table
                                        Uri u = getActivity().getContentResolver().insert(MyContentProvider.ACCOUNTS_URI, accountValues);

                                        ContentValues transactionValues=new ContentValues();
                                        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(u.getLastPathSegment()));
                                        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, transactionPlanId);
                                        transactionValues.put(DatabaseHelper.TRANS_NAME, transactionName);
                                        transactionValues.put(DatabaseHelper.TRANS_VALUE, transactionValue.getBigDecimal(locale)+"");
                                        transactionValues.put(DatabaseHelper.TRANS_TYPE, transactionType);
                                        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, transactionCategory);
                                        transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, transactionCheckNum);
                                        transactionValues.put(DatabaseHelper.TRANS_MEMO, transactionMemo);
                                        transactionValues.put(DatabaseHelper.TRANS_TIME, transactionTime);
                                        transactionValues.put(DatabaseHelper.TRANS_DATE, transactionDate);
                                        transactionValues.put(DatabaseHelper.TRANS_CLEARED, transactionCleared);

                                        //Insert values into accounts table
                                        getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
                                    }

                                    else {
                                        Toast.makeText(getActivity(), "Needs a Name", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                catch(Exception e){
                                    Log.e("Accounts-AddDialog", "Exception e=" + e);
                                    Toast.makeText(getActivity(), "Error Adding Account!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
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
