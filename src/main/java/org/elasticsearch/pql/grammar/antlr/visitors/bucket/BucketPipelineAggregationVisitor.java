package org.elasticsearch.pql.grammar.antlr.visitors.bucket;

import static org.elasticsearch.pql.grammar.antlr.visitors.expression.BooleanExpressionVisitors.booleanExpressionToScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.pql.grammar.PqlException;
import org.elasticsearch.pql.grammar.antlr.PqlBaseVisitor;
import org.elasticsearch.pql.grammar.antlr.PqlParser.BooleanExpressionContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.BucketStatementContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.StatsFunctionContext;
import org.elasticsearch.pql.grammar.antlr.PqlParser.StatsFunctionsContext;
import org.elasticsearch.pql.grammar.antlr.visitors.bucket.BucketPipelineAggregationVisitor.BucketPipelineAggregation;
import org.elasticsearch.pql.grammar.antlr.visitors.expression.BooleanExpressionScriptVisitor.BooleanExpressionScript;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;

public class BucketPipelineAggregationVisitor extends PqlBaseVisitor<BucketPipelineAggregation> {

    private Map<String, String> references;
    private Map<String, String> localReferences;
    private int counter = 0;

    public static BucketPipelineAggregation bucketPipelineAggregation(BucketStatementContext ctx,
            Map<String, String> references) {
        return new BucketPipelineAggregationVisitor(references).visitBucketStatement(ctx);
    }

    public BucketPipelineAggregationVisitor(Map<String, String> references) {
        this.references = references;
        this.localReferences = new HashMap<>();
    }

    @Override
    public BucketPipelineAggregation visitBucketStatement(BucketStatementContext ctx) {
        List<PipelineAggregationBuilder> aggregations = new ArrayList<>();
        if (ctx.T_SELECT() != null) {
            aggregations.addAll(bucketSelector(ctx));
        }
        if (ctx.T_STATS() != null) {
            aggregations.addAll(functionAggregation(ctx.statsFunctions()));
        }
        return new BucketPipelineAggregation(aggregations, localReferences);
    }

    private List<PipelineAggregationBuilder> functionAggregation(StatsFunctionsContext functionsContext) {
        List<PipelineAggregationBuilder> pipelines = new ArrayList<>();
        for (StatsFunctionContext statsFunction : functionsContext.statsFunction()) {
            PipelineAggregationBuilder pipelineAggregationBuilder = functionAggregate(statsFunction, "");
            pipelines.add(pipelineAggregationBuilder);
        }
        return pipelines;
    }


    private List<PipelineAggregationBuilder> bucketSelector(BucketStatementContext ctx) {
        List<PipelineAggregationBuilder> aggregations = new ArrayList<>();
        for (BooleanExpressionContext booleanExpressionContext : ctx.booleanExpression()) {
            BooleanExpressionScript script = booleanExpressionToScript(booleanExpressionContext);
            Map<String, String> params = new HashMap<>();
            for (String variable : script.getVariables()) {
//                params.put(variable, references.get(variable));
                params.put(variable, variable);
            }
            aggregations.add(PipelineAggregatorBuilders
                    .bucketSelector(generateName(), params, new Script(script.getScript())));
        }
        return aggregations;
    }

    private String generateName() {
        return "bucket_pipeline_" + counter++;
    }

    private PipelineAggregationBuilder functionAggregate(StatsFunctionContext ctx, String path) {
        PipelineAggregationBuilder result;
        String bucketsPath = ctx.fieldName.getText();//references.get(ctx.fieldName.getText());
        String name = ctx.columnName.getText();

        switch (ctx.methodName.getText()) {
            case "avg":
                result = PipelineAggregatorBuilders.avgBucket(name, bucketsPath);
                break;
            case "min":
                result = PipelineAggregatorBuilders.minBucket(name, bucketsPath);
                break;
            case "max":
                result = PipelineAggregatorBuilders.maxBucket(name, bucketsPath);
                break;
            case "percentiles":
                result = PipelineAggregatorBuilders.percentilesBucket(name, bucketsPath);
                break;
            case "sum":
                result = PipelineAggregatorBuilders.sumBucket(name, bucketsPath);
                break;
            case "derivative":
                result = PipelineAggregatorBuilders.derivative(name, bucketsPath);
                break;
            default:
                throw new PqlException(ctx.methodName, "Could not find method " + ctx.methodName.getText());
        }
        localReferences.put(name, path + ">" + name);
        return result;
    }

    public static class BucketPipelineAggregation {

        private List<PipelineAggregationBuilder> pipelineAggregations;
        private Map<String, String> references;

        BucketPipelineAggregation(List<PipelineAggregationBuilder> pipelineAggregations,
                Map<String, String> references) {
            this.pipelineAggregations = pipelineAggregations;
            this.references = references;
        }

        public List<PipelineAggregationBuilder> getPipelineAggregations() {
            return pipelineAggregations;
        }

        public Map<String, String> getReferences() {
            return references;
        }
    }
}
