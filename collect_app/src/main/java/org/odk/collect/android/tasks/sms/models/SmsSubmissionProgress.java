package org.odk.collect.android.tasks.sms.models;

public class SmsSubmissionProgress {
    private int totalCount;
    private int completedCount;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public int getPercentage() {
        return (completedCount / totalCount) * 100;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }
}
