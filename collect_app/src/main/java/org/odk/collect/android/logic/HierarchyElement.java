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

package org.odk.collect.android.logic;

import android.os.Parcel;
import android.os.Parcelable;

import org.javarosa.core.model.FormIndex;

import java.util.ArrayList;

public class HierarchyElement implements Parcelable {
    private int type;
    private FormIndex formIndex;
    private ArrayList<HierarchyElement> list;
    private HierarchyElement parent;
    private String primaryText = "";
    private String secondaryText = "";
    private int displayIcon;
    private int color;

    public HierarchyElement(String text1, String text2, int showIcon, int color, int type,
                            FormIndex f, HierarchyElement parent, ArrayList<HierarchyElement> list) {
        displayIcon = showIcon;
        primaryText = text1;
        secondaryText = text2;
        this.color = color;
        formIndex = f;
        this.type = type;
        this.parent = parent;
        this.list = list;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeSerializable(formIndex);
        dest.writeTypedList(list);
        dest.writeParcelable(parent, 1);
        dest.writeString(primaryText);
        dest.writeString(secondaryText);
        dest.writeInt(displayIcon);
        dest.writeInt(color);
    }

    public ArrayList<HierarchyElement> getList() {
        return list;
    }

    public void setList(ArrayList<HierarchyElement> list) {
        this.list = list;
    }

    public HierarchyElement getParent() {
        return parent;
    }

    public void setParent(HierarchyElement parent) {
        this.parent = parent;
    }

    public String getPrimaryText() {
        return primaryText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public int getDisplayIcon() {
        return displayIcon;
    }

    public FormIndex getFormIndex() {
        return formIndex;
    }

    public int getType() {
        return type;
    }

    public void setType(int newType) {
        type = newType;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
