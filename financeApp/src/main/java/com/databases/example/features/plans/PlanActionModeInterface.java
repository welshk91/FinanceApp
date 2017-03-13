package com.databases.example.features.plans;

import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.MenuItem;

import com.databases.example.app.BaseActionModeInterface;

/**
 * Created by kwelsh on 3/12/17.
 */

public interface PlanActionModeInterface extends BaseActionModeInterface {
    boolean toggleClicked(ActionMode mode, MenuItem item, SparseBooleanArray selectedIds);
}
