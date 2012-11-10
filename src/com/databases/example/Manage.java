package com.databases.example;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Manage extends SherlockActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Manage");
		setContentView(R.layout.manage);

	}//end onCreate

	public void backupDialog(View v){
		LayoutInflater li = LayoutInflater.from(Manage.this);
		final View categoryAddView = li.inflate(R.layout.manage_backup, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				Manage.this);

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

				Toast.makeText(Manage.this, "Your backup is named " + backupName, Toast.LENGTH_SHORT).show();

			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}//end of backup

	public void restoreDialog(View v){
		LayoutInflater li = LayoutInflater.from(Manage.this);
		final View categoryAddView = li.inflate(R.layout.manage_restore, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				Manage.this);

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

				Toast.makeText(Manage.this, "Your restore is named " + restoreName, Toast.LENGTH_SHORT).show();		

			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}//end of restore

}//end of Manage