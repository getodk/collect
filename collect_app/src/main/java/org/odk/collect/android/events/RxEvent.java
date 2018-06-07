package org.odk.collect.android.events;

/**
 * Base event that all RxEvent classes should extend.
 * All classes being passed through the event bus should have a post fix of RxEvent so that other
 * developers know it's purpose.
 */
public abstract class RxEvent {
   private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
