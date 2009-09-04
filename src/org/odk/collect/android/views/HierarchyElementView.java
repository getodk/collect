package org.odk.collect.android.views;

import org.odk.collect.android.logic.HierarchyElement;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class HierarchyElementView extends RelativeLayout {
    
    private TextView mPrimaryTextView;
    private TextView mSecondaryTextView;
    private ImageView mIcon;


    public HierarchyElementView(Context context, HierarchyElement it) {
        super(context);

        mIcon = new ImageView(context);
        mIcon.setImageDrawable(it.getIcon());
        mIcon.setId(1);
        mIcon.setPadding(0, 15, 5, 0);
        addView(mIcon, new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        mPrimaryTextView = new TextView(context);
        mPrimaryTextView.setText(it.getPrimaryText());
        mPrimaryTextView.setId(2);
        LayoutParams l =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        l.addRule(RelativeLayout.RIGHT_OF, mIcon.getId());
        addView(mPrimaryTextView, l);

        mSecondaryTextView = new TextView(context);
        mSecondaryTextView.setText(it.getSecondaryText());
        LayoutParams lp =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.BELOW, mPrimaryTextView.getId());
        lp.addRule(RelativeLayout.RIGHT_OF, mIcon.getId());
        addView(mSecondaryTextView, lp);
    }


    public void setPrimaryText(String text) {
        mPrimaryTextView.setText(text);
    }


    public void setSecondaryText(String text) {
        mSecondaryTextView.setText(text);
    }


    public void setIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
    }



}
