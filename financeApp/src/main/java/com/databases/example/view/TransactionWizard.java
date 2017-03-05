package com.databases.example.view;

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
import com.databases.example.app.TransactionsFragment;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.Money;
import com.databases.example.data.MyContentProvider;
import com.databases.example.data.TransactionWizardInfoPage;
import com.databases.example.data.TransactionWizardOptionalPage;
import com.databases.example.model.Transaction;
import com.wizardpager.wizard.WizardDialogFragment;
import com.wizardpager.wizard.model.AbstractWizardModel;
import com.wizardpager.wizard.model.PageList;
import com.wizardpager.wizard.ui.StepPagerStrip;

import java.util.Locale;

public class TransactionWizard extends WizardDialogFragment {
    private final AbstractWizardModel mWizardModel = new TransactionWizardModel(getActivity());

    public static TransactionWizard newInstance(Transaction record) {
        TransactionWizard frag = new TransactionWizard();

        if (record != null) {
            final Bundle bundle = new Bundle();

            final Bundle bdl1 = new Bundle();
            bdl1.putInt("id", record.id);
            bdl1.putInt("acct_id", record.acctId);
            bdl1.putInt("plan_id", record.planId);
            bdl1.putString("name", record.name);
            bdl1.putString("value", record.value);
            bdl1.putString("type", record.type);
            bdl1.putString("category", record.category);
            bundle.putBundle("Transaction Info", bdl1);

            final Bundle bdl2 = new Bundle();
            bdl2.putString("checknum", record.checknum);
            bdl2.putString("memo", record.memo);
            bdl2.putString("date", record.date);
            bdl2.putString("time", record.time);
            bdl2.putString("cleared", record.cleared);
            bundle.putBundle("Optional", bdl2);

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
        final Bundle bundleInfo = mWizardModel.findByKey("Transaction Info").getData();
        final Bundle bundleOptional = mWizardModel.findByKey("Optional").getData();
        final Locale locale = getResources().getConfiguration().locale;

        String value = "";
        final DateTime transactionDate = new DateTime();
        transactionDate.setStringReadable(bundleOptional.getString(TransactionWizardOptionalPage.DATE_DATA_KEY).trim());
        final DateTime transactionTime = new DateTime();
        transactionTime.setStringReadable(bundleOptional.getString(TransactionWizardOptionalPage.TIME_DATA_KEY).trim());

        //Check to see if value is a number
        boolean validValue;
        try {
            Money transactionValue = new Money(bundleInfo.getString(TransactionWizardInfoPage.VALUE_DATA_KEY).trim());
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
                transactionValues.put(DatabaseHelper.TRANS_ID, bundleInfo.getInt(TransactionWizardInfoPage.ID_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, bundleInfo.getInt(TransactionWizardInfoPage.ACCOUNT_ID_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, bundleInfo.getInt(TransactionWizardInfoPage.PLAN_ID_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_NAME, bundleInfo.getString(TransactionWizardInfoPage.NAME_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_VALUE, value);
                transactionValues.put(DatabaseHelper.TRANS_TYPE, bundleInfo.getString(TransactionWizardInfoPage.TYPE_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_CATEGORY, bundleInfo.getString(TransactionWizardInfoPage.CATEGORY_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, bundleOptional.getString(TransactionWizardOptionalPage.CHECKNUM_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_MEMO, bundleOptional.getString(TransactionWizardOptionalPage.MEMO_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_TIME, transactionTime.getSQLTime(locale));
                transactionValues.put(DatabaseHelper.TRANS_DATE, transactionDate.getSQLDate(locale));
                transactionValues.put(DatabaseHelper.TRANS_CLEARED, bundleOptional.getString(TransactionWizardOptionalPage.CLEARED_DATA_KEY));

                getActivity().getContentResolver().update(Uri.parse(MyContentProvider.TRANSACTIONS_URI + "/" + bundleInfo.getInt(TransactionWizardInfoPage.ID_DATA_KEY)), transactionValues, DatabaseHelper.TRANS_ID + "=" + bundleInfo.getInt(TransactionWizardInfoPage.ID_DATA_KEY), null);
            } else {
                ContentValues transactionValues = new ContentValues();
                transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, TransactionsFragment.account_id);
                transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, 0);
                transactionValues.put(DatabaseHelper.TRANS_NAME, bundleInfo.getString(TransactionWizardInfoPage.NAME_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_VALUE, value);
                transactionValues.put(DatabaseHelper.TRANS_TYPE, bundleInfo.getString(TransactionWizardInfoPage.TYPE_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_CATEGORY, bundleInfo.getString(TransactionWizardInfoPage.CATEGORY_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, bundleOptional.getString(TransactionWizardOptionalPage.CHECKNUM_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_MEMO, bundleOptional.getString(TransactionWizardOptionalPage.MEMO_DATA_KEY));
                transactionValues.put(DatabaseHelper.TRANS_TIME, transactionTime.getSQLTime(locale));
                transactionValues.put(DatabaseHelper.TRANS_DATE, transactionDate.getSQLDate(locale));
                transactionValues.put(DatabaseHelper.TRANS_CLEARED, bundleOptional.getString(TransactionWizardOptionalPage.CLEARED_DATA_KEY));

                getActivity().getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
            }

        }

    }

    //Allow back button to be used to go back a step in the wizard
    @Override
    public boolean useBackForPrevious() {
        return true;
    }
}

class TransactionWizardModel extends AbstractWizardModel {
    public TransactionWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(

                new TransactionWizardInfoPage(this, "Transaction Info")
                        .setRequired(true),

                new TransactionWizardOptionalPage(this, "Optional")
        );
    }
}