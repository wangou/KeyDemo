package com.fido.egistec.yukeyring;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;

import com.fido.egistec.fpservice.Command;
import com.fido.egistec.fpservice.FingerVerifyListener;
import com.fido.egistec.fpservice.YouKeyUWrapper;

public class InitVerifyActivity extends Activity implements FingerVerifyListener {

    private ImageView mImagLock;
    private YouKeyUWrapper mUKey = null;
    private boolean mVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_verify);
        mImagLock = (ImageView) findViewById(R.id.img_lock);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Validate fingerprint entry application");
        mUKey = YouKeyUWrapper.getInstance();
        mUKey.registerOnFingerVerify(this);
        mUKey.sendCommand(Command.REQUEST_VERIFY);
    }

    @Override
    public void noFingerEnrolled() {
        runOnUiThread(new ToastRunnable(InitVerifyActivity.this, "noFingerEnrolled"));
    }

    @Override
    public void noiseIgnored() {

    }

    @Override
    public void notAFingerPrint() {

    }

    @Override
    public void badFingerPrint() {

    }

    @Override
    public void onFail() {
        runOnUiThread(new ToastRunnable(InitVerifyActivity.this, getString(R.string.verify_fail)));
        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);
        mVerified = false;
        updateFPView();
    }

    @Override
    public void onSuccess() {
        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);
        mVerified = true;
        updateFPView();
    }

    @Override
    public void waitForMatchResult() {

    }

    @Override
    public void verifyTimeOut() {

    }

    private void updateFPView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mVerified) {
                    mImagLock.setImageResource(R.drawable.unlock);
                    runOnUiThread(new ToastRunnable(InitVerifyActivity.this, getString(R.string.verify_success)));
                    new CountDownTimer(1000, 500) {
                        public void onTick(long m) {
                        }

                        public void onFinish() {
                            doMain();
                        }
                    }.start();
                } else {
                    runOnUiThread(new ToastRunnable(InitVerifyActivity.this, getString(R.string.verify_again)));
//                    mUKey.sendCommand(Command.REQUEST_VERIFY);
                }
            }
        });
    }

    private void doMain() {
        try {
            startActivity(new Intent(InitVerifyActivity.this, MainActivity.class));
        } catch (ActivityNotFoundException activityNotFound) {
            Log.e("InitVerifyActivity", "VerifyKeyActivity is not found");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
