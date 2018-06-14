package org.elasticsearch.pql.grammar.antlr.visitors.stats;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.elasticsearch.pql.grammar.antlr.visitors.identifier.IdentifierVisitors.identifierToString;
import static org.elasticsearch.pql.grammar.antlr.visitors.literal.LiteralVisitors.literalToObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.elasticsearch.pql.grammar.PqlException;
import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlLexer;
import org.elasticsearch.pql.grammar.antlr.PqlParser.StatsFunctionContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.StatsParametersContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.StatsStatementContext;
import org.elasticsearch.pql.grammar.antlr.visitors.stats.StatsStatementVisitor.StatsAggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

public class StatsStatementVisitor extends PqlBaseVisitor<List<StatsAggregation>> {

    private Map<String, String> references = new HashMap<>();

    public static List<StatsAggregation> statsStatement(StatsStatementContext ctx) {
        return new StatsStatementVisitor().visitStatsStatement(ctx);
    }

    @Override
    public List<StatsAggregation> visitStatsStatement(StatsStatementContext ctx) {
        AggregationBuilder leafAggregation = null, rootAggregation = null;

        switch (ctx.op.getType()) {
            case PqlLexer.T_DATEHISTOGRAM:
                rootAggregation = leafAggregation = dateHistogram(ctx.statsParameters());
                break;
            case PqlLexer.T_HISTOGRAM:
                rootAggregation = leafAggregation = histogram(ctx.statsParameters());
                break;
        }

        StringBuilder path = new StringBuilder();
        if (ctx.statsGroupingColumns() != null) {
            for (TerminalNode identifier : ctx.statsGroupingColumns().IDENTIFIER()) {
                if (leafAggregation == null) {
                    rootAggregation = leafAggregation = fieldAggregation(identifier.getText());
                } else {
                    AggregationBuilder subAggregation = fieldAggregation(identifier.getText());
                    leafAggregation.subAggregation(subAggregation);
                    leafAggregation = subAggregation;
                }
            }
            for (TerminalNode terminalNode : ctx.statsGroupingColumns().IDENTIFIER()) {
                path.append(identifierToString(terminalNode)).append(">");
            }
        }

        if (ctx.statsFunctions() != null) {
            if (rootAggregation != null) {
                for (StatsFunctionContext statsFunctionContext : ctx.statsFunctions().statsFunction()) {
                    leafAggregation.subAggregation(functionAggregation(statsFunctionContext, path.toString()));
                }
                return singletonList(new StatsAggregation(rootAggregation, leafAggregation, references));
            } else {
                List<StatsAggregation> statsAggregations = new ArrayList<>();
                for (StatsFunctionContext statsFunctionContext : ctx.statsFunctions().statsFunction()) {
                    AggregationBuilder aggregationBuilder = functionAggregation(statsFunctionContext, "");
                    statsAggregations.add(new StatsAggregation(aggregationBuilder, aggregationBuilder, references));
                }
                return statsAggregations;
            }
        }
        return singletonList(new StatsAggregation(rootAggregation, leafAggregation, references));
    }

    private TermsAggregationBuilder fieldAggregation(String columnName) {
        return AggregationBuilders.terms(columnName).field(columnName);
    }

    private DateHistogramAggregationBuilder dateHistogram(StatsParametersContext paramsContext) {
        Map<String, Object> parameters = assertParameters(asList("interval", "field"), singletonList("format"),
                paramsContext);
        return AggregationBuilders.dateHistogram("date_histogram")
                .field((String) parameters.get("field"))
                .dateHistogramInterval(new DateHistogramInterval((String) parameters.get("interval")));
    }

    private HistogramAggregationBuilder histogram(StatsParametersContext paramsContext) {
        Map<String, Object> parameters = assertParameters(asList("interval", "field"), emptyList(),
                paramsContext);
        return AggregationBuilders.histogram("histogram")
                .field((String) parameters.get("field"))
                .interval(Double.valueOf(parameters.get("interval").toString()));
    }

    private Map<String, Object> assertParameters(List<String> mandatoryParams, List<String> optionalParams,
            StatsParametersContext paramContext) {
        Map<String, Object> params = new HashMap<>();
        Set<String> statsParams = new HashSet<>();
        for (int i = 0; i < paramContext.IDENTIFIER().size(); i++) {
            String identifier = identifierToString(paramContext.IDENTIFIER(i));
            Object value = literalToObject(paramContext.literal(i));
            params.put(identifier, value);
            statsParams.add(identifier);
            if (!mandatoryParams.contains(identifier) && !optionalParams.contains(identifier)) {
                throw new PqlException("Illegal parameter " + identifier + "missing");
            }
        }
        for (String mandatoryParam : mandatoryParams) {
            if (!statsParams.contains(mandatoryParam)) {
                throw new PqlException("Mandatory parameter " + mandatoryParam + "missing");
            }
        }
        return params;
    }

    private AggregationBuilder functionAggregation(StatsFunctionContext ctx, String path) {
        AggregationBuilder result;
        switch (ctx.methodName.getText()) {
            case "avg":
                result = AggregationBuilders
                        .avg(ctx.columnName.getText())
                        .field(ctx.fieldName.getText());
                break;
            case "min":
                result = AggregationBuilders
                        .min(ctx.columnName.getText())
                        .field(ctx.fieldName.getText());
                break;
            case "max":
                result = AggregationBuilders
                        .max(ctx.columnName.getText())
                        .field(ctx.fieldName.getText());
                break;
            case "count":
                result = AggregationBuilders
                        .count(ctx.columnName.getText())
                        .field(ctx.fieldName.getText());
                break;
            case "percentiles":
                result = AggregationBuilders
                        .percentiles(ctx.columnName.getText())
                        .field(ctx.fieldName.getText());
                break;
            case "sum":
                result = AggregationBuilders
                        .sum(ctx.columnName.getText())
                        .field(ctx.fieldName.getText());
                break;
            case "cardinality":
                result = AggregationBuilders
                        .cardinality(ctx.columnName.getText())
                        .field(ctx.fieldName.getText());
                break;
            default:
                throw new PqlException(ctx.methodName, "Could not find method " + ctx.methodName.getText());
        }

        references.put(ctx.columnName.getText(), path + ctx.columnName.getText());
        return result;
    }

    public static class StatsAggregation {

        private AggregationBuilder rootAggregation;
        private AggregationBuilder leafAggregation;
        private Map<String, String> references;

        StatsAggregation(AggregationBuilder rootAggregation, AggregationBuilder leafAggregation,
                Map<String, String> references) {
            this.rootAggregation = rootAggregation;
            this.leafAggregation = leafAggregation;
            this.references = references;
        }

        public AggregationBuilder getRootAggregation() {
            return rootAggregation;
        }

        public AggregationBuilder getLeafAggregation() {
            return leafAggregation;
        }

        public Map<String, String> getReferences() {
            return references;
        }
    }
}