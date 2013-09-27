package com.databases.example;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

	//SlidingMenu
	private SliderMenu menu;

	/** Called when the activity is first created. */
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

		CardStack stackRegular = new CardStack();
		stackRegular.setTitle("REGULAR CARDS");
		mCardView.addStack(stackRegular);

		// add AndroidViews Cards
		mCardView.addCard(new MyCard("Get the CardsUI view", "get view description"));
		mCardView.addCardToLastStack(new MyCard("for Android at", "Android description"));
		MyCard androidViewsCard = new MyCard("www.androidviews.net", ".net descrpition");
		androidViewsCard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("http://www.androidviews.net/"));
				startActivity(intent);

			}
		});
		androidViewsCard.setOnLongClickListener(new OnLongClickListener() {    		

			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(v.getContext(), "This is a long click", Toast.LENGTH_SHORT).show();
				return true;
			}

		});
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://www.androidviews.net/"));

		mCardView.addCardToLastStack(androidViewsCard);

		CardStack stackPlay = new CardStack();
		stackPlay.setTitle("GOOGLE PLAY CARDS");
		mCardView.addStack(stackPlay);

		// add one card, and then add another one to the last stack.
		mCardView.addCard(new MyCard("Google Play Cards","Description for play cards"));
		mCardView.addCardToLastStack(new MyCard("By Androguide & GadgetCheck", "androguide description"));

		mCardView.addCardToLastStack(new MyPlayCard("Google Play",
				"This card mimics the new Google play cards look", "#33b6ea",
				"#33b6ea", true, false));

		mCardView
		.addCardToLastStack(new MyPlayCard(
				"Menu Overflow",
				"The PlayCards allow you to easily set a menu overflow on your card.\nYou can also declare the left stripe's color in a String, like \"#33B5E5\" for the holo blue color, same for the title color.",
				"#e00707", "#e00707", false, true));

		// add one card
		mCardView
		.addCard(new MyPlayCard(
				"Different Colors for Title & Stripe",
				"You can set any color for the title and any other color for the left stripe",
				"#f2a400", "#9d36d0", false, false));

		mCardView
		.addCardToLastStack(new MyPlayCard(
				"Set Clickable or Not",
				"You can easily implement an onClickListener on any card, but the last boolean parameter of the PlayCards allow you to toggle the clickable background.",
				"#4ac925", "#222222", true, true));

		// draw cards
		mCardView.refresh();		


		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

	}// end onCreate

	//Method handling 'mouse-click'
	public OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {


			/*			case R.id.dashboard_exit:
				Main.this.finish();
				//android.os.Process.killProcess(android.os.Process.myPid());				
				onDestroy();
				//Intent i = new Intent();
				//i.setAction(Intent.ACTION_MAIN);
				//i.addCategory(Intent.CATEGORY_HOME);
				//startActivity(i); 
				finish(); 
				break;	
			 */
			default:
				Log.e("Main", "Default onClickListner fired?");
				break;	
			}

		}// end onClick
	};// end onClickListener


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
			menu.toggle();
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