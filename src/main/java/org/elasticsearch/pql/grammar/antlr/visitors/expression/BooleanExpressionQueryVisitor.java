package org.elasticsearch.pql.grammar.antlr.visitors.expression;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static org.elasticsearch.pql.grammar.antlr.visitors.identifier.IdentifierVisitors.identifierToString;
import static org.elasticsearch.pql.grammar.antlr.visitors.literal.LiteralVisitors.literalToObject;
import static org.elasticsearch.pql.grammar.antlr.visitors.literal.LiteralVisitors.literalToString;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.pql.grammar.PqlException;
import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.BooleanExpressionContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.PrimaryBooleanExpressionContext;

public class BooleanExpressionQueryVisitor extends PqlBaseVisitor<QueryBuilder> {

    @Override
    public QueryBuilder visitBooleanExpression(BooleanExpressionContext ctx) {
        if (ctx.primaryBooleanExpression() != null) {
            return visitPrimaryBooleanExpression(ctx.primaryBooleanExpression());
        }
        Operator op = Operator.toOperator(ctx.op);
        BooleanExpressionContext left = ctx.booleanExpression(0);
        BooleanExpressionContext right = ctx.booleanExpression(1);
        switch (op) {
            case AND:
                return boolQuery()
                        .must(visitBooleanExpression(left))
                        .must(visitBooleanExpression(right));
            case OR:
                return boolQuery()
                        .should(visitBooleanExpression(left))
                        .should(visitBooleanExpression(right));
            case EQ:
                return matchQuery(identifierToString(ctx.identifier), literalToObject(ctx.literal()));
            case EQ3:
                return termQuery(identifierToString(ctx.identifier), literalToObject(ctx.literal()));
            case GT:
                return rangeQuery(identifierToString(ctx.identifier)).gt(literalToObject(ctx.literal()));
            case GTE:
                return rangeQuery(identifierToString(ctx.identifier)).gte(literalToObject(ctx.literal()));
            case LT:
                return rangeQuery(identifierToString(ctx.identifier)).lt(literalToObject(ctx.literal()));
            case LTE:
                return rangeQuery(identifierToString(ctx.identifier)).lte(literalToObject(ctx.literal()));
            case NEQ:
                return boolQuery()
                        .mustNot(matchQuery(identifierToString(ctx.identifier), literalToObject(ctx.literal())));
            case REGEX:
                return regexpQuery(identifierToString(ctx.identifier), literalToString(ctx.literal()));
            case WILDCARD:
                return wildcardQuery(identifierToString(ctx.identifier), literalToString(ctx.literal()));
        }
        throw new PqlException("Illegal state");
    }

    @Override
    public QueryBuilder visitPrimaryBooleanExpression(PrimaryBooleanExpressionContext ctx) {
        return visitBooleanExpression(ctx.booleanExpression());
    }
}