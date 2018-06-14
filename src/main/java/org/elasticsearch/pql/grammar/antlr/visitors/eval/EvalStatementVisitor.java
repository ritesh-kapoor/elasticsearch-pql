package org.elasticsearch.pql.grammar.antlr.visitors.eval;

import static org.elasticsearch.pql.grammar.antlr.visitors.identifier.IdentifierVisitors.identifierToString;
import static org.elasticsearch.pql.grammar.antlr.visitors.literal.LiteralVisitors.stringLiteralToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.EvalStatementContext;
import org.elasticsearch.pql.grammar.antlr.visitors.eval.EvalStatementVisitor.EvalScript;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

public class EvalStatementVisitor extends PqlBaseVisitor<List<EvalScript>> {

    public static List<EvalScript> evalStatement(EvalStatementContext ctx) {
        return new EvalStatementVisitor().visitEvalStatement(ctx);
    }

    @Override
    public List<EvalScript> visitEvalStatement(EvalStatementContext ctx) {
        List<EvalScript> scripts = new ArrayList<>();
        for (int i = 0; i < ctx.IDENTIFIER().size(); i++) {
            Script script = new Script(ScriptType.INLINE, "painless", stringLiteralToString(ctx.stringLiteral(i)),
                    new HashMap<>());
            scripts.add(new EvalScript(identifierToString(ctx.IDENTIFIER(i)), script));
        }
        return scripts;
    }

    public static class EvalScript {

        private String field;
        private Script script;

        public EvalScript(String field, Script script) {
            this.field = field;
            this.script = script;
        }

        public String getField() {
            return field;
        }

        public Script getScript() {
            return script;
        }
    }
}
