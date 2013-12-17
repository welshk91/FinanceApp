/* Class that handles how dates are represented
 * Mostly just formats dates so that sqlite can sort by time
 */

package com.databases.example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class DateTime{
	protected Date dateSQL;
	protected Date dateReadable;

	protected String stringSQL;
	protected String stringReadable;

	protected Date date;
	protected Calendar cal;

	//Time Format to use for user (01:42 PM)
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

	//Date Format to use for user (03-26-2013)
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");		

	//Time Format to use for sql
	private final static SimpleDateFormat timeSQLFormat = new SimpleDateFormat("HH:mm");

	//Date Format to use for sql
	private final static SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd");		

	public DateTime(){
	}

	public void setDateSQL(Date dateSQL){
		this.dateSQL=dateSQL;
	}

	public void setDateReadable(Date dateReadable){
		this.dateReadable=dateReadable;
	}

	public void setStringSQL(String stringSQL){
		this.stringSQL=stringSQL;
	}

	public void setStringReadable(String stringReadable){
		this.stringReadable=stringReadable;
	}

	public void setDate(Date date){
		this.date=date;
	}

	public void setCalendar(Calendar cal){
		this.cal=cal;
	}

	public String getReadableDate(){
		String newDate = null;

		if(stringReadable!=null){
			return stringReadable;
		}

		else if(cal!=null){
			return dateFormat.format(cal.getTime());
		}

		else{
			try {
				Date oldDate = dateSQLFormat.parse(stringSQL);
				newDate = dateFormat.format(oldDate);
			} catch (Exception e) {
				Log.e("DateTime-getReadableDate","Error parsing readable date("+stringSQL+")!");
				e.printStackTrace();
			}
			return newDate;
		}
	}

	public String getReadableTime(){
		String newTime = null;		

		if(stringReadable!=null){
			return stringReadable;
		}
		
		else if(cal!=null){
			return timeFormat.format(cal.getTime());
		}
		
		else{
			try {
				Date oldTime = timeSQLFormat.parse(stringSQL);
				newTime = timeFormat.format(oldTime);
			} catch (Exception e) {
				Log.e("DateTime-getReadableTime","Error parsing readable time("+stringSQL+")!");
				e.printStackTrace();
			}

			return newTime;
		}
	}

	public String getSQLDate(Locale l){
		if(stringSQL!=null){
			return stringSQL;
		}
		
		else if(cal!=null){
			return dateSQLFormat.format(cal.getTime());
		}

		else if(date!=null){
			String d = dateSQLFormat.format(date);
			return d;	
		}
		else{
			String newDate = null;
			try {
				Date oldDate = dateFormat.parse(stringReadable);
				newDate = dateSQLFormat.format(oldDate);
			} catch (ParseException e) {
				Log.e("DateTime-getSQLDate","Error parsing date("+stringReadable+")!");
				e.printStackTrace();
			}

			return newDate;
		}
	}

	public String getSQLTime(Locale l){
		if(stringSQL!=null){
			return stringSQL;
		}

		else if(cal!=null){
			return timeSQLFormat.format(cal.getTime());
		}
		
		else if(date!=null){
			String t = timeSQLFormat.format(date);
			return t;			
		}
		else{
			String newTime = null;
			try {
				Date oldTime = timeFormat.parse(stringReadable);
				newTime = timeSQLFormat.format(oldTime);
			} catch (ParseException e) {
				Log.e("DateTime-getSQLTime","Error parsing date("+stringReadable+")!");
				e.printStackTrace();
			}

			return newTime;			
		}
	}

	public Date getYearMonthDay() throws ParseException{
		return dateSQLFormat.parse(stringSQL);
	}

}//End DateTime