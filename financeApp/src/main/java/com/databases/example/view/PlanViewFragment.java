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
import com.databases.example.utils.Constants;

import java.util.Locale;

public class PlanViewFragment extends DialogFragment {
    public static PlanViewFragment newInstance(String id) {
        PlanViewFragment frag = new PlanViewFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String ID = getArguments().getString("id");
        final Cursor cursor = getActivity().getContentResolver().query(Uri.parse(MyContentProvider.PLANS_URI + "/" + (ID)), null, null, null, null);

        int entry_id = 0;
        String entry_acctId;
        String entry_name;
        String entry_value;
        String entry_type;
        String entry_category;
        String entry_memo;
        String entry_offset;
        String entry_rate;
        String entry_next;
        String entry_scheduled;
        String entry_cleared;

        cursor.moveToFirst();
        do {
            entry_id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.PLAN_ID));
            entry_acctId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_ACCT_ID));
            entry_name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_NAME));
            entry_value = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_VALUE));
            entry_type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_TYPE));
            entry_category = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_CATEGORY));
            entry_memo = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_MEMO));
            entry_offset = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_OFFSET));
            entry_rate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_RATE));
            entry_next = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_NEXT));
            entry_scheduled = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED));
            entry_cleared = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAN_CLEARED));
        } while (cursor.moveToNext());

        final LayoutInflater li = LayoutInflater.from(this.getActivity());
        final View planStatsView = li.inflate(R.layout.plan_item, null);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final boolean useDefaults = prefs.getBoolean(getString(R.string.pref_key_account_default_appearance), true);

        final Locale locale = getResources().getConfiguration().locale;
        final Money value = new Money(entry_value);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
        alertDialogBuilder.setView(planStatsView);
        alertDialogBuilder.setCancelable(true);

        DateTime temp = new DateTime();

        //Change gradient
        try {
            LinearLayout l;
            l = (LinearLayout) planStatsView.findViewById(R.id.plan_gradient);
            GradientDrawable defaultGradientPos = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{0xFF4ac925, 0xFF4ac925});

            GradientDrawable defaultGradientNeg = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{0xFFe00707, 0xFFe00707});

            if (useDefaults) {
                if (entry_type.contains(Constants.DEPOSIT)) {
                    l.setBackgroundDrawable(defaultGradientPos);
                } else {
                    l.setBackgroundDrawable(defaultGradientNeg);
                }

            } else {
                if (entry_type.contains(Constants.DEPOSIT)) {
                    l.setBackgroundDrawable(defaultGradientPos);
                } else {
                    l.setBackgroundDrawable(defaultGradientNeg);
                }
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
        }

        //Set Statistics
        TextView statsName = (TextView) planStatsView.findViewById(R.id.plan_name);
        statsName.setText(entry_name);
        TextView statsAccount = (TextView) planStatsView.findViewById(R.id.plan_account);
        statsAccount.setText("Account: " + entry_acctId);
        TextView statsValue = (TextView) planStatsView.findViewById(R.id.plan_value);
        statsValue.setText("Value: " + value.getNumberFormat(locale));
        TextView statsType = (TextView) planStatsView.findViewById(R.id.plan_type);
        statsType.setText("Type: " + entry_type);
        TextView statsCategory = (TextView) planStatsView.findViewById(R.id.plan_category);
        statsCategory.setText("Category: " + entry_category);
        TextView statsMemo = (TextView) planStatsView.findViewById(R.id.plan_memo);
        statsMemo.setText("Memo: " + entry_memo);
        TextView statsOffset = (TextView) planStatsView.findViewById(R.id.plan_offset);
        temp.setStringSQL(entry_offset);
        statsOffset.setText("Offset: " + temp.getReadableDate());
        TextView statsRate = (TextView) planStatsView.findViewById(R.id.plan_rate);
        statsRate.setText("Rate: " + entry_rate);
        TextView statsNext = (TextView) planStatsView.findViewById(R.id.plan_next);
        temp.setStringSQL(entry_next);
        statsNext.setText("Next: " + temp.getReadableDate());
        TextView statsScheduled = (TextView) planStatsView.findViewById(R.id.plan_scheduled);
        statsScheduled.setText("Scheduled: " + entry_scheduled);
        TextView statsCleared = (TextView) planStatsView.findViewById(R.id.plan_cleared);
        statsCleared.setText("Cleared: " + entry_cleared);

        return alertDialogBuilder.create();
    }

}
