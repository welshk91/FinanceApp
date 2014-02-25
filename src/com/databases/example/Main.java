/* Class that handles the Card Notification View seen in the Home Screen
 * Sets up the app and displays the cards notifying the user of important events
 */

package com.databases.example;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import group.pals.android.lib.ui.lockpattern.util.Settings;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.widget.SearchView;

public class Main extends SherlockFragmentActivity {
	private static final int LOCKSCREEN_SIGNIN = 1;
	private Drawer drawer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
		boolean lockEnabled = prefs.getBoolean("checkbox_lock_enabled", false);

		if(lockEnabled){
			confirmPattern();
		}

		setContentView(R.layout.main);

		//Add Cards Fragments
		Cards cards_frag = new Cards();

		getSupportFragmentManager().beginTransaction()
		.replace(R.id.card_frame, cards_frag,"cards_tag").commit();
		getSupportFragmentManager().executePendingTransactions();		

		//NavigationDrawer
		drawer = new Drawer(this);
						
	}// end onCreate

	//For Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);
		SearchWidget searchWidget = new SearchWidget(this,menu.findItem(R.id.main_menu_search).getActionView());
		return true;
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			drawer.toggle();
			break;
		}
		return true;
	}

	//Confirm Lockscreen
	public void confirmPattern(){
		if(Settings.Security.getPattern(this)!=null){
			//Log.d("Main", "valueOf getPattern="+String.valueOf(Settings.Security.getPattern(this)));
			//Log.d("Main", "getPattern="+String.valueOf(Settings.Security.getPattern(this)));

			Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null, Main.this, LockPatternActivity.class);
			//Intent intentForget = new Intent(this, LoginHelper.class);
			//intent.putExtra(LockPatternActivity.EXTRA_INTENT_ACTIVITY_FORGOT_PATTERN, intentForget);
			startActivityForResult(intent, LOCKSCREEN_SIGNIN);
		}
		else{
			Toast.makeText(Main.this, "Cannot Use Lockscreen\nNo Pattern Set Yet", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case LOCKSCREEN_SIGNIN:
			switch (resultCode) {
			case RESULT_OK:
				Toast.makeText(Main.this, "Sign In\nAccepted", Toast.LENGTH_SHORT).show();
				break;
			case RESULT_CANCELED:
				Toast.makeText(Main.this, "Sign In\nCanceled", Toast.LENGTH_SHORT).show();
				this.finish();
				this.moveTaskToBack(true);
				super.onDestroy();
				break;
			case LockPatternActivity.RESULT_FAILED:
				Toast.makeText(Main.this, "Sign In\nFailed", Toast.LENGTH_SHORT).show();
				this.finish();
				this.moveTaskToBack(true);
				super.onDestroy();
				break;
			case LockPatternActivity.RESULT_FORGOT_PATTERN:
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
				boolean lockEnabled = prefs.getBoolean("checkbox_lock_enabled", false);				

				if(!lockEnabled){
					Toast.makeText(Main.this, "Sign In\nReset", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(Main.this, "Sign In\nForgotten", Toast.LENGTH_SHORT).show();					
				}

				break;
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawer.getDrawerToggle().syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawer.getDrawerToggle().onConfigurationChanged(newConfig);
	}
	
}// end Main