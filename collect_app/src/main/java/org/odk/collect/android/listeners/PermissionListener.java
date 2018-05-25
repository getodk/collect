package org.odk.collect.android.listeners;

/**
 * This listener serves the purpose of telling the calling activity if a
 * permission was granted or not. It's primarily utilized with the PermissionUtils
 * class that's delegated with the task of handling all permission related grants, dialogs
 * and other conditions so that that Activities can be cleaner and just know about the result
 * of a grant request.
 */
public interface PermissionListener {
    void granted();

    void denied();
}
