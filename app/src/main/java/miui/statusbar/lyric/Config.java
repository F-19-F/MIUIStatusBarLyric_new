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
            FileInputStream fileInputStream = new FileInputStream(Utils.Config_PATH);
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
            FileOutputStream fileOutputStream = new FileOutputStream(Utils.Config_PATH);
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

    public void setHideIcons(Boolean bool) {
        try {
            this.config.put("hideIcons", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getHideIcons() {
        try {
            return (Boolean) this.config.get("hideIcons");
        } catch (JSONException e) {
            return true;
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
            return 35;
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
            return 35;
        }
    }


    public void setLyricAutoOff(Boolean bool) {
        try {
            this.config.put("LyricOff", bool);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public Boolean getLyricAutoOff() {
        try {
            return (Boolean) this.config.get("LyricOff");
        } catch (JSONException e) {
            return false;
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
            return "关闭";
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
            return false;
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
            return false;
        }
    }

    public void setIconReverseColor(String str) {
        try {
            this.config.put("IconReverseColor", str);
            setConfig(this.config.toString());
        } catch (JSONException ignored) {
        }
    }

    public String getIconReverseColor() {
        try {
            return (String) this.config.get("IconReverseColor");
        } catch (JSONException e) {
            return "关闭";
        }
    }
}