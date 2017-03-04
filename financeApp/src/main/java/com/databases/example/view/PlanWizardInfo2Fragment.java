package com.databases.example.view;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.databases.example.R;
import com.databases.example.app.Plans;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.PlanWizardInfo2Page;
import com.wizardpager.wizard.ui.PageFragmentCallbacks;

import java.util.Calendar;

public class PlanWizardInfo2Fragment extends Fragment {
    private static final String ARG_KEY = "plan_info2_key";

    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    public static PlanWizardInfo2Page mPage;
    private Spinner mAccountsView;
    private EditText mRateView;
    private Spinner mRateTypeView;

    public static PlanWizardInfo2Fragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        PlanWizardInfo2Fragment fragment = new PlanWizardInfo2Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PlanWizardInfo2Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = (PlanWizardInfo2Page) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.plan_page_info2, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        mRateView = ((EditText) rootView.findViewById(R.id.EditRate));
        mRateView.setText(mPage.getData().getString(PlanWizardInfo2Page.RATE_DATA_KEY));

        mAccountsView = (Spinner) rootView.findViewById(R.id.spinner_transaction_account);
        mAccountsView.setAdapter(Plans.accountSpinnerAdapter);

        Plans.pDate = (Button) rootView.findViewById(R.id.ButtonTransactionDate);
        if (mPage.getData().getString(PlanWizardInfo2Page.DATE_DATA_KEY) != null && mPage.getData().getString(PlanWizardInfo2Page.DATE_DATA_KEY).length() > 0) {
            final DateTime date = new DateTime();
            date.setStringSQL(mPage.getData().getString(PlanWizardInfo2Page.DATE_DATA_KEY));
            Plans.pDate.setText(date.getReadableDate());
            mPage.getData().putString(PlanWizardInfo2Page.DATE_DATA_KEY, date.getReadableDate());
        } else if (mPage.getData().getString(PlanWizardInfo2Page.DATE_DATA_KEY) == null) {
            final Calendar c = Calendar.getInstance();
            final DateTime date = new DateTime();
            date.setCalendar(c);

            Plans.pDate.setText(date.getReadableDate());
            mPage.getData().putString(PlanWizardInfo2Page.DATE_DATA_KEY, date.getReadableDate());
        }

        mRateTypeView = (Spinner) rootView.findViewById(R.id.spinner_rate_type);
        if (mPage.getData().getString(PlanWizardInfo2Page.RATE_TYPE_DATA_KEY) == null || mPage.getData().getString(PlanWizardInfo2Page.RATE_TYPE_DATA_KEY).equals("Days")) {
            mRateTypeView.setSelection(0);
        } else if (mPage.getData().getString(PlanWizardInfo2Page.RATE_TYPE_DATA_KEY).equals("Weeks")) {
            mRateTypeView.setSelection(1);
        } else {
            mRateTypeView.setSelection(2);
        }

        final int accountID = mPage.getData().getInt(PlanWizardInfo2Page.ACCOUNT_ID_DATA_KEY);
        final int count = Plans.accountSpinnerAdapter.getCount();
        int acctID;
        Cursor cursor;

        for (int i = 0; i < count; i++) {
            cursor = (Cursor) mAccountsView.getItemAtPosition(i);
            acctID = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_ID));
            if (acctID == accountID) {
                mAccountsView.setSelection(i);
                break;
            }
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            mCallbacks = (PageFragmentCallbacks) getParentFragment();
        } else {
            mCallbacks = (PageFragmentCallbacks) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRateView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(PlanWizardInfo2Page.RATE_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mRateTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                mPage.getData().putString(PlanWizardInfo2Page.RATE_TYPE_DATA_KEY, item.toString());
                mPage.notifyDataChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mAccountsView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Cursor cursor = (Cursor) Plans.accountSpinnerAdapter.getItem(pos);
                int accountID = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_ID));
                String account = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT_NAME));

                mPage.getData().putInt(PlanWizardInfo2Page.ACCOUNT_ID_DATA_KEY, accountID);
                mPage.getData().putString(PlanWizardInfo2Page.ACCOUNT_DATA_KEY, account);
                mPage.notifyDataChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mRateView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}