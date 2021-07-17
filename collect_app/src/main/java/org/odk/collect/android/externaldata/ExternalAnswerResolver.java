/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.externaldata;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.DefaultAnswerResolver;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.javarosa.xpath.expr.XPathFuncExpr;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Author: Meletis Margaritis
 * Date: 17/05/13
 * Time: 16:51
 */
public class ExternalAnswerResolver extends DefaultAnswerResolver {

    @Override
    public IAnswerData resolveAnswer(String textVal, TreeElement treeElement, FormDef formDef) {
        QuestionDef questionDef = XFormParser.ghettoGetQuestionDef(treeElement.getDataType(),
                formDef, treeElement.getRef());
        if (questionDef != null && (questionDef.getControlType() == Constants.CONTROL_SELECT_ONE
                || questionDef.getControlType() == Constants.CONTROL_SELECT_MULTI
                || questionDef.getControlType() == Constants.CONTROL_RANK)) {
            boolean containsSearchExpression = false;

            XPathFuncExpr xpathExpression = null;
            try {
                xpathExpression = ExternalDataUtil.getSearchXPathExpression(
                        questionDef.getAppearanceAttr());
            } catch (Exception e) {
                Timber.e(e);
                // there is a search expression, but has syntax errors
                containsSearchExpression = true;
            }

            if (xpathExpression != null || containsSearchExpression) {
                // that means that we have dynamic selects

                // read the static choices from the options sheet
                List<SelectChoice> staticChoices = questionDef.getChoices();
                for (int index = 0; index < staticChoices.size(); index++) {
                    SelectChoice selectChoice = staticChoices.get(index);
                    String selectChoiceValue = selectChoice.getValue();
                    if (ExternalDataUtil.isAnInteger(selectChoiceValue)) {

                        Selection selection = selectChoice.selection();

                        switch (questionDef.getControlType()) {
                            case Constants.CONTROL_SELECT_ONE: {
                                if (selectChoiceValue.equals(textVal)) {
                                    // This means that the user selected a static selection.
                                    //
                                    // Although (for select1 fields) the default implementation
                                    // will catch this and return the right thing
                                    // (if we call super.resolveAnswer(textVal, treeElement,
                                    // formDef))
                                    // we just need to make sure, so we will override that.
                                    if (questionDef.getControlType()
                                            == Constants.CONTROL_SELECT_ONE) {
                                        // we don't need another, just return the static choice.
                                        return new SelectOneData(selection);
                                    }
                                }
                                break;
                            }
                            case Constants.CONTROL_SELECT_MULTI:
                            case Constants.CONTROL_RANK: {
                                // we should search in a potential comma-separated string of
                                // values for a match
                                // copied from org.javarosa.xform.util.XFormAnswerDataParser
                                // .getSelections()
                                List<String> textValues = DateUtils.split(textVal,
                                        XFormAnswerDataSerializer.DELIMITER, true);
                                if (textValues.contains(textVal)) {
                                    // this means that the user has selected AT LEAST the static
                                    // choice.
                                    if (selectChoiceValue.equals(textVal)) {
                                        // this means that the user selected ONLY the static
                                        // answer, so just return that
                                        List<Selection> customSelections = new ArrayList<>();
                                        customSelections.add(selection);
                                        return new SelectMultiData(customSelections);
                                    } else {
                                        // we will ignore it for now since we will return that
                                        // selection together with the dynamic ones.
                                    }
                                }
                                break;
                            }
                            default: {
                                // There is a bug if we get here, so let's throw an Exception
                                throw createBugRuntimeException(treeElement, textVal);
                            }
                        }

                    } else {
                        switch (questionDef.getControlType()) {
                            case Constants.CONTROL_SELECT_ONE: {
                                // the default implementation will search for the "textVal"
                                // (saved answer) inside the static choices.
                                // Since we know that there isn't such, we just wrap the textVal
                                // in a virtual choice in order to
                                // create a SelectOneData object to be used as the IAnswer to the
                                // TreeElement.
                                // (the caller of this function is searching for such an answer
                                // to populate the in-memory model.)
                                SelectChoice customSelectChoice = new SelectChoice(textVal, textVal,
                                        false);
                                customSelectChoice.setIndex(index);
                                return new SelectOneData(customSelectChoice.selection());
                            }
                            case Constants.CONTROL_SELECT_MULTI:
                            case Constants.CONTROL_RANK: {
                                // we should create multiple selections and add them to the pile
                                List<SelectChoice> customSelectChoices = createCustomSelectChoices(
                                        textVal);
                                List<Selection> customSelections = new ArrayList<>();
                                for (SelectChoice customSelectChoice : customSelectChoices) {
                                    customSelections.add(customSelectChoice.selection());
                                }
                                return new SelectMultiData(customSelections);
                            }
                            default: {
                                // There is a bug if we get here, so let's throw an Exception
                                throw createBugRuntimeException(treeElement, textVal);
                            }
                        }

                    }
                }

                // if we get there then that means that we have a bug
                throw createBugRuntimeException(treeElement, textVal);
            }
        }
        // default behavior matches original behavior (for static selects, etc.)
        return super.resolveAnswer(textVal, treeElement, formDef);
    }

    private RuntimeException createBugRuntimeException(TreeElement treeElement, String textVal) {
        return new RuntimeException("The appearance column of the field " + treeElement.getName()
                + " contains a search() call and the field type is " + treeElement.getDataType()
                + " and the saved answer is " + textVal);
    }

    protected List<SelectChoice> createCustomSelectChoices(String completeTextValue) {
        // copied from org.javarosa.xform.util.XFormAnswerDataParser.getSelections()
        List<String> textValues = DateUtils.split(completeTextValue,
                XFormAnswerDataSerializer.DELIMITER, true);

        int index = 0;
        List<SelectChoice> customSelectChoices = new ArrayList<>();
        for (String textValue : textValues) {
            SelectChoice selectChoice = new SelectChoice(textValue, textValue, false);
            selectChoice.setIndex(index++);
            customSelectChoices.add(selectChoice);
        }

        return customSelectChoices;
    }
}
