/* Class that handles the Card Notification View seen in the Home Screen
 * Sets up the app and displays the cards notifying the user of important events
 */

package com.databases.example;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;

public class Cards extends SherlockFragment {
	private Drawer mDrawerLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getLoaderManager();

		//setHasOptionsMenu(true);

		setRetainInstance(false);
	}// end onCreate

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View myFragmentView = inflater.inflate(R.layout.cards, null, false);

		//Initialize Card View
		CardUI mCardView = (CardUI) myFragmentView.findViewById(R.id.cardsview);
		mCardView.setSwipeable(true);

		dealCardsCheckbook(mCardView);
		dealCardsPlans(mCardView);
		dealCardsStatistics(mCardView);


		//Draw cards
		mCardView.refresh();

		return myFragmentView;
	}

	//For Menu
	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		super.onCreateOptionsMenu(menu);
	//		MenuInflater inflater = getSupportMenuInflater();
	//		inflater.inflate(R.layout.main_menu, menu);
	//		return true;
	//	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mDrawerLayout.toggle();
			break;

		case R.id.main_menu_search:    
			//onSearchRequested();
			break;

		}
		return true;
	}

	//Override method to send the search extra data, letting it know which class called it
	//	@Override
	//	public boolean onSearchRequested() {
	//		Bundle appData = new Bundle();
	//		startSearch(null, false, appData, false);
	//		return true;
	//	}


	public void dealCardsCheckbook(CardUI view){
		CardStack stackCheckbook = new CardStack();
		stackCheckbook.setTitle("CHECKBOOK");
		view.addStack(stackCheckbook);

		view.addCard(new MyPlayCard("Lake Michigan Credit Union",
				"This account is overdrawn.\nYou might want to review the total balance", 
				"#e00707", "#222222", false, false));

		view.addCardToLastStack(new MyPlayCard("Cash",
				"This account is doing well.\nPerhaps you should deposit some money into Lake Michigan Credit Union",
				"#4ac925", "#222222", false, false));

		view.addCard(new MyPlayCard("Rent",
				"This transaction occured recently",
				"#f2a400", "#222222", false, false));

		view.addCardToLastStack(new MyPlayCard("IOU",
				"This transaction occured recently",
				"#f2a400", "#222222", false, false));

	}

	public void dealCardsPlans(CardUI view){
		CardStack stackPlans = new CardStack();
		stackPlans.setTitle("PLANS");
		view.addStack(stackPlans);

		view.addCard(new MyPlayCard("Paycheck",
				"This planned transaction occured recently",
				"#33b6ea", "#222222", false, false));

		view.addCardToLastStack(new MyPlayCard("Gas Bill",
				"This planned transaction occured recently",
				"#f2a400", "#222222", false, false));
	}

	public void dealCardsStatistics(CardUI view){
		CardStack stackStatistics = new CardStack();
		stackStatistics.setTitle("STATISTICS");
		view.addStack(stackStatistics);

		view.addCard(new MyPlayCard("Lake Michigan Credit Union",
				"You are significantly over your monthly budget for this account.\nThis may be due to a new transaction \"Car-New Tires\" ",
				"#e00707", "#222222", false, false));

		view.addCardToLastStack(new MyPlayCard("Lake Michigan Credit Union",
				"You are significantly over your monthly budget for this account.\nThis may be due to a new transaction \"House-New Roof\" ",
				"#e00707", "#222222", false, false));

		view.addCardToLastStack(new MyPlayCard("Cash",
				"You are making more money than usual for this account",
				"#4ac925", "#222222", false, false));
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
}// end Cards