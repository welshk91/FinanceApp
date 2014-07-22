package com.databases.example.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.databases.example.R;
import com.databases.example.app.Categories;
import com.databases.example.data.CategoryRecord;
import com.databases.example.data.SubCategoryRecord;

public class CategoryViewFragment extends SherlockDialogFragment {
    public static CategoryViewFragment newInstance(int gPos, int cPos,int t) {
        CategoryViewFragment frag = new CategoryViewFragment();
        Bundle args = new Bundle();
        args.putInt("group", gPos);
        args.putInt("child", cPos);
        args.putInt("type", t);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int type = getArguments().getInt("type");
        final int groupPos = getArguments().getInt("group");
        final int childPos = getArguments().getInt("child");

        final LayoutInflater li = LayoutInflater.from(this.getActivity());

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setCancelable(true);

        if(type== ExpandableListView.PACKED_POSITION_TYPE_CHILD){
            final View categoryStatsView = li.inflate(R.layout.subcategory_item, null);
            alertDialogBuilder.setView(categoryStatsView);

            SubCategoryRecord record = Categories.adapterCategory.getSubCategory(groupPos, childPos);

            //Set Statistics
            TextView statsName = (TextView)categoryStatsView.findViewById(R.id.subcategory_name);
            statsName.setText(record.name);
            TextView statsValue = (TextView)categoryStatsView.findViewById(R.id.subcategory_parent);
            statsValue.setText(record.catId);
            TextView statsDate = (TextView)categoryStatsView.findViewById(R.id.subcategory_note);
            statsDate.setText(record.note);
        }
        else if(type==ExpandableListView.PACKED_POSITION_TYPE_GROUP){
            final View categoryStatsView = li.inflate(R.layout.category_item, null);
            alertDialogBuilder.setView(categoryStatsView);

            CategoryRecord record = Categories.adapterCategory.getCategory(groupPos);

            //Set Statistics
            TextView statsName = (TextView)categoryStatsView.findViewById(R.id.category_name);
            statsName.setText(record.name);
            TextView statsDate = (TextView)categoryStatsView.findViewById(R.id.category_note);
            statsDate.setText(record.note);
        }

        return alertDialogBuilder.create();
    }
}
