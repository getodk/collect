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

import android.app.Activity;
import android.os.Build;
import android.view.MenuItem;

/**
 * Compatibility utilities for backward-compatible support of Android APIs above SDK 8
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class CompatibilityUtils {
	public static void setShowAsAction(MenuItem item, int action) {
		if ( Build.VERSION.SDK_INT >= 11 ) {
			item.setShowAsAction(action);
		}
	}

	public static void invalidateOptionsMenu(final Activity a) {
		if ( Build.VERSION.SDK_INT >= 11 ) {
			a.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					a.invalidateOptionsMenu();
				}
			});

		}
	}
}
