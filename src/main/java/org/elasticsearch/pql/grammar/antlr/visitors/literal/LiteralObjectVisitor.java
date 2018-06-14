package org.elasticsearch.pql.grammar.antlr.visitors.literal;

import static org.elasticsearch.pql.grammar.antlr.visitors.literal.LiteralVisitors.stringLiteralToString;

import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.FloatLiteralContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.IntegerLiteralContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.StringLiteralContext;

class LiteralObjectVisitor extends PqlBaseVisitor<Object> {

    @Override
    public Long visitIntegerLiteral(IntegerLiteralContext ctx) {
        return Long.parseLong(ctx.getText());
    }

    @Override
    public Float visitFloatLiteral(FloatLiteralContext ctx) {
        return Float.parseFloat(ctx.getText());
    }

    @Override
    public String visitStringLiteral(StringLiteralContext ctx) {
        return stringLiteralToString(ctx);
    }
}