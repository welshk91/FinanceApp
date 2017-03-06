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
import com.databases.example.model.Plan;
import com.databases.example.utils.Constants;

import java.util.Locale;

public class PlansListViewAdapter extends CursorAdapter {
    private SparseBooleanArray mSelectedItemsIds;

    public PlansListViewAdapter(Context context, Cursor plans) {
        super(context, plans, 0);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public Plan getPlan(long position) {
        final Cursor group = getCursor();

        group.moveToPosition((int) position);
        final int columnID = group.getColumnIndex(DatabaseHelper.PLAN_ID);
        final int columnToID = group.getColumnIndex(DatabaseHelper.PLAN_ACCT_ID);
        final int columnName = group.getColumnIndex(DatabaseHelper.PLAN_NAME);
        final int columnValue = group.getColumnIndex(DatabaseHelper.PLAN_VALUE);
        final int columnType = group.getColumnIndex(DatabaseHelper.PLAN_TYPE);
        final int columnCategory = group.getColumnIndex(DatabaseHelper.PLAN_CATEGORY);
        final int columnMemo = group.getColumnIndex(DatabaseHelper.PLAN_MEMO);
        final int columnOffset = group.getColumnIndex(DatabaseHelper.PLAN_OFFSET);
        final int columnRate = group.getColumnIndex(DatabaseHelper.PLAN_RATE);
        final int columnNext = group.getColumnIndex(DatabaseHelper.PLAN_NEXT);
        final int columnScheduled = group.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED);
        final int columnCleared = group.getColumnIndex(DatabaseHelper.PLAN_CLEARED);

        final int id = group.getInt(0);
        final int to_id = group.getInt(columnToID);
        final String name = group.getString(columnName);
        final String value = group.getString(columnValue);
        final String type = group.getString(columnType);
        final String category = group.getString(columnCategory);
        final String memo = group.getString(columnMemo);
        final String offset = group.getString(columnOffset);
        final String rate = group.getString(columnRate);
        final String next = group.getString(columnNext);
        final String scheduled = group.getString(columnScheduled);
        final String cleared = group.getString(columnCleared);

        return new Plan(id, to_id, name, value, type, category, memo, offset, rate, next, scheduled, cleared);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Cursor user = getCursor();

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_plan_default_appearance), true);

        if (user != null) {
            TextView tvName = (TextView) view.findViewById(R.id.plan_name);
            TextView tvAccount = (TextView) view.findViewById(R.id.plan_account);
            TextView tvValue = (TextView) view.findViewById(R.id.plan_value);
            TextView tvType = (TextView) view.findViewById(R.id.plan_type);
            TextView tvCategory = (TextView) view.findViewById(R.id.plan_category);
            TextView tvMemo = (TextView) view.findViewById(R.id.plan_memo);
            TextView tvOffset = (TextView) view.findViewById(R.id.plan_offset);
            TextView tvRate = (TextView) view.findViewById(R.id.plan_rate);
            TextView tvNext = (TextView) view.findViewById(R.id.plan_next);
            TextView tvScheduled = (TextView) view.findViewById(R.id.plan_scheduled);
            TextView tvCleared = (TextView) view.findViewById(R.id.plan_cleared);

            final int columnID = user.getColumnIndex(DatabaseHelper.PLAN_ID);
            final int columnToID = user.getColumnIndex(DatabaseHelper.PLAN_ACCT_ID);
            final int columnName = user.getColumnIndex(DatabaseHelper.PLAN_NAME);
            final int columnValue = user.getColumnIndex(DatabaseHelper.PLAN_VALUE);
            final int columnType = user.getColumnIndex(DatabaseHelper.PLAN_TYPE);
            final int columnCategory = user.getColumnIndex(DatabaseHelper.PLAN_CATEGORY);
            final int columnMemo = user.getColumnIndex(DatabaseHelper.PLAN_MEMO);
            final int columnOffset = user.getColumnIndex(DatabaseHelper.PLAN_OFFSET);
            final int columnRate = user.getColumnIndex(DatabaseHelper.PLAN_RATE);
            final int columnNext = user.getColumnIndex(DatabaseHelper.PLAN_NEXT);
            final int columnScheduled = user.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED);
            final int columnCleared = user.getColumnIndex(DatabaseHelper.PLAN_CLEARED);

            String id = user.getString(0);
            String account_id = user.getString(columnToID);
            String name = user.getString(columnName);
            Money value = new Money(user.getString(columnValue));
            String type = user.getString(columnType);
            String category = user.getString(columnCategory);
            String memo = user.getString(columnMemo);
            String offset = user.getString(columnOffset);
            String rate = user.getString(columnRate);
            String next = user.getString(columnNext);
            String scheduled = user.getString(columnScheduled);
            String cleared = user.getString(columnCleared);

            Locale locale = context.getResources().getConfiguration().locale;

            //Change gradient
            try {
                LinearLayout l;
                l = (LinearLayout) view.findViewById(R.id.plan_gradient);
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

            final DateTime temp = new DateTime();

            if (name != null) {
                tvName.setText(name);
            }
            if (account_id != null) {
                tvAccount.setText(context.getString(R.string.acount_id) + " " + account_id);
            }
            if (value != null) {
                tvValue.setText(context.getString(R.string.value) + " " + value.getNumberFormat(locale));
            }
            if (type != null) {
                tvType.setText(context.getString(R.string.type) + " " + type);
            }
            if (category != null) {
                tvCategory.setText(context.getString(R.string.category) + " " + category);
            }
            if (memo != null) {
                tvMemo.setText(context.getString(R.string.memo) + " " + memo);
            }
            if (offset != null) {
                temp.setStringSQL(offset);
                tvOffset.setText(context.getString(R.string.offset) + " " + temp.getReadableDate());
            }
            if (rate != null) {
                tvRate.setText(context.getString(R.string.rate) +" " + rate);
            }
            if (next != null) {
                temp.setStringSQL(next);
                tvNext.setText(context.getString(R.string.next) +" " + temp.getReadableDate());
            }
            if (scheduled != null) {
                tvScheduled.setText(context.getString(R.string.scheduled) + " " + scheduled);
            }
            if (cleared != null) {
                tvCleared.setText(context.getString(R.string.cleared) + " " + cleared);
            }

            if (scheduled.equals("false")) {
                view.setAlpha(.5f);
            } else {
                view.setAlpha(1.0f);
            }

            view.setBackgroundColor(mSelectedItemsIds.get(user.getPosition()) ? 0x9934B5E4 : Color.TRANSPARENT);
        }

    }

    @Override
    public View newView(Context context, Cursor plans, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.plan_item, parent, false);

        TextView tvName = (TextView) v.findViewById(R.id.plan_name);
        TextView tvAccount = (TextView) v.findViewById(R.id.plan_account);
        TextView tvValue = (TextView) v.findViewById(R.id.plan_value);
        TextView tvType = (TextView) v.findViewById(R.id.plan_type);
        TextView tvCategory = (TextView) v.findViewById(R.id.plan_category);
        TextView tvMemo = (TextView) v.findViewById(R.id.plan_memo);
        TextView tvOffset = (TextView) v.findViewById(R.id.plan_offset);
        TextView tvRate = (TextView) v.findViewById(R.id.plan_rate);
        TextView tvNext = (TextView) v.findViewById(R.id.plan_next);
        TextView tvScheduled = (TextView) v.findViewById(R.id.plan_scheduled);
        TextView tvCleared = (TextView) v.findViewById(R.id.plan_cleared);

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_plan_default_appearance), true);

        //Change Background Colors
        try {
            if (!useDefaults) {
                LinearLayout l;
                l = (LinearLayout) v.findViewById(R.id.plan_layout);
                int startColor = prefs.getInt(context.getString(R.string.pref_key_plan_start_background_color), ContextCompat.getColor(context, R.color.white));
                int endColor = prefs.getInt(context.getString(R.string.pref_key_plan_end_background_color), ContextCompat.getColor(context, R.color.white));
                GradientDrawable customGradient = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{startColor, endColor});
                l.setBackgroundDrawable(customGradient);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
        }

        //Change Size of main field
        try {
            String customSize = prefs.getString(context.getString(R.string.pref_key_plan_name_size), "24");

            if (useDefaults) {
                tvName.setTextSize(24);
            } else {
                tvName.setTextSize(Integer.parseInt(customSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int customColor = prefs.getInt(context.getString(R.string.pref_key_plan_name_color), ContextCompat.getColor(context, R.color.plans_title_default));

            if (useDefaults) {
                tvName.setTextColor(ContextCompat.getColor(context, R.color.plans_title_default));
            } else {
                tvName.setTextColor(customColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            String defaultSize = prefs.getString(context.getString(R.string.pref_key_plan_field_size), "14");
            int customSize = Integer.parseInt(defaultSize);

            if (useDefaults) {
                tvAccount.setTextSize(14);
                tvValue.setTextSize(14);
                tvType.setTextSize(14);
                tvCategory.setTextSize(14);
                tvMemo.setTextSize(14);
                tvOffset.setTextSize(14);
                tvRate.setTextSize(14);
                tvNext.setTextSize(14);
                tvScheduled.setTextSize(14);
                tvCleared.setTextSize(14);
            } else {
                tvAccount.setTextSize(customSize);
                tvValue.setTextSize(customSize);
                tvType.setTextSize(customSize);
                tvCategory.setTextSize(customSize);
                tvMemo.setTextSize(customSize);
                tvOffset.setTextSize(customSize);
                tvRate.setTextSize(customSize);
                tvNext.setTextSize(customSize);
                tvScheduled.setTextSize(customSize);
                tvCleared.setTextSize(customSize);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt(context.getString(R.string.pref_key_plan_details_color), ContextCompat.getColor(context, R.color.plans_details_default));

            if (useDefaults) {
                tvAccount.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvValue.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvType.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvCategory.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvMemo.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvOffset.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvRate.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvNext.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvScheduled.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
                tvCleared.setTextColor(ContextCompat.getColor(context, R.color.plans_details_default));
            } else {
                tvAccount.setTextColor(DefaultColor);
                tvValue.setTextColor(DefaultColor);
                tvType.setTextColor(DefaultColor);
                tvCategory.setTextColor(DefaultColor);
                tvMemo.setTextColor(DefaultColor);
                tvOffset.setTextColor(DefaultColor);
                tvRate.setTextColor(DefaultColor);
                tvNext.setTextColor(DefaultColor);
                tvScheduled.setTextColor(DefaultColor);
                tvCleared.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
        }

        //For User-Defined Field Visibility
        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_plan_name_show), true)) {
            tvName.setVisibility(View.VISIBLE);
        } else {
            tvName.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_plan_account_show), true)) {
            tvAccount.setVisibility(View.VISIBLE);
        } else {
            tvAccount.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_plan_value_show), true)) {
            tvValue.setVisibility(View.VISIBLE);
        } else {
            tvValue.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(context.getString(R.string.pref_key_plan_type_show), false) && !useDefaults) {
            tvType.setVisibility(View.VISIBLE);
        } else {
            tvType.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_plan_category_show), true)) {
            tvCategory.setVisibility(View.VISIBLE);
        } else {
            tvCategory.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(context.getString(R.string.pref_key_plan_memo_show), false) && !useDefaults) {
            tvMemo.setVisibility(View.VISIBLE);
        } else {
            tvMemo.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(context.getString(R.string.pref_key_plan_offset_show), false) && !useDefaults) {
            tvOffset.setVisibility(View.VISIBLE);
        } else {
            tvOffset.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_plan_rate_show), true)) {
            tvRate.setVisibility(View.VISIBLE);
        } else {
            tvRate.setVisibility(View.GONE);
        }

        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_plan_next_show), true)) {
            tvNext.setVisibility(View.VISIBLE);
        } else {
            tvNext.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(context.getString(R.string.pref_key_plan_scheduled_show), false) && !useDefaults) {
            tvScheduled.setVisibility(View.VISIBLE);
        } else {
            tvScheduled.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(context.getString(R.string.pref_key_plan_cleared_show), false) && !useDefaults) {
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
