package org.odk.collect.android.utilities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/07/2018
 */

public final class FormDownloadBroadcastHelper {

    private FormDownloadBroadcastHelper() {}

    public static final String ACTION = "org.odk.collect.FORM_DOWNLOAD.COMPLETE";

    public static void sendDownloadServiceBroadcastResult(Context context, @Nullable String formId, boolean success, @Nullable String errorReason) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(ApplicationConstants.BundleKeys.SUCCESS_KEY, success);

        if (!success && errorReason != null) {
            intent.putExtra(ApplicationConstants.BundleKeys.ERROR_REASON, errorReason);
        }

        if (formId != null) {
            intent.putExtra(ApplicationConstants.BundleKeys.FORM_ID, formId);
        }

        context.sendBroadcast(intent);
    }
}
