package com.fido.egistec.yukeyring;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageButton;

import com.fido.egistec.fpservice.Command;
import com.fido.egistec.fpservice.FingerVerifyListener;
import com.fido.egistec.fpservice.YouKeyUWrapper;

/**
 * Verify
 */
public class VerifyKeyActivity extends Activity implements FingerVerifyListener {

    public final static int VERIFY_KEY_REQ = 0x1103;
    private final String LOG_TAG = CreateKeyActivity.class.getName();
    private ImageButton mVerifybtn;
    private YouKeyUWrapper mUKey = null;

    private boolean mVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verifykey_activity);
        mVerifybtn = (ImageButton) findViewById(R.id.verifybtn);
        mUKey = YouKeyUWrapper.getInstance();
        mUKey.registerOnFingerVerify(this);
        mUKey.sendCommand(Command.REQUEST_VERIFY);
        mVerified = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSuccess() {
        Log.e(LOG_TAG, "onSuccess");
        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);
        mVerified = true;
        updateFPView();
    }

    @Override
    public void waitForMatchResult() {
        Log.e(LOG_TAG, "waitForMatchResult");
    }

    @Override
    public void verifyTimeOut() {
        Log.e(LOG_TAG, "verifyTimeOut");
    }

    @Override
    public void noFingerEnrolled() {
        Log.e(LOG_TAG, "noFingerEnrolled");
        runOnUiThread(new ToastRunnable(VerifyKeyActivity.this, "noFingerEnrolled"));
    }

    @Override
    public void noiseIgnored() {
        Log.e(LOG_TAG, "noiseIgnored");
    }

    @Override
    public void notAFingerPrint() {
        Log.e(LOG_TAG, "notAFingerPrint");
    }

    @Override
    public void badFingerPrint() {
        Log.e(LOG_TAG, "badFingerPrint");
    }

    @Override
    public void onFail() {
        Log.e(LOG_TAG, "onFail");
        runOnUiThread(new ToastRunnable(VerifyKeyActivity.this, getString(R.string.verify_fail)));
        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);
        mVerified = false;
        updateFPView();
    }

    private void updateFPView() {
        runOnUiThread(new ToastRunnable(VerifyKeyActivity.this, getString(R.string.verify_success)));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mVerified) {
                    mVerifybtn.setImageDrawable(getResources().getDrawable(R.drawable.key_8_p));
                    mVerifybtn.invalidate();
                    new CountDownTimer(1000, 500) {
                        public void onTick(long m) {
                        }

                        public void onFinish() {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }.start();
                } else {
                    runOnUiThread(new ToastRunnable(VerifyKeyActivity.this, getString(R.string.verify_again)));
//                    mUKey.sendCommand(Command.REQUEST_VERIFY);
                }
            }
        });
    }
}
