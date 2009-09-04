package org.odk.collect.android.logic;

import org.javarosa.core.model.FormIndex;

import android.graphics.drawable.Drawable;

public class HierarchyElement {


    private String mPrimaryText = "";
    private String mSecondaryText = "";
    private Drawable mIcon;
    int mType;
    FormIndex mFormIndex;


    public HierarchyElement(String text1, String text2, Drawable bullet, int type, FormIndex f) {
        mIcon = bullet;
        mPrimaryText = text1;
        mSecondaryText = text2;
        mFormIndex = f;
        mType = type;
    }


    public String getPrimaryText() {
        return mPrimaryText;
    }


    public String getSecondaryText() {
        return mSecondaryText;
    }


    public void setPrimaryText(String text) {
        mPrimaryText = text;
    }


    public void setSecondaryText(String text) {
        mSecondaryText = text;
    }


    public void setIcon(Drawable icon) {
        mIcon = icon;
    }


    public Drawable getIcon() {
        return mIcon;
    }


    public FormIndex getFormIndex() {
        return mFormIndex;
    }


    public int getType() {
        return mType;
    }


}
