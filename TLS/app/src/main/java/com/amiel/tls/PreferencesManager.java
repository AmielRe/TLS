package com.amiel.tls;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    private static final String PREF_NAME = "com.amiel.tls.sharedPref";
    private static final String IS_ADMIN_KEY = "com.amiel.tls.isAdmin";
    private static final String ROOM_ID_KEY = "com.amiel.tls.roomID";
    private static final String IS_FIRST_LAUNCH = "com.amiel.tls.firstLaunch";

    private static PreferencesManager sInstance;
    private final SharedPreferences mPref;

    private PreferencesManager(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(context);
        }
    }

    public static synchronized PreferencesManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }

    public void setIsAdminValue(boolean value) {
        mPref.edit()
                .putBoolean(IS_ADMIN_KEY, value)
                .apply();
    }

    public boolean getIsAdminValue() {
        return mPref.getBoolean(IS_ADMIN_KEY, false);
    }

    public boolean getFirstLaunch() { return mPref.getBoolean(IS_FIRST_LAUNCH, true); }

    public void setFirstLaunch(boolean value) {
        mPref.edit()
            .putBoolean(IS_FIRST_LAUNCH, value)
            .apply();
    }

    public void setRoomIDValue(int value) {
        mPref.edit()
                .putInt(ROOM_ID_KEY, value)
                .apply();
    }

    public int getRoomIDValue() {
        return mPref.getInt(ROOM_ID_KEY, -1);
    }

    public void remove(String key) {
        mPref.edit()
                .remove(key)
                .apply();
    }

    public boolean clear() {
        return mPref.edit()
                .clear()
                .commit();
    }
}
