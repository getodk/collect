package org.odk.collect.android.audio;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.visualizer.amplitude.AudioRecordView;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.databinding.WaveformLayoutBinding;

import java.util.Random;

public class Waveform extends FrameLayout {

    // Allows us to play with the waveform on an emulator
    @SuppressWarnings("PMD.RedundantFieldInitializer")
    public static final boolean SIMULATED = false;

    private AudioRecordView audioRecordView;
    private Integer lastAmplitude;

    public Waveform(@NotNull Context context) {
        super(context);
        init(context);
    }

    public Waveform(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Waveform(@NotNull Context context, @NotNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        audioRecordView = WaveformLayoutBinding.inflate(LayoutInflater.from(context), this, true).getRoot();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        audioRecordView.setChunkMaxHeight(getLayoutParams().height);
    }

    public void addAmplitude(int amplitude) {
        if (SIMULATED) {
            amplitude = new Random().nextInt(22760);
        }

        lastAmplitude = amplitude;
        audioRecordView.update(amplitude);
    }

    @Nullable
    public Integer getLatestAmplitude() {
        return lastAmplitude;
    }

    public void clear() {
        if (lastAmplitude != null) {
            lastAmplitude = null;
            audioRecordView.recreate();
        }
    }
}
