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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Manage extends SherlockFragmentActivity{

	private SliderMenu menu;

	private final static String DEFAULT_BACKUP_DIR = "/WelshFinanceBackUps";
	private final static int PICKFILE_RESULT_CODE = 123;

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

	public void manageRestore(View v) {
		try {
			File sd = Environment.getExternalStorageDirectory();

			if (sd.canWrite()) {
				Log.e("Manage-RestoreDialogFragment", "SD can write into");

				try{
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("file/*");
					startActivityForResult(intent,PICKFILE_RESULT_CODE);
				} catch(Exception e){
					Log.e("Manage-RestoreDialogFragment", "Error e = "+e);
					return;
				}

			}
			else{
				Log.e("Manage-RestoreDialogFragment", "Cannot write into SD");
				Toast.makeText(this, "No SD Card Found!", Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			Log.e("Manage-RestoreDialogFragment", "Error restoring. e="+e);
			Toast.makeText(this, "Error restoring \n"+e, Toast.LENGTH_LONG).show();
		}							

	}

	public void showBackupDialog(View v) {
		EditText tvLogStatus = (EditText)findViewById(R.id.EditTextBackupDir);
		String customBackupDir = tvLogStatus.getText().toString().trim();

		DialogFragment newFragment = BackupDialogFragment.newInstance(customBackupDir);
		newFragment.show(getSupportFragmentManager(), "dialogBackup");
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

	//Method called after picking a file
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
		case PICKFILE_RESULT_CODE:
			if(resultCode==RESULT_OK){
				Log.e("Manage-onActivityResult", "OK. Picked "+ getPath(data.getData()));

				DatabaseHelper dh = new DatabaseHelper(this);
				String restoreDBPath = getPath(data.getData());
				File currentDB = dh.getDatabase();
				File restoreDB = new File(restoreDBPath);

				//write restore file into current database file
				try{
					FileChannel src = new FileInputStream(restoreDB).getChannel();
					FileChannel dst = new FileOutputStream(currentDB).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
					Log.e("Manage-onActivityResult", "Successfully restored database to " + restoreDB.getAbsolutePath());
					Toast.makeText(this, "You restored from \n" + restoreDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
				} catch(Exception e){
					Log.e("Manage-onActivityResult", "Successfully restored database to " + restoreDB.getAbsolutePath());
					Toast.makeText(this, "Restore failed \n" + e, Toast.LENGTH_LONG).show();
				}
			}

			if(resultCode==RESULT_CANCELED){
				Log.e("Manage-onActivityResult", "canceled");
			}

			break;
		}

	}

	//Method finds path name, both from gallery or file manager
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		String linkFilePath;

		if(cursor != null){
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			linkFilePath = cursor.getString(column_index);
		}
		else{
			linkFilePath = uri.getPath();
		}

		return linkFilePath;
	}

}//end of Manage