/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.bundle;

import android.app.Dialog;

import java.io.Serializable;

public class CollectDialogBundle implements Serializable {

    private static final String COLLECT_DIALOG_TAG = "collectDialogTag";

    private String dialogTag;
    private String dialogTitle;
    private String dialogMessage;
    private String negativeButtonText;
    private String positiveButtonText;
    private String neutralButtonText;

    public SingleButtonCallback onNegativeButtonClickCallback;
    public SingleButtonCallback onPositiveButtonClickCallback;
    public SingleButtonCallback onNeutralButtonClickCallback;

    private boolean cancelable;

    private int icon;

    CollectDialogBundle(Builder builder) {
        dialogTag = builder.dialogTag;
        dialogTitle = builder.dialogTitle;
        dialogMessage = builder.dialogMessage;
        negativeButtonText = builder.negativeButtonText;
        positiveButtonText = builder.positiveButtonText;
        neutralButtonText = builder.neutralButtonText;
        onNegativeButtonClickCallback = builder.onNegativeButtonClickCallback;
        onPositiveButtonClickCallback = builder.onPositiveButtonClickCallback;
        onNeutralButtonClickCallback = builder.onNeutralButtonClickCallback;
        cancelable = builder.cancelable;
        icon = builder.icon;
    }

    public String getDialogTag() {
        return dialogTag;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    public String getNegativeButtonText() {
        return negativeButtonText;
    }

    public String getPositiveButtonText() {
        return positiveButtonText;
    }

    public String getNeutralButtonText() {
        return neutralButtonText;
    }

    public SingleButtonCallback getOnNegativeButtonClickCallback() {
        return onNegativeButtonClickCallback;
    }

    public SingleButtonCallback getOnPositiveButtonClickCallback() {
        return onPositiveButtonClickCallback;
    }

    public SingleButtonCallback getOnNeutralButtonClickCallback() {
        return onNeutralButtonClickCallback;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public Integer getIcon() {
        return icon;
    }

    public static class Builder {

        private String dialogTag;
        private String dialogTitle;
        private String dialogMessage;
        private String negativeButtonText;
        private String positiveButtonText;
        private String neutralButtonText;

        private SingleButtonCallback onNegativeButtonClickCallback;
        private SingleButtonCallback onPositiveButtonClickCallback;
        private SingleButtonCallback onNeutralButtonClickCallback;

        private boolean cancelable;

        private int icon;

        public Builder() {
            this(COLLECT_DIALOG_TAG);
        }

        public Builder(String dialogTag) {
            this.dialogTag = dialogTag;
        }

        public Builder setDialogTitle(String dialogTitle) {
            this.dialogTitle = dialogTitle;
            return this;
        }

        public Builder setDialogMessage(String dialogMessage) {
            this.dialogMessage = dialogMessage;
            return this;
        }

        public Builder setNegativeButtonText(String negativeButtonText) {
            this.negativeButtonText = negativeButtonText;
            return this;
        }

        public Builder setPositiveButtonText(String positiveButtonText) {
            this.positiveButtonText = positiveButtonText;
            return this;
        }

        public Builder setNeutralButtonText(String neutralButtonText) {
            this.neutralButtonText = neutralButtonText;
            return this;
        }

        public Builder setOnNegatibeButtonClickCallback(SingleButtonCallback onNegativeButtonClickCallback) {
            this.onNegativeButtonClickCallback = onNegativeButtonClickCallback;
            return this;
        }

        public Builder setOnPositiveButtonClickCallback(SingleButtonCallback onPositiveButtonClickCallback) {
            this.onPositiveButtonClickCallback = onPositiveButtonClickCallback;
            return this;
        }

        public Builder setOnNeutralButtonClickCallback(SingleButtonCallback onNeutralButtonClickCallback) {
            this.onNeutralButtonClickCallback = onNeutralButtonClickCallback;
            return this;
        }

        public Builder setCancelable(Boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setIcon(int icon) {
            this.icon = icon;
            return this;
        }

        public CollectDialogBundle build() {
            return new CollectDialogBundle(this);
        }
    }

    public interface SingleButtonCallback {
        void onClick(Dialog dialog);
    }
}