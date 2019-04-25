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

import android.content.Context;
import android.content.res.Configuration;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.external.ExternalDataUtil;

import java.util.Locale;

import androidx.annotation.NonNull;
import timber.log.Timber;

public class WidgetAppearances {
    public static final String NO_APPEARANCE    = "";
    public static final String ETHIOPIAN        = "ethiopian";
    public static final String COPTIC           = "coptic";
    public static final String ISLAMIC          = "islamic";
    public static final String BIKRAM_SAMBAT    = "bikram-sambat";
    public static final String NO_CALENDAR      = "no-calendar";
    public static final String MONTH_YEAR       = "month-year";
    public static final String YEAR             = "year";
    public static final String BEARING          = "bearing";
    public static final String EX               = "ex:";
    public static final String THOUSANDS_SEP    = "thousands-sep";
    public static final String PRINTER          = "printer";
    public static final String NUMBERS          = "numbers";
    public static final String QUICK            = "quick";
    public static final String URL              = "url";
    public static final String SIGNATURE        = "signature";
    public static final String ANNOTATE         = "annotate";
    public static final String DRAW             = "draw";
    public static final String COMPACT          = "compact";
    public static final String COMPACT_N        = "compact-";
    public static final String QUICKCOMPACT     = "quickcompact";
    public static final String COLUMNS          = "columns";
    public static final String COLUMNS_N        = "columns-";
    public static final String COLUMNS_FLEX     = "columns-flex";
    public static final String MINIMAL          = "minimal";
    public static final String SEARCH           = "search";
    public static final String AUTOCOMPLETE     = "autocomplete";
    public static final String LIST_NO_LABEL    = "list-nolabel";
    public static final String LIST             = "list";
    public static final String LABEL            = "label";
    public static final String IMAGE_MAP        = "image-map";
    public static final String RATING           = "rating";
    public static final String NO_BUTTONS       = "no-buttons";
    public static final String SELFIE           = "selfie";
    public static final String NEW_FRONT        = "new-front";
    public static final String NEW              = "new";
    public static final String FRONT            = "front";
    public static final String PLACEMENT_MAP    = "placement-map";
    public static final String MAPS             = "maps";

    private WidgetAppearances() {
    }

    // Get appearance hint and clean it up so it is lower case, without the search function and never null.
    @NonNull
    public static String getAppearance(FormEntryPrompt fep) {
        String appearance = fep.getAppearanceHint();
        if (appearance == null) {
            appearance = WidgetAppearances.NO_APPEARANCE;
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

    public static int getNumberOfColumns(FormEntryPrompt formEntryPrompt, Context context) {
        int numColumns = 1;
        String appearance = WidgetAppearances.getAppearance(formEntryPrompt);
        if (!appearance.startsWith(WidgetAppearances.COMPACT_N) && (appearance.startsWith(WidgetAppearances.COMPACT)
                || appearance.startsWith(WidgetAppearances.QUICKCOMPACT)
                || appearance.startsWith(WidgetAppearances.COLUMNS_FLEX))) {
            numColumns = -1;
            try {
                String firstWord = appearance.split("\\s+")[0];
                int idx = firstWord.indexOf('-');
                if (idx != -1) {
                    numColumns = Integer.parseInt(firstWord.substring(idx + 1));
                }
            } catch (Exception e) {
                // Do nothing, leave numColumns as -1
                Timber.e("Exception parsing columns");
            }
        } else if (appearance.contains(WidgetAppearances.COLUMNS_N) || appearance.contains(WidgetAppearances.COMPACT_N)) {
            try {
                String columnsAppearance = appearance.contains(WidgetAppearances.COLUMNS_N) ? WidgetAppearances.COLUMNS_N : WidgetAppearances.COMPACT_N;
                if (appearance.contains(columnsAppearance)) {
                    try {
                        appearance =
                                appearance.substring(appearance.indexOf(columnsAppearance), appearance.length());
                        int idx = appearance.indexOf(columnsAppearance);
                        if (idx != -1) {
                            String substringFromNumColumns = appearance.substring(idx + columnsAppearance.length());
                            numColumns = Integer.parseInt(substringFromNumColumns.substring(0, substringFromNumColumns.contains(" ")
                                    ? substringFromNumColumns.indexOf(' ')
                                    : substringFromNumColumns.length()));
                        }
                    } catch (Exception e) {
                        Timber.e("Exception parsing columns");
                    }
                }
            } catch (Exception e) {
                Timber.e("Exception parsing columns");
            }
        } else if (appearance.contains(WidgetAppearances.COLUMNS)) {
            switch (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
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
}
