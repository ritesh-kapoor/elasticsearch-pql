package org.elasticsearch.pql.grammar.antlr.visitors.source;

import static org.elasticsearch.pql.grammar.antlr.visitors.literal.LiteralVisitors.stringLiteralToString;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.SourceStatementContext;

public class SourceStatementVisitor extends PqlBaseVisitor<SearchRequestBuilder> {

    private Client client;

    public SourceStatementVisitor(Client client) {
        this.client = client;
    }

    @Override
    public SearchRequestBuilder visitSourceStatement(SourceStatementContext ctx) {
        return client.prepareSearch(stringLiteralToString(ctx.stringLiteral()));
    }

    public static SearchRequestBuilder sourceStatement(Client client, SourceStatementContext ctx) {
        return new SourceStatementVisitor(client).visitSourceStatement(ctx);
    }
}