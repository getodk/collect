package org.odk.collect.android.formentry.javarosawrapper;

import androidx.annotation.Nullable;

import org.javarosa.core.model.FormIndex;

public class FormIndexUtils {

    private FormIndexUtils() {

    }

    /**
     * used to go up one level in the formIndex. That is, if you're at 5_0, 1 (the second question
     * in a repeating group), this method will return a FormIndex of 5_0 (the start of the repeating
     * group). If your at index 16 or 5_0, this will return null;
     */
    @Nullable
    public static FormIndex getPreviousLevel(FormIndex index) {
        if (index.isTerminal()) {
            return null;
        } else {
            return new FormIndex(getPreviousLevel(index.getNextLevel()), index);
        }
    }
}
