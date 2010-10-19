/**
 * 
 */

package org.odk.collect.android.logic;

import org.javarosa.core.reference.PrefixedRootFactory;
import org.javarosa.core.reference.Reference;

/**
 * @author ctsims
 */
public class FileReferenceFactory extends PrefixedRootFactory {

    String localRoot;


    public FileReferenceFactory(String localRoot) {
        super(new String[] {
            "file"
        });
        this.localRoot = localRoot;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.javarosa.core.reference.PrefixedRootFactory#factory(java.lang.String,
     * java.lang.String)
     */
    @Override
    protected Reference factory(String terminal, String URI) {
        return new FileReference(localRoot, terminal);
    }

}
