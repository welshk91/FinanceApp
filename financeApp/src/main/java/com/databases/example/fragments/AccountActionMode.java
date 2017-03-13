package com.databases.example.fragments;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.databases.example.R;

import timber.log.Timber;

/**
 * Created by kwelsh on 3/12/17.
 * Class to handle the ActionMode code
 */

public class AccountActionMode implements ActionMode.Callback {
    private AccountActionModeInterface accountActionModeInterface;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.add(0, AccountsFragment.CONTEXT_MENU_VIEW, 0, R.string.view).setIcon(android.R.drawable.ic_menu_view);
        menu.add(0, AccountsFragment.CONTEXT_MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
        menu.add(0, AccountsFragment.CONTEXT_MENU_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
        accountActionModeInterface.onCreateActionMode(mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu.clear();
        if (accountActionModeInterface.getSelectedCount() == 1 && mode != null) {
            menu.add(0, AccountsFragment.CONTEXT_MENU_VIEW, 0, R.string.view).setIcon(android.R.drawable.ic_menu_view);
            menu.add(0, AccountsFragment.CONTEXT_MENU_EDIT, 1, R.string.edit).setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, AccountsFragment.CONTEXT_MENU_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
        } else if (accountActionModeInterface.getSelectedCount() > 1) {
            menu.add(0, AccountsFragment.CONTEXT_MENU_DELETE, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
        }

        accountActionModeInterface.onPrepareActionMode(mode, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case AccountsFragment.CONTEXT_MENU_VIEW:
                accountActionModeInterface.viewClicked(mode, item, accountActionModeInterface.getSelectedIds());
                mode.finish();
                return true;
            case AccountsFragment.CONTEXT_MENU_EDIT:
                accountActionModeInterface.editClicked(mode, item, accountActionModeInterface.getSelectedIds());
                mode.finish();
                return true;
            case AccountsFragment.CONTEXT_MENU_DELETE:
                accountActionModeInterface.deleteClicked(mode, item, accountActionModeInterface.getSelectedIds());
                mode.finish();
                return true;

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