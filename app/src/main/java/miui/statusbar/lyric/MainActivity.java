package miui.statusbar.lyric;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Config config;

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            checkPermission();
            Utils.init();
            Utils.initIcon(requireContext());
            config = new Config();

            // 隐藏桌面图标
            CheckBoxPreference hideIcons = findPreference("hideLauncherIcon");
            assert hideIcons != null;
            hideIcons.setChecked(config.getHideLauncherIcon());
            hideIcons.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.toString().equals("false")) {
                    PackageManager packageManager = Objects.requireNonNull(requireActivity()).getPackageManager();
                    int show = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                    packageManager.setComponentEnabledSetting(new ComponentName(requireActivity(), "miui.statusbar.lyric.launcher"), show, PackageManager.DONT_KILL_APP);
                } else {
                    PackageManager packageManager = Objects.requireNonNull(requireActivity()).getPackageManager();
                    int show = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                    packageManager.setComponentEnabledSetting(new ComponentName(requireActivity(), "miui.statusbar.lyric.launcher"), show, PackageManager.DONT_KILL_APP);
                }
                config.setHideLauncherIcon((Boolean) newValue);

                return true;
            });
            // 歌词总开关
            CheckBoxPreference lyricService = findPreference("lyricService");
            assert lyricService != null;
            lyricService.setChecked(config.getLyricService());
            lyricService.setOnPreferenceChangeListener((preference, newValue) -> {
                lyricService.setDefaultValue(newValue);
                config.setLyricService((Boolean) newValue);
                return true;
            });

            // 歌词宽度
            EditTextPreference lyricWidth = findPreference("lyricWidth");
            assert lyricWidth != null;
            lyricWidth.setSummary(config.getLyricWidth() + "%");
            if (config.getLyricWidth() == -1) {
                lyricWidth.setSummary("自适应");
            }
            lyricWidth.setDialogMessage("(-1~100，-1为自适应)，当前:" + lyricWidth.getSummary());
            lyricWidth.setOnPreferenceChangeListener((preference, newValue) -> {
                lyricWidth.setSummary(newValue.toString());
                lyricWidth.setDefaultValue(newValue);
                if (newValue.toString().equals("-1")) {
                    lyricWidth.setSummary("自适应");
                    lyricWidth.setDialogMessage("(-1~100，-1为自适应)，当前:自适应");
                } else {
                    lyricWidth.setDialogMessage("(-1~100，-1为自适应)，当前:" + newValue + "%");
                }
                try {
                    config.setLyricMaxWidth(Integer.parseInt(newValue.toString()));
                } catch (java.lang.NumberFormatException e) {
                    config.setLyricMaxWidth(-1);
                    lyricWidth.setSummary("自适应");
                    lyricWidth.setDialogMessage("(-1~100，-1为自适应)，当前:自适应");
                }
                return true;
            });

            // 歌词最大自适应宽度
            EditTextPreference lyricMaxWidth = findPreference("lyricMaxWidth");
            assert lyricMaxWidth != null;
            lyricMaxWidth.setSummary(config.getLyricMaxWidth() + "%");
            if (config.getLyricMaxWidth() == -1) {
                lyricMaxWidth.setSummary("关闭");
                lyricMaxWidth.setDialogMessage("(-1~100，-1为关闭，仅在歌词宽度为自适应时生效)，当前:关闭");
            } else {
                lyricMaxWidth.setDialogMessage("(-1~100，-1为关闭，仅在歌词宽度为自适应时生效)，当前:" + lyricMaxWidth.getSummary());
            }
            lyricMaxWidth.setOnPreferenceChangeListener((preference, newValue) -> {
                lyricMaxWidth.setSummary(newValue.toString());
                lyricMaxWidth.setDefaultValue(newValue);
                if (newValue.toString().equals("-1")) {
                    lyricMaxWidth.setDialogMessage("(-1~100，-1为关闭，仅在歌词宽度为自适应时生效)，当前:关闭");
                    lyricMaxWidth.setSummary("关闭");
                } else {
                    lyricMaxWidth.setDialogMessage("(-1~100，-1为关闭，仅在歌词宽度为自适应时生效)，当前:" + newValue + "%");
                }
                try {
                    config.setLyricMaxWidth(Integer.parseInt(newValue.toString()));
                } catch (java.lang.NumberFormatException e) {
                    lyricMaxWidth.setSummary("关闭");
                    lyricMaxWidth.setDialogMessage("(-1~100，-1为关闭，仅在歌词宽度为自适应时生效)，当前:关闭");
                    config.setLyricMaxWidth(Integer.parseInt("-1"));
                }

                return true;
            });

            // 歌词颜色
            EditTextPreference lyricColour = findPreference("lyricColour");
            assert lyricColour != null;
            lyricColour.setSummary(config.getLyricColor());
            if (config.getLyricColor().equals("off")) {
                lyricColour.setSummary("关闭");
                lyricColour.setDialogMessage("请输入16进制颜色代码，例如: #C0C0C0，目前：关闭");
            } else {
                lyricColour.setSummary(config.getLyricColor());
                lyricColour.setDialogMessage("请输入16进制颜色代码，例如: #C0C0C0，目前：" + config.getLyricColor());
            }
            lyricColour.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.toString().equals("") | newValue.toString().equals("关闭")) {
                    config.setLyricColor("off");
                } else {
                    try {
                        Color.parseColor(newValue.toString());
                        lyricColour.setSummary(newValue.toString());
                        config.setLyricColor(newValue.toString());
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "颜色代码不正确!", Toast.LENGTH_SHORT).show();
                        config.setLyricColor("off");
                    }
                }
                return true;
            });

            // 图标
            ListPreference icon = findPreference("icon");
            assert icon != null;
            String[] strArr = new String[2];
            strArr[0] = "关闭";
            strArr[1] = "开启";
            icon.setEntries(strArr);
            icon.setEntryValues(strArr);
            if (config.getIcon()) {
                icon.setSummary(strArr[1]);
            } else {
                icon.setSummary(strArr[0]);
            }
            icon.setOnPreferenceChangeListener((preference, newValue) ->

            {
                switch (newValue.toString()) {
                    case "关闭":
                        config.setIcon(false);
                        break;
                    case "开启":
                        config.setIcon(true);
                        break;
                }
                icon.setSummary(newValue.toString());
                return true;
            });

            // 图标反色
            ListPreference iconColor = findPreference("iconColor");
            assert iconColor != null;
            strArr = new String[3];
            strArr[0] = "关闭";
            strArr[1] = "白色图标";
            strArr[2] = "黑色图标";
            iconColor.setEntries(strArr);
            iconColor.setEntryValues(strArr);
            switch (config.getIconColor()) {
                case "off":
                    iconColor.setSummary(strArr[0]);
                    break;
                case "white":
                    iconColor.setSummary(strArr[1]);
                    break;
                case "black":
                    iconColor.setSummary(strArr[2]);
                    break;
            }
            iconColor.setOnPreferenceChangeListener((preference, newValue) ->

            {
                switch (newValue.toString()) {
                    case "关闭":
                        config.setIconColor("off");
                        break;
                    case "白色图标":
                        config.setIconColor("white");
                        break;
                    case "黑色图标":
                        config.setIconColor("black");
                        break;
                }
                iconColor.setSummary(newValue.toString());
                return true;
            });

            // 暂停关闭歌词
            CheckBoxPreference lyricOff = findPreference("lyricOff");
            assert lyricOff != null;
            lyricOff.setChecked(config.getLyricAutoOff());
            lyricOff.setOnPreferenceChangeListener((preference, newValue) ->

            {
                config.setLyricAutoOff((Boolean) newValue);
                return true;
            });

            // 隐藏通知图标
            CheckBoxPreference hideNoticeIcon = findPreference("hideNoticeIcon");
            assert hideNoticeIcon != null;
            hideNoticeIcon.setChecked(config.getHideNoticeIcon());
            hideNoticeIcon.setOnPreferenceChangeListener((preference, newValue) ->

            {
                config.setHideNoticeIcon((Boolean) newValue);
                return true;
            });

            // 隐藏实时网速
            CheckBoxPreference hideNetWork = findPreference("hideNetWork");
            assert hideNetWork != null;
            hideNetWork.setChecked(config.getHideNetSpeed());
            hideNetWork.setOnPreferenceChangeListener((preference, newValue) ->
            {
                config.setHideNetSpeed((Boolean) newValue);
                return true;
            });

            // 隐藏运营商名称
            CheckBoxPreference hideCUK = findPreference("hideCUK");
            assert hideCUK != null;
            hideCUK.setChecked(config.getHideCUK());
            hideCUK.setOnPreferenceChangeListener((preference, newValue) ->
            {
                config.setHideCUK((Boolean) newValue);
                return true;
            });
            // Debug模式
            CheckBoxPreference debug = findPreference("debug");
            assert debug != null;
            debug.setChecked(config.getDebug());
            debug.setOnPreferenceChangeListener((preference, newValue) ->
            {
                config.setDebug((Boolean) newValue);
                return true;
            });
            // 重启SystemUI
            Preference reSystemUI = findPreference("restartUI");
            assert reSystemUI != null;
            reSystemUI.setOnPreferenceClickListener(((preference) ->
            {
                new AlertDialog.Builder(requireActivity())
                        .setTitle("确定重启系统界面吗？")
                        .setMessage("若使用中突然发现不能使用，可尝试重启系统界面。")
//                        .setPositiveButton("确定", (dialog, which) -> Utils.killProcess("systemui"))
                        .setPositiveButton("确定", (dialog, which) -> {
                            try {
                                Process p = Runtime.getRuntime().exec("su");
                                OutputStream outputStream = p.getOutputStream();
                                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                                dataOutputStream.writeBytes("pkill -f com.android.systemui");
                                dataOutputStream.flush();
                                dataOutputStream.close();
                                outputStream.close();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
                return true;
            }));

            // 重置插件
            Preference reset = findPreference("reset");
            assert reset != null;
            reset.setOnPreferenceClickListener((preference) ->
            {
                new AlertDialog.Builder(requireActivity())
                        .setTitle("是否要重置模块")
                        .setMessage("模块没问题请不要随意重置")
                        .setPositiveButton("确定", (dialog, which) -> {
                            SharedPreferences userSettings = requireActivity().getSharedPreferences("miui.statusbar.lyric_preferences", 0);
                            SharedPreferences.Editor editor = userSettings.edit();
                            editor.clear();
                            editor.apply();
                            new File(Utils.ConfigPATH).delete();
                            Toast.makeText(requireActivity(), "重置成功", Toast.LENGTH_SHORT).show();
                            requireActivity().finishAffinity();
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
                return true;
            });


            //版本介绍
            Preference verExplain = findPreference("ver_explain");
            assert verExplain != null;
            verExplain.setSummary("当前版本: " + Utils.getLocalVersionCode(requireContext()));
            verExplain.setOnPreferenceClickListener((preference) ->
            {
                new AlertDialog.Builder(requireActivity())
                        .setTitle("当前版本[" + Utils.getLocalVersionCode(requireContext()) + "]适用于")
                        .setMessage("酷狗音乐:v10.8.4\n酷我音乐:v9.4.6.2\n网易云音乐:v8.5.40\nQQ音乐:v10.17.0.11")
                        .setPositiveButton("确定", null)
                        .create()
                        .show();
                return true;
            });


            // 作者主页
            Preference author = findPreference("author");
            assert author != null;
            author.setOnPreferenceClickListener((preference) ->
            {
                new AlertDialog.Builder(requireActivity())
                        .setTitle("作者主页")
                        .setNegativeButton("577fkj", (dialog, which) -> {
                            Uri uri = Uri.parse("https://github.com/577fkj");
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setPositiveButton("xiaowine", (dialog, which) -> {
                            Uri uri = Uri.parse("https://github.com/xiaowine");
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .create()
                        .show();

                return true;
            });

        }

        private void checkPermission() {
            if (ContextCompat.checkSelfPermission(requireActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), "android.permission.WRITE_EXTERNAL_STORAGE")) {
                    Toast.makeText(requireActivity(), "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
                }
                String[] strArr = new String[1];
                strArr[0] = "android.permission.WRITE_EXTERNAL_STORAGE";
                ActivityCompat.requestPermissions(requireActivity(), strArr, 1);
            }


        }
    }
}