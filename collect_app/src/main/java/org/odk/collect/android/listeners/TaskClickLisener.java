package org.odk.collect.android.listeners;

import org.odk.collect.android.loaders.TaskEntry;

public interface TaskClickLisener {
     void onAcceptClicked(TaskEntry taskEntry);
     void onSMSClicked(long taskId);
     void onPhoneClicked(long taskId);
     void onRejectClicked(TaskEntry taskEntry);
}
