package com.fido.egistec.yukeyring;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Administrator on 2016/8/18.
 */
public class InitActivity extends Activity {
    private static final String LOG_TAG = InitActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("key", MODE_PRIVATE);
        if (sp.getBoolean("has_key", false)) {
            Log.e("ac", "verify");
            doVerifyKey();
        } else {
            Log.e("ac", "main");
            doMain();
        }
    }


    private void doVerifyKey() {
        try {
            startActivity(new Intent(InitActivity.this, InitVerifyActivity.class));
        } catch (ActivityNotFoundException activityNotFound) {
            Log.e(LOG_TAG, "VerifyKeyActivity is not found");
        }
    }

    private void doMain() {
        try {
            startActivity(new Intent(InitActivity.this, MainActivity.class));
        } catch (ActivityNotFoundException activityNotFound) {
            Log.e(LOG_TAG, "VerifyKeyActivity is not found");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
