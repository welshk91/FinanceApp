/* Class that handles the NavigationDrawer when a user swipes from the right or 
 * clicks the icon in the ActionBar
 */

package com.databases.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;

//An Object Class used to handle the NavigationDrawer
public class Drawer extends Activity{
	private Context context;
	private DrawerLayout mDrawerLayout;
	private ScrollView mDrawerView;

	private Button drawerHome;
	private Button drawerCheckbook;
	private Button drawerCategories;
	private Button drawerSchedule;
	private Button drawerStats;
	private Button drawerOptions;
	private Button drawerHelp;
	private Button drawerExit;

	public Drawer(Context context, DrawerLayout layout, ScrollView drawer) {
		this.context=context;
		this.mDrawerLayout=layout;
		this.mDrawerView=drawer;		

		drawerHome = (Button)drawer.findViewById(R.id.slidingmenu_home);
		drawerHome.setOnClickListener(myListener);
		drawerCheckbook = (Button)drawer.findViewById(R.id.slidingmenu_checkbook);
		drawerCheckbook.setOnClickListener(myListener);
		drawerCategories = (Button)drawer.findViewById(R.id.slidingmenu_categories);
		drawerCategories.setOnClickListener(myListener);
		drawerSchedule = (Button)drawer.findViewById(R.id.slidingmenu_plans);
		drawerSchedule.setOnClickListener(myListener);
		drawerStats = (Button)drawer.findViewById(R.id.slidingmenu_statistics);
		drawerStats.setOnClickListener(myListener);
		drawerOptions = (Button)drawer.findViewById(R.id.slidingmenu_options);
		drawerOptions.setOnClickListener(myListener);
		drawerHelp = (Button)drawer.findViewById(R.id.slidingmenu_help);
		drawerHelp.setOnClickListener(myListener);
		drawerExit = (Button)drawer.findViewById(R.id.slidingmenu_exit);
		drawerExit.setOnClickListener(myListener);
	}//end constructor

	//Method handling 'mouse-click'
	public OnClickListener myListener = new OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.slidingmenu_home:
				Log.d("SliderMenu", "Home Listener Fired");
				Drawer.this.toggle();
				Intent intentHome = new Intent(context, Main.class);
				intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intentHome);
				break;	

			case R.id.slidingmenu_checkbook:
				Log.d("SliderMenu", "Checkbook Listener Fired");
				Drawer.this.toggle();
				Intent intentCheckbook = new Intent(context, Checkbook.class);
				intentCheckbook.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intentCheckbook);
				break;

			case R.id.slidingmenu_categories:
				Log.d("SliderMenu", "Categories Listener Fired");
				Drawer.this.toggle();
				Intent intentCategories = new Intent(context, Categories.class);
				intentCategories.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intentCategories);				
				break;

			case R.id.slidingmenu_plans:
				Log.d("SliderMenu", "Plans Listener Fired");
				Drawer.this.toggle();
				Intent intentPlans = new Intent(context, Plans.class);
				intentPlans.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intentPlans);
				break;

			case R.id.slidingmenu_statistics:
				Log.d("SliderMenu", "Statistics Listener Fired");
				Drawer.this.toggle();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();
				break;

			case R.id.slidingmenu_options:
				Log.d("SliderMenu", "Options Listener Fired");
				Drawer.this.toggle();
				Intent intentOptions = new Intent(context, Options.class);
				context.startActivity(intentOptions);
				break;

			case R.id.slidingmenu_help:
				Log.d("SliderMenu", "Help Listener Fired");
				Drawer.this.toggle();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();
				break;

			case R.id.slidingmenu_exit:
				Log.d("SliderMenu", "Exit Listener Fired");
				Drawer.this.toggle();
				closeApp();
				break;

			default:
				Log.d("SliderMenu", "Default Listener Fired");
				break;
			}

		}// end onClick


	};// end onClickListener

	//Method to exit app
	private void closeApp() {
		System.exit(0);
	}

	//Close/Open drawer
	protected void toggle() {
		if (mDrawerLayout.isDrawerOpen(mDrawerView)) {
			mDrawerLayout.closeDrawer(mDrawerView);
		} else {
			mDrawerLayout.openDrawer(mDrawerView);
		}
	}

}//end class