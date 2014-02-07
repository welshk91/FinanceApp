/* Class that handles the Card Notification View seen in the Home Screen
 * Sets up the app and displays the cards notifying the user of important events
 */

package com.databases.example;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;

public class Cards extends SherlockFragment {
	private Drawer mDrawerLayout;
	private CardUI mCardView;

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
		mCardView = (CardUI) myFragmentView.findViewById(R.id.cardsview);
		mCardView.setSwipeable(true);

		dealCardsCheckbook(mCardView);
		dealCardsPlans(mCardView);
		//dealCardsStatistics(mCardView);

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

		Cursor accountCursor = getActivity().getContentResolver().query(MyContentProvider.ACCOUNTS_URI, null, null, null, null);
		Cursor transactionCursor = getActivity().getContentResolver().query(MyContentProvider.TRANSACTIONS_URI, null, null, null, null);

		CardTaskAccounts taskAccount = new CardTaskAccounts();
		taskAccount.execute(accountCursor);

		CardTaskTransactions taskTransaction = new CardTaskTransactions();
		taskTransaction.execute(transactionCursor);
	}

	public void dealCardsPlans(CardUI view){

		Cursor planCursor = getActivity().getContentResolver().query(MyContentProvider.PLANNED_TRANSACTIONS_URI, null, null, null, null);

		CardTaskPlans runner = new CardTaskPlans();
		runner.execute(planCursor);
	}

	public void dealCardsStatistics(CardUI view){

		//CardTask runner = new CardTask();
		//runner.execute("Statistics",view);

	}

	private class CardTaskAccounts extends AsyncTask<Object,Void, ArrayList<Card>> {

		@Override
		protected ArrayList<Card> doInBackground(Object... params) {
			Cursor cursor = (Cursor)params[0];
			ArrayList<Card> cards = new ArrayList<Card>();

			int entry_id;
			String entry_name;
			String entry_balance;
			String entry_time;
			String entry_date;

			while (cursor.moveToNext() && !isCancelled()) {
				String title = "";
				String description = "";
				String color = "";

				entry_id = cursor.getInt(0);
				entry_name = cursor.getString(1);
				entry_balance = cursor.getString(2);

				//Determine if Account health is good or not
				if(Float.parseFloat(entry_balance)>=0){
					title=entry_name;
					description="This account is doing well.";
					color="#4ac925";
				}
				else if(Float.parseFloat(entry_balance)<0){
					title=entry_name;
					description="This account is overdrawn.\nYou might want to review the total balance";
					color = "#e00707";
				}

				if(title.length()>0){
					cards.add(new MyPlayCard(title,description,color, "#222222", false, false));
				}

			}//end while


			return cards;
		}

		@Override
		protected void onPostExecute(ArrayList<Card> result) {
			CardStack stackCheckbook = new CardStack();
			stackCheckbook.setTitle("CHECKBOOK");
			mCardView.addStack(stackCheckbook);
			int count = 0;
			
			for (Card item : result) {
				if(count==0){
					mCardView.addCard(item);
				}
				else{
					mCardView.addCardToLastStack(item);
				}
				
				count++;
			}
		}		
	}

	private class CardTaskTransactions extends AsyncTask<Object,Void, ArrayList<Card>> {

		@Override
		protected ArrayList<Card> doInBackground(Object... params) {
			Cursor cursor = (Cursor)params[0];
			ArrayList<Card> cards = new ArrayList<Card>();

			int entry_id;
			String entry_name;
			String entry_value;
			String entry_type = null;
			String entry_category;
			String entry_checknum;
			String entry_memo;
			String entry_time;
			DateTime entry_date;
			String entry_cleared;

			while (cursor.moveToNext() && !isCancelled()) {
				String title = "";
				String description = "";
				String color = "";
				long difference = 0;

				entry_id = cursor.getInt(0);
				entry_name = cursor.getString(3);
				entry_value = cursor.getString(4);
				entry_type = cursor.getString(5);
				entry_time = cursor.getString(9);
				entry_date = new DateTime ();
				entry_date.setStringSQL(cursor.getString(10));
				entry_cleared = cursor.getString(11);

				//Calculate difference of dates
				try {
					Date today_date = new Date();
					difference = (today_date.getTime()- entry_date.getYearMonthDay().getTime())/86400000;
					Log.e("Cards",entry_name + " Difference="+difference);
				} catch (ParseException e) {
					Log.e("Cards", "Error parsing transaction time? e="+e);
					e.printStackTrace();
				}

				//Uncleared transactions
				if(!Boolean.parseBoolean(entry_cleared)){
					title=entry_name;
					description="This transaction has not been cleared.";
					color="#f2a400";
				}

				//Recent transactions within last five days
				if(difference<5 && entry_type.equals("Withdraw")){
					title=entry_name;
					description="This transaction occured recently.";
					color="#f2a400";
				}
				else if(difference<5 && entry_type.equals("Deposit")){
					title=entry_name;
					description="This transaction occured recently.";
					color="#f2a400";
				}

				if(title.length()>0){
					cards.add(new MyPlayCard(title,description,color, "#222222", false, false));
				}

			}//end while

			return cards;
		}

		@Override
		protected void onPostExecute(ArrayList<Card> result) {
			int count = 0;
			
			for (Card item : result) {
				if(count==0){
					mCardView.addCard(item);
				}
				else{
					mCardView.addCardToLastStack(item);
				}
				
				count++;
			}
		}		
	}

	private class CardTaskPlans extends AsyncTask<Object,Void, ArrayList<Card>> {

		@Override
		protected ArrayList<Card> doInBackground(Object... params) {
			Cursor cursor = (Cursor)params[0];

			ArrayList<Card> cards = new ArrayList<Card>();

			String title = "";
			String description = "";
			String color = "";

			int entry_id;
			String entry_name;
			String entry_balance;
			String entry_time;
			String entry_date;

			while (cursor.moveToNext() && !isCancelled()) {
				entry_id = cursor.getInt(0);
				entry_name = cursor.getString(2);
				//entry_balance = cursor.getString(cursor.getColumnIndex("AcctBalance"));
				//entry_time = cursor.getString(cursor.getColumnIndex("AcctTime"));
				//entry_date = cursor.getString(cursor.getColumnIndex("AcctDate"));

				//Recent plans
				if(true){
					title=entry_name;
					description="This planned transaction occured recently";
					color="#33b6ea";
				}

				if(title.length()>0){
					cards.add(new MyPlayCard(title,description,color, "#222222", false, false));
				}

			}//end while

			return cards;
		}

		@Override
		protected void onPostExecute(ArrayList<Card> result) {
			CardStack stackPlans = new CardStack();
			stackPlans.setTitle("PLANS");
			mCardView.addStack(stackPlans);
			int count = 0;
			
			for (Card item : result) {
				if(count==0){
					mCardView.addCard(item);
				}
				else{
					mCardView.addCardToLastStack(item);
				}
				
				count++;
			}
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

}// end Cards