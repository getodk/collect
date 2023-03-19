package org.odk.collect.android.javarosawrapper;

import androidx.annotation.Nullable;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;

public final class FormIndexUtils {

    private FormIndexUtils() {

    }

    /**
     * Used to find one level up from the formIndex. That is, if you're at 5_0, 1 (the second question
     * in a repeating group), this method will return a FormIndex of 5_0 (the start of the repeating
     * group). If you're at index 16 or 5_0, this will return null
     */
    @Nullable
    public static FormIndex getPreviousLevel(FormIndex index) {
        if (index.isTerminal()) {
            return null;
        } else {
            return new FormIndex(getPreviousLevel(index.getNextLevel()), index);
        }
    }

    @Nullable
    public static FormIndex getRepeatGroupIndex(FormIndex index, FormDef formDef) {
        IFormElement element = formDef.getChild(index);
        if (element instanceof GroupDef && ((GroupDef) element).getRepeat()) {
            return index;
        } else {
            FormIndex previousLevel = getPreviousLevel(index);

            if (previousLevel != null) {
                return getRepeatGroupIndex(previousLevel, formDef);
            } else {
                return null;
            }
        }
    }
}
