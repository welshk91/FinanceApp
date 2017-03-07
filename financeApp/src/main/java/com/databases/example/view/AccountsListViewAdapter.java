package com.databases.example.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.fragments.AccountsFragment;
import com.databases.example.model.Account;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;

import java.util.Locale;

import timber.log.Timber;

public class AccountsListViewAdapter extends CursorAdapter {
    private SparseBooleanArray mSelectedItemsIds;

    public AccountsListViewAdapter(Context context, Cursor accounts) {
        super(context, accounts, 0);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public Account getAccount(long position) {
        final Cursor group = getCursor();

        group.moveToPosition((int) position);
        final int NameColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_NAME);
        final int BalanceColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
        final int TimeColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_TIME);
        final int DateColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_DATE);

        final int id = group.getInt(0);
        final String name = group.getString(NameColumn);
        final String balance = group.getString(BalanceColumn);
        final String time = group.getString(TimeColumn);
        final String date = group.getString(DateColumn);

        return new Account(id, name, balance, time, date);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Cursor user = getCursor();

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_account_default_appearance), true);

        if (user != null) {
            TextView tvName = (TextView) view.findViewById(R.id.account_name);
            TextView tvBalance = (TextView) view.findViewById(R.id.account_balance);
            TextView tvDate = (TextView) view.findViewById(R.id.account_date);
            TextView tvTime = (TextView) view.findViewById(R.id.account_time);

            int NameColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_NAME);
            int BalanceColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
            int TimeColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_TIME);
            int DateColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_DATE);

            int id = user.getInt(0);
            String name = user.getString(NameColumn);
            Money balance = new Money(user.getString(BalanceColumn));
            String time = user.getString(TimeColumn);
            String date = user.getString(DateColumn);
            Locale locale = context.getResources().getConfiguration().locale;

            //Change gradient
            try {
                LinearLayout l;
                l = (LinearLayout) view.findViewById(R.id.account_gradient);
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
                Timber.e("Error setting custom gradient");
                e.printStackTrace();
                Toast.makeText(context, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
            }

            if (name != null) {
                tvName.setText(name);
            }

            if (balance != null) {
                tvBalance.setText(context.getString(R.string.balance) + " " + balance.getNumberFormat(locale));
            }

            if (date != null) {
                DateTime d = new DateTime();
                d.setStringSQL(date);
                tvDate.setText(context.getString(R.string.date) + " " + d.getReadableDate());
            }

            if (time != null) {
                DateTime t = new DateTime();
                t.setStringSQL(time);
                tvTime.setText(context.getString(R.string.time) + " " + t.getReadableTime());
            }

            if (user.getPosition() == AccountsFragment.currentAccount && AccountsFragment.mActionMode == null) {
                view.setBackgroundColor(0x7734B5E4);
            } else if (mSelectedItemsIds.get(user.getPosition())) {
                view.setBackgroundColor(0x9934B5E4);
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.account_item, parent, false);

        TextView tvName = (TextView) v.findViewById(R.id.account_name);
        TextView tvBalance = (TextView) v.findViewById(R.id.account_balance);
        TextView tvTime = (TextView) v.findViewById(R.id.account_time);
        TextView tvDate = (TextView) v.findViewById(R.id.account_date);

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_account_default_appearance), true);

        //Change Background Colors
        try {
            if (!useDefaults) {
                LinearLayout l;
                l = (LinearLayout) v.findViewById(R.id.account_layout);
                int startColor = prefs.getInt(context.getString(R.string.pref_key_account_start_background_color), ContextCompat.getColor(context, R.color.white));
                int endColor = prefs.getInt(context.getString(R.string.pref_key_account_end_background_color), ContextCompat.getColor(context, R.color.white));
                GradientDrawable defaultGradient = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{startColor, endColor});
                l.setBackgroundDrawable(defaultGradient);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
        }

        //Change Size of main field
        try {
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_account_name_size), "24");

            if (useDefaults) {
                tvName.setTextSize(24);
            } else {
                tvName.setTextSize(Integer.parseInt(DefaultSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt(context.getString(R.string.pref_key_account_name_color), ContextCompat.getColor(context, R.color.account_title_default));

            if (useDefaults) {
                tvName.setTextColor(ContextCompat.getColor(context, R.color.account_title_default));
            } else {
                tvName.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_account_details_size), "14");

            if (useDefaults) {
                tvBalance.setTextSize(14);
                tvDate.setTextSize(14);
                tvTime.setTextSize(14);
            } else {
                tvBalance.setTextSize(Integer.parseInt(DefaultSize));
                tvDate.setTextSize(Integer.parseInt(DefaultSize));
                tvTime.setTextSize(Integer.parseInt(DefaultSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt(context.getString(R.string.pref_key_account_details_color), ContextCompat.getColor(context, R.color.account_details_default));

            if (useDefaults) {
                tvBalance.setTextColor(ContextCompat.getColor(context, R.color.account_details_default));
                tvDate.setTextColor(ContextCompat.getColor(context, R.color.account_details_default));
                tvTime.setTextColor(ContextCompat.getColor(context, R.color.account_details_default));
            } else {
                tvBalance.setTextColor(DefaultColor);
                tvDate.setTextColor(DefaultColor);
                tvTime.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
        }

        //For User-Defined Field Visibility
        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_account_name_show), true)) {
            tvName.setVisibility(View.VISIBLE);
        } else {
            tvName.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_account_balance_show), true)) {
            tvBalance.setVisibility(View.VISIBLE);
        } else {
            tvBalance.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_account_date_show), true)) {
            tvDate.setVisibility(View.VISIBLE);
        } else {
            tvDate.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(context.getString(R.string.pref_key_account_time_show), false) && !useDefaults) {
            tvTime.setVisibility(View.VISIBLE);
        } else {
            tvTime.setVisibility(View.GONE);
        }

        return v;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    private void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

}
