package com.databases.example.features.plans;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.database.DatabaseHelper;
import com.databases.example.database.MyContentProvider;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;
import com.wizardpager.wizard.WizardDialogFragment;
import com.wizardpager.wizard.model.AbstractWizardModel;
import com.wizardpager.wizard.model.PageList;
import com.wizardpager.wizard.ui.StepPagerStrip;

import java.util.Locale;

/**
 * Created by kev on 7/31/14.
 */
public class PlanWizard extends WizardDialogFragment {
    private final AbstractWizardModel mWizardModel = new PlanWizardModel(getActivity());
    private static Plan oldPlan;

    public static PlanWizard newInstance(Plan record) {
        PlanWizard frag = new PlanWizard();

        if (record != null) {
            oldPlan = record;

            //Parse Rate (token 0 is amount, token 1 is type)
            String phrase = record.rate;
            String delims = "[ ]+";
            String[] tokens = phrase.split(delims);

            final Bundle bundle = new Bundle();

            final Bundle bdl1 = new Bundle();
            bdl1.putInt("id", record.id);
            bdl1.putString("name", record.name);
            bdl1.putString("value", record.value);
            bdl1.putString("type", record.type);
            bdl1.putString("category", record.category);
            bundle.putBundle("Transaction Info", bdl1);

            final Bundle bdl2 = new Bundle();
            bdl2.putInt("accountID", record.acctId);
            bdl2.putString("account", String.valueOf(record.acctId));
            bdl2.putString("date", record.offset);
            bdl2.putString("rate", tokens[0]);
            bdl2.putString("rate type", tokens[1]);
            bundle.putBundle("Plan Info", bdl2);

            final Bundle bdl3 = new Bundle();
            bdl3.putString("memo", record.memo);
            bdl3.putString("cleared", record.cleared);
            bundle.putBundle("Optional", bdl3);

            frag.setArguments(bundle);
        }

        return frag;
    }

    //Set Style & Theme of Dialog
    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 14) {
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog);
        } else {
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Dialog);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.wizard, container, false);

        ViewPager mPager = (ViewPager) myFragmentView.findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(5);
        StepPagerStrip mStepPagerStrip = (StepPagerStrip) myFragmentView.findViewById(R.id.strip);
        Button mNextButton = (Button) myFragmentView.findViewById(R.id.next_button);
        Button mPrevButton = (Button) myFragmentView.findViewById(R.id.prev_button);
        setControls(mPager, mStepPagerStrip, mNextButton, mPrevButton);

        //Load Data into Wizard
        final Bundle bundle = getArguments();
        if (bundle != null) {
            mWizardModel.load(bundle);
        }
        return myFragmentView;
    }

