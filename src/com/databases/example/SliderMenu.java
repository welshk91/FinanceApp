package com.databases.example;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu; //import com.slidingmenu.lib.SlidingMenu;

//An Object Class used to hold the data of each account record
public class SliderMenu extends SlidingMenu{
	//Slidingmenu Buttons
	Button SlidingMenu_Checkbook_Button;
	Button SlidingMenu_Categories_Button;
	Button SlidingMenu_Schedule_Button;
	Button SlidingMenu_Manage_Button;
	Button SlidingMenu_Stats_Button;
	Button SlidingMenu_Options_Button;
	Button SlidingMenu_Help_Button;
	Button SlidingMenu_Exit_Button;

	public SliderMenu(Context context) {
		super(context);
		
		//Menu attributes (Need a way to adjust width for different resolutions)
		setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		setMode(SlidingMenu.LEFT);
		setShadowWidthRes(R.dimen.shadow_width);
		setShadowDrawable(R.drawable.shadow);
		//setAboveOffsetRes(R.dimen.slidingmenu_offset);
		//setBehindOffsetRes(R.dimen.slidingmenu_offset);
		//setBehindWidth(R.dimen.slidingmenu_width);
		setBehindWidth(350);
		setFadeDegree(0.50f);
		setMenu(R.layout.sliding_menu);
		
		//SlidingMenu Buttons
		SlidingMenu_Checkbook_Button = (Button)findViewById(R.id.slidingmenu_checkbook);
		SlidingMenu_Checkbook_Button.setOnClickListener(myListener);
		SlidingMenu_Categories_Button = (Button)findViewById(R.id.slidingmenu_categories);
		SlidingMenu_Categories_Button.setOnClickListener(myListener);
		SlidingMenu_Schedule_Button = (Button)findViewById(R.id.slidingmenu_schedules);
		SlidingMenu_Schedule_Button.setOnClickListener(myListener);
		SlidingMenu_Manage_Button = (Button)findViewById(R.id.slidingmenu_manage);
		SlidingMenu_Manage_Button.setOnClickListener(myListener);
		SlidingMenu_Stats_Button = (Button)findViewById(R.id.slidingmenu_statistics);
		SlidingMenu_Stats_Button.setOnClickListener(myListener);
		SlidingMenu_Options_Button = (Button)findViewById(R.id.slidingmenu_options);
		SlidingMenu_Options_Button.setOnClickListener(myListener);
		SlidingMenu_Help_Button = (Button)findViewById(R.id.slidingmenu_help);
		SlidingMenu_Help_Button.setOnClickListener(myListener);
		SlidingMenu_Exit_Button = (Button)findViewById(R.id.slidingmenu_exit);
		SlidingMenu_Exit_Button.setOnClickListener(myListener);
		
	}//end constructor

	//Method handling 'mouse-click'
	public OnClickListener myListener = new OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.slidingmenu_checkbook:
				Log.d("SliderMenu", "Checkbook Listener Fired");
				SliderMenu.this.toggle();
				Intent intentCheckbook = new Intent(getContext(), Checkbook.class);
				intentCheckbook.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getContext().startActivity(intentCheckbook);
				break;

			case R.id.slidingmenu_categories:
				Log.d("SliderMenu", "Schedules Listener Fired");
				SliderMenu.this.toggle();
				Intent intentCategories = new Intent(getContext(), Categories.class);
				intentCategories.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getContext().startActivity(intentCategories);				
				break;
				
			case R.id.slidingmenu_schedules:
				Log.d("SliderMenu", "Schedules Listener Fired");
				SliderMenu.this.toggle();
				Intent intentPlans = new Intent(getContext(), Plans.class);
				intentPlans.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getContext().startActivity(intentPlans);
				break;

			case R.id.slidingmenu_manage:
				Log.d("SliderMenu", "Manage Listener Fired");
				SliderMenu.this.toggle();
				Intent intentManage = new Intent(getContext(), SD.class);
				getContext().startActivity(intentManage);
				break;	

			case R.id.slidingmenu_statistics:
				Log.d("SliderMenu", "Statistics Listener Fired");
				SliderMenu.this.toggle();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();
				break;

			case R.id.slidingmenu_options:
				Log.d("SliderMenu", "Options Listener Fired");
				SliderMenu.this.toggle();
				Intent intentOptions = new Intent(getContext(), Options.class);
				getContext().startActivity(intentOptions);
				break;

			case R.id.slidingmenu_help:
				Log.d("SliderMenu", "Help Listener Fired");
				SliderMenu.this.toggle();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();
				break;

			case R.id.slidingmenu_exit:
				Log.d("SliderMenu", "Exit Listener Fired");
				SliderMenu.this.toggle();
				closeApp();
				break;
				
			default:
				Log.d("SliderMenu", "Default Listener Fired");
				break;
			}

		}// end onClick

		
	};// end onClickListener
	
	//Method to exit app
	private void closeApp() {
		System.exit(0);
	}

}//end class