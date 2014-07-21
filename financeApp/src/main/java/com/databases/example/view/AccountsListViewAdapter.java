package com.databases.example.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.app.Accounts;
import com.databases.example.data.AccountRecord;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.Money;

import java.util.Locale;

public class AccountsListViewAdapter extends CursorAdapter{
    public SparseBooleanArray mSelectedItemsIds;

    public AccountsListViewAdapter(Context context, Cursor accounts) {
        super(context, accounts,0);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public AccountRecord getAccount(long position){
        final Cursor group = getCursor();

        group.moveToPosition((int) position);
        final int NameColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_NAME);
        final int BalanceColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
        final int TimeColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_TIME);
        final int DateColumn = group.getColumnIndex(DatabaseHelper.ACCOUNT_DATE);

        final String id = group.getString(0);
        final String name = group.getString(NameColumn);
        final String balance = group.getString(BalanceColumn);
        final String time = group.getString(TimeColumn);
        final String date = group.getString(DateColumn);

        final AccountRecord record = new AccountRecord(id, name, balance, time, date);
        return record;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        View v = view;
        Cursor user = getCursor();

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_account", true);

        if (user != null) {
            TextView tvName = (TextView) v.findViewById(R.id.account_name);
            TextView tvBalance = (TextView) v.findViewById(R.id.account_balance);
            TextView tvDate = (TextView) v.findViewById(R.id.account_date);
            TextView tvTime = (TextView) v.findViewById(R.id.account_time);

            int NameColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_NAME);
            int BalanceColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
            int TimeColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_TIME);
            int DateColumn = user.getColumnIndex(DatabaseHelper.ACCOUNT_DATE);

            String id = user.getString(0);
            String name = user.getString(NameColumn);
            Money balance = new Money(user.getString(BalanceColumn));
            String time = user.getString(TimeColumn);
            String date = user.getString(DateColumn);
            Locale locale=context.getResources().getConfiguration().locale;

            //Change gradient
            try{
                LinearLayout l;
                l=(LinearLayout)v.findViewById(R.id.account_gradient);
                //Older color to black gradient (0xFF00FF33,0xFF000000)
                GradientDrawable defaultGradientPos = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[] {0xFF4ac925,0xFF4ac925});
                GradientDrawable defaultGradientNeg = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[] {0xFFe00707,0xFFe00707});

                if(useDefaults){
                    if(balance.isPositive(locale)){
                        l.setBackgroundDrawable(defaultGradientPos);
                    }
                    else{
                        l.setBackgroundDrawable(defaultGradientNeg);
                    }

                }
                else{
                    if(balance.isPositive(locale)){
                        l.setBackgroundDrawable(defaultGradientPos);
                    }
                    else{
                        l.setBackgroundDrawable(defaultGradientNeg);
                    }
                }

            }
            catch(Exception e){
                Toast.makeText(context, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
            }

            if (name != null) {
                tvName.setText(name);
            }

            if(balance != null) {
                tvBalance.setText("Balance: " + balance.getNumberFormat(locale));
            }

            if(date != null) {
                DateTime d = new DateTime();
                d.setStringSQL(date);
                tvDate.setText("Date: " + d.getReadableDate());
            }

            if(time != null) {
                DateTime t = new DateTime();
                t.setStringSQL(time);
                tvTime.setText("Time: " + t.getReadableTime());
            }

            if(user.getPosition()==Accounts.currentAccount && Accounts.mActionMode==null){
                v.setBackgroundColor(0x7734B5E4);
            }

            else if(mSelectedItemsIds.get(user.getPosition())){
                v.setBackgroundColor(0x9934B5E4);
            }
            else{
                v.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.account_item, parent, false);

        TextView tvName = (TextView)v.findViewById(R.id.account_name);
        TextView tvBalance = (TextView)v.findViewById(R.id.account_balance);
        TextView tvTime = (TextView)v.findViewById(R.id.account_time);
        TextView tvDate = (TextView)v.findViewById(R.id.account_date);

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean("checkbox_default_appearance_account", true);

        //Change Background Colors
        try{
            if(!useDefaults){
                LinearLayout l;
                l=(LinearLayout)v.findViewById(R.id.account_layout);
                int startColor = prefs.getInt("key_account_startBackgroundColor", Color.parseColor("#FFFFFF"));
                int endColor = prefs.getInt("key_account_endBackgroundColor", Color.parseColor("#FFFFFF"));
                GradientDrawable defaultGradient = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[] {startColor,endColor});
                l.setBackgroundDrawable(defaultGradient);
            }
        }
        catch(Exception e){
            Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
        }

        //Change Size of main field
        try{
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_account_nameSize), "24");

            if(useDefaults){
                tvName.setTextSize(24);
            }
            else{
                tvName.setTextSize(Integer.parseInt(DefaultSize));
            }

        }
        catch(Exception e){
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try{
            int DefaultColor = prefs.getInt("key_account_nameColor", Color.parseColor("#222222"));

            if(useDefaults){
                tvName.setTextColor(Color.parseColor("#222222"));
            }
            else{
                tvName.setTextColor(DefaultColor);
            }

        }
        catch(Exception e){
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try{
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_account_fieldSize), "14");

            if(useDefaults){
                tvBalance.setTextSize(14);
                tvDate.setTextSize(14);
                tvTime.setTextSize(14);
            }
            else{
                tvBalance.setTextSize(Integer.parseInt(DefaultSize));
                tvDate.setTextSize(Integer.parseInt(DefaultSize));
                tvTime.setTextSize(Integer.parseInt(DefaultSize));
            }

        }
        catch(Exception e){
            Toast.makeText(context, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
        }

        try{
            int DefaultColor = prefs.getInt("key_account_fieldColor", Color.parseColor("#000000"));

            if(useDefaults){
                tvBalance.setTextColor(Color.parseColor("#000000"));
                tvDate.setTextColor(Color.parseColor("#000000"));
                tvTime.setTextColor(Color.parseColor("#000000"));
            }
            else{
                tvBalance.setTextColor(DefaultColor);
                tvDate.setTextColor(DefaultColor);
                tvTime.setTextColor(DefaultColor);
            }

        }
        catch(Exception e){
            Toast.makeText(context, "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
        }

        //For User-Defined Field Visibility
        if(useDefaults||prefs.getBoolean("checkbox_account_nameField", true)){
            tvName.setVisibility(View.VISIBLE);
        }
        else{
            tvName.setVisibility(View.GONE);
        }

        if(useDefaults||prefs.getBoolean("checkbox_account_balanceField", true)){
            tvBalance.setVisibility(View.VISIBLE);
        }
        else{
            tvBalance.setVisibility(View.GONE);
        }

        if(useDefaults||prefs.getBoolean("checkbox_account_dateField", true)){
            tvDate.setVisibility(View.VISIBLE);
        }
        else{
            tvDate.setVisibility(View.GONE);
        }

        if(prefs.getBoolean("checkbox_account_timeField", false) && !useDefaults){
            tvTime.setVisibility(View.VISIBLE);
        }
        else{
            tvTime.setVisibility(View.GONE);
        }

        return v;
    }

    public void toggleSelection(int position){
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
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
