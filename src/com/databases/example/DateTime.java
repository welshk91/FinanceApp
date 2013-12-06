/* Class that handles how dates are represented
 * Mostly just formats dates so that sqlite can sort by time
 */

package com.databases.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class DateTime{
	protected Date date;
	protected String stringDate;

	//Time Format to use for user (01:42 PM)
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

	//Date Format to use for user (03-26-2013)
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");		

	//Time Format to use for sql
	private final static SimpleDateFormat timeSQLFormat = new SimpleDateFormat("HH:mm");

	//Date Format to use for sql
	private final static SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");		

	public DateTime(Date date){
		this.date=date;
	}

	public DateTime(String stringDate){
		this.stringDate=stringDate;
	}

	public String getReadableDate(){
		String newDate = null;
		try {
			Date oldDate = dateSQLFormat.parse(stringDate);
			newDate = dateFormat.format(oldDate);
		} catch (Exception e) {
			Log.e("DateTime-getReadableDate","Error parsing readable date("+stringDate+")!");
			e.printStackTrace();
		}

		return newDate;
	}

	public String getReadableTime(){
		String newTime = null;
		try {
			Date oldTime = timeSQLFormat.parse(stringDate);
			newTime = timeFormat.format(oldTime);
		} catch (Exception e) {
			Log.e("DateTime-getReadableTime","Error parsing readable time("+stringDate+")!");
			e.printStackTrace();
		}

		return newTime;
	}

	public String getSQLDate(Locale l){
		String d = dateSQLFormat.format(date);
		return d;
	}

	public String getSQLTime(Locale l){
		String t = timeSQLFormat.format(date);
		return t;
	}

}//End DateTime