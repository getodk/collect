package org.odk.collect.android.utilities;

public interface MediaManagerListener {
    void markOriginalFileOrDelete(String questionIndex, String fileName);

    void replaceRecentFileForQuestion(String questionIndex, String fileName);
}

