package com.databases.example.features.plans;

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
import com.databases.example.app.RecyclerViewListener;
import com.databases.example.utils.Constants;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;

import java.util.ArrayList;
import java.util.Locale;

public class PlansRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_EMPTY = 0;
    private final int VIEW_TYPE_NORMAL = 1;

    public final Context context;
    public ArrayList<Plan> plans;
    private final RecyclerViewListener onItemClickListener;

    private SparseBooleanArray mSelectedItemsIds;

    public SharedPreferences prefs;
    public boolean useDefaults;

    public PlansRecyclerViewAdapter(Context context, ArrayList<Plan> plans, RecyclerViewListener onItemClickListener) {
        this.context = context;
        this.plans = plans;
        this.onItemClickListener = onItemClickListener;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public Plan getPlan(int position) {
        return plans.get(position);
    }

    public ArrayList<Plan> getPlans() {
        return plans;
    }

    public void setPlans(ArrayList<Plan> plans) {
        this.plans = plans;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //For Custom View Properties
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_plan_default_appearance), true);

        if (viewType == VIEW_TYPE_EMPTY) {
            TextView view = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty, parent, false);
            return new ViewHolderEmpty(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_item, parent, false);
            return new ViewHolder(context, view, prefs, useDefaults);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder genericHolder, int position) {
        switch (genericHolder.getItemViewType()) {
            case VIEW_TYPE_NORMAL:
                ViewHolder holder = (ViewHolder) genericHolder;
                Plan plan = plans.get(position);

                Money value = new Money(plan.value);
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
                        if (plan.type.contains(Constants.DEPOSIT)) {
                            holder.sideBar.setBackgroundDrawable(defaultGradientPos);
                        } else {
                            holder.sideBar.setBackgroundDrawable(defaultGradientNeg);
                        }

                    } else {
                        if (plan.type.contains(Constants.DEPOSIT)) {
                            holder.sideBar.setBackgroundDrawable(defaultGradientPos);
                        } else {
                            holder.sideBar.setBackgroundDrawable(defaultGradientNeg);
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(context, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
                }

                final DateTime temp = new DateTime();

                if (plan.name != null) {
                    holder.tvName.setText(plan.name);
                }
                if (plan.acctId != -1) {
                    holder.tvAccount.setText(context.getString(R.string.acount_id) + ": " + plan.acctId);
                }
                if (value != null) {
                    holder.tvValue.setText(context.getString(R.string.value) + ": " + value.getNumberFormat(locale));
                }
                if (plan.type != null) {
                    holder.tvType.setText(context.getString(R.string.type) + ": " + plan.type);
                }
                if (plan.category != null) {
                    holder.tvCategory.setText(context.getString(R.string.category) + ": " + plan.category);
                }
                if (plan.memo != null) {
                    holder.tvMemo.setText(context.getString(R.string.memo) + ": " + plan.memo);
                }
                if (plan.offset != null) {
                    temp.setStringSQL(plan.offset);
                    holder.tvOffset.setText(context.getString(R.string.offset) + ": " + temp.getReadableDate());
                }
                if (plan.rate != null) {
                    holder.tvRate.setText(context.getString(R.string.rate) + ": " + plan.rate);
                }
                if (plan.next != null) {
                    temp.setStringSQL(plan.next);
                    holder.tvNext.setText(context.getString(R.string.next) + ": " + temp.getReadableDate());
                }
                if (plan.scheduled != null) {
                    holder.tvScheduled.setText(context.getString(R.string.scheduled) + ": " + plan.scheduled);
                }
                if (plan.cleared != null) {
                    holder.tvCleared.setText(context.getString(R.string.cleared) + ": " + plan.cleared);
                }

                if (plan.scheduled.equals("false")) {
                    holder.view.setAlpha(.5f);
                } else {
                    holder.view.setAlpha(1.0f);
                }

                holder.setOnItemClickListener(plan, onItemClickListener);

                holder.view.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4 : Color.TRANSPARENT);

                break;

            case VIEW_TYPE_EMPTY:
                ViewHolderEmpty holderEmpty = (ViewHolderEmpty) genericHolder;
                holderEmpty.view.setText("Nothing Scheduled\n\nTo Add A Plan, Please Use The ActionBar On The Top");
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (plans == null || plans.isEmpty()) {
            return 1;   //return 1 to show the empty view
        }

        return plans.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (plans == null || plans.size() == 0) {
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
        return mSelectedItemsIds.size();// mSelectedCount;
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public View layout;
        public LinearLayout sideBar;
        TextView tvName;
        TextView tvAccount;
        TextView tvValue;
        TextView tvType;
        TextView tvCategory;
        TextView tvMemo;
        TextView tvOffset;
        TextView tvRate;
        TextView tvNext;
        TextView tvScheduled;
        TextView tvCleared;

        public ViewHolder(final Context context, final View view, final SharedPreferences prefs, final boolean useDefaults) {
            super(view);
            this.view = view;

            layout = view.findViewById(R.id.plan_layout);
            sideBar = (LinearLayout) view.findViewById(R.id.plan_gradient);
            tvName = (TextView) view.findViewById(R.id.plan_name);
            tvAccount = (TextView) view.findViewById(R.id.plan_account);
            tvValue = (TextView) view.findViewById(R.id.plan_value);
            tvType = (TextView) view.findViewById(R.id.plan_type);
            tvCategory = (TextView) view.findViewById(R.id.plan_category);
            tvMemo = (TextView) view.findViewById(R.id.plan_memo);
            tvOffset = (TextView) view.findViewById(R.id.plan_offset);
            tvRate = (TextView) view.findViewById(R.id.plan_rate);
            tvNext = (TextView) view.findViewById(R.id.plan_next);
            tvScheduled = (TextView) view.findViewById(R.id.plan_scheduled);
            tvCleared = (TextView) view.findViewById(R.id.plan_cleared);

            //Change Background Colors
            try {
                if (!useDefaults) {
                    int startColor = prefs.getInt(context.getString(R.string.pref_key_plan_start_background_color), ContextCompat.getColor(context, R.color.white));
                    int endColor = prefs.getInt(context.getString(R.string.pref_key_plan_end_background_color), ContextCompat.getColor(context, R.color.white));
                    GradientDrawable customGradient = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{startColor, endColor});
                    layout.setBackgroundDrawable(customGradient);
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

        }

        public void setOnItemClickListener(final Plan plan, final RecyclerViewListener onItemClickListener) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(plan, getAdapterPosition());
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onItemClickListener.onItemLongClick(plan, getAdapterPosition());
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
