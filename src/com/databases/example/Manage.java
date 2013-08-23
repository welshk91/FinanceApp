package com.databases.example;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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