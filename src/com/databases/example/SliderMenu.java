package com.databases.example;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import com.slidingmenu.lib.SlidingMenu;

//An Object Class used to hold the data of each account record
public class SliderMenu extends SlidingMenu{
	//Slidingmenu Buttons
	Button SlidingMenu_Checkbook_Button;
	Button SlidingMenu_Schedule_Button;
	Button SlidingMenu_Manage_Button;
	Button SlidingMenu_Stats_Button;
	Button SlidingMenu_Options_Button;
	Button SlidingMenu_Help_Button;
	Button SlidingMenu_Exit_Button;

	public SliderMenu(Context context) {
		super(context);

		//Menu attributes
		setContent(R.layout.sliding_menu);
		setClickable(true);		
		setMenu(R.layout.sliding_menu);
		setMode(SlidingMenu.LEFT);
		setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		setShadowWidthRes(R.dimen.shadow_width);
		setShadowDrawable(R.drawable.shadow);
		//setAboveOffsetRes(R.dimen.slidingmenu_offset);
		//setBehindOffsetRes(R.dimen.slidingmenu_offset);
		setBehindWidth(250);
		setFadeDegree(0.50f);

		//SlidingMenu Buttons
		SlidingMenu_Checkbook_Button = (Button)findViewById(R.id.slidingmenu_checkbook);
		SlidingMenu_Checkbook_Button.setOnClickListener(myListener);
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
				//Log.e("SliderMenu", "Checkbook Listener Fired");
				SliderMenu.this.toggle();
				//Toast.makeText(SliderMenu.this.getContext(), "Clicked Checkbook", Toast.LENGTH_SHORT).show();
				//	createDatabase();
				Intent intentCheckbook = new Intent(getContext(), Checkbook.class);
				intentCheckbook.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getContext().startActivity(intentCheckbook);
				break;

			case R.id.slidingmenu_schedules:
				//Log.e("SliderMenu", "Schedules Listener Fired");
				SliderMenu.this.toggle();
				break;

			case R.id.slidingmenu_manage:
				//Log.e("SliderMenu", "Manage Listener Fired");
				SliderMenu.this.toggle();
				//	createDatabase();
				Intent intentManage = new Intent(getContext(), Manage.class);
				getContext().startActivity(intentManage);
				break;	

			case R.id.slidingmenu_statistics:
				//Log.e("SliderMenu", "Statistics Listener Fired");
				SliderMenu.this.toggle();
				//	createDatabase();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();
				break;

			case R.id.slidingmenu_options:
				//Log.e("SliderMenu", "Options Listener Fired");
				SliderMenu.this.toggle();
				Intent intentOptions = new Intent(getContext(), Options.class);
				getContext().startActivity(intentOptions);
				break;

			case R.id.slidingmenu_help:
				//Log.e("SliderMenu", "Help Listener Fired");
				SliderMenu.this.toggle();
				//	createDatabase();
				//	Intent intentStats = new Intent(Main.this, Accounts.class);
				//	startActivity(intentStats);
				//drawPattern();
				break;

			case R.id.slidingmenu_exit:
				//Log.e("SliderMenu", "Exit Listener Fired");
				SliderMenu.this.toggle();
				
//				if (Main.this.myDB != null){
//					myDB.close();
//					
//				}

				//	Main.this.finish();
				//	onDestroy();
				break;
				
			default:
				//Log.e("SliderMenu", "Default Listener Fired");
				break;

			}

		}// end onClick
	};// end onClickListener

}//end class