package org.odk.collect.android.instancemanagement

import java.lang.Exception

class SubmitException(val type: Type) : Exception() {
    enum class Type {
        GOOGLE_ACCOUNT_NOT_SET,
        GOOGLE_ACCOUNT_NOT_PERMITTED,
        NOTHING_TO_SUBMIT
    }
}
