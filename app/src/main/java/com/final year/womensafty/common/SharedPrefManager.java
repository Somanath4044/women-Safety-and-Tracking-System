package com.pemchip.womensafty.common;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "MyAppPrefs";
    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    // Private constructor (Singleton)
    private SharedPrefManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Singleton instance getter
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // ======================
    // CRUD operations below
    // ======================

    // CREATE / UPDATE
    public void saveString(String key, String value) {
        editor.putString(key, value).apply();
    }

    public void saveInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    // READ
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // DELETE
    public void remove(String key) {
        editor.remove(key).apply();
    }

    // CLEAR ALL
    public void clear() {
        editor.clear().apply();
    }
}
