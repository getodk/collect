package org.odk.collect.android.widgets.interfaces;

import org.javarosa.core.model.data.IAnswerData;

import javax.annotation.Nullable;

/**
 * @author James Knight
 */
public interface Widget {

    @Nullable
    IAnswerData getAnswer();

    void clearAnswer();
}
