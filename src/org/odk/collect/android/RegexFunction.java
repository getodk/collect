/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android;

import org.javarosa.core.model.condition.IFunctionHandler;

import java.util.Vector;

/**
 * Looks for a "regex" call in XForm and uses String matching to return true and
 * false.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class RegexFunction implements IFunctionHandler {

    public Object eval(Object[] args) {

        String str = (String) args[0];
        String regex = (String) args[1];

        Boolean result = str.matches(regex);

        return result;
    }


    public String getName() {
        return "regex";
    }


    @SuppressWarnings("unchecked")
    public Vector getPrototypes() {

        Class[] prototypes = {String.class, String.class};
        Vector v = new Vector();
        v.add(prototypes);
        return v;
    }



    public boolean rawArgs() {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean realTime() {
        // TODO Auto-generated method stub
        return false;
    }

}
