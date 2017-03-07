package com.databases.example.wizard;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.wizardpager.wizard.model.ModelCallbacks;
import com.wizardpager.wizard.model.Page;
import com.wizardpager.wizard.model.ReviewItem;

import java.util.ArrayList;

public class PlanWizardInfo1Page extends Page {
    public static final String ID_DATA_KEY = "id";

    public static final String NAME_DATA_KEY = "name";
    public static final String VALUE_DATA_KEY = "value";
    public static final String TYPE_DATA_KEY = "type";
    public static final String CATEGORY_DATA_KEY = "category";

    public PlanWizardInfo1Page(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return PlanWizardInfo1Fragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Name", mData.getString(NAME_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Value", mData.getString(VALUE_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Type", mData.getString(TYPE_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Category", mData.getString(CATEGORY_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(NAME_DATA_KEY)) && !TextUtils.isEmpty(mData.getString(VALUE_DATA_KEY));
    }
}