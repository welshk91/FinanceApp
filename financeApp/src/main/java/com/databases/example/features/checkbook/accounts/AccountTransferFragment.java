package com.databases.example.features.checkbook.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.database.DatabaseHelper;
import com.databases.example.database.MyContentProvider;
import com.databases.example.utils.Constants;
import com.databases.example.utils.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

//Class that handles transfers fragment
public class AccountTransferFragment extends DialogFragment {
    private Spinner transferSpinnerTo;
    private Spinner transferSpinnerFrom;

    private final String transferName = "TRANSFER";
    private final String transferPlanId = "-1";
    private final String transferCategory = "TRANSFER";
    private final String transferCheckNum = "None";
    private final String transferMemo = "This is an automatically generated transaction created when you transfer money";
    private final String transferCleared = "true";
    private String transferType = Constants.WITHDRAW;

    public static AccountTransferFragment newInstance() {
        return new AccountTransferFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater li = LayoutInflater.from(getActivity());
        final View promptsView = li.inflate(R.layout.account_transfer, null);
        final TextInputEditText tAmount = (TextInputEditText) promptsView.findViewById(R.id.edit_account_amount);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle(R.string.transfer_money);

        transferSpinnerFrom = (Spinner) promptsView.findViewById(R.id.spinner_account_from);
        transferSpinnerTo = (Spinner) promptsView.findViewById(R.id.spinner_account_to);

        //Populate Account Drop-down List
        accountPopulate();

        //Set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.transfer,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Account accountFrom = ((Account) transferSpinnerFrom.getItemAtPosition(transferSpinnerFrom.getSelectedItemPosition()));
                                Account accountTo = ((Account) transferSpinnerTo.getItemAtPosition(transferSpinnerTo.getSelectedItemPosition()));

                                if (accountFrom == null || accountTo == null) {
                                    Toast.makeText(getActivity(), "No Accounts \n\nUse The ActionBar To Create Accounts", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    return;
                                }

                                if (accountFrom.id == accountTo.id) {
                                    Toast.makeText(getActivity(), "You picked the same account!", Toast.LENGTH_LONG).show();
                                    dismiss();
                                    return;
                                }

                                String transferAmount = tAmount.getText().toString().trim();

                                Timber.d("From Account:" + accountFrom + " To Account:" + accountTo + " Amount:" + transferAmount);

                                //Transfer From
                                final Calendar cal = Calendar.getInstance();
                                Locale locale = getResources().getConfiguration().locale;
                                DateTime transferDate = new DateTime();
                                transferDate.setDate(cal.getTime());

                                float tAmount;

                                //Check Value to see if it's valid
                                try {
                                    tAmount = Float.parseFloat(transferAmount);
                                } catch (Exception e) {
                                    Timber.e("Invalid amount? Error e=" + e);
                                    return;
                                }

                                ContentValues transferValues = new ContentValues();

                                try {
                                    transferValues.put(DatabaseHelper.TRANS_ACCT_ID, accountFrom.id);
                                    transferValues.put(DatabaseHelper.TRANS_PLAN_ID, transferPlanId);
                                    transferValues.put(DatabaseHelper.TRANS_NAME, transferName);
                                    transferValues.put(DatabaseHelper.TRANS_VALUE, tAmount);
                                    transferValues.put(DatabaseHelper.TRANS_TYPE, transferType);
                                    transferValues.put(DatabaseHelper.TRANS_CATEGORY, transferCategory);
                                    transferValues.put(DatabaseHelper.TRANS_CHECKNUM, transferCheckNum);
                                    transferValues.put(DatabaseHelper.TRANS_MEMO, transferMemo);
                                    transferValues.put(DatabaseHelper.TRANS_TIME, transferDate.getSQLTime(locale));
                                    transferValues.put(DatabaseHelper.TRANS_DATE, transferDate.getSQLDate(locale));
                                    transferValues.put(DatabaseHelper.TRANS_CLEARED, transferCleared);

                                    //Insert values into transaction table
                                    getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transferValues);

                                    //Update Account Info
                                    ContentValues accountValues = new ContentValues();
                                    accountValues.put(DatabaseHelper.ACCOUNT_ID, accountFrom.id);
                                    accountValues.put(DatabaseHelper.ACCOUNT_NAME, accountFrom.name);
                                    accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, Float.parseFloat(accountFrom.balance) - tAmount + "");
                                    accountValues.put(DatabaseHelper.ACCOUNT_TIME, accountFrom.time);
                                    accountValues.put(DatabaseHelper.ACCOUNT_DATE, accountFrom.date);

                                    getActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + accountFrom.id), accountValues, DatabaseHelper.ACCOUNT_ID + "=" + accountFrom.id, null);
                                } catch (Exception e) {
                                    Timber.e("Transfer From failed. Exception e=" + e);
                                    Toast.makeText(getActivity(), "Error Transferring!\n Did you enter valid input? ", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                //Transfer To
                                transferType = Constants.DEPOSIT;

                                try {
                                    transferValues.clear();
                                    transferValues.put(DatabaseHelper.TRANS_ACCT_ID, accountTo.id);
                                    transferValues.put(DatabaseHelper.TRANS_PLAN_ID, transferPlanId);
                                    transferValues.put(DatabaseHelper.TRANS_NAME, transferName);
                                    transferValues.put(DatabaseHelper.TRANS_VALUE, tAmount);
                                    transferValues.put(DatabaseHelper.TRANS_TYPE, transferType);
                                    transferValues.put(DatabaseHelper.TRANS_CATEGORY, transferCategory);
                                    transferValues.put(DatabaseHelper.TRANS_CHECKNUM, transferCheckNum);
                                    transferValues.put(DatabaseHelper.TRANS_MEMO, transferMemo);
                                    transferValues.put(DatabaseHelper.TRANS_TIME, transferDate.getSQLTime(locale));
                                    transferValues.put(DatabaseHelper.TRANS_DATE, transferDate.getSQLDate(locale));
                                    transferValues.put(DatabaseHelper.TRANS_CLEARED, transferCleared);

                                    //Insert values into transaction table
                                    getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transferValues);

                                    //Update Account Info
                                    ContentValues accountValues = new ContentValues();
                                    accountValues.put(DatabaseHelper.ACCOUNT_ID, accountTo.id);
                                    accountValues.put(DatabaseHelper.ACCOUNT_NAME, accountTo.name);
                                    accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, Float.parseFloat(accountTo.balance) + tAmount + "");
                                    accountValues.put(DatabaseHelper.ACCOUNT_TIME, accountTo.time);
                                    accountValues.put(DatabaseHelper.ACCOUNT_DATE, accountTo.date);

                                    getActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + accountTo.id), accountValues, DatabaseHelper.ACCOUNT_ID + "=" + accountTo.id, null);
                                } catch (Exception e) {
                                    Timber.e("Transfer To failed. Exception e=" + e);
                                    Toast.makeText(getActivity(), "Error Transferring!\n Did you enter valid input? ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                );

        return alertDialogBuilder.create();
    }

    //Method to get the list of accounts for transfer spinner
    private void accountPopulate() {
        //TODO: need to customize this layout since we dont use a SimpleCursorAdapter anymore
        String[] from = new String[]{DatabaseHelper.ACCOUNT_NAME};
        int[] to = new int[]{android.R.id.text1};

        ArrayList<Account> accounts = AccountsFragment.adapterAccounts.getAccounts();
        ArrayAdapter<Account> adapterFrom = new ArrayAdapter<Account>(getContext(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, accounts);
        ArrayAdapter<Account> adapterTo = new ArrayAdapter<Account>(getContext(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, accounts);

        adapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        transferSpinnerFrom.setAdapter(adapterFrom);
        transferSpinnerTo.setAdapter(adapterTo);

        transferSpinnerFrom.setSelection(0);
        transferSpinnerTo.setSelection(1);
    }

}
