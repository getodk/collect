package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

public class ActivityGeoDataRequester {
    public static final String LOCATION = "gp";
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    public static final String DRAGGABLE_ONLY = "draggable";

    private final PermissionUtils permissionUtils;

    public ActivityGeoDataRequester() {
        permissionUtils = new PermissionUtils();
    }

    public ActivityGeoDataRequester(PermissionUtils permissionUtils) {
        this.permissionUtils = permissionUtils;
    }

    public void requestGeoPoint(Context context, FormEntryPrompt prompt, WaitingForDataRegistry waitingForDataRegistry) {

        permissionUtils.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Bundle bundle = new Bundle();

                String answer = prompt.getAnswerText();
                if (answer != null && !answer.isEmpty()) {
                    bundle.putDoubleArray(LOCATION, GeoWidgetUtils.getLocationParamsFromStringAnswer(answer));
                }
                bundle.putDouble(ACCURACY_THRESHOLD, GeoWidgetUtils.getAccuracyThreshold(prompt.getQuestion()));
                bundle.putBoolean(READ_ONLY, prompt.isReadOnly());

                boolean isMapAppearance = true;
                if (WidgetAppearanceUtils.hasAppearance(prompt, WidgetAppearanceUtils.PLACEMENT_MAP)) {
                    bundle.putBoolean(DRAGGABLE_ONLY, true);
                } else if (WidgetAppearanceUtils.hasAppearance(prompt, WidgetAppearanceUtils.MAPS)) {
                    bundle.putBoolean(DRAGGABLE_ONLY, false);
                } else {
                    isMapAppearance = false;
                }

                Intent intent = new Intent(context, isMapAppearance ? GeoPointMapActivity.class : GeoPointActivity.class);
                intent.putExtras(bundle);
                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.LOCATION_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    public void requestGeoShape(Context context, FormEntryPrompt prompt, WaitingForDataRegistry waitingForDataRegistry) {
        permissionUtils.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Intent intent = new Intent(context, GeoPolyActivity.class);
                intent.putExtra(GeoPolyActivity.ANSWER_KEY, prompt.getAnswerText());
                intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE);
                intent.putExtra(READ_ONLY, prompt.isReadOnly());

                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    public void requestGeoTrace(Context context, FormEntryPrompt prompt, WaitingForDataRegistry waitingForDataRegistry) {
        permissionUtils.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Intent intent = new Intent(context, GeoPolyActivity.class);
                intent.putExtra(GeoPolyActivity.ANSWER_KEY, prompt.getAnswerText());
                intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOTRACE);
                intent.putExtra(READ_ONLY, prompt.isReadOnly());

                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }
}