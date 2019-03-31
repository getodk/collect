package org.odk.collect.android.ui.formdownload;

import android.support.annotation.Nullable;

import java.util.HashMap;

public interface FormDownloadNavigator {

    void setReturnResult(boolean successful, @Nullable String message, @Nullable HashMap<String, Boolean> resultFormIds);

    void goBack();
}
