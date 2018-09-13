package org.odk.collect.android.listeners;

import android.net.Uri;

public interface WidgetAnswerListener {

    boolean saveAnswersForCurrentScreen(boolean evaluateConstraints);

    void saveChosenImage(Uri uri);
}
