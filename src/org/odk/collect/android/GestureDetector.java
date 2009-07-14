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

package org.odk.collect.android;

import org.odk.collect.android.GestureDirection.UserGesture;

import android.view.MotionEvent;

/**
 * Detects when a gesture/fling has occurred and in what direction.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class GestureDetector {

    //private final static String t = "GestureDetector";

    private GestureDirection mDirectionPoint;

    public GestureDetector() {
        // Log.i(t,"called constructor");

        mDirectionPoint = null;
    }


    public UserGesture getGesture(MotionEvent motionEvent) {
        final float x = motionEvent.getX();
        final float y = motionEvent.getY();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDirectionPoint = new GestureDirection(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mDirectionPoint.updateEndPoint(x, y);
                break;
            case MotionEvent.ACTION_UP:
                if (mDirectionPoint != null) {
                    return mDirectionPoint.getDirection();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mDirectionPoint = null;
                break;
        }
        return UserGesture.SWIPE_UNKNOWN;
    }
}
