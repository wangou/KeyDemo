package com.fido.egistec.fpservice;

import android.widget.Toast;

import com.fido.egistec.yukeyring.MyApplication;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016/8/22.
 */
public class FingerPowerManage {
    public static void setPowerSwitch(String enabled) {
//        File file = new File("/sys/bus/platform/drivers/pmic_mt6323/vgp1");
//        Log.e("exist", file.exists() + "");
        FileOutputStream fos = null;
//        FileInputStream fis = null;
        try {
            fos = new FileOutputStream("/sys/bus/platform/drivers/pmic_mt6323/vgp1");
            fos.write(enabled.getBytes());
            System.out.println("setPowerSwitch enabled=" + enabled);
            fos.flush();
//            byte[] buffer = new byte[128];
//            fis = new FileInputStream("/syss/platform/drivers/pmic_mt6323gp1");
//            fis.read(buffer);
//            System.out.println("vgp1=" + Arrays.toString(buffer));

        } catch (IOException e) {
            Toast.makeText(MyApplication.getApplication(), "unknown error", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                fos.close();
//                fis.close();
            } catch (IOException e) {
                Toast.makeText(MyApplication.getApplication(), "unknown error", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
