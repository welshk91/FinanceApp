package com.databases.example.wizard;

import android.support.v4.app.Fragment;

import com.wizardpager.wizard.model.ModelCallbacks;
import com.wizardpager.wizard.model.Page;
import com.wizardpager.wizard.model.ReviewItem;

import java.util.ArrayList;

public class TransactionWizardOptionalPage extends Page{
    public static final String CHECKNUM_DATA_KEY = "checknum";
    public static final String MEMO_DATA_KEY = "memo";
    public static final String DATE_DATA_KEY = "date";
    public static final String TIME_DATA_KEY = "time";
    public static final String CLEARED_DATA_KEY = "cleared";

    public TransactionWizardOptionalPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return TransactionWizardOptionalFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Check Number", mData.getString(CHECKNUM_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Memo", mData.getString(MEMO_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Date", mData.getString(DATE_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Time", mData.getString(TIME_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Cleared", mData.getString(CLEARED_DATA_KEY), getKey(), -1));
    }
}
