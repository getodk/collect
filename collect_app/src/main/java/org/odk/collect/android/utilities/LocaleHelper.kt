package org.odk.collect.android.utilities

import org.odk.collect.android.application.Collect
import org.odk.collect.shared.strings.StringUtils.isBlank
import java.util.Locale
import java.util.TreeMap

object LocaleHelper {
    private val TRANSLATIONS_AVAILABLE = arrayOf(
        "af", "am", "ar", "bg", "bn", "ca", "cs", "da", "de", "en", "es", "et", "fa", "fa_AF",
        "fi", "fr", "hi", "ht", "in", "it", "ja", "ka", "km", "ln", "lo_LA", "lt", "mg", "ml", "mr",
        "ms", "my", "ne_NP", "nl", "no", "pl", "ps", "pt", "ro", "ru", "rw", "si", "sl", "so", "sq",
        "sr", "sv_SE", "sw", "sw_KE", "te", "th_TH", "ti", "tl", "tr", "uk", "ur", "vi", "zh", "zu"
    )

    @JvmStatic
    fun languageList(): TreeMap<String, String> {
        val languageList = TreeMap<String, String>(java.lang.String.CASE_INSENSITIVE_ORDER)
        for (localeCode in TRANSLATIONS_AVAILABLE) {
            val locale = getLocale(localeCode)
            languageList[locale.getDisplayName(locale)] = localeCode
        }
        return languageList
    }

    @JvmStatic
    fun getLocale(localeCode: String?): Locale {
        val sanitizedLocaleCode =
            if (localeCode == null || isBlank(localeCode)) Collect.defaultSysLanguage else localeCode

        return if (sanitizedLocaleCode.contains("_")) {
            val args = sanitizedLocaleCode.split("_").toTypedArray()
            Locale(args[0], args[1])
        } else {
            Locale(sanitizedLocaleCode)
        }
    }
}
