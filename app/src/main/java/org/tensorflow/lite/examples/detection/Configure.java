package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.SharedPreferences;

public class Configure {

    public static String getIPaddress(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("mPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString("ip_address", "192.168.1.191");
    }

    public static String getPhpDirPath(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("mPref", Context.MODE_PRIVATE);
        return sharedPreferences.getString("php_address", "dog");
    }

    public static String getServerPath(Context context)
    {
        String ip = getIPaddress(context);
        String path = getPhpDirPath(context);

        return "http://" + ip + "/" + path + "/";
    }
}