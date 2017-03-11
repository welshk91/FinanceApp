package com.databases.example.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.model.Account;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;

import java.util.Locale;

//Class that handles view fragment
public class AccountViewFragment extends DialogFragment {
    private static final String KEY = "account";

    public static AccountViewFragment newInstance(Account account) {
        AccountViewFragment frag = new AccountViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY, account);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Account account = getArguments().getParcelable(KEY);

        final LayoutInflater li = LayoutInflater.from(this.getActivity());
        final View accountStatsView = li.inflate(R.layout.account_item, null);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final boolean useDefaults = prefs.getBoolean(getString(R.string.pref_key_account_default_appearance), true);

        final Locale locale = getResources().getConfiguration().locale;
        final Money balance = new Money(account.balance);

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
        statsName.setText(account.name);
        TextView statsValue = (TextView) accountStatsView.findViewById(R.id.account_balance);
        statsValue.setText("Balance: " + balance.getNumberFormat(locale));
        DateTime d = new DateTime();
        d.setStringSQL(account.date);
        TextView statsDate = (TextView) accountStatsView.findViewById(R.id.account_date);
        statsDate.setText("Date: " + d.getReadableDate());
        DateTime t = new DateTime();
        t.setStringSQL(account.time);
        TextView statsTime = (TextView) accountStatsView.findViewById(R.id.account_time);
        statsTime.setText("Time: " + t.getReadableTime());
        return alertDialogBuilder.create();
    }
}
