/* Class that handles the BroadcastReceiver needed for Scheduling Transactions
 * Handles rescheduling previous plans if the user reboots the phone 
 */

package com.databases.example.data;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.databases.example.R;
import com.databases.example.app.Checkbook;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class PlanReceiver extends BroadcastReceiver{
    final int NOTIFICATION_ID = 0123456;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("PlanReceiver-onReceive", "Notified of boot");
            reschedulePlans(context);
        }
        else{
            String name = bundle.getString("plan_name");
            Log.d("PlanReceiver-onReceive", "PlanReceiver received " + name);

            try {
                String plan_id = bundle.getString("plan_id");
                String plan_acct_id = bundle.getString("plan_acct_id");
                String plan_name = bundle.getString("plan_name");
                String plan_value = bundle.getString("plan_value");
                String plan_type = bundle.getString("plan_type");
                String plan_category = bundle.getString("plan_category");
                String plan_memo = bundle.getString("plan_memo");
                String plan_offset = bundle.getString("plan_offset");
                String plan_rate = bundle.getString("plan_rate");
                String plan_next = bundle.getString("plan_next");
                String plan_scheduled = bundle.getString("plan_scheduled");
                String plan_cleared = bundle.getString("plan_cleared");

                PlanRecord record = new PlanRecord(plan_id,plan_acct_id, plan_name, plan_value, plan_type, plan_category, plan_memo, plan_offset, plan_rate, plan_next, plan_scheduled, plan_cleared);

                transactionAdd(record,context);

                notify(context, bundle);
            } catch (Exception e) {
                Toast.makeText(context, "There was an error somewhere \n e = " + e, Toast.LENGTH_SHORT).show();
                Log.e("onReceive", "ERROR: " + e);
                e.printStackTrace();
            }

        }

    }

    //For Adding a Transaction
    public void transactionAdd(PlanRecord plan, Context context){
        final Calendar cal = Calendar.getInstance();
        Locale locale=null;
        DateTime date = new DateTime();
        date.setCalendar(cal);

        ContentValues transactionValues=new ContentValues();
        transactionValues.put(DatabaseHelper.TRANS_ACCT_ID, plan.acctId);
        transactionValues.put(DatabaseHelper.TRANS_PLAN_ID, plan.id);
        transactionValues.put(DatabaseHelper.TRANS_NAME, plan.name);
        transactionValues.put(DatabaseHelper.TRANS_VALUE, plan.value);
        transactionValues.put(DatabaseHelper.TRANS_TYPE, plan.type);
        transactionValues.put(DatabaseHelper.TRANS_CATEGORY, plan.category);
        transactionValues.put(DatabaseHelper.TRANS_MEMO, plan.memo);
        transactionValues.put(DatabaseHelper.TRANS_TIME, date.getSQLTime(locale));
        transactionValues.put(DatabaseHelper.TRANS_DATE, date.getSQLDate(locale));
        transactionValues.put(DatabaseHelper.TRANS_CLEARED, plan.cleared);

        //Insert values into accounts table
        context.getContentResolver().insert(MyContentProvider.TRANSACTIONS_URI, transactionValues);
    }//end of transactionAdd


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void notify(Context context, Bundle bundle) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationCount=0;
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        String plan_name = bundle.getString("plan_name");
        String plan_value = bundle.getString("plan_value");

        //Intent fired when notification is clicked on
        Intent intent = new Intent(context,Checkbook.class);
        intent.putExtra("fromNotification", true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get today's readable date
        DateTime today = new DateTime();
        today.setCalendar(Calendar.getInstance());

        //Get Value with correct money format
        Money value = new Money(plan_value);
        value.getNumberFormat(context.getResources().getConfiguration().locale);

        //Add current notification to database of notifications
        ContentValues notificationValues = new ContentValues();
        notificationValues.put(DatabaseHelper.NOT_NAME,plan_name);
        notificationValues.put(DatabaseHelper.NOT_VALUE,plan_value);
        notificationValues.put(DatabaseHelper.NOT_DATE,today.getSQLDate(context.getResources().getConfiguration().locale));
        context.getContentResolver().insert(MyContentProvider.NOTIFICATIONS_URI, notificationValues);

        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Plan " + plan_name + " Occured");
        mBuilder.setContentText(value.getNumberFormat(context.getResources().getConfiguration().locale) + " " + today.getReadableDate());
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);

        //Inbox Style
        inboxStyle.setBigContentTitle("Plans:");
        Cursor notifications = context.getContentResolver().query(Uri.parse(MyContentProvider.NOTIFICATIONS_URI+"/"), null, null, null, null);

        while (notifications.moveToNext()){
            notificationCount++;
            String notification_name = notifications.getString(1);
            String notification_value = notifications.getString(2);
            String notification_date = notifications.getString(3);
            DateTime date = new DateTime();
            date.setStringSQL(notification_date);

            inboxStyle.addLine(notification_name + ": " + notification_value + " " + date.getReadableDate());
        }

        if(notificationCount>1){
            mBuilder.setNumber(notificationCount);
        }

        if(notificationCount>7){
            inboxStyle.setSummaryText("+" + (notificationCount-7) + " more");
        }
        mBuilder.setStyle(inboxStyle);

        nm.notify(NOTIFICATION_ID, mBuilder.build());
    }

    //Method that remakes the planned transaction
    public void reschedulePlans(Context context){
        Cursor cursorPlans = context.getContentResolver().query(Uri.parse(MyContentProvider.PLANS_URI+"/"), null, null, null, null);

        //startManagingCursor(cursorPlans);
        int columnID = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_ID);
        int columnToID = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_ACCT_ID);
        int columnName = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_NAME);
        int columnValue = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_VALUE);
        int columnType = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_TYPE);
        int columnCategory = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_CATEGORY);
        int columnMemo = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_MEMO);
        int columnOffset = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_OFFSET);
        int columnRate = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_RATE);
        int columnNext = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_NEXT);
        int columnScheduled = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED);
        int columnCleared = cursorPlans.getColumnIndex(DatabaseHelper.PLAN_CLEARED);

        cursorPlans.moveToFirst();
        if (cursorPlans != null) {
            if (cursorPlans.isFirst()) {
                do {

                    String id = cursorPlans.getString(0);
                    String to_id = cursorPlans.getString(columnToID);
                    String name = cursorPlans.getString(columnName);
                    String value = cursorPlans.getString(columnValue);
                    String type = cursorPlans.getString(columnType);
                    String category = cursorPlans.getString(columnCategory);
                    String memo = cursorPlans.getString(columnMemo);
                    String offset = cursorPlans.getString(columnOffset);
                    String rate = cursorPlans.getString(columnRate);
                    String next = cursorPlans.getString(columnNext);
                    String scheduled = cursorPlans.getString(columnScheduled);
                    String cleared = cursorPlans.getString(columnCleared);

                    /****RESET ALARMS HERE****/
                    Log.d("PlanReceiver-reschedulePlans", "rescheduling " + id + to_id + name + value + type + category + memo + offset + rate + cleared);
                    final PlanRecord record = new PlanRecord(id,to_id,name,value,type,category,memo,offset,rate,next,scheduled,cleared);
                    schedule(record,context);

                } while (cursorPlans.moveToNext());
            }

            else {
                //No Results Found
                Log.d("PlanReceiver-reschedulePlans", "No Plans to reschedule");
            }
        }

    }

    //Re-Hash of the schedule method of Plans.java
    private void schedule(PlanRecord plan, Context context) {
        PlanRecord record = plan;
        Date d = null;

        try {
            DateTime test = new DateTime();
            test.setStringSQL(record.offset);
            d = test.getYearMonthDay();
        }catch (java.text.ParseException e) {
            Log.e("PlanReceiver-schedule", "Couldn't schedule " + record.name + "\n e:"+e);
            e.printStackTrace();
        }

        Log.e("PlanReceiver-schedule", "d.year=" + (d.getYear()+1900) + " d.date=" + d.getDate() + " d.month=" + d.getMonth());

        Calendar firstRun = new GregorianCalendar(d.getYear()+1900,d.getMonth(),d.getDate());
        Log.e("PlanReceiver-schedule", "FirstRun:" + firstRun);

        Intent intent = new Intent(context, PlanReceiver.class);
        intent.putExtra("plan_id", record.id);
        intent.putExtra("plan_acct_id",record.acctId);
        intent.putExtra("plan_name",record.name);
        intent.putExtra("plan_value",record.value);
        intent.putExtra("plan_type",record.type);
        intent.putExtra("plan_category",record.category);
        intent.putExtra("plan_memo",record.memo);
        intent.putExtra("plan_offset",record.offset);
        intent.putExtra("plan_rate",record.rate);
        intent.putExtra("plan_next",record.next);
        intent.putExtra("plan_scheduled",record.scheduled);
        intent.putExtra("plan_cleared",record.cleared);

        //Parse Rate (token 0 is amount, token 1 is type)
        final String phrase = record.rate;
        final String delims = "[ ]+";
        final String[] tokens = phrase.split(delims);

        final PendingIntent sender = PendingIntent.getBroadcast(context, Integer.parseInt(record.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get the AlarmManager service
        final AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        final Locale locale=context.getResources().getConfiguration().locale;
        final DateTime nextRun = new DateTime();

        if(tokens[1].contains("Days")){
            Log.d("PlanReceiver-schedule", "Days");

            //If Starting Time is in the past, fire off next day(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[0]));
            }

            Log.d("PlanReceiver-schedule", "firstRun is " + firstRun);

            nextRun.setCalendar(firstRun);

            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
            context.getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI+"/"+record.id), planValues, DatabaseHelper.PLAN_ID+"="+record.id, null);

            am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_DAY), sender);
        }
        else if(tokens[1].contains("Weeks")){
            Log.d("PlanReceiver-schedule", "Weeks");

            //If Starting Time is in the past, fire off next week(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(tokens[0]));
            }

            Log.d("PlanReceiver-schedule", "firstRun is " + firstRun);

            nextRun.setCalendar(firstRun);

            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
            context.getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI+"/"+record.id), planValues, DatabaseHelper.PLAN_ID+"="+record.id, null);

            am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_DAY)*7, sender);
        }
        else if(tokens[1].contains("Months")){
            Log.d("PlanReceiver-schedule", "Months");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(cal.getTimeInMillis());
            cal.add(Calendar.MONTH, Integer.parseInt(tokens[0]));

            //If Starting Time is in the past, fire off next month(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.MONTH, Integer.parseInt(tokens[0]));
            }

            Log.d("PlanReceiver-schedule", "firstRun is " + firstRun);

            nextRun.setCalendar(firstRun);

            ContentValues planValues = new ContentValues();
            planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
            context.getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI+"/"+record.id), planValues, DatabaseHelper.PLAN_ID+"="+record.id, null);

            am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), cal.getTimeInMillis(), sender);
        }
        else{
            Log.e("PlanReceiver-schedule", "Could not set alarm; Something wrong with the rate");
        }

    }

    public class PlanRecord {
        protected String id;
        protected String acctId;
        protected String name;
        protected String value;
        protected String type;
        protected String category;
        protected String memo;
        protected String offset;
        protected String rate;
        protected String next;
        protected String scheduled;
        protected String cleared;

        public PlanRecord(String id, String acctId, String name, String value, String type, String category, String memo, String offset, String rate, String next, String scheduled, String cleared) {
            this.id = id;
            this.acctId = acctId;
            this.name = name;
            this.value = value;
            this.type = type;
            this.category = category;
            this.memo = memo;
            this.offset = offset;
            this.rate = rate;
            this.next = next;
            this.scheduled = scheduled;
            this.cleared = cleared;
        }
    }

}//end of PlanReceiver