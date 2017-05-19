package com.production.sidorov.ivan.tabata.preferences;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.production.sidorov.ivan.tabata.R;

/**
 * Created by Иван on 17.05.2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_screen);
    }
}
