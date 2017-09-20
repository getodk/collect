package org.odk.collect.android.utilities;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.Lexer;
import org.javarosa.xpath.parser.Parser;
import org.javarosa.xpath.parser.XPathSyntaxException;

/**
 * @author James Knight
 */
public class XPathParseTool {
    public XPathExpression parseXPath(String xpath) throws XPathSyntaxException {
        return Parser.parse(Lexer.lex(xpath));
    }
}
