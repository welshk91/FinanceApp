package com.databases.example.fragments;

import android.widget.TimePicker;

/**
 * Created by kwelsh on 3/5/17.
 */

public interface TimePickerInterface {
    void onTimeSet(TimePicker view, int hourOfDay, int minute);
}