package org.odk.collect.android.formentry.questions;

import androidx.annotation.NonNull;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.XPathParseTool;

public class ItemsetQuestionDetails extends QuestionDetails {
    private final XPathParseTool parseTool;
    private final ItemsetDbAdapter adapter;
    private final FileUtil fileUtil;

    public ItemsetQuestionDetails(FormEntryPrompt prompt, String formAnalyticsID, @NonNull XPathParseTool parseTool,
                                  @NonNull ItemsetDbAdapter adapter, @NonNull FileUtil fileUtil) {
        super(prompt, formAnalyticsID);

        this.parseTool = parseTool;
        this.adapter = adapter;
        this.fileUtil = fileUtil;
    }

    public XPathParseTool getParseTool() {
        return parseTool;
    }

    public ItemsetDbAdapter getAdapter() {
        return adapter;
    }

    public FileUtil getFileUtil() {
        return fileUtil;
    }
}
