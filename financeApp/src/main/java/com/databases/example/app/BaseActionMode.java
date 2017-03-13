package com.databases.example.app;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.databases.example.features.checkbook.accounts.AccountsFragment;
import com.databases.example.features.checkbook.transactions.TransactionsFragment;
import com.databases.example.features.plans.PlanActionModeInterface;
import com.databases.example.features.plans.PlansActivity;

import timber.log.Timber;

/**
 * Created by kwelsh on 3/12/17.
 * Class to handle the ActionMode code
 */

public class BaseActionMode implements ActionMode.Callback {
    private BaseActionModeInterface baseActionModeInterface;
    private PlanActionModeInterface planActionModeInterface;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return baseActionModeInterface.onCreateActionMode(mode, menu);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return baseActionModeInterface.onPrepareActionMode(mode, menu);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case AccountsFragment.CONTEXT_MENU_VIEW:
                return baseActionModeInterface.viewClicked(mode, item, baseActionModeInterface.getSelectedIds());
            case AccountsFragment.CONTEXT_MENU_EDIT:
                return baseActionModeInterface.editClicked(mode, item, baseActionModeInterface.getSelectedIds());
            case AccountsFragment.CONTEXT_MENU_DELETE:
                return baseActionModeInterface.deleteClicked(mode, item, baseActionModeInterface.getSelectedIds());

            case TransactionsFragment.CONTEXT_MENU_VIEW:
                return baseActionModeInterface.viewClicked(mode, item, baseActionModeInterface.getSelectedIds());
            case TransactionsFragment.CONTEXT_MENU_EDIT:
                return baseActionModeInterface.editClicked(mode, item, baseActionModeInterface.getSelectedIds());
            case TransactionsFragment.CONTEXT_MENU_DELETE:
                return baseActionModeInterface.deleteClicked(mode, item, baseActionModeInterface.getSelectedIds());

            case PlansActivity.ACTION_MODE_VIEW:
                return baseActionModeInterface.viewClicked(mode, item, baseActionModeInterface.getSelectedIds());
            case PlansActivity.ACTION_MODE_EDIT:
                return baseActionModeInterface.editClicked(mode, item, baseActionModeInterface.getSelectedIds());
            case PlansActivity.ACTION_MODE_DELETE:
                return baseActionModeInterface.deleteClicked(mode, item, baseActionModeInterface.getSelectedIds());
            case PlansActivity.ACTION_MODE_TOGGLE:
                return planActionModeInterface.toggleClicked(mode, item, planActionModeInterface.getSelectedIds());

            default:
                mode.finish();
                Timber.e("ERROR. Clicked " + item);
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        baseActionModeInterface.onDestroyActionMode(mode);
    }

    public void setBaseActionModeInterface(BaseActionModeInterface baseActionModeInterface) {
        this.baseActionModeInterface = baseActionModeInterface;
    }

    public BaseActionModeInterface getBaseActionModeInterface() {
        return this.baseActionModeInterface;
    }

    public PlanActionModeInterface getPlanActionModeInterface() {
        return planActionModeInterface;
    }

    public void setPlanActionModeInterface(PlanActionModeInterface planActionModeInterface) {
        this.planActionModeInterface = planActionModeInterface;
    }
}