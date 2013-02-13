package com.databases.example;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.databases.example.Transactions.DatePickerFragment;
import com.databases.example.Transactions.TimePickerFragment;
import com.slidingmenu.lib.SlidingMenu;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class Checkbook extends SherlockFragmentActivity {

	//SlidingMenu
	private SliderMenu menu;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		setContentView(R.layout.checkbook);

		//The transaction frame, if null it means we can't see transactions in this particular view
		View checkbook_frame = findViewById(R.id.checkbook_frag_frame);

		/*
		 * Hack fix for when in transactions (single pane), rotating keeps the account back stack
		 * so dual pane users have to hit back twice to leave checkbook
		 */
		if(savedInstanceState!=null){
			//Clear BackStack
			while(getSupportFragmentManager().getBackStackEntryCount()>0){
				getSupportFragmentManager().popBackStackImmediate();
			}
		}

		/*NOTE To Self
		 * took out the if because changing orientation resulted
		 *  in transaction fragment staying in accountsFrame
		 *  if you went to transactions in a single pane and then rotated
		 *  Removing if forces the frags to be replaced every time so not very efficient
		 */
		//if (savedInstanceState==null){
		Accounts account_frag = new Accounts();
		Transactions transaction_frag = new Transactions();

		//Bundle for Transaction fragment
		Bundle argsTran = new Bundle();
		argsTran.putBoolean("showAll", true);
		argsTran.putBoolean("boolSearch", false);

		//Bundle for Account fragment
		Bundle argsAccount = new Bundle();
		argsAccount.putBoolean("boolSearch", false);

		transaction_frag.setArguments(argsTran);
		account_frag.setArguments(argsAccount);

		if(checkbook_frame==null){
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.account_frag_frame, account_frag,"account_frag_tag").replace(R.id.transaction_frag_frame, transaction_frag, "transaction_frag_tag").commit();
		}
		else{
			getSupportFragmentManager().beginTransaction().
			replace(R.id.checkbook_frag_frame, account_frag,"account_frag_tag").commit();
		}

		getSupportFragmentManager().executePendingTransactions();

		//}


	}//end onCreate

	@Override
	public void onDestroy(){
		super.onDestroy();
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

	//Method for selecting a Time when adding a transaction
	public void showTimePickerDialog(View v){
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(this.getSupportFragmentManager(), "timePicker");	
	}

	//Method for selecting a Date when adding a transaction
	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(this.getSupportFragmentManager(), "datePicker");
	}

}//end Checkbook