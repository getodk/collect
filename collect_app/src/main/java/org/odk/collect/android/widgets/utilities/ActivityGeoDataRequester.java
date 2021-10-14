package org.odk.collect.android.widgets.utilities;

import static org.odk.collect.geo.Constants.EXTRA_DRAGGABLE_ONLY;
import static org.odk.collect.geo.Constants.EXTRA_READ_ONLY;
import static org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.widgets.interfaces.GeoDataRequester;
import org.odk.collect.geo.GeoPointActivity;
import org.odk.collect.geo.GeoPointMapActivity;
import org.odk.collect.geo.GeoPolyActivity;

public class ActivityGeoDataRequester implements GeoDataRequester {

    private final PermissionsProvider permissionsProvider;

    public ActivityGeoDataRequester(PermissionsProvider permissionsProvider) {
        this.permissionsProvider = permissionsProvider;
    }

    @Override
    public void requestGeoPoint(Context context, FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry) {

        permissionsProvider.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Bundle bundle = new Bundle();

                if (answerText != null && !answerText.isEmpty()) {
                    bundle.putDoubleArray(GeoPointMapActivity.EXTRA_LOCATION, GeoWidgetUtils.getLocationParamsFromStringAnswer(answerText));
                }

                bundle.putDouble(GeoPointActivity.EXTRA_ACCURACY_THRESHOLD, GeoWidgetUtils.getAccuracyThreshold(prompt.getQuestion()));
                bundle.putBoolean(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt));
                bundle.putBoolean(EXTRA_READ_ONLY, prompt.isReadOnly());
                bundle.putBoolean(EXTRA_DRAGGABLE_ONLY, hasPlacementMapAppearance(prompt));

                Intent intent = new Intent(context, isMapsAppearance(prompt) ? GeoPointMapActivity.class : GeoPointActivity.class);
                intent.putExtras(bundle);
                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.LOCATION_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    @Override
    public void requestGeoShape(Context context, FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry) {
        permissionsProvider.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Intent intent = new Intent(context, GeoPolyActivity.class);
                intent.putExtra(GeoPolyActivity.ANSWER_KEY, answerText);
                intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE);
                intent.putExtra(EXTRA_READ_ONLY, prompt.isReadOnly());
                intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt));

                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    @Override
    public void requestGeoTrace(Context context, FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry) {
        permissionsProvider.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Intent intent = new Intent(context, GeoPolyActivity.class);
                intent.putExtra(GeoPolyActivity.ANSWER_KEY, answerText);
                intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOTRACE);
                intent.putExtra(EXTRA_READ_ONLY, prompt.isReadOnly());
                intent.putExtra(EXTRA_RETAIN_MOCK_ACCURACY, getAllowMockAccuracy(prompt));

                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    private boolean getAllowMockAccuracy(FormEntryPrompt prompt) {
        return Boolean.parseBoolean(FormEntryPromptUtils.getAttributeValue(prompt, "allow-mock-accuracy"));
    }

    private boolean isMapsAppearance(FormEntryPrompt prompt) {
        return hasMapsAppearance(prompt) || hasPlacementMapAppearance(prompt);
    }

    private boolean hasMapsAppearance(FormEntryPrompt prompt) {
        return Appearances.hasAppearance(prompt, Appearances.MAPS);
    }

    private boolean hasPlacementMapAppearance(FormEntryPrompt prompt) {
        return Appearances.hasAppearance(prompt, Appearances.PLACEMENT_MAP);
    }
}
