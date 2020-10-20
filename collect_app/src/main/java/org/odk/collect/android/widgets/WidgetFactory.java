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

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;

import org.javarosa.core.model.Constants;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.utilities.CameraUtils;
import org.odk.collect.android.utilities.CustomTabHelper;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.items.ItemsetWidget;
import org.odk.collect.android.widgets.items.LabelWidget;
import org.odk.collect.android.widgets.items.LikertWidget;
import org.odk.collect.android.widgets.items.ListMultiWidget;
import org.odk.collect.android.widgets.items.ListWidget;
import org.odk.collect.android.widgets.items.RankingWidget;
import org.odk.collect.android.widgets.items.SelectMultiImageMapWidget;
import org.odk.collect.android.widgets.items.SelectMultiMinimalWidget;
import org.odk.collect.android.widgets.items.SelectMultiWidget;
import org.odk.collect.android.widgets.items.SelectOneImageMapWidget;
import org.odk.collect.android.widgets.items.SelectOneMinimalWidget;
import org.odk.collect.android.widgets.items.SelectOneWidget;
import org.odk.collect.android.widgets.utilities.ActivityGeoDataRequester;
import org.odk.collect.android.widgets.utilities.AudioPlayer;
import org.odk.collect.android.widgets.utilities.ExternalAppAudioDataRequester;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.analytics.AnalyticsEvents.PROMPT;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.MAPS;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.PLACEMENT_MAP;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.hasAppearance;

