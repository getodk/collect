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

/**
 * Useful static methods to construct the selection string and selectionArgs array
 * for passing to a content provider.  Handles the details of null-valued criteria. 
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class FilterUtils {

	private static final String SPACE_AND_SPACE = " AND ";
	private static final String IS_NULL = " IS NULL";
	private static final String IS_EQUAL_SUBSTITUTION = " = ?";

	public static final class FilterCriteria {
		public final String selection;
		public final String[] selectionArgs;
		
		FilterCriteria(String selection, String[] selectionArgs) {
			this.selection = selection;
			this.selectionArgs = selectionArgs;
		}
	}
	
	/**
	 * Builds the selection string and selectionArgs[] arrays for a ContentProvider or DB query.
	 * Takes an array of keys (Strings) and a corresponding array of values for those keys.
	 * Constructs the selection clause, allowing null values in the value list and properly
	 * representing those constraints as "key is null".  Assumes the toString() method will
	 * provide the string representation for the value.
	 *   
	 * @param keys
	 * @param values
	 * @param additionalClause
	 * @return
	 */
	public static final FilterCriteria buildSelectionClause( String[] keys, Object[] values, String additionalClause ) {
		if ( keys.length != values.length ) 
			throw new IllegalArgumentException("mismatched lengths for key and value arrays");
		
		int nNonNullArgs = 0;
		for ( Object s : values ) {
			if ( s != null ) nNonNullArgs++;
		}
		
		String[] selectionArgs;
		if ( nNonNullArgs > 0 ) {
			selectionArgs = new String[nNonNullArgs];
		} else {
			selectionArgs = null;
		}
		
		int j = 0;
		StringBuilder b = new StringBuilder();
		for ( int i = 0 ; i < values.length ; ++i ) {
			if ( b.length() > 0 ) {
				b.append(SPACE_AND_SPACE);
			}
			if ( values[i] == null ) {
				b.append(keys[i]);
				b.append(IS_NULL);
			} else {
				b.append(keys[i]);
				b.append(IS_EQUAL_SUBSTITUTION);
				selectionArgs[j++] = values[i].toString();
			}
		}
		
		if ( additionalClause != null && additionalClause.trim().length() > 0 ) {
			if ( b.length() > 0 ) {
				b.append(SPACE_AND_SPACE);
			}
			b.append(additionalClause);
		}
		return new FilterCriteria( b.toString(), selectionArgs );
	}

	public static final FilterCriteria buildSelectionClause( String key, Object value ) {
		StringBuilder b = new StringBuilder();
		String[] selectionArgs;
		if ( value != null ) {
			selectionArgs = new String[1];
			selectionArgs[0] = value.toString();
			b.append(key);
			b.append(IS_EQUAL_SUBSTITUTION);
		} else {
			selectionArgs = null;
			b.append(key);
			b.append(IS_NULL);
		}
		return new FilterCriteria( b.toString(), selectionArgs );
	}
}