//		@Override
//		public void onStart() {
//			super.onStart();
//
//			// safety check
//			if (getDialog() == null) {
//				return;
//			}
//
//			int dialogWidth = 500;
//			int dialogHeight = 600;
//
//			getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
//		}

    //Create Wizard
    @Override
    public AbstractWizardModel onCreateModel() {
        return mWizardModel;
    }

    //Method that runs after wizard is finished
    @Override
    public void onSubmit() {
        final Bundle bundleInfo1 = mWizardModel.findByKey("Transaction Info").getData();
        final Bundle bundleInfo2 = mWizardModel.findByKey("Plan Info").getData();
        final Bundle bundleOptional = mWizardModel.findByKey("Optional").getData();
        final Locale locale = getResources().getConfiguration().locale;

        String value = "";
        final DateTime transactionDate = new DateTime();
        transactionDate.setStringReadable(bundleInfo2.getString(PlanWizardInfo2Page.DATE_DATA_KEY).trim());

        //Check to see if value is a number
        boolean validValue;
        try {
            Money transactionValue = new Money(bundleInfo1.getString(PlanWizardInfo1Page.VALUE_DATA_KEY).trim());
            value = transactionValue.getBigDecimal(locale) + "";
            validValue = true;
        } catch (Exception e) {
            validValue = false;
            Toast.makeText(getActivity(), "Please enter a valid value", Toast.LENGTH_SHORT).show();
        }

        if (validValue) {
            getDialog().cancel();

            if (getArguments() != null) {
                ContentValues transactionValues = new ContentValues();
                transactionValues.put(DatabaseHelper.PLAN_ID, bundleInfo1.getInt(PlanWizardInfo1Page.ID_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_ACCT_ID, bundleInfo2.getInt(PlanWizardInfo2Page.ACCOUNT_ID_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_NAME, bundleInfo1.getString(PlanWizardInfo1Page.NAME_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_VALUE, value);
                transactionValues.put(DatabaseHelper.PLAN_TYPE, bundleInfo1.getString(PlanWizardInfo1Page.TYPE_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_CATEGORY, bundleInfo1.getString(PlanWizardInfo1Page.CATEGORY_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_MEMO, bundleOptional.getString(PlanWizardOptionalPage.MEMO_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_OFFSET, transactionDate.getSQLDate(locale));
                transactionValues.put(DatabaseHelper.PLAN_RATE, bundleInfo2.getString(PlanWizardInfo2Page.RATE_DATA_KEY) + " " + bundleInfo2.getString(PlanWizardInfo2Page.RATE_TYPE_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_NEXT, "");
                transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "true");
                transactionValues.put(DatabaseHelper.PLAN_CLEARED, bundleOptional.getString(PlanWizardOptionalPage.CLEARED_DATA_KEY));

                //Cancel old plan
                PlanUtils.cancelPlan(getActivity(), oldPlan);

                //Update Plan
                getActivity().getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + bundleInfo1.getInt(PlanWizardInfo1Page.ID_DATA_KEY)), transactionValues, DatabaseHelper.PLAN_ID + "=" + bundleInfo1.getInt(PlanWizardInfo1Page.ID_DATA_KEY), null);

                //Schedule Plan
                Plan record = new Plan(bundleInfo1.getInt(PlanWizardInfo1Page.ID_DATA_KEY),
                        bundleInfo2.getInt(PlanWizardInfo2Page.ACCOUNT_ID_DATA_KEY),
                        bundleInfo1.getString(PlanWizardInfo1Page.NAME_DATA_KEY),
                        value, bundleInfo1.getString(PlanWizardInfo1Page.TYPE_DATA_KEY),
                        bundleInfo1.getString(PlanWizardInfo1Page.CATEGORY_DATA_KEY),
                        bundleOptional.getString(PlanWizardOptionalPage.MEMO_DATA_KEY),
                        transactionDate.getSQLDate(locale),
                        bundleInfo2.getString(PlanWizardInfo2Page.RATE_DATA_KEY) + " " + bundleInfo2.getString(PlanWizardInfo2Page.RATE_TYPE_DATA_KEY),
                        "",
                        "true",
                        bundleOptional.getString(PlanWizardOptionalPage.CLEARED_DATA_KEY));

                PlanUtils.schedule(getActivity(), record);
            } else {
                ContentValues transactionValues = new ContentValues();
                transactionValues.put(DatabaseHelper.PLAN_ACCT_ID, bundleInfo2.getInt(PlanWizardInfo2Page.ACCOUNT_ID_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_NAME, bundleInfo1.getString(PlanWizardInfo1Page.NAME_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_VALUE, value);
                transactionValues.put(DatabaseHelper.PLAN_TYPE, bundleInfo1.getString(PlanWizardInfo1Page.TYPE_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_CATEGORY, bundleInfo1.getString(PlanWizardInfo1Page.CATEGORY_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_MEMO, bundleOptional.getString(PlanWizardOptionalPage.MEMO_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_OFFSET, transactionDate.getSQLDate(locale));
                transactionValues.put(DatabaseHelper.PLAN_RATE, bundleInfo2.getString(PlanWizardInfo2Page.RATE_DATA_KEY) + " " + bundleInfo2.getString(PlanWizardInfo2Page.RATE_TYPE_DATA_KEY));
                transactionValues.put(DatabaseHelper.PLAN_NEXT, "");
                transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "true");
                transactionValues.put(DatabaseHelper.PLAN_CLEARED, bundleOptional.getString(PlanWizardOptionalPage.CLEARED_DATA_KEY));

                Uri u = getActivity().getContentResolver().insert(MyContentProvider.PLANS_URI, transactionValues);

                //Schedule Plan
                Plan record = new Plan(Integer.parseInt(u.getLastPathSegment()),
                        bundleInfo2.getInt(PlanWizardInfo2Page.ACCOUNT_ID_DATA_KEY),
                        bundleInfo1.getString(PlanWizardInfo1Page.NAME_DATA_KEY),
                        value, bundleInfo1.getString(PlanWizardInfo1Page.TYPE_DATA_KEY),
                        bundleInfo1.getString(PlanWizardInfo1Page.CATEGORY_DATA_KEY),
                        bundleOptional.getString(PlanWizardOptionalPage.MEMO_DATA_KEY),
                        transactionDate.getSQLDate(locale),
                        bundleInfo2.getString(PlanWizardInfo2Page.RATE_DATA_KEY) + " " + bundleInfo2.getString(PlanWizardInfo2Page.RATE_TYPE_DATA_KEY),
                        "",
                        "true",
                        bundleOptional.getString(PlanWizardOptionalPage.CLEARED_DATA_KEY));

                PlanUtils.schedule(getActivity(), record);
            }
        }
    }

    //Allow back button to be used to go back a step in the wizard
    @Override
    public boolean useBackForPrevious() {
        return true;
    }
}

class PlanWizardModel extends AbstractWizardModel {
    public PlanWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(

                new PlanWizardInfo1Page(this, "Transaction Info")
                        .setRequired(true),
                new PlanWizardInfo2Page(this, "Plan Info")
                        .setRequired(true),
                new PlanWizardOptionalPage(this, "Optional")
        );
    }
}
