package com.databases.example;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class Checkbook extends SherlockFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkbook);

		boolean dualPane = false;
		View checkbook_frame;
		checkbook_frame = findViewById(R.id.checkbook_frag_frame);

		if(checkbook_frame!=null){
			dualPane=false;

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			Accounts account_frag = new Accounts();

			getSupportFragmentManager().beginTransaction()
			.add(R.id.checkbook_frag_frame, account_frag).commit();

		}
		else{
			dualPane=true;

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			Accounts account_frag = new Accounts();
			Accounts account_frag2 = new Accounts();
			Transactions transaction_frag = new Transactions();
			Bundle args = new Bundle();
			transaction_frag.setArguments(args);

			getSupportFragmentManager().beginTransaction()
			.add(R.id.account_frag_frame, account_frag).add(R.id.transaction_frag_frame, transaction_frag).commit();

			//getSupportFragmentManager().beginTransaction()
			//.add(R.id.transaction_frag_frame, transaction_frag).commit();

		}

		Toast.makeText(this, "DualPane: " + dualPane, Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onDestroy(){
		//Toast.makeText(this, "Destroying...", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	//For Menu
	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		super.onCreateOptionsMenu(menu);
	//		MenuInflater inflater = getSupportMenuInflater();
	//		inflater.inflate(R.layout.search_menu, menu);
	//		return true;
	//	}

	//	//If android version supports it, smooth gradient
	//	@TargetApi(5)
	//	@Override
	//	public void onAttachedToWindow() {
	//		super.onAttachedToWindow();
	//		Window window = (Window) getWindow();
	//		window.setFormat(PixelFormat.RGBA_8888);
	//	}

}//end SearchTime