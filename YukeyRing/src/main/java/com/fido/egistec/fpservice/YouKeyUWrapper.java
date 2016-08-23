package com.fido.egistec.fpservice;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android_serialport_api.SerialPort;

/**
 * YoukeyUWrapper
 */
public class YouKeyUWrapper {

    private static final String TAG = "YouKeyUWrapper";

    private static YouKeyUWrapper gUWrapper;

    private SerialPort mSerialPort = null;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;

    private FingerEnrollListener mFingerEnrollListener;
    private FingerVerifyListener mFingerVerifyListener;
    private OnFingerDeleteListener mOnFingerDeleteListener;
    private FingerOpCallback mFingerOpCallback;


    private ReadThread mReadThread;

    public static YouKeyUWrapper getInstance() {
        if (gUWrapper == null) {
            gUWrapper = new YouKeyUWrapper();
        }
        return gUWrapper;
    }

    public YouKeyUWrapper() {
        try {
            mSerialPort = new SerialPort(new File("/dev/ttyMT0"), 115200, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        /* Create a receiving thread */
        mReadThread = new ReadThread();
        mReadThread.start();
    }

    public void finalize() {
        try {
            mInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }

        if (mReadThread != null) {
            mReadThread.interrupt();
            mReadThread = null;
        }
        gUWrapper = null;
    }

    protected void onDataReceived(byte[] buffer, int size) {
        if (size < 10) {
            Log.e(TAG, "Unexpected response");
            return;
        } else if (buffer[0] != (byte) 0x55 || buffer[1] != (byte) 0xaa
                || buffer[2] != (byte) 0x00 || buffer[3] == (byte) 0x80) {
            Log.e(TAG, "Unexpected response");
            return;
        }
        switch (buffer[3]) {
            case (byte) 0x81:
                doEnroll(buffer);
                break;
            case (byte) 0x82:
                doVerify(buffer);
                break;
            case (byte) 0x83:
                doDelete(buffer);
                break;
            case (byte) 0x84:
                doCancel(buffer);
                break;
            case (byte) 0x85:
                if (buffer[5] == (byte) 0x00) {
                    Log.e(TAG, "The command is forwarded");
                    if (mFingerOpCallback != null) {
                        mFingerOpCallback.callback(5, "The command is forwarded");
                    }
                }
                break;
            case (byte) 0x86:
                if (buffer[5] == (byte) 0x00) {
                    Log.e(TAG, "The detected mode is set");
                    if (mFingerOpCallback != null) {
                        mFingerOpCallback.callback(6, "The detected mode is set");
                    }
                }
                break;
            case (byte) 0x87:
                if (buffer[5] == (byte) 0x00) {
                    Log.e(TAG, "The calibration is done.");
                    if (mFingerOpCallback != null) {
                        mFingerOpCallback.callback(7, "The calibration is done.");
                    }
                }
                break;
            case (byte) 0x89:
                if (buffer[5] == (byte) 0x00) {
                    Log.e(TAG, "get configuration: " + byte2hex(Arrays.copyOfRange(buffer, 8, 22)));
                    if (mFingerOpCallback != null) {
                        mFingerOpCallback.callback(9, byte2hex(Arrays.copyOfRange(buffer, 8, 22)));
                    }
                }
                break;
            case (byte) 0x8a:
                if (buffer[5] == (byte) 0x00) {
                    Log.e(TAG, "Set configuration End.");
                    if (mFingerOpCallback != null) {
                        mFingerOpCallback.callback(10, "Set configuration End.");
                    }
                }
                break;
            case (byte) 0xff:
                if (buffer[5] == (byte) 0x00) {
                    if (mFingerOpCallback != null) {
                        mFingerOpCallback.callback(11, ASCIIString(buffer));
                    }
                }
                break;
            default:
                Log.e(TAG, "Unexpected response");
                break;
        }

        switch (buffer[4]) {
            case (byte) 0x00:
                break;
            case (byte) 0xff:
                Log.e(TAG, "status general error");
                break;
            case (byte) 0xf1:
                Log.e(TAG, "status sync byte error");
                break;
            case (byte) 0xf2:
                Log.e(TAG, "status cipher byte error");
                break;
            case (byte) 0xf3:
                Log.e(TAG, "status command type error");
                break;
            case (byte) 0xf4:
                Log.e(TAG, "status checksum error");
                break;
            case (byte) 0xf5:
                Log.e(TAG, "status length error");
                break;
            default:
                Log.e(TAG, "status unknown error");
                break;
        }
    }

    /**
     * 0x84
     *
     * @param buffer
     */
    private void doCancel(byte[] buffer) {
        if (buffer[4] == (byte) 0x00 && buffer[5] == (byte) 0x00) {
            Log.e(TAG, "fingerCaceled");
        } else {
            Log.e(TAG, "cancelFailed");
        }
    }

    /**
     * 0x83
     *
     * @param buffer
     */
    private void doDelete(byte[] buffer) {
        if (mOnFingerDeleteListener == null) {
            return;
        }
        if (buffer[4] == (byte) 0x00 && buffer[5] == (byte) 0x00) {
            mOnFingerDeleteListener.onSuccess();
        } else {
            mOnFingerDeleteListener.onFail();
        }
    }

    private String ASCIIString(byte[] buffer) {
        int size = buffer.length;
        char[] chars = new char[size - 8];
        for (int i = 8; i < size; i++) {
            chars[i - 8] = (char) buffer[i];
        }
        String version = String.copyValueOf(chars);
        Log.e("version", version);
        return version;
    }

    private int verifyTime = 0;

    /**
     * 0x82
     *
     * @param buffer
     */
    private void doVerify(byte[] buffer) {
        if (mFingerVerifyListener == null) {
            return;
        }
        if (buffer[4] == (byte) 0x00) {
            if (buffer[5] == (byte) 0x00) {
                mFingerVerifyListener.onSuccess();
            } else if (buffer[5] == (byte) 0x22) {
                mFingerVerifyListener.waitForMatchResult();
            } else if (buffer[5] == (byte) 0xfe) {
                mFingerVerifyListener.noFingerEnrolled();
            } else {
                if (buffer[5] == (byte) 0x01) {
                    mFingerVerifyListener.onFail();
                } else if (buffer[5] == (byte) 0x93) {
                    mFingerVerifyListener.badFingerPrint();
                } else if (buffer[5] == (byte) 0x95) {
                    mFingerVerifyListener.notAFingerPrint();
                } else if (buffer[5] == (byte) 0x97) {
                    mFingerVerifyListener.noiseIgnored();
                } else {
                    Log.e(TAG, "Unexpected response");
                }
                if (verifyTime++ < 10) {
                    sendCommand(Command.REQUEST_VERIFY);
                } else {
                    mFingerVerifyListener.verifyTimeOut();
                }
            }
        } else {
            Log.e(TAG, "Unexpected response");
        }
    }

    /**
     * 0x81
     *
     * @param buffer
     */
    private void doEnroll(byte[] buffer) {
        if (mFingerEnrollListener == null) {
            return;
        }
        if (buffer[4] == (byte) 0x00) {
            if (buffer[5] == (byte) 0x21) {
                mFingerEnrollListener.onProgress();
            } else if (buffer[5] == (byte) 0xff) {
                mFingerEnrollListener.onFail();
            } else if (buffer[5] == (byte) 0x00) {
                mFingerEnrollListener.onSuccess();
            } else if (buffer[5] == (byte) 0x25) {
                mFingerEnrollListener.waitForFingerOn();
            } else if (buffer[5] == (byte) 0x19) {
                mFingerEnrollListener.fingerLeftOrRight();
            } else if (buffer[5] == (byte) 0x20) {
                mFingerEnrollListener.changeFinger();
            } else if (buffer[5] == (byte) 0x23) {
                mFingerEnrollListener.fingerUp();
            } else if (buffer[5] == (byte) 0x28) {
                mFingerEnrollListener.fingerCoveredPatialSensor();
            } else {
                Log.e(TAG, "Unexpected response");
            }
        } else {
            Log.e(TAG, "Unexpected response");
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        size = (size > 10) ? 10 : size;
                        Log.e("responseCMD", byte2hex(buffer));
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            Log.e(TAG, "Interrupt thread!! ");
        }
    }

    public String byte2hex(byte[] buffer) {
        String h = "";

        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }
        return h;

    }

    public void sendCommand(byte[] cmd) {
        byte[] buffer = CRC16.CalcCRC16Kermit(cmd);
        try {
            mOutputStream.write(buffer);
            mOutputStream.flush();
            Log.e("RequestCMD", byte2hex(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerOnFingerFetch(FingerEnrollListener fingerEnrollListener) {
        mFingerEnrollListener = fingerEnrollListener;
    }

    public void registerOnFingerVerify(FingerVerifyListener fingerVerifyListener) {
        mFingerVerifyListener = fingerVerifyListener;
    }

    public void registOnFingerDelete(OnFingerDeleteListener fingerDeleteListener) {
        mOnFingerDeleteListener = fingerDeleteListener;
    }

    public void registFingerOpCallBack(FingerOpCallback callback) {
        mFingerOpCallback = callback;
    }
}
