package com.databases.example.features.plans;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.databases.example.R;
import com.databases.example.features.checkbook.transactions.TransactionWizardOptionalPage;
import com.wizardpager.wizard.ui.PageFragmentCallbacks;

public class PlanWizardOptionalFragment extends Fragment {
    private static final String ARG_KEY = "plan_optional_key";

    private PageFragmentCallbacks mCallbacks;
    public static PlanWizardOptionalPage mPage;
    private TextInputEditText mMemoView;
    private CheckBox mClearedView;

    public static PlanWizardOptionalFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        PlanWizardOptionalFragment fragment = new PlanWizardOptionalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PlanWizardOptionalFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String mKey = args.getString(ARG_KEY);
        mPage = (PlanWizardOptionalPage) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Bundle data = mPage.getData();

        View rootView = inflater.inflate(R.layout.plan_page_optional, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        mMemoView = ((TextInputEditText) rootView.findViewById(R.id.transaction_memo));
        mMemoView.setText(data.getString(TransactionWizardOptionalPage.MEMO_DATA_KEY));

//        //Adapter for memo's autocomplete
//        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_dropdown_item_1line, PlansActivity.dropdownResults);
//        mMemoView.setAdapter(dropdownAdapter);
//
//        //Add dictionary back to autocomplete
//        TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.NONE);
//        mMemoView.setKeyListener(input);

        mClearedView = (CheckBox) rootView.findViewById(R.id.transaction_cleared);
        if (mPage.getData().getString(TransactionWizardOptionalPage.CLEARED_DATA_KEY) != null) {
            mClearedView.setChecked(Boolean.parseBoolean(mPage.getData().getString(PlanWizardOptionalPage.CLEARED_DATA_KEY)));
        } else {
            mClearedView.setChecked(true);
            mPage.getData().putString(PlanWizardOptionalPage.CLEARED_DATA_KEY, "true");
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
                mPage.getData().putString(PlanWizardOptionalPage.MEMO_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mClearedView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (mClearedView.isChecked()) {
                    mPage.getData().putString(PlanWizardOptionalPage.CLEARED_DATA_KEY, "true");
                } else {
                    mPage.getData().putString(PlanWizardOptionalPage.CLEARED_DATA_KEY, "false");
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
        if (mMemoView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}