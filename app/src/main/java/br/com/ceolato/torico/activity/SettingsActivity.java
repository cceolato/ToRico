package br.com.ceolato.torico.activity;


import android.os.Bundle;

import android.preference.PreferenceActivity;

import br.com.ceolato.torico.R;


public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }
}
