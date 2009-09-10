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

package org.odk.collect.android.logic;


/**
 * Used by {@link FormHandler} to create a Vector of groups.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class GroupElement {
    private String mGroupText;
    private int mRepeatCount;
    private boolean mRepeats;


    public GroupElement(String groupText, int repeatCount, boolean repeats) {

        mGroupText = groupText;
        mRepeatCount = repeatCount;
        mRepeats = repeats;

    }


    public boolean isRepeat() {
        return mRepeats;
    }


    public void setRepeat(boolean repeats) {
        mRepeats = repeats;
    }


    public String getGroupText() {
        return mGroupText;
    }


    public int getRepeatCount() {
        return mRepeatCount;
    }

}
