package org.odk.collect.android.utilities

/**
 * Used to allow tests to understand whether a UI action has been successfully detected or not
 */
object ActionRegister {

    @JvmStatic
    var isActionDetected = false
        private set

    @JvmStatic
    fun attemptingAction() {
        isActionDetected = false
    }

    @JvmStatic
    fun actionDetected() {
        isActionDetected = true
    }
}