/**
 * Convenience class that handles creation of widgets.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class WidgetFactory {

    private static final String PICKER_APPEARANCE = "picker";

    private WidgetFactory() {

    }

    /**
     * Returns the appropriate QuestionWidget for the given FormEntryPrompt.
     *  @param prompt              prompt element to be rendered
     * @param context          Android context
     * @param readOnlyOverride a flag to be ORed with JR readonly attribute.
     */
    public static QuestionWidget createWidgetFromPrompt(FormEntryPrompt prompt,
                                                        Context context,
                                                        boolean readOnlyOverride,
                                                        WaitingForDataRegistry waitingForDataRegistry,
                                                        QuestionMediaManager questionMediaManager,
                                                        Analytics analytics,
                                                        AudioPlayer audioPlayer) {

        String appearance = WidgetAppearanceUtils.getSanitizedAppearanceHint(prompt);
        QuestionDetails questionDetails = new QuestionDetails(prompt, Collect.getCurrentFormIdentifierHash());
        PermissionUtils permissionUtils = new PermissionUtils();
        ActivityAvailability activityAvailability = new ActivityAvailability(context);

        final QuestionWidget questionWidget;
        switch (prompt.getControlType()) {
            case Constants.CONTROL_INPUT:
                switch (prompt.getDataType()) {
                    case Constants.DATATYPE_DATE_TIME:
                        questionWidget = new DateTimeWidget(context, questionDetails);
                        break;
                    case Constants.DATATYPE_DATE:
                        questionWidget = new DateWidget(context, questionDetails);
                        break;
                    case Constants.DATATYPE_TIME:
                        questionWidget = new TimeWidget(context, questionDetails);
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        if (appearance.startsWith(WidgetAppearanceUtils.EX)) {
                            questionWidget = new ExDecimalWidget(context, questionDetails, waitingForDataRegistry);
                        } else if (appearance.equals(WidgetAppearanceUtils.BEARING)) {
                            questionWidget = new BearingWidget(context, questionDetails, waitingForDataRegistry,
                                    (SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
                        } else {
                             questionWidget = new DecimalWidget(context, questionDetails, readOnlyOverride);
                        }
                        break;
                    case Constants.DATATYPE_INTEGER:
                        if (appearance.startsWith(WidgetAppearanceUtils.EX)) {
                            questionWidget = new ExIntegerWidget(context, questionDetails, waitingForDataRegistry);
                        } else {
                            questionWidget = new IntegerWidget(context, questionDetails, readOnlyOverride);
                        }
                        break;
                    case Constants.DATATYPE_GEOPOINT:
                        if (hasAppearance(questionDetails.getPrompt(), PLACEMENT_MAP) || hasAppearance(questionDetails.getPrompt(), MAPS)) {
                            questionWidget = new GeoPointMapWidget(context, questionDetails, waitingForDataRegistry,
                                    new ActivityGeoDataRequester(permissionUtils));
                        } else {
                            questionWidget = new GeoPointWidget(context, questionDetails, waitingForDataRegistry,
                                    new ActivityGeoDataRequester(permissionUtils));
                        }
                        break;
                    case Constants.DATATYPE_GEOSHAPE:
                        questionWidget = new GeoShapeWidget(context, questionDetails, waitingForDataRegistry,
                                new ActivityGeoDataRequester(permissionUtils));
                        break;
                    case Constants.DATATYPE_GEOTRACE:
                        questionWidget = new GeoTraceWidget(context, questionDetails, waitingForDataRegistry,
                                MapProvider.getConfigurator(), new ActivityGeoDataRequester(permissionUtils));
                        break;
                    case Constants.DATATYPE_BARCODE:
                        questionWidget = new BarcodeWidget(context, questionDetails, waitingForDataRegistry, new CameraUtils());
                        break;
                    case Constants.DATATYPE_TEXT:
                        String query = prompt.getQuestion().getAdditionalAttribute(null, "query");
                        if (query != null) {
                            questionWidget = new ItemsetWidget(context, questionDetails, appearance.startsWith(WidgetAppearanceUtils.QUICK));
                        } else if (appearance.startsWith(WidgetAppearanceUtils.PRINTER)) {
                            questionWidget = new ExPrinterWidget(context, questionDetails, waitingForDataRegistry);
                        } else if (appearance.startsWith(WidgetAppearanceUtils.EX)) {
                            questionWidget = new ExStringWidget(context, questionDetails, waitingForDataRegistry);
                        } else if (appearance.contains(WidgetAppearanceUtils.NUMBERS)) {
                            questionWidget = new StringNumberWidget(context, questionDetails, readOnlyOverride);
                        } else if (appearance.equals(WidgetAppearanceUtils.URL)) {
                            questionWidget = new UrlWidget(context, questionDetails, new CustomTabHelper());

                            analytics.logEvent(PROMPT, "Url", questionDetails.getFormAnalyticsID());
                        } else {
                            questionWidget = new StringWidget(context, questionDetails, readOnlyOverride);
                        }
                        break;
                    default:
                        questionWidget = new StringWidget(context, questionDetails, readOnlyOverride);
                        break;
                }
                break;
            case Constants.CONTROL_FILE_CAPTURE:
                questionWidget = new ArbitraryFileWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry);
                break;
            case Constants.CONTROL_IMAGE_CHOOSE:
                if (appearance.equals(WidgetAppearanceUtils.SIGNATURE)) {
                    questionWidget = new SignatureWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry);
                } else if (appearance.contains(WidgetAppearanceUtils.ANNOTATE)) {
                    questionWidget = new AnnotateWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry);
                } else if (appearance.equals(WidgetAppearanceUtils.DRAW)) {
                    questionWidget = new DrawWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry);
                } else {
                    questionWidget = new ImageWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry);
                }
                break;
            case Constants.CONTROL_OSM_CAPTURE:
                questionWidget = new OSMWidget(context, questionDetails, waitingForDataRegistry);
                break;
            case Constants.CONTROL_AUDIO_CAPTURE:
                ExternalAppAudioDataRequester audioDataRequester = new ExternalAppAudioDataRequester((Activity) context, activityAvailability, waitingForDataRegistry, permissionUtils);
                questionWidget = new AudioWidget(context, questionDetails, questionMediaManager, audioPlayer, audioDataRequester);
                break;
            case Constants.CONTROL_VIDEO_CAPTURE:
                questionWidget = new VideoWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry);
                break;
            case Constants.CONTROL_SELECT_ONE:
                boolean isQuick = appearance.contains(WidgetAppearanceUtils.QUICK);
                // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
                // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
                // This means normal appearances should be put before search().
                if (appearance.contains(WidgetAppearanceUtils.MINIMAL)) {
                    questionWidget = new SelectOneMinimalWidget(context, questionDetails, isQuick, waitingForDataRegistry);
                } else if (appearance.contains(WidgetAppearanceUtils.LIKERT)) {
                    questionWidget = new LikertWidget(context, questionDetails);
                } else if (appearance.contains(WidgetAppearanceUtils.LIST_NO_LABEL)) {
                    questionWidget = new ListWidget(context, questionDetails, false, isQuick);
                } else if (appearance.contains(WidgetAppearanceUtils.LIST)) {
                    questionWidget = new ListWidget(context, questionDetails, true, isQuick);
                } else if (appearance.equals(WidgetAppearanceUtils.LABEL)) {
                    questionWidget = new LabelWidget(context, questionDetails);
                } else if (appearance.contains(WidgetAppearanceUtils.IMAGE_MAP)) {
                    questionWidget = new SelectOneImageMapWidget(context, questionDetails, isQuick);
                } else {
                    questionWidget = new SelectOneWidget(context, questionDetails, isQuick);
                }
                break;
            case Constants.CONTROL_SELECT_MULTI:
                // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
                // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
                // This means normal appearances should be put before search().
                if (appearance.contains(WidgetAppearanceUtils.MINIMAL)) {
                    questionWidget = new SelectMultiMinimalWidget(context, questionDetails, waitingForDataRegistry);
                } else if (appearance.startsWith(WidgetAppearanceUtils.LIST_NO_LABEL)) {
                    questionWidget = new ListMultiWidget(context, questionDetails, false);
                } else if (appearance.startsWith(WidgetAppearanceUtils.LIST)) {
                    questionWidget = new ListMultiWidget(context, questionDetails, true);
                } else if (appearance.startsWith(WidgetAppearanceUtils.LABEL)) {
                    questionWidget = new LabelWidget(context, questionDetails);
                } else if (appearance.startsWith(WidgetAppearanceUtils.IMAGE_MAP)) {
                    questionWidget = new SelectMultiImageMapWidget(context, questionDetails);
                } else {
                    questionWidget = new SelectMultiWidget(context, questionDetails);
                }
                break;
            case Constants.CONTROL_RANK:
                questionWidget = new RankingWidget(context, questionDetails);
                break;
            case Constants.CONTROL_TRIGGER:
                questionWidget = new TriggerWidget(context, questionDetails);
                break;
            case Constants.CONTROL_RANGE:
                if (appearance.startsWith(WidgetAppearanceUtils.RATING)) {
                    questionWidget = new RatingWidget(context, questionDetails);
                } else {
                    switch (prompt.getDataType()) {
                        case Constants.DATATYPE_INTEGER:
                            if (prompt.getQuestion().getAppearanceAttr() != null && prompt.getQuestion().getAppearanceAttr().contains(PICKER_APPEARANCE)) {
                                questionWidget = new RangePickerIntegerWidget(context, questionDetails);
                            } else {
                                questionWidget = new RangeIntegerWidget(context, questionDetails);
                            }
                            break;
                        case Constants.DATATYPE_DECIMAL:
                            if (prompt.getQuestion().getAppearanceAttr() != null && prompt.getQuestion().getAppearanceAttr().contains(PICKER_APPEARANCE)) {
                                questionWidget = new RangePickerDecimalWidget(context, questionDetails);
                            } else {
                                questionWidget = new RangeDecimalWidget(context, questionDetails);
                            }
                            break;
                        default:
                            questionWidget = new StringWidget(context, questionDetails, readOnlyOverride);
                            break;
                    }
                }
                break;
            default:
                questionWidget = new StringWidget(context, questionDetails, readOnlyOverride);
                break;
        }

        return questionWidget;
    }
}