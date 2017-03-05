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
import com.databases.example.data.DateTime;
import com.databases.example.data.Money;
import com.databases.example.model.Transaction;
import com.databases.example.utils.Constants;

import java.util.Locale;

public class TransactionsListViewAdapter extends CursorAdapter {
    private SparseBooleanArray mSelectedItemsIds;

    public TransactionsListViewAdapter(Context context, Cursor transactions) {
        super(context, transactions, 0);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public Transaction getTransaction(long position) {
        final Cursor group = getCursor();

        group.moveToPosition((int) position);
        final int idColumn = group.getColumnIndex(DatabaseHelper.TRANS_ID);
        final int acctIDColumn = group.getColumnIndex(DatabaseHelper.TRANS_ACCT_ID);
        final int planIDColumn = group.getColumnIndex(DatabaseHelper.TRANS_PLAN_ID);
        final int nameColumn = group.getColumnIndex(DatabaseHelper.TRANS_NAME);
        final int valueColumn = group.getColumnIndex(DatabaseHelper.TRANS_VALUE);
        final int typeColumn = group.getColumnIndex(DatabaseHelper.TRANS_TYPE);
        final int categoryColumn = group.getColumnIndex(DatabaseHelper.TRANS_CATEGORY);
        final int checknumColumn = group.getColumnIndex(DatabaseHelper.TRANS_CHECKNUM);
        final int memoColumn = group.getColumnIndex(DatabaseHelper.TRANS_MEMO);
        final int timeColumn = group.getColumnIndex(DatabaseHelper.TRANS_TIME);
        final int dateColumn = group.getColumnIndex(DatabaseHelper.TRANS_DATE);
        final int clearedColumn = group.getColumnIndex(DatabaseHelper.TRANS_CLEARED);

        //int id = group.getInt(idColumn);
        final int id = group.getInt(0);
        final int acctId = group.getInt(acctIDColumn);
        final int planId = group.getInt(planIDColumn);
        final String name = group.getString(nameColumn);
        final String value = group.getString(valueColumn);
        final String type = group.getString(typeColumn);
        final String category = group.getString(categoryColumn);
        final String checknum = group.getString(checknumColumn);
        final String memo = group.getString(memoColumn);
        final String time = group.getString(timeColumn);
        final String date = group.getString(dateColumn);
        final String cleared = group.getString(clearedColumn);

        return new Transaction(id, acctId, planId, name, value, type, category, checknum, memo, time, date, cleared);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_transaction", true);

        if (cursor != null) {
            TextView tvName = (TextView) view.findViewById(R.id.transaction_name);
            TextView tvValue = (TextView) view.findViewById(R.id.transaction_value);
            TextView tvType = (TextView) view.findViewById(R.id.transaction_type);
            TextView tvCategory = (TextView) view.findViewById(R.id.transaction_category);
            TextView tvChecknum = (TextView) view.findViewById(R.id.transaction_checknum);
            TextView tvMemo = (TextView) view.findViewById(R.id.transaction_memo);
            TextView tvDate = (TextView) view.findViewById(R.id.transaction_date);
            TextView tvTime = (TextView) view.findViewById(R.id.transaction_time);
            TextView tvCleared = (TextView) view.findViewById(R.id.transaction_cleared);

            int idColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_ID);
            int acctIDColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_ACCT_ID);
            int planIDColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_PLAN_ID);
            int nameColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_NAME);
            int valueColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_VALUE);
            int typeColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_TYPE);
            int categoryColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_CATEGORY);
            int checknumColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_CHECKNUM);
            int memoColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_MEMO);
            int timeColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_TIME);
            int dateColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_DATE);
            int clearedColumn = cursor.getColumnIndex(DatabaseHelper.TRANS_CLEARED);

            int id = cursor.getInt(0);
            int acctId = cursor.getInt(acctIDColumn);
            int planId = cursor.getInt(planIDColumn);
            String name = cursor.getString(nameColumn);
            Money value = new Money(cursor.getString(valueColumn));
            String type = cursor.getString(typeColumn);
            String category = cursor.getString(categoryColumn);
            String checknum = cursor.getString(checknumColumn);
            String memo = cursor.getString(memoColumn);
            String time = cursor.getString(timeColumn);
            String date = cursor.getString(dateColumn);
            String cleared = cursor.getString(clearedColumn);
            Locale locale = context.getResources().getConfiguration().locale;

            //Change gradient
            try {
                LinearLayout l;
                l = (LinearLayout) view.findViewById(R.id.transaction_gradient);
                GradientDrawable defaultGradientPos = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{0xFF4ac925, 0xFF4ac925});

                GradientDrawable defaultGradientNeg = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{0xFFe00707, 0xFFe00707});

                if (useDefaults) {
                    if (type.contains(Constants.DEPOSIT)) {
                        l.setBackgroundDrawable(defaultGradientPos);
                    } else {
                        l.setBackgroundDrawable(defaultGradientNeg);
                    }

                } else {
                    if (type.contains(Constants.DEPOSIT)) {
                        l.setBackgroundDrawable(defaultGradientPos);
                    } else {
                        l.setBackgroundDrawable(defaultGradientNeg);
                    }
                }

            } catch (Exception e) {
                Toast.makeText(context, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
            }

            if (name != null) {
                tvName.setText(name);

                if (planId != 0) {
                    tvName.setTextColor(ContextCompat.getColor(context, R.color.transaction_plans_yes));
                } else {
                    tvName.setTextColor(ContextCompat.getColor(context, R.color.transaction_plans_no));
                }

            }

            if (value != null) {
                tvValue.setText("Value: " + value.getNumberFormat(locale));
            }

            if (type != null) {
                tvType.setText("Type: " + type);
            }

            if (category != null) {
                tvCategory.setText("Category: " + category);
            }

            if (checknum != null) {
                tvChecknum.setText("Check Num: " + checknum);
            }

            if (memo != null) {
                tvMemo.setText("Memo: " + memo);
            }

            if (date != null) {
                DateTime d = new DateTime();
                d.setStringSQL(date);
                tvDate.setText("Date: " + d.getReadableDate());
            }

            if (time != null) {
                DateTime t = new DateTime();
                t.setStringSQL(time);
                tvTime.setText("Time: " + t.getReadableTime());
            }

            if (cleared != null) {
                tvCleared.setText("Cleared: " + cleared);
            }

            view.setBackgroundColor(mSelectedItemsIds.get(cursor.getPosition()) ? 0x9934B5E4 : Color.TRANSPARENT);
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.transaction_item, null);

        LinearLayout l = (LinearLayout) v.findViewById(R.id.transaction_layout);
        TextView tvName = (TextView) v.findViewById(R.id.transaction_name);
        TextView tvValue = (TextView) v.findViewById(R.id.transaction_value);
        TextView tvType = (TextView) v.findViewById(R.id.transaction_type);
        TextView tvCategory = (TextView) v.findViewById(R.id.transaction_category);
        TextView tvChecknum = (TextView) v.findViewById(R.id.transaction_checknum);
        TextView tvMemo = (TextView) v.findViewById(R.id.transaction_memo);
        TextView tvTime = (TextView) v.findViewById(R.id.transaction_time);
        TextView tvDate = (TextView) v.findViewById(R.id.transaction_date);
        TextView tvCleared = (TextView) v.findViewById(R.id.transaction_cleared);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_transaction", true);

        //Change Background Colors
        try {
            if (!useDefaults) {
                int startColor = prefs.getInt("key_transaction_startBackgroundColor", ContextCompat.getColor(context, R.color.white));
                int endColor = prefs.getInt("key_transaction_endBackgroundColor", ContextCompat.getColor(context, R.color.white));

                GradientDrawable defaultGradient = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{startColor, endColor});
                l.setBackgroundDrawable(defaultGradient);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
        }

        try {
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_transaction_nameSize), "24");

            if (useDefaults) {
                tvName.setTextSize(24);
            } else {
                tvName.setTextSize(Integer.parseInt(DefaultSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt("key_transaction_nameColor", ContextCompat.getColor(context, R.color.transaction_title_default));

            if (useDefaults) {
                tvName.setTextColor(ContextCompat.getColor(context, R.color.transaction_title_default));
            } else {
                tvName.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_transaction_fieldSize), "14");

            if (useDefaults) {
                tvValue.setTextSize(14);
                tvDate.setTextSize(14);
                tvTime.setTextSize(14);
                tvCategory.setTextSize(14);
                tvMemo.setTextSize(14);
                tvChecknum.setTextSize(14);
                tvCleared.setTextSize(14);
                tvType.setTextSize(14);
            } else {
                tvValue.setTextSize(Integer.parseInt(DefaultSize));
                tvType.setTextSize(Integer.parseInt(DefaultSize));
                tvCategory.setTextSize(Integer.parseInt(DefaultSize));
                tvChecknum.setTextSize(Integer.parseInt(DefaultSize));
                tvMemo.setTextSize(Integer.parseInt(DefaultSize));
                tvTime.setTextSize(Integer.parseInt(DefaultSize));
                tvDate.setTextSize(Integer.parseInt(DefaultSize));
                tvCleared.setTextSize(Integer.parseInt(DefaultSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt("key_transaction_fieldColor", ContextCompat.getColor(context, R.color.transaction_details_default));

            if (useDefaults) {
                tvValue.setTextColor(ContextCompat.getColor(context, R.color.transaction_details_default));
                tvType.setTextColor(ContextCompat.getColor(context, R.color.transaction_details_default));
                tvCategory.setTextColor(ContextCompat.getColor(context, R.color.transaction_details_default));
                tvChecknum.setTextColor(ContextCompat.getColor(context, R.color.transaction_details_default));
                tvMemo.setTextColor(ContextCompat.getColor(context, R.color.transaction_details_default));
                tvTime.setTextColor(ContextCompat.getColor(context, R.color.transaction_details_default));
                tvDate.setTextColor(ContextCompat.getColor(context, R.color.transaction_details_default));
                tvCleared.setTextColor(ContextCompat.getColor(context, R.color.transaction_details_default));
            } else {
                tvValue.setTextColor(DefaultColor);
                tvType.setTextColor(DefaultColor);
                tvCategory.setTextColor(DefaultColor);
                tvChecknum.setTextColor(DefaultColor);
                tvMemo.setTextColor(DefaultColor);
                tvTime.setTextColor(DefaultColor);
                tvDate.setTextColor(DefaultColor);
                tvCleared.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
        }

        if (useDefaults || prefs.getBoolean("checkbox_transaction_nameField", true)) {
            tvName.setVisibility(View.VISIBLE);
        } else {
            tvName.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean("checkbox_transaction_valueField", true)) {
            tvValue.setVisibility(View.VISIBLE);
        } else {
            tvValue.setVisibility(View.GONE);
        }

        if (prefs.getBoolean("checkbox_transaction_typeField", false) && !useDefaults) {
            tvType.setVisibility(View.VISIBLE);
        } else {
            tvType.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean("checkbox_transaction_categoryField", true)) {
            tvCategory.setVisibility(View.VISIBLE);
        } else {
            tvCategory.setVisibility(View.GONE);
        }

        if (prefs.getBoolean("checkbox_transaction_checknumField", false) && !useDefaults) {
            tvChecknum.setVisibility(View.VISIBLE);
        } else {
            tvChecknum.setVisibility(View.GONE);
        }

        if (prefs.getBoolean("checkbox_transaction_memoField", false) && !useDefaults) {
            tvMemo.setVisibility(View.VISIBLE);
        } else {
            tvMemo.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean("checkbox_transaction_dateField", true)) {
            tvDate.setVisibility(View.VISIBLE);
        } else {
            tvDate.setVisibility(View.GONE);
        }

        if (prefs.getBoolean("checkbox_transaction_timeField", false) && !useDefaults) {
            tvTime.setVisibility(View.VISIBLE);
        } else {
            tvTime.setVisibility(View.GONE);
        }

        if (prefs.getBoolean("checkbox_transaction_clearedField", false) && !useDefaults) {
            tvCleared.setVisibility(View.VISIBLE);
        } else {
            tvCleared.setVisibility(View.GONE);
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
        return mSelectedItemsIds.size();// mSelectedCount;
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

}
