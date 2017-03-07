/* Class that handles the Card Notification View seen in the Home Screen
 * Sets up the app and displays the cards notifying the user of important events
 */

package com.databases.example.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.fragments.CardsFragment;
import com.databases.example.model.SearchWidget;

import haibison.android.lockpattern.LockPatternActivity;
import haibison.android.lockpattern.utils.AlpSettings;

public class MainActivity extends AppCompatActivity {
    private static final int LOCKSCREEN_SIGNIN = 1;
    private DrawerActivity drawerActivity;

    private final String CARDS_TAG = "cards_tag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean lockEnabled = prefs.getBoolean(getString(R.string.pref_key_lock_enabled), false);

        if (lockEnabled) {
            confirmPattern();
        }

        setContentView(R.layout.main);

        //Add CardsFragment Fragments
        CardsFragment cards_frag = new CardsFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.card_frame, cards_frag, CARDS_TAG).commit();
        getSupportFragmentManager().executePendingTransactions();

        //NavigationDrawer
        drawerActivity = new DrawerActivity(this);

    }

    //For Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        new SearchWidget(this, (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.main_menu_search)));
        return true;
    }

    //For Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerActivity.toggle();
                break;
        }
        return true;
    }

    //Confirm Lockscreen
    private void confirmPattern() {
        if (AlpSettings.Security.getPattern(this) != null) {
            //Log.d("MainActivity", "valueOf getPattern="+String.valueOf(Settings.Security.getPattern(this)));
            //Log.d("MainActivity", "getPattern="+String.valueOf(Settings.Security.getPattern(this)));

            Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null, MainActivity.this, LockPatternActivity.class);
            //Intent intentForget = new Intent(this, PatternRetrievalActivity.class);
            //intent.putExtra(LockPatternActivity.EXTRA_INTENT_ACTIVITY_FORGOT_PATTERN, intentForget);
            startActivityForResult(intent, LOCKSCREEN_SIGNIN);
        } else {
            Toast.makeText(MainActivity.this, "Cannot Use Lockscreen\nNo Pattern Set Yet", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCKSCREEN_SIGNIN:
                switch (resultCode) {
                    case RESULT_OK:
                        Toast.makeText(MainActivity.this, "Sign In\nAccepted", Toast.LENGTH_SHORT).show();
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "Sign In\nCanceled", Toast.LENGTH_SHORT).show();
                        this.finish();
                        this.moveTaskToBack(true);
                        super.onDestroy();
                        break;
                    case LockPatternActivity.RESULT_FAILED:
                        Toast.makeText(MainActivity.this, "Sign In\nFailed", Toast.LENGTH_SHORT).show();
                        this.finish();
                        this.moveTaskToBack(true);
                        super.onDestroy();
                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        boolean lockEnabled = prefs.getBoolean(getString(R.string.pref_key_lock_enabled), false);

                        if (!lockEnabled) {
                            Toast.makeText(MainActivity.this, "Sign In\nReset", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Sign In\nForgotten", Toast.LENGTH_SHORT).show();
                        }

                        break;
                }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerActivity.getDrawerToggle().syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerActivity.getDrawerToggle().onConfigurationChanged(newConfig);
    }

}// end MainActivity