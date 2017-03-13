/* Class that handles the NavigationDrawer when a user swipes from the right or 
 * clicks the icon in the ActionBar
 */

package com.databases.example.app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.databases.example.R;
import com.databases.example.utils.Constants.ActivityTag;

import timber.log.Timber;

//An Object Class used to handle the NavigationDrawer
public class DrawerActivity {
    private final AppCompatActivity appCompatActivity;
    private ActivityTag activityTag;
    private final DrawerToggleInterface drawerToggleInterface;

    private DrawerLayout drawerLayout;
    private NavigationView drawerNavView;

    public DrawerActivity(final AppCompatActivity appCompatActivity, final DrawerToggleInterface drawerToggleInterface) {
        this.appCompatActivity = appCompatActivity;
        this.drawerToggleInterface = drawerToggleInterface;
    }

    public void initialize() {
        Toolbar toolbar = (Toolbar) appCompatActivity.findViewById(R.id.toolbar);
        appCompatActivity.setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) appCompatActivity.findViewById(R.id.drawer_layout);
        drawerNavView = (NavigationView) appCompatActivity.findViewById(R.id.drawer);

        drawerNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {

                    case R.id.home:
                        Intent intentHome = new Intent(appCompatActivity, MainActivity.class);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ActivityOptionsCompat optionsHome = ActivityOptionsCompat.makeCustomAnimation(appCompatActivity, R.anim.slide_in_right, R.anim.slide_out_left);
                        ActivityCompat.startActivity(appCompatActivity, intentHome, optionsHome.toBundle());
                        return true;

                    case R.id.checkbook:
                        Intent intentCheckbook = new Intent(appCompatActivity, CheckbookActivity.class);
                        intentCheckbook.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ActivityOptionsCompat optionsCheckbook = ActivityOptionsCompat.makeCustomAnimation(appCompatActivity, R.anim.slide_in_right, R.anim.slide_out_left);
                        ActivityCompat.startActivity(appCompatActivity, intentCheckbook, optionsCheckbook.toBundle());
                        return true;

                    case R.id.categories:
                        Intent intentCategories = new Intent(appCompatActivity, CategoriesActivity.class);
                        intentCategories.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ActivityOptionsCompat optionsCategories = ActivityOptionsCompat.makeCustomAnimation(appCompatActivity, R.anim.slide_in_right, R.anim.slide_out_left);
                        ActivityCompat.startActivity(appCompatActivity, intentCategories, optionsCategories.toBundle());
                        return true;

                    case R.id.plans:
                        Intent intentPlans = new Intent(appCompatActivity, PlansActivity.class);
                        intentPlans.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ActivityOptionsCompat optionsPlans = ActivityOptionsCompat.makeCustomAnimation(appCompatActivity, R.anim.slide_in_right, R.anim.slide_out_left);
                        ActivityCompat.startActivity(appCompatActivity, intentPlans, optionsPlans.toBundle());
                        return true;

                    case R.id.statistics:
                        Timber.v("Statistics Listener Fired");
                        return true;

                    case R.id.options:
                        Intent intentOptions = new Intent(appCompatActivity, SettingsActivity.class);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(appCompatActivity, R.anim.slide_in_right, R.anim.slide_out_left);
                        ActivityCompat.startActivity(appCompatActivity, intentOptions, options.toBundle());
                        return true;

                    case R.id.help:
                        Timber.v("Help Listener Fired");
                        return true;

                    case R.id.exit:
                        closeApp();
                        return true;

                    default:
                        Timber.e("Default Listener Fired");
                        return true;
                }
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(appCompatActivity, drawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_closed) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (drawerToggleInterface != null) {
                    drawerToggleInterface.onDrawerClosed(drawerView);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (drawerToggleInterface != null) {
                    drawerToggleInterface.onDrawerOpened(drawerView);
                }
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    public void setActivityTag(ActivityTag activityTag) {
        this.activityTag = activityTag;

        switch (activityTag) {
            case MAIN:
                drawerNavView.getMenu().getItem(0).setChecked(true);
                break;
            case CHECKBOOK:
                drawerNavView.getMenu().getItem(1).setChecked(true);
                break;
            case CATEGROIES:
                drawerNavView.getMenu().getItem(2).setChecked(true);
                break;
            case PLANS:
                drawerNavView.getMenu().getItem(3).setChecked(true);
                break;
            case STATISTICS:
                drawerNavView.getMenu().getItem(4).setChecked(true);
                break;
            case OPTIONS:
                drawerNavView.getMenu().getItem(5).setChecked(true);
                break;
            case HELP:
                drawerNavView.getMenu().getItem(6).setChecked(true);
                break;
            case EXIT:
                drawerNavView.getMenu().getItem(7).setChecked(true);
                break;
            case SEARCH:
                break;
            case LINKS:
                break;
            default:
                Timber.e("Unknown Drawer Menu Item");
                break;
        }
    }

    public ActivityTag getActivityTag(){
        return activityTag;
    }

    //Method to exit app
    private void closeApp() {
        System.exit(0);
    }
}