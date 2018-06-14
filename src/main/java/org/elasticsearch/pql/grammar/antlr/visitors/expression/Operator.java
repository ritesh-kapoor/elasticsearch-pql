package org.elasticsearch.pql.grammar.antlr.visitors.expression;

import org.antlr.v4.runtime.Token;
import org.elasticsearch.pql.grammar.PqlException;

public enum Operator {
    AND, OR, EQ, NEQ, LT, GT, LTE, GTE, EQ3, REGEX, WILDCARD;

    public static Operator toOperator(Token op) {
        if (op == null) {
            return null;
        }
        String opText = op.getText();
        if (opText.equalsIgnoreCase("AND") || opText.equalsIgnoreCase("&&")) {
            return AND;
        } else if (opText.equalsIgnoreCase("OR") || opText.equalsIgnoreCase("||")) {
            return OR;
        } else if (opText.equals("==") || opText.equals("=")) {
            return EQ;
        } else if (opText.equals("!=") || opText.equalsIgnoreCase("NOT")) {
            return NEQ;
        } else if (opText.equals("<")) {
            return LT;
        } else if (opText.equals("<=")) {
            return LTE;
        } else if (opText.equals(">")) {
            return GT;
        } else if (opText.equals(">=")) {
            return GTE;
        } else if (opText.equals("===")) {
            return EQ3;
        } else if (opText.equalsIgnoreCase("REGEX")) {
            return REGEX;
        } else if (opText.equalsIgnoreCase("WILDCARD")) {
            return WILDCARD;
        }
        throw new PqlException(op, "Illegal operator " + opText);
    }
}
