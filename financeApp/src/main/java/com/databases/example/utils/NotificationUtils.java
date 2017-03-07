package com.databases.example.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.databases.example.data.MyContentProvider;

/**
 * Created by kwelsh on 3/5/17.
 */

public class NotificationUtils {

    //Method for clearing notifications if they were clicked on
    public static void clearNotifications(Context context) {
        Log.v("CheckbookActivity", "Clearing notifications...");
        Uri uri = Uri.parse(MyContentProvider.NOTIFICATIONS_URI + "/" + 0);
        context.getContentResolver().delete(uri, null, null);
    }
}
