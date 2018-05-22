package org.odk.collect.android.tasks.sms.models;

public class SmsProgress {
    private double totalCount;
    private double completedCount;

    public SmsProgress() {
        totalCount = 0;
        completedCount = 0;
    }

    public double getPercentage() {
        return (completedCount / totalCount) * 100;
    }

    public double getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(double totalCount) {
        this.totalCount = totalCount;
    }

    public double getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(double completedCount) {
        this.completedCount = completedCount;
    }
}
