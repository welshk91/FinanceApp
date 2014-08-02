package com.databases.example.data;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.databases.example.view.PlanWizardInfo2Fragment;
import com.wizardpager.wizard.model.ModelCallbacks;
import com.wizardpager.wizard.model.Page;
import com.wizardpager.wizard.model.ReviewItem;

import java.util.ArrayList;

public class PlanWizardInfo2Page extends Page {
    public static final String ACCOUNT_ID_DATA_KEY = "accountID";
    public static final String ACCOUNT_DATA_KEY = "account";
    public static final String DATE_DATA_KEY = "date";
    public static final String RATE_DATA_KEY = "rate";
    public static final String RATE_TYPE_DATA_KEY = "rate type";

    public PlanWizardInfo2Page(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return PlanWizardInfo2Fragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Account", mData.getString(ACCOUNT_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Starting On", mData.getString(DATE_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Rate", mData.getString(RATE_DATA_KEY) + " " + mData.getString(RATE_TYPE_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(RATE_DATA_KEY));
    }
}