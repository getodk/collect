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

    private HierarchyElement(HierarchyElementBuilder builder) {
        type = builder.type;
        formIndex = builder.formIndex;
        itemsAtLevel = builder.itemsAtLevel;
        parent = builder.parent;
        primaryText = builder.primaryText;
        secondaryText = builder.secondaryText;
        icon = builder.icon;
        color = builder.color;
    }

    public Drawable getIcon() {
        return icon;
    }

    public ArrayList<HierarchyElement> getItemsAtLevel() {
        return itemsAtLevel;
    }

    public HierarchyElement getParent() {
        return parent;
    }

    public String getPrimaryText() {
        return primaryText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public FormIndex getFormIndex() {
        return formIndex;
    }

    public int getType() {
        return type;
    }

    public int getColor() {
        return color;
    }

    public static class HierarchyElementBuilder {
        private int type;
        private FormIndex formIndex;
        private ArrayList<HierarchyElement> itemsAtLevel;
        private HierarchyElement parent;
        private String primaryText = "";
        private String secondaryText = "";
        private Drawable icon;
        private int color;

        public HierarchyElementBuilder setParent(HierarchyElement parent) {
            this.parent = parent;
            return this;
        }

        public HierarchyElementBuilder setType(int newType) {
            type = newType;
            return this;
        }

        public HierarchyElementBuilder setFormIndex(FormIndex formIndex) {
            this.formIndex = formIndex;
            return this;
        }

        public HierarchyElementBuilder setItemsAtLevel(ArrayList<HierarchyElement> itemsAtLevel) {
            this.itemsAtLevel = itemsAtLevel;
            return this;
        }

        public HierarchyElementBuilder setPrimaryText(String primaryText) {
            this.primaryText = primaryText;
            return this;
        }

        public HierarchyElementBuilder setSecondaryText(String secondaryText) {
            this.secondaryText = secondaryText;
            return this;
        }

        public HierarchyElementBuilder setIcon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        public HierarchyElementBuilder setColor(int color) {
            this.color = color;
            return this;
        }

        public HierarchyElement build() {
            return new HierarchyElement(this);
        }
    }
}
