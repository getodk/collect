/*
 * Copyright (C) 2011 University of Washington
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper class for accessing Base64 functionality.
 * This allows API Level 7 deployment of ODK Collect while
 * enabling API Level 8 and higher phone to support encryption.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class Base64Wrapper {

	private static final int FLAGS = 2;// NO_WRAP
	private Class<?> base64 = null;

	public Base64Wrapper() throws ClassNotFoundException {
		base64 = this.getClass().getClassLoader()
				.loadClass("android.util.Base64");
	}

	public String encodeToString(byte[] ba) {
		Class<?>[] argClassList = new Class[]{byte[].class, int.class};
		try {
			Method m = base64.getDeclaredMethod("encode", argClassList);
			Object[] argList = new Object[]{ ba, FLAGS };
			Object o = m.invoke(null, argList);
			byte[] outArray = (byte[]) o;
			String s = new String(outArray, "UTF-8");
			return s;
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		}
	}

	public byte[] decode(String base64String) {
		Class<?>[] argClassList = new Class[]{String.class, int.class};
		Object o;
		try { 	
			Method m = base64.getDeclaredMethod("decode", argClassList);
			Object[] argList = new Object[]{ base64String, FLAGS };
			o = m.invoke(null, argList);
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.toString());
		}
		return (byte[]) o;
	}
}
