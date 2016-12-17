/*
 * Copyright (C) 2013 University of Washington
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.view.MenuItem;

/**
 * Compatibility utilities for backward-compatible support of Android APIs above SDK 8
 *
 * @author mitchellsundt@gmail.com
 *
 */
@SuppressLint("NewApi")
public class CompatibilityUtils {
	public static void setShowAsAction(MenuItem item, int action) {
		if ( Build.VERSION.SDK_INT >= 11 ) {
			item.setShowAsAction(action);
		}
	}

	public static void invalidateOptionsMenu(final Activity a) {
		if ( Build.VERSION.SDK_INT >= 11 ) {
			a.runOnUiThread(
					new Runnable() {

				@Override
				public void run() {
					a.invalidateOptionsMenu();
				}
			});

		}
	}

	public static boolean useMapsV2(final Context context) {
		if ( Build.VERSION.SDK_INT >= 8 ) {
			final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
			boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
			return supportsEs2;
		}
		return false;
	}
}
