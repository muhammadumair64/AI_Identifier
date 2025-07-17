package com.iobits.tech.app.ai_identifier.manager;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PreferenceManager {

    private static final String SETTINGS_NAME = "default_settings";
    private static PreferenceManager sSharedPrefs;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private boolean mBulkUpdate = false;

    public enum Key {
        IS_APP_ADS_FREE,
        IS_APP_FIRST_RUN,
        IS_APP_PREMIUM,
        POST_IMAGE_URI,
        IS_FIRST_REMINDERS_OPEN,
        CAMERA_FLASH,
        CAMERA_TIMER,
        CAMERA_FOCUS,
        FOLDER_NAME,
        CAMERA_SOUND,
        rateUsKey,
        SCAN_COUNT,
        SCAN_ANYTHING_COUNT,
        ID_DOG_COUNT,
        ID_PLANT_COUNT,
        ID_CAT_COUNT,
        ID_INSECT_COUNT,
        ID_BIRD_COUNT,
        ID_OBJECT_COUNT,
        ID_ROCK_COUNT,
        ID_MUSHROOM_COUNT,
        ID_CELEBRITY_COUNT,
        ID_COUNTRY_COUNT
    }

    @Inject
    public PreferenceManager(Context context) {
        mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }


    public static PreferenceManager getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new PreferenceManager(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    public static PreferenceManager getInstance() {
        if (sSharedPrefs != null) {
            return sSharedPrefs;
        }
        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");

    }

    public void put(Key key, String val) {
        doEdit();
        mEditor.putString(key.name(), val);
        doCommit();
    }

    public void put(Key key, int val) {
        doEdit();
        mEditor.putInt(key.name(), val);
        doCommit();
    }

    public void put(Key key, boolean val) {
        doEdit();
        mEditor.putBoolean(key.name(), val);
        doCommit();
    }

    public void put(Key key, float val) {
        doEdit();
        mEditor.putFloat(key.name(), val);
        doCommit();
    }


    public void put(Key key, double val) {
        doEdit();
        mEditor.putString(key.name(), String.valueOf(val));
        doCommit();
    }

    public void put(Key key, long val) {
        doEdit();
        mEditor.putLong(key.name(), val);
        doCommit();
    }

    public String getString(Key key, String defaultValue) {
        return mPref.getString(key.name(), defaultValue);
    }

    public String getString(Key key) {
        return mPref.getString(key.name(), null);
    }

    public int getInt(Key key) {
        return mPref.getInt(key.name(), 0);
    }

    public int getInt(Key key, int defaultValue) {
        return mPref.getInt(key.name(), defaultValue);
    }

    public long getLong(Key key) {
        return mPref.getLong(key.name(), 0);
    }

    public long getLong(Key key, long defaultValue) {
        return mPref.getLong(key.name(), defaultValue);
    }

    public float getFloat(Key key) {
        return mPref.getFloat(key.name(), 0);
    }

    public float getFloat(Key key, float defaultValue) {
        return mPref.getFloat(key.name(), defaultValue);
    }

    public double getDouble(Key key) {
        return getDouble(key, 0);
    }


    public double getDouble(Key key, double defaultValue) {
        try {
            return Double.valueOf(mPref.getString(key.name(), String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public boolean getBoolean(Key key, boolean defaultValue) {
        return mPref.getBoolean(key.name(), defaultValue);
    }

    public boolean getBoolean(Key key) {
        return mPref.getBoolean(key.name(), false);
    }

    public void remove(Key... keys) {
        doEdit();
        for (Key key : keys) {
            mEditor.remove(key.name());
        }
        doCommit();
    }


    public void clear() {
        doEdit();
        mEditor.clear();
        doCommit();
    }

    public void edit() {
        mBulkUpdate = true;
        mEditor = mPref.edit();
    }

    public void commit() {
        mBulkUpdate = false;
        mEditor.commit();
        mEditor = null;
    }

    private void doEdit() {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doCommit() {
        if (!mBulkUpdate && mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }
}
