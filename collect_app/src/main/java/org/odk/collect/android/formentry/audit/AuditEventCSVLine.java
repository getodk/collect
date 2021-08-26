package org.odk.collect.android.formentry.audit;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.shared.strings.StringUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.android.utilities.CSVUtils.getEscapedValueForCsv;

public final class AuditEventCSVLine {

    private AuditEventCSVLine() {

    }

    public static String toCSVLine(AuditEvent auditEvent, boolean isTrackingLocationsEnabled, boolean isTrackingChangesEnabled, boolean isTrackingChangesReasonEnabled) {
        FormIndex formIndex = auditEvent.getFormIndex();
        AuditEvent.AuditEventType auditEventType = auditEvent.getAuditEventType();
        long start = auditEvent.getStart();
        long end = auditEvent.getEnd();
        String latitude = auditEvent.getLatitude();
        String longitude = auditEvent.getLongitude();
        String accuracy = auditEvent.getAccuracy();
        String oldValue = auditEvent.getOldValue();
        String newValue = auditEvent.getNewValue();
        String user = auditEvent.getUser();
        String changeReason = auditEvent.getChangeReason();

        String node = formIndex == null || formIndex.getReference() == null ? "" : getXPathPath(formIndex);

        String string = String.format("%s,%s,%s,%s", auditEventType.getValue(), node, start, end != 0 ? end : "");

        if (isTrackingLocationsEnabled) {
            string += String.format(",%s,%s,%s", latitude, longitude, accuracy);
        }

        if (isTrackingChangesEnabled) {
            string += String.format(",%s,%s", getEscapedValueForCsv(oldValue), getEscapedValueForCsv(newValue));
        }

        if (user != null) {
            string += String.format(",%s", getEscapedValueForCsv(user));
        }

        if (isTrackingChangesReasonEnabled) {
            if (changeReason != null) {
                string += String.format(",%s", getEscapedValueForCsv(changeReason));
            } else {
                string += ",";
            }
        }

        return string;
    }

    /**
     * Get the XPath path of the node at a particular {@link FormIndex}.
     * <p>
     * Differs from {@link TreeReference#toString()} in that position predicates are only
     * included for repeats. For example, given a group named {@code my-group} that contains a
     * repeat named {@code my-repeat} which in turn contains a question named {@code my-question},
     * {@link TreeReference#toString()} would return paths that look like
     * {@code /my-group[1]/my-repeat[3]/my-question[1]}. In contrast, this method would return
     * {@code /my-group/my-repeat[3]/my-question}.
     * <p>
     * TODO: consider moving to {@link FormIndex}
     */
    private static String getXPathPath(FormIndex formIndex) {
        List<String> nodeNames = new ArrayList<>();
        nodeNames.add(formIndex.getReference().getName(0));

        FormIndex walker = formIndex;
        int i = 1;
        while (walker != null) {
            try {
                String currentNodeName = formIndex.getReference().getName(i);
                if (walker.getInstanceIndex() != -1) {
                    currentNodeName = currentNodeName + "[" + (walker.getInstanceIndex() + 1) + "]";
                }
                nodeNames.add(currentNodeName);
            } catch (IndexOutOfBoundsException e) {
                Timber.i(e);
            }

            walker = walker.getNextLevel();
            i++;
        }
        return "/" + StringUtils.join("/", nodeNames);
    }
}
