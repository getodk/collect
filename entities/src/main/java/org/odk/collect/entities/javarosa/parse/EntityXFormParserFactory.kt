package org.odk.collect.entities.javarosa.parse

import org.javarosa.xform.parse.IXFormParserFactory
import org.javarosa.xform.parse.XFormParser

class EntityXFormParserFactory(base: IXFormParserFactory) : IXFormParserFactory.Wrapper(base) {
    override fun apply(parser: XFormParser): XFormParser {
        val processor = EntityFormParseProcessor()
        parser.addProcessor(processor)

        return parser
    }
}
