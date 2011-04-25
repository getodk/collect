/**
 * 
 */

package org.odk.collect.android.views;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends ImageButton implements OnClickListener {
    private final static String t = "AudioButton";
    private String URI;
    private MediaPlayer player;


    public AudioButton(Context context, String URI) {
        super(context);
        this.setOnClickListener(this);
        this.URI = URI;
        Bitmap b =
            BitmapFactory.decodeResource(context.getResources(),
                android.R.drawable.ic_lock_silent_mode_off);
        this.setImageBitmap(b);
        player = null;
    }


    @Override
    public void onClick(View v) {
        if (URI == null) {
            // No audio file specified
            Log.e(t, "No audio file was specified");
            Toast.makeText(getContext(), getContext().getString(R.string.audio_file_error),
                Toast.LENGTH_LONG).show();
            return;
        }

        String audioFilename = "";
        try {
            audioFilename = ReferenceManager._().DeriveReference(URI).getLocalURI();
        } catch (InvalidReferenceException e) {
            Log.e(t, "Invalid reference exception");
            e.printStackTrace();
        }

        File audioFile = new File(audioFilename);
        if (!audioFile.exists()) {
            // We should have an audio clip, but the file doesn't exist.
            String errorMsg = getContext().getString(R.string.file_missing, audioFile);
            Log.e(t, errorMsg);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            return;
        }

        // In case we're currently playing sounds.
        stopPlaying();

        player = new MediaPlayer();
        try {
            player.setDataSource(audioFilename);
            player.prepare();
            player.start();
            player.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                }

            });
        } catch (IOException e) {
            String errorMsg = getContext().getString(R.string.audio_file_invalid);
            Log.e(t, errorMsg);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }


    public void stopPlaying() {
        if (player != null) {
            player.release();
        }
    }
}
