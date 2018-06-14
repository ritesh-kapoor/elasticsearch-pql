package org.elasticsearch.pql.grammar.antlr.visitors.expression;

import static org.elasticsearch.pql.grammar.antlr.visitors.identifier.IdentifierVisitors.identifierToString;
import static org.elasticsearch.pql.grammar.antlr.visitors.literal.LiteralVisitors.literalToString;

import java.util.HashSet;
import java.util.Set;
import org.elasticsearch.pql.grammar.PqlException;
import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.BooleanExpressionContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.PrimaryBooleanExpressionContext;
import org.elasticsearch.pql.grammar.antlr.visitors.expression.BooleanExpressionScriptVisitor.BooleanExpressionScript;

public class BooleanExpressionScriptVisitor extends PqlBaseVisitor<BooleanExpressionScript> {

    @Override
    public BooleanExpressionScript visitBooleanExpression(BooleanExpressionContext ctx) {
        if (ctx == null) {
            return null;
        }
        if (ctx.primaryBooleanExpression() != null) {
            return visitPrimaryBooleanExpression(ctx.primaryBooleanExpression());
        }
        Operator op = Operator.toOperator(ctx.op);
        BooleanExpressionScript left = visitBooleanExpression(ctx.booleanExpression(0));
        BooleanExpressionScript right = visitBooleanExpression(ctx.booleanExpression(1));
        String identifier = identifierToString(ctx.identifier);

        switch (op) {
            case AND:
                return new BooleanExpressionScript(left.script + " && " + right.script, left.variables,
                        right.variables);
            case OR:
                return new BooleanExpressionScript(left.script + " || " + right.script, left.variables,
                        right.variables);
            case EQ:
                return new BooleanExpressionScript(
                        "params." + identifier + " == " + literalToString(ctx.literal()), identifier);
            case NEQ:
                return new BooleanExpressionScript(
                        "params." + identifier + " != " + literalToString(ctx.literal()), identifier);
            case GT:
                return new BooleanExpressionScript(
                        "params." + identifier + " > " + literalToString(ctx.literal()), identifier);
            case GTE:
                return new BooleanExpressionScript(
                        "params." + identifier + " >= " + literalToString(ctx.literal()), identifier);
            case LT:
                return new BooleanExpressionScript(
                        "params." + identifier + " < " + literalToString(ctx.literal()), identifier);
            case LTE:
                return new BooleanExpressionScript(
                        "params." + identifier + " <= " + literalToString(ctx.literal()), identifier);
        }
        throw new PqlException("Illegal state");
    }

    @Override
    public BooleanExpressionScript visitPrimaryBooleanExpression(PrimaryBooleanExpressionContext ctx) {
        BooleanExpressionScript result = visitBooleanExpression(ctx.booleanExpression());
        return new BooleanExpressionScript("(" + result + ")", result.variables, new HashSet<>());
    }

    public static class BooleanExpressionScript {

        private String script;
        private Set<String> variables;

        BooleanExpressionScript(String script, Set<String> params1, Set<String> params2) {
            this.script = script;
            this.variables = new HashSet<>(params1);
            this.variables.addAll(params2);
        }

        BooleanExpressionScript(String script, String params1) {
            this.script = script;
            this.variables = new HashSet<>();
            this.variables.add(params1);
        }

        public String getScript() {
            return script;
        }

        public Set<String> getVariables() {
            return variables;
        }
    }
}