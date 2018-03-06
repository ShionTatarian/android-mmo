package fi.qvik.android_mmo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Application specific settings.
 *
 * @author Tommy
 */
public class AppUtils {

    private static final String TAG = "AppUtils";

    private static AppUtils instance;
    private Context context;
    private SharedPreferences preferences;

    /**
     * Constructor.
     *
     * @param ctx
     */
    private AppUtils(Context ctx) {
        this.context = ctx;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

    }

    /**
     * Get singleton instance of {@link AppUtils}.
     *
     * @return Instance of {@link AppUtils}
     */
    public static AppUtils getInstance(Context context) {
        if (instance == null) {
            instance = new AppUtils(context);
        }

        return instance;
    }

    /**
     * Convenience method for storing String values to applications
     * {@link SharedPreferences}.
     *
     * @param key
     * @param value
     */
    protected void storeString(String key, String value) {
        Editor editor = this.preferences.edit();
        editor.putString(key, value);
        apply(editor);
    }

    protected void clearKey(String key) {
        Editor editor = this.preferences.edit();
        editor.remove(key);
        apply(editor);
    }

    /**
     * Load String from SharedPreferences. If no String can be found with given
     * key defValue is returned.
     *
     * @param key
     * @param defValue
     * @return Stored String or defValue.
     */
    protected String loadString(String key, String defValue) {
        return this.preferences.getString(key, defValue);
    }

    /**
     * Store boolean value to SharedPreferences.
     *
     * @param key
     * @param value
     */
    protected void storeBoolean(String key, boolean value) {
        Editor editor = this.preferences.edit();
        editor.putBoolean(key, value);
        apply(editor);
    }

    /**
     * Load stored Boolean value from SharedPreferences or given defValue.
     *
     * @param key
     * @param defValue
     * @return Stored Boolean value or given defValue if key is not found.
     */
    protected Boolean loadBoolean(String key, Boolean defValue) {
        if (!preferences.contains(key)) {
            // this is to support returning of null value
            return defValue;
        }

        return this.preferences.getBoolean(key, defValue != null ? defValue : false);
    }

    protected boolean preferencesContains(String key) {
        return this.preferences.contains(key);
    }

    /**
     * Store Integer value to SharedPreferences.
     *
     * @param key
     * @param value
     */
    protected void storeInt(String key, int value) {
        Editor editor = this.preferences.edit();
        editor.putInt(key, value);
        apply(editor);
    }

    /**
     * Load stored Integer value from SharedPreferences or given defValue.
     *
     * @param key
     * @param defValue
     * @return Stored Integer value or given defValue if key is not found.
     */
    protected int loadInt(String key, int defValue) {
        return this.preferences.getInt(key, defValue);
    }

    /**
     * Store Float value to SharedPreferenses.
     *
     * @param key
     * @param value
     */
    protected void storeFloat(String key, float value) {
        Editor editor = this.preferences.edit();
        editor.putFloat(key, value);
        apply(editor);
    }

    /**
     * Load stored Float value from SharedPreferences or given defValue.
     *
     * @param key
     * @param defValue
     * @return Stored Float value or given defValue if key is not found.
     */
    protected float loadFloat(String key, float defValue) {
        return this.preferences.getFloat(key, defValue);
    }

    /**
     * Store Long value to SharedPreferenses.
     *
     * @param key
     * @param value
     */
    protected void storeLong(String key, Long value) {
        Editor editor = this.preferences.edit();
        if (value == null) {
            editor.remove(key);
        } else {
            editor.putLong(key, value);
        }
        apply(editor);
    }

    /**
     * Load stored Long value from SharedPreferences or given defValue.
     *
     * @param key
     * @param defValue
     * @return Stored Long value or given defValue if key is not found.
     */
    protected long loadLong(String key, long defValue) {
        return this.preferences.getLong(key, defValue);
    }

    /**
     * Stores the editors stored values. If SDK version >= 9 then apply(editor)
     * is called. Else editor.commit() is called inside semaphore lock so that
     * editor commits do not clash.
     *
     * @param editor {@link Editor}
     */
    protected void apply(final Editor editor) {
        editor.apply();
    }

}
