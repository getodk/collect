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

import org.javarosa.core.services.IPropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.ReadPhoneStatePermissionRxEvent;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.shared.Settings;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.DeviceDetailsProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_METADATA_PHONENUMBER;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_METADATA_USERNAME;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_USERNAME;

/**
 * Returns device properties and metadata to JavaRosa
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class PropertyManager implements IPropertyManager {

    public static final String PROPMGR_DEVICE_ID        = "deviceid";
    public static final String PROPMGR_PHONE_NUMBER     = "phonenumber";
    public static final String PROPMGR_USERNAME         = "username";
    public static final String PROPMGR_EMAIL            = "email";

    public static final String SCHEME_USERNAME     = "username";
    private static final String SCHEME_TEL          = "tel";
    private static final String SCHEME_MAILTO       = "mailto";

    private final Map<String, String> properties = new HashMap<>();

    @Inject
    RxEventBus eventBus;

    @Inject
    DeviceDetailsProvider deviceDetailsProvider;

    @Inject
    PermissionsProvider permissionsProvider;

    @Inject
    SettingsProvider settingsProvider;

    public String getName() {
        return "Property Manager";
    }

    public PropertyManager() {
        Collect.getInstance().getComponent().inject(this);

        reload();
    }

    public PropertyManager(RxEventBus rxEventBus, PermissionsProvider permissionsProvider, DeviceDetailsProvider deviceDetailsProvider, SettingsProvider settingsProvider) {
        this.eventBus = rxEventBus;
        this.permissionsProvider = permissionsProvider;
        this.deviceDetailsProvider = deviceDetailsProvider;
        this.settingsProvider = settingsProvider;
    }

    public PropertyManager reload() {
        try {
            putProperty(PROPMGR_DEVICE_ID,     "",         deviceDetailsProvider.getDeviceId());
            putProperty(PROPMGR_PHONE_NUMBER,  SCHEME_TEL,          deviceDetailsProvider.getLine1Number());
        } catch (SecurityException e) {
            Timber.i(e);
        }

        // User-defined properties. Will replace any above with the same PROPMGR_ name.
        Settings generalSettings = settingsProvider.getUnprotectedSettings();
        initUserDefined(generalSettings, KEY_METADATA_USERNAME,    PROPMGR_USERNAME,      SCHEME_USERNAME);
        initUserDefined(generalSettings, KEY_METADATA_PHONENUMBER, PROPMGR_PHONE_NUMBER,  SCHEME_TEL);
        initUserDefined(generalSettings, KEY_METADATA_EMAIL,       PROPMGR_EMAIL,         SCHEME_MAILTO);

        // Use the server username by default if the metadata username is not defined
        if (getSingularProperty(PROPMGR_USERNAME) == null || getSingularProperty(PROPMGR_USERNAME).isEmpty()) {
            putProperty(PROPMGR_USERNAME, SCHEME_USERNAME, settingsProvider.getUnprotectedSettings().getString(KEY_USERNAME));
        }

        return this;
    }

    /**
     * Initializes a property and its associated “with URI” property, from shared preferences.
     * @param generalSettings the preferences object to be used
     * @param prefKey the preferences key
     * @param propName the name of the property to set
     * @param scheme the scheme for the associated “with URI” property
     */
    private void initUserDefined(Settings generalSettings, String prefKey,
                                 String propName, String scheme) {
        putProperty(propName, scheme, generalSettings.getString(prefKey));
    }

    public void putProperty(String propName, String scheme, String value) {
        if (value != null) {
            properties.put(propName, value);
            properties.put(withUri(propName), scheme + ":" + value);
        }
    }

    @Override
    public List<String> getProperty(String propertyName) {
        return null;
    }

    @Override
    public String getSingularProperty(String propertyName) {
        if (!permissionsProvider.isReadPhoneStatePermissionGranted() && isPropertyDangerous(propertyName)) {
            eventBus.post(new ReadPhoneStatePermissionRxEvent());
        }

        // for now, all property names are in english...
        return properties.get(propertyName.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Dangerous properties are those which require reading phone state:
     * https://developer.android.com/reference/android/Manifest.permission#READ_PHONE_STATE
     * @param propertyName The name of the property
     * @return True if the given property is dangerous, false otherwise.
     */
    private boolean isPropertyDangerous(String propertyName) {
        return propertyName != null && propertyName.equalsIgnoreCase(PROPMGR_PHONE_NUMBER);
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
