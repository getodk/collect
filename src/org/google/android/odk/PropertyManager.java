package org.google.android.odk;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.javarosa.core.services.IPropertyManager;
import org.javarosa.core.services.IService;
import org.javarosa.core.services.properties.IPropertyRules;

import java.util.HashMap;
import java.util.Vector;

public class PropertyManager implements IService, IPropertyManager {

    private String t = "Property Manager";
    
    private Context mContext;

    private TelephonyManager mTelephonyManager;
    private HashMap<String, String> mProperties;

    private final static String DEVICE_ID_PROPERTY = "deviceid"; // imei
    private final static String SUBSCRIBER_ID_PROPERTY = "subscriberid"; // imsi
    private final static String SIM_SERIAL_PROPERTY = "simserial";
    private final static String PHONE_NUMBER_PROPERTY = "phonenumber";

    public String getName() {
        return "Property Manager";
    }

    public PropertyManager(Context context) {
        mContext = context;
        
        mProperties = new HashMap<String, String>();
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        
        mProperties.put(DEVICE_ID_PROPERTY, mTelephonyManager.getDeviceId());
        mProperties.put(SUBSCRIBER_ID_PROPERTY, mTelephonyManager.getSubscriberId());
        mProperties.put(SIM_SERIAL_PROPERTY, mTelephonyManager.getSimSerialNumber());
        mProperties.put(PHONE_NUMBER_PROPERTY, mTelephonyManager.getLine1Number());
    }

    @SuppressWarnings("unchecked")
    public Vector getProperty(String propertyName) {
        Log.i(t, "get property:" + propertyName);
        return null;
    }

    public String getSingularProperty(String propertyName) {
        Log.i(t, propertyName+":"+ mProperties.get(propertyName));
        return mProperties.get(propertyName.toLowerCase());
    }
    
    public void setProperty(String propertyName, String propertyValue) {
        Log.i(t, "set property string:" + propertyName + " value:" + propertyValue);
    }


    @SuppressWarnings("unchecked")
    public void setProperty(String propertyName, Vector propertyValue) {
        Log.i(t, "set property vector:" + propertyName + " value:" + propertyValue);
    }

    public void addRules(IPropertyRules rules) {
        // TODO Auto-generated method stub
        
    }

    @SuppressWarnings("unchecked")
    public Vector getRules() {
        // TODO Auto-generated method stub
        return null;
    }

}
