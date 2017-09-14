package org.odk.collect.android.widgets;

import org.javarosa.core.model.data.IAnswerData;

/**
 * @author James Knight
 */
public interface IQuestionWidget {

    IAnswerData getAnswer();

    void clearAnswer();
}
