package com.databases.example.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.databases.example.R;
import com.databases.example.app.Categories;
import com.databases.example.data.CategoryRecord;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.MyContentProvider;

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
        String itemID = "0";
        CategoryRecord catRecord;

        if (!this.getArguments().isEmpty()) {
            isCategory = false;
            int groupPos = getArguments().getInt("group");
            int childPos = getArguments().getInt("child");
            catRecord = Categories.adapterCategory.getCategory(groupPos);
            itemID = catRecord.id;
            //Log.e("categoryAdd", "itemID: " + catRecord.id);
        }

        final boolean isCat = isCategory;
        final String catID = itemID;

        LayoutInflater li = LayoutInflater.from(this.getActivity());
        final View categoryAddView = li.inflate(R.layout.category_add, null);

        final EditText editName = (EditText) categoryAddView.findViewById(R.id.EditCategoryName);
        final EditText editNote = (EditText) categoryAddView.findViewById(R.id.EditCategoryNote);

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
                                getActivity().getContentResolver().insert(MyContentProvider.CATEGORIES_URI, categoryValues);
                                getActivity().getSupportLoaderManager().restartLoader(Categories.CATEGORIES_LOADER, null, (Categories) getActivity());
                            }
                            //Add a subcategory
                            else {
                                ContentValues subcategoryValues = new ContentValues();
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, catID);
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, name);
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, note);
                                getActivity().getContentResolver().insert(MyContentProvider.SUBCATEGORIES_URI, subcategoryValues);
                                ((Categories) getActivity()).subcategoryPopulate(catID);

                            }

                        } catch (Exception e) {
                            Log.e("Categories-AddDialog", "Error adding Categories. e = " + e);
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
