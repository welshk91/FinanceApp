package com.databases.example.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.Money;
import com.databases.example.data.MyContentProvider;

import java.util.Locale;

//Class that handles view fragment
public class AccountViewFragment extends DialogFragment {

    public static AccountViewFragment newInstance(String id) {
        AccountViewFragment frag = new AccountViewFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String ID = getArguments().getString("id");
        final Cursor c = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + (ID)), null, null, null, null);

        int entry_id = 0;
        String entry_name;
        String entry_balance;
        String entry_time;
        String entry_date;

        c.moveToFirst();
        do {
            entry_id = c.getInt(c.getColumnIndex(DatabaseHelper.ACCOUNT_ID));
            entry_name = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));
            entry_balance = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE));
            entry_time = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_TIME));
            entry_date = c.getString(c.getColumnIndex(DatabaseHelper.ACCOUNT_DATE));
        } while (c.moveToNext());

        final LayoutInflater li = LayoutInflater.from(this.getActivity());
        final View accountStatsView = li.inflate(R.layout.account_item, null);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final boolean useDefaults = prefs.getBoolean(getString(R.string.pref_key_account_default_appearance), true);

        final Locale locale = getResources().getConfiguration().locale;
        final Money balance = new Money(entry_balance);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this.getActivity());

        alertDialogBuilder.setView(accountStatsView);
        alertDialogBuilder.setCancelable(true);

        //Change gradient
        try {
            LinearLayout l;
            l = (LinearLayout) accountStatsView.findViewById(R.id.account_gradient);
            //Older color to black gradient (0xFF00FF33,0xFF000000)
            GradientDrawable defaultGradientPos = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{0xFF4ac925, 0xFF4ac925});
            GradientDrawable defaultGradientNeg = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{0xFFe00707, 0xFFe00707});

            if (useDefaults) {
                if (balance.isPositive(locale)) {
                    l.setBackgroundDrawable(defaultGradientPos);
                } else {
                    l.setBackgroundDrawable(defaultGradientNeg);
                }

            } else {
                if (balance.isPositive(locale)) {
                    l.setBackgroundDrawable(defaultGradientPos);
                } else {
                    l.setBackgroundDrawable(defaultGradientNeg);
                }
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
        }

        //Set Statistics
        TextView statsName = (TextView) accountStatsView.findViewById(R.id.account_name);
        statsName.setText(entry_name);
        TextView statsValue = (TextView) accountStatsView.findViewById(R.id.account_balance);
        statsValue.setText("Balance: " + balance.getNumberFormat(locale));
        DateTime d = new DateTime();
        d.setStringSQL(entry_date);
        TextView statsDate = (TextView) accountStatsView.findViewById(R.id.account_date);
        statsDate.setText("Date: " + d.getReadableDate());
        DateTime t = new DateTime();
        t.setStringSQL(entry_time);
        TextView statsTime = (TextView) accountStatsView.findViewById(R.id.account_time);
        statsTime.setText("Time: " + t.getReadableTime());

        c.close();
        return alertDialogBuilder.create();
    }
}
