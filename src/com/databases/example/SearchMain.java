package com.databases.example;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

/*
 * NOTE TO MYSELF
 * Adding "android:launchMode="singleTop"" for this activity makes it only be able
 * to search once. Multiple attempts make the fragments not launch.
 * Possible explanation: 
 * http://stackoverflow.com/questions/6611504/android-fragment-lifecycle-of-single-instance-activity
 */

/*
 * NOTE TO MYSELF
 * Look at Dictionary example for reference (Virtual Tables)
 */

public class SearchMain extends SherlockFragmentActivity {

	//Used in searching to id the last activity
	//private String SEARCH_CONTEXT = "SearchTime.java";
	private String query;

	//Variables for the Database
	public final String tblAccounts = "tblAccounts";
	final String tblTrans = "tblTrans";
	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;

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
		Toast.makeText(this, "SearchTime Query: " + query, Toast.LENGTH_SHORT).show();

		ViewPager mViewPager = (ViewPager)findViewById(R.id.search_pager);
		mViewPager.setOffscreenPageLimit(1);

		MyPagerAdapter mTabsAdapter = new MyPagerAdapter(this, mViewPager);
		
		mTabsAdapter.addTab(SearchAccounts.class, null);
		mTabsAdapter.addTab(SearchTransactions.class, null);
		mTabsAdapter.notifyDataSetChanged();
		
		//Toast.makeText(this, "Added tabs...", Toast.LENGTH_SHORT).show();
	}

	//Override method to send the search extra data, letting it know which class called it
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		startSearch(null, false, appData, false);
		return true;
	}

	public static class MyPagerAdapter extends FragmentPagerAdapter
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
			return mTabs.size();
			//return 2;
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

	}//end mypageadapter

	@Override
	public void onDestroy(){
		//Toast.makeText(this, "Destroying...", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

}//end SearchTime