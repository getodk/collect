package org.odk.collect.android.listeners;

import android.net.Uri;

import org.odk.collect.android.widgets.QuestionWidget;

public interface WidgetAnswerListener {

    boolean saveAnswersForCurrentScreen(boolean evaluateConstraints);

    void saveChosenFile(QuestionWidget questionWidget, Uri uri);

    void refreshCurrentView();
}
