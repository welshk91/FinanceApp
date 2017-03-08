package com.databases.example.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.model.Category;
import com.databases.example.model.Subcategory;

import java.util.ArrayList;

import timber.log.Timber;

public class CategoriesListViewAdapter extends BaseExpandableListAdapter {
    private Cursor category;
    private final ArrayList<Cursor> subcategory;
    private final Context context;

    public CategoriesListViewAdapter(Context context, int textViewResourceId, Cursor cats, ArrayList<Cursor> subcats) {
        this.context = context;
        this.subcategory = subcats;
    }

    //My method for getting a Category Record at a certain position
    public Category getCategory(long id) {
        Cursor group = category;

        ArrayList<Category> categories = Category.getCategories(group);
        categories.size();

        group.moveToPosition((int) id);
        int isDefaultColumn = group.getColumnIndex(DatabaseHelper.CATEGORY_IS_DEFAULT);
        int nameColumn = group.getColumnIndex(DatabaseHelper.CATEGORY_NAME);
        int noteColumn = group.getColumnIndex(DatabaseHelper.CATEGORY_NOTE);

        int itemId = group.getInt(0);
        boolean itemIsDefault = Boolean.parseBoolean(group.getString(isDefaultColumn));
        String itemName = group.getString(nameColumn);
        String itemNote = group.getString(noteColumn);

        return new Category(itemId, itemIsDefault, itemName, itemNote);
    }

    //My method for getting a Category Record at a certain position
    public Subcategory getSubCategory(int groupId, int childId) {
        Cursor group = subcategory.get(groupId);

        ArrayList<Subcategory> subcategories = Subcategory.getSubcategories(group);
        subcategories.size();

        group.moveToPosition(childId);
        int toIdColumn = group.getColumnIndex(DatabaseHelper.SUBCATEGORY_CAT_ID);
        int isDefaultColumn = group.getColumnIndex(DatabaseHelper.SUBCATEGORY_IS_DEFAULT);
        int nameColumn = group.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME);
        int noteColumn = group.getColumnIndex(DatabaseHelper.SUBCATEGORY_NOTE);

        int itemId = group.getInt(0);
        int itemTo_id = group.getInt(toIdColumn);
        boolean itemIsDefault = Boolean.parseBoolean(group.getString(isDefaultColumn));
        String itemSubname = group.getString(nameColumn);
        String itemNote = group.getString(noteColumn);

        return new Subcategory(itemId, itemTo_id, itemIsDefault, itemSubname, itemNote);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = convertView;
        Cursor user = category;

