package org.odk.collect.android.audio;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.databinding.VolumeBarBinding;

public class VolumeBar extends FrameLayout {

    private ProgressBar progressBar;
    private Integer lastAmplitude;

    public VolumeBar(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VolumeBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VolumeBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        progressBar = VolumeBarBinding.inflate(LayoutInflater.from(context), this, true).getRoot();
    }

    public void addAmplitude(int amplitude) {
        lastAmplitude = amplitude;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress((int) ((amplitude * 6) / 22760d * 100), true);
        }
    }

    @Nullable
    public Integer getLatestAmplitude() {
        return lastAmplitude;
    }
}
