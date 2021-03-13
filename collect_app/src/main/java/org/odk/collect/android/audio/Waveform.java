package org.odk.collect.android.audio;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.visualizer.amplitude.AudioRecordView;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.WaveformLayoutBinding;

import java.util.Random;

public class Waveform extends FrameLayout {

    // Allows us to play with the waveform on an emulator
    @SuppressWarnings("PMD.RedundantFieldInitializer")
    public static final boolean SIMULATED = false;

    private AudioRecordView audioRecordView;
    private Integer lastAmplitude;
    private boolean mini;

    public Waveform(@NotNull Context context) {
        super(context);
        init(context, null);
    }

    public Waveform(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Waveform(@NotNull Context context, @NotNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        audioRecordView = WaveformLayoutBinding.inflate(LayoutInflater.from(context), this, true).getRoot();

        if (attrs != null) {
            TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Waveform, 0, 0);
            mini = styledAttributes.getBoolean(R.styleable.Waveform_mini, false);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        audioRecordView.setChunkMaxHeight(getLayoutParams().height);
    }

    public void addAmplitude(int amplitude) {
        lastAmplitude = amplitude;

        if (SIMULATED) {
            amplitude = new Random().nextInt(22760);
        }

        if (mini) {
            audioRecordView.update(amplitude * 6);
        } else {
            audioRecordView.update(amplitude);
        }
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
