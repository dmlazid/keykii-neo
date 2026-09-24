package com.keykii.neo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class VoicePermissionActivity extends Activity {

    private static final int REQUEST_MIC=2283;
    private boolean resultSent=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(
            android.os.Build.VERSION.SDK_INT<23 ||
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            )==PackageManager.PERMISSION_GRANTED
        ) {
            sendResult(true);
            finishWithoutAnimation();
            return;
        }

        requestPermissions(
            new String[]{
                Manifest.permission.RECORD_AUDIO
            },
            REQUEST_MIC
        );
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        );

        if(requestCode!=REQUEST_MIC)
            return;

        boolean granted=
            grantResults.length>0 &&
            grantResults[0]==
                PackageManager.PERMISSION_GRANTED;

        sendResult(granted);
        finishWithoutAnimation();
    }

    @Override
    protected void onDestroy() {
        if(!resultSent) {
            boolean granted=
                android.os.Build.VERSION.SDK_INT<23 ||
                checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
                )==PackageManager.PERMISSION_GRANTED;

            sendResult(granted);
        }

        super.onDestroy();
    }

    private void sendResult(boolean granted) {
        if(resultSent)
            return;

        resultSent=true;

        Intent result=
            new Intent(
                "com.keykii.neo.VOICE_PERMISSION_RESULT"
            );

        result.setPackage(
            getPackageName()
        );

        result.putExtra(
            "granted",
            granted
        );

        sendBroadcast(result);
    }

    private void finishWithoutAnimation() {
        finish();
        overridePendingTransition(0,0);
    }
}
