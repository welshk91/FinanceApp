package com.databases.example.data;

import android.support.v4.app.Fragment;

import com.databases.example.view.PlanWizardOptionalFragment;
import com.wizardpager.wizard.model.ModelCallbacks;
import com.wizardpager.wizard.model.Page;
import com.wizardpager.wizard.model.ReviewItem;

import java.util.ArrayList;

public class PlanWizardOptionalPage extends Page {
    public static final String MEMO_DATA_KEY = "memo";
    public static final String CLEARED_DATA_KEY = "cleared";

    public PlanWizardOptionalPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return PlanWizardOptionalFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Memo", mData.getString(MEMO_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Cleared", mData.getString(CLEARED_DATA_KEY), getKey(), -1));
    }
}
