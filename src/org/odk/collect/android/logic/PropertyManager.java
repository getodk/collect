/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.javarosa.core.services.IPropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;
import org.odk.collect.android.preferences.PreferencesActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Used to return device properties to JavaRosa
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class PropertyManager implements IPropertyManager {

    private String t = "PropertyManager";

    private Context mContext;

    private TelephonyManager mTelephonyManager;
    private HashMap<String, String> mProperties;

    public final static String DEVICE_ID_PROPERTY = "deviceid"; // imei
    private final static String SUBSCRIBER_ID_PROPERTY = "subscriberid"; // imsi
    private final static String SIM_SERIAL_PROPERTY = "simserial";
    private final static String PHONE_NUMBER_PROPERTY = "phonenumber";
    private final static String USERNAME = "username";
    private final static String EMAIL = "email";

    public final static String OR_DEVICE_ID_PROPERTY = "uri:deviceid"; // imei
    public final static String OR_SUBSCRIBER_ID_PROPERTY = "uri:subscriberid"; // imsi
    public final static String OR_SIM_SERIAL_PROPERTY = "uri:simserial";
    public final static String OR_PHONE_NUMBER_PROPERTY = "uri:phonenumber";
    public final static String OR_USERNAME = "uri:username";
    public final static String OR_EMAIL = "uri:email";


    public String getName() {
        return "Property Manager";
    }


    public PropertyManager(Context context) {
        Log.i(t, "calling constructor");

        mContext = context;

        mProperties = new HashMap<String, String>();
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = mTelephonyManager.getDeviceId();
        String orDeviceId = null;
        if (deviceId != null ) {
        	if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
        		deviceId =
        				Settings.Secure
                        	.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        		orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
        	} else {
        		orDeviceId = "imei:" + deviceId;
        	}
        }

        if ( deviceId == null ) {
        	// no SIM -- WiFi only
        	// Retrieve WiFiManager
        	WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

    		// Get WiFi status
    		WifiInfo info = wifi.getConnectionInfo();
    		if ( info != null ) {
    			deviceId = info.getMacAddress();
    			orDeviceId = "mac:" + deviceId;
    		}
        }

        // if it is still null, use ANDROID_ID
        if ( deviceId == null ) {
            deviceId =
                    Settings.Secure
                            .getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    		orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
        }

        mProperties.put(DEVICE_ID_PROPERTY, deviceId);
        mProperties.put(OR_DEVICE_ID_PROPERTY, orDeviceId);

        String value;

        value = mTelephonyManager.getSubscriberId();
        if ( value != null ) {
        	mProperties.put(SUBSCRIBER_ID_PROPERTY, value);
        	mProperties.put(OR_SUBSCRIBER_ID_PROPERTY, "imsi:" + value);
        }
        value = mTelephonyManager.getSimSerialNumber();
        if ( value != null ) {
        	mProperties.put(SIM_SERIAL_PROPERTY, value);
        	mProperties.put(OR_SIM_SERIAL_PROPERTY, "simserial:" + value);
        }
        value = mTelephonyManager.getLine1Number();
        if ( value != null ) {
        	mProperties.put(PHONE_NUMBER_PROPERTY, value);
        	mProperties.put(OR_PHONE_NUMBER_PROPERTY, "tel:" + value);
        }

        // Get the username from the settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        value = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        if ( value != null ) {
        	mProperties.put(USERNAME, value);
        	mProperties.put(OR_USERNAME, "username:" + value);
        }
        value = settings.getString(PreferencesActivity.KEY_SELECTED_GOOGLE_ACCOUNT, null);
        if ( value != null ) {
        	mProperties.put(EMAIL, value);
        	mProperties.put(OR_EMAIL, "mailto:" + value);
        }
    }

    @Override
    public Vector<String> getProperty(String propertyName) {
        return null;
    }


    @Override
    public String getSingularProperty(String propertyName) {
    	// for now, all property names are in english...
        return mProperties.get(propertyName.toLowerCase(Locale.ENGLISH));
    }


    @Override
    public void setProperty(String propertyName, String propertyValue) {
    }


    @Override
    public void setProperty(String propertyName, @SuppressWarnings("rawtypes") Vector propertyValue) {

    }


    @Override
    public void addRules(IPropertyRules rules) {

    }


    @Override
    public Vector<IPropertyRules> getRules() {
        return null;
    }

}
