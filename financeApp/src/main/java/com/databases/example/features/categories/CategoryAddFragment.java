package com.databases.example.features.categories;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.databases.example.R;
import com.databases.example.database.DatabaseHelper;
import com.databases.example.database.MyContentProvider;

import timber.log.Timber;

public class CategoryAddFragment extends DialogFragment {
    public static CategoryAddFragment newInstance() {
        CategoryAddFragment frag = new CategoryAddFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    public static CategoryAddFragment newInstance(int gPos, int cPos) {
        CategoryAddFragment frag = new CategoryAddFragment();
        Bundle args = new Bundle();
        args.putInt("group", gPos);
        args.putInt("child", cPos);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean isCategory = true;
        int itemID = -1;
        Category catRecord;

        if (!this.getArguments().isEmpty()) {
            isCategory = false;
            int groupPos = getArguments().getInt("group");
            int childPos = getArguments().getInt("child");
            catRecord = ((CategoriesActivity) getActivity()).getAdapterCategory().getCategory(groupPos);
            itemID = catRecord.id;
            //Log.e("categoryAdd", "itemID: " + catRecord.id);
        }

        final boolean isCat = isCategory;
        final int catID = itemID;

        LayoutInflater li = LayoutInflater.from(this.getActivity());
        final View categoryAddView = li.inflate(R.layout.category_add, null);

        final TextInputEditText editName = (TextInputEditText) categoryAddView.findViewById(R.id.edit_category_name);
        final TextInputEditText editNote = (TextInputEditText) categoryAddView.findViewById(R.id.EditCategoryNote);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
        alertDialogBuilder.setView(categoryAddView);

        //Set title
        if (isCat) {
            alertDialogBuilder.setTitle("Create A Category");
        } else {
            alertDialogBuilder.setTitle("Create A SubCategory");
        }

        //Set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String name = editName.getText().toString().trim();
                        String note = editNote.getText().toString().trim();

                        try {
                            //Add a category
                            if (isCat) {
                                ContentValues categoryValues = new ContentValues();
                                categoryValues.put(DatabaseHelper.CATEGORY_NAME, name);
                                categoryValues.put(DatabaseHelper.CATEGORY_NOTE, note);
                                categoryValues.put(DatabaseHelper.CATEGORY_IS_DEFAULT, "false");
                                getActivity().getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);
                                getActivity().getSupportLoaderManager().restartLoader(CategoriesActivity.CATEGORIES_LOADER, null, (CategoriesActivity) getActivity());
                            }
                            //Add a subcategory
                            else {
                                ContentValues subcategoryValues = new ContentValues();
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, catID);
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, name);
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, note);
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_IS_DEFAULT, "false");
                                getActivity().getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);
                                ((CategoriesActivity) getActivity()).subcategoryPopulate(catID);
                            }

                        } catch (Exception e) {
                            Timber.e("Error adding Categories. e = " + e);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return alertDialogBuilder.create();
    }

}
