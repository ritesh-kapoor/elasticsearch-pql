package org.elasticsearch.pql.grammar;

import static org.elasticsearch.pql.grammar.antlr.PqlAntlrErrorListener.formatMessage;

import org.antlr.v4.runtime.Token;

public class PqlException extends RuntimeException {

    public PqlException(String message) {
        super(message);
    }

    public PqlException(Token token, String message) {
        super(formatMessage(token.getLine(), token.getCharPositionInLine(), message));
    }

    public PqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
