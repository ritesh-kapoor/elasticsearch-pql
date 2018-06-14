package org.elasticsearch.pql.grammar.antlr.visitors.literal;

import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.FloatLiteralContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.IntegerLiteralContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.StringLiteralContext;

class LiteralStringVisitor extends PqlBaseVisitor<String> {

    @Override
    public String visitIntegerLiteral(IntegerLiteralContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitFloatLiteral(FloatLiteralContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitStringLiteral(StringLiteralContext ctx) {
        return ctx.getText().substring(1, ctx.getText().length() - 1);
    }
}