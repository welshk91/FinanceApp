/* A simple Activity for the Checkbook screen
 * Most of the work seen in the Checkbook screen is actually the fragments,
 * not this class. This class is just a simple parent Activity for the fragments
 */

package com.databases.example;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.databases.example.Transactions.DatePickerFragment;
import com.databases.example.Transactions.TimePickerFragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;

public class Checkbook extends SherlockFragmentActivity {

	//NavigationDrawer
	private Drawer drawer;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkbook);
		setTitle("Checkbook");

		/*
		 * This crashes if you change orientation
		 * has to do with onPostCreate()
		 * */
		
		//if(savedInstanceState!=null){
		//	Log.e("Checkbook","SavedState");
		//	return;
		//}
		
		//The transaction frame, if null it means we can't see transactions in this particular view
		View checkbook_frame = findViewById(R.id.checkbook_frag_frame);

		//Clear notifications
		if (getIntent().getExtras() != null) {
			Bundle b = getIntent().getExtras();
		
			if(b.getBoolean("fromNotification")){
				clearNotifications();
			}
		}

		//NavigationDrawer
		drawer = new Drawer(this);

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
			Log.d("Checkbook-onCreate","Mode:dualpane");
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.account_frag_frame, account_frag,"account_frag_tag").replace(R.id.transaction_frag_frame, transaction_frag, "transaction_frag_tag").commit();
		}
		else{
			Log.d("Checkbook-onCreate","Mode:singlepane");
			getSupportFragmentManager().beginTransaction().
			replace(R.id.checkbook_frag_frame, account_frag,"account_frag_tag").commit();
		}

		getSupportFragmentManager().executePendingTransactions();

	}//end onCreate

	//Needed to have notification extras work
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		if (getIntent().getExtras() != null) {
			Bundle b = getIntent().getExtras();

			if(b.getBoolean("fromNotification")){
				clearNotifications();
			}
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle bundle1){
		super.onSaveInstanceState(bundle1);
	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			drawer.toggle();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	//Method for clearing notifications if they were clicked on
	public void clearNotifications(){
		Log.v("Checkbook","Clearing notifications...");
		Uri uri = Uri.parse(MyContentProvider.NOTIFICATIONS_URI + "/" + 0);
		getContentResolver().delete(uri, null,null);
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
	
}//end Checkbook