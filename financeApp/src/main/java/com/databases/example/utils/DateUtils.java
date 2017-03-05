package com.databases.example.utils;

import android.support.v7.app.AppCompatActivity;

import com.databases.example.app.DatePickerFragment;
import com.databases.example.app.DatePickerInterface;
import com.databases.example.app.TimePickerFragment;
import com.databases.example.app.TimePickerInterface;

/**
 * Created by kwelsh on 3/5/17.
 */

public class DateUtils {
    private static final String TIME_PICKER_TAG = "timePicker";
    private static final String DATE_PICKER_TAG = "datePicker";


    //Method for selecting a Time when adding a transaction
    public static void showTimePickerDialog(AppCompatActivity appCompatActivity, TimePickerInterface timePickerInterface) {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setTimePickerInterface(timePickerInterface);

        newFragment.show(appCompatActivity.getSupportFragmentManager(), TIME_PICKER_TAG);
    }

    //Method for selecting a Date when adding a transaction
    public static void showDatePickerDialog(AppCompatActivity appCompatActivity, DatePickerInterface datePickerInterface) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setDatePickerInterface(datePickerInterface);

        newFragment.show(appCompatActivity.getSupportFragmentManager(), DATE_PICKER_TAG);
    }
}
