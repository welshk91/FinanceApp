package com.databases.example.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.databases.example.R;
import com.databases.example.app.Transactions;
import com.databases.example.data.DateTime;
import com.databases.example.data.TransactionWizardOptionalPage;
import com.wizardpager.wizard.ui.PageFragmentCallbacks;

import java.util.Calendar;

public class TransactionWizardOptionalFragment extends Fragment {
    private static final String ARG_KEY = "transaction_optional_key";

    private PageFragmentCallbacks mCallbacks;
    public static TransactionWizardOptionalPage mPage;
    private TextInputEditText mCheckNumView;
    private TextInputEditText mMemoView;
    private CheckBox mClearedView;

    public static TransactionWizardOptionalFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        TransactionWizardOptionalFragment fragment = new TransactionWizardOptionalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public TransactionWizardOptionalFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String mKey = args.getString(ARG_KEY);
        mPage = (TransactionWizardOptionalPage) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Bundle data = mPage.getData();

        View rootView = inflater.inflate(R.layout.transaction_page_optional, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        mCheckNumView = ((TextInputEditText) rootView.findViewById(R.id.transaction_checknum));
        mCheckNumView.setText(data.getString(TransactionWizardOptionalPage.CHECKNUM_DATA_KEY));

        mMemoView = ((TextInputEditText) rootView.findViewById(R.id.transaction_memo));
        mMemoView.setText(data.getString(TransactionWizardOptionalPage.MEMO_DATA_KEY));

//        //Adapter for memo's autocomplete
//        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, Transactions.dropdownResults);
//        mMemoView.setAdapter(dropdownAdapter);
//
//        //Add dictionary back to autocomplete
//        TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
//        mMemoView.setKeyListener(input);

        Transactions.tTime = (Button) rootView.findViewById(R.id.transaction_time);
        Transactions.tDate = (Button) rootView.findViewById(R.id.transaction_date);

        if (data.getString(TransactionWizardOptionalPage.DATE_DATA_KEY) != null && data.getString(TransactionWizardOptionalPage.DATE_DATA_KEY).length() > 0) {
            final DateTime date = new DateTime();
            date.setStringSQL(data.getString(TransactionWizardOptionalPage.DATE_DATA_KEY));
            Transactions.tDate.setText(date.getReadableDate());
            mPage.getData().putString(TransactionWizardOptionalPage.DATE_DATA_KEY, date.getReadableDate());
        }
        if (data.getString(TransactionWizardOptionalPage.TIME_DATA_KEY) != null && data.getString(TransactionWizardOptionalPage.TIME_DATA_KEY).length() > 0) {
            final DateTime time = new DateTime();
            time.setStringSQL(data.getString(TransactionWizardOptionalPage.TIME_DATA_KEY));
            Transactions.tTime.setText(time.getReadableTime());
            mPage.getData().putString(TransactionWizardOptionalPage.TIME_DATA_KEY, time.getReadableTime());
        } else if (data.getString(TransactionWizardOptionalPage.DATE_DATA_KEY) == null && data.getString(TransactionWizardOptionalPage.TIME_DATA_KEY) == null) {
            final Calendar c = Calendar.getInstance();
            final DateTime date = new DateTime();
            date.setCalendar(c);

            Transactions.tDate.setText(date.getReadableDate());
            Transactions.tTime.setText(date.getReadableTime());
            mPage.getData().putString(TransactionWizardOptionalPage.DATE_DATA_KEY, date.getReadableDate());
            mPage.getData().putString(TransactionWizardOptionalPage.TIME_DATA_KEY, date.getReadableTime());
        }

        mClearedView = (CheckBox) rootView.findViewById(R.id.transaction_cleared);
        if (mPage.getData().getString(TransactionWizardOptionalPage.CLEARED_DATA_KEY) != null) {
            mClearedView.setChecked(Boolean.parseBoolean(mPage.getData().getString(TransactionWizardOptionalPage.CLEARED_DATA_KEY)));
        } else {
            mClearedView.setChecked(true);
            mPage.getData().putString(TransactionWizardOptionalPage.CLEARED_DATA_KEY, "true");
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

        mCheckNumView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(TransactionWizardOptionalPage.CHECKNUM_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mMemoView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(TransactionWizardOptionalPage.MEMO_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mClearedView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (mClearedView.isChecked()) {
                    mPage.getData().putString(TransactionWizardOptionalPage.CLEARED_DATA_KEY, "true");
                } else {
                    mPage.getData().putString(TransactionWizardOptionalPage.CLEARED_DATA_KEY, "false");
                }

                mPage.notifyDataChanged();
            }
        });

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mCheckNumView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}