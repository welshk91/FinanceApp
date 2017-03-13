package com.databases.example.features.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.app.BackupActivity;
import com.databases.example.database.DatabaseUtils;
import com.databases.example.features.home.MainActivity;

import haibison.android.lockpattern.LockPatternActivity;
import haibison.android.lockpattern.utils.AlpSettings;
import timber.log.Timber;

/**
 * Created by kwelsh on 3/9/17.
 * <p>
 * Inspiration From <a href="http://stackoverflow.com/a/27422401/2128921">
 * StackOverflow</a>
 */
public class SettingsFragment extends PreferenceFragment {
    private static final int REQUEST_CREATE_PATTERN = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActivity().setTheme(R.style...);
//        ((SettingsActivity)getActivity()).bindPreferenceSummaryToValue(
//                getPreferenceScreen().findPreference(getResources().getString(R.string.pref_key_account_default_appearance)));

        if (getArguments() != null) {
            String page = getArguments().getString("page");
            if (page != null)
                switch (page) {
                    case "appearance":
                        addPreferencesFromResource(R.xml.preference_appearance_accounts);
                        addPreferencesFromResource(R.xml.preference_appearance_transactions);
                        addPreferencesFromResource(R.xml.preference_appearance_plans);
                        addPreferencesFromResource(R.xml.preference_appearance_categories);
                        addPreferencesFromResource(R.xml.preference_appearance_subcategories);

                        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_appearance_accounts, false);
                        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_appearance_transactions, false);
                        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_appearance_plans, false);
                        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_appearance_categories, false);
                        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_appearance_subcategories, false);
                        break;
                    case "behavior":
                        addPreferencesFromResource(R.xml.preference_behavior);

                        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_behavior, false);

                        //Draw Pattern
                        Preference prefDraw = findPreference("pref_setlock");
                        prefDraw
                                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                                    public boolean onPreferenceClick(Preference preference) {
                                        drawPattern();
                                        return true;
                                    }
                                });

                        //Local Backup OptionsActivity
                        Preference prefSD = findPreference("pref_sd");
                        prefSD
                                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                                    public boolean onPreferenceClick(Preference preference) {
                                        sdOptions();
                                        return true;
                                    }

                                });
                        break;
                    case "misc":
                        addPreferencesFromResource(R.xml.preference_misc);

                        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_misc, false);

                        //Reset Preferences
                        Preference prefReset = findPreference(getString(R.string.pref_key_reset));
                        prefReset
                                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                                    public boolean onPreferenceClick(Preference preference) {
                                        prefsReset();
                                        return true;
                                    }

                                });

                        //Add Dummy Data
                        Preference prefAddDummyData = findPreference(getString(R.string.pref_key_add_dummy_data));
                        prefAddDummyData
                                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                                    public boolean onPreferenceClick(Preference preference) {
                                        DatabaseUtils.addTestData(getActivity());
                                        return true;
                                    }
                                });

                        //Clear Database
                        Preference prefBackupDB = findPreference(getString(R.string.pref_key_backup_database));
                        prefBackupDB
                                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                                    public boolean onPreferenceClick(Preference preference) {
                                        DatabaseUtils.exportDB(getActivity().getApplicationContext());
                                        return true;
                                    }
                                });

                        //Clear Database
                        Preference prefClearDB = findPreference(getString(R.string.pref_key_clear_database));
                        prefClearDB
                                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                                    public boolean onPreferenceClick(Preference preference) {
                                        clearDB();
                                        return true;
                                    }
                                });
                        break;
                }
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.settings_page, container, false);
        if (layout != null) {
            AppCompatPreferenceActivity activity = (AppCompatPreferenceActivity) getActivity();
            Toolbar toolbar = (Toolbar) layout.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);

            ActionBar bar = activity.getSupportActionBar();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
            //bar.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            bar.setTitle(getPreferenceScreen().getTitle());
        }
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() != null) {
            View frame = (View) getView().getParent();
            if (frame != null)
                frame.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CREATE_PATTERN:
                if (resultCode == getActivity().RESULT_OK) {
                    //String savedPattern = data.getStringExtra(LockPatternActivity._Pattern);
                    //char[] savedPattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);

                    //SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
                    //preferences.edit().putString("myPattern", savedPattern).commit();
                    //preferences.edit().putString("myPattern", String.valueOf(savedPattern)).commit();
                    Toast.makeText(this.getActivity(), "Saved Pattern", Toast.LENGTH_SHORT).show();
                    Timber.d("Saved a lockscreen pattern");
                } else {
                    Toast.makeText(this.getActivity(), "Could not save pattern", Toast.LENGTH_LONG).show();
                    Timber.e("Failed to save a lockscreen pattern");
                }

                break;
        }
    }


    //Draw a lockscreen pattern
    public void drawPattern() {
        AlpSettings.Security.setAutoSavePattern(getActivity(), true);
        Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, getActivity(), LockPatternActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_PATTERN);
    }

    //Launch BackupActivity OptionsActivity screen
    public void sdOptions() {
        Intent intentSD = new Intent(getActivity(), BackupActivity.class);
        startActivity(intentSD);
    }

    //Reset Preferences
    public void prefsReset() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Reset Preferences?");
        alertDialogBuilder
                .setMessage("Do you wish to reset all the preferences?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        prefs.edit().clear().commit();
                        AlpSettings.Security.setPattern(getActivity(), null);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialogReset = alertDialogBuilder.create();
        alertDialogReset.show();
    }

    //Ask if user wants to delete checkbook
    public void clearDB() {
        AlertDialog.Builder builderDelete;
        builderDelete = new AlertDialog.Builder(getActivity());
        builderDelete.setTitle("Delete Your CheckbookActivity?");

        builderDelete.setMessage(
                "Do you want to completely delete the database?\n\nTHIS IS PERMANENT.")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                                DatabaseUtils.deleteDatabase(getActivity());

                                //Navigate User back home
                                Intent intentDashboard = new Intent(getActivity(), MainActivity.class);
                                intentDashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intentDashboard);
                            }
                        }
                )
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                // no action taken
                            }
                        }
                ).show();
    }
}