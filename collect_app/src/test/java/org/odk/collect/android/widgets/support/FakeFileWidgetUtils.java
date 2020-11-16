package org.odk.collect.android.widgets.support;

import android.content.Context;
import android.net.Uri;

import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.MediaWidgetDataRequester;
import org.odk.collect.android.widgets.utilities.FileWidgetUtils;

public class FakeFileWidgetUtils extends FileWidgetUtils implements MediaWidgetDataRequester {

    public Object object;
    public String binaryName;
    public Uri uri;
    public boolean isImageType;

    @Override
    public String getUpdatedWidgetAnswer(Context context, QuestionMediaManager questionMediaManager, Object object,
                                         String questionIndex, String binaryName, Uri uri, boolean isImageType) {
        this.object = object;
        this.binaryName = binaryName;
        this.uri = uri;
        this.isImageType = isImageType;

        return object.toString();
    }
}
