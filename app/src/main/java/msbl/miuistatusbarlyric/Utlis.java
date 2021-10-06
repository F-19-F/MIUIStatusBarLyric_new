package msbl.miuistatusbarlyric;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.robv.android.xposed.XposedBridge;

public class Utlis {
    public static String PATH = Environment.getExternalStorageDirectory() + "/Android/media/msbl.miuistatusbarlyric/";
    public static String Config_PATH = PATH + "Config.json";

    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    public static String getMIUIVer() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("getprop ro.miui.ui.version.name").getInputStream()), 1024);
            String str = bufferedReader.readLine();
            bufferedReader.close();
            return str;
        } catch (Exception e) {
            XposedBridge.log("无法获取MIUI版本！");
            return "";
        }
    }
}
