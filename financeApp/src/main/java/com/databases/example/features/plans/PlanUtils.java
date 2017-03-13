package com.databases.example.features.plans;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.databases.example.database.DatabaseHelper;
import com.databases.example.database.MyContentProvider;
import com.databases.example.database.PlanReceiver;
import com.databases.example.utils.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by kwelsh on 3/11/17.
 */

public class PlanUtils {
    public static final String PLAN_ID = "plan_id";

    public static boolean cancelPlan(Context context, Plan plan) {
        Intent intent = new Intent(context, PlanReceiver.class);
        intent.putExtra(PLAN_ID, plan);
        PendingIntent sender = PendingIntent.getBroadcast(context, plan.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            am.cancel(sender);
            Toast.makeText(context, "Canceled plan:\n" + plan.name, Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "Error canceling plan", Toast.LENGTH_SHORT).show();
            Timber.e("AlarmManager update was not canceled. " + e.toString());
            return false;
        }
    }

    public static boolean schedule(Context context, Plan plan) {
        Date d = null;

        try {
            DateTime test = new DateTime();
            test.setStringSQL(plan.offset);
            d = test.getYearMonthDay();
        } catch (java.text.ParseException e) {
            Timber.e("Couldn't schedule " + plan.name + "\n e:" + e);
        }

        Timber.d("d.year=" + (d.getYear() + 1900) + " d.date=" + d.getDate() + " d.month=" + d.getMonth());

        Calendar firstRun = new GregorianCalendar(d.getYear() + 1900, d.getMonth(), d.getDate());
        Timber.d("FirstRun:" + firstRun);

        Intent intent = new Intent(context, PlanReceiver.class);
        intent.putExtra(PLAN_ID, plan);

        //Parse Rate (token 0 is amount, token 1 is type)
        String phrase = plan.rate;
        String delims = "[ ]+";
        String[] tokens = phrase.split(delims);

        final PendingIntent sender = PendingIntent.getBroadcast(context, plan.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        final Locale locale = context.getResources().getConfiguration().locale;
        final DateTime nextRun = new DateTime();

        if (tokens[1].contains("Days")) {
            Timber.v("Days");

            //If Starting Time is in the past, fire off next day(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[0]));
            }

            Timber.d("firstRun is " + firstRun);
            nextRun.setCalendar(firstRun);

            try {
                am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0]) * AlarmManager.INTERVAL_DAY), sender);

                ContentValues planValues = new ContentValues();
                planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
                context.getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), planValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);

                Toast.makeText(context, "Next Transaction scheduled for " + nextRun.getReadableDate(), Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception e) {
                Toast.makeText(context, "Could not schedule plan", Toast.LENGTH_LONG).show();
                Timber.e("Could not set alarm.", e);
                return false;
            }
        } else if (tokens[1].contains("Weeks")) {
            Timber.v("Weeks");

            //If Starting Time is in the past, fire off next week(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(tokens[0]));
            }

            Timber.d("firstRun is " + firstRun);
            nextRun.setCalendar(firstRun);

            try {
                am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), (Integer.parseInt(tokens[0]) * AlarmManager.INTERVAL_DAY) * 7, sender);

                ContentValues planValues = new ContentValues();
                planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
                context.getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), planValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);

                Toast.makeText(context, "Next Transaction scheduled for " + nextRun.getReadableDate(), Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception e) {
                Toast.makeText(context, "Could not schedule plan", Toast.LENGTH_LONG).show();
                Timber.e("Could not set alarm.", e);
                return false;
            }
        } else if (tokens[1].contains("Months")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(cal.getTimeInMillis());
            cal.add(Calendar.MONTH, Integer.parseInt(tokens[0]));

            //If Starting Time is in the past, fire off next month(s)
            while (firstRun.before(Calendar.getInstance())) {
                firstRun.add(Calendar.MONTH, Integer.parseInt(tokens[0]));
            }

            Timber.d("firstRun is " + firstRun);
            nextRun.setCalendar(firstRun);

            try {
                am.setRepeating(AlarmManager.RTC_WAKEUP, firstRun.getTimeInMillis(), cal.getTimeInMillis(), sender);

                ContentValues planValues = new ContentValues();
                planValues.put(DatabaseHelper.PLAN_NEXT, nextRun.getSQLDate(locale));
                context.getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), planValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);

                Toast.makeText(context, "Next Transaction scheduled for " + nextRun.getReadableDate(), Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception e) {
                Toast.makeText(context, "Could not schedule plan", Toast.LENGTH_LONG).show();
                Timber.e("Could not set alarm.", e);
                return false;
            }
        } else {
            Toast.makeText(context, "Could not schedule plan", Toast.LENGTH_LONG).show();
            Timber.e("Could not set alarm; Something wrong with the rate");
            return false;
        }
    }

    public static void togglePlan(Context context, Plan plan) {
        try {
            if (plan.scheduled.equals("true")) {
                if (PlanUtils.cancelPlan(context, plan)) {
                    ContentValues transactionValues = new ContentValues();
                    transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "false");
                    context.getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), transactionValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);
                }
            } else {
                if (PlanUtils.schedule(context, plan)) {
                    ContentValues transactionValues = new ContentValues();
                    transactionValues.put(DatabaseHelper.PLAN_SCHEDULED, "true");
                    context.getContentResolver().update(Uri.parse(MyContentProvider.PLANS_URI + "/" + plan.id), transactionValues, DatabaseHelper.PLAN_ID + "=" + plan.id, null);
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error toggling plan \n" + plan.name, Toast.LENGTH_SHORT).show();
            Timber.e("Error toggling a plan. " + e.toString());
        }
    }

    public static boolean reschedulePlans(Context context) {
        ArrayList<Plan> plans = Plan.getPlans(context.getContentResolver()
                .query(Uri.parse(MyContentProvider.PLANS_URI + "/"), null, null, null, null));

        boolean noErrors = true;
        for (Plan plan : plans) {
            Timber.d("rescheduling " + plan.toString());
            if (!PlanUtils.schedule(context, plan)) {
                noErrors = false;
            }
        }

        return noErrors;
    }
}
