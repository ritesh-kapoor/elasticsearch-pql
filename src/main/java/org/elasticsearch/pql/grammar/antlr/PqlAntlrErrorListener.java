package org.elasticsearch.pql.grammar.antlr;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class PqlAntlrErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        throw new ParseCancellationException(formatMessage(line, charPositionInLine, msg));
    }

    public static String formatMessage(int line, int charPositionInLine, String message) {
        return "line " + line + ":" + charPositionInLine + " " + message;
    }
}
