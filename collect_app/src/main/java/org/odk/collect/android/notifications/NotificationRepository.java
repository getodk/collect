package org.odk.collect.android.notifications;

public interface NotificationRepository {

    void markFormUpdateNotified(String formId, String formHash, String manifestHash);

    boolean hasFormUpdateBeenNotified(String formHash, String manifestHash);
}
