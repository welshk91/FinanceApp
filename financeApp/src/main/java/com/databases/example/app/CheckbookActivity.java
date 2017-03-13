/* A simple Activity for the Checkbook screen
 * Most of the work seen in the Checkbook screen is actually the fragments,
 * not this class. This class is just a simple parent Activity for the fragments
 */

package com.databases.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.databases.example.R;
import com.databases.example.data.PlanReceiver;
import com.databases.example.fragments.AccountsFragment;
import com.databases.example.fragments.TransactionsFragment;
import com.databases.example.utils.Constants;
import com.databases.example.utils.NotificationUtils;

import timber.log.Timber;

public class CheckbookActivity extends BaseActivity {
    public static final String SHOW_ALL_KEY = "showAll";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.checkbook);
        setTitle(getString(R.string.checkbook));

        if (savedInstanceState != null) {
            Timber.d("SavedState, returning...");
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
            Timber.d("Mode:dual-pane");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.account_frag_frame, account_frag, AccountsFragment.ACCOUNT_FRAG_TAG)
                    .replace(R.id.transaction_frag_frame, transaction_frag, TransactionsFragment.TRANSACTION_FRAG_TAG)
                    .commit();
        } else {
            Timber.d("Mode:single-pane");
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

    @Override
    public Constants.ActivityTag setDrawerTag() {
        return Constants.ActivityTag.CHECKBOOK;
    }
}