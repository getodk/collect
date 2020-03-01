package org.odk.collect.android.formentry;

import org.javarosa.core.model.FormIndex;

/**
 * Responsible for determining how a new "screen" in a form should be animated to based on
 * the {@link FormIndex}.
 */
public class FormIndexAnimationHandler {

    private final Listener listener;
    private FormIndex lastIndex;

    public FormIndexAnimationHandler(Listener listener) {
        this.listener = listener;
    }

    public void handle(FormIndex index) {
        if (lastIndex == null) {
            listener.refreshCurrentView();
        } else {
            if (index.compareTo(lastIndex) > 0) {
                listener.animateToNextView();
            } else if (index.compareTo(lastIndex) < 0) {
                listener.animateToPreviousView();
            } else {
                listener.refreshCurrentView();
            }
        }

        lastIndex = index;
    }

    /**
     * Can be used to update the handler on the starting index in situations
     * where {@link #handle(FormIndex)} isn't be called.
     */
    public void setLastIndex(FormIndex lastIndex) {
        this.lastIndex = lastIndex;
    }

    public interface Listener {
        void animateToPreviousView();

        void animateToNextView();

        void refreshCurrentView();
    }
}
