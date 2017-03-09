/* Class that handles the NavigationDrawer when a user swipes from the right or 
 * clicks the icon in the ActionBar
 */

package com.databases.example.app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.databases.example.R;

import timber.log.Timber;

//An Object Class used to handle the NavigationDrawer
public class DrawerActivity extends AppCompatActivity {
    private final DrawerLayout drawerLayout;
    private final NavigationView drawerNavView;

    public DrawerActivity(final Context context) {
        Toolbar toolbar = (Toolbar) ((AppCompatActivity) context).findViewById(R.id.toolbar);
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
//        ((AppCompatActivity) context).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ((AppCompatActivity) context).getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout = (DrawerLayout) ((AppCompatActivity) context).findViewById(R.id.drawer_layout);
        drawerNavView = (NavigationView) ((AppCompatActivity) context).findViewById(R.id.drawer);
        drawerNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.home:
                        Timber.v("Home Listener Fired");
                        Intent intentHome = new Intent(context, MainActivity.class);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intentHome);
                        return true;

                    case R.id.checkbook:
                        Timber.v("CheckbookActivity Listener Fired");
                        Intent intentCheckbook = new Intent(context, CheckbookActivity.class);
                        intentCheckbook.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intentCheckbook);
                        return true;

                    case R.id.categories:
                        Timber.v("CategoriesActivity Listener Fired");
                        Intent intentCategories = new Intent(context, CategoriesActivity.class);
                        intentCategories.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intentCategories);
                        return true;

                    case R.id.plans:
                        Timber.v("PlansActivity Listener Fired");
                        Intent intentPlans = new Intent(context, PlansActivity.class);
                        intentPlans.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intentPlans);
                        return true;

                    case R.id.statistics:
                        Timber.v("Statistics Listener Fired");
                        //	Intent intentStats = new Intent(MainActivity.this, AccountsFragment.class);
                        //	startActivity(intentStats);
                        //drawPattern();
                        return true;

                    case R.id.options:
                        Timber.v("OptionsActivity Listener Fired");
                        Intent intentOptions = new Intent(context, SettingsActivity.class);
                        context.startActivity(intentOptions);
                        return true;

                    case R.id.help:
                        Timber.v("Help Listener Fired");
                        return true;

                    case R.id.exit:
                        Timber.v("Exit Listener Fired");
                        closeApp();
                        return true;

                    default:
                        Timber.e("Default Listener Fired");
                        return true;
                }
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle((AppCompatActivity) context, drawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_closed) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    //Method to exit app
    private void closeApp() {
        System.exit(0);
    }
}