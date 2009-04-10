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

package org.google.android.odk;

/**
 * Used by {@link FormHandler} to create a Vector of groups.
 * 
 * @author Yaw Anokwa
 */
public class GroupElement {

    private String mGroupText;
    private int mRepeatCount;

    public GroupElement(String groupText, int repeatCount) {
        setGroupText(groupText);
        setRepeatCount(repeatCount);
    }

    public String getGroupText() {
        return mGroupText;
    }

    private void setGroupText(String groupName) {
        this.mGroupText = groupName;
    }

    public int getRepeatCount() {
        return mRepeatCount;
    }

    private void setRepeatCount(int repeatCount) {
        this.mRepeatCount = repeatCount;
    }
}
