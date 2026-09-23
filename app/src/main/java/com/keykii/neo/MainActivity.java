package com.keykii.neo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Keep the existing launcher component while sharing the keyboard's settings hub. */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent settings = new Intent(this, SettingsActivity.class);
        settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(settings);
        finish();
    }
}
