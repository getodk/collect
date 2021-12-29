package org.odk.collect.permissions

/**
 * This listener serves the purpose of telling the calling activity if a
 * permission was granted or not. It's primarily utilized with the PermissionsProvider
 * class that's delegated with the task of handling all permission related grants, dialogs
 * and other conditions so that that Activities can be cleaner and just know about the result
 * of a grant request.
 */
interface PermissionListener {
    fun granted()
    fun denied()
}
