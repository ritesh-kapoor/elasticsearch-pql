package org.elasticsearch.pql.grammar.antlr;

import static java.lang.String.join;

import java.util.stream.Collectors;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

public class PqlAntlrErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void reportInputMismatch(Parser recognizer, InputMismatchException e) throws RecognitionException {
        String msg = "mismatched input " + getTokenErrorDisplay(e.getOffendingToken());
        String tokens = getTokens(recognizer.getTokenNames(), e.getExpectedTokens());
        msg += " expecting one of " + tokens;
        RecognitionException ex = new RecognitionException(msg, recognizer, recognizer.getInputStream(),
                recognizer.getContext());
        ex.initCause(e);
        recognizer.notifyErrorListeners(e.getOffendingToken(), msg, ex);
    }

    private String getTokens(String[] tokenNames, IntervalSet expectedTokens) {
        return join(",", expectedTokens.toList().stream().map(s ->
                PqlLexer.TOKEN_MAPPING.containsKey(s) ?
                        PqlLexer.TOKEN_MAPPING.get(s) : PqlLexer.VOCABULARY.getDisplayName(s))
                .collect(Collectors.toList()));
    }

    @Override
    protected void reportUnwantedToken(Parser recognizer) {
        if (!this.inErrorRecoveryMode(recognizer)) {
            this.beginErrorCondition(recognizer);
            Token t = recognizer.getCurrentToken();
            String tokenName = this.getTokenErrorDisplay(t);
            IntervalSet expecting = this.getExpectedTokens(recognizer);
            String msg = "extraneous input " + tokenName + " expecting "
                    + getTokens(recognizer.getTokenNames(), expecting);
            recognizer.notifyErrorListeners(t, msg, (RecognitionException) null);
        }
    }

    @Override
    public void reportMissingToken(Parser recognizer) {
        beginErrorCondition(recognizer);
        Token t = recognizer.getCurrentToken();
        IntervalSet expecting = getExpectedTokens(recognizer);
        String msg = "missing " + expecting.toString(recognizer.getTokenNames()) + " at " + getTokenErrorDisplay(t);
        throw new RecognitionException(msg, recognizer, recognizer.getInputStream(), recognizer.getContext());
    }
}
