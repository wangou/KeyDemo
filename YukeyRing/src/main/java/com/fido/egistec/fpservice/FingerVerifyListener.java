package com.fido.egistec.fpservice;

/**
 * Created by Administrator on 2016/7/30.
 */
public interface FingerVerifyListener {
    void noFingerEnrolled();

    void noiseIgnored();

    void notAFingerPrint();

    void badFingerPrint();

    void onFail();

    void onSuccess();

    void waitForMatchResult();

    void verifyTimeOut();
}
