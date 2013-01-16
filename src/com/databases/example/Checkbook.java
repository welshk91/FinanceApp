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

		View transaction_frame = findViewById(R.id.transaction_frag_frame);

		if (savedInstanceState==null){

			Accounts account_frag = new Accounts();
			Transactions transaction_frag = new Transactions();
			Bundle args = new Bundle();
			transaction_frag.setArguments(args);

			if(transaction_frame!=null){
				getSupportFragmentManager().beginTransaction()
				.add(R.id.account_frag_frame, account_frag,"account_frag_tag").add(R.id.transaction_frag_frame, transaction_frag, "transaction_frag_tag").commit();
			}
			else{
				getSupportFragmentManager().beginTransaction()
				.add(R.id.account_frag_frame, account_frag,"account_frag_tag").commit();
			}
		}


	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	//For Menu
	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		super.onCreateOptionsMenu(menu);
	//		MenuInflater inflater = getSupportMenuInflater();
	//		return true;
	//
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