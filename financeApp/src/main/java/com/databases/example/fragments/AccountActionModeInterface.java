package com.databases.example.fragments;

import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by kwelsh on 3/12/17.
 */

public interface AccountActionModeInterface {
    boolean onCreateActionMode(ActionMode mode, Menu menu);
    boolean onPrepareActionMode(ActionMode mode, Menu menu);
    void onDestroyActionMode(ActionMode mode);
    boolean viewClicked (ActionMode mode, MenuItem item, SparseBooleanArray selectedIds);
    boolean editClicked (ActionMode mode, MenuItem item, SparseBooleanArray selectedIds);
    boolean deleteClicked (ActionMode mode, MenuItem item, SparseBooleanArray selectedIds);

    SparseBooleanArray getSelectedIds();
    int getSelectedCount();
}
