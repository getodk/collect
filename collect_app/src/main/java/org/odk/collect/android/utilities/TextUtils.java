/*
 * Copyright (C) 2015 University of Washington
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

package org.odk.collect.android.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.odk.collect.android.application.Collect;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public class TextUtils {
	private static final String t = "TextUtils";

	private TextUtils() {
		// static methods only
	}

    public static CharSequence fixHtml(String brokenHtml) {
        // There's some terrible bug that displays all the text as the 
        // opening tag if a tag is the first thing in the string
        // so we hack around it so it begins with something else
        // when we convert it
        
        // terrible hack, just add some chars
        Spanned html = Html.fromHtml("aa" + brokenHtml);
        // after we have the good html, remove the chars
        CharSequence fixedText = html.subSequence(2, html.length());
        return fixedText;
    }
} 
