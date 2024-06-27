package org.odk.collect.entities.javarosa.parse;

import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParser;
import org.jetbrains.annotations.NotNull;

public class EntityXFormParserFactory extends IXFormParserFactory.Wrapper {

    public EntityXFormParserFactory(IXFormParserFactory base) {
        super(base);
    }

    @Override
    public XFormParser apply(@NotNull XFormParser parser) {
        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        parser.addProcessor(processor);

        return parser;
    }
}
