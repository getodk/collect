package org.odk.collect.android.instancemanagement

import java.lang.Exception

class SubmitException(val type: Type) : Exception() {
    enum class Type {
        NOTHING_TO_SUBMIT
    }
}
