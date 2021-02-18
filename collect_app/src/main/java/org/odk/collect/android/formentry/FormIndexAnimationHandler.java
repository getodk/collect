package org.odk.collect.android.formentry;

import androidx.annotation.Nullable;

import org.javarosa.core.model.FormIndex;

/**
 * Responsible for determining how a new "screen" in a form should be animated to based on
 * the {@link FormIndex}.
 */
public class FormIndexAnimationHandler {

    public enum Direction {
        FORWARDS,
        BACKWARDS
    }

    private final Listener listener;
    private FormIndex lastIndex;

    public FormIndexAnimationHandler(Listener listener) {
        this.listener = listener;
    }

    public void handle(@Nullable FormIndex index) {
        if (index == null) {
            return;
        }

        if (lastIndex == null) {
            listener.onScreenRefresh();
        } else {
            if (index.compareTo(lastIndex) > 0) {
                listener.onScreenChange(Direction.FORWARDS);
            } else if (index.compareTo(lastIndex) < 0) {
                listener.onScreenChange(Direction.BACKWARDS);
            } else {
                listener.onScreenRefresh();
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
        void onScreenChange(Direction direction);

        void onScreenRefresh();
    }
}
