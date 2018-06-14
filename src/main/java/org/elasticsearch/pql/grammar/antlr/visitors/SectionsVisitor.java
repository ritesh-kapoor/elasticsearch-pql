package org.elasticsearch.pql.grammar.antlr.visitors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.pql.grammar.antlr.visitors.bucket.BucketPipelineAggregationVisitor.bucketPipelineAggregation;
import static org.elasticsearch.pql.grammar.antlr.visitors.eval.EvalStatementVisitor.evalStatement;
import static org.elasticsearch.pql.grammar.antlr.visitors.literal.LiteralVisitors.literalToInteger;
import static org.elasticsearch.pql.grammar.antlr.visitors.search.SearchStatementVisitors.searchStatementToQuery;
import static org.elasticsearch.pql.grammar.antlr.visitors.sort.SortStatementVisitor.sortStatement;
import static org.elasticsearch.pql.grammar.antlr.visitors.source.SourceStatementVisitor.sourceStatement;
import static org.elasticsearch.pql.grammar.antlr.visitors.stats.StatsStatementVisitor.statsStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.EvalStatementContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.SearchStatementContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.SectionsContext;
import org.elasticsearch.pql.grammar.antlr.visitors.bucket.BucketPipelineAggregationVisitor.BucketPipelineAggregation;
import org.elasticsearch.pql.grammar.antlr.visitors.eval.EvalStatementVisitor.EvalScript;
import org.elasticsearch.pql.grammar.antlr.visitors.sort.SortStatementVisitor.Sort;
import org.elasticsearch.pql.grammar.antlr.visitors.stats.StatsStatementVisitor.StatsAggregation;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;

public class SectionsVisitor extends PqlBaseVisitor<SearchRequestBuilder> {

    private Client client;
    private QueryBuilder query;
    private SearchRequestBuilder searchRequest;
    private List<StatsAggregation> statsAggregations = new ArrayList<>();
    private List<BucketPipelineAggregation> bucketPipelineAggregations = new ArrayList<>();
    private Map<String, String> references = new HashMap<>();


    private int i = 0;

    public SectionsVisitor(Client client) {
        this.client = client;
    }

    @Override
    public SearchRequestBuilder visitSections(SectionsContext ctx) {
        createEsQuery(ctx);
        if (query != null) {
            searchRequest.setQuery(query);
        } else {
            searchRequest.setQuery(matchAllQuery());
        }
        for (StatsAggregation aggregation : statsAggregations) {
            searchRequest.addAggregation(aggregation.getRootAggregation());
        }
        for (BucketPipelineAggregation bucketPipelineAggregation : bucketPipelineAggregations) {
            for (PipelineAggregationBuilder pipelineAggregationBuilder : bucketPipelineAggregation
                    .getPipelineAggregations()) {
                for (StatsAggregation aggregation : statsAggregations) {
                    aggregation.getLeafAggregation().subAggregation(pipelineAggregationBuilder);
                }
            }
        }
        searchRequest.setFetchSource(true);
        return searchRequest;
    }

    public void createEsQuery(SectionsContext ctx) {
        this.searchRequest = sourceStatement(client, ctx.sourceStatement());
        for (SearchStatementContext dataFilterStatement : ctx.searchStatement()) {
            if (this.query == null) {
                this.query = searchStatementToQuery(dataFilterStatement);
            } else {
                this.query = boolQuery().must(this.query).must(searchStatementToQuery(dataFilterStatement));
            }
        }

        for (EvalStatementContext evalStatement : ctx.evalStatement()) {
            List<EvalScript> scripts = evalStatement(evalStatement);
            for (EvalScript script : scripts) {
                searchRequest.addScriptField(script.getField(), script.getScript());
            }
        }
        if (ctx.sortStatement() != null) {
            List<Sort> sorts = sortStatement(ctx.sortStatement());
            for (Sort sort : sorts) {
                searchRequest.addSort(sort.getField(), sort.getSortOrder());
            }
        }

        if (ctx.limitStatement() != null) {
            Long from = literalToInteger(ctx.limitStatement().integerLiteral(0));
            Long size = literalToInteger(ctx.limitStatement().integerLiteral(1));
            searchRequest.setFrom((int) (long) from);
            searchRequest.setSize((int) (long) size);
        }

        if (ctx.statsStatement() != null) {
            List<StatsAggregation> statsAggregation = statsStatement(ctx.statsStatement());
            for (StatsAggregation stats : statsAggregation) {
                statsAggregations.add(stats);
                references.putAll(stats.getReferences());
            }
        }

        if (ctx.bucketStatement() != null) {
            BucketPipelineAggregation bucketPipelineAggregation = bucketPipelineAggregation(ctx.bucketStatement(),
                    references);
            references.putAll(bucketPipelineAggregation.getReferences());
            bucketPipelineAggregations.add(bucketPipelineAggregation);
        }

    }

    public static SearchRequestBuilder sections(Client client, SectionsContext ctx) {
        return new SectionsVisitor(client).visitSections(ctx);
    }

}