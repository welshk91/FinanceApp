package com.databases.example;

import java.util.List;
import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
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
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;


public class Options extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{

	private static final int REQUEST_CREATE_PATTERN = 0;
	String savedPattern = null;

	//SlidingMenu
	private SliderMenu menu;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Options");
		
		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.preference_appearance);
			addPreferencesFromResource(R.xml.preference_behavior);
			addPreferencesFromResource(R.xml.preference_misc);
			PreferenceManager.setDefaultValues(this, R.xml.preference_appearance, false);
			PreferenceManager.setDefaultValues(this, R.xml.preference_behavior, false);
			PreferenceManager.setDefaultValues(this, R.xml.preference_misc, false);
		}//End if Build<Honeycomb

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);		
	}//end onCreate


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class AppearanceSettingsPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_appearance);
			getActivity().setTitle("Appearance");
		}
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.e("AppearanceSettingsPreferenceFragment-onSharedPreferenceChanged","Here...");
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class BehaviorSettingsPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_behavior);
			getActivity().setTitle("Behavior");

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

		}

		//Draw a lockscreen pattern
		public void drawPattern(){
			Intent intent = new Intent(getActivity(), LockPatternActivity.class);
			intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
			startActivityForResult(intent, REQUEST_CREATE_PATTERN);
		}

		//Launch SD Options screen
		public void sdOptions(){
			Intent intentSD = new Intent(getActivity(), SD.class);
			startActivity(intentSD);
		}

		//Launch Dropbox Options screen
		public void dropboxOptions(){
			Intent intentDropbox = new Intent(getActivity(), Dropbox.class);
			startActivity(intentDropbox);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.e("BehaviorSettingsPreferenceFragment-onSharedPreferenceChanged","Here...");
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class MiscSettingsPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_misc);
			getActivity().setTitle("Misc");

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			prefs.registerOnSharedPreferenceChangeListener(this);

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

		}

		//Reset Preferences
		public void prefsReset(){
			Log.e("prefsReset","clicked!");
			//Set an alert dialog to confirm
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

			// set title
			alertDialogBuilder.setTitle("Reset Preferences?");

			// set dialog message
			alertDialogBuilder
			.setMessage("Do you wish to reset all the preferences?")
			.setCancelable(false)
			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					//Reset Preferences
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
					prefs.edit().clear().commit();
					//getActivity().finish();
					//startActivity(getActivity().getIntent());
				}
			})
			.setNegativeButton("No",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			// create alert dialog
			AlertDialog	alertDialogReset = alertDialogBuilder.create();

			// show it
			alertDialogReset.show();

		}//end of prefsReset

		//Ask if user wants to delete checkbook
		public void clearDB(){
			AlertDialog.Builder builderDelete;
			builderDelete = new AlertDialog.Builder(getActivity());

			// set title
			builderDelete.setTitle("Delete Your Checkbook?");

			builderDelete.setMessage(
					"Do you want to completely delete the database?\n\nTHIS IS PERMANENT.")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0,
								int arg1) {

							Uri uri = Uri.parse(MyContentProvider.DATABASE_URI+"");
							getActivity().getContentResolver().delete(uri, null, null);

							//Navigate User back to dashboard
							Intent intentDashboard = new Intent(getActivity(), Main.class);
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

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.e("MiscSettingsPreferenceFragment-onSharedPreferenceChanged","Here...");
		}

	}

	//Used after a change in settings occurs
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Log.e("Options-onSharedPreferenceChanged","Here...");
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
		//		if(alertDialogReset!=null){
		//			alertDialogReset.dismiss();
		//		}
		super.onPause();
	}

}//end of Options