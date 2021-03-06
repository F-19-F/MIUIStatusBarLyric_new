package miui.statusbar.lyric;

import android.app.ActivityManager;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.app.MiuiStatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static miui.statusbar.lyric.Utils.log;

public class MainHook implements IXposedHookLoadPackage {
    private static final String KEY_LYRIC = "lyric";
    private static String musicName = "";
    private Context context = null;
    private static String lyric = "";
    private static String iconPath = "";

    public static class LyricReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("Lyric_Server")) {
                lyric = intent.getStringExtra("Lyric_Data");
                if (new Config().getIcon()) {
                    iconPath = new Config().getIconPath() + intent.getStringExtra("Lyric_Icon") + ".png";
                } else {
                    iconPath = "";
                }
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // ??????Context
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                context = (Context) param.args[0];
                // ????????????
                if (lpparam.packageName.equals("com.android.systemui")) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("Lyric_Server");
                    context.registerReceiver(new LyricReceiver(), filter);
                }
            }
        });

        switch (lpparam.packageName) {
            case "com.android.systemui":
                log("Hook SystemUI");
                // ???????????????
                XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.CollapsedStatusBarFragment", lpparam.classLoader, "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Field clockField;

                        // ?????????????????????Application
                        Application application = AndroidAppHelper.currentApplication();

                        // ?????????????????????
                        AudioManager audioManager = (AudioManager) application.getSystemService(Context.AUDIO_SERVICE);

                        // ?????????????????????
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        ((WindowManager) application.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

                        // ??????????????????
                        int dw = displayMetrics.widthPixels;

                        // ??????????????????
                        String miuiVer = Utils.getMIUIVer();
                        log("MIUI Ver: " + miuiVer);

                        // ??????????????????
                        if (miuiVer.equals("V12")) {
                            clockField = XposedHelpers.findField(param.thisObject.getClass(), "mStatusClock");
                        } else if (miuiVer.equals("V125")) {
                            clockField = XposedHelpers.findField(param.thisObject.getClass(), "mClockView");
                        } else {
                            log("Unknown version");
                            clockField = XposedHelpers.findField(param.thisObject.getClass(), "mClockView");
                        }
                        TextView clock = (TextView) clockField.get(param.thisObject);

                        // ??????TextView
                        AutoMarqueeTextView lyricTextView = new AutoMarqueeTextView(application);
                        lyricTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        lyricTextView.setWidth((dw * 35) / 100);
                        lyricTextView.setHeight(clock.getHeight());
                        lyricTextView.setTypeface(clock.getTypeface());
                        lyricTextView.setTextSize(0, clock.getTextSize());
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) lyricTextView.getLayoutParams();
                        layoutParams.setMargins(10, 0, 0, 0);
                        lyricTextView.setLayoutParams(layoutParams);

                        // ?????????????????????
                        lyricTextView.setSingleLine(true);
                        lyricTextView.setMarqueeRepeatLimit(-1);
                        lyricTextView.setVisibility(View.GONE);
                        lyricTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);

                        // ?????????????????????????????????
                        LinearLayout clockLayout = (LinearLayout) clock.getParent();
                        clockLayout.setGravity(Gravity.CENTER);
                        clockLayout.setOrientation(LinearLayout.HORIZONTAL);
                        clockLayout.addView(lyricTextView, 1);

                        // ????????????
                        TextView iconView = new TextView(application);
                        iconView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        layoutParams = (LinearLayout.LayoutParams) iconView.getLayoutParams();
                        layoutParams.setMargins(0, 2, 0, 0);
                        iconView.setLayoutParams(layoutParams);
                        clockLayout.addView(iconView, 1);

                        final Handler iconUpdate = new Handler(Looper.getMainLooper(), message -> {
                            iconView.setCompoundDrawables((Drawable) message.obj, null, null, null);
                            return true;
                        });

                        // ???????????? Handler
                        Handler LyricUpdate = new Handler(Looper.getMainLooper(), message -> {
                            Config config = new Config();
                            String string = message.getData().getString(KEY_LYRIC);
                            if (!string.equals("")) {
                                if (!string.equals(lyricTextView.getText().toString())) {
                                    // ???????????????
                                    if (config.getHideNoticeIcon() && MiuiStatusBarManager.isShowNotificationIcon(application)) {
                                        MiuiStatusBarManager.setShowNotificationIcon(application, false);
                                    }
                                    if (config.getHideNetSpeed() && MiuiStatusBarManager.isShowNetworkSpeed(application)) {
                                        MiuiStatusBarManager.setShowNetworkSpeed(application, false);
                                    }
                                    if (config.getHideCUK() && Settings.System.getInt(context.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1) == 1) {
                                        Settings.System.putInt(context.getContentResolver(), "status_bar_show_carrier_under_keyguard", 0);
                                    }
                                    // ??????????????????
                                    lyricTextView.setText(string);
                                    // ????????????
                                    lyricTextView.setVisibility(View.VISIBLE);

                                    // ?????????/????????????
                                    if (config.getLyricWidth() == -1) {
                                        TextPaint paint1 = lyricTextView.getPaint(); // ????????????
                                        if (config.getLyricMaxWidth() == -1 || ((int) paint1.measureText(string)) + 6 <= (dw * config.getLyricMaxWidth()) / 100) {
                                            lyricTextView.setWidth(((int) paint1.measureText(string)) + 6);

                                        } else {
                                            lyricTextView.setWidth((dw * config.getLyricMaxWidth()) / 100);
                                        }
                                    } else {
                                        lyricTextView.setWidth((dw * config.getLyricWidth()) / 100);
                                    }
                                }
                                // ????????????
                                clock.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                                return false;
                            }
                            // ????????????
                            iconView.setCompoundDrawables(null, null, null, null);
                            // ????????????
                            clock.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
                            // ????????????
                            lyricTextView.setVisibility(View.GONE);
                            return true;
                        });

                        new Timer().schedule(
                                new TimerTask() {
                                    boolean enable = false;
                                    Config config = new Config();
                                    ColorStateList color = null;
                                    int count = 0;
                                    int lyricSpeed = 0;
                                    String oldLyric = "";
                                    boolean lyricServer = false;
                                    boolean lyricOff = false;
                                    Boolean iconReverseColor = false;
                                    boolean iconReverseColorStatus = false;

                                    @Override
                                    public void run() {
                                        if (count == 100) {
                                            if (isServiceRunning(application, "com.kugou") | isServiceRunning(application, "com.netease.cloudmusic") | isServiceRunning(application, "com.tencent.qqmusic.service") | isServiceRunning(application, "cn.kuwo") | isServiceRunning(application, "com.maxmpz.audioplayer") | isServiceRunning(application, "remix.myplayer")) {
                                                enable = true;
                                                config = new Config();
                                                lyricServer = config.getLyricService();
                                                if (config.getLyricAutoOff()) lyricOff = audioManager.isMusicActive();
                                                iconReverseColor = config.getIconAutoColor();
                                                iconReverseColorStatus = true;
                                            } else {
                                                if (enable || (lyricTextView.getVisibility() != View.GONE)) {
                                                    log("??????????????? ????????????");
                                                    lyric = "";
                                                    enable = false;

                                                    // ????????????
                                                    Message obtainMessage = LyricUpdate.obtainMessage();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(KEY_LYRIC, "");
                                                    obtainMessage.setData(bundle);
                                                    LyricUpdate.sendMessage(obtainMessage);

                                                    // ???????????????
                                                    if (config.getHideNoticeIcon() && !MiuiStatusBarManager.isShowNotificationIcon(application)) {
                                                        MiuiStatusBarManager.setShowNotificationIcon(application, true);
                                                    }
                                                    if (config.getHideNetSpeed() && !MiuiStatusBarManager.isShowNetworkSpeed(application)) {
                                                        MiuiStatusBarManager.setShowNetworkSpeed(application, true);
                                                    }
                                                    if (config.getHideCUK() && Settings.System.getInt(application.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1) != 1) {
                                                        Settings.System.putInt(application.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1);
                                                    }
                                                }
                                            }
                                            if (enable && !lyric.equals("")) {
                                                // ????????????
                                                log((!config.getLyricColor().equals("off") && !config.getLyricColor().equals("")) + "  :  " + config.getLyricColor());
                                                if (!config.getLyricColor().equals("off") && !config.getLyricColor().equals("")) {
                                                    if (color != ColorStateList.valueOf(Color.parseColor(config.getLyricColor()))) {
                                                        color = ColorStateList.valueOf(Color.parseColor(config.getLyricColor()));
                                                        lyricTextView.setTextColor(color);
                                                    }
                                                } else if (!(clock.getTextColors() == null || color == clock.getTextColors())) {
                                                    color = clock.getTextColors();
                                                    lyricTextView.setTextColor(color);
                                                    iconReverseColorStatus = true;
                                                }

                                                if (!iconPath.equals("")) {
                                                    if (iconReverseColorStatus) {
                                                        if (new File(iconPath).exists()) {
                                                            Drawable createFromPath = Drawable.createFromPath(iconPath);
                                                            createFromPath.setBounds(0, 0, (int) clock.getTextSize(), (int) clock.getTextSize());
                                                            if (iconReverseColor) {
                                                                createFromPath = reverseColor(createFromPath, isDark(clock.getTextColors().getDefaultColor()));
                                                            }
                                                            Message obtainMessage2 = iconUpdate.obtainMessage();
                                                            obtainMessage2.obj = createFromPath;
                                                            iconUpdate.sendMessage(obtainMessage2);
                                                        }
                                                        iconReverseColorStatus = false;
                                                    }
                                                }
                                            }
                                            count = 0;
                                        }
                                        count++;

                                        if (enable && lyricSpeed == 10) {
                                            lyricSpeed = 0;
                                            if (!lyric.equals("") && lyricServer && lyricOff) {
                                                if (!oldLyric.equals(lyric)) {
                                                    Message message = LyricUpdate.obtainMessage();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(KEY_LYRIC, lyric);
                                                    message.setData(bundle);
                                                    LyricUpdate.sendMessage(message);
                                                    oldLyric = lyric;
                                                }
                                            } else if (enable) {
                                                if (lyricTextView.getVisibility() != View.GONE) {
                                                    log("?????????????????????????????? ????????????");
                                                    Message message = LyricUpdate.obtainMessage();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(KEY_LYRIC, "");
                                                    message.setData(bundle);
                                                    LyricUpdate.sendMessage(message);
                                                    lyric = "";
                                                    oldLyric = lyric;
                                                    enable = false;


                                                    // ???????????????
                                                    if (config.getHideNoticeIcon() && !MiuiStatusBarManager.isShowNotificationIcon(application)) {
                                                        MiuiStatusBarManager.setShowNotificationIcon(application, true);
                                                    }
                                                    if (config.getHideNetSpeed() && !MiuiStatusBarManager.isShowNetworkSpeed(application)) {
                                                        MiuiStatusBarManager.setShowNetworkSpeed(application, true);
                                                    }
                                                    log(String.valueOf(MiuiStatusBarManager.isShowNetworkSpeed(application)));
                                                    log("1111");
                                                    if (config.getHideCUK() && Settings.System.getInt(context.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1) != 1) {
                                                        Settings.System.putInt(application.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1);
                                                    }
                                                }
                                            }
                                        }

                                        if (lyricSpeed < 10) lyricSpeed++;
                                    }
                                }, 0, 10);

                    }
                });
                break;
            case "com.android.settings":
                log("Hook Settings");
                XposedHelpers.findAndHookMethod("com.android.settings.NotchStatusBarSettings", lpparam.classLoader, "onCreate", Class.forName("android.os.Bundle"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Application currentApplication = AndroidAppHelper.currentApplication();
                        Object objectField = XposedHelpers.getObjectField(param.thisObject, "mCustomCarrier");
                        XposedHelpers.setObjectField(objectField, "mTitle", "???????????????");
                        XposedHelpers.setObjectField(objectField, "mText", "???????????????");
                        XposedHelpers.setObjectField(objectField, "mClickListener", (View.OnClickListener) view -> {
                            Intent intent = new Intent("miui.statusbar.lyric.MainActivity");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            currentApplication.startActivity(intent);
                        });
                    }
                });
                break;
            case "com.netease.cloudmusic":
                XposedHelpers.findAndHookMethod("com.netease.cloudmusic.module.player.t.e", lpparam.classLoader, "o", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        param.setResult(true);
                    }
                });
                XposedHelpers.findAndHookMethod(lpparam.classLoader.loadClass("com.netease.cloudmusic.module.player.t.e"), "B", Class.forName("java.lang.String"), Class.forName("java.lang.String"), Class.forName("java.lang.String"), Long.TYPE, Class.forName("java.lang.Boolean"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        sendLyric(context, param.args[0].toString(), "netease");
                        musicName = param.args[0].toString();
                        log("???????????? " + param.args[0].toString());
                    }
                });
                XposedHelpers.findAndHookMethod(lpparam.classLoader.loadClass("com.netease.cloudmusic.module.player.t.e"), "F", Class.forName("java.lang.String"), Class.forName("java.lang.String"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        sendLyric(context, param.args[0].toString(), "netease");
                        log("???????????? " + param.args[0].toString());
                        param.args[0] = musicName;
                        param.setResult(param.args);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                    }
                });
                break;
            case "com.kugou.android":
                log("??????hook????????????");
                XposedHelpers.findAndHookMethod("android.media.AudioManager", lpparam.classLoader, "isBluetoothA2dpOn", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        param.setResult(true);
                    }
                });
                XposedHelpers.findAndHookMethod("com.kugou.framework.player.c", lpparam.classLoader, "a", HashMap.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        log("????????????:" + ((HashMap) param.args[0]).values().toArray()[0]);
                        sendLyric(context, "" + ((HashMap) param.args[0]).values().toArray()[0], "kugou");
                    }
                });
                break;
            case "cn.kuwo.player":
                XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", lpparam.classLoader, "isEnabled", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        param.setResult(true);
                    }
                });
                XposedHelpers.findAndHookMethod("cn.kuwo.mod.playcontrol.RemoteControlLyricMgr", lpparam.classLoader, "updateLyricText", Class.forName("java.lang.String"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        String str = (String) param.args[0];
                        log("????????????:" + str);
                        if (param.args[0] != null && !str.equals("") && !str.equals("????????? ?????????") && !str.equals("??????????????????...") && !str.contains(" - ")) {
                            sendLyric(context, "" + str, "kuwo");
                        }
                        param.setResult(replaceHookedMethod());
                    }

                    private Object replaceHookedMethod() {
                        return null;
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                    }
                });
                break;
            case "com.tencent.qqmusic":
                XposedHelpers.findAndHookMethod(lpparam.classLoader.loadClass("com.tencent.qqmusicplayerprocess.servicenew.mediasession.d$d"), "run", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Class<?> findClass = XposedHelpers.findClass("com.lyricengine.base.h", lpparam.classLoader);
                        Field declaredField = findClass.getDeclaredField("a");
                        declaredField.setAccessible(true);

                        Object obj = XposedHelpers.findField(param.thisObject.getClass(), "b").get(param.thisObject);
                        String str = (String) declaredField.get(obj);

                        log("qq??????: " + str);

                        sendLyric(context, str, "qqmusic");
                    }
                });
                break;
        }
    }

    public static boolean isServiceRunning(Context context, String str) {
        List<ActivityManager.RunningServiceInfo> runningServices = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(200);
        if (runningServices.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().contains(str)) {
                return true;
            }
        }
        return false;
    }

    public void sendLyric(Context context, String lyric, String icon) {
        context.sendBroadcast(new Intent().setAction("Lyric_Server").putExtra("Lyric_Data", lyric).putExtra("Lyric_Icon", icon));
    }

    private Drawable reverseColor(Drawable icon, Boolean black) {
        ColorMatrix cm = new ColorMatrix();
        if (black) {
            cm.set(new float[]{
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
            });
        }
        icon.setColorFilter(new ColorMatrixColorFilter(cm));
        return icon;
    }

    boolean isDark(int color) {
        return ColorUtils.calculateLuminance(color) < 0.5;
    }

}
