package org.odk.collect.android.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;

import org.odk.collect.android.R;

/**
 * Created by rajat on 20/3/17.
 */

public class FullScreenMediaController extends MediaController {
    private Context mContext;
    private ImageButton mFullScreenButton;
    private AlertDialog mLangDialog;
    private boolean fullScreenFlag;

    private OnClickListener mFullScreenOnClickListener;
    public FullScreenMediaController(Context context) {
        super(context);
        mContext = context;
        fullScreenFlag = true;
    }

    public void setMFullScreenOnClickListener(OnClickListener mFullScreenOnClickListener) {
        this.mFullScreenOnClickListener = mFullScreenOnClickListener;
    }

    public void toggleImageButton() {
        fullScreenFlag = !fullScreenFlag;
    }

    public boolean getFullScreenFlag() {
        return fullScreenFlag;
    }

    public void setAnchorView(View view) {
        super.setAnchorView(view);
        mFullScreenButton = new ImageButton(mContext);
        if (fullScreenFlag) {
            mFullScreenButton.setImageResource(R.drawable.ic_fullscreen);
        } else {
            mFullScreenButton.setImageResource(R.drawable.ic_fullscreen_exit);
        }
        mFullScreenButton.setOnClickListener(mFullScreenOnClickListener);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        addView(mFullScreenButton, params);

    }

}
