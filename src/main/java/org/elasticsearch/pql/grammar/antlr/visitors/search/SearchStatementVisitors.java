package org.elasticsearch.pql.grammar.antlr.visitors.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.pql.grammar.antlr.PqlParser.SearchStatementContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.SearchStatementsContext;

public class SearchStatementVisitors {

    public static QueryBuilder searchStatementToQuery(SearchStatementContext ctx) {
        return new SearchStatementQueryVisitor().visitSearchStatement(ctx);
    }


    public static QueryBuilder searchStatementsToQuery(SearchStatementsContext ctx) {
        return new SearchStatementsQueryVisitor().visitSearchStatements(ctx);
    }

}
