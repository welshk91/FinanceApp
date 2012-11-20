package com.databases.example;

import java.io.File;

import com.actionbarsherlock.app.SherlockActivity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Links extends SherlockActivity{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	final int PICKFILE_RESULT_CODE = 1;
	Intent lastLink;
	String linkFilePath = null;

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

		try{
			startActivityForResult(intent,PICKFILE_RESULT_CODE);		
		}
		catch(Exception e){
			Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
		}
	}//end of linkAdd

	//Method for when you click the View button
	public void linkView(View v){
		try{
			Intent intent= new Intent();
			intent.setAction(Intent.ACTION_VIEW);

			File file = new File(getPath(lastLink.getData()));

			MimeTypeMap mime = MimeTypeMap.getSingleton();
			String ext = file.getName().substring(file.getName().indexOf(".")+1);
			String type = mime.getMimeTypeFromExtension(ext);
			intent.setDataAndType(Uri.fromFile(file), type);

			try {
				startActivityForResult(Intent.createChooser(intent, "Open with..."), PICKFILE_RESULT_CODE);
			} catch (android.content.ActivityNotFoundException e) {
				Toast.makeText(this, "Could not find an app for this type of file.", Toast.LENGTH_SHORT).show();
			}

		}
		catch(Exception e){
			//Most likely caused by not picking a pile first (NullPointer)
			Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
		}

	}//end of linkView

	//Method called after picking a file
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		Toast.makeText(this, "onActivityResult", Toast.LENGTH_SHORT).show();
		try{
			switch (requestCode) {
			case PICKFILE_RESULT_CODE:
				if(resultCode==RESULT_OK){
					linkFilePath = null;
					linkFilePath = getPath(data.getData());
					lastLink = data;

					TextView currentLink = (TextView)findViewById(R.id.TextViewCurrentLink);
					currentLink.setText("Current Attachment : " + linkFilePath);

					//Set thumbnail
					ImageView image = (ImageView) findViewById(R.id.imageView1);

					try{
						image.setImageURI(lastLink.getData());
					}
					catch(Exception e){
						//Most likely caused by not picking a pile first (NullPointer)
						//Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
					}

				}
				break;
			}

		}
		catch(Exception e){
			Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
		}
	}//end of onActivityResult

	//Method finds path name, both from gallery or file manager
	public String getPath(Uri uri) {
		Toast.makeText(this, "getPath", Toast.LENGTH_SHORT).show();

		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);

		if(cursor != null){
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			linkFilePath = cursor.getString(column_index);
		}else{
			linkFilePath = uri.getPath();
		}

		return linkFilePath;
	}

}//end of Manage