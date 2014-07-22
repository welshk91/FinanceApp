package com.databases.example.data;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.databases.example.view.AccountWizardInfoFragment;
import com.wizardpager.wizard.model.ModelCallbacks;
import com.wizardpager.wizard.model.Page;
import com.wizardpager.wizard.model.ReviewItem;

import java.util.ArrayList;

public class AccountWizardInfoPage extends Page{
    public static final String ID_DATA_KEY = "id";

    public static final String NAME_DATA_KEY = "name";
    public static final String BALANCE_DATA_KEY = "balance";
    public static final String TIME_DATA_KEY = "time";
    public static final String DATE_DATA_KEY = "date";

    public AccountWizardInfoPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return AccountWizardInfoFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Name", mData.getString(NAME_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Balance", mData.getString(BALANCE_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Time", mData.getString(TIME_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Date", mData.getString(DATE_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(NAME_DATA_KEY)) && !TextUtils.isEmpty(mData.getString(BALANCE_DATA_KEY));
    }
}
