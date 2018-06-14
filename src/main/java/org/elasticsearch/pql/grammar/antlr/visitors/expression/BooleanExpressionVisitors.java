package org.elasticsearch.pql.grammar.antlr.visitors.expression;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.pql.grammar.antlr.PqlParser.BooleanExpressionContext;
import org.elasticsearch.pql.grammar.antlr.visitors.expression.BooleanExpressionScriptVisitor.BooleanExpressionScript;

public abstract class BooleanExpressionVisitors {

    public static QueryBuilder booleanExpressionToQuery(BooleanExpressionContext ctx) {
        return new BooleanExpressionQueryVisitor().visitBooleanExpression(ctx);
    }

    public static BooleanExpressionScript booleanExpressionToScript(BooleanExpressionContext ctx) {
        return new BooleanExpressionScriptVisitor().visitBooleanExpression(ctx);
    }
}