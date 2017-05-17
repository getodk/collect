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

import java.util.ArrayList;

public class HierarchyElement {
    private String primaryText = "";
    private String secondaryText = "";
    private Drawable icon;
    private int color;
    int type;
    FormIndex formIndex;
    ArrayList<HierarchyElement> children;


    public HierarchyElement(String text1, String text2, Drawable bullet, int color, int type,
            FormIndex f) {
        icon = bullet;
        primaryText = text1;
        secondaryText = text2;
        this.color = color;
        formIndex = f;
        this.type = type;
        children = new ArrayList<HierarchyElement>();
    }


    public String getPrimaryText() {
        return primaryText;
    }


    public String getSecondaryText() {
        return secondaryText;
    }


    public void setPrimaryText(String text) {
        primaryText = text;
    }


    public void setSecondaryText(String text) {
        secondaryText = text;
    }


    public void setIcon(Drawable icon) {
        this.icon = icon;
    }


    public Drawable getIcon() {
        return icon;
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


    public ArrayList<HierarchyElement> getChildren() {
        return children;
    }


    public void addChild(HierarchyElement h) {
        children.add(h);
    }


    public void setChildren(ArrayList<HierarchyElement> children) {
        this.children = children;
    }


    public void setColor(int color) {
        this.color = color;
    }


    public int getColor() {
        return color;
    }

}
