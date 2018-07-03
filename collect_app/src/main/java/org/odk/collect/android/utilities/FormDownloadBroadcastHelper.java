package org.odk.collect.android.utilities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.odk.collect.android.services.FormDownloadService;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/07/2018
 */

public class FormDownloadBroadcastHelper {

    public static final String ACTION = "org.odk.collect.FORM_DOWNLOAD.COMPLETE";
    public static final String SUCCESS_KEY = "SUCCESSFUL";
    public static final String ERROR_REASON = "ERROR_MSG";

    public static void sendDownloadServiceBroadcastResult(Context context, @Nullable String formId, boolean success, @Nullable String errorReason) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(SUCCESS_KEY, success);

        if (!success && errorReason != null) {
            intent.putExtra(ERROR_REASON, errorReason);
        }

        if (formId != null) {
            intent.putExtra(FormDownloadService.FORM_ID, formId);
        }

        context.sendBroadcast(intent);
    }
}
