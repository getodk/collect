package org.odk.collect.android.openrosa;

import org.jetbrains.annotations.Nullable;
import org.kxml2.kdom.Document;
import org.odk.collect.android.forms.FormListItem;

import java.util.List;

public interface OpenRosaFormListParser {

    @Nullable List<FormListItem> parse(Document document);
}
