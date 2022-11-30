package org.odk.collect.android.listeners;

import org.odk.collect.android.loaders.TaskEntry;

public interface TaskClickLisener {
     void onAcceptClicked(TaskEntry taskEntry);
     void onSMSClicked(long taskId);
     void onPhoneClicked(TaskEntry taskEntry);
     void onRejectClicked(TaskEntry taskEntry);
     void onLocateClick(TaskEntry taskEntry);
}
