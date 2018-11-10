package org.odk.collect.android.widgets.interfaces;

import android.widget.Button;

import org.javarosa.core.model.data.IAnswerData;

import java.util.List;

/**
 * @author James Knight
 */
public interface Widget {

    IAnswerData getAnswer();

    void clearAnswer();

    void waitForData();

    void cancelWaitingForData();

    boolean isWaitingForData();

    List<Button> getSimpleButtonList();
}
