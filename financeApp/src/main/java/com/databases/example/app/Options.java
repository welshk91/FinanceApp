/* Class that handles the options in the options screen
 * Handles options for Appearance, Behavior, Misc
 */

package com.databases.example.app;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.data.MyContentProvider;

import java.util.List;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import group.pals.android.lib.ui.lockpattern.util.Settings;

public class Options extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final int REQUEST_CREATE_PATTERN = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Options");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_appearance);
            addPreferencesFromResource(R.xml.preference_behavior);
            addPreferencesFromResource(R.xml.preference_misc);
            PreferenceManager.setDefaultValues(this, R.xml.preference_appearance, false);
            PreferenceManager.setDefaultValues(this, R.xml.preference_behavior, false);
            PreferenceManager.setDefaultValues(this, R.xml.preference_misc, false);
        }//End if Build<Honeycomb

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }//end onCreate

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AppearanceSettingsPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_appearance);
            getActivity().setTitle("Appearance");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Log.e("AppearanceSettingsPreferenceFragment-onSharedPreferenceChanged","Here...");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BehaviorSettingsPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_behavior);
            getActivity().setTitle("Behavior");

            //Draw Pattern
            Preference prefDraw = findPreference("pref_setlock");
            prefDraw
                    .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        public boolean onPreferenceClick(Preference preference) {
                            drawPattern();
                            return true;
                        }
                    });

            //Local Backup Options
            Preference prefSD = findPreference("pref_sd");
            prefSD
                    .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        public boolean onPreferenceClick(Preference preference) {
                            sdOptions();
                            return true;
                        }

                    });

            //Dropbox Options
            Preference prefDropbox = findPreference("pref_dropbox");
            prefDropbox
                    .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        public boolean onPreferenceClick(Preference preference) {
                            dropboxOptions();
                            return true;
                        }

                    });

        }

        //Draw a lockscreen pattern
        public void drawPattern() {
            //			Intent intent = new Intent(getActivity(), LockPatternActivity.class);
            //			intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
            //			startActivityForResult(intent, REQUEST_CREATE_PATTERN);

            Settings.Security.setAutoSavePattern(getActivity(), true);
            Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, getActivity(), LockPatternActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_PATTERN);
        }

        //Launch SD Options screen
        public void sdOptions() {
            Intent intentSD = new Intent(getActivity(), SD.class);
            startActivity(intentSD);
        }

        //Launch Dropbox Options screen
        public void dropboxOptions() {
            Intent intentDropbox = new Intent(getActivity(), Dropbox.class);
            startActivity(intentDropbox);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Log.e("BehaviorSettingsPreferenceFragment-onSharedPreferenceChanged","Here...");
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQUEST_CREATE_PATTERN:
                    if (resultCode == RESULT_OK) {
                        //String savedPattern = data.getStringExtra(LockPatternActivity._Pattern);
                        //char[] savedPattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);

                        //SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
                        //preferences.edit().putString("myPattern", savedPattern).commit();
                        //preferences.edit().putString("myPattern", String.valueOf(savedPattern)).commit();
                        Toast.makeText(this.getActivity(), "Saved Pattern", Toast.LENGTH_SHORT).show();
                        Log.d("onActivityResult", "Saved a lockscreen pattern");
                    } else {
                        Toast.makeText(this.getActivity(), "Could not save pattern", Toast.LENGTH_LONG).show();
                        Log.e("onActivityResult", "Failed to save a lockscreen pattern");
                    }
                    break;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MiscSettingsPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_misc);
            getActivity().setTitle("Misc");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.registerOnSharedPreferenceChangeListener(this);

            //Reset Preferences
            Preference prefReset = findPreference("pref_reset");
            prefReset
                    .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        public boolean onPreferenceClick(Preference preference) {
                            prefsReset();
                            return true;
                        }

                    });

            //Clear Database
            Preference prefClearDB = findPreference("pref_clearDB");
            prefClearDB
                    .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        public boolean onPreferenceClick(Preference preference) {
                            clearDB();
                            return true;
                        }
                    });

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
                            Settings.Security.setPattern(getActivity(), null);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialogReset = alertDialogBuilder.create();
            alertDialogReset.show();

        }//end of prefsReset

        //Ask if user wants to delete checkbook
        public void clearDB() {
            AlertDialog.Builder builderDelete;
            builderDelete = new AlertDialog.Builder(getActivity());
            builderDelete.setTitle("Delete Your Checkbook?");

            builderDelete.setMessage(
                    "Do you want to completely delete the database?\n\nTHIS IS PERMANENT.")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {

                                    Uri uri = Uri.parse(MyContentProvider.DATABASE_URI + "");
                                    getActivity().getContentResolver().delete(uri, null, null);

                                    //Navigate User back home
                                    Intent intentDashboard = new Intent(getActivity(), Main.class);
                                    intentDashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

            //DialogFragment newFragment = DeleteDialogFragment.newInstance();
            //newFragment.show(this.getActivity().getFragmentManager(), "dialogDelete");
            //newFragment.show(this.getActivity().getSupportFragmentManager(), "dialogDelete");

        }//end of clearDB

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Log.e("MiscSettingsPreferenceFragment-onSharedPreferenceChanged","Here...");
        }

    }

    //Used after a change in settings occurs
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        //Log.d("Options-onSharedPreferenceChanged","Settings changed");
    }

    //For Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //Fixes theme problems on devices lower than Honeycomb
    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        if (preference != null)
            if (preference instanceof PreferenceScreen)
                if (((PreferenceScreen) preference).getDialog() != null)
                    ((PreferenceScreen) preference).getDialog().getWindow().getDecorView().setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
        return false;
    }

    //checks to see if fragment is allowed to be attached
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    //Class that handles delete dialog
    //	public static class DeleteDialogFragment extends SherlockDialogFragment {
    //
    //		public static DeleteDialogFragment newInstance() {
    //			DeleteDialogFragment frag = new DeleteDialogFragment();
    //			Bundle args = new Bundle();
    //			frag.setArguments(args);
    //			return frag;
    //		}
    //
    //		@Override
    //		public Dialog onCreateDialog(Bundle savedInstanceState) {
    //			//LayoutInflater li = LayoutInflater.from(this.getSherlockActivity());
    //			//View optionsDeleteView = li.inflate(R.layout.d, null);
    //
    //			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getSherlockActivity());
    //
    //			//alertDialogBuilder.setView(optionsDeleteView);
    //			alertDialogBuilder.setTitle("Delete Your Checkbook?");
    //			alertDialogBuilder.setCancelable(true);
    //
    //			alertDialogBuilder.setMessage(
    //					"Do you want to completely delete the database?\n\nTHIS IS PERMANENT.")
    //					.setCancelable(false)
    //					.setPositiveButton("Yes",
    //							new DialogInterface.OnClickListener() {
    //						public void onClick(DialogInterface arg0,
    //								int arg1) {
    //
    //							Uri uri = Uri.parse(MyContentProvider.DATABASE_URI+"");
    //							getActivity().getContentResolver().delete(uri, null, null);
    //
    //							//Navigate User back home
    //							Intent intentDashboard = new Intent(getActivity(), Main.class);
    //							intentDashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //							startActivity(intentDashboard);
    //						}
    //					})
    //					.setNegativeButton("No",
    //							new DialogInterface.OnClickListener() {
    //						public void onClick(DialogInterface arg0, int arg1) {
    //							// no action taken
    //						}
    //					});
    //
    //			return alertDialogBuilder.create();
    //		}
    //	}

}//end of Options