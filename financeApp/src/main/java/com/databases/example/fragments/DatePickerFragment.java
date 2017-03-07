package com.databases.example.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kwelsh on 3/5/17.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    DatePickerInterface datePickerInterface;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar cal = Calendar.getInstance();

        SimpleDateFormat dateFormatYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MM");
        SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd");

        int year = Integer.parseInt(dateFormatYear.format(cal.getTime()));
        int month = Integer.parseInt(dateFormatMonth.format(cal.getTime())) - 1;
        int day = Integer.parseInt(dateFormatDay.format(cal.getTime()));

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (datePickerInterface != null) {
            datePickerInterface.onDateSet(view, year, month, day);
        }
    }

    public void setDatePickerInterface(DatePickerInterface datePickerInterface) {
        this.datePickerInterface = datePickerInterface;
    }
}
