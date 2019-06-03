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
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

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

        String appearance = WidgetAppearanceUtils.getSanitizedAppearanceHint(fep);

        final QuestionWidget questionWidget;
        switch (fep.getControlType()) {
            case Constants.CONTROL_INPUT:
                switch (fep.getDataType()) {
                    case Constants.DATATYPE_DATE_TIME:
                        questionWidget = new DateTimeWidget(context, fep);
                        break;
                    case Constants.DATATYPE_DATE:
                        if (appearance.contains(WidgetAppearanceUtils.ETHIOPIAN)) {
                            questionWidget = new EthiopianDateWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearanceUtils.COPTIC)) {
                            questionWidget = new CopticDateWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearanceUtils.ISLAMIC)) {
                            questionWidget = new IslamicDateWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearanceUtils.BIKRAM_SAMBAT)) {
                            questionWidget = new BikramSambatDateWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearanceUtils.MYANMAR)) {
                            questionWidget = new MyanmarDateWidget(context, fep);
                        } else {
                            questionWidget = new DateWidget(context, fep);
                        }
                        break;
                    case Constants.DATATYPE_TIME:
                        questionWidget = new TimeWidget(context, fep);
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        if (appearance.startsWith(WidgetAppearanceUtils.EX)) {
                            questionWidget = new ExDecimalWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearanceUtils.BEARING)) {	// smap change to contains rather than equals
                            questionWidget = new BearingWidget(context, fep);
                        } else {
                             questionWidget = new DecimalWidget(context, fep, readOnlyOverride,
                                    appearance.contains(WidgetAppearanceUtils.THOUSANDS_SEP));
                        }
                        break;
                    case Constants.DATATYPE_INTEGER:
                        if (appearance.startsWith(WidgetAppearanceUtils.EX)) {
                            questionWidget = new ExIntegerWidget(context, fep);
                        } else {
                            questionWidget = new IntegerWidget(context, fep, readOnlyOverride,
                                    appearance.contains(WidgetAppearanceUtils.THOUSANDS_SEP));
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
                        if (appearance.contains("read_nfc")) {
                            questionWidget = new NfcWidget(context, fep);
                        } else {
                            questionWidget = new BarcodeWidget(context, fep);
                        }
                        break;
                    case Constants.DATATYPE_TEXT:
                        String query = fep.getQuestion().getAdditionalAttribute(null, "query");
                        if (query != null) {
                            questionWidget = new ItemsetWidget(context, fep, appearance.contains(WidgetAppearanceUtils.QUICK), readOnlyOverride);  // smap change to contains rather than equals
                        } else if (appearance.contains(WidgetAppearanceUtils.PRINTER)) {    // smap change to contains rather than equals
                            questionWidget = new ExPrinterWidget(context, fep);
                        } else if (appearance.startsWith(WidgetAppearanceUtils.EX)) {
                            questionWidget = new ExStringWidget(context, fep);
                        } else if (appearance.contains(WidgetAppearanceUtils.NUMBERS)) {
                            questionWidget = new StringNumberWidget(context, fep, readOnlyOverride,
                                    appearance.contains(WidgetAppearanceUtils.THOUSANDS_SEP));
                        } else if (appearance.contains(WidgetAppearanceUtils.URL)) {	// smap change to contains rather than equals
                            questionWidget = new UrlWidget(context, fep);
                        } else if (appearance.contains("chart")) {        // smap chart
                            String chartType = fep.getQuestion().getAdditionalAttribute(null, "chart_type");
                            if(chartType == null) {
                                chartType = "line";
                            }
                            if(chartType.equals("line")) {
                                questionWidget = new SmapChartLineWidget(context, fep, appearance);
                            } else if(chartType.equals("horizontal_bar")){
                                questionWidget = new SmapChartHorizontalBarWidget(context, fep, appearance);
                            } else {
                                questionWidget = new SmapChartLineWidget(context, fep, appearance);
                            }

                        } else if (fep.getQuestion().getAdditionalAttribute(null, "form_identifier") != null) {	// smap
                            questionWidget = new SmapFormWidget(context, fep, appearance, readOnlyOverride);	// smap
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
                if (appearance.contains(WidgetAppearanceUtils.SIGNATURE)) {	// smap change to contains rather than equals
                    questionWidget = new SignatureWidget(context, fep);
                } else if (appearance.contains(WidgetAppearanceUtils.ANNOTATE)) {
                    questionWidget = new AnnotateWidget(context, fep);
                } else if (appearance.contains(WidgetAppearanceUtils.DRAW)) {	// smap change to contains rather than equals
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
                if (!appearance.contains(WidgetAppearanceUtils.COMPACT_N) && (appearance.contains(WidgetAppearanceUtils.COMPACT)   // smap contains
                        || appearance.contains(WidgetAppearanceUtils.QUICKCOMPACT)
                        || appearance.contains(WidgetAppearanceUtils.COLUMNS_PACK))) {
                    questionWidget = new GridWidget(context, fep, appearance.contains(WidgetAppearanceUtils.QUICK));
                } else if (appearance.contains(WidgetAppearanceUtils.MINIMAL)) {
                    questionWidget = new SpinnerWidget(context, fep, appearance.contains(WidgetAppearanceUtils.QUICK));
                } else if (appearance.contains(WidgetAppearanceUtils.SEARCH) || appearance.contains(WidgetAppearanceUtils.AUTOCOMPLETE)) {
                    questionWidget = new SelectOneSearchWidget(context, fep, appearance.contains(WidgetAppearanceUtils.QUICK), readOnlyOverride);
                } else if (appearance.contains(WidgetAppearanceUtils.LIST_NO_LABEL)) {
                    questionWidget = new ListWidget(context, fep, false, appearance.contains(WidgetAppearanceUtils.QUICK));
                } else if (appearance.contains(WidgetAppearanceUtils.LIST)) {
                    questionWidget = new ListWidget(context, fep, true, appearance.contains(WidgetAppearanceUtils.QUICK));
                } else if (appearance.equals(WidgetAppearanceUtils.LABEL)) {
                    questionWidget = new LabelWidget(context, fep);
                } else if (appearance.contains(WidgetAppearanceUtils.IMAGE_MAP)) {
                    questionWidget = new SelectOneImageMapWidget(context, fep, appearance.contains(WidgetAppearanceUtils.QUICK));
                } else {
                    questionWidget = new SelectOneWidget(context, fep, appearance.contains(WidgetAppearanceUtils.QUICK), readOnlyOverride);    // smap
                }
                break;
            case Constants.CONTROL_SELECT_MULTI:
                // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
                // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
                // This means normal appearances should be put before search().
                if (!appearance.startsWith(WidgetAppearanceUtils.COMPACT_N)
                        && (appearance.startsWith(WidgetAppearanceUtils.COMPACT)
                            || appearance.startsWith(WidgetAppearanceUtils.COLUMNS_PACK))) {
                    questionWidget = new GridMultiWidget(context, fep);
                } else if (appearance.startsWith(WidgetAppearanceUtils.MINIMAL)) {
                    questionWidget = new SpinnerMultiWidget(context, fep);
                } else if (appearance.startsWith(WidgetAppearanceUtils.LIST_NO_LABEL)) {
                    questionWidget = new ListMultiWidget(context, fep, false);
                } else if (appearance.startsWith(WidgetAppearanceUtils.LIST)) {
                    questionWidget = new ListMultiWidget(context, fep, true);
                } else if (appearance.startsWith(WidgetAppearanceUtils.LABEL)) {
                    questionWidget = new LabelWidget(context, fep);
                } else if (appearance.contains(WidgetAppearanceUtils.SEARCH) || appearance.contains(WidgetAppearanceUtils.AUTOCOMPLETE)) {
                    questionWidget = new SelectMultipleAutocompleteWidget(context, fep, readOnlyOverride);  // smap
                } else if (appearance.startsWith(WidgetAppearanceUtils.IMAGE_MAP)) {
                    questionWidget = new SelectMultiImageMapWidget(context, fep);
                } else {
                    questionWidget = new SelectMultiWidget(context, fep,  readOnlyOverride);
                }
                break;
            case Constants.CONTROL_RANK:
                questionWidget = new RankingWidget(context, fep);
                break;
            case Constants.CONTROL_TRIGGER:
                questionWidget = new TriggerWidget(context, fep);
                break;
            case Constants.CONTROL_RANGE:
                if (appearance.startsWith(WidgetAppearanceUtils.RATING)) {
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
