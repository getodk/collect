package org.odk.collect.android.analytics

object AnalyticsEvents {
    /**
     * Track changes to the server URL setting. The action should be the scheme followed by a space
     * followed by one of Appspot, Ona, Kobo or Other. The label should be a hash of the URL.
     */
    const val SET_SERVER = "SetServer"

    /**
     * Track submission encryption. The action should be a hash of the form definition.
     */
    const val ENCRYPT_SUBMISSION = "EncryptSubmission"

    /**
     * Track submissions. The action should describe how it's being sent and the label should be a
     * hash of the form definition.
     */
    const val SUBMISSION = "Submission"

    /**
     * Tracks how often instances that have been deleted on disk are opened for editing/viewing
     */
    const val OPEN_DELETED_INSTANCE = "OpenDeletedInstance"

    /**
     * Tracks how often users switch between projects
     */
    const val SWITCH_PROJECT = "ProjectSwitch"

    /**
     * Tracks how often users choose to try the demo project
     */
    const val TRY_DEMO = "ProjectCreateDemo"

    /**
     * Tracks how often projects are created using QR codes.
     */
    const val QR_CREATE_PROJECT = "ProjectCreateQR"

    /**
     * Tracks how often projects are created by manually entering details.
     */
    const val MANUAL_CREATE_PROJECT = "ProjectCreateManual"

    /**
     * Tracks how often projects with the same connection settings as an existing one are attempted
     * to be created.
     */
    const val DUPLICATE_PROJECT = "ProjectCreateDuplicate"

    /**
     * Tracks how often users try to create projects with the same connection settings but then decide
     * to switch to an existing project instead. This will give us a sense of whether users are getting
     * confused about project identity and trying to recreate the same one multiple times.
     */
    const val DUPLICATE_PROJECT_SWITCH = "ProjectCreateDuplicateSwitch"

    /**
     * Tracks how often users delete projects
     */
    const val DELETE_PROJECT = "ProjectDelete"

    /**
     * These events track how often users change project display settings
     */
    const val CHANGE_PROJECT_NAME = "ProjectChangeName"
    const val CHANGE_PROJECT_ICON = "ProjectChangeIcon"
    const val CHANGE_PROJECT_COLOR = "ProjectChangeColor"

    /**
     * Tracks how often users reconfigure a project using QR codes
     */
    const val RECONFIGURE_PROJECT = "ProjectReconfigure"

    const val FORMS_PROVIDER_QUERY = "FormsProviderQuery"

    const val FORMS_PROVIDER_DELETE = "FormsProviderDelete"

    const val INSTANCE_PROVIDER_QUERY = "InstanceProviderQuery"

    const val INSTANCE_PROVIDER_INSERT = "InstanceProviderInsert"

    const val INSTANCE_PROVIDER_DELETE = "InstanceProviderDelete"

    /**
     * Tracks how often drafts that can't be bulk finalized are attempted to be
     */
    const val BULK_FINALIZE_ENCRYPTED_FORM = "BulkFinalizeEncryptedForm"
    const val BULK_FINALIZE_SAVE_POINT = "BulkFinalizeSavePoint"

    /**
     * Tracks how often saved forms are manually deleted and in what number
     */
    const val DELETE_SAVED_FORM_FEW = "DeleteSavedFormFew" // < 10
    const val DELETE_SAVED_FORM_TENS = "DeleteSavedFormTens" // >= 10
    const val DELETE_SAVED_FORM_HUNDREDS = "DeleteSavedFormHundreds" // >= 100

    /**
     * Tracks how often the INSTANCE_UPLOAD action is used with a custom server URL
     */
    const val INSTANCE_UPLOAD_CUSTOM_SERVER = "InstanceUploadCustomServer"

    /**
     * Tracks how often projects are reset
     */
    const val RESET_PROJECT = "ResetProject"

    /**
     * Tracks how often finalized or sent forms are edited
     */
    const val EDIT_FINALIZED_OR_SENT_FORM = "EditFinalizedOrSentForm"

    /**
     * Tracks how often shortcuts for forms are added
     */
    const val ADD_SHORTCUT = "AddShortcut"
}
