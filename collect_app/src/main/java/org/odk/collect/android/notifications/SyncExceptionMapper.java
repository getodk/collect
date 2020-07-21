package org.odk.collect.android.notifications;

import org.odk.collect.android.formmanagement.matchexactly.SyncException;

public class SyncExceptionMapper {
    public String getMessage(SyncException exception) {
        return "Updating forms failed. Please try again.";
    }
}
