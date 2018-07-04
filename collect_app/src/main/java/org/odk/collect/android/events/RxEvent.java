package org.odk.collect.android.events;

/**
 * Base event that all RxEvent classes should extend.
 * All classes being passed through the event bus should have names ending in RxEvent so that other
 * developers know its purpose.
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
