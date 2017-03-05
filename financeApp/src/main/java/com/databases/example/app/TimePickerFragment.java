package com.databases.example.app;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kwelsh on 3/5/17.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    TimePickerInterface timePickerInterface;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar cal = Calendar.getInstance();

        SimpleDateFormat dateFormatHour = new SimpleDateFormat("hh");
        SimpleDateFormat dateFormatMinute = new SimpleDateFormat("mm");

        int hour = Integer.parseInt(dateFormatHour.format(cal.getTime()));
        int minute = Integer.parseInt(dateFormatMinute.format(cal.getTime()));

        return new TimePickerDialog(getActivity(), this, hour, minute,
                false);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(timePickerInterface != null){
            timePickerInterface.onTimeSet(view, hourOfDay, minute);
        }
    }

    public void setTimePickerInterface(TimePickerInterface timePickerInterface){
        this.timePickerInterface = timePickerInterface;
    }
}
