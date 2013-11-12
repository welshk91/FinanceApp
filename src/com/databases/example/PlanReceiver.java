/* Class that handles the BroadcastReceiver needed for Scheduling Transactions
 * Handles rescheduling previous plans if the user reboots the phone 
 */

package com.databases.example;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.databases.example.Plans.PlanRecord;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

public class PlanReceiver extends BroadcastReceiver{	
	private static DatabaseHelper dh = null;

	//Date Format to use for date (03-26-2013)
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");		

	//Date Format to use for time (01:42 PM)
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		dh = new DatabaseHelper(context);

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
				String plan_cleared = bundle.getString("plan_cleared");

				PlanRecord record = new PlanRecord(plan_id,plan_acct_id, plan_name, plan_value, plan_type, plan_category, plan_memo, plan_offset, plan_rate,plan_cleared);

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
		String time = timeFormat.format(cal.getTime());
		String date = dateFormat.format(cal.getTime());

		ContentValues transactionValues=new ContentValues();
		transactionValues.put("ToAcctID", plan.acctId);
		transactionValues.put("ToPlanID", plan.id);
		transactionValues.put("TransName", plan.name);
		transactionValues.put("TransValue", plan.value);
		transactionValues.put("TransType", plan.type);
		transactionValues.put("TransCategory", plan.category);
		transactionValues.put("TransMemo", plan.memo);
		transactionValues.put("TransTime", time);
		transactionValues.put("TransDate", date);
		transactionValues.put("TransCleared", plan.cleared);

		//Insert values into accounts table
		dh.addTransaction(transactionValues);							
	}//end of transactionAdd


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void notify(Context context, Bundle bundle) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence from = "Welsh Finances";
		CharSequence message = "Sample Notification text here...";

		String plan_id = bundle.getString("plan_id");
		String plan_acct_id = bundle.getString("plan_acct_id");
		String plan_name = bundle.getString("plan_name");
		String plan_value = bundle.getString("plan_value");
		String plan_type = bundle.getString("plan_type");
		String plan_category = bundle.getString("plan_category");
		String plan_memo = bundle.getString("plan_memo");
		String plan_offset = bundle.getString("plan_offset");
		String plan_rate = bundle.getString("plan_rate");
		String plan_cleared = bundle.getString("plan_cleared");

		//Intent fired when notification is clicked on
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context,Checkbook.class), 0);

		Calendar cal = Calendar.getInstance();

		Notification notification = new NotificationCompat.Builder(context).
				setContentTitle(from+ ": " + plan_name)
				.setContentText(plan_id + " " + plan_name + " " + plan_value + " " + plan_offset + " " + plan_rate)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(contentIntent)
				.build();

		//Notification's Big View
		if (Build.VERSION.SDK_INT > 15){
			RemoteViews customNotifView = new RemoteViews("com.databases.example", 
					R.layout.notification_big);
			customNotifView.setTextViewText(R.id.TextNotification, plan_id + " " + plan_name + " " + plan_value + " " + plan_offset + " " + plan_rate + "\n Fired on " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) +":" + cal.get(Calendar.SECOND));

			notification.bigContentView = customNotifView;
		}

		//notification.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(Integer.parseInt(plan_id), notification);

	}

	//Method that remakes the planned transaction
	public void reschedulePlans(Context context){
		Cursor cursorPlans = dh.getPlannedTransactionsAll();

		//startManagingCursor(cursorPlans);
		int IDColumn = cursorPlans.getColumnIndex("PlanID");
		int ToIDColumn = cursorPlans.getColumnIndex("ToAcctID");
		int NameColumn = cursorPlans.getColumnIndex("PlanName");
		int ValueColumn = cursorPlans.getColumnIndex("PlanValue");
		int TypeColumn = cursorPlans.getColumnIndex("PlanType");
		int CategoryColumn = cursorPlans.getColumnIndex("PlanCategory");
		int MemoColumn = cursorPlans.getColumnIndex("PlanMemo");
		int OffsetColumn = cursorPlans.getColumnIndex("PlanOffset");
		int RateColumn = cursorPlans.getColumnIndex("PlanRate");
		int ClearedColumn = cursorPlans.getColumnIndex("PlanCleared");

		cursorPlans.moveToFirst();
		if (cursorPlans != null) {
			if (cursorPlans.isFirst()) {
				do {

					String id = cursorPlans.getString(0);
					String to_id = cursorPlans.getString(ToIDColumn);
					String name = cursorPlans.getString(NameColumn);
					String value = cursorPlans.getString(ValueColumn);
					String type = cursorPlans.getString(TypeColumn);
					String category = cursorPlans.getString(CategoryColumn);
					String memo = cursorPlans.getString(MemoColumn);
					String offset = cursorPlans.getString(OffsetColumn);
					String rate = cursorPlans.getString(RateColumn);
					String cleared = cursorPlans.getString(ClearedColumn);

					/****RESET ALARMS HERE****/
					Log.e("PlanReceiver-reschedulePlans", "rescheduling " + id + to_id + name + value + type + category + memo + offset + rate + cleared);
					PlanRecord record = new PlanRecord(id,to_id,name,value,type,category,memo,offset,rate,cleared);
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
			d = dateFormat.parse(record.offset);
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
		intent.putExtra("plan_cleared",record.cleared);

		//Parse Rate (token 0 is amount, token 1 is type)
		String phrase = record.rate;
		String delims = "[ ]+";
		String[] tokens = phrase.split(delims);

		// In reality, you would want to have a static variable for the request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(context, Integer.parseInt(record.id), intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);

		if(tokens[1].contains("Days")){
			Log.d("PlanReceiver-schedule", "Days");

			//If Starting Time is in the past, fire off next month(s)
			while (firstRun.before(Calendar.getInstance())) {
				firstRun.add(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[0]));
			}

			Log.d("PlanReceiver-schedule", "firstRun is " + firstRun);
			am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0])*AlarmManager.INTERVAL_DAY), sender);
		}
		else if(tokens[1].contains("Weeks")){
			Log.d("PlanReceiver-schedule", "Weeks");

			//If Starting Time is in the past, fire off next month(s)
			while (firstRun.before(Calendar.getInstance())) {
				firstRun.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(tokens[0]));
			}

			Log.d("PlanReceiver-schedule", "firstRun is " + firstRun);
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
			am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), cal.getTimeInMillis(), sender);
		}
		else{
			Log.e("PlanReceiver-schedule", "Could not set alarm; Something wrong with the rate");
		}

		//am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 1000*6, sender);
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
		protected String cleared;

		public PlanRecord(String id, String acctId, String name, String value, String type, String category, String memo, String offset, String rate, String cleared) {
			this.id = id;
			this.acctId = acctId;
			this.name = name;
			this.value = value;
			this.type = type;
			this.category = category;
			this.memo = memo;
			this.offset = offset;
			this.rate = rate;
			this.cleared = cleared;
		}
	}

}//end of PlanReceiver