package org.elasticsearch.pql.plugin;

import java.io.IOException;
import java.util.Objects;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.pql.grammar.PqlQuery;

public class PqlQueryBuilder extends AbstractQueryBuilder<PqlQueryBuilder> {

    public static final String NAME = "pql";
    private static final ParseField QUERY_FIELD = new ParseField("query");
    private final String query;

    public PqlQueryBuilder(String query) {
        if (Strings.isEmpty(query)) {
            throw new IllegalArgumentException("query is null or empty");
        } else {
            this.query = query;
        }
    }

    public PqlQueryBuilder(StreamInput in) throws IOException {
        super(in);
        this.query = in.readString();
    }

    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(this.query);
    }

    protected int doHashCode() {
        return Objects.hash(this.query);
    }

    protected boolean doEquals(PqlQueryBuilder other) {
        return Objects.equals(this.query, other.query);
    }

    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.field(QUERY_FIELD.getPreferredName(), this.query);
        this.printBoostAndQueryName(builder);
        builder.endObject();
    }

    public String getWriteableName() {
        return NAME;
    }

    public static PqlQueryBuilder fromXContent(XContentParser parser) throws IOException {
        String fieldPattern = null;
        String queryName = null;
        float boost = 1.0F;
        String currentFieldName = null;

        Token token;
        while ((token = parser.nextToken()) != Token.END_OBJECT) {
            if (token == Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else {
                if (!token.isValue()) {
                    throw new ParsingException(parser.getTokenLocation(),
                            "[pql] unknown token [" + token + "] after [" + currentFieldName + "]", new Object[0]);
                }

                if (QUERY_FIELD.match(currentFieldName)) {
                    fieldPattern = parser.text();
                } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName)) {
                    queryName = parser.text();
                } else {
                    if (!AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName)) {
                        throw new ParsingException(parser.getTokenLocation(),
                                "[pql] query does not support [" + currentFieldName + "]", new Object[0]);
                    }
                    boost = parser.floatValue();
                }
            }
        }

        if (fieldPattern == null) {
            throw new ParsingException(parser.getTokenLocation(),
                    "[pql] must be provided with a [query]", new Object[0]);
        } else {
            PqlQueryBuilder builder = new PqlQueryBuilder(fieldPattern);
            builder.queryName(queryName);
            builder.boost(boost);
            return builder;
        }
    }

    protected Query doToQuery(QueryShardContext context) throws IOException {
        PqlQuery pqlQuery = new PqlQuery();
        return pqlQuery.buildQuery(query).toQuery(context);
    }
}
