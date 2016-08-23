package com.fido.egistec.yukeyring;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fido.egistec.fpservice.Command;
import com.fido.egistec.fpservice.FingerEnrollListener;
import com.fido.egistec.fpservice.YouKeyUWrapper;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Enroll
 */
public class CreateKeyActivity extends Activity implements FingerEnrollListener {

    public final static int CREATE_KEY_REQ = 0x1104;
    public static ArrayList<Integer> pListfp = new ArrayList<>(Arrays.asList(R.drawable.key_1, R.drawable.key_2, R.drawable.key_3, R.drawable.key_4,
            R.drawable.key_5, R.drawable.key_6, R.drawable.key_7, R.drawable.key_8, R.drawable.key_9, R.drawable.key_10, R.drawable.key_11, R.drawable.key_8_p));
    private final String LOG_TAG = CreateKeyActivity.class.getName();
    private final int ENROLL_MAX_SIZE = 12;


    public static ImageButton mEnrollbtn;
    private int nCounter = 0;
    private boolean mCreated = false;
    private YouKeyUWrapper mUKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createkey_activity);

        mEnrollbtn = (ImageButton) findViewById(R.id.enrollmentbtn);
        nCounter = 0;
        mCreated = false;
        mUKey = YouKeyUWrapper.getInstance();
        mUKey.registerOnFingerFetch(this);
        mUKey.sendCommand(Command.REQUEST_ENROLL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUKey.sendCommand(Command.REQUEST_CANCEL);
    }

    @Override
    public void onPause() {
        nCounter = 0;
        mCreated = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        nCounter = 0;
        mCreated = false;
        super.onResume();
    }

    @Override
    public void onSuccess() {
        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);
        mCreated = true;
        updateFPView();
    }

    @Override
    public void onFail() {
        Log.e(LOG_TAG, getString(R.string.enroll_fail));
        runOnUiThread(new ToastRunnable(CreateKeyActivity.this, getString(R.string.enroll_fail)));
    }

    @Override
    public void waitForFingerOn() {
        Log.e(LOG_TAG, getString(R.string.enroll_waiting_for_finger_on));
        runOnUiThread(new ToastRunnable(CreateKeyActivity.this, getString(R.string.enroll_waiting_for_finger_on)));
    }

    @Override
    public void fingerCoveredPatialSensor() {
        Log.e(LOG_TAG, getString(R.string.enroll_finger_covered_sensor));
//        runOnUiThread(new ToastRunnable(CreateKeyActivity.this, getString(R.string.enroll_finger_covered_sensor)));
    }

    @Override
    public void fingerUp() {
        Log.e(LOG_TAG, getString(R.string.enroll_finger_up));
//        runOnUiThread(new ToastRunnable(CreateKeyActivity.this, getString(R.string.enroll_finger_up)));
    }

    @Override
    public void changeFinger() {
        Log.e(LOG_TAG, getString(R.string.enroll_change_finger));
        runOnUiThread(new ToastRunnable(CreateKeyActivity.this, getString(R.string.enroll_change_finger)));
    }

    @Override
    public void fingerLeftOrRight() {
        Log.e(LOG_TAG, getString(R.string.enroll_finger_left_or_right));
        runOnUiThread(new ToastRunnable(CreateKeyActivity.this, getString(R.string.enroll_finger_left_or_right)));
    }

    @Override
    public void onProgress() {
        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);
        updateFPView();
    }

    private void updateFPView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCreated) {
                    mEnrollbtn.setImageDrawable(getResources().getDrawable(pListfp.get((nCounter++) % ENROLL_MAX_SIZE)));
                    mEnrollbtn.invalidate();
                    nCounter = 0;
                    final Toast toast = Toast.makeText(getApplicationContext(), R.string.create_success, Toast.LENGTH_SHORT);
                    toast.show();
                    new CountDownTimer(3000, 500) {
                        public void onTick(long m) {
                        }

                        public void onFinish() {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }.start();
                } else {
                    mEnrollbtn.setImageDrawable(getResources().getDrawable(pListfp.get((nCounter++) % ENROLL_MAX_SIZE)));
                    mEnrollbtn.invalidate();
                }
            }
        });
    }
}
