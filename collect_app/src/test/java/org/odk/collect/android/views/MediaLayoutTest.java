package org.odk.collect.android.views;

import android.media.MediaPlayer;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import junit.framework.Assert;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.logic.FileReference;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class MediaLayoutTest {

    private static final String RANDOM_URI = "randomMediaURI";

    private final String audioURI;
    private final String imageURI;
    private final String videoURI;

    private FormIndex formIndex;
    private MediaPlayer mediaPlayer;
    private ReferenceManager referenceManager;
    private FileReference reference;

    private MediaLayout mediaLayout;
    private AudioButton audioButton;
    private AppCompatImageButton videoButton;
    private ImageView imageView;
    private TextView textView;
    private TextView missingImage;
    private ImageView divider;
    private boolean isReferenceManagerStubbed;

    public MediaLayoutTest(String audioURI, String imageURI, String videoURI) {
        this.audioURI = audioURI;
        this.imageURI = imageURI;
        this.videoURI = videoURI;
    }

    @ParameterizedRobolectricTestRunner.Parameters()
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null,          null,           null},
                {RANDOM_URI,    null,           null},
                {null,          RANDOM_URI,     null},
                {null,          null,           RANDOM_URI},
                {RANDOM_URI,    RANDOM_URI,     null},
                {RANDOM_URI,    null,           RANDOM_URI},
                {null,          RANDOM_URI,     RANDOM_URI},
                {RANDOM_URI,    RANDOM_URI,     RANDOM_URI}
        });
    }

    @Before
    public void setUp() throws InvalidReferenceException {
        formIndex = mock(FormIndex.class);
        mediaPlayer = mock(MediaPlayer.class);
        reference = mock(FileReference.class);
        referenceManager = mock(ReferenceManager.class);
        textView = new TextView(RuntimeEnvironment.application);

        mediaLayout = new MediaLayout(RuntimeEnvironment.application);

        audioButton = mediaLayout.audioButton;
        videoButton = mediaLayout.videoButton;
        imageView = mediaLayout.imageView;
        missingImage = mediaLayout.missingImage;
        divider = mediaLayout.divider;

        /*
         * Stub reference manager randomly to account for both illegal URI and proper URI while
         * attempting to load image view
         */
        if (new Random().nextBoolean()) {
            stubReferenceManager();
        }
    }

    @Test
    public void viewShouldBecomeVisibleIfUriPresent() {
        Assert.assertNotNull(mediaLayout);
        Assert.assertEquals(VISIBLE, mediaLayout.getVisibility());
        assertVisibility(GONE, audioButton, videoButton, imageView, missingImage, divider);

        mediaLayout.setAVT(formIndex, "", textView, audioURI, imageURI, videoURI, null, mediaPlayer);

        // we do not check for the validity of the URIs for the audio and video while loading MediaLayout
        assertVisibility(audioURI == null ? GONE : VISIBLE, audioButton);
        assertVisibility(videoURI == null ? GONE : VISIBLE, videoButton);

        if (imageURI == null || !isReferenceManagerStubbed) {
            // either the URI wasn't provided or it encountered InvalidReferenceException
            assertVisibility(GONE, imageView, missingImage);
        } else {
            // either the bitmap was successfully loaded or the file was missing
            Assert.assertNotSame(imageView.getVisibility(), missingImage.getVisibility());
        }
    }

    /*
     * Stubbing {@link ReferenceManager} to return random file name in order to prevent
     * {@link InvalidReferenceException}
     */
    private void stubReferenceManager() throws InvalidReferenceException {
        isReferenceManagerStubbed = true;

        doReturn(reference).when(referenceManager).DeriveReference(RANDOM_URI);
        doReturn(RANDOM_URI).when(reference).getLocalURI();
        mediaLayout.setReferenceManager(referenceManager);
    }

    /**
     * @param visibility Expected visibility
     * @param views      Views whose actual visibility is to be asserted
     */
    private void assertVisibility(int visibility, View... views) {
        for (View view : views) {
            Assert.assertEquals(visibility, view.getVisibility());
        }
    }
}
