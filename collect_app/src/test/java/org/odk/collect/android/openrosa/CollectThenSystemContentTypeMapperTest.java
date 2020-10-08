package org.odk.collect.android.openrosa;

import android.webkit.MimeTypeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectThenSystemContentTypeMapperTest {

    @Mock
    public MimeTypeMap mimeTypeMap;

    private CollectThenSystemContentTypeMapper mapper;

    @Before
    public void setup() {
        mapper = new CollectThenSystemContentTypeMapper(mimeTypeMap);
    }

    @Test
    public void whenExtensionIsRecognized_returnsTypeForFile() {
        assertEquals("audio/amr", mapper.map("file.amr"));
        assertEquals("audio/ogg", mapper.map("file.oga"));
        assertEquals("video/ogg", mapper.map("file.ogv"));
        assertEquals("video/webm", mapper.map("file.webm"));
    }

    @Test
    public void whenExtensionIsNotRecognized_returnsTypeFromAndroid() {
        when(mimeTypeMap.getMimeTypeFromExtension("mystery")).thenReturn("text/mystery");
        assertEquals("text/mystery", mapper.map("file.mystery"));
    }

    @Test
    public void whenExtensionIsNotRecognized_andAndroidDoesNotRecognize_returnsOctetStreamType() {
        assertEquals("application/octet-stream", mapper.map("file.bizarre"));
    }
}