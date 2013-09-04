package com.databases.example;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class Options extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{

	private static final int REQUEST_CREATE_PATTERN = 0;
	String savedPattern = null;

	//Dialogs to be dismissed
	AlertDialog alertDialogReset;
	AlertDialog.Builder builderDelete;

	//SlidingMenu
	private SliderMenu menu;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setTitle("Options");
		addPreferencesFromResource(R.layout.options);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		checkDefaults();

		//Reset Preferences
		Preference prefReset = (Preference) findPreference("pref_reset");
		prefReset
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				prefsReset();
				return true;
			}

		});

		//Clear Database
		Preference prefClearDB = (Preference) findPreference("pref_clearDB");
		prefClearDB
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				clearDB();
				return true;
			}

		});

		//Draw Pattern
		Preference prefDraw = (Preference) findPreference("pref_setlock");
		prefDraw
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				drawPattern();
				return true;
			}

		});

		//Local Backup Options
		Preference prefSD = (Preference) findPreference("pref_sd");
		prefSD
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				sdOptions();
				return true;
			}

		});

		//Dropbox Options
		Preference prefDropbox = (Preference) findPreference("pref_dropbox");
		prefDropbox
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				dropboxOptions();
				return true;
			}

		});

	}//end onCreate

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
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
				finish();
				startActivity(getIntent());
			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		alertDialogReset = alertDialogBuilder.create();

		// show it
		alertDialogReset.show();

	}//end of prefsReset

	//Ask if user wants to delete checkbook
	public void clearDB(){
		builderDelete = new AlertDialog.Builder(this);

		// set title
		builderDelete.setTitle("Delete Your Checkbook?");

		builderDelete.setMessage(
				"Do you want to completely delete the database?\n\nTHIS IS PERMANENT.")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0,
							int arg1) {

						DatabaseHelper dh = null;
						dh.deleteDatabase();

						//Navigate User back to dashboard
						Intent intentDashboard = new Intent(Options.this, Main.class);
						intentDashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intentDashboard);

					}
				})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// no action taken
					}
				}).show();

	}//end of clearDB

	//Draw a lockscreen pattern
	public void drawPattern(){
		Intent intent = new Intent(this, LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
		startActivityForResult(intent, REQUEST_CREATE_PATTERN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CREATE_PATTERN:
			if (resultCode == RESULT_OK) {
				savedPattern = data.getStringExtra(LockPatternActivity._Pattern);
				SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
				preferences.edit().putString("myPattern", savedPattern).commit();
			}
			break;
		}
	}

	//Launch SD Options screen
	public void sdOptions(){
		Intent intentSD = new Intent(this, SD.class);
		startActivity(intentSD);
	}

	//Launch Dropbox Options screen
	public void dropboxOptions(){
		Intent intentDropbox = new Intent(this, Dropbox.class);
		startActivity(intentDropbox);
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			menu.toggle();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	//Close dialogs to prevent window leaks
	@Override
	public void onPause() {
		if(alertDialogReset!=null){
			alertDialogReset.dismiss();
		}
		super.onPause();
	}

}//end of Options