        CategoryViewHolder viewHolder;

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_category_default_appearance), true);

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.category_item, null);

            viewHolder = new CategoryViewHolder();
            viewHolder.tvName = (TextView) v.findViewById(R.id.category_name);
            viewHolder.tvNote = (TextView) v.findViewById(R.id.category_note);
            viewHolder.tvIsDefault = (TextView) v.findViewById(R.id.category_is_default);

            v.setTag(viewHolder);
        } else {
            viewHolder = (CategoryViewHolder) v.getTag();
        }

        //Change Background Colors
        try {
            if (!useDefaults) {
                LinearLayout l;
                l = (LinearLayout) v.findViewById(R.id.category_layout);
                int startColor = prefs.getInt(context.getString(R.string.pref_key_category_start_background_color), ContextCompat.getColor(context, R.color.white));
                int endColor = prefs.getInt(context.getString(R.string.pref_key_category_end_background_color), ContextCompat.getColor(context, R.color.white));
                GradientDrawable defaultGradient = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{startColor, endColor});
                l.setBackgroundDrawable(defaultGradient);
            }


        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
        }

        //Change Size/Color of main field
        try {
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_category_name_size), "24");

            if (useDefaults) {
                viewHolder.tvName.setTextSize(24);
            } else {
                viewHolder.tvName.setTextSize(Integer.parseInt(DefaultSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt(context.getString(R.string.pref_key_category_name_color), ContextCompat.getColor(context, R.color.categories_main_category_default));

            if (useDefaults) {
                viewHolder.tvName.setTextColor(ContextCompat.getColor(context, R.color.categories_main_category_default));
            } else {
                viewHolder.tvName.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        int IsDefaultColumn = user.getColumnIndex(DatabaseHelper.CATEGORY_IS_DEFAULT);
        int NameColumn = user.getColumnIndex(DatabaseHelper.CATEGORY_NAME);
        int NoteColumn = user.getColumnIndex(DatabaseHelper.CATEGORY_NOTE);

        user.moveToPosition(groupPosition);
        String itemId = user.getString(0);
        String itemIsDefault = user.getString(IsDefaultColumn);
        String itemName = user.getString(NameColumn);
        String itemNote = user.getString(NoteColumn);
        //Log.d("getGroupView", "Found Category: " + itemName);

        //For User-Defined Field Visibility
        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_category_name_show), true)) {
            viewHolder.tvName.setVisibility(View.VISIBLE);
            viewHolder.tvName.setText(itemName);
        } else {
            viewHolder.tvName.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(context.getString(R.string.pref_key_category_note_show), false) && !useDefaults && itemNote != null) {
            viewHolder.tvNote.setVisibility(View.VISIBLE);
            viewHolder.tvNote.setText(itemNote);
        } else {
            viewHolder.tvNote.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(context.getString(R.string.pref_key_category_is_default_show), false) && !useDefaults && itemIsDefault != null) {
            viewHolder.tvIsDefault.setVisibility(View.VISIBLE);
            viewHolder.tvIsDefault.setText(itemIsDefault);
        } else {
            viewHolder.tvIsDefault.setVisibility(View.GONE);
        }

        return v;
    }//end getView

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Cursor temp = subcategory.get(groupPosition);
        temp.moveToPosition(childPosition);
        String itemId = temp.getString(0);

        //Log.d("getChildID", "returning " + Long.parseLong(itemId));

        return Long.parseLong(itemId);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        //Log.d("Cagtegories","groupPos: " + groupPosition + " childPos: " + childPosition + " isLastChild: " + isLastChild + " parent: " + parent);

        View v = convertView;
        Cursor user = subcategory.get(groupPosition);
        SubCategoryViewHolder viewHolder;

        //For Custom View Properties
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useDefaults = prefs.getBoolean(context.getString(R.string.pref_key_subcategory_default_appearance), true);

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.subcategory_item, null);

            viewHolder = new SubCategoryViewHolder();
            viewHolder.tvName = (TextView) v.findViewById(R.id.subcategory_name);
            viewHolder.tvNote = (TextView) v.findViewById(R.id.subcategory_note);
            viewHolder.tvCategory = (TextView) v.findViewById(R.id.subcategory_parent);
            viewHolder.tvIsDefault = (TextView) v.findViewById(R.id.subcategory_is_default);
            v.setTag(viewHolder);
        } else {
            viewHolder = (SubCategoryViewHolder) v.getTag();
        }

        //Change Background Colors
        try {
            if (!useDefaults) {
                LinearLayout l;
                l = (LinearLayout) v.findViewById(R.id.subcategory_item_layout);
                int startColor = prefs.getInt(context.getString(R.string.pref_key_subcategory_start_background_color), ContextCompat.getColor(context, R.color.white));
                int endColor = prefs.getInt(context.getString(R.string.pref_key_subcategory_end_background_color), ContextCompat.getColor(context, R.color.white));
                GradientDrawable defaultGradient = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{startColor, endColor});
                l.setBackgroundDrawable(defaultGradient);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
        }

        //Change Size/color of main field
        try {
            String DefaultSize = prefs.getString(context.getString(R.string.pref_key_subcategory_name_size), "24");

            if (useDefaults) {
                viewHolder.tvName.setTextSize(24);
            } else {
                viewHolder.tvName.setTextSize(Integer.parseInt(DefaultSize));
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        try {
            int DefaultColor = prefs.getInt(context.getString(R.string.pref_key_subcategory_name_color), ContextCompat.getColor(context, R.color.categories_subcategory_default));

            if (useDefaults) {
                viewHolder.tvName.setTextColor(ContextCompat.getColor(context, R.color.categories_subcategory_default));
            } else {
                viewHolder.tvName.setTextColor(DefaultColor);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
        }

        int ToIDColumn = user.getColumnIndex(DatabaseHelper.SUBCATEGORY_CAT_ID);
        int IsDefaultIDColumn = user.getColumnIndex(DatabaseHelper.SUBCATEGORY_IS_DEFAULT);
        int NameColumn = user.getColumnIndex(DatabaseHelper.SUBCATEGORY_NAME);
        int NoteColumn = user.getColumnIndex(DatabaseHelper.SUBCATEGORY_NOTE);

        user.moveToPosition(childPosition);
        String itemId = user.getString(0);
        String itemTo_id = user.getString(ToIDColumn);
        String itemIsDefault = user.getString(IsDefaultIDColumn);
        String itemSubname = user.getString(NameColumn);
        String itemNote = user.getString(NoteColumn);
        //Log.d("getChildView", "Found SubCategory: " + itemSubname);

        if (useDefaults || prefs.getBoolean(context.getString(R.string.pref_key_subcategory_name_show), true)) {
            viewHolder.tvName.setVisibility(View.VISIBLE);
            viewHolder.tvName.setText(itemSubname);
        } else {
            viewHolder.tvName.setVisibility(View.GONE);
        }
        if (prefs.getBoolean(context.getString(R.string.pref_key_subcategory_note_show), false) && !useDefaults && itemNote != null) {
            viewHolder.tvNote.setVisibility(View.VISIBLE);
            viewHolder.tvNote.setText(itemNote);
        } else {
            viewHolder.tvNote.setVisibility(View.GONE);
        }
        if (prefs.getBoolean(context.getString(R.string.pref_key_subcategory_is_default_show), false) && !useDefaults && itemIsDefault != null) {
            viewHolder.tvIsDefault.setVisibility(View.VISIBLE);
            viewHolder.tvIsDefault.setText(itemIsDefault);
        } else {
            viewHolder.tvIsDefault.setVisibility(View.GONE);
        }

        viewHolder.tvCategory.setVisibility(View.GONE);

        return v;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return subcategory.get(groupPosition).getCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getGroupCount() {
        if (category == null) {
            return 0;
        }

        return category.getCount();
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void swapCategoryCursor(Cursor data) {
        category = data;
        notifyDataSetChanged();
    }

    public void swapSubCategoryCursor(Cursor data) {
        Timber.v("Cursor data=" + data + " data size=" + data.getCount());
        subcategory.add(data);
    }

}

//ViewHolder for CategoriesActivity
class CategoryViewHolder {
    TextView tvName;
    TextView tvNote;
    TextView tvIsDefault;
}

//ViewHolder for SubCategories
class SubCategoryViewHolder {
    TextView tvName;
    TextView tvCategory;
    TextView tvNote;
    TextView tvIsDefault;
}
