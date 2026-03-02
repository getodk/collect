package org.odk.collect.timedgrid

enum class FinishType(val code: Int) {
    /** User confirms and picks last attempted item manually */
    CONFIRM_AND_PICK(1),

    /** User confirms, auto-pick last item in list */
    CONFIRM_AND_AUTO_PICK(2),

    /** No confirm, auto-pick last item in list */
    AUTO_PICK_NO_CONFIRM(3);

    companion object {
        fun fromInt(value: Int): FinishType =
            entries.find { it.code == value } ?: CONFIRM_AND_PICK
    }
}
