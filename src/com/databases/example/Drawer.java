/* Class that handles the NavigationDrawer when a user swipes from the right or 
 * clicks the icon in the ActionBar
 */

package com.databases.example;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;

//An Object Class used to handle the NavigationDrawer
public class Drawer extends Activity{
	private Context context;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	int notificationCount;
	NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
	final int NOTIFICATION_ID = 0123456;

	private String[] drawerItems;

	private static MyAdapter adapterDrawer = null;

	public Drawer(Context context, DrawerLayout layout, ListView drawer) {
		this.context=context;
		this.mDrawerLayout=layout;
		this.mDrawerList=drawer;

		drawerItems = context.getResources().getStringArray(R.array.drawer_items);

		adapterDrawer = new MyAdapter(context, drawerItems);
		mDrawerList.setAdapter(adapterDrawer);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

	}//end constructor

	/* The listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@SuppressLint("NewApi")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			switch (position) {
			case 0:
				Log.d("SliderMenu", "Home Listener Fired");
				Drawer.this.toggle();
				Intent intentHome = new Intent(context, Main.class);
				intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intentHome);
				break;	

			case 1:
				Log.d("SliderMenu", "Checkbook Listener Fired");
				Drawer.this.toggle();
				Intent intentCheckbook = new Intent(context, Checkbook.class);
				intentCheckbook.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intentCheckbook);
				break;

			case 2:
				Log.d("SliderMenu", "Categories Listener Fired");
				Drawer.this.toggle();
				Intent intentCategories = new Intent(context, Categories.class);
				intentCategories.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intentCategories);				
				break;

			case 3:
				Log.d("SliderMenu", "Plans Listener Fired");
				Drawer.this.toggle();
				Intent intentPlans = new Intent(context, Plans.class);
				intentPlans.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intentPlans);
				break;

			case 4:
				Log.d("SliderMenu", "Statistics Listener Fired");
				Drawer.this.toggle();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();
				break;

			case 5:
				Log.d("SliderMenu", "Options Listener Fired");
				Drawer.this.toggle();
				Intent intentOptions = new Intent(context, Options.class);
				context.startActivity(intentOptions);
				break;

			case 6:
				Log.d("SliderMenu", "Help Listener Fired");
				Drawer.this.toggle();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();

				NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				String plan_id = "0";
				String plan_acct_id = "0";
				String plan_name = "plan_name";
				String plan_value = "5000";
				String plan_type = "plan_type";
				String plan_category = "plan_category";
				String plan_memo = "plan_memo";
				String plan_offset = "plan_offset";
				String plan_rate = "plan_rate";
				String plan_cleared = "plan_cleared";

				PendingIntent contentIntent = PendingIntent.getActivity(context, 0,new Intent(context,Checkbook.class), 0);

				//Get today's readable date
				DateTime today = new DateTime();
				today.setCalendar(Calendar.getInstance());

				//Get Value with correct money format
				Money value = new Money(plan_value);
				value.getNumberFormat(context.getResources().getConfiguration().locale);

				NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(context);	
				mBuilder.setContentTitle("Plan " + plan_name + " Occured");
				mBuilder.setContentText(value.getNumberFormat(context.getResources().getConfiguration().locale) + " " + today.getReadableDate());
				//mBuilder.setTicker("New Message Alert!");
				mBuilder.setSmallIcon(R.drawable.ic_launcher);
				mBuilder.setContentIntent(contentIntent);
				mBuilder.setAutoCancel(true);								
				mBuilder.setNumber(notificationCount++);

				//Inbox Style
				inboxStyle.setBigContentTitle("Plans:");
				//inboxStyle.setSummaryText(" +9 more!");
				inboxStyle.addLine(plan_name + ":" + value.getNumberFormat(context.getResources().getConfiguration().locale) + " " + today.getReadableDate());
				mBuilder.setStyle(inboxStyle);
				
				nm.notify(NOTIFICATION_ID, mBuilder.build());
				break;

			case 7:
				Log.d("SliderMenu", "Exit Listener Fired");
				Drawer.this.toggle();
				closeApp();
				break;

			default:
				Log.e("SliderMenu", "Default Listener Fired");
				break;
			}			

		}
	}

	//Method to exit app
	private void closeApp() {
		System.exit(0);
	}

	//Close/Open drawer
	protected void toggle() {
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			mDrawerLayout.openDrawer(mDrawerList);
		}
	}

	//ArrayAdapter with a custom getView
	public class MyAdapter  extends ArrayAdapter<String> {
		Context context;
		private String[] names;

		public MyAdapter(Context context, String[] drawerItems) {
			super(context, R.layout.drawer_item, drawerItems);
			this.context = context;
			this.names = drawerItems;
		}

		@Override
		public int getViewTypeCount(){
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return position % 2;
		}

		@Override
		public View getView(int position, View coverView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.drawer_item, parent, false);

			TextView itemName = (TextView)rowView.findViewById(R.id.drawer_item_name);
			itemName.setText(names[position]);			

			switch (position) {
			case 0:
				itemName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_home, 0, 0, 0);
				break;	

			case 1:
				itemName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_archive, 0, 0, 0);
				break;

			case 2:
				itemName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_sort_by_size, 0, 0, 0);
				break;

			case 3:
				itemName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_today, 0, 0, 0);
				break;

			case 4:
				itemName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_agenda, 0, 0, 0);
				break;

			case 5:
				itemName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_preferences, 0, 0, 0);
				break;

			case 6:
				itemName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_help, 0, 0, 0);
				break;

			case 7:
				itemName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
				break;

			default:
				Log.e("SliderMenu", "Default Listener Fired");
				break;
			}			

			return rowView;
		}

	}	

}//end class