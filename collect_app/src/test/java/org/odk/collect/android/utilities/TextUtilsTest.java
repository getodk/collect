package org.odk.collect.android.utilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class TextUtilsTest {

    private TextUtils textUtils;

    @Mock
    TextUtils.HtmlWrapper mHtml;

    @Captor
    private ArgumentCaptor<String> captor;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        textUtils = new TextUtils(mHtml);
    }

    /**
     * Should return null if provided with null and not throw a NPE.
     */
    @Test
    public void textToHtml_BounceNullInput() {
        String input = null;
        CharSequence observed = textUtils.textToHtml(input);
        assertNull(observed);
    }

    /**
     * Should call HTML processor if provided with non-null string.
     */
    @Test
    public void textToHtml_CallsHTMLOnRealInput() {
        String input = "This *bold* markdown is \n- nice,\n- efficient.\n";
        mHtml.toHtml(input);
        verify(mHtml).toHtml(captor.capture());
        String input_arg = captor.getValue();
        assertEquals(input, input_arg);
    }
}
