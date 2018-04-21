package org.odk.collect.android.tasks.sms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class SubmissionTracker {

    private SharedPreferences preferences;

    public SubmissionTracker(Context appContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    // Getters

    /**
     * Get int value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
     *
     * @param key SharedPreferences key
     * @return int value at 'key' or 'defaultValue' if key not found
     */
    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    /**
     * Get String value from SharedPreferences at 'key'. If key not found, return ""
     *
     * @param key SharedPreferences key
     * @return String value at 'key' or "" (empty String) if key not found
     */
    public String getString(String key) {
        return preferences.getString(key, "");
    }

    /**
     * Get parsed ArrayList of String from SharedPreferences at 'key'
     *
     * @param key SharedPreferences key
     * @return ArrayList of String
     */
    public ArrayList<String> getListString(String key) {
        return new ArrayList<String>(
                Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    /**
     * Get Arraylist of Objects from SharedPreferences at 'key'
     *
     * @param 	key 	SharedPreferences key
     * @param 	mClass 	Class of the stored object
     * @return 			Stored ArrayList of Objects
     */
    public ArrayList<Object> getListObject(String key, Class<?> mClass) {
        return getListObject(key, mClass, new Gson());
    }

    /**
     * Get Objects from SharedPreferences at 'key'
     *
     * @param 	key 		SharedPreferences key
     * @param 	classOfT 	Class of the stored object
     * @return 				Stored Object
     */
    public Object getObject(String key, Class<?> classOfT) {
        return getObject(key, classOfT, new Gson());
    }

    /**
     * Get Arraylist of Objects from SharedPreferences at 'key'
     *
     * @param 	key 	SharedPreferences key
     * @param 	mClass 	Class of the stored object
     * @param		gson	custom Gson object
     * @return 			Stored ArrayList of Objects
     */
    public ArrayList<Object> getListObject(String key, Class<?> mClass, Gson gson) {
        ArrayList<String> objStrings = getListString(key);
        ArrayList<Object> objects = new ArrayList<Object>();

        for (String jObjString : objStrings) {
            Object value = gson.fromJson(jObjString, mClass);
            objects.add(value);
        }
        return objects;
    }

    /**
     * Get Objects from SharedPreferences at 'key'
     *
     * @param 	key 		SharedPreferences key
     * @param 	classOfT 	Class of the stored object
     * @param		gson	custom Gson object
     * @return 				Stored Object
     */
    public Object getObject(String key, Class<?> classOfT, Gson gson) {
        String json = getString(key);
        Object value = gson.fromJson(json, classOfT);
        if (value == null) throw new NullPointerException();
        return value;
    }

    /**
     * Get Objects from SharedPreferences at 'key'
     *
     * @param 	key 		SharedPreferences key
     * @param 	token 	Gson token determing desired return type
     * @param		gson	custom Gson object
     * @return 				Stored Object
     */
    public <T> T getObject(String key, TypeToken<T> token, Gson gson) {
        String json = getString(key);
        T value = gson.fromJson(json, token.getType());
        if (value == null) throw new NullPointerException();
        return value;
    }

    // Put methods

    /**
     * Put int value into SharedPreferences with 'key' and save
     *
     * @param key SharedPreferences key
     * @param value int value to be added
     */
    public void putInt(String key, int value) {
        checkForNullKey(key);
        preferences.edit().putInt(key, value).apply();
    }


    /**
     * Put String value into SharedPreferences with 'key' and save
     *
     * @param key SharedPreferences key
     * @param value String value to be added
     */
    public void putString(String key, String value) {
        checkForNullKey(key);
        checkForNullValue(value);
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Put ArrayList of String into SharedPreferences with 'key' and save
     *
     * @param key SharedPreferences key
     * @param stringList ArrayList of String to be added
     */
    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    /**
     * Put boolean value into SharedPreferences with 'key' and save
     *
     * @param key SharedPreferences key
     * @param value boolean value to be added
     */
    public void putBoolean(String key, boolean value) {
        checkForNullKey(key);
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Put ArrayList of Boolean into SharedPreferences with 'key' and save
     *
     * @param key SharedPreferences key
     * @param boolList ArrayList of Boolean to be added
     */
    public void putListBoolean(String key, ArrayList<Boolean> boolList) {
        checkForNullKey(key);
        ArrayList<String> newList = new ArrayList<String>();

        for (Boolean item : boolList) {
            if (item) {
                newList.add("true");
            } else {
                newList.add("false");
            }
        }

        putListString(key, newList);
    }

    /**
     * Put ObJect any type into SharedPrefrences with 'key' and save
     *
     * @param key SharedPreferences key
     * @param obj is the Object you want to put
     */
    public void putObject(String key, Object obj) {
        putObject(key, obj, new Gson());
    }

    public void putListObject(String key, ArrayList<Object> objArray) {
        putListObject(key, objArray, new Gson());
    }

    /**
     * Put ObJect any type into SharedPrefrences with 'key' and save
     *
     * @param key SharedPreferences key
     * @param obj is the Object you want to put
     * @param gson custom Gson object
     */
    public void putObject(String key, Object obj, Gson gson) {
        checkForNullKey(key);
        putString(key, gson.toJson(obj));
    }

    public void putListObject(String key, ArrayList<Object> objArray, Gson gson) {
        checkForNullKey(key);
        ArrayList<String> objStrings = new ArrayList<String>();
        for (Object obj : objArray) {
            objStrings.add(gson.toJson(obj));
        }
        putListString(key, objStrings);
    }

    /**
     * Remove SharedPreferences item with 'key'
     *
     * @param key SharedPreferences key
     */
    public boolean contains(String key) {
        return preferences.contains(key);
    }

    /**
     * Remove SharedPreferences item with 'key'
     *
     * @param key SharedPreferences key
     */
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    /**
     * Delete image file at 'path'
     *
     * @param path path of image file
     * @return true if it successfully deleted, false otherwise
     */
    public boolean deleteImage(String path) {
        return new File(path).delete();
    }

    /**
     * Clear SharedPreferences (remove everything)
     */
    public void clear() {
        preferences.edit().clear().apply();
    }

    /**
     * Retrieve all values from SharedPreferences. Do not modify collection return by method
     *
     * @return a Map representing a list of key/value pairs from SharedPreferences
     */
    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    /**
     * Register SharedPreferences change listener
     *
     * @param listener listener object of OnSharedPreferenceChangeListener
     */
    public void registerOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {

        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregister SharedPreferences change listener
     *
     * @param listener listener object of OnSharedPreferenceChangeListener to be unregistered
     */
    public void unregisterOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {

        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive
     * measure
     *
     * @param key the pref key
     */
    public void checkForNullKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive
     * measure
     *
     * @param value the pref value
     */
    public void checkForNullValue(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
    }
}