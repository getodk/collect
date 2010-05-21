/**
 * 
 */
package org.odk.collect.android.views;

import java.io.IOException;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * @author ctsims
 *
 */
public class AudioButton extends ImageButton implements OnClickListener, OnCompletionListener {

	private String URI;
	private MediaPlayer player;
	
	public AudioButton(Context context, String URI) {
		super(context);
		this.setOnClickListener(this);
		this.URI = URI;
		Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_play_audio);
		this.setImageBitmap(b);
		this.setMinimumWidth(b.getScaledWidth(context.getResources().getDisplayMetrics()));
		player = new MediaPlayer();
	}

	public void onClick(View v) {
		try {
			synchronized(player) {
				initPlayer();
				player.setDataSource(ReferenceManager._().DeriveReference(URI).getLocalURI());
				player.prepare();
				player.setOnCompletionListener(this);
				
				player.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidReferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initPlayer() {
		if(player.isPlaying()) {
			player.stop();
			player.release();
			player = new MediaPlayer();
		}
	}

	public void onCompletion(MediaPlayer mp) {
		synchronized(mp) {
			mp.release();
			player = new MediaPlayer();
		}
	}

}
