package org.elasticsearch.pql.grammar.antlr.visitors.search;

import static org.elasticsearch.pql.grammar.antlr.visitors.expression.BooleanExpressionVisitors.booleanExpressionToQuery;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.SearchStatementContext;

public class SearchStatementQueryVisitor extends PqlBaseVisitor<QueryBuilder> {

    @Override
    public QueryBuilder visitSearchStatement(SearchStatementContext ctx) {
        return booleanExpressionToQuery(ctx.booleanExpression());
    }
}