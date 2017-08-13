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

import android.graphics.drawable.Drawable;

import org.javarosa.core.model.FormIndex;

import java.io.Serializable;
import java.util.ArrayList;

public class HierarchyElement implements Serializable {

    private int type;
    private FormIndex formIndex;
    private ArrayList<HierarchyElement> itemsAtLevel;
    private HierarchyElement parent;
    private String primaryText = "";
    private String secondaryText = "";
    private Drawable icon;
    private int color;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public ArrayList<HierarchyElement> getItemsAtLevel() {
        return itemsAtLevel;
    }

    public void setItemsAtLevel(ArrayList<HierarchyElement> itemsAtLevel) {
        this.itemsAtLevel = itemsAtLevel;
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

    public void setPrimaryText(String primaryText) {
        this.primaryText = primaryText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public void setSecondaryText(String secondaryText) {
        this.secondaryText = secondaryText;
    }

    public FormIndex getFormIndex() {
        return formIndex;
    }

    public void setFormIndex(FormIndex formIndex) {
        this.formIndex = formIndex;
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
}
