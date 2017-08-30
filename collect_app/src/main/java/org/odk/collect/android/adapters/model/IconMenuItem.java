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

package org.odk.collect.android.adapters.model;

/**
 * Icon Menu Item representation
 */

public class IconMenuItem {

    private final int imageResId;
    private final int textResId;

    public IconMenuItem(int imageResId, int textResId) {
        this.imageResId = imageResId;
        this.textResId = textResId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getTextResId() {
        return textResId;
    }
}
