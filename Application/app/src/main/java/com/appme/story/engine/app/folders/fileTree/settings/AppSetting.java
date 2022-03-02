package com.appme.story.engine.app.folders.fileTree.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.appme.story.R;

public class AppSetting {

    public static final String FILE_PATH = "last_file";
    public static final String LAST_FIND = "LAST_FIND";
    public static final String LAST_REPLACE = "LAST_REPLACE";
    public static final String TAB_POSITION_FILE = "TAB_POSITION_FILE";
    private static final String TAG = AppSetting.class.getSimpleName();

    @NonNull
    protected SharedPreferences.Editor editor;
    @NonNull
    protected Context context;
    @NonNull
    private SharedPreferences sharedPreferences;

    @SuppressLint("CommitPrefEdits")
    public AppSetting(@NonNull Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = sharedPreferences.edit();
    }

    @SuppressLint("CommitPrefEdits")
    public AppSetting(@NonNull SharedPreferences mPreferences, @NonNull Context context) {
        this.context = context;
        this.sharedPreferences = mPreferences;
        this.editor = sharedPreferences.edit();
    }

    /**
     * reset default setting
     *
     * @param context
     */
    public static void setFirstOpen(Context context) {
        String key = "first_open";
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    @NonNull
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void put(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void put(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void put(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void put(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int def) {
        try {
            return sharedPreferences.getInt(key, def);
        } catch (Exception e) {
            try {
                return Integer.parseInt(getString(key));
            } catch (Exception ignored) {
                return def;
            }
        }
    }

    /**
     * get long value from key,
     *
     * @param key - key
     * @return -1 if not found
     */
    public long getLong(String key) {
        try {
            return sharedPreferences.getLong(key, -1);
        } catch (Exception e) {
            try {
                return Long.parseLong(getString(key));
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    public String getString(String key) {
        String s = "";
        try {
            s = sharedPreferences.getString(key, "");
        } catch (Exception ignored) {
        }
        return s;
    }

    public boolean getBoolean(String key) {
        try {
            return sharedPreferences.getBoolean(key, false);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean getBoolean(String key, boolean def) {
        try {
            return sharedPreferences.getBoolean(key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public boolean useFullScreen() {
        return getBoolean(context.getString(R.string.key_full_screen));
    }

}

