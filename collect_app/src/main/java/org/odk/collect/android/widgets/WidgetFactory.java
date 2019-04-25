/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;

import org.javarosa.core.model.Constants;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.WidgetAppearances;

/**
 * Convenience class that handles creation of widgets.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class WidgetFactory {

    private WidgetFactory() {

    }

    /**
     * Returns the appropriate QuestionWidget for the given FormEntryPrompt.
     *
     * @param fep              prompt element to be rendered
     * @param context          Android context
     * @param readOnlyOverride a flag to be ORed with JR readonly attribute.
     */
    public static QuestionWidget createWidgetFromPrompt(FormEntryPrompt fep, Context context,
                                                        boolean readOnlyOverride) {

        String appearance = WidgetAppearances.getAppearance(fep);

        final QuestionWidget questionWidget;
        switch (fep.getControlType()) {
            case Constants.CONTROL_INPUT:
                switch (fep.getDataType()) {
                    case Constants.DATATYPE_DATE_TIME:
                        questionWidget = new DateTimeWidget(context, fep);
                        break;
                    case Constants.DATATYPE_DATE:
                        if (appearance.contains(WidgetAppearances.ETHIOPIAN)) {
                            questionWidget = new EthiopianDateWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearances.COPTIC)) {
                            questionWidget = new CopticDateWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearances.ISLAMIC)) {
                            questionWidget = new IslamicDateWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearances.BIKRAM_SAMBAT)) {
                            questionWidget = new BikramSambatDateWidget(context, fep);
                        } else if (appearance.contains("myanmar")) {
                            questionWidget = new MyanmarDateWidget(context, fep);
                        } else {
                            questionWidget = new DateWidget(context, fep);
                        }
                        break;
                    case Constants.DATATYPE_TIME:
                        questionWidget = new TimeWidget(context, fep);
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        if (appearance.startsWith(WidgetAppearances.EX)) {
                            questionWidget = new ExDecimalWidget(context, fep);
                        } else if (appearance.equals(WidgetAppearances.BEARING)) {
                            questionWidget = new BearingWidget(context, fep);
                        } else {
                            boolean useThousandSeparator = false;
                            if (appearance.contains(WidgetAppearances.THOUSANDS_SEP)) {
                                useThousandSeparator = true;
                            }
                            questionWidget = new DecimalWidget(context, fep, readOnlyOverride,
                                    useThousandSeparator);
                        }
                        break;
                    case Constants.DATATYPE_INTEGER:
                        if (appearance.startsWith(WidgetAppearances.EX)) {
                            questionWidget = new ExIntegerWidget(context, fep);
                        } else {
                            boolean useThousandSeparator = false;
                            if (appearance.contains(WidgetAppearances.THOUSANDS_SEP)) {
                                useThousandSeparator = true;
                            }
                            questionWidget = new IntegerWidget(context, fep, readOnlyOverride,
                                    useThousandSeparator);
                        }
                        break;
                    case Constants.DATATYPE_GEOPOINT:
                        questionWidget = new GeoPointWidget(context, fep);
                        break;
                    case Constants.DATATYPE_GEOSHAPE:
                        questionWidget = new GeoShapeWidget(context, fep);
                        break;
                    case Constants.DATATYPE_GEOTRACE:
                        questionWidget = new GeoTraceWidget(context, fep);
                        break;
                    case Constants.DATATYPE_BARCODE:
                        questionWidget = new BarcodeWidget(context, fep);
                        break;
                    case Constants.DATATYPE_TEXT:
                        String query = fep.getQuestion().getAdditionalAttribute(null, "query");
                        if (query != null) {
                            questionWidget = new ItemsetWidget(context, fep, appearance.startsWith(WidgetAppearances.QUICK));
                        } else if (appearance.startsWith(WidgetAppearances.PRINTER)) {
                            questionWidget = new ExPrinterWidget(context, fep);
                        } else if (appearance.startsWith(WidgetAppearances.EX)) {
                            questionWidget = new ExStringWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearances.NUMBERS)) {
                            boolean useThousandsSeparator = false;
                            if (appearance.contains(WidgetAppearances.THOUSANDS_SEP)) {
                                useThousandsSeparator = true;
                            }
                            questionWidget = new StringNumberWidget(context, fep, readOnlyOverride,
                                    useThousandsSeparator);
                        } else if (appearance.equals(WidgetAppearances.URL)) {
                            questionWidget = new UrlWidget(context, fep);
                        } else {
                            questionWidget = new StringWidget(context, fep, readOnlyOverride);
                        }
                        break;
                    case Constants.DATATYPE_BOOLEAN:
                        questionWidget = new BooleanWidget(context, fep);
                        break;
                    default:
                        questionWidget = new StringWidget(context, fep, readOnlyOverride);
                        break;
                }
                break;
            case Constants.CONTROL_FILE_CAPTURE:
                questionWidget = new ArbitraryFileWidget(context, fep);
                break;
            case Constants.CONTROL_IMAGE_CHOOSE:
                if (appearance.equals(WidgetAppearances.SIGNATURE)) {
                    questionWidget = new SignatureWidget(context, fep);
                } else if (appearance.contains(WidgetAppearances.ANNOTATE)) {
                    questionWidget = new AnnotateWidget(context, fep);
                } else if (appearance.equals(WidgetAppearances.DRAW)) {
                    questionWidget = new DrawWidget(context, fep);
                } else {
                    questionWidget = new ImageWidget(context, fep);
                }
                break;
            case Constants.CONTROL_OSM_CAPTURE:
                questionWidget = new OSMWidget(context, fep);
                break;
            case Constants.CONTROL_AUDIO_CAPTURE:
                questionWidget = new AudioWidget(context, fep);
                break;
            case Constants.CONTROL_VIDEO_CAPTURE:
                questionWidget = new VideoWidget(context, fep);
                break;
            case Constants.CONTROL_SELECT_ONE:
                // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
                // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
                // This means normal appearances should be put before search().
                if (!appearance.startsWith(WidgetAppearances.COMPACT_N) && (appearance.startsWith(WidgetAppearances.COMPACT)
                        || appearance.startsWith(WidgetAppearances.QUICKCOMPACT)
                        || appearance.startsWith(WidgetAppearances.COLUMNS_FLEX))) {
                    questionWidget = new GridWidget(context, fep, WidgetAppearances.getNumberOfColumns(fep, context), appearance.contains(WidgetAppearances.QUICK));
                } else if (appearance.contains(WidgetAppearances.MINIMAL)) {
                    questionWidget = new SpinnerWidget(context, fep, appearance.contains(WidgetAppearances.QUICK));
                } else if (appearance.contains(WidgetAppearances.SEARCH) || appearance.contains(WidgetAppearances.AUTOCOMPLETE)) {
                    questionWidget = new SelectOneSearchWidget(context, fep, appearance.contains(WidgetAppearances.QUICK));
                } else if (appearance.contains(WidgetAppearances.LIST_NO_LABEL)) {
                    questionWidget = new ListWidget(context, fep, false, appearance.contains(WidgetAppearances.QUICK));
                } else if (appearance.contains(WidgetAppearances.LIST)) {
                    questionWidget = new ListWidget(context, fep, true, appearance.contains(WidgetAppearances.QUICK));
                } else if (appearance.equals(WidgetAppearances.LABEL)) {
                    questionWidget = new LabelWidget(context, fep);
                } else if (appearance.contains(WidgetAppearances.IMAGE_MAP)) {
                    questionWidget = new SelectOneImageMapWidget(context, fep, appearance.contains(WidgetAppearances.QUICK));
                } else {
                    questionWidget = new SelectOneWidget(context, fep, appearance.contains(WidgetAppearances.QUICK));
                }
                break;
            case Constants.CONTROL_SELECT_MULTI:
                // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
                // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
                // This means normal appearances should be put before search().
                if (!appearance.startsWith(WidgetAppearances.COMPACT_N)
                        && (appearance.startsWith(WidgetAppearances.COMPACT)
                            || appearance.startsWith(WidgetAppearances.COLUMNS_FLEX))) {
                    questionWidget = new GridMultiWidget(context, fep, WidgetAppearances.getNumberOfColumns(fep, context));
                } else if (appearance.startsWith(WidgetAppearances.MINIMAL)) {
                    questionWidget = new SpinnerMultiWidget(context, fep);
                } else if (appearance.startsWith(WidgetAppearances.LIST_NO_LABEL)) {
                    questionWidget = new ListMultiWidget(context, fep, false);
                } else if (appearance.startsWith(WidgetAppearances.LIST)) {
                    questionWidget = new ListMultiWidget(context, fep, true);
                } else if (appearance.startsWith(WidgetAppearances.LABEL)) {
                    questionWidget = new LabelWidget(context, fep);
                } else if (appearance.contains(WidgetAppearances.SEARCH) || appearance.contains(WidgetAppearances.AUTOCOMPLETE)) {
                    questionWidget = new SelectMultipleAutocompleteWidget(context, fep);
                } else if (appearance.startsWith(WidgetAppearances.IMAGE_MAP)) {
                    questionWidget = new SelectMultiImageMapWidget(context, fep);
                } else {
                    questionWidget = new SelectMultiWidget(context, fep);
                }
                break;
            case Constants.CONTROL_RANK:
                questionWidget = new RankingWidget(context, fep);
                break;
            case Constants.CONTROL_TRIGGER:
                questionWidget = new TriggerWidget(context, fep);
                break;
            case Constants.CONTROL_RANGE:
                if (appearance.startsWith(WidgetAppearances.RATING)) {
                    questionWidget = new RatingWidget(context, fep);
                } else {
                    switch (fep.getDataType()) {
                        case Constants.DATATYPE_INTEGER:
                            questionWidget = new RangeIntegerWidget(context, fep);
                            break;
                        case Constants.DATATYPE_DECIMAL:
                            questionWidget = new RangeDecimalWidget(context, fep);
                            break;
                        default:
                            questionWidget = new StringWidget(context, fep, readOnlyOverride);
                            break;
                    }
                }
                break;
            default:
                questionWidget = new StringWidget(context, fep, readOnlyOverride);
                break;
        }

        return questionWidget;
    }
}