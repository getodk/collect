package org.odk.collect.android.views;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

/**
 * Copyright 2018 Vaibhav Maheshwari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ScrolledToTopSpinner is used in SpinnerWidget so that the spinner scrolls to top
 * when opened for first time or when last item is selected (which is remove response)
 */

public class ScrolledToTopSpinner extends AppCompatSpinner {

    private boolean spinnerClicked;

    public ScrolledToTopSpinner(Context context, AttributeSet attrs,
                                int defStyle, int mode) {
        super(context, attrs, defStyle, mode);
    }

    public ScrolledToTopSpinner(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrolledToTopSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrolledToTopSpinner(Context context, int mode) {
        super(context, mode);
    }

    public ScrolledToTopSpinner(Context context) {
        super(context);
    }

    @Override
    public int getSelectedItemPosition() {
        // this toggle is required because this method will get called in other
        // places too, the most important being called for the
        // OnItemSelectedListener
        if (spinnerClicked && super.getSelectedItemPosition() == getAdapter().getCount() - 1) {
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
        spinnerClicked = true;
        boolean result = super.performClick();
        spinnerClicked = false;
        return result;
    }

}