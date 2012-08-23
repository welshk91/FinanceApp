package com.databases.example;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

public class Options extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.options);
		//GetOptions();
	}


	private void GetOptions()
	{
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String sDefaultTipPercent = prefs.getString(this.getString(R.string.pref_key_transaction_backgroundColor), "#E8E8E8");
		String sDefaultTaxPercent = prefs.getString(this.getString(R.string.pref_key_transaction_nameSize), "20");

		//Toast.makeText(this, "Options\n" + sDefaultTipPercent + sDefaultTaxPercent + sDefaultExtraAmount, Toast.LENGTH_LONG).show();

	}

}



