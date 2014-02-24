/* A simple Activity for the Search screen
 * Sets up the PageAdapter for the Search screen.
 * Most of the work seen in the Search screen is actually the fragments,
 * not this class. This class is just a simple parent Activity for the fragments.
 */

package com.databases.example;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;

/*
 * NOTE TO MYSELF
 * Look at Dictionary example for reference (Virtual Tables)
 */

public class Search extends SherlockFragmentActivity {
	//The word being searched
	private String query;

	//NavigationDrawer
	private Drawer drawer;	

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		handleIntent(getIntent()); 
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent); 
		handleIntent(intent);
	} 

	private void handleIntent(Intent intent) { 
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
			query = intent.getStringExtra(SearchManager.QUERY);
			setTitle("Search <" + query + ">");
			makeView();
		}
	}    

	//Method that handles setting up the Tabs
	public void makeView(){
		setContentView(R.layout.search);

		ViewPager mViewPager = (ViewPager)findViewById(R.id.search_pager);
		mViewPager.setOffscreenPageLimit(2);

		MyPagerAdapter mTabsAdapter = new MyPagerAdapter(this, mViewPager);

		mTabsAdapter.addTab(Accounts.class, null);
		mTabsAdapter.addTab(Transactions.class, null);
		mTabsAdapter.notifyDataSetChanged();

		//NavigationDrawer
		drawer = new Drawer(this);
	}

	//Override method to send the search extra data, letting it know which class called it
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		startSearch(null, false, appData, false);
		return true;
	}

	public static class MyPagerAdapter extends FragmentStatePagerAdapter
	implements ViewPager.OnPageChangeListener{

		private final Context mContext;
		private final ViewPager mPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(Class<?> _class, Bundle _args) {
				clss = _class;
				args = _args;
			}
		}

		public MyPagerAdapter(FragmentActivity activity, ViewPager pager) {

			super(activity.getSupportFragmentManager());
			mContext = activity;
			mPager = pager;
			mPager.setAdapter(this);
			mPager.setOnPageChangeListener(this);
		}

		public void addTab(Class<?> clss, Bundle args) {
			TabInfo info = new TabInfo(clss, args);
			mTabs.add(info);
			notifyDataSetChanged();
		}

		//Removes tab at certain position (zero-based)
		public void removeTab(ViewPager pager, int position,MyPagerAdapter adapter){
			mTabs.remove(position);
			adapter.notifyDataSetChanged();
		}

		public int getCount() {
			//One for Account tab, One for Transactions tab
			//return 2;
			return mTabs.size();
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public CharSequence getPageTitle(int position){
			switch (position) {
			case 0:
				return "Accounts";

			case 1:
				return "Transactions";

			default:
				return "Unknown Page!";
			}

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(), info.args);
		}

		//Allows for multiple pages
		@Override
		public float getPageWidth(int position){
			//To have two pages, return .5
			return(1f);
		}

	}//end mypageadapter

	@Override
	public void onDestroy(){
		//Toast.makeText(this, "Destroying...", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	//For Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.layout.search_menu, menu);
		return true;
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			drawer.toggle();
			break;

		case R.id.search_menu_search:    
			onSearchRequested();
			break;

		}
		return true;
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
	
}//end SearchMain