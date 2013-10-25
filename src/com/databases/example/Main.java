package com.databases.example;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;

public class Main extends SherlockActivity {	
	//Flag used for lockscreen
	private static final int LOCKSCREEN_SIGNIN = 1;

	//NavigationDrawer
	private Drawer mDrawerLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//For Clear preferences!!!!!! REMOVE EVENTUALLY
		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		//Editor editor = settings.edit();
		//editor.clear();
		//editor.commit();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
		boolean lockEnabled = prefs.getBoolean("checkbox_lock_enabled", false);

		if(lockEnabled){
			confirmPattern();
		}

		setContentView(R.layout.main);
		
		//Card View
		CardUI mCardView = (CardUI) findViewById(R.id.cardsview);
		mCardView.setSwipeable(true);

		CardStack stackCheckbook = new CardStack();
		stackCheckbook.setTitle("CHECKBOOK");
		mCardView.addStack(stackCheckbook);

		mCardView.addCard(new MyPlayCard("Lake Michigan Credit Union",
				"This account is overdrawn.\nYou might want to review the total balance", 
				"#e00707", "#222222", false, false));

		mCardView.addCardToLastStack(new MyPlayCard("Cash",
				"This account is doing well.\nPerhaps you should deposit some money into Lake Michigan Credit Union",
				"#4ac925", "#222222", false, false));

		mCardView.addCard(new MyPlayCard("Rent",
				"This transaction occured recently",
				"#f2a400", "#222222", false, false));

		mCardView.addCardToLastStack(new MyPlayCard("IOU",
				"This transaction occured recently",
				"#f2a400", "#222222", false, false));

		CardStack stackPlans = new CardStack();
		stackPlans.setTitle("PLANS");
		mCardView.addStack(stackPlans);

		mCardView.addCard(new MyPlayCard("Paycheck",
				"This planned transaction occured recently", 
				"#33b6ea", "#222222", false, false));

		mCardView.addCardToLastStack(new MyPlayCard("Gas Bill",
				"This planned transaction occured recently",
				"#f2a400", "#222222", false, false));

		CardStack stackStatistics = new CardStack();
		stackStatistics.setTitle("STATISTICS");
		mCardView.addStack(stackStatistics);

		mCardView.addCard(new MyPlayCard("Lake Michigan Credit Union",
				"You are significantly over your monthly budget for this account.\nThis may be due to a new transaction \"Car-New Tires\" ", 
				"#e00707", "#222222", false, false));

		mCardView.addCardToLastStack(new MyPlayCard("Lake Michigan Credit Union",
				"You are significantly over your monthly budget for this account.\nThis may be due to a new transaction \"House-New Roof\" ",
				"#e00707", "#222222", false, false));

		mCardView.addCardToLastStack(new MyPlayCard("Cash",
				"You are making more money than usual for this account",
				"#4ac925", "#222222", false, false));

		// draw cards
		mCardView.refresh();
		
		//NavigationDrawer
		DrawerLayout view = (DrawerLayout) findViewById(R.id.drawer_layout);
		ScrollView drawer = (ScrollView) findViewById(R.id.drawer);
		mDrawerLayout = new Drawer(this,view,drawer);

	}// end onCreate

	//For Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);
		return true;
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mDrawerLayout.toggle();
			break;

		case R.id.main_menu_search:    
			onSearchRequested();
			break;

		}
		return true;
	}

	//Override method to send the search extra data, letting it know which class called it
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		startSearch(null, false, appData, false);
		return true;
	}

	//Confirm Lockscreen
	public void confirmPattern(){
		Intent intent = new Intent(Main.this, LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.ComparePattern);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
		String savedPattern = prefs.getString("myPattern", null);

		if(savedPattern!=null){
			intent.putExtra(LockPatternActivity._Pattern, savedPattern);
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
			if (resultCode == RESULT_OK) {
				// signing in ok
				Toast.makeText(Main.this, "Sign In\nAccepted", Toast.LENGTH_SHORT).show();
			} else {
				// signing in failed
				Toast.makeText(Main.this, "Sign In\nFailed", Toast.LENGTH_SHORT).show();
				this.finish();
				this.moveTaskToBack(true);
				super.onDestroy();
			}
			break;

		}
	}

	//MyCard Class
	public class MyCard extends Card {

		public MyCard(String title, String desc){
			super(title, desc);			
		}

		@Override
		public View getCardContent(Context context) {
			View v = LayoutInflater.from(context).inflate(R.layout.card_ex, null);
			((TextView) v.findViewById(R.id.title)).setText(title);
			((TextView) v.findViewById(R.id.description)).setText(desc);
			return v;
		}

	}//End of MyCard Class

	//MyImageCard Class
	public class MyImageCard extends Card {

		public MyImageCard(String title, int image){
			super(title, image);
		}

		@Override
		public View getCardContent(Context context) {
			View v = LayoutInflater.from(context).inflate(R.layout.card_picture, null);

			((TextView) v.findViewById(R.id.title)).setText(title);
			((ImageView) v.findViewById(R.id.imageView1)).setImageResource(image);

			return v;
		}	

	}//End of MyImageCard


	//MyPlayCard Class
	public class MyPlayCard extends Card {

		public MyPlayCard(String titlePlay, String description, String color,
				String titleColor, Boolean hasOverflow, Boolean isClickable) {
			super(titlePlay, description, color, titleColor, hasOverflow,
					isClickable);
		}

		@Override
		public View getCardContent(Context context) {
			View v = LayoutInflater.from(context).inflate(R.layout.card_play, null);

			((TextView) v.findViewById(R.id.title)).setText(titlePlay);
			((TextView) v.findViewById(R.id.title)).setTextColor(Color
					.parseColor(titleColor));
			((TextView) v.findViewById(R.id.description)).setText(description);
			((ImageView) v.findViewById(R.id.stripe)).setBackgroundColor(Color
					.parseColor(color));

			if (isClickable == true)
				((LinearLayout) v.findViewById(R.id.contentLayout))
				.setBackgroundResource(R.drawable.selectable_background_cardbank);

			if (hasOverflow == true)
				((ImageView) v.findViewById(R.id.overflow))
				.setVisibility(View.VISIBLE);
			else
				((ImageView) v.findViewById(R.id.overflow))
				.setVisibility(View.GONE);

			return v;
		}
	}//End of MyPlayCard Class	

}// end Main