package msbl.miuistatusbarlyric;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class SettingsActivity extends AppCompatActivity {

    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Config config;



        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            checkPermission();
            init();
            config = new Config();

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
                lyricWidth.setDialogMessage("(-1~100，-1为自适应)，当前:" + lyricWidth.getSummary());
            } else {
                lyricWidth.setDialogMessage("(-1~100，-1为自适应)，当前:" + lyricWidth.getSummary());
            }

            lyricWidth.setOnPreferenceChangeListener((preference, newValue) -> {
                lyricWidth.setSummary(newValue.toString());
                lyricWidth.setDefaultValue(newValue);
                if (newValue.toString().equals("-1")) {
                    lyricWidth.setSummary("自适应");
                    lyricWidth.setDialogMessage("(-1~100，-1为自适应)，当前:自适应");
                } else {
                    lyricWidth.setDialogMessage("(-1~100，-1为自适应)，当前:" + newValue.toString() + "%");
                }
                config.setLyricWidth(Integer.parseInt(newValue.toString()));
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
                    lyricMaxWidth.setDefaultValue("关闭");
                } else {
                    lyricMaxWidth.setDialogMessage("(-1~100，-1为关闭，仅在歌词宽度为自适应时生效)，当前:" + newValue.toString() + "%");
                }
                config.setLyricMaxWidth(Integer.parseInt(newValue.toString()));
                return true;
            });

            // 隐藏通知图标
            CheckBoxPreference hideNoti = findPreference("hideNoti");
            assert hideNoti != null;
            hideNoti.setChecked(config.getHideNoti());
            hideNoti.setOnPreferenceChangeListener((preference, newValue) -> {
                config.setHideNoti((Boolean) newValue);
                return true;
            });

            // 暂停关闭歌词
            CheckBoxPreference lyricOff = findPreference("lyricOff");
            assert lyricOff != null;
            lyricOff.setChecked(config.getLyricOff());
            lyricOff.setOnPreferenceChangeListener((preference, newValue) -> {
                config.setLyricOff((Boolean) newValue);
                return true;
            });

            // 重启SystemUI
            Preference reSystemUI = findPreference("restartUI");
            assert reSystemUI != null;
            reSystemUI.setOnPreferenceClickListener(((preference) -> {
                new AlertDialog.Builder(requireActivity())
                        .setTitle("确定重启系统界面吗？")
                        .setMessage("若使用中突然发现不能使用，可尝试重启系统界面。")
                        .setPositiveButton("确定", (dialog, which) -> killProcess("systemui"))
                        .create()
                        .show();
                return true;
            }));

            //版本介绍
            Preference verExplain = findPreference("ver_explain");
            assert verExplain != null;
            verExplain.setSummary("当前版本: " + Utlis.getLocalVersionName(requireContext()));
            verExplain.setOnPreferenceClickListener((preference) -> {
                new AlertDialog.Builder(requireActivity())
                        .setTitle("当前版本[" + Utlis.getLocalVersionName(requireContext()) + "]适用于")
                        .setMessage("酷狗音乐:v10.8.4\n酷我音乐:v9.4.6.2\n网易云音乐:v8.5.30\nQQ音乐:v10.17.0.11")
                        .setPositiveButton("确定", (dialog, which) -> {})
                        .create()
                        .show();
                return true;
            });

            // 重置插件
            Preference reset = findPreference("reset");
            assert reset != null;
            reset.setOnPreferenceClickListener((preference) -> {
                new AlertDialog.Builder(requireActivity())
                        .setTitle("是否要重置模块")
                        .setMessage("模块没问题请不要随意重置")
                        .setPositiveButton("确定", (dialog, which) -> {
                            delete(new File(Utlis.PATH));
                            delete(new File("/data/data/msbl.miuistatusbarlyric/shared_prefs/"));
                            Toast.makeText(requireActivity(), "重置成功", Toast.LENGTH_SHORT).show();
                            System.exit(0);
                        })
                        .create()
                        .show();
                return true;
            });

            // 歌词颜色
            EditTextPreference lyricColour = findPreference("lyricColour");
            assert lyricColour != null;
            lyricColour.setSummary(config.getLyricColor());
            lyricColour.setDialogMessage("请输入16进制颜色代码，例如: #C0C0C0，目前：" + config.getLyricColor());
            lyricColour.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!newValue.toString().equals("关闭")) {
                    try {
                        Color.parseColor(newValue.toString());
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "颜色代码不正确!", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                lyricColour.setDialogMessage("请输入16进制颜色代码，例如: #C0C0C0，目前：" + newValue.toString());
                lyricColour.setSummary(newValue.toString());
                config.setLyricColor(newValue.toString());
                return true;
            });

            // 隐藏实时网速
            CheckBoxPreference hideNetWork = findPreference("hideNetWork");
            assert hideNetWork != null;
            hideNetWork.setChecked(config.getHideNetSpeed());
            hideNetWork.setOnPreferenceChangeListener((preference, newValue) -> {
                config.setHideNetSpeed((Boolean) newValue);
                return true;
            });

            // 隐藏运营商名称
            CheckBoxPreference hideCUK = findPreference("hideCUK");
            assert hideCUK != null;
            hideCUK.setChecked(config.getHideCUK());
            hideCUK.setOnPreferenceChangeListener((preference, newValue) -> {
                config.setHideCUK((Boolean) newValue);
                return true;
            });
        }

        private void checkPermission() {
            if (ContextCompat.checkSelfPermission(requireActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), "android.permission.WRITE_EXTERNAL_STORAGE")) {
                    Toast.makeText(getActivity(), "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
                }
                String[] strArr = new String[1];
                strArr[0] = "android.permission.WRITE_EXTERNAL_STORAGE";
                ActivityCompat.requestPermissions(requireActivity(), strArr, 1);
                return;
            }
            Log.e("MIUIStatBar", "checkPermission: 已经授权！");
        }

        public void killProcess(String str) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("su");
                DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
                dataOutputStream.write(("pgrep -l " + str + "\n").getBytes());
                dataOutputStream.flush();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String stringBuffer = "kill -9 " + bufferedReader.readLine() + "\n";
                bufferedReader.close();
                dataOutputStream.writeBytes(stringBuffer);
                dataOutputStream.flush();
                dataOutputStream.close();
                process.destroy();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                process.waitFor();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }


        public void init() {
            File file = new File(Utlis.PATH);
            File file2 = new File(Utlis.PATH + "Config.json");
            if (!file.exists()) {
                file.mkdirs();
            }
            if (!file2.exists()) {
                try {
                    Config config = new Config();
                    file2.createNewFile();
                    config.setLyricService(true);
                    config.setLyricWidth(-1);
                    config.setLyricMaxWidth(-1);
                    config.setFanse("关闭");
                    config.setLyricOff(false);
                    config.setHideNoti(false);
                    config.setLyricColor("关闭");
                    config.setHideCUK(false);
                    config.setHideNetSpeed(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}