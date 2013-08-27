package com.databases.example;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
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
import android.widget.EditText;
import android.widget.Toast;

public class Manage extends SherlockFragmentActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	private SliderMenu menu;

	//Dialogs that need to be dismissed
	AlertDialog alertDialogCreate;
	AlertDialog alertDialogRestore;

	//DropBox
	private DbxAccountManager dbAccountManager;
	static final int REQUEST_LINK_TO_DBX = 123;


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Manage");
		setContentView(R.layout.manage);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		//Initialize DropBox Account Manager
		dbAccountManager = DbxAccountManager.getInstance(getApplicationContext(), "n98lux9z2rp08lb", "exp7ofyw3illtlw");

	}//end onCreate

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

	//Close dialogs to prevent window leaks
	@Override
	public void onPause() {
		//		if(alertDialogCreate!=null){
		//			alertDialogCreate.dismiss();
		//		}
		//		if(alertDialogRestore!=null){
		//			alertDialogRestore.dismiss();
		//		}
		super.onPause();
	}

	public void showRestoreDialog(View v) {
		DialogFragment newFragment = RestoreDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialogRestore");
	}

	public void showBackupDialog(View v) {
		DialogFragment newFragment = BackupDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialogBackup");
	}

	public void syncTime(View v) {
		if(dbAccountManager.hasLinkedAccount()==false){
			Log.e("Manage-syncTime","Starting link...");
			dbAccountManager.startLink(this, REQUEST_LINK_TO_DBX);
		}
		else{
			Log.e("Manage-syncTime","Already have a link established!");
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LINK_TO_DBX) {
			if (resultCode == SherlockFragmentActivity.RESULT_OK) {
				// ... Start using Dropbox files.
				Log.e("Manage-onActivityResult","Result Okay. Start using dropbox...");
				
				DbxFileSystem dbxFs = null;
				
				try {
					dbxFs = DbxFileSystem.forAccount(dbAccountManager.getLinkedAccount());
				} catch (Unauthorized e) {
					Toast.makeText(this, "Unauthorized to use Dropbox account", Toast.LENGTH_LONG).show();
					Log.e("Manage-onActivityResult", "Unauthorized to use dropbox account? e = "+e);
					e.printStackTrace();
				}
				
				//Create a sync folder to house the database currently synced
				DbxPath syncPath = new DbxPath("/Sync");
				try {
					dbxFs.createFolder(syncPath);
					Log.e("Manage-onActivityResult", "Created Sync Folder successfully");
				} catch (DbxException e) {
					Log.e("Manage-onActivityResult", "Could Not create Sync Folder. e = "+e);
					e.printStackTrace();
				}
				
				//Make the sync file (should be the current database)
				
				
				
			} else {
				// ... Link failed or was cancelled by the user.
				Log.e("Manage-onActivityResult","Result FAILED. Cant use dropbox");
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public static class RestoreDialogFragment extends SherlockDialogFragment {

		public static RestoreDialogFragment newInstance() {
			RestoreDialogFragment frag = new RestoreDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(this.getActivity());
			final View categoryAddView = li.inflate(R.layout.manage_restore, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this.getActivity());

			// set account_add.xml to AlertDialog builder
			alertDialogBuilder.setView(categoryAddView);

			//set Title
			alertDialogBuilder.setTitle("Restoring A Backup");

			// set dialog message
			alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Restore",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					EditText restoreTextBox = (EditText)categoryAddView.findViewById(R.id.EditRestoreName);
					String restoreName = restoreTextBox.getText().toString().trim();
					Toast.makeText(getActivity(), "Your restore is named " + restoreName, Toast.LENGTH_SHORT).show();		

				}
			})
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			return alertDialogBuilder.create();

		}
	}

	public static class BackupDialogFragment extends SherlockDialogFragment {

		public static BackupDialogFragment newInstance() {
			BackupDialogFragment frag = new BackupDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(this.getActivity());
			final View categoryAddView = li.inflate(R.layout.manage_backup, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

			// set account_add.xml to AlertDialog builder
			alertDialogBuilder.setView(categoryAddView);

			//set Title
			alertDialogBuilder.setTitle("Creating A Backup");

			// set dialog message
			alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Backup",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					EditText backupTextBox = (EditText)categoryAddView.findViewById(R.id.EditBackupName);
					String backupName = backupTextBox.getText().toString().trim();

					Toast.makeText(getActivity(), "Your backup is named " + backupName, Toast.LENGTH_SHORT).show();

				}
			})
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			return alertDialogBuilder.create();

		}
	}

}//end of Manage