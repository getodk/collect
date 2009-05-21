package org.google.android.odk;

import org.javarosa.core.model.condition.IFunctionHandler;
import java.util.Vector;

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
