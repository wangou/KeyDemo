package com.fido.egistec.fpservice;

/**
 * Created by Administrator on 2016/7/30.
 */
public interface FingerEnrollListener {
    void waitForFingerOn();

    void fingerCoveredPatialSensor();

    void fingerUp();

    void changeFinger();

    void fingerLeftOrRight();

    void onProgress();

    void onSuccess();

    void onFail();
}
