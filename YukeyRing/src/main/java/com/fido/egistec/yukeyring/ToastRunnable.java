package com.fido.egistec.yukeyring;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/7/30.
 */
public class ToastRunnable implements Runnable {
    private String msg;
    private Context mContext;

    public ToastRunnable(Context context, String msg) {
        this.mContext = context;
        this.msg = msg;
    }

    @Override
    public void run() {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
