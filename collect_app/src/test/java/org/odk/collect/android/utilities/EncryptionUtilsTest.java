package org.odk.collect.android.utilities;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.odk.collect.android.exception.EncryptionException;
import org.odk.collect.android.logic.FormController;

public class EncryptionUtilsTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test public void missingInstanceID_causesEncryptionException() throws EncryptionException {
        exceptionRule.expect(EncryptionException.class);
        exceptionRule.expectMessage("This form does not specify an instanceID. You must specify one to enable encryption.");
        EncryptionUtils.getEncryptedFormInformation(null, new FormController.InstanceMetadata(null, null, null));
    }
}
