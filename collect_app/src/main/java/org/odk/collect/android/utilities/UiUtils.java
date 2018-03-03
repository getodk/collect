/*
 * Copyright 2017 Yura Laguta
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

package org.odk.collect.android.utilities;

import android.support.annotation.DimenRes;

import org.odk.collect.android.application.Collect;

/**
 * Created by laguta.yurii@gmail.com on 26/8/17.
 * <p>
 * Collection of utils related to Android UI components
 * ViewUtils name already taken by support library
 */
public class UiUtils {

    private UiUtils() {
    }

    public static int getDimen(@DimenRes int dimenResId) {
        return Collect.getInstance().getResources().getDimensionPixelSize(dimenResId);
    }
}
