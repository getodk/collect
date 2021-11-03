package org.odk.collect.android.utilities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.externaldata.ExternalAppsUtils;

import java.util.Map;

public class ExternalAppIntentProvider {
    // If an extra with this key is specified, it will be parsed as a URI and used as intent data
    private static final String URI_KEY = "uri_data";

    public Intent getIntentToRunExternalApp(FormEntryPrompt formEntryPrompt) throws ExternalParamsException, XPathSyntaxException {
        String exSpec = formEntryPrompt.getAppearanceHint().replaceFirst("^ex[:]", "");
        final String intentName = ExternalAppsUtils.extractIntentName(exSpec);
        final Map<String, String> exParams = ExternalAppsUtils.extractParameters(exSpec);

        Intent intent = new Intent(intentName);

        // Use special "uri_data" key to set intent data. This must be done before checking if an
        // activity is available to handle implicit intents.
        if (exParams.containsKey(URI_KEY)) {
            String uriValue = (String) ExternalAppsUtils.getValueRepresentedBy(exParams.get(URI_KEY),
                    formEntryPrompt.getIndex().getReference());
            intent.setData(Uri.parse(uriValue));
            exParams.remove(URI_KEY);
        }

        ExternalAppsUtils.populateParameters(intent, exParams, formEntryPrompt.getIndex().getReference());
        return intent;
    }

    // https://github.com/getodk/collect/issues/4194
    public Intent getIntentToRunExternalAppWithoutDefaultCategory(FormEntryPrompt formEntryPrompt, PackageManager packageManager) throws ExternalParamsException {
        String exSpec = formEntryPrompt.getAppearanceHint().replaceFirst("^ex[:]", "");
        final String intentName = ExternalAppsUtils.extractIntentName(exSpec);
        final Map<String, String> exParams = ExternalAppsUtils.extractParameters(exSpec);

        Intent intent = packageManager.getLaunchIntentForPackage(intentName);
        if (intent != null) {
            // Make sure FLAG_ACTIVITY_NEW_TASK is not set because it doesn't work with startActivityForResult
            intent.setFlags(0);
            ExternalAppsUtils.populateParameters(intent, exParams, formEntryPrompt.getIndex().getReference());
        }

        return intent;
    }
}
