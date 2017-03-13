package com.databases.example.features.checkbook.transactions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
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
import com.databases.example.database.MyContentProvider;
import com.databases.example.utils.Constants;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;

import java.util.ArrayList;
import java.util.Locale;

public class TransactionViewFragment extends DialogFragment {
    public static TransactionViewFragment newInstance(int id) {
        TransactionViewFragment frag = new TransactionViewFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<Transaction> transactions = Transaction.getTransactions(getActivity().getContentResolver().query(
                Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + getArguments().getInt("id")), null, null, null, null));

        Transaction transaction = transactions.get(0);

        final LayoutInflater li = LayoutInflater.from(this.getActivity());
        final View transStatsView = li.inflate(R.layout.transaction_item, null);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final boolean useDefaults = prefs.getBoolean(getString(R.string.pref_key_account_default_appearance), true);

        final Locale locale = getResources().getConfiguration().locale;
        final Money value = new Money(transaction.value);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());

        alertDialogBuilder.setView(transStatsView);
        alertDialogBuilder.setCancelable(true);

        //Change gradient
        try {
            LinearLayout l;
            l = (LinearLayout) transStatsView.findViewById(R.id.transaction_gradient);
            GradientDrawable defaultGradientPos = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{0xFF4ac925, 0xFF4ac925});

            GradientDrawable defaultGradientNeg = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{0xFFe00707, 0xFFe00707});

            if (useDefaults) {
                if (transaction.type.contains(Constants.DEPOSIT)) {
                    l.setBackgroundDrawable(defaultGradientPos);
                } else {
                    l.setBackgroundDrawable(defaultGradientNeg);
                }

            } else {
                if (transaction.type.contains(Constants.DEPOSIT)) {
                    l.setBackgroundDrawable(defaultGradientPos);
                } else {
                    l.setBackgroundDrawable(defaultGradientNeg);
                }
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
        }

        //Set Statistics
        TextView statsName = (TextView) transStatsView.findViewById(R.id.transaction_name);
        statsName.setText(transaction.name);
        TextView statsValue = (TextView) transStatsView.findViewById(R.id.transaction_value);
        statsValue.setText("Value: " + value.getNumberFormat(locale));
        TextView statsType = (TextView) transStatsView.findViewById(R.id.transaction_type);
        statsType.setText("Type: " + transaction.type);
        TextView statsCategory = (TextView) transStatsView.findViewById(R.id.transaction_category);
        statsCategory.setText("Category: " + transaction.category);
        TextView statsCheckNum = (TextView) transStatsView.findViewById(R.id.transaction_checknum);
        statsCheckNum.setText("Check Num: " + transaction.checknum);
        TextView statsMemo = (TextView) transStatsView.findViewById(R.id.transaction_memo);
        statsMemo.setText("Memo: " + transaction.memo);
        DateTime d = new DateTime();
        d.setStringSQL(transaction.date);
        TextView statsDate = (TextView) transStatsView.findViewById(R.id.transaction_date);
        statsDate.setText("Date: " + d.getReadableDate());
        DateTime t = new DateTime();
        t.setStringSQL(transaction.time);
        TextView statsTime = (TextView) transStatsView.findViewById(R.id.transaction_time);
        statsTime.setText("Time: " + t.getReadableTime());
        TextView statsCleared = (TextView) transStatsView.findViewById(R.id.transaction_cleared);
        statsCleared.setText("Cleared: " + transaction.cleared);

        return alertDialogBuilder.create();
    }
}
