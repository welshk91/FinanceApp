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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Links extends SherlockActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	final int PICKFILE_RESULT_CODE = 1;
	Intent lastLink;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Attachments");
		setContentView(R.layout.links);

	}//end onCreate

	//Method for when you click the Add button
	public void linkAdd(View v){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		startActivityForResult(intent,PICKFILE_RESULT_CODE);		
	}//end of linkAdd

	//Method for when you click the View button
	public void linkView(View v){

		ImageView image = (ImageView) findViewById(R.id.imageView1);

		//Grab Image from data
		try{
			image.setImageURI(lastLink.getData());
		}
		catch(Exception e){
			//Most likely caused by not picking a pile first (NullPointer)
			//Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
		}

	}//end of linkView

	//Method called after picking a file
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
		case PICKFILE_RESULT_CODE:
			if(resultCode==RESULT_OK){
				String FilePath = data.getData().getPath();
				Toast.makeText(this, "File Path : " + FilePath, Toast.LENGTH_LONG).show();
				lastLink = data;

				TextView currentLink = (TextView)findViewById(R.id.TextViewCurrentLink);
				currentLink.setText("Current Attachment : " + FilePath);
			}
			break;
		}
	}//end of onActivityResult

}//end of Manage