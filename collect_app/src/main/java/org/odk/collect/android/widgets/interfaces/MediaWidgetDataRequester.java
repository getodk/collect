package org.odk.collect.android.widgets.interfaces;

import android.content.Context;
import android.net.Uri;

import org.odk.collect.android.utilities.QuestionMediaManager;

public interface MediaWidgetDataRequester {

    String getUpdatedWidgetAnswer(Context context, QuestionMediaManager questionMediaManager, Object object,
                                  String questionIndex, String binaryName, Uri uri, boolean isImageType);
}
