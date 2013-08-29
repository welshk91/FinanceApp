package com.databases.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Manage extends SherlockFragmentActivity{

	private SliderMenu menu;

	private final static String DEFAULT_BACKUP_DIR = "/WelshFinanceBackUps";

	//Dialogs that need to be dismissed
	AlertDialog alertDialogCreate;
	AlertDialog alertDialogRestore;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Manage");
		setContentView(R.layout.manage);

		//Add Sliding Menu
		menu = new SliderMenu(this);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

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

	public void showRestoreDialog(View v) {
		DialogFragment newFragment = RestoreDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialogRestore");
	}

	public void showBackupDialog(View v) {
		EditText tvLogStatus = (EditText)findViewById(R.id.EditTextBackupDir);
		String customBackupDir = tvLogStatus.getText().toString().trim();

		DialogFragment newFragment = BackupDialogFragment.newInstance(customBackupDir);
		newFragment.show(getSupportFragmentManager(), "dialogBackup");
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

		public static BackupDialogFragment newInstance(String customBackupDir) {
			BackupDialogFragment frag = new BackupDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			args.putString("customBackupDir", customBackupDir);			
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(this.getActivity());
			final View categoryAddView = li.inflate(R.layout.manage_backup, null);
			final String customBackupDir = getArguments().getString("customBackupDir");

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

			//set account_add.xml to AlertDialog builder
			alertDialogBuilder.setView(categoryAddView);

			//set Title
			alertDialogBuilder.setTitle("Creating A Backup");

			//set dialog message
			alertDialogBuilder
			.setCancelable(true)
			.setPositiveButton("Backup",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					EditText backupTextBox = (EditText)categoryAddView.findViewById(R.id.EditBackupName);
					String backupName = backupTextBox.getText().toString().trim();

					/**
					 * Code derived from StackOverflow
					 * http://stackoverflow.com/questions/1995320/how-to-backup-database-file-to-sdcard-on-android
					 **/

					try {
						File sd = Environment.getExternalStorageDirectory();

						if (sd.canWrite()) {
							Log.e("Manage-BackupDialogFragment", "SD can write into");

							File backupDir;

							//Handle Custom Directory
							if(customBackupDir.matches("")){
								Log.e("Manage-BackupDialogFragment", "Use default directory");
								backupDir = new File(sd.getAbsoluteFile()+DEFAULT_BACKUP_DIR);
								backupDir.mkdir();
							}
							else{
								Log.e("Manage-BackupDialogFragment", "Use custom directory");
								if(!customBackupDir.startsWith("/")){
									backupDir = new File(sd.getAbsoluteFile()+"/"+customBackupDir);
								}
								else{
									backupDir = new File(sd.getAbsoluteFile()+customBackupDir);
								}

								backupDir.mkdir();
							}

							DatabaseHelper dh = new DatabaseHelper(getActivity());
							String backupDBPath = backupDir.getAbsolutePath()+"/"+backupName;
							File currentDB = dh.getDatabase();
							File backupDB = new File(backupDBPath);

							if (currentDB.exists()) {
								Log.e("Manage-BackupDialogFragment", "currentDB exists");
								FileChannel src = new FileInputStream(currentDB).getChannel();
								FileChannel dst = new FileOutputStream(backupDB).getChannel();
								dst.transferFrom(src, 0, src.size());
								src.close();
								dst.close();
								Log.e("Manage-BackupDialogFragment", "Successfully backed up database to " + backupDB.getAbsolutePath());
								Toast.makeText(getActivity(), "Your backup\n" + backupDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
							}
						}
						else{
							Log.e("Manage-BackupDialogFragment", "Cannot write into SD");
							Toast.makeText(getActivity(), "No SD Card Found!", Toast.LENGTH_LONG).show();
						}

					} catch (Exception e) {
						Log.e("Manage-BackupDialogFragment", "Error backing up. e="+e);
						Toast.makeText(getActivity(), "Error backing up \n"+e, Toast.LENGTH_LONG).show();
					}

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