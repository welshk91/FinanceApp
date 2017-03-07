/* A activity to handle the when a user forgets his lockscreen pattern
 * Not used just yet. Might be better to make this a plugin for the app
 * to avoid the Internet permission and extra third-party library needed
 * to email
 */

package com.databases.example.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.databases.example.R;

import haibison.android.lockpattern.utils.AlpSettings;

public class PatternRetrievalActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fireDialog();
    }

    private void fireDialog() {
        DialogFragment newFragment = EmailDialogFragment.newInstance();
        newFragment.setCancelable(false);
        newFragment.show(getSupportFragmentManager(), "dialogEmail");
    }

    private void emailCode() {
        Toast.makeText(this, "emailing...", Toast.LENGTH_SHORT).show();
        EmailTask runner = new EmailTask();
        runner.execute();
        enterCode();
    }

    private void enterCode() {
        DialogFragment newFragment = VerifyDialogFragment.newInstance();
        newFragment.setCancelable(false);
        newFragment.show(getSupportFragmentManager(), "dialogVerify");
    }

    //Class that handles dialog for user to confirm email
    public static class EmailDialogFragment extends DialogFragment {

        public static EmailDialogFragment newInstance() {
            EmailDialogFragment frag = new EmailDialogFragment();
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("Email Me Code");
            alertDialogBuilder.setMessage("Should I email you the code?");

            alertDialogBuilder
                    .setPositiveButton("Email",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    ((PatternRetrievalActivity) getActivity()).emailCode();
                                }
                            }
                    )
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    System.exit(0);
                                }
                            }
                    );

            return alertDialogBuilder.create();
        }
    }

    //Class that handles confirming the code that was emailed
    public static class VerifyDialogFragment extends DialogFragment {

        public static VerifyDialogFragment newInstance() {
            VerifyDialogFragment frag = new VerifyDialogFragment();
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("Verify Code");
            alertDialogBuilder.setMessage("Please enter the code?");

            alertDialogBuilder
                    .setPositiveButton("Enter",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                    //if code = pattern
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                    prefs.edit().putBoolean(getString(R.string.pref_key_lock_enabled), false).commit();
                                    AlpSettings.Security.setPattern(getActivity(), null);

                                    //else
                                    //Toast.makeText(getActivity(), "Code is incorrect", Toast.LENGTH_SHORT).show();

                                    System.exit(0);

                                }
                            }
                    )
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    System.exit(0);
                                }
                            }
                    );

            return alertDialogBuilder.create();
        }
    }

    private class EmailTask extends AsyncTask<Void, Integer, Long> {
        @Override
        protected Long doInBackground(Void... params) {

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"welshk91@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "This is the subject of the email");
            i.putExtra(Intent.EXTRA_TEXT, "This is the body of the email");

            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
                Log.d(getClass().getSimpleName(), "Successfully emailed pattern");
            } catch (android.content.ActivityNotFoundException e) {
                Log.e(getClass().getSimpleName(), "No Email clieant found? Error e=" + e);
                Toast.makeText(getParent(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
            Log.e("AsyncTask", "onPostExecute");
            //Toast.makeText(getParent(), "Finished emailing", Toast.LENGTH_SHORT).show();
        }

    }
}