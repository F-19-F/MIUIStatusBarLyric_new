package miui.statusbar.lyric;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Config {
    JSONObject config;

    public static String getConfig() {
        String str = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(Utils.ConfigPATH);
            byte[] bArr = new byte[fileInputStream.available()];
            fileInputStream.read(bArr);
            str = new String(bArr);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void setConfig(String str) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Utils.ConfigPATH);
            fileOutputStream.write(str.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config() {
        try {
            if (getConfig().equals("")) {
                this.config = new JSONObject();
                return;
            }
            this.config = new JSONObject(getConfig());
        } catch (JSONException ignored) {
        }
    }

    public void setHideLauncherIcon(Boolean bool) {
        try {
            this.config.put("HideLauncherIcon", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getHideLauncherIcon() {
        try {
            return (Boolean) this.config.get("HideLauncherIcon");
        } catch (JSONException e) {
            return false;
        }
    }

    public void setLyricService(Boolean bool) {
        try {
            this.config.put("LyricService", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getLyricService() {
        try {
            return (Boolean) this.config.get("LyricService");
        } catch (JSONException e) {
            return true;
        }
    }

    public void setLyricWidth(int i) {
        try {
            this.config.put("LyricWidth", i);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public int getLyricWidth() {
        try {
            return (Integer) this.config.get("LyricWidth");
        } catch (JSONException e) {
            return -1;
        }
    }

    public void setLyricMaxWidth(int i) {
        try {
            this.config.put("LyricMaxWidth", i);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public int getLyricMaxWidth() {
        try {
            return (Integer) this.config.get("LyricMaxWidth");
        } catch (JSONException e) {
            return -1;
        }
    }


    public void setLyricAutoOff(Boolean bool) {
        try {
            this.config.put("LyricAutoOff", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getLyricAutoOff() {
        try {
            return (Boolean) this.config.get("LyricAutoOff");
        } catch (JSONException e) {
            return true;
        }
    }

    public Boolean getHideNoticeIcon() {
        try {
            return (Boolean) this.config.get("hideNoticeIcon");
        } catch (JSONException e) {
            return false;
        }
    }

    public void setHideNoticeIcon(Boolean bool) {
        try {
            this.config.put("hideNoticeIcon", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public void setLyricColor(String str) {
        try {
            this.config.put("LyricColor", str);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public String getLyricColor() {
        try {
            return (String) this.config.get("LyricColor");
        } catch (JSONException e) {
            return "off";
        }
    }

    public void setHideNetSpeed(Boolean bool) {
        try {
            this.config.put("HideNetSpeed", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getHideNetSpeed() {
        try {
            return (Boolean) this.config.get("HideNetSpeed");
        } catch (JSONException e) {
            return true;
        }
    }

    public void setHideCUK(Boolean bool) {
        try {
            this.config.put("HideCUK", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getHideCUK() {
        try {
            return (Boolean) this.config.get("HideCUK");
        } catch (JSONException e) {
            return false;
        }
    }

    public void setDebug(Boolean bool) {
        try {
            this.config.put("debug", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getDebug() {
        try {
            return (Boolean) this.config.get("debug");
        } catch (JSONException e) {
            return false;
        }
    }

    public void setIcon(Boolean bool) {
        try {
            this.config.put("Icon", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public boolean getIcon() {
        try {
            return (Boolean) this.config.get("Icon");
        } catch (JSONException e) {
            return true;
        }
    }

    public void setIconAutoColor(Boolean bool) {
        try {
            this.config.put("IconAutoColor", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getIconAutoColor() {
        try {
            return (Boolean) this.config.get("IconAutoColor");
        } catch (JSONException e) {
            return true;
        }
    }
}