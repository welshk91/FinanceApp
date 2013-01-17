package com.databases.example;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class Options extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	// this is your preferred flag
	private static final int _ReqCreatePattern = 0;
	String savedPattern = null;

	//SlidingMenu
	private SlidingMenu menu;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Options");
		addPreferencesFromResource(R.layout.options);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		checkDefaults();

		// configure the SlidingMenu
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.sliding_menu);

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
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}//end of prefsReset

	//Ask if user wants to delete checkbook
	public void clearDB(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// set title
		builder.setTitle("Delete Your Checkbook?");

		builder.setMessage(
				"Do you want to completely delete the database?\n\nTHIS IS PERMANENT.")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0,
							int arg1) {
						destroyDatabase();
					}
				})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// no action taken
					}
				}).show();

	}//end of clearDB

	//Method for Deleting Database
	public void destroyDatabase(){

		//Make sure database exist so you don't attempt to delete nothing
		myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);

		//Make sure database is closed before deleting; not sure if necessary
		if (myDB != null){
			myDB.close();
		}

		try{
			this.deleteDatabase(dbFinance);
		}
		catch(Exception e){
			Toast.makeText(this, "Error Deleting Database!!!\n\n" + e, Toast.LENGTH_LONG).show();
		}

		//Navigate User back to dashboard
		Intent intentDashboard = new Intent(Options.this, Main.class);
		intentDashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intentDashboard);

	}//end of destroyDatabase

	//Draw a lockscreen pattern
	public void drawPattern(){
		Intent intent = new Intent(this, LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
		startActivityForResult(intent, _ReqCreatePattern);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case _ReqCreatePattern:
			if (resultCode == RESULT_OK) {
				savedPattern = data.getStringExtra(LockPatternActivity._Pattern);
				SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
				preferences.edit().putString("myPattern", savedPattern).commit();
			}
			break;
		}
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

}//end of Options