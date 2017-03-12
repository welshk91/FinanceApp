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
import com.databases.example.model.Transaction;
import com.databases.example.utils.Constants;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;

import java.util.ArrayList;
import java.util.Locale;

public class TransactionsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_EMPTY = 0;
    private final int VIEW_TYPE_NORMAL = 1;

    public final Context context;
    public ArrayList<Transaction> transactions;
    private final RecyclerViewListener onItemClickListener;

    private SparseBooleanArray mSelectedItemsIds;

    public SharedPreferences prefs;
    public boolean useDefaults;

    public TransactionsRecyclerViewAdapter(Context context, ArrayList<Transaction> transactions, RecyclerViewListener onItemClickListener) {
        this.context = context;
        this.transactions = transactions;
        this.onItemClickListener = onItemClickListener;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //For Custom View Properties
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_transaction_default_appearance), true);

        if (viewType == VIEW_TYPE_EMPTY) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty, parent, false);
            return new ViewHolderEmpty(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
            return new ViewHolder(context, view, prefs, useDefaults);
        }
    }


    public Transaction getTransaction(int position) {
        return transactions.get(position);
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder genericHolder, int position) {
        switch (genericHolder.getItemViewType()) {
            case VIEW_TYPE_NORMAL:
                ViewHolder holder = (ViewHolder) genericHolder;
                Transaction transaction = transactions.get(position);

                Money value = new Money(transaction.value);
                Locale locale = context.getResources().getConfiguration().locale;

                //Change gradient
                try {
                    GradientDrawable defaultGradientPos = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{0xFF4ac925, 0xFF4ac925});

                    GradientDrawable defaultGradientNeg = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{0xFFe00707, 0xFFe00707});

                    if (useDefaults) {
                        if (transaction.type.contains(Constants.DEPOSIT)) {
                            holder.sideBar.setBackgroundDrawable(defaultGradientPos);
                        } else {
                            holder.sideBar.setBackgroundDrawable(defaultGradientNeg);
                        }

                    } else {
                        if (transaction.type.contains(Constants.DEPOSIT)) {
                            holder.sideBar.setBackgroundDrawable(defaultGradientPos);
                        } else {
                            holder.sideBar.setBackgroundDrawable(defaultGradientNeg);
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(context, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
                }

                if (transaction.name != null) {
                    holder.tvName.setText(transaction.name);

                    if (transaction.planId != 0) {
                        holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.transaction_plans_yes));
                    } else {
                        holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.transaction_plans_no));
                    }

                }

                if (transaction.value != null) {
                    holder.tvValue.setText(context.getString(R.string.value) + " " + value.getNumberFormat(locale));
                }

                if (transaction.type != null) {
                    holder.tvType.setText(context.getString(R.string.type) + " " + transaction.type);
                }

                if (transaction.category != null) {
                    holder.tvCategory.setText(context.getString(R.string.category) + " " + transaction.category);
                }

                if (transaction.checknum != null) {
                    holder.tvChecknum.setText(context.getString(R.string.checknum) + " " + transaction.checknum);
                }

                if (transaction.memo != null) {
                    holder.tvMemo.setText(context.getString(R.string.memo) + " " + transaction.memo);
                }

                if (transaction.date != null) {
                    DateTime d = new DateTime();
                    d.setStringSQL(transaction.date);
                    holder.tvDate.setText(context.getString(R.string.date) + " " + d.getReadableDate());
                }

                if (transaction.time != null) {
                    DateTime t = new DateTime();
                    t.setStringSQL(transaction.time);
                    holder.tvTime.setText(context.getString(R.string.time) + " " + t.getReadableTime());
                }

                if (transaction.cleared != null) {
                    holder.tvCleared.setText(context.getString(R.string.cleared) + " " + transaction.cleared);
                }

                holder.setOnItemClickListener(transaction, onItemClickListener);

                holder.view.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4 : Color.TRANSPARENT);

                break;

            case VIEW_TYPE_EMPTY:
                ViewHolderEmpty holderEmpty = (ViewHolderEmpty) genericHolder;
                holderEmpty.view.setText("No Transactions\n\nTo Add A Transaction, Please Use The ActionBar On The Top");
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (transactions == null || transactions.isEmpty()) {
            return 1;   //return 1 to show the empty view
        }

        return transactions.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (transactions == null || transactions.size() == 0) {
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public View layout;
        public LinearLayout sideBar;
        public TextView tvName;
        public TextView tvValue;
        public TextView tvType;
        public TextView tvCategory;
        public TextView tvChecknum;
        public TextView tvMemo;
        public TextView tvTime;
        public TextView tvDate;
        public TextView tvCleared;

        public ViewHolder(final Context context, final View view, final SharedPreferences prefs, final boolean useDefaults) {
            super(view);
            this.view = view;

            layout = view.findViewById(R.id.transaction_layout);
            sideBar = (LinearLayout) view.findViewById(R.id.transaction_gradient);
            tvName = (TextView) view.findViewById(R.id.transaction_name);
            tvValue = (TextView) view.findViewById(R.id.transaction_value);
            tvType = (TextView) view.findViewById(R.id.transaction_type);
            tvCategory = (TextView) view.findViewById(R.id.transaction_category);
            tvChecknum = (TextView) view.findViewById(R.id.transaction_checknum);
            tvMemo = (TextView) view.findViewById(R.id.transaction_memo);
            tvDate = (TextView) view.findViewById(R.id.transaction_date);
            tvTime = (TextView) view.findViewById(R.id.transaction_time);
            tvCleared = (TextView) view.findViewById(R.id.transaction_cleared);

            //Change Background Colors
            try {
                if (!useDefaults) {
                    int startColor = prefs.getInt(context.getString(R.string.pref_key_transaction_start_background_color), ContextCompat.getColor(context, R.color.white));
                    int endColor = prefs.getInt(context.getString(R.string.pref_key_transaction_end_background_color), ContextCompat.getColor(context, R.color.white));

                    GradientDrawable defaultGradient = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{startColor, endColor});
                    layout.setBackgroundDrawable(defaultGradient);
                }
            } catch (Exception e) {
                Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
            }

            try {
                String DefaultSize = prefs.getString(context.getString(R.string.pref_key_transaction_name_size), "24");

                if (useDefaults) {
                    tvName.setTextSize(24);
                } else {
                    tvName.setTextSize(Integer.parseInt(DefaultSize));
                }

            } catch (Exception e) {
                Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
            }

            try {
                int DefaultColor = prefs.getInt(context.getString(R.string.pref_key_transaction_name_color), ContextCompat.getColor(context, R.color.transaction_title_default));

                if (useDefaults) {
                    tvName.setTextColor(ContextCompat.getColor(context, R.color.transaction_title_default));
                } else {
                    tvName.setTextColor(DefaultColor);
                }

            } catch (Exception e) {
                Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
            }

            try {
                String DefaultSize = prefs.getString(context.getString(R.string.pref_key_transaction_field_size), "14");

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
                int DefaultColor = prefs.getInt(context.getString(R.string.pref_key_transaction_details_color), ContextCompat.getColor(context, R.color.transaction_details_default));

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

            if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_transaction_name_show), true)) {
                tvName.setVisibility(View.VISIBLE);
            } else {
                tvName.setVisibility(View.GONE);
            }

            if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_transaction_value_show), true)) {
                tvValue.setVisibility(View.VISIBLE);
            } else {
                tvValue.setVisibility(View.GONE);
            }

            if (prefs.getBoolean(context.getString(R.string.pref_key_transaction_type_show), false) && !useDefaults) {
                tvType.setVisibility(View.VISIBLE);
            } else {
                tvType.setVisibility(View.GONE);
            }

            if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_transaction_category_show), true)) {
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            if (prefs.getBoolean(context.getString(R.string.pref_key_transaction_checknum_show), false) && !useDefaults) {
                tvChecknum.setVisibility(View.VISIBLE);
            } else {
                tvChecknum.setVisibility(View.GONE);
            }

            if (prefs.getBoolean(context.getString(R.string.pref_key_transaction_memo_show), false) && !useDefaults) {
                tvMemo.setVisibility(View.VISIBLE);
            } else {
                tvMemo.setVisibility(View.GONE);
            }

            if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_transaction_date_show), true)) {
                tvDate.setVisibility(View.VISIBLE);
            } else {
                tvDate.setVisibility(View.GONE);
            }

            if (prefs.getBoolean(context.getString(R.string.pref_key_transaction_time_show), false) && !useDefaults) {
                tvTime.setVisibility(View.VISIBLE);
            } else {
                tvTime.setVisibility(View.GONE);
            }

            if (prefs.getBoolean(context.getString(R.string.pref_key_transaction_cleared_show), false) && !useDefaults) {
                tvCleared.setVisibility(View.VISIBLE);
            } else {
                tvCleared.setVisibility(View.GONE);
            }
        }

        public void setOnItemClickListener(final Transaction transaction, final RecyclerViewListener onItemClickListener) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(transaction, getAdapterPosition());
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onItemClickListener.onItemLongClick(transaction, getAdapterPosition());
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
