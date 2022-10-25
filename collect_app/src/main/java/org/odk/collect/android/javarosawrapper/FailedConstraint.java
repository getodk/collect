package org.odk.collect.android.javarosawrapper;

import org.javarosa.core.model.FormIndex;

public class FailedConstraint {
    public final FormIndex index;
    public final int status;

    public FailedConstraint(FormIndex index, int status) {
        this.index = index;
        this.status = status;
    }
}
