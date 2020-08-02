package org.odk.collect.android.widgets.interfaces;

public interface MediaManagerListener {
    void markOriginalFileOrDelete(String questionIndex, String fileName);

    void replaceRecentFileForQuestion(String questionIndex, String fileName);
}
