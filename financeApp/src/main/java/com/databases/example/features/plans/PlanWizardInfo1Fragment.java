package com.databases.example.features.plans;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.databases.example.R;
import com.databases.example.database.DatabaseHelper;
import com.databases.example.utils.Constants;
import com.wizardpager.wizard.ui.PageFragmentCallbacks;

public class PlanWizardInfo1Fragment extends Fragment {
    private static final String ARG_KEY = "plan_info1_key";

    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private PlanWizardInfo1Page mPage;
    private TextInputEditText mNameView;
    private TextInputEditText mValueView;
    private Spinner mTypeView;
    private Spinner mCategoryView;

    public static PlanWizardInfo1Fragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        PlanWizardInfo1Fragment fragment = new PlanWizardInfo1Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PlanWizardInfo1Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = (PlanWizardInfo1Page) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.plan_page_info1, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        mNameView = ((TextInputEditText) rootView.findViewById(R.id.transaction_name));
        mNameView.setText(mPage.getData().getString(PlanWizardInfo1Page.NAME_DATA_KEY));

        mValueView = ((TextInputEditText) rootView.findViewById(R.id.transaction_value));
        mValueView.setText(mPage.getData().getString(PlanWizardInfo1Page.VALUE_DATA_KEY));

        mTypeView = (Spinner) rootView.findViewById(R.id.spinner_transaction_type);
        if (mPage.getData().getString(PlanWizardInfo1Page.TYPE_DATA_KEY) == null || mPage.getData().getString(PlanWizardInfo1Page.TYPE_DATA_KEY).equals(Constants.WITHDRAW)) {
            mTypeView.setSelection(0);
        } else {
            mTypeView.setSelection(1);
        }

        mCategoryView = (Spinner) rootView.findViewById(R.id.spinner_transaction_category);
        mCategoryView.setAdapter(PlansActivity.categorySpinnerAdapter);

        String category = mPage.getData().getString(PlanWizardInfo1Page.CATEGORY_DATA_KEY);
        final int count = PlansActivity.categorySpinnerAdapter.getCount();
        String catName;
        Cursor cursor;

        if (category != null) {
            for (int i = 0; i < count; i++) {
                cursor = (Cursor) mCategoryView.getItemAtPosition(i);
                catName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME));
                if (catName.contentEquals(category)) {
                    mCategoryView.setSelection(i);
                    break;
                }
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
                mPage.getData().putString(PlanWizardInfo1Page.NAME_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mValueView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                          int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(PlanWizardInfo1Page.VALUE_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                mPage.getData().putString(PlanWizardInfo1Page.TYPE_DATA_KEY, item.toString());
                mPage.notifyDataChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mCategoryView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Cursor cursor = (Cursor) PlansActivity.categorySpinnerAdapter.getItem(pos);
                String category = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME));

                mPage.getData().putString(PlanWizardInfo1Page.CATEGORY_DATA_KEY, category);
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
        if (mNameView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}