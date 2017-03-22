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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.javarosa.core.services.IPropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Returns device properties to JavaRosa
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class PropertyManager implements IPropertyManager {

    private static final String TAG = "PropertyManager";

    private static final String ANDROID6_FAKE_MAC = "02:00:00:00:00:00";

    private final Map<String, String> mProperties = new HashMap<>();

    public final static String DEVICE_ID_PROPERTY           = "deviceid";       // imei
    public final static String SUBSCRIBER_ID_PROPERTY       = "subscriberid";   // imsi
    public final static String SIM_SERIAL_PROPERTY          = "simserial";
    public final static String PHONE_NUMBER_PROPERTY        = "phonenumber";
    public final static String USERNAME_PROPERTY            = "username";
    public final static String EMAIL_PROPERTY               = "email";

    public String getName() {
        return "Property Manager";
    }

    private class IdAndPrefix {
        String id;
        String prefix;

        IdAndPrefix(String id, String prefix) {
            this.id = id;
            this.prefix = prefix;
        }
    }

    public PropertyManager(Context context) {
        Log.i(TAG, "calling constructor");

        // User-defined properties
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        initUserDefinedProperty(preferences, PHONE_NUMBER_PROPERTY,    "tel");
        initUserDefinedProperty(preferences, USERNAME_PROPERTY,        "username");
        initUserDefinedProperty(preferences, EMAIL_PROPERTY,           "mailto");

        // Device-defined properties
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        IdAndPrefix idp = findDeviceId(context, telephonyManager);
        putProperty(DEVICE_ID_PROPERTY,     idp.prefix,    idp.id);
        putProperty(SUBSCRIBER_ID_PROPERTY, "imsi",        telephonyManager.getSubscriberId());
        putProperty(SIM_SERIAL_PROPERTY,    "simserial",   telephonyManager.getSimSerialNumber());
    }

    private IdAndPrefix findDeviceId(Context context, TelephonyManager telephonyManager) {
        final String androidIdName = Settings.Secure.ANDROID_ID;
        String deviceId = telephonyManager.getDeviceId();
        String prefix = null;

        if (deviceId != null) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId = Settings.Secure.getString(context.getContentResolver(), androidIdName);
                prefix = androidIdName;
            } else {
                prefix = "imei";
            }
        }

        if (deviceId == null) {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();
            if (info != null && !ANDROID6_FAKE_MAC.equals(info.getMacAddress())) {
                deviceId = info.getMacAddress();
                prefix = "mac";
            }
        }

        // if it is still null, use ANDROID_ID
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), androidIdName);
            prefix = androidIdName;
        }

        return new IdAndPrefix(deviceId, prefix);
    }

    /**
     * Initializes a property and its associated “with URI” property, from shared preferences.
     * @param settings the shared preferences object to be used
     * @param propName the name of the property to set
     * @param prefix the string prepended to the value for the associated “with URI” property
     */
    private void initUserDefinedProperty(SharedPreferences settings, String propName, String prefix) {
        String value = settings.getString(propName, null);
        putProperty(propName, prefix, value);
    }

    private void putProperty(String propName, String prefix, String value) {
        if (value != null) {
            mProperties.put(propName, value);
            mProperties.put(withUri(propName), prefix + ":" + value);
        }
    }

    @Override
    public List<String> getProperty(String propertyName) {
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
    public void setProperty(String propertyName, List<String> propertyValue) {
    }

    @Override
    public void addRules(IPropertyRules rules) {
    }

    @Override
    public List<IPropertyRules> getRules() {
        return null;
    }

    public static String withUri(String name) {
        return "uri:" + name;
    }
}
