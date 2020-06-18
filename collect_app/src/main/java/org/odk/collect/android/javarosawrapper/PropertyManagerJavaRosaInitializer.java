package org.odk.collect.android.javarosawrapper;

import android.app.Application;

import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import static org.odk.collect.android.logic.PropertyManager.PROPMGR_USERNAME;
import static org.odk.collect.android.logic.PropertyManager.SCHEME_USERNAME;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_USERNAME;

public class PropertyManagerJavaRosaInitializer implements JavaRosaInitializer {

    private final Application application;

    public PropertyManagerJavaRosaInitializer(Application application) {
        this.application = application;
    }

    @Override
    public void initialize() {
        PropertyManager mgr = new PropertyManager(application);

        // Use the server username by default if the metadata username is not defined
        if (mgr.getSingularProperty(PROPMGR_USERNAME) == null || mgr.getSingularProperty(PROPMGR_USERNAME).isEmpty()) {
            mgr.putProperty(PROPMGR_USERNAME, SCHEME_USERNAME, (String) GeneralSharedPreferences.getInstance().get(KEY_USERNAME));
        }

        FormController.initializeJavaRosa(mgr);
    }
}
