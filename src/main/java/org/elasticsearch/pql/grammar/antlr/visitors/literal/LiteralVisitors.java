package org.elasticsearch.pql.grammar.antlr.visitors.literal;

import org.elasticsearch.pql.grammar.antlr.PqlParser.IntegerLiteralContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.LiteralContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.StringLiteralContext;

public class LiteralVisitors {

    public static String literalToString(LiteralContext ctx) {
        return new LiteralStringVisitor().visitLiteral(ctx);
    }

    public static Long literalToInteger(IntegerLiteralContext ctx) {
        return new LiteralObjectVisitor().visitIntegerLiteral(ctx);
    }

    public static Object literalToObject(LiteralContext ctx) {
        return new LiteralObjectVisitor().visitLiteral(ctx);
    }

    public static String stringLiteralToString(StringLiteralContext ctx) {
        return new LiteralStringVisitor().visitStringLiteral(ctx);
    }
}
