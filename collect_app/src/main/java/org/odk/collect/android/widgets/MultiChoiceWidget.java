package org.odk.collect.android.widgets;

/**
 * @author James Knight
 */
public interface MultiChoiceWidget extends Widget {
    int getChoiceCount();

    void setChoiceSelected(int choiceIndex, boolean isSelected);
}
