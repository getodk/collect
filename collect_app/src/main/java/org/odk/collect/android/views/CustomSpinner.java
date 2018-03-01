package org.odk.collect.android.views;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

/**
 * Created by vaibhav on 1/3/18.
 */

public class CustomSpinner extends AppCompatSpinner {

    private boolean toggleFlag = true;

    public CustomSpinner(Context context, AttributeSet attrs,
                                  int defStyle, int mode) {
        super(context, attrs, defStyle, mode);
    }

    public CustomSpinner(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(Context context, int mode) {
        super(context, mode);
    }

    public CustomSpinner(Context context) {
        super(context);
    }

    @Override
    public int getSelectedItemPosition() {
        // this toggle is required because this method will get called in other
        // places too, the most important being called for the
        // OnItemSelectedListener
        if (!toggleFlag && super.getSelectedItemPosition() == getAdapter().getCount() - 1) {
            return 0; // get us to the first element
        }
        return super.getSelectedItemPosition();
    }

    @Override
    public boolean performClick() {
        // this method shows the list of elements from which to select one.
        // we have to make the getSelectedItemPosition to return 0 so you can
        // fool the Spinner and let it think that the selected item is the first
        // element
        toggleFlag = false;
        boolean result = super.performClick();
        toggleFlag = true;
        return result;
    }

}