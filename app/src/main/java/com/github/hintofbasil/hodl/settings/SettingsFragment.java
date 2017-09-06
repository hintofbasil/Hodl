package com.github.hintofbasil.hodl.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.github.hintofbasil.hodl.R;

/**
 * Created by will on 9/6/17.
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
