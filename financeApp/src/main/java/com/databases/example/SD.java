/* Class that handles the Local Storage Options seen in the Options screen
 * Allows user to make a local backup to a custom folder
 * and restore from a local backup file
 * No error checking yet...
 */

package com.databases.example;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class SD extends SherlockFragmentActivity{
    private final static String DEFAULT_BACKUP_DIR = "/WelshFinanceBackUps";
    private final static int PICKFILE_RESULT_CODE = 123;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sd);
        setTitle("Local Backup");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }//end onCreate

    //For Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sdRestore(View v) {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                Log.e("SD-sdRestore", "SD can write into");

                try{
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent,PICKFILE_RESULT_CODE);
                } catch(ActivityNotFoundException e){
                    Log.e("SD-sdRestore", "No program to handle intent? Error e=" + e);
                    Toast.makeText(this, "Please install a file manager", Toast.LENGTH_LONG).show();
                } catch(Exception e){
                    Log.e("SD-sdRestore", "Error e = "+e);
                    return;
                }

            }
            else{
                Log.e("SD-sdRestore", "Cannot write into SD");
                Toast.makeText(this, "No SD Card Found!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("SD-sdRestore", "Error restoring. e="+e);
            Toast.makeText(this, "Error restoring \n"+e, Toast.LENGTH_LONG).show();
        }

    }

    public void sdBackup(View v) {
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
            final View categoryAddView = li.inflate(R.layout.sd_backup, null);
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
                                    Log.d("SD-BackupDialogFragment", "SD can write into");

                                    File backupDir;

                                    //Handle Custom Directory
                                    if(customBackupDir.matches("")){
                                        Log.d("SD-BackupDialogFragment", "Use default directory");
                                        backupDir = new File(sd.getAbsoluteFile()+DEFAULT_BACKUP_DIR);
                                        backupDir.mkdir();
                                    }
                                    else{
                                        Log.d("SD-BackupDialogFragment", "Use custom directory");
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
                                        Log.d("SD-BackupDialogFragment", "currentDB exists");
                                        FileChannel src = new FileInputStream(currentDB).getChannel();
                                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                                        dst.transferFrom(src, 0, src.size());
                                        src.close();
                                        dst.close();
                                        Log.d("SD-BackupDialogFragment", "Successfully backed up database to " + backupDB.getAbsolutePath());
                                        Toast.makeText(getActivity(), "Your backup\n" + backupDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                    }
                                }
                                else{
                                    Log.e("SD-BackupDialogFragment", "Cannot write into SD");
                                    Toast.makeText(getActivity(), "No SD Card Found!", Toast.LENGTH_LONG).show();
                                }

                            } catch (Exception e) {
                                Log.e("SD-BackupDialogFragment", "Error backing up. e="+e);
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
                    Log.e("SD-onActivityResult", "OK. Picked "+ getPath(data.getData()));

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
                        Log.e("SD-onActivityResult", "Successfully restored database to " + restoreDB.getAbsolutePath());
                        Toast.makeText(this, "You restored from \n" + restoreDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch(Exception e){
                        Log.e("SD-onActivityResult", "Restore failed \n" + e);
                        Toast.makeText(this, "Restore failed \n" + e, Toast.LENGTH_LONG).show();
                    }
                }

                if(resultCode==RESULT_CANCELED){
                    Log.e("SD-onActivityResult", "canceled");
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

}//end of SD