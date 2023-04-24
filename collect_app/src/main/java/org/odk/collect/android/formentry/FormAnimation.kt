package org.odk.collect.android.formentry

import android.content.Context
import org.odk.collect.strings.localization.isLTR

enum class FormAnimationType {
    LEFT, RIGHT, FADE
}

object FormAnimation {
    @JvmStatic
    fun getAnimationTypeBasedOnLanguageDirection(context: Context, formAnimationType: FormAnimationType): FormAnimationType {
        return if (context.isLTR()) {
            formAnimationType
        } else {
            when (formAnimationType) {
                FormAnimationType.LEFT -> FormAnimationType.RIGHT
                FormAnimationType.RIGHT -> FormAnimationType.LEFT
                else -> FormAnimationType.FADE
            }
        }
    }
}
