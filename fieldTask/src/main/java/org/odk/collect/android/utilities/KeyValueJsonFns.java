package org.odk.collect.android.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/*
 * This class contains miscellaneous utility functions
 */
public final class KeyValueJsonFns {
	
	public class KeyValue {
		public String key;
		public String value;
	}
	
	/*
	 * Accept a JSON string of key value pairs and return a comma separated list of values
	 */
	public static final String getValues(String in) {
		String out = null;
		
		if (in != null) {
	    	Gson gson = new GsonBuilder().create();
	    	Type type = new TypeToken<ArrayList<KeyValue>>(){}.getType();		
	    	ArrayList <KeyValue> kv = gson.fromJson(in, type);
	    	
	    	StringBuffer outBuf = new StringBuffer();
	    	for(int i = 0; i < kv.size(); i++) {
	    		if(i > 0) {
	    			outBuf.append(",");
	    		}
	    		outBuf.append(kv.get(i).value);
	    	}
	    	out = outBuf.toString();
		}
		
		return out;
	}
}
