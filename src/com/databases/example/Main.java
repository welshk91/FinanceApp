package com.databases.example;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Main extends Activity {	
	int page;

	//Variables for the Account Table
	String accountName = null;
	String accountTime = null;
	String accountBalance = null;
	String accountDate = null;

	//Variables for the Views
	Button Track_Button;
	Button Database_Button;
	Button Exit_Button;
	Button Start_Button;
	Button Stop_Button;
	Button View_Button;
	Button Clear_Button;
	Button Main_Button;
	Button StartDone_Button;
	Button StartBack_Button;
	Button StopDone_Button;
	Button StopBack_Button;
	EditText startName;
	EditText startTime;
	EditText addAddress;
	EditText removeName;
	TextView startLabel;

	//Variables for the Database
	public final String tblAccounts = "tblAccounts";
	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;

	//Variables for the ListView
	public ArrayList<String> results = new ArrayList<String>();

	//Method handling 'mouse-click'
	public OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.Track:
				//code here for track button
				break;

			case R.id.Database:
				page = R.layout.database;
				break;

			case R.id.Exit:
				if (myDB != null){
					myDB.close();
				}

				Main.this.finish();
				break;

			case R.id.Start:
				page = R.layout.account_add;
				break;

			case R.id.View:
				Intent v = new Intent(Main.this, ViewDB.class);
				startActivity(v);
				break;

			case R.id.Clear:
				AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
				builder.setMessage(
						"Do you want to completely delete the database?\n\nTHIS IS PERMANENT.")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0,
									int arg1) {
								myDB.execSQL("DELETE FROM "
										+ tblAccounts + ";");
							}
						})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								// no action taken
							}
						}).show();
				break;

			case R.id.Main:
				page = R.layout.main;
				break;
			}

			switch (page) {
			case R.layout.main:
				setContentView(R.layout.main);
				Track_Button = (Button) findViewById(R.id.Track);
				Track_Button.setOnClickListener(buttonListener);
				Database_Button = (Button) findViewById(R.id.Database);
				Database_Button.setOnClickListener(buttonListener);
				Exit_Button = (Button) findViewById(R.id.Exit);
				Exit_Button.setOnClickListener(buttonListener);
				break;

			case R.layout.database:
				setContentView(R.layout.database);
				Start_Button = (Button) findViewById(R.id.Start);
				Start_Button.setOnClickListener(buttonListener);
				Stop_Button = (Button) findViewById(R.id.Stop);
				Stop_Button.setOnClickListener(buttonListener);
				View_Button = (Button) findViewById(R.id.View);
				View_Button.setOnClickListener(buttonListener);
				Clear_Button = (Button) findViewById(R.id.Clear);
				Clear_Button.setOnClickListener(buttonListener);
				Main_Button = (Button) findViewById(R.id.Main);
				Main_Button.setOnClickListener(buttonListener);
				break;


			}// end switch(page)

		}// end onClick
	};// end onClickListener

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		page = R.layout.main;
		setContentView(R.layout.main);
		Track_Button = (Button) findViewById(R.id.Track);
		Track_Button.setOnClickListener(buttonListener);
		Database_Button = (Button) findViewById(R.id.Database);
		Database_Button.setOnClickListener(buttonListener);
		Exit_Button = (Button) findViewById(R.id.Exit);
		Exit_Button.setOnClickListener(buttonListener);

		/*
		 * Where the Database is created(if not created already) and opened
		 * Where the Table is created(if not created already) and opened Table
		 * is populated by 'default' data
		 */
		try {
			myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
			myDB.execSQL("CREATE TABLE IF NOT EXISTS "
					+ tblAccounts
					+ " (ID INTEGER PRIMARY KEY, Name VARCHAR, Balance VARCHAR, Time VARCHAR, Date VARCHAR);");
			/*
			myDB.execSQL("INSERT INTO "
					+ tblAccounts
					+ " (Name, Balance, Time, Date)"
					+ " VALUES ('LMCU', '$500', '5:30', '5-26-2012');");
			 */
		} 
		catch (Exception e) {
			System.out.print("Error Creating Database!!!");
		}

	}// end onCreate

	/*
	 * Handle closing database properly to avoid corruption
	 * */
	@Override
	public void onDestroy() {
		if (myDB != null){
			myDB.close();
		}
		super.onDestroy();
	}

}// end Database