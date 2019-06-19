package org.odk.collect.android.http;

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
        assertEquals("text/xml", mapper.map("file.xml"));
        assertEquals("audio/3gpp", mapper.map("file.3gpp"));
        assertEquals("video/3gpp", mapper.map("file.3gp"));
        assertEquals("video/avi", mapper.map("file.avi"));
        assertEquals("audio/amr", mapper.map("file.amr"));
        assertEquals("text/csv", mapper.map("file.csv"));
        assertEquals("image/jpeg", mapper.map("file.jpg"));
        assertEquals("audio/mp3", mapper.map("file.mp3"));
        assertEquals("video/mp4", mapper.map("file.mp4"));
        assertEquals("audio/ogg", mapper.map("file.oga"));
        assertEquals("audio/ogg", mapper.map("file.ogg"));
        assertEquals("video/ogg", mapper.map("file.ogv"));
        assertEquals("audio/wav", mapper.map("file.wav"));
        assertEquals("video/webm", mapper.map("file.webm"));
        assertEquals("application/vnd.ms-excel", mapper.map("file.xls"));
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