package com.github.hintofbasil.hodl.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by will on 9/6/17.
 */

public class SettingsActivity extends Activity {

    public static final String DISPLAY_CURRENCY = "preferences_currency";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
