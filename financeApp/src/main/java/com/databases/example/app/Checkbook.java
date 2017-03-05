/* A simple Activity for the Checkbook screen
 * Most of the work seen in the Checkbook screen is actually the fragments,
 * not this class. This class is just a simple parent Activity for the fragments
 */

package com.databases.example.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.databases.example.R;
import com.databases.example.data.PlanReceiver;
import com.databases.example.utils.NotificationUtils;
import com.databases.example.view.Drawer;

public class Checkbook extends AppCompatActivity {
    private Drawer drawer;

    public static final String SHOW_ALL_KEY = "showAll";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.checkbook);
        setTitle(getString(R.string.checkbook));

        //NavigationDrawer
        drawer = new Drawer(this);

        if (savedInstanceState != null) {
            Log.e("Checkbook", "SavedState");
            return;
        }

        //The transaction frame, if null it means we can't see transactions in this particular view
        View checkbook_frame = findViewById(R.id.checkbook_frag_frame);

        //Clear notifications
        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();

            if (b.getBoolean(PlanReceiver.FROM_NOTIFICATION_KEY)) {
                NotificationUtils.clearNotifications(this);
            }
        }

        AccountsFragment account_frag = new AccountsFragment();
        TransactionsFragment transaction_frag = new TransactionsFragment();

        //Bundle for Transaction fragment
        Bundle argsTran = new Bundle();
        argsTran.putBoolean(SHOW_ALL_KEY, true);
        argsTran.putBoolean(SearchActivity.BOOLEAN_SEARCH_KEY, false);

        //Bundle for Account fragment
        Bundle argsAccount = new Bundle();
        argsAccount.putBoolean(SearchActivity.BOOLEAN_SEARCH_KEY, false);

        transaction_frag.setArguments(argsTran);
        account_frag.setArguments(argsAccount);

        if (checkbook_frame == null) {
            Log.d("Checkbook-onCreate", "Mode:dualpane");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.account_frag_frame, account_frag, AccountsFragment.ACCOUNT_FRAG_TAG)
                    .replace(R.id.transaction_frag_frame, transaction_frag, TransactionsFragment.TRANSACTION_FRAG_TAG)
                    .commit();
        } else {
            Log.d("Checkbook-onCreate", "Mode:singlepane");
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.checkbook_frag_frame, account_frag, AccountsFragment.ACCOUNT_FRAG_TAG).commit();
        }

        getSupportFragmentManager().executePendingTransactions();

    }

    //Needed to have notification extras work
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();

            if (b.getBoolean(PlanReceiver.FROM_NOTIFICATION_KEY)) {
                NotificationUtils.clearNotifications(this);
            }
        }

    }

    //For Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.toggle();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawer.getDrawerToggle().syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawer.getDrawerToggle().onConfigurationChanged(newConfig);
    }

}//end Checkbook