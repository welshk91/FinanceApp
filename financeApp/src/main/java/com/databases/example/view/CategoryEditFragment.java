package com.databases.example.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.databases.example.R;
import com.databases.example.app.Categories;
import com.databases.example.data.CategoryRecord;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.MyContentProvider;
import com.databases.example.data.SubCategoryRecord;

public class CategoryEditFragment extends DialogFragment {
    public static CategoryEditFragment newInstance(int gPos, int cPos, int t) {
        CategoryEditFragment frag = new CategoryEditFragment();
        Bundle args = new Bundle();
        args.putInt("group", gPos);
        args.putInt("child", cPos);
        args.putInt("type", t);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(this.getActivity());
        final View categoryEditView = li.inflate(R.layout.category_add, null);

        final int type = getArguments().getInt("type");
        final int groupPos = getArguments().getInt("group");
        final int childPos = getArguments().getInt("child");

        SubCategoryRecord subrecord;
        CategoryRecord record;

        final EditText editName = (EditText) categoryEditView.findViewById(R.id.EditCategoryName);
        final EditText editNote = (EditText) categoryEditView.findViewById(R.id.EditCategoryNote);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            subrecord = Categories.adapterCategory.getSubCategory(groupPos, childPos);
            alertDialogBuilder.setTitle("Editing " + subrecord.name);
            editName.setText(subrecord.name);
            editNote.setText(subrecord.note);
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            record = Categories.adapterCategory.getCategory(groupPos);
            alertDialogBuilder.setTitle("Editing " + record.name);
            editName.setText(record.name);
            editNote.setText(record.note);
        }

        alertDialogBuilder.setView(categoryEditView);
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newName = editName.getText().toString().trim();
                        String newNote = editNote.getText().toString().trim();

                        try {
                            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                                SubCategoryRecord oldRecord = Categories.adapterCategory.getSubCategory(groupPos, childPos);

                                ContentValues subcategoryValues = new ContentValues();
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_ID, oldRecord.id);
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_CAT_ID, oldRecord.catId);
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NAME, newName);
                                subcategoryValues.put(DatabaseHelper.SUBCATEGORY_NOTE, newNote);
                                getActivity().getContentResolver().update(Uri.parse(MyContentProvider.SUBCATEGORIES_URI + "/" + oldRecord.id), subcategoryValues, DatabaseHelper.SUBCATEGORY_ID + " = " + oldRecord.id, null);
                                ((Categories) getActivity()).subcategoryPopulate(oldRecord.id);
                            } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                                CategoryRecord oldRecord = Categories.adapterCategory.getCategory(groupPos);

                                ContentValues categoryValues = new ContentValues();
                                categoryValues.put(DatabaseHelper.CATEGORY_ID, oldRecord.id);
                                categoryValues.put(DatabaseHelper.CATEGORY_NAME, newName);
                                categoryValues.put(DatabaseHelper.CATEGORY_NOTE, newNote);
                                getActivity().getContentResolver().update(Uri.parse(MyContentProvider.CATEGORIES_URI + "/" + oldRecord.id), categoryValues, DatabaseHelper.CATEGORY_ID + " = " + oldRecord.id, null);
                                getActivity().getSupportLoaderManager().restartLoader(Categories.CATEGORIES_LOADER, null, (Categories) getActivity());
                            }
                        } catch (Exception e) {
                            Log.e("Categories-EditDialog", "Error editing Categories");
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
