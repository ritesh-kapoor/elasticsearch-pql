package org.elasticsearch.pql.plugin;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.pql.grammar.PqlQuery;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;

public class PqlRestActions extends BaseRestHandler {

    private static final Logger log = LogManager.getLogger(PqlRestActions.class);
    public static final String NAME = "pql";

    protected PqlRestActions(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(GET, "_pql", this);
        controller.registerHandler(POST, "_pql", this);
        controller.registerHandler(GET, "_pql/{action}", this);
        controller.registerHandler(POST, "_pql/{action}", this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        log.debug("Handle [pql] endpoint request :", request);
        if ("explain".equalsIgnoreCase(request.param("action"))) {
            return createExplainResponse(request, client);
        } else {
            return createSearchResponse(request, client);
        }
    }

    private RestChannelConsumer createSearchResponse(RestRequest restRequest, NodeClient client) {
        String query = restRequest.param("query");
        PqlQuery pqlQuery = new PqlQuery();
        SearchRequestBuilder searchRequestBuilder = pqlQuery.buildRequest(query, client);
        logger.debug("[pql] request for query {} : request : {}", query, searchRequestBuilder.toString());

        return channel -> searchRequestBuilder.execute(new RestBuilderListener<SearchResponse>(channel) {
            @Override
            public RestResponse buildResponse(SearchResponse searchResponse, XContentBuilder xContentBuilder)
                    throws Exception {
                return new BytesRestResponse(RestStatus.OK,
                        searchResponse.toXContent(xContentBuilder, restRequest));
            }
        });
    }

    private RestChannelConsumer createExplainResponse(RestRequest restRequest, NodeClient client) {
        String query = restRequest.param("query");
        PqlQuery pqlQuery = new PqlQuery();
        SearchRequestBuilder searchRequestBuilder = pqlQuery.buildRequest(query, client);
        return restChannel -> restChannel
                .sendResponse(new BytesRestResponse(RestStatus.OK, searchRequestBuilder.toString()));
    }
}
