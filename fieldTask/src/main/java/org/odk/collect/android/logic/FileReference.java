/**
 * 
 */

package org.odk.collect.android.logic;

import org.javarosa.core.reference.Reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ctsims
 */
public class FileReference implements Reference {
    String localPart;
    String referencePart;


    public FileReference(String localPart, String referencePart) {
        this.localPart = localPart;
        this.referencePart = referencePart;
    }


    private String getInternalURI() {
        return "/" + localPart + referencePart;
    }


    @Override
    public boolean doesBinaryExist() {
        return new File(getInternalURI()).exists();
    }


    @Override
    public InputStream getStream() throws IOException {
        return new FileInputStream(getInternalURI());
    }


    @Override
    public String getURI() {
        return "jr://file" + referencePart;
    }


    @Override
    public boolean isReadOnly() {
        return false;
    }


    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(getInternalURI());
    }


    @Override
    public void remove() {
        // TODO bad practice to ignore return values
        new File(getInternalURI()).delete();
    }


    @Override
    public String getLocalURI() {
        return getInternalURI();
    }


	@Override
	public Reference[] probeAlternativeReferences() {
		//We can't poll the JAR for resources, unfortunately. It's possible
		//we could try to figure out something about the file and poll alternatives
		//based on type (PNG-> JPG, etc)
		return new Reference [0];
	}

}
