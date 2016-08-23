package com.fido.egistec.yukeyring;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.fido.egistec.fpservice.Command;
import com.fido.egistec.fpservice.FingerOpCallback;
import com.fido.egistec.fpservice.FingerPowerManage;
import com.fido.egistec.fpservice.OnFingerDeleteListener;
import com.fido.egistec.fpservice.YouKeyUWrapper;


public class MainActivity extends Activity implements OnFingerDeleteListener, FingerOpCallback {

    private final String LOG_TAG = MainActivity.class.getName();
    private Button createKeybtn;
    private Button verifyKeybtn;
    private long mLastClickTime = SystemClock.elapsedRealtime();

    static public boolean bHasKey = false;
    static public boolean bKeyVerified = false;

    private YouKeyUWrapper mUkey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUkey = YouKeyUWrapper.getInstance();
        mUkey.registOnFingerDelete(this);
        mUkey.registFingerOpCallBack(this);
        createKeybtn = (Button) findViewById(R.id.createkey);
        createKeybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                doCreateKey();
            }
        });

        verifyKeybtn = (Button) findViewById(R.id.verifykey);
        verifyKeybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                doVerifyKey();
            }
        });

        SharedPreferences sp = getSharedPreferences("key", MODE_PRIVATE);
        if (sp.getBoolean("has_key", false)) {
            MainActivity.bHasKey = true;
        } else {
            MainActivity.bHasKey = false;
        }

        if (sp.getBoolean("key_verified", false)) {
            MainActivity.bKeyVerified = true;
        } else {
            MainActivity.bKeyVerified = false;
        }
        doUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUkey.sendCommand(Command.REQUEST_CANCEL);
        doUpdate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_remove_key) {
            mUkey.sendCommand(Command.REQUEST_DELETE);
        } else if (id == R.id.action_get_cos_version) {
            mUkey.sendCommand(Command.REQUEST_GET_COS_VERSION);
        } else if (id == R.id.action_calibration) {
            mUkey.sendCommand(Command.REQUEST_CALIBRATION);
        } else if (id == R.id.action_get_config) {
            mUkey.sendCommand(Command.REQUEST_GET_CONFIHURATION);
        } else if (id == R.id.action_cancel) {
            mUkey.sendCommand(Command.REQUEST_CANCEL);
        }/*else if (id == R.id.action_set_config) {
            mUkey.sendCommand(Command.REQUEST_SET_CONFIGURATION);
        } else if (id == R.id.action_set_detect_mode) {
            mUkey.sendCommand(Command.REQUEST_SET_DETECT_MODE);
        }*/
        doUpdate();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (CreateKeyActivity.CREATE_KEY_REQ == requestCode) {
            if (resultCode == RESULT_OK) {
                this.bHasKey = true;
                this.bKeyVerified = true;
                SharedPreferences sp = getSharedPreferences("key", MODE_PRIVATE);
                sp.edit().putBoolean("has_key", true).commit();
                sp.edit().putBoolean("key_verified", true).commit();
            }
        } else if (VerifyKeyActivity.VERIFY_KEY_REQ == requestCode) {
            if (resultCode == RESULT_OK) {
                this.bKeyVerified = true;
                SharedPreferences sp = getSharedPreferences("key", MODE_PRIVATE);
                sp.edit().putBoolean("key_verified", true).commit();
            }
        } else {
            Log.e(LOG_TAG, "no support request code");
        }
    }

    private void doCreateKey() {
        try {
            startActivityForResult(new Intent(MainActivity.this, CreateKeyActivity.class),
                    CreateKeyActivity.CREATE_KEY_REQ);
        } catch (ActivityNotFoundException activityNotFound) {
            Log.e(LOG_TAG, "CreateKeyActivity is not found");
        }
    }

    private void doVerifyKey() {
        try {
            startActivityForResult(new Intent(MainActivity.this, VerifyKeyActivity.class),
                    VerifyKeyActivity.VERIFY_KEY_REQ);
        } catch (ActivityNotFoundException activityNotFound) {
            Log.e(LOG_TAG, "VerifyKeyActivity is not found");
        }
    }

    private void doUpdate() {
        if (bHasKey) {
            createKeybtn.setVisibility(View.GONE);
            verifyKeybtn.setVisibility(View.VISIBLE);
        } else {
            createKeybtn.setVisibility(View.VISIBLE);
            verifyKeybtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSuccess() {
        Log.e(LOG_TAG, getString(R.string.delete_success));
        runOnUiThread(new ToastRunnable(MainActivity.this, getString(R.string.delete_success)));
        bHasKey = false;
        bKeyVerified = false;
        SharedPreferences sp = getSharedPreferences("key", MODE_PRIVATE);
        sp.edit().putBoolean("has_key", false).commit();
        sp.edit().putBoolean("key_verified", false).commit();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doUpdate();
            }
        });
    }

    @Override
    public void onFail() {
        Log.e(LOG_TAG, getString(R.string.delete_fail));
        runOnUiThread(new ToastRunnable(MainActivity.this, getString(R.string.delete_fail)));
    }

    private void showInfoDialog(int type, String version) {
        InfoDialog infoDialog = new InfoDialog();
        Bundle args = new Bundle();
        args.putInt("type", type);
        args.putString("info", version);
        infoDialog.setArguments(args);
        infoDialog.show(getFragmentManager(), "info");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUkey.finalize();
        FingerPowerManage.setPowerSwitch("0");
    }

    @Override
    public void callback(int type, String msg) {
        if (type == 9) {
            showInfoDialog(1, msg);
        } else if (type == 11) {
            showInfoDialog(0, msg);
        } else {
            runOnUiThread(new ToastRunnable(MainActivity.this, msg));
        }
    }
}
