package org.odk.collect.android.utilities;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;

import org.javarosa.core.model.SelectChoice;

import java.util.List;

public class UnderlyingValuesConcat {
    public String asString(List<SelectChoice> items) {
        return Joiner
                .on(", ")
                .join(FluentIterable
                        .from(items)
                        .transform((item -> item.getValue()))
                );
    }
}
