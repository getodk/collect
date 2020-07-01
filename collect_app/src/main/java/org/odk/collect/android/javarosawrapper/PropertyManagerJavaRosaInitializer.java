package org.odk.collect.android.javarosawrapper;

import android.app.Application;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.XFormParser;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler;

public class PropertyManagerJavaRosaInitializer implements JavaRosaInitializer {

    private static boolean isJavaRosaInitialized;

    private final Application application;

    public PropertyManagerJavaRosaInitializer(Application application) {
        this.application = application;
    }

    @Override
    public void initialize() {
        PropertyManager mgr = new PropertyManager(application);

        if (!isJavaRosaInitialized) {
            // Register prototypes for classes that FormDef uses
            PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
            PrototypeManager.registerPrototypes(CoreModelModule.classNames);
            new XFormsModule().registerModule();

            // When registering prototypes from Collect, a proguard exception also needs to be added
            PrototypeManager.registerPrototype("org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointAction");
            XFormParser.registerActionHandler(CollectSetGeopointActionHandler.ELEMENT_NAME, new CollectSetGeopointActionHandler());

            isJavaRosaInitialized = true;
        }

        org.javarosa.core.services.PropertyManager
                .setPropertyManager(mgr);
    }
}
