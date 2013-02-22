package com.databases.example;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class PlanReceiver extends BroadcastReceiver{

	public final String dbFinance = "dbFinance";
	public SQLiteDatabase myDB = null;
	private SliderMenu menu;

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			String message = bundle.getString("alarm_message");
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			notify(context);
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			Log.e("onReceive", "ERROR: " + e);
			e.printStackTrace();
		}

	}

	public void notify(Context context) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence from = "Welsh Finances";
		CharSequence message = "Sample Notification text here...";
		
		//Intent fired when notification is clicked on
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context,Checkbook.class), 0);
		
		Notification notification = new NotificationCompat.Builder(context).
				setContentTitle(from)
				.setContentText(message)
				.setSmallIcon(R.drawable.calculator)
				.setContentIntent(contentIntent)
				.build();
		
		//notification.flags |= Notification.FLAG_AUTO_CANCEL;
		nm.notify(0, notification);
		
	}


}//end of PlanReceiver