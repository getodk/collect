/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CaptureSelfieVideoActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.databinding.VideoWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.CameraUtils;
import org.odk.collect.android.utilities.ContentUriProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.FileWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.analytics.AnalyticsEvents.REQUEST_HIGH_RES_VIDEO;
import static org.odk.collect.android.analytics.AnalyticsEvents.REQUEST_VIDEO_NOT_HIGH_RES;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class VideoWidget extends QuestionWidget implements WidgetDataReceiver {
    public static final boolean DEFAULT_HIGH_RESOLUTION = true;

    VideoWidgetAnswerBinding binding;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final QuestionMediaManager questionMediaManager;
    private final ActivityAvailability activityAvailability;
    private final ContentUriProvider contentUriProvider;
    private final FileWidgetUtils fileWidgetUtils;

    private String binaryName;

    public VideoWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        this(context, prompt, waitingForDataRegistry, new CameraUtils(), questionMediaManager,
                new ActivityAvailability(context), new ContentUriProvider(), new FileWidgetUtils());
    }

    public VideoWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry, CameraUtils cameraUtils,
                       QuestionMediaManager questionMediaManager, ActivityAvailability activityAvailability, ContentUriProvider contentUriProvider, FileWidgetUtils fileWidgetUtils) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.questionMediaManager = questionMediaManager;
        this.activityAvailability = activityAvailability;
        this.contentUriProvider = contentUriProvider;
        this.fileWidgetUtils = fileWidgetUtils;

        if (WidgetAppearanceUtils.isFrontCameraAppearance(getFormEntryPrompt())) {
            if (!cameraUtils.isFrontCameraAvailable()) {
                binding.captureVideo.setEnabled(false);
                ToastUtils.showLongToast(R.string.error_front_camera_unavailable);
            }
        }
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = VideoWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        if (prompt.isReadOnly()) {
            binding.captureVideo.setVisibility(View.GONE);
            binding.chooseVideo.setVisibility(View.GONE);
        } else {
            binding.captureVideo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.captureVideo.setOnClickListener(v -> onCaptureVideoButtonClick());

            if (WidgetAppearanceUtils.isNewWidget(prompt)) {
                binding.chooseVideo.setVisibility(View.GONE);
            } else {
                binding.chooseVideo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
                binding.chooseVideo.setOnClickListener(v -> chooseVideo());
            }
        }

        binding.playVideo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.playVideo.setOnClickListener(v -> playVideoFile());

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();
        binding.playVideo.setEnabled(binaryName != null && binaryName.isEmpty());

        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        // remove the file
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(),
                FileWidgetUtils.getInstanceFolder() + File.separator + binaryName);
        binaryName = null;

        // reset buttons
        binding.playVideo.setEnabled(false);

        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        return binaryName != null ? new StringData(binaryName) : null;
    }

    /**
     * Set this widget with the actual file returned by OnActivityResult.
     * Both of Uri and File are supported.
     * If the file is local, a Uri is enough for the copy task below.
     * If the chose file is from cloud(such as Google Drive),
     * The retrieve and copy task is already executed in the previous step,
     * so a File object would be presented.
     *
     * @param object Uri or File of the chosen file.
     * @see org.odk.collect.android.activities.FormEntryActivity#onActivityResult(int, int, Intent)
     */
    @Override
    public void setData(Object object) {
        binaryName = fileWidgetUtils.getUpdatedWidgetAnswer(getContext(), questionMediaManager, object, getFormEntryPrompt().getIndex().toString(),
                binaryName, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false);
        binding.playVideo.setEnabled(!binaryName.isEmpty());
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.captureVideo.setOnLongClickListener(l);
        binding.chooseVideo.setOnLongClickListener(l);
        binding.playVideo.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.captureVideo.cancelLongPress();
        binding.chooseVideo.cancelLongPress();
        binding.playVideo.cancelLongPress();
    }

    private void onCaptureVideoButtonClick() {
        if (WidgetAppearanceUtils.isFrontCameraAppearance(getFormEntryPrompt())) {
            getPermissionUtils().requestCameraAndRecordAudioPermissions((Activity) getContext(), new PermissionListener() {
                @Override
                public void granted() {
                    captureVideo();
                }

                @Override
                public void denied() {
                }
            });
        } else {
            getPermissionUtils().requestCameraPermission((Activity) getContext(), new PermissionListener() {
                @Override
                public void granted() {
                    captureVideo();
                }

                @Override
                public void denied() {
                }
            });
        }
    }

    private void captureVideo() {
        Intent intent;
        if (WidgetAppearanceUtils.isFrontCameraAppearance(getFormEntryPrompt())) {
            intent = new Intent(getContext(), CaptureSelfieVideoActivity.class);
        } else {
            intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString());
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());

        // request high resolution if configured for that...
        boolean highResolution = settings.getBoolean(GeneralKeys.KEY_HIGH_RESOLUTION, VideoWidget.DEFAULT_HIGH_RESOLUTION);

        if (highResolution) {
            intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
            analytics.logEvent(REQUEST_HIGH_RES_VIDEO, getQuestionDetails().getFormAnalyticsID(), "");
        } else {
            analytics.logEvent(REQUEST_VIDEO_NOT_HIGH_RES, getQuestionDetails().getFormAnalyticsID(), "");
        }

        if (activityAvailability.isActivityAvailable(intent)) {
            waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
            ((Activity) getContext()).startActivityForResult(intent, RequestCodes.VIDEO_CAPTURE);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.activity_not_found,
                    getContext().getString(R.string.capture_video)), Toast.LENGTH_SHORT).show();
            waitingForDataRegistry.cancelWaitingForData();
        }
    }

    private void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        if (activityAvailability.isActivityAvailable(intent)) {
            waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
            ((Activity) getContext()).startActivityForResult(intent, RequestCodes.VIDEO_CHOOSER);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.activity_not_found,
                    getContext().getString(R.string.choose_video)), Toast.LENGTH_SHORT).show();
            waitingForDataRegistry.cancelWaitingForData();
        }
    }

    private void playVideoFile() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(FileWidgetUtils.getInstanceFolder() + File.separator + binaryName);

        Uri uri = null;
        try {
            uri = contentUriProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
            FileUtils.grantFileReadPermissions(intent, uri, getContext());
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }

        intent.setDataAndType(uri, "video/*");

        if (activityAvailability.isActivityAvailable(intent)) {
            getContext().startActivity(intent);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.activity_not_found,
                    getContext().getString(R.string.view_video)), Toast.LENGTH_SHORT).show();
        }
    }
}
