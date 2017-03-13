package com.databases.example.fragments;

import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.MenuItem;

/**
 * Created by kwelsh on 3/12/17.
 */

public interface PlanActionModeInterface extends BaseActionModeInterface {
    boolean toggleClicked(ActionMode mode, MenuItem item, SparseBooleanArray selectedIds);
}
