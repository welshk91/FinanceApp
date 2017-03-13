package com.databases.example.fragments;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import timber.log.Timber;

/**
 * Created by kwelsh on 3/12/17.
 * Class to handle the ActionMode code
 */

public class AccountActionMode implements ActionMode.Callback {
    private AccountActionModeInterface accountActionModeInterface;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return accountActionModeInterface.onCreateActionMode(mode, menu);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return accountActionModeInterface.onPrepareActionMode(mode, menu);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case AccountsFragment.CONTEXT_MENU_VIEW:
                return accountActionModeInterface.viewClicked(mode, item, accountActionModeInterface.getSelectedIds());
            case AccountsFragment.CONTEXT_MENU_EDIT:
                return accountActionModeInterface.editClicked(mode, item, accountActionModeInterface.getSelectedIds());
            case AccountsFragment.CONTEXT_MENU_DELETE:
                return accountActionModeInterface.deleteClicked(mode, item, accountActionModeInterface.getSelectedIds());

            default:
                mode.finish();
                Timber.e("ERROR. Clicked " + item);
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        accountActionModeInterface.onDestroyActionMode(mode);
    }

    public void setAccountActionModeInterface(AccountActionModeInterface accountActionModeInterface) {
        this.accountActionModeInterface = accountActionModeInterface;
    }

    public AccountActionModeInterface getAccountActionModeInterface() {
        return this.accountActionModeInterface;
    }
}