package org.odk.collect.android.analytics

object AnalyticsEvents {
    /**
     * Track changes to the server URL setting. The action should be the scheme followed by a space
     * followed by one of Appspot, Ona, Kobo or Other. The label should be a hash of the URL.
     */
    const val SET_SERVER = "SetServer"

    /**
     * Track video requests with high resolution setting turned off. The action should be a hash of
     * the form definition.
     */
    const val REQUEST_VIDEO_NOT_HIGH_RES = "RequestVideoNotHighRes"

    /**
     * Track video requests with high resolution setting turned on. This is tracked to contextualize
     * the counts with the high resolution setting turned off since we expect that video is not very
     * common overall. The action should be a hash of the form definition.
     */
    const val REQUEST_HIGH_RES_VIDEO = "RequestHighResVideo"

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
     * Track form definitions with the saveIncomplete attribute. The action should be saveIncomplete
     * and the label should be a hash of the form definition.
     */
    const val SAVE_INCOMPLETE = "WidgetAttribute"

    /**
     * Tracks if any forms are being used as part of a workflow where instances are imported
     * from disk
     */
    const val IMPORT_INSTANCE = "ImportInstance"

    /**
     * Tracks if any forms are being used as part of a workflow where instances are imported
     * from disk and then encrypted
     */
    const val IMPORT_AND_ENCRYPT_INSTANCE = "ImportAndEncryptInstance"

    /**
     * Tracks responses from OpenMapKit to the OSMWidget
     */
    const val OPEN_MAP_KIT_RESPONSE = "OpenMapKitResponse"

    /**
     * Tracks how often users create shortcuts to forms
     */
    const val CREATE_SHORTCUT = "CreateShortcut"

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

    const val FORMS_PROVIDER_INSERT = "FormsProviderInsert"

    const val FORMS_PROVIDER_UPDATE = "FormsProviderUpdate"

    const val FORMS_PROVIDER_DELETE = "FormsProviderDelete"

    const val INSTANCE_PROVIDER_QUERY = "InstanceProviderQuery"

    const val INSTANCE_PROVIDER_INSERT = "InstanceProviderInsert"

    const val INSTANCE_PROVIDER_UPDATE = "InstanceProviderUpdate"

    const val INSTANCE_PROVIDER_DELETE = "InstanceProviderDelete"

    /**
     * Tracks how often "cellular_only" option is used in auto send
     */
    const val CELLULAR_ONLY = "CellularOnly"

    /**
     * Tracks how often non finalized forms are edited
     */
    const val EDIT_NON_FINALIZED_FORM = "EditNonFinalizedForm"

    /**
     * Tracks how often finalized forms are edited
     */
    const val EDIT_FINALIZED_FORM = "EditFinalizedForm"

    /**
     * Tracks how often form-level auto-delete setting is used
     */
    const val FORM_LEVEL_AUTO_DELETE = "FormLevelAutoDelete"

    /**
     * Tracks how often form-level auto-send setting is used
     */
    const val FORM_LEVEL_AUTO_SEND = "FormLevelAutoSend"

    /**
     * Tracks how often a form is finalized using a `ref` attribute on the `submission` element
     */
    const val PARTIAL_FORM_FINALIZED = "PartialFormFinalized"

    /**
     * Tracks how often drafts that can't be bulk finalized are attempted to be
     */
    const val BULK_FINALIZE_ENCRYPTED_FORM = "BulkFinalizeEncryptedForm"
    const val BULK_FINALIZE_SAVE_POINT = "BulkFinalizeSavePoint"
}
