package org.odk.collect.android.widgets.utilities

import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

object QuestionFontSizeUtils {
    const val DEFAULT_FONT_SIZE = 21
    private const val HEADLINE_6_DIFF = -1
    private const val SUBTITLE_1_DIFF = -5
    private const val BODY_MEDIUM_DIFF = -7
    private const val BODY_LARGE_DIFF = -5

    @JvmStatic
    fun getFontSize(settings: Settings, fontSize: FontSize?): Int {
        val settingsValue = settings.getString(ProjectKeys.KEY_FONT_SIZE)!!.toInt()

        return when (fontSize) {
            FontSize.HEADLINE_6 -> settingsValue + HEADLINE_6_DIFF
            FontSize.SUBTITLE_1 -> settingsValue + SUBTITLE_1_DIFF
            FontSize.BODY_MEDIUM -> settingsValue + BODY_MEDIUM_DIFF
            FontSize.BODY_LARGE -> settingsValue + BODY_LARGE_DIFF
            else -> throw IllegalArgumentException()
        }
    }

    @JvmStatic
    @Deprecated("Use {@link QuestionFontSizeUtils#getFontSize(Settings, FontSize)} instead")
    fun getQuestionFontSize(): Int {
        return try {
            val fontSize = DaggerUtils.getComponent(Collect.getInstance()).settingsProvider().getUnprotectedSettings().getString(ProjectKeys.KEY_FONT_SIZE)!!.toInt()
            fontSize + HEADLINE_6_DIFF
        } catch (e: Exception) {
            DEFAULT_FONT_SIZE
        } catch (e: Error) {
            DEFAULT_FONT_SIZE
        }
    }

    enum class FontSize {
        HEADLINE_6, SUBTITLE_1, BODY_MEDIUM, BODY_LARGE
    }
}
