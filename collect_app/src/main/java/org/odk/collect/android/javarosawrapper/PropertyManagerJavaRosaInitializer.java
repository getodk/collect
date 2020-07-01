package org.odk.collect.android.javarosawrapper;

import android.app.Application;

import org.odk.collect.android.logic.PropertyManager;

public class PropertyManagerJavaRosaInitializer implements JavaRosaInitializer {

    private final PropertyManager propertyManager;
    private final Application application;

    public PropertyManagerJavaRosaInitializer(Application application, PropertyManager propertyManager) {
        this.application = application;
        this.propertyManager = propertyManager;
    }

    @Override
    public void initialize() {
        propertyManager.reload(application);
    }
}
