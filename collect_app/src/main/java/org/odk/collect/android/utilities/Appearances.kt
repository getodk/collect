/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.utilities

import android.content.res.Configuration
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.externaldata.ExternalDataUtil
import org.odk.collect.androidshared.utils.ScreenUtils
import java.lang.Exception

object Appearances {
    // Date appearances
    const val ETHIOPIAN = "ethiopian"
    const val COPTIC = "coptic"
    const val ISLAMIC = "islamic"
    const val BIKRAM_SAMBAT = "bikram-sambat"
    const val MYANMAR = "myanmar"
    const val PERSIAN = "persian"
    const val NO_CALENDAR = "no-calendar"
    const val MONTH_YEAR = "month-year"
    const val YEAR = "year"

    // Select one/multiple appearances
    @Deprecated("")
    const val COMPACT = "compact"

    @Deprecated("")
    const val COMPACT_N = "compact-"
    const val MINIMAL = "minimal"
    const val COLUMNS = "columns"
    const val COLUMNS_N = "columns-"
    const val COLUMNS_PACK = "columns-pack"

    @Deprecated("")
    const val QUICKCOMPACT = "quickcompact"

    @Deprecated("")
    const val SEARCH = "search"
    const val AUTOCOMPLETE = "autocomplete"
    const val LIST_NO_LABEL = "list-nolabel"
    const val LIST = "list"
    const val LIKERT = "likert"
    const val LABEL = "label"
    const val IMAGE_MAP = "image-map"
    const val NO_BUTTONS = "no-buttons"
    const val QUICK = "quick"
    const val MAP = "map"

    // Media appearances
    const val SIGNATURE = "signature"
    const val ANNOTATE = "annotate"
    const val DRAW = "draw"

    @Deprecated("")
    const val SELFIE = "selfie"
    const val NEW_FRONT = "new-front"
    const val NEW = "new"
    const val FRONT = "front"

    // Maps appearances
    const val PLACEMENT_MAP = "placement-map"
    const val MAPS = "maps"

    // Groups and repeats
    const val FIELD_LIST = "field-list"

    // Other appearances
    const val NO_APPEARANCE = ""
    const val BEARING = "bearing"
    const val EX = "ex:"
    const val THOUSANDS_SEP = "thousands-sep"
    const val PRINTER = "printer"
    const val NUMBERS = "numbers"
    const val URL = "url"
    const val RATING = "rating"

    // Get appearance hint and clean it up so it is lower case, without the search function and never null.
    @JvmStatic
    fun getSanitizedAppearanceHint(fep: FormEntryPrompt): String {
        var appearance = fep.appearanceHint
        if (appearance == null) {
            appearance = NO_APPEARANCE
        } else {
            // For now, all appearance tags are in English.
            appearance = appearance.lowercase()

            // Strip out the search() appearance/function which is handled in ExternalDataUtil so that
            // it is not considered when matching other appearances. For example, a file named list.csv
            // used as a parameter to search() should not be interpreted as a list appearance.
            appearance = ExternalDataUtil.SEARCH_FUNCTION_REGEX.matcher(appearance).replaceAll("")
        }
        return appearance
    }

    /** Returns whether an appearance is present.  (Appearances are the constants above.)  */
    @JvmStatic
    fun hasAppearance(fep: FormEntryPrompt, appearance: String): Boolean {
        return getSanitizedAppearanceHint(fep).contains(appearance)
    }

    /*
    Gets the number of columns that choices should be displayed in. In the case of the columns-n
    appearance (compact-n for backwards compatibility), that number is determined from the n in the
    appearance string. For columns (without any number), this is determined by the device screen size.
     */
    @JvmStatic
    fun getNumberOfColumns(formEntryPrompt: FormEntryPrompt, screenUtils: ScreenUtils): Int {
        var numColumns = 1
        var appearance = getSanitizedAppearanceHint(formEntryPrompt)
        if (appearance.contains(COLUMNS_N) || appearance.contains(COMPACT_N)) {
            try {
                val columnsAppearance = if (appearance.contains(COLUMNS_N)) COLUMNS_N else COMPACT_N
                appearance = appearance.substring(appearance.indexOf(columnsAppearance))
                val idx = appearance.indexOf(columnsAppearance)
                if (idx != -1) {
                    val substringFromNumColumns = appearance.substring(idx + columnsAppearance.length)
                    numColumns = substringFromNumColumns.substring(
                        0,
                        if (substringFromNumColumns.contains(" ")) {
                            substringFromNumColumns.indexOf(
                                ' '
                            )
                        } else {
                            substringFromNumColumns.length
                        }
                    ).toInt()
                    if (numColumns < 1) {
                        numColumns = 1
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        } else if (appearance.contains(COLUMNS)) {
            numColumns = when (screenUtils.screenSizeConfiguration) {
                Configuration.SCREENLAYOUT_SIZE_SMALL -> 2
                Configuration.SCREENLAYOUT_SIZE_NORMAL -> 3
                Configuration.SCREENLAYOUT_SIZE_LARGE -> 4
                Configuration.SCREENLAYOUT_SIZE_XLARGE -> 5
                else -> 3
            }
        }
        return numColumns
    }

    @JvmStatic
    fun isNoButtonsAppearance(prompt: FormEntryPrompt): Boolean {
        return getSanitizedAppearanceHint(prompt).contains(NO_BUTTONS)
    }

    @JvmStatic
    fun isCompactAppearance(prompt: FormEntryPrompt): Boolean {
        return getSanitizedAppearanceHint(prompt).contains(COMPACT)
    }

    @JvmStatic
    fun useThousandSeparator(prompt: FormEntryPrompt): Boolean {
        return getSanitizedAppearanceHint(prompt).contains(THOUSANDS_SEP)
    }

    @JvmStatic
    fun isFrontCameraAppearance(prompt: FormEntryPrompt): Boolean {
        val appearance = getSanitizedAppearanceHint(prompt)
        return appearance.contains(FRONT) || appearance.contains(NEW_FRONT) || appearance.contains(SELFIE)
    }

    @JvmStatic
    fun isFlexAppearance(prompt: FormEntryPrompt): Boolean {
        val appearance = getSanitizedAppearanceHint(prompt)
        return !appearance.contains(COMPACT_N) &&
            (appearance.contains(COMPACT) || appearance.contains(QUICKCOMPACT) || appearance.contains(COLUMNS_PACK))
    }

    @JvmStatic
    fun isAutocomplete(prompt: FormEntryPrompt): Boolean {
        val appearance = getSanitizedAppearanceHint(prompt)
        return appearance.contains(SEARCH) || appearance.contains(AUTOCOMPLETE)
    }
}
