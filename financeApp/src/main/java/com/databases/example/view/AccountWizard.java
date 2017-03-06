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
import com.databases.example.data.AccountWizardInfoPage;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.Money;
import com.databases.example.data.MyContentProvider;
import com.databases.example.model.Account;
import com.databases.example.utils.Constants;
import com.wizardpager.wizard.WizardDialogFragment;
import com.wizardpager.wizard.model.AbstractWizardModel;
import com.wizardpager.wizard.model.PageList;
import com.wizardpager.wizard.ui.StepPagerStrip;

import java.util.Locale;

public class AccountWizard extends WizardDialogFragment {
    private final AbstractWizardModel mWizardModel = new AccountWizardModel(getActivity());

    public static AccountWizard newInstance(Account record) {
        AccountWizard frag = new AccountWizard();

        if (record != null) {
            final Bundle bundle = new Bundle();

            final Bundle bdl1 = new Bundle();
            bdl1.putInt("id", record.id);
            bdl1.putString("name", record.name);
            bdl1.putString("balance", record.balance);
            bdl1.putString("time", record.time);
            bdl1.putString("date", record.date);
            bundle.putBundle("Account Info", bdl1);

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
        final Bundle bundleInfo = mWizardModel.findByKey("Account Info").getData();
        final Locale locale = getResources().getConfiguration().locale;

        String balance = "";

        //Check to see if balance is a number
        boolean validValue;
        try {
            Money accountBalance = new Money(bundleInfo.getString(AccountWizardInfoPage.BALANCE_DATA_KEY).trim());
            balance = accountBalance.getBigDecimal(locale) + "";
            validValue = true;
        } catch (Exception e) {
            validValue = false;
            Toast.makeText(getActivity(), "Please enter a valid balance", Toast.LENGTH_SHORT).show();
        }

        if (validValue) {
            getDialog().cancel();

            if (getArguments() != null) {
                ContentValues accountValues = new ContentValues();
                accountValues.put(DatabaseHelper.ACCOUNT_ID, bundleInfo.getInt(AccountWizardInfoPage.ID_DATA_KEY));
                accountValues.put(DatabaseHelper.ACCOUNT_NAME, bundleInfo.getString(AccountWizardInfoPage.NAME_DATA_KEY));
                accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, balance);
                accountValues.put(DatabaseHelper.ACCOUNT_TIME, bundleInfo.getString(AccountWizardInfoPage.TIME_DATA_KEY));
                accountValues.put(DatabaseHelper.ACCOUNT_DATE, bundleInfo.getString(AccountWizardInfoPage.DATE_DATA_KEY));

                getActivity().getContentResolver().update(Uri.parse(MyContentProvider.ACCOUNTS_URI + "/" + bundleInfo.getInt(AccountWizardInfoPage.ID_DATA_KEY)), accountValues, DatabaseHelper.ACCOUNT_ID + "=" + bundleInfo.getInt(AccountWizardInfoPage.ID_DATA_KEY), null);
            } else {

                //Variables for adding Starting Balance transaction
                final String transactionName = "STARTING BALANCE";
                final int transactionPlanId = -1;
                String transactionValue = balance;
                final String transactionCategory = "STARTING BALANCE";
                final String transactionCheckNum = "None";
                final String transactionMemo = "This is an automatically generated transaction created when you add an account";
                final String transactionTime = bundleInfo.getString(AccountWizardInfoPage.TIME_DATA_KEY);
                final String transactionDate = bundleInfo.getString(AccountWizardInfoPage.DATE_DATA_KEY);
                final String transactionCleared = "true";
                String transactionType = "Unknown";

                try {
                    if (Float.parseFloat(transactionValue) > 0) {
                        transactionType = Constants.DEPOSIT;
                    } else {
                        transactionType = Constants.WITHDRAW;
                        transactionValue = (Float.parseFloat(transactionValue) * -1) + "";
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error\nWas balance a valid format?", Toast.LENGTH_SHORT).show();
                }

                ContentValues accountValues = new ContentValues();
                accountValues.put(DatabaseHelper.ACCOUNT_NAME, bundleInfo.getString(AccountWizardInfoPage.NAME_DATA_KEY));
                accountValues.put(DatabaseHelper.ACCOUNT_BALANCE, balance);
                accountValues.put(DatabaseHelper.ACCOUNT_TIME, bundleInfo.getString(AccountWizardInfoPage.TIME_DATA_KEY));
                accountValues.put(DatabaseHelper.ACCOUNT_DATE, bundleInfo.getString(AccountWizardInfoPage.DATE_DATA_KEY));

                Uri u = getActivity().getContentResolver().insert(MyContentProvider.ACCOUNTS_URI, accountValues);

                ContentValues transactionValues = new ContentValues();
                transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, Long.parseLong(u.getLastPathSegment()));
                transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, transactionPlanId);
                transactionValues.put(DatabaseHelper.TRANS_NAME, transactionName);
                transactionValues.put(DatabaseHelper.TRANS_VALUE, transactionValue);
                transactionValues.put(DatabaseHelper.TRANS_TYPE, transactionType);
                transactionValues.put(DatabaseHelper.TRANS_CATEGORY, transactionCategory);
                transactionValues.put(DatabaseHelper.TRANS_CHECKNUM, transactionCheckNum);
                transactionValues.put(DatabaseHelper.TRANS_MEMO, transactionMemo);
                transactionValues.put(DatabaseHelper.TRANS_TIME, transactionTime);
                transactionValues.put(DatabaseHelper.TRANS_DATE, transactionDate);
                transactionValues.put(DatabaseHelper.TRANS_CLEARED, transactionCleared);

                //Insert values into accounts table
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

class AccountWizardModel extends AbstractWizardModel {
    public AccountWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(

                new AccountWizardInfoPage(this, "Account Info")
                        .setRequired(true)
        );
    }
}