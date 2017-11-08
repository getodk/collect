package org.odk.collect.android.external;


import android.content.Intent;

import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.exception.ExternalParamsException;

import java.util.Map;

public class ExternalAppsUtil {
    public String extractIntentName(String exString) {
        return ExternalAppsUtils.extractIntentName(exString);
    }

    public void populateParameters(Intent intent,
                                   Map<String, String> exParams,
                                   TreeReference reference)
            throws ExternalParamsException {

         ExternalAppsUtils.populateParameters(intent, exParams, reference);
    }
}
