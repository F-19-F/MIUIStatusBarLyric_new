package miui.statusbar.lyric;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import de.robv.android.xposed.XposedBridge;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class Utils {
    public static String PATH = Environment.getExternalStorageDirectory() + "/Android/media/miui.statusbar.lyric/";

    public static String ConfigPATH = PATH + "Config.json";

    public static String getLocalVersionCode(Context context) {
        String localVersion = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    public static String getMIUIVer() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                    (Runtime.getRuntime().exec(" ro.miui.ui.version.name").getInputStream()), 1024);
            String ver = bufferedReader.readLine();
            bufferedReader.close();
            return ver;
        } catch (Exception e) {
            return "";
        }
    }

    public static void init() {
        File file = new File(Utils.PATH);
        File file2 = new File(Utils.PATH + "Config.json");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file2.exists()) {
            try {
                Config config = new Config();
                file2.createNewFile();
                config.setHideLauncherIcon(false);
                config.setLyricService(true);
                config.setLyricWidth(-1);
                config.setLyricMaxWidth(-1);
                config.setLyricColor("");
                config.setIcon(true);
                config.setIconColor("off");
                config.setLyricAutoOff(true);
                config.setHideNoticeIcon(false);
                config.setHideNetSpeed(true);
                config.setHideCUK(false);
                config.setDebug(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initIcon(Context context) {
        if (!new File(Utils.PATH, "kugou.png").exists()) {
            copyAssets(context, "icon/kugou.png", Utils.PATH + "kugou.png");
        }
        if (!new File(Utils.PATH, "netease.png").exists()) {
            copyAssets(context, "icon/netease.png", Utils.PATH + "netease.png");
        }
        if (!new File(Utils.PATH, "qqmusic.png").exists()) {
            copyAssets(context, "icon/qqmusic.png", Utils.PATH + "qqmusic.png");
        }
        if (!new File(Utils.PATH, "kuwo.png").exists()) {
            copyAssets(context, "icon/kuwo.png", Utils.PATH + "kuwo.png");
        }
        if (!new File(Utils.PATH, ".nomedia").exists()) {
            try {
                new File(Utils.PATH, ".nomedia").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyAssets(Context context, String str, String str2) {
        try {
            File file = new File(str2);
            InputStream open = context.getAssets().open(str);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bArr = new byte[1024];
            while (true) {
                int read = open.read(bArr);
                if (read == -1) {
                    fileOutputStream.flush();
                    open.close();
                    fileOutputStream.close();
                    return;
                }
                fileOutputStream.write(bArr, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String text) {
        if (new Config().getDebug()) {
            XposedBridge.log("MIUI状态栏歌词： " + text);
        }
    }

    private static void checkUpdate(Application application, FragmentActivity fragmentActivity) {
        Toast.makeText(application, "开始检查更新", Toast.LENGTH_SHORT).show();
        Handler handler = new Handler(Looper.getMainLooper(), message -> {
            String data = message.getData().getString("value");
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (Utils.getLocalVersionCode(application).equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);
                    builder.setTitle("发现新版本[" + jsonObject.getString("tag_name") + "]")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage(jsonObject.getString("body").replace("#", ""))
                            .setPositiveButton("更新", (dialog, which) -> {
                                try {
                                    Uri uri = Uri.parse(jsonObject.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"));
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    intent.setData(uri);
                                    fragmentActivity.startActivity(intent);
                                } catch (JSONException e) {
                                    Toast.makeText(application, "获取最新版下载地址失败: " + e, Toast.LENGTH_SHORT).show();
                                }

                            }).setNegativeButton("取消", null).create().show();
                } else {
                    Toast.makeText(application, "已是最新版, 无需更新!", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        });


        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/577fkj/MIUIStatusBarLyric_new/releases/latest").openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("value", reader.readLine());
                message.setData(bundle);
                handler.sendMessage(message);
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(application, "检查更新失败: " + e, Toast.LENGTH_SHORT).show();
                Log.d("checkUpdate: ", e + "");
                e.printStackTrace();
                Looper.loop();
            }
        }).start();
    }

}
