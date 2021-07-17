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

package org.odk.collect.android.utilities;

import android.content.res.Configuration;

import androidx.annotation.NonNull;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.externaldata.ExternalDataUtil;

import java.util.Locale;

import timber.log.Timber;

public class Appearances {
    private static final String EXCEPTION_PARSING_COLUMNS = "Exception parsing columns";

    // Date appearances
    public static final String ETHIOPIAN                = "ethiopian";
    public static final String COPTIC                   = "coptic";
    public static final String ISLAMIC                  = "islamic";
    public static final String BIKRAM_SAMBAT            = "bikram-sambat";
    public static final String MYANMAR                  = "myanmar";
    public static final String PERSIAN                  = "persian";
    public static final String NO_CALENDAR              = "no-calendar";
    public static final String MONTH_YEAR               = "month-year";
    public static final String YEAR                     = "year";

    // Select one/multiple appearances
    @Deprecated public static final String COMPACT      = "compact";
    @Deprecated public static final String COMPACT_N    = "compact-";
    public static final String MINIMAL                  = "minimal";
    public static final String COLUMNS                  = "columns";
    public static final String COLUMNS_N                = "columns-";
    public static final String COLUMNS_PACK             = "columns-pack";
    @Deprecated public static final String QUICKCOMPACT = "quickcompact";
    @Deprecated public static final String SEARCH       = "search";
    public static final String AUTOCOMPLETE             = "autocomplete";
    public static final String LIST_NO_LABEL            = "list-nolabel";
    public static final String LIST                     = "list";
    public static final String LIKERT                   = "likert";
    public static final String LABEL                    = "label";
    public static final String IMAGE_MAP                = "image-map";
    public static final String NO_BUTTONS               = "no-buttons";
    public static final String QUICK                    = "quick";

    // Media appearances
    public static final String SIGNATURE                = "signature";
    public static final String ANNOTATE                 = "annotate";
    public static final String DRAW                     = "draw";
    @Deprecated public static final String SELFIE       = "selfie";
    public static final String NEW_FRONT                = "new-front";
    public static final String NEW                      = "new";
    public static final String FRONT                    = "front";

    // Maps appearances
    public static final String PLACEMENT_MAP            = "placement-map";
    public static final String MAPS                     = "maps";

    // Groups and repeats
    public static final String FIELD_LIST = "field-list";

    // Other appearances
    public static final String NO_APPEARANCE            = "";
    public static final String BEARING                  = "bearing";
    public static final String EX                       = "ex:";
    public static final String THOUSANDS_SEP            = "thousands-sep";
    public static final String PRINTER                  = "printer";
    public static final String NUMBERS                  = "numbers";
    public static final String URL                      = "url";
    public static final String RATING                   = "rating";

    private Appearances() {
    }

    // Get appearance hint and clean it up so it is lower case, without the search function and never null.
    public static @NonNull String getSanitizedAppearanceHint(FormEntryPrompt fep) {
        String appearance = fep.getAppearanceHint();
        if (appearance == null) {
            appearance = NO_APPEARANCE;
        } else {
            // For now, all appearance tags are in English.
            appearance = appearance.toLowerCase(Locale.ENGLISH);

            // Strip out the search() appearance/function which is handled in ExternalDataUtil so that
            // it is not considered when matching other appearances. For example, a file named list.csv
            // used as a parameter to search() should not be interpreted as a list appearance.
            appearance = ExternalDataUtil.SEARCH_FUNCTION_REGEX.matcher(appearance).replaceAll("");
        }

        return appearance;
    }

    /** Returns whether an appearance is present.  (Appearances are the constants above.) */
    public static boolean hasAppearance(FormEntryPrompt fep, String appearance) {
        return getSanitizedAppearanceHint(fep).contains(appearance);
    }

    /*
    Gets the number of columns that choices should be displayed in. In the case of the columns-n
    appearance (compact-n for backwards compatibility), that number is determined from the n in the
    appearance string. For columns (without any number), this is determined by the device screen size.
     */
    public static int getNumberOfColumns(FormEntryPrompt formEntryPrompt, ScreenUtils screenUtils) {
        int numColumns = 1;
        String appearance = getSanitizedAppearanceHint(formEntryPrompt);
        if (appearance.contains(COLUMNS_N) || appearance.contains(COMPACT_N)) {
            try {
                String columnsAppearance = appearance.contains(COLUMNS_N) ? COLUMNS_N : COMPACT_N;
                try {
                    appearance =
                            appearance.substring(appearance.indexOf(columnsAppearance));
                    int idx = appearance.indexOf(columnsAppearance);
                    if (idx != -1) {
                        String substringFromNumColumns = appearance.substring(idx + columnsAppearance.length());
                        numColumns = Integer.parseInt(substringFromNumColumns.substring(0, substringFromNumColumns.contains(" ")
                                ? substringFromNumColumns.indexOf(' ')
                                : substringFromNumColumns.length()));

                        if (numColumns < 1) {
                            numColumns = 1;
                        }
                    }
                } catch (Exception e) {
                    Timber.e(EXCEPTION_PARSING_COLUMNS);
                }
            } catch (Exception e) {
                Timber.e(EXCEPTION_PARSING_COLUMNS);
            }
        } else if (appearance.contains(COLUMNS)) {
            switch (screenUtils.getScreenSizeConfiguration()) {
                case Configuration.SCREENLAYOUT_SIZE_SMALL:
                    numColumns = 2;
                    break;
                case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                    numColumns = 3;
                    break;
                case Configuration.SCREENLAYOUT_SIZE_LARGE:
                    numColumns = 4;
                    break;
                case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                    numColumns = 5;
                    break;
                default:
                    numColumns = 3;
            }
        }
        return numColumns;
    }

    public static boolean isNoButtonsAppearance(FormEntryPrompt prompt) {
        return getSanitizedAppearanceHint(prompt).contains(NO_BUTTONS);
    }

    public static boolean isCompactAppearance(FormEntryPrompt prompt) {
        return getSanitizedAppearanceHint(prompt).contains(COMPACT);
    }

    public static boolean useThousandSeparator(FormEntryPrompt prompt) {
        return getSanitizedAppearanceHint(prompt).contains(THOUSANDS_SEP);
    }

    public static boolean isFrontCameraAppearance(FormEntryPrompt prompt) {
        String appearance = getSanitizedAppearanceHint(prompt);
        return appearance.contains(FRONT) || appearance.contains(NEW_FRONT) || appearance.contains(SELFIE);
    }

    public static boolean isFlexAppearance(FormEntryPrompt prompt) {
        String appearance = getSanitizedAppearanceHint(prompt);

        return !appearance.contains(COMPACT_N) && (appearance.contains(COMPACT)
                || appearance.contains(QUICKCOMPACT) || appearance.contains(COLUMNS_PACK));
    }

    public static boolean isAutocomplete(FormEntryPrompt prompt) {
        String appearance = getSanitizedAppearanceHint(prompt);
        return appearance.contains(Appearances.SEARCH) || appearance.contains(Appearances.AUTOCOMPLETE);
    }
}
