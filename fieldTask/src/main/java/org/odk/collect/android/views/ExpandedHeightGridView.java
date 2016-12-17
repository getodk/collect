/*
 * Copyright (C) 2013 University of Washington
 *
 * http://creativecommons.org/licenses/by-sa/3.0/ -- code is based upon an answer on Stack Overflow:
 * http://stackoverflow.com/questions/8481844/gridview-height-gets-cut
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

package org.odk.collect.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * From Stack Overflow: http://stackoverflow.com/questions/8481844/gridview-height-gets-cut
 * Changed to always be expanded, since widgets are always embedded in a scroll view.
 *
 * @author tacone based on answer by Neil Traft
 * @author mitchellsundt@gmail.com
 *
 */
public class ExpandedHeightGridView extends GridView
{

    public ExpandedHeightGridView(Context context)
    {
        super(context);
    }

    public ExpandedHeightGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ExpandedHeightGridView(Context context, AttributeSet attrs,
            int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // HACK! TAKE THAT ANDROID!
        // Calculate entire height by providing a very large height hint.
        // But do not use the highest 2 bits of this integer; those are
        // reserved for the MeasureSpec mode.
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
