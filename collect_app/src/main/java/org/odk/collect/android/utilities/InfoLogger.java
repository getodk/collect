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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.odk.collect.android.application.Collect;

import android.util.Log;

/**
 * Used for logging data to log files that could be retrieved after a field deployment.
 * The initial use for this is to assist in diagnosing a report of a cached geopoints
 * being reported, causing stale GPS coordinates to be recorded (issue 780).
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class InfoLogger {
	private static final String t = "InfoLogger";

	private static final String LOG_DIRECTORY = "logging";
	private static final String LOG_FILE = "geotrace.log";

	public static final void geolog(String msg) {
		geologToLogcat(msg);
	}

	private static final void geologToLogcat(String msg) {
		Log.i(t, msg);
	}

	@SuppressWarnings("unused")
	private static final void geologToFile(String msg) {
		File dir = new File( Collect.ODK_ROOT + File.separator + LOG_DIRECTORY );
		if ( !dir.exists() ) {
			dir.mkdirs();
		}
		File log = new File(dir, LOG_FILE);

		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(log, true);
			msg = msg + "\n";
			fo.write( msg.getBytes("UTF-8") );
			fo.flush();
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e(t, "exception: " + e.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e(t, "exception: " + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(t, "exception: " + e.toString());
		}
	}
}
