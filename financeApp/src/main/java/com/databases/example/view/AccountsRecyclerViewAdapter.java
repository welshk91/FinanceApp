package com.databases.example.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.fragments.AccountsFragment;
import com.databases.example.model.Account;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;

import java.util.ArrayList;
import java.util.Locale;

import timber.log.Timber;

public class AccountsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_EMPTY = 0;
    private final int VIEW_TYPE_NORMAL = 1;

    public final Context context;
    public ArrayList<Account> accounts;
    private final RecyclerViewListener onItemClickListener;

    private SparseBooleanArray mSelectedItemsIds;

    public SharedPreferences prefs;
    public boolean useDefaults;

    public AccountsRecyclerViewAdapter(Context context, ArrayList<Account> accounts, RecyclerViewListener onItemClickListener) {
        this.context = context;
        this.accounts = accounts;
        this.onItemClickListener = onItemClickListener;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //For Custom View Properties
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_account_default_appearance), true);

        if (viewType == VIEW_TYPE_EMPTY) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.accounts_empty, parent, false);
            return new ViewHolderEmpty(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_item, parent, false);
            return new ViewHolder(context, view, prefs, useDefaults);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder genericHolder, int position) {
        switch (genericHolder.getItemViewType()) {
            case VIEW_TYPE_NORMAL:
                ViewHolder holder = (ViewHolder) genericHolder;
                Account account = accounts.get(position);
                Money balance = new Money(account.balance);
                Locale locale = context.getResources().getConfiguration().locale;

                //Change gradient
                try {
                    //Older color to black gradient (0xFF00FF33,0xFF000000)
                    GradientDrawable defaultGradientPos = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{0xFF4ac925, 0xFF4ac925});
                    GradientDrawable defaultGradientNeg = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{0xFFe00707, 0xFFe00707});

                    if (useDefaults) {
                        if (balance.isPositive(locale)) {
                            holder.sideBar.setBackgroundDrawable(defaultGradientPos);
                        } else {
                            holder.sideBar.setBackgroundDrawable(defaultGradientNeg);
                        }

                    } else {
                        if (balance.isPositive(locale)) {
                            holder.sideBar.setBackgroundDrawable(defaultGradientPos);
                        } else {
                            holder.sideBar.setBackgroundDrawable(defaultGradientNeg);
                        }
                    }

                } catch (Exception e) {
                    Timber.e("Error setting custom gradient");
                    e.printStackTrace();
                    Toast.makeText(context, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
                }

                if (account.name != null) {
                    holder.tvName.setText(account.name);
                }

                if (balance != null) {
                    holder.tvBalance.setText(context.getString(R.string.balance) + " " + balance.getNumberFormat(locale));
                }

                if (account.date != null) {
                    DateTime d = new DateTime();
                    d.setStringSQL(account.date);
                    holder.tvDate.setText(context.getString(R.string.date) + " " + d.getReadableDate());
                }

                if (account.time != null) {
                    DateTime t = new DateTime();
                    t.setStringSQL(account.time);
                    holder.tvTime.setText(context.getString(R.string.time) + " " + t.getReadableTime());
                }

                holder.setOnItemClickListener(account, onItemClickListener);

                if (position == AccountsFragment.currentAccount && AccountsFragment.mActionMode == null) {
                    holder.view.setBackgroundColor(0x7734B5E4);
                } else if (mSelectedItemsIds.get(position)) {
                    holder.view.setBackgroundColor(0x9934B5E4);
                } else {
                    holder.view.setBackgroundColor(Color.TRANSPARENT);
                }

                break;

            case VIEW_TYPE_EMPTY:
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (accounts == null || accounts.isEmpty()) {
            return 1;   //return 1 to show the empty view
        }

        return accounts.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (accounts == null || accounts.size() == 0) {
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_NORMAL;
        }
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

    private Context getContext() {
        return context;
    }

    public Account getAccount(int position) {
        return accounts.get(position);
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public View layout;
        public LinearLayout sideBar;
        public TextView tvName;
        public TextView tvBalance;
        public TextView tvDate;
        public TextView tvTime;

        public ViewHolder(final Context context, final View view, final SharedPreferences prefs, final boolean useDefaults) {
            super(view);
            this.view = view;
            layout = view.findViewById(R.id.account_layout);
            sideBar = (LinearLayout) view.findViewById(R.id.account_gradient);
            tvName = (TextView) view.findViewById(R.id.account_name);
            tvBalance = (TextView) view.findViewById(R.id.account_balance);
            tvDate = (TextView) view.findViewById(R.id.account_date);
            tvTime = (TextView) view.findViewById(R.id.account_time);


            //Change Background Colors
            try {
                if (!useDefaults) {
                    int startColor = prefs.getInt(context.getString(R.string.pref_key_account_start_background_color), ContextCompat.getColor(context, R.color.white));
                    int endColor = prefs.getInt(context.getString(R.string.pref_key_account_end_background_color), ContextCompat.getColor(context, R.color.white));
                    GradientDrawable defaultGradient = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{startColor, endColor});
                    layout.setBackgroundDrawable(defaultGradient);
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
        }

        public void setOnItemClickListener(final Account account, final RecyclerViewListener onItemClickListener) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(account, getAdapterPosition());
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onItemClickListener.onItemLongClick(account, getAdapterPosition());
                }
            });
        }
    }

    public static class ViewHolderEmpty extends RecyclerView.ViewHolder {
        public TextView view;

        public ViewHolderEmpty(final TextView view) {
            super(view);
            this.view = view;
        }
    }
}
