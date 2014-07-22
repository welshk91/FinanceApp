package com.databases.example.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.Money;
import com.databases.example.data.MyContentProvider;

import java.util.Locale;

public class TransactionViewFragment extends SherlockDialogFragment {
    public static TransactionViewFragment newInstance(int id) {
        TransactionViewFragment frag = new TransactionViewFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int id = getArguments().getInt("id");
        final Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + id), null, null, null, null);

        int entry_id = 0;
        int entry_acctId = 0;
        int entry_planId = 0;
        String entry_name = null;
        String entry_value = null;
        String entry_type = null;
        String entry_category = null;
        String entry_checknum = null;
        String entry_memo = null;
        String entry_time = null;
        String entry_date = null;
        String entry_cleared = null;

        c.moveToFirst();
        do{
            entry_id = c.getInt(c.getColumnIndex(DatabaseHelper.TRANS_ID));
            entry_acctId = c.getInt(c.getColumnIndex(DatabaseHelper.TRANS_ACCT_ID));
            entry_planId = c.getInt(c.getColumnIndex(DatabaseHelper.TRANS_PLAN_ID));
            entry_name = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_NAME));
            entry_value = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_VALUE));
            entry_type = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_TYPE));
            entry_category = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_CATEGORY));
            entry_checknum = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_CHECKNUM));
            entry_memo = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_MEMO));
            entry_time = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_TIME));
            entry_date = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_DATE));
            entry_cleared = c.getString(c.getColumnIndex(DatabaseHelper.TRANS_CLEARED));
        }while(c.moveToNext());

        final LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
        final View transStatsView = li.inflate(R.layout.transaction_item, null);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_account", true);

        final Locale locale=getResources().getConfiguration().locale;
        final Money value = new Money(entry_value);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());

        alertDialogBuilder.setView(transStatsView);
        alertDialogBuilder.setCancelable(true);

        //Change gradient
        try{
            LinearLayout l;
            l=(LinearLayout)transStatsView.findViewById(R.id.transaction_gradient);
            GradientDrawable defaultGradientPos = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[] {0xFF4ac925,0xFF4ac925});

            GradientDrawable defaultGradientNeg = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[] {0xFFe00707,0xFFe00707});

            if(useDefaults){
                if(entry_type.contains("Deposit")){
                    l.setBackgroundDrawable(defaultGradientPos);
                }
                else{
                    l.setBackgroundDrawable(defaultGradientNeg);
                }

            }
            else{
                if(entry_type.contains("Deposit")){
                    l.setBackgroundDrawable(defaultGradientPos);
                }
                else{
                    l.setBackgroundDrawable(defaultGradientNeg);
                }
            }

        }
        catch(Exception e){
            Toast.makeText(getActivity(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
        }

        //Set Statistics
        TextView statsName = (TextView)transStatsView.findViewById(R.id.transaction_name);
        statsName.setText(entry_name);
        TextView statsValue = (TextView)transStatsView.findViewById(R.id.transaction_value);
        statsValue.setText("Value: " + value.getNumberFormat(locale));
        TextView statsType = (TextView)transStatsView.findViewById(R.id.transaction_type);
        statsType.setText("Type: " + entry_type);
        TextView statsCategory = (TextView)transStatsView.findViewById(R.id.transaction_category);
        statsCategory.setText("Category: " + entry_category);
        TextView statsCheckNum = (TextView)transStatsView.findViewById(R.id.transaction_checknum);
        statsCheckNum.setText("Check Num: " + entry_checknum);
        TextView statsMemo = (TextView)transStatsView.findViewById(R.id.transaction_memo);
        statsMemo.setText("Memo: " + entry_memo);
        DateTime d = new DateTime();
        d.setStringSQL(entry_date);
        TextView statsDate = (TextView)transStatsView.findViewById(R.id.transaction_date);
        statsDate.setText("Date: " + d.getReadableDate());
        DateTime t = new DateTime();
        t.setStringSQL(entry_time);
        TextView statsTime = (TextView)transStatsView.findViewById(R.id.transaction_time);
        statsTime.setText("Time: " + t.getReadableTime());
        TextView statsCleared = (TextView)transStatsView.findViewById(R.id.transaction_cleared);
        statsCleared.setText("Cleared: " + entry_cleared);

        //c.close();
        return alertDialogBuilder.create();
    }
}
