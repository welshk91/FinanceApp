package com.databases.example.fragments;

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
import com.databases.example.data.MyContentProvider;
import com.databases.example.model.Plan;
import com.databases.example.utils.Constants;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;

import java.util.ArrayList;
import java.util.Locale;

public class PlanViewFragment extends DialogFragment {
    public static PlanViewFragment newInstance(int id) {
        PlanViewFragment frag = new PlanViewFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int ID = getArguments().getInt("id");
        ArrayList<Plan> plans = Plan.getPlans(getActivity().getContentResolver().query(Uri.parse(MyContentProvider.PLANS_URI + "/" + (ID)), null, null, null, null));
        Plan plan = plans.get(0);

        final LayoutInflater li = LayoutInflater.from(this.getActivity());
        final View planStatsView = li.inflate(R.layout.plan_item, null);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final boolean useDefaults = prefs.getBoolean(getString(R.string.pref_key_account_default_appearance), true);

        final Locale locale = getResources().getConfiguration().locale;
        final Money value = new Money(plan.value);

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
                if (plan.type.contains(Constants.DEPOSIT)) {
                    l.setBackgroundDrawable(defaultGradientPos);
                } else {
                    l.setBackgroundDrawable(defaultGradientNeg);
                }

            } else {
                if (plan.type.contains(Constants.DEPOSIT)) {
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
        statsName.setText(plan.name);
        TextView statsAccount = (TextView) planStatsView.findViewById(R.id.plan_account);
        statsAccount.setText("Account: " + plan.acctId);
        TextView statsValue = (TextView) planStatsView.findViewById(R.id.plan_value);
        statsValue.setText("Value: " + value.getNumberFormat(locale));
        TextView statsType = (TextView) planStatsView.findViewById(R.id.plan_type);
        statsType.setText("Type: " + plan.type);
        TextView statsCategory = (TextView) planStatsView.findViewById(R.id.plan_category);
        statsCategory.setText("Category: " + plan.category);
        TextView statsMemo = (TextView) planStatsView.findViewById(R.id.plan_memo);
        statsMemo.setText("Memo: " + plan.memo);
        TextView statsOffset = (TextView) planStatsView.findViewById(R.id.plan_offset);
        temp.setStringSQL(plan.offset);
        statsOffset.setText("Offset: " + temp.getReadableDate());
        TextView statsRate = (TextView) planStatsView.findViewById(R.id.plan_rate);
        statsRate.setText("Rate: " + plan.rate);
        TextView statsNext = (TextView) planStatsView.findViewById(R.id.plan_next);
        temp.setStringSQL(plan.next);
        statsNext.setText("Next: " + temp.getReadableDate());
        TextView statsScheduled = (TextView) planStatsView.findViewById(R.id.plan_scheduled);
        statsScheduled.setText("Scheduled: " + plan.scheduled);
        TextView statsCleared = (TextView) planStatsView.findViewById(R.id.plan_cleared);
        statsCleared.setText("Cleared: " + plan.cleared);

        return alertDialogBuilder.create();
    }

}
