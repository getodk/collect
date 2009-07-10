/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.collect.android;

import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.formmanager.view.FormElementBinding;

/**
 * Used by {@link QuestionView} to display Questions and by {@link FormEntry} to
 * display Repeat dialogs. Much of this class is a wrapper for @ link
 * FormElementBinding}
 * 
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * 
 */

public class PromptElement {

   // private final static String t = "PromptElement";

    // object to access question and answer data
    private FormElementBinding mBinding;

    // every group the prompt belongs to
    private Vector<GroupElement> mGroups;

    // needed to trigger repeat dialog
    private boolean isRepeat;


    public PromptElement(Vector<GroupElement> groups) {
        //Log.i(t,"calling constructor");

        mGroups = groups;
        isRepeat = true;
    }


    public PromptElement(FormIndex formIndex, FormDef formDef, Vector<GroupElement> groups) {
        //Log.i(t,"calling constructor");

        mBinding = new FormElementBinding(null, formIndex, formDef);
        mGroups = groups;
        isRepeat = false;
        
        
    }


    /**
     * A prompt's {@link QuestionDef}
     * 
     * @return QuestionDef
     */
    public QuestionDef getQuestionDef() {
        return (QuestionDef) mBinding.element;
    }
    
    
    /**
     * The instance name (question id) and the repeat count
     */
    public String getInstanceId() {
       int count = getLastRepeatCount()+1;
       if (count < 1) {
           return mBinding.instanceNode.getName();
       } else {
           return mBinding.instanceNode.getName()+count;
       }
    }
    
    
    /**
     * The data type of the answer (input, upload, etc)
     */
    public int getQuestionType() {
        return ((QuestionDef) mBinding.element).getControlType();
    }


    /**
     * The data type of the answer (decimal, integer, string, select multi, etc)
     */
    public int getAnswerType() {
        return mBinding.instanceNode.dataType;
    }


    /**
     * Is a response to this question required?
     */
    public boolean isRequired() {
        return mBinding.instanceNode.required;
    }

    /**
     * Is this question read only?
     */
    public boolean isReadonly() {
        return !mBinding.instanceNode.isEnabled();
    }

    /**
     * @see TreeReference
     */
    public TreeReference getInstanceRef() {
        return mBinding.instanceRef;
    }


    /**
     * @see TreeElement
     */
    public TreeElement getInstanceNode() {
        return mBinding.instanceNode;
    }


    /**
     * The answer to a question in the prompt as a standard Java object.
     */
    public Object getAnswerObject() {
        if (getAnswerValue() != null) {
            return getAnswerValue().getValue();
        } else {
            return null;
        }
    }


    /**
     * The answer to a question in the prompt as an {@link IAnswerData}.
     */
    public IAnswerData getAnswerValue() {
        return mBinding.getValue();
    }


    /**
     * The text of the answer to a question in the prompt.
     */
    public String getAnswerText() {
        if (getAnswerValue() != null) {
            return getAnswerValue().getDisplayText();
        } else {
            return null;
        }
    }


    /**
     * The text of message to display if a constraint is violated.
     */
    public String getConstraintText() {
        return mBinding.instanceNode.getConstraint().constraintMsg;
    }


    /**
     * The items in a select question type.
     */
    public OrderedHashtable getSelectItems() {
        return (((QuestionDef) mBinding.element).getSelectItems());
    }


    /**
     * The text of question in the prompt.
     */
    public String getQuestionText() {
        return (((QuestionDef) mBinding.element).getLongText());
    }

    /**
     * The help text of question in the prompt.
     */
    public String getHelpText() {
        return (((QuestionDef) mBinding.element).getHelpText());
    }

    
    /**
     * The repeat count of closest group the prompt belongs to.
     */
    public int getLastRepeatCount() {
        if (getLastGroup() != null) {
            return getLastGroup().getRepeatCount();
        }
        return -1;
    }


    /**
     * The text of closest group the prompt belongs to.
     */
    public String getLastGroupText() {
        if (getLastGroup() != null) {
            return getLastGroup().getGroupText();
        }
        return null;
    }
    
    /**
     * The name of the closest group that repeats or null.
     */
    public String getLastRepeatedGroupName() {
        for (GroupElement g : mGroups) {
            if (g.isRepeat())
                return g.getGroupText();
        }
        return null;
    }
    
    /**
     * The count of the closest group that repeats or -1.
     */
    public int getLastRepeatedGroupRepeatCount() {
        for (GroupElement g : mGroups) {
            if (g.isRepeat())
                return g.getRepeatCount();            
        }
        return -1;
    }


    /**
     * Get all the groups a prompt belongs to.
     */
    public Vector<GroupElement> getGroups() {
        return mGroups;
    }


    /**
     * The closest group the prompt belongs to.
     * 
     * @return GroupElement
     */
    private GroupElement getLastGroup() {
        if (!mGroups.isEmpty()) {
            return mGroups.get(mGroups.size() - 1);
        }
        return null;
    }
    
    
    /**
     * Is the prompt a repeat dialog?
     */
    public boolean isRepeat() {
        return isRepeat;
    }
    
    public boolean isInRepeatableGroup() {
        if (mGroups.isEmpty())
            return false;
        for (GroupElement group : mGroups) {
            if (group.isRepeat())
                return true;
        }
        return false;
    }

}
