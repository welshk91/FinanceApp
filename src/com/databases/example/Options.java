package com.databases.example;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class Options extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.options);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		checkDefaults();

		Preference customPref = (Preference) findPreference("pref_reset");
		customPref
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				prefsReset();
				return true;
			}

		});

	}

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Toast.makeText(this, "Options Just Changed: ViewDB.Java", Toast.LENGTH_SHORT).show();
		checkDefaults();
	}

	//Set visibility of options depending on whether user wants to use defaults
	public void checkDefaults(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Options.this);
		boolean defaultOn = prefs.getBoolean("checkbox_default", true);
		//Toast.makeText(this, "Default: " + defaultOn, Toast.LENGTH_SHORT).show();
		if(defaultOn){
			//Code Here
			try{
				PreferenceCategory aCategory = (PreferenceCategory)findPreference("category_appearance");
				PreferenceScreen tScreen = (PreferenceScreen)findPreference("pref_screen_transactions");
				PreferenceScreen aScreen = (PreferenceScreen)findPreference("pref_screen_accounts");
				aCategory.setSelectable(false);
				aCategory.setEnabled(false);
				tScreen.setEnabled(false);
				tScreen.setSelectable(false);
				aScreen.setEnabled(false);
				aScreen.setSelectable(false);
			}
			catch(Exception e){
				Toast.makeText(Options.this, "ERROR PREFERENCES\n" + e.toString(), Toast.LENGTH_LONG).show();
			}
		}
		else{
			//Code Here
			try{
				PreferenceCategory aCategory = (PreferenceCategory)findPreference("category_appearance");
				PreferenceScreen tScreen = (PreferenceScreen)findPreference("pref_screen_transactions");
				PreferenceScreen aScreen = (PreferenceScreen)findPreference("pref_screen_accounts");
				aCategory.setSelectable(true);
				aCategory.setEnabled(true);
				tScreen.setEnabled(true);
				tScreen.setSelectable(true);
				aScreen.setEnabled(true);
				aScreen.setSelectable(true);
			}
			catch(Exception e){
				Toast.makeText(Options.this, "ERROR PREFERENCES\n" + e.toString(), Toast.LENGTH_LONG).show();
			}
		}

	}//end Check Defaults

	public void prefsReset(){
		//Set an alert dialog to confirm
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Reset Preferences?");

		// set dialog message
		alertDialogBuilder
		.setMessage("Do you wish to reset all the preferences?")
		.setCancelable(false)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				//Reset Preferences
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Options.this);
				prefs.edit().clear().commit();
				prefs.edit().putBoolean("checkbox_default", true).commit();
			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

}




