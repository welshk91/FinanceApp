/* Class that handles the BroadcastReceiver needed for Scheduling Transactions
 * Handles rescheduling previous plans if the user reboots the phone 
 */

package com.databases.example.data;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.app.CheckbookActivity;
import com.databases.example.model.Notification;
import com.databases.example.model.Plan;
import com.databases.example.utils.DateTime;
import com.databases.example.utils.Money;
import com.databases.example.utils.PlanUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class PlanReceiver extends BroadcastReceiver {
    private final int NOTIFICATION_ID = 0123456;

    public static final String FROM_NOTIFICATION_KEY = "fromNotification";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Timber.v("Notified of boot");
            PlanUtils.reschedulePlans(context);
        } else {
            String name = bundle.getString("plan_name");
            Timber.v(getClass().getSimpleName(), "PlanReceiver received " + name);

            try {
                Plan plan = bundle.getParcelable(PlanUtils.PLAN_ID);
                transactionAdd(context, plan);

                notify(context, bundle);
            } catch (Exception e) {
                Toast.makeText(context, "There was an error somewhere \n e = " + e, Toast.LENGTH_SHORT).show();
                Timber.e("ERROR: " + e);
                e.printStackTrace();
            }
        }
    }

    //For Adding a Transaction
    private void transactionAdd(Context context, Plan plan) {
        final Calendar cal = Calendar.getInstance();
        Locale locale = context.getResources().getConfiguration().locale;
        DateTime date = new DateTime();
        date.setCalendar(cal);

        ContentValues transactionValues = new ContentValues();
        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, plan.acctId);
        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, plan.id);
        transactionValues.put(DatabaseHelper.TRANS_NAME, plan.name);
        transactionValues.put(DatabaseHelper.TRANS_VALUE, plan.value);
        transactionValues.put(DatabaseHelper.TRANS_TYPE, plan.type);
        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, plan.category);
        transactionValues.put(DatabaseHelper.TRANS_MEMO, plan.memo);
        transactionValues.put(DatabaseHelper.TRANS_DATE, date.getSQLDate(locale));
        transactionValues.put(DatabaseHelper.TRANS_TIME, date.getSQLTime(locale));
        transactionValues.put(DatabaseHelper.TRANS_CLEARED, plan.cleared);

        //Insert values into accounts table
        context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void notify(Context context, Bundle bundle) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationCount = 0;
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        String plan_name = bundle.getString("plan_name");
        String plan_value = bundle.getString("plan_value");

        //Intent fired when notification is clicked on
        Intent intent = new Intent(context, CheckbookActivity.class);
        intent.putExtra(FROM_NOTIFICATION_KEY, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get today's readable date
        DateTime today = new DateTime();
        today.setCalendar(Calendar.getInstance());

        //Get Value with correct money format
        Money value = new Money(plan_value);
        value.getNumberFormat(context.getResources().getConfiguration().locale);

        //Add current notification to database of notifications
        ContentValues notificationValues = new ContentValues();
        notificationValues.put(DatabaseHelper.NOT_NAME, plan_name);
        notificationValues.put(DatabaseHelper.NOT_VALUE, plan_value);
        notificationValues.put(DatabaseHelper.NOT_DATE, today.getSQLDate(context.getResources().getConfiguration().locale));
        context.getContentResolver().insert(MyContentProvider.NOTIFICATIONS_URI, notificationValues);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Plan " + plan_name + " Occurred");
        mBuilder.setContentText(value.getNumberFormat(context.getResources().getConfiguration().locale) + " " + today.getReadableDate());
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);

        //Inbox Style
        inboxStyle.setBigContentTitle("Plans:");
        ArrayList<Notification> notifications = Notification.getNotifications(context.getContentResolver().query(Uri.parse(MyContentProvider.NOTIFICATIONS_URI + "/"), null, null, null, null));

        for (Notification notification : notifications) {
            notificationCount++;
            DateTime date = new DateTime();
            date.setStringSQL(notification.date);

            inboxStyle.addLine(notification.name + ": " + notification.value + " " + date.getReadableDate());
        }

        if (notificationCount > 1) {
            mBuilder.setNumber(notificationCount);
        }

        if (notificationCount > 7) {
            inboxStyle.setSummaryText("+" + (notificationCount - 7) + " more");
        }
        mBuilder.setStyle(inboxStyle);

        nm.notify(NOTIFICATION_ID, mBuilder.build());
    }
}