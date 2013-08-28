package com.databases.example;

import java.io.File;
import java.io.IOException;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Dropbox extends SherlockFragmentActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	private SliderMenu menu;

	//DropBox
	private DbxAccountManager dbAccountManager;
	static final int REQUEST_LINK_TO_DBX = 123;
	private static final String appKey = "n98lux9z2rp08lb";
	private static final String appSecret = "exp7ofyw3illtlw";

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Dropbox");
		setContentView(R.layout.dropbox);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		//Initialize DropBox Account Manager
		dbAccountManager = DbxAccountManager.getInstance(getApplicationContext(), appKey, appSecret);

		//Display Login Status
		dropboxStatus();

	}//end onCreate

	//Update Status
	public void dropboxStatus(){
		TextView tvLogStatus = (TextView)findViewById(R.id.TextViewLogStatus);
		Button bLogStatus = (Button)findViewById(R.id.ButtonLogin);

		if(dbAccountManager.hasLinkedAccount()==true){
			tvLogStatus.setText("Status: Logged In");	
			bLogStatus.setText("Log Out");
		}
		else{
			tvLogStatus.setText("Status: Logged Out");
			bLogStatus.setText("Log In");
		}
	}

	//Login or out
	public void dropboxLogin(View v){
		if(dbAccountManager.hasLinkedAccount()==false){
			dbAccountManager.startLink(this, REQUEST_LINK_TO_DBX);
		}
		else{
			dbAccountManager.unlink();
			dropboxStatus();
		}
	}

	//When the sync button is pressed
	public void dropboxSync(View v) throws DbxException {
		DbxFileSystem dbFileSystem = null;

		try {
			dbFileSystem = DbxFileSystem.forAccount(dbAccountManager.getLinkedAccount());
		} catch (Unauthorized e) {
			Toast.makeText(this, "Unauthorized to use Dropbox account", Toast.LENGTH_LONG).show();
			Log.e("Dropbox-dropboxSync", "Unauthorized to use dropbox account? e = "+e);
			e.printStackTrace();
			return;
		}
		catch(Exception e){
			Log.e("Dropbox-dropboxSync", "Are you logged in? Error e ="+e);
			e.printStackTrace();
			Toast.makeText(this, "Not Logged In", Toast.LENGTH_SHORT).show();
			return;
		}

		//Create a sync folder to house the database currently synced
		DbxPath syncFolderPath = new DbxPath("/Sync");
		try {
			dbFileSystem.createFolder(syncFolderPath);
			Log.e("Dropbox-dropboxSync", "Created Sync Folder successfully");
		} catch(DbxException.Exists e){
			Log.e("Dropbox-dropboxSync", "Folder already created? e = "+e);
			e.printStackTrace();
		}
		catch (DbxException e) {
			Log.e("Dropbox-dropboxSync", "Error creating folder. e = "+e);
			e.printStackTrace();
			Toast.makeText(this, "Error creating folder for dropbox syncing", Toast.LENGTH_LONG).show();
			return;
		}

		//Make the sync file (should be the current database)
		DbxPath syncFilePath = new DbxPath("/Sync/dbSync");
		DbxFile syncFile = null;

		try {
			syncFile = dbFileSystem.open(syncFilePath);
			Log.e("Dropbox-dropboxSync", "Opened Sync File successfully");
		} catch(DbxException.NotFound e){
			Log.e("Dropbox-dropboxSync", "File not found? e = "+e);
			e.printStackTrace();
			Log.e("Dropbox-dropboxSync", "Create a new file");
			syncFile = dbFileSystem.create(syncFilePath);
		}
		catch (DbxException e) {
			Log.e("Dropbox-dropboxSync", "Error opening file. e = "+e);
			e.printStackTrace();
			Toast.makeText(this, "Error opening file for dropbox syncing", Toast.LENGTH_LONG).show();
			return;
		}

		//Write current database into the sync file
		DatabaseHelper dh = new DatabaseHelper(this);
		File currentDB = dh.getDatabase();
		try {
			syncFile.writeFromExistingFile(currentDB, false);					
			syncFile.close();
			Log.e("Dropbox-dropboxSync", "Synced File successfully");
		} catch (IOException e) {
			Log.e("Dropbox-dropboxSync", "I/O Error syncing file. e = "+e);
			e.printStackTrace();
			Toast.makeText(this, "I/O Error syncing file", Toast.LENGTH_LONG).show();
			return;
		}
		catch(DbxFile.StreamExclusionException e){
			Log.e("Dropbox-dropboxSync", "Read/Write stream already opened? e = "+e);
			e.printStackTrace();
			Toast.makeText(this, "Read/Write stream already opened?", Toast.LENGTH_LONG).show();
			return;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LINK_TO_DBX) {
			if (resultCode == SherlockFragmentActivity.RESULT_OK) {
				dropboxStatus();
				Log.d("Dropbox-onActivityResult","Logged In/Out Successfully To Dropbox");

			} else {
				//Link failed or was cancelled by the user.
				Log.e("Dropbox-onActivityResult","Result FAILED. Cant use dropbox");
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
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

}//end of Manage