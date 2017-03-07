package com.databases.example.wizard;

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
import android.widget.TextView;

import com.databases.example.R;
import com.databases.example.utils.DateTime;
import com.wizardpager.wizard.ui.PageFragmentCallbacks;

import java.util.Calendar;
import java.util.Locale;

public class AccountWizardInfoFragment extends Fragment {
    private static final String ARG_KEY = "account_info_key";

    private PageFragmentCallbacks mCallbacks;
    private AccountWizardInfoPage mPage;
    private TextInputEditText mNameView;
    private TextInputEditText mBalanceView;

    public static AccountWizardInfoFragment create(String key) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, key);

        AccountWizardInfoFragment fragment = new AccountWizardInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public AccountWizardInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String mKey = args.getString(ARG_KEY);
        mPage = (AccountWizardInfoPage) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.account_page_info, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        mNameView = ((TextInputEditText) rootView.findViewById(R.id.account_name));
        mNameView.setText(mPage.getData().getString(AccountWizardInfoPage.NAME_DATA_KEY));

        mBalanceView = ((TextInputEditText) rootView.findViewById(R.id.account_balance));
        mBalanceView.setText(mPage.getData().getString(AccountWizardInfoPage.BALANCE_DATA_KEY));
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

        mNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(AccountWizardInfoPage.NAME_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mBalanceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(AccountWizardInfoPage.BALANCE_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        //Get Time & Date
        final Calendar cal = Calendar.getInstance();
        final Locale locale = getResources().getConfiguration().locale;
        DateTime accountDate = new DateTime();
        accountDate.setDate(cal.getTime());

        mPage.getData().putString(AccountWizardInfoPage.DATE_DATA_KEY, accountDate.getSQLDate(locale));
        mPage.getData().putString(AccountWizardInfoPage.TIME_DATA_KEY, accountDate.getSQLTime(locale));
        mPage.notifyDataChanged();

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mNameView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}