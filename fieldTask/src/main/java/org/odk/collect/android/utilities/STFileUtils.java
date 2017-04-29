/*
 * Copyright (C) 2014 Smap Consulting
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

import org.odk.collect.android.application.Collect;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.odk.collect.android.utilities.FileUtils;

/**
 * Static methods used for common file operations.
 * 
 * @author Neil Penman
 */
public final class STFileUtils {
    private final static String t = "STFileUtils";

    // Storage paths
    public static final String LOGS_PATH = Environment.getExternalStorageDirectory()
            + "/fieldTask/logs/";
    public static final String METRIC_FILE = LOGS_PATH + "metrics.csv";
    
    /**
     * Get the time in human readable format
     * 
     * @param timestamp date modified of the file
     * @return date modified of the file formatted as human readable string
     */
    public static String getTime(Long timestamp) {

        String ts =
            new SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm").format(new Date(timestamp));
        return ts;
    }
    
    /**
     * Get the time for the logger
     * 
     * @param timestamp date modified of the file
     * @return date modified of the file formatted as human readable string
     */
    public static String getLogTime(Long timestamp) {

        String ts =
            new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date(timestamp));  // TODO make this configurable
        return ts;
    }


    /**
     * Get the file name from a path name
     * 
     * @param path path to the file
     * @return name of the file formatted as human readable string
     */
    public static String getName(String path) {
      
        try {
        	// remove path and extension from form
        	String filename = path.substring(path.lastIndexOf("/") + 1);
            return filename.substring(0, filename.lastIndexOf("."));

        } catch (StringIndexOutOfBoundsException e) {
            return path;
        }
    }

    /**
     * Get the file name from a path name
     * 
     * @param path path to the file
     * @return name of the file including its extension
     */
    public static String getFileName(String path) {
      
        try {
        	// remove path and extension from form
            return path.substring(path.lastIndexOf("/") + 1);

        } catch (StringIndexOutOfBoundsException e) {
            return path;
        }
    }


    /**
     * Get the source from the URL of the server
     * 
     * @param url of the server
     * @return source
     */
    public static String getSource(String url) {
      
        String source = null;
        // Remove the protocol
        if(url == null) {
            source = "none";
        } else if(url.startsWith("http")) {
        	int idx = url.indexOf("//");
        	if(idx > 0) {
        		source = url.substring(idx + 2);
        	} else {
        		source = url;
        	}
        } else {
            source = url;
        }

        // Only return the domain
        if(source.contains("/")) {
        	int idx = source.indexOf("/");
        	if(idx > 0) {
        		source = source.substring(0, idx);
        	} 
        }
        return source.trim().toLowerCase();
    }
    
    /*
     * Create a new instance file
     */
    public static String createInstanceFile(String formName, String contents) {
    	String instanceFile = null;

    	if(formName != null) {
	        String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").
	        		format(Calendar.getInstance().getTime());
	        String formBase = formName.substring(0, formName.lastIndexOf('.'));
	        String path = Collect.INSTANCES_PATH + formBase + "_" + time;
	        if (FileUtils.createFolder(path)) {
	            instanceFile = path + "/" + formBase + "_" + time + ".xml";
	            if(contents != null) {
	            	try {	// TODO write in background task
	                    
	                    BufferedWriter bw = new BufferedWriter(new FileWriter(instanceFile));
	                    bw.write(contents);
	                    bw.flush();
	                    bw.close();

	                } catch (IOException e) {
	                    Log.e(t, "Error writing XML file");
	                    e.printStackTrace();
	                }
	            }
	        }
    	}
        
        return instanceFile;
    }
}
