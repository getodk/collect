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
package org.odk.collect.android.utilities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import org.odk.collect.android.activities.GoogleDriveActivity;

import java.io.Serializable;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ODKDialogFragment extends DialogFragment {

    public enum Action { FINISH, TRY_TO_LIST_FILES_FROM_GDRIVE_AGAIN
    }

    public static final String ODK_DIALOG_BUNDLE = "odkDialogBundle";

    public static ODKDialogFragment newInstance(ODKDialogBundle odkDialogBundle) {
        ODKDialogFragment odkDialogFragment = new ODKDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ODK_DIALOG_BUNDLE, odkDialogBundle);
        odkDialogFragment.setArguments(bundle);
        return odkDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ODKDialogBundle odkDialogBundle = (ODKDialogBundle) getArguments().getSerializable(ODK_DIALOG_BUNDLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(odkDialogBundle.getDialogTitle())
                .setMessage(odkDialogBundle.getDialogMessage())
                .setCancelable(odkDialogBundle.isCancelable());

        if (odkDialogBundle.getLeftButtonText() != null) {
            builder.setNegativeButton(odkDialogBundle.getLeftButtonText(), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    resolveButtonAction(odkDialogBundle.getLeftButtonAction());
                }
            });
        }

        if (odkDialogBundle.getRightButtonText() != null) {
            builder.setPositiveButton(odkDialogBundle.getRightButtonText(), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    resolveButtonAction(odkDialogBundle.getRightButtonAction());
                }
            });
        }

        if (odkDialogBundle.getIcon() != null) {
            builder.setIcon(odkDialogBundle.getIcon());
        }

        return builder.create();
    }

    private void resolveButtonAction(final Action action) {
        switch (action) {
            case FINISH:
                dismiss();
                getActivity().finish();
                break;
            case TRY_TO_LIST_FILES_FROM_GDRIVE_AGAIN:
                dismiss();
                ((GoogleDriveActivity) getActivity()).tryListFilesAgain();
                break;
            default:
        }
    }

    public static class ODKDialogBundle implements Serializable {
        private String mDialogTitle;
        private String mDialogMessage;
        private String mLeftButtonText;
        private String mRightButtonText;

        private Action mLeftButtonAction;
        private Action mRightButtonAction;

        private boolean mCancelable;

        private int mIcon;

        public ODKDialogBundle(Builder builder) {
            mDialogTitle = builder.mDialogTitle;
            mDialogMessage = builder.mDialogMessage;
            mLeftButtonText = builder.mLeftButtonText;
            mRightButtonText = builder.mRightButtonText;
            mLeftButtonAction = builder.mLeftButtonAction;
            mRightButtonAction = builder.mRightButtonAction;
            mCancelable = builder.mCancelable;
            mIcon = builder.mIcon;
        }

        public String getDialogTitle() {
            return  mDialogTitle;
        }

        public String getDialogMessage() {
            return  mDialogMessage;
        }

        public String getLeftButtonText() {
            return mLeftButtonText;
        }

        public String getRightButtonText() {
            return mRightButtonText;
        }

        public Action getLeftButtonAction() {
            return mLeftButtonAction;
        }

        public Action getRightButtonAction() {
            return mRightButtonAction;
        }

        public boolean isCancelable() {
            return mCancelable;
        }

        public Integer getIcon() {
            return mIcon;
        }

        public static class Builder {
            private String mDialogTitle;
            private String mDialogMessage;
            private String mLeftButtonText;
            private String mRightButtonText;

            private Action mLeftButtonAction;
            private Action mRightButtonAction;

            private boolean mCancelable;

            private int mIcon;

            public Builder() {
            }

            public Builder setDialogTitle(String dialogTitle) {
                mDialogTitle = dialogTitle;
                return this;
            }

            public Builder setDialogMessage(String dialogMessage) {
                mDialogMessage = dialogMessage;
                return this;
            }

            public Builder setLeftButtonText(String leftButtonText) {
                mLeftButtonText = leftButtonText;
                return this;
            }

            public Builder setRightButtonText(String rightButtonText) {
                mRightButtonText = rightButtonText;
                return this;
            }

            public Builder setLeftButtonAction(Action leftButtonAction) {
                mLeftButtonAction = leftButtonAction;
                return this;
            }

            public Builder setRightButtonAction(Action rightButtonAction) {
                mRightButtonAction = rightButtonAction;
                return this;
            }

            public Builder setCancelable(Boolean cancelable) {
                mCancelable = cancelable;
                return this;
            }

            public Builder setIcon(int icon) {
                mIcon = icon;
                return this;
            }

            public ODKDialogBundle build() {
                return new ODKDialogBundle(this);
            }
        }
    }
}
