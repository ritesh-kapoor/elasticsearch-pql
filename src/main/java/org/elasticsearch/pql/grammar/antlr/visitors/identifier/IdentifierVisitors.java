package org.elasticsearch.pql.grammar.antlr.visitors.identifier;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class IdentifierVisitors {

    public static String identifierToString(Token ctx) {
        if (ctx == null) {
            return null;
        }
        return ctx.getText();
    }

    public static String identifierToString(TerminalNode identifier) {
        return identifier.getText();
    }
}
