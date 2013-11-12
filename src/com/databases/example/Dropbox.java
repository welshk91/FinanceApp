/* Class that handles the Dropbox Options in the Options Menu
 * Incomplete: still doesn't do syncing to multiple devices, Dropbox Drop-In Saver not available yet
 * Complete: Dropbox Drop-In Chooser works
 */

package com.databases.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.dropbox.chooser.android.DbxChooser;
import com.dropbox.chooser.android.DbxChooser.Result;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Dropbox extends SherlockFragmentActivity{
	//NavigationDrawer
	private Drawer mDrawerLayout;

	//DropBox
	private DbxAccountManager dbAccountManager;
	private static final int REQUEST_LINK_TO_DBX = 100;
	private static final int DBX_CHOOSER_REQUEST = 200;
	private static final String appKey = "n98lux9z2rp08lb";
	private static final String appSecret = "exp7ofyw3illtlw";

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Dropbox");
		setContentView(R.layout.dropbox);

		//NavigationDrawer
		DrawerLayout view = (DrawerLayout) findViewById(R.id.drawer_layout);
		ScrollView drawer = (ScrollView) findViewById(R.id.drawer);
		mDrawerLayout = new Drawer(this,view,drawer);

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

	//Fires Up Dropbox Chooser
	public void dropboxChooser(View v){
		DbxFileSystem dbFileSystem = null;

		//Create Dropbox Chooser
		DbxChooser mChooser = new DbxChooser(appKey);
		mChooser.forResultType(DbxChooser.ResultType.FILE_CONTENT).launch(this, DBX_CHOOSER_REQUEST);
	}

	//Restores database file from Dropbox Chooser
	public void dropboxRestore(Result result){
		DatabaseHelper dh = new DatabaseHelper(this);
		String restoreDBPath = result.getLink().getPath();
		File currentDB = dh.getDatabase();
		File restoreDB = new File(restoreDBPath);

		//write restore file into current database file
		try{
			FileChannel src = new FileInputStream(restoreDB).getChannel();
			FileChannel dst = new FileOutputStream(currentDB).getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
			Log.e("Dropbox-DropboxRestore", "Successfully restored database to " + restoreDB.getAbsolutePath());
			Toast.makeText(this, "You restored from \n" + restoreDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
		} catch(Exception e){
			Log.e("Dropbox-DropboxRestore", "Restore failed \n" + e);
			Toast.makeText(this, "Restore failed \n" + e, Toast.LENGTH_LONG).show();
		}		
	}

	//Dropbox Drop-In "Saver"
	public void dropboxBackup(View v){
		Toast.makeText(this, "Coming Soon...", Toast.LENGTH_SHORT).show();
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

		//Make the sync file (should be the current database)
		DbxPath syncFilePath = new DbxPath("/Sync/dbSync");
		DbxFile syncFile = null;

		//Need to force sync to stop "conflicting copy" bugs
		//dbFileSystem.syncNowAndWait();

		try {
			syncFile = dbFileSystem.open(syncFilePath);
			Log.e("Dropbox-dropboxSync", "Opened Sync File successfully");
		} catch(DbxException.NotFound e){
			Log.e("Dropbox-dropboxSync", "File not found? e = "+e);
			e.printStackTrace();
			Log.e("Dropbox-dropboxSync", "Create a new file");
			syncFile = dbFileSystem.create(syncFilePath);
		} catch (DbxException e) {
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
			Log.e("Dropbox-dropboxSync", "Synced File successfully");
			Toast.makeText(this, "Synced File successfully", Toast.LENGTH_LONG).show();
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
		} finally{
			syncFile.close();
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
		} 

		if (requestCode == DBX_CHOOSER_REQUEST) {
			if (resultCode == SherlockFragmentActivity.RESULT_OK) {
				DbxChooser.Result result = new DbxChooser.Result(data);
				Log.d("Dropbox-onActivityResult", "Link to selected file: " + result.getLink());
				dropboxRestore(result);
			} 

			else {
				// Failed or was cancelled by the user.
				Log.d("Dropbox-onActivityResult", "Dropbox Chooser: Failed or Canceled");
				Toast.makeText(this, "Failed or canceled", Toast.LENGTH_LONG).show();
			}
		}

		else {
			super.onActivityResult(requestCode, resultCode, data);
		}

	}

	//For Menu Items
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:    
			mDrawerLayout.toggle();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}//end of Dropbox