/* Currently unused Class that handles will allow user to attach a file to a Transaction
 * Everything appears to work. Just needs to be integrated with Checkbook screen
 */

package com.databases.example;

import java.io.File;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Links extends SherlockFragmentActivity{
	private final static int PICKFILE_RESULT_CODE = 1;
	private final static int PICKCONTACT_RESULT_CODE = 2;

	//NavigationDrawer
	private Drawer mDrawerLayout;

	private Intent lastLink;
	private String linkFilePath = null;
	private static int linkItem;

	//Contact Info
	private long contactId = 0;
	private String contactName = null;
	private String contactPhone = null;
	private String contactEmail = null;
	private Uri contactPhoto = null;

	private static Intent intent = null;
	private AlertDialog alertDialogAttachment;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle("Attachments");
		setContentView(R.layout.links);

		//NavigationDrawer
		DrawerLayout view = (DrawerLayout) findViewById(R.id.drawer_layout);
		ScrollView drawer = (ScrollView) findViewById(R.id.drawer);
		mDrawerLayout = new Drawer(this,view,drawer);

	}//end onCreate

	//Method for when you click the Add button
	public void linkAdd(View v){
		DialogFragment newFragment = AttachDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialogAttach");
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
				Log.e("Links-linkView","No App for this type of file. Error e="+e);
				Toast.makeText(this, "Could not find an app for this type of file.", Toast.LENGTH_SHORT).show();
			}

		}
		catch(Exception e){
			//Most likely caused by not picking a pile first (NullPointer)
			Log.e("Links-linkView", "Error e=" + e);
			Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
		}

	}//end of linkView

	//Method for when you click the View button
	public void linkDone(View v){

		Log.e("Links-linkDone", "linkFilePath=" + linkFilePath);
		Log.e("Links-linkDone", "AcctID=" + getIntent().getExtras().getString("AcctID"));
		Log.e("Links-linkDone", "AcctName=" + getIntent().getExtras().getString("AcctName"));

		Intent returnIntent = new Intent();
		setResult(RESULT_OK,returnIntent);     
		finish();
	}//end of linkView

	//Method called after picking a file
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
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
						//Most likely caused by not picking a file first (NullPointer)
						Log.e("Links-onActivityResult", "Error e=" + e);
					}

				}
				break;

			case PICKCONTACT_RESULT_CODE:	
				if(resultCode==RESULT_OK){
					getContactInfo(data);
					Log.e("Links-onActivityResult","contact: " + contactId + " " + contactName + " " + contactPhone + " " + contactEmail);

					TextView currentLink = (TextView)findViewById(R.id.TextViewCurrentLink);
					currentLink.setText("Current Attachment : " + contactName);

					ImageView image = (ImageView) findViewById(R.id.imageView1);

					try{
						image.setImageURI(contactPhoto);
					}
					catch(Exception e){
						Log.e("Links-onActivityResult", "Error e=" + e);
					}

				}
				break;

			}

		}
		catch(Exception e){
			Log.e("Links-onActivityResult","Error e=" + e);
			Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
		}
	}//end of onActivityResult

	//Method finds path name, both from gallery or file manager
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);

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

	//Method to grab contact info
	protected void getContactInfo(Intent intent)
	{
		Cursor cursor =  managedQuery(intent.getData(), null, null, null, null);      

		cursor.moveToFirst();  
		contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

		String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

		if (hasPhone.equalsIgnoreCase("1"))
			hasPhone = "true";
		else
			hasPhone = "false" ;

		if (Boolean.parseBoolean(hasPhone)) 
		{
			Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
			while (phones.moveToNext()) 
			{
				contactPhone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			}
			phones.close();
		}

		// Find Email Addresses
		Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,null, null);
		while (emails.moveToNext()) 
		{
			contactEmail = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		}
		emails.close();
		cursor.close();

		//Get contact picture
		Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactId);
		contactPhoto = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

	}//end getContactInfo

	//Close dialogs to prevent window leaks
	@Override
	public void onPause() {
		//		if(alertDialogAttachment!=null){
		//			alertDialogAttachment.dismiss();
		//		}
		super.onPause();
	}

	public static class AttachDialogFragment extends SherlockDialogFragment {

		public static AttachDialogFragment newInstance() {
			AttachDialogFragment frag = new AttachDialogFragment();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater li = LayoutInflater.from(getActivity());
			View linkChooser = li.inflate(R.layout.link_chooser, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

			alertDialogBuilder.setView(linkChooser);
			alertDialogBuilder.setTitle("Attachment");
			alertDialogBuilder
			.setCancelable(true);

			ListView linkTypes = (ListView)linkChooser.findViewById(R.id.linkchooser_types);
			linkTypes.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					linkItem = position;

					//Call an intent based on what type of link
					switch (linkItem) {

					//Picture
					case 0:
						intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("image/*");
						break;

						//Video
					case 1:
						intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("video/*");
						break;

						//Audio
					case 2:
						intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("audio/*");
						break;

						//File	
					case 3:
						intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("file/*");
						break;

						//Contact	
					case 4:
						intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
						getActivity().startActivityForResult(intent,PICKCONTACT_RESULT_CODE); 

						getDialog().cancel();
						return;

						//Any	
					default:
						intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						break;

					}//end switch

					try{
						getActivity().startActivityForResult(intent,PICKFILE_RESULT_CODE);		
					}
					catch(ActivityNotFoundException e){
						Log.e("Links-onItemClick", "Error e=" + e);
						Toast.makeText(getActivity(), "No Program Found", Toast.LENGTH_SHORT).show();
					}
					catch(Exception e){
						Log.e("Links-onItemClick", "Error e=" + e);
					}

					getDialog().cancel();

				}
			});

			return alertDialogBuilder.create();

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
		return true;
	}

}//end of Links