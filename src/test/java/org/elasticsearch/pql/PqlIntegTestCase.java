package org.elasticsearch.pql;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.pql.plugin.PqlPlugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.junit.annotations.TestLogging;
import org.elasticsearch.transport.Netty4Plugin;

@TestLogging("level:DEBUG")
public class PqlIntegTestCase extends ESIntegTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return asList(PainlessPlugin.class, Netty4Plugin.class, PqlPlugin.class);
    }

    public void testPluginIsLoaded() {
        NodesInfoResponse response = client().admin().cluster().prepareNodesInfo().setPlugins(true).get();
        for (NodeInfo nodeInfo : response.getNodes()) {
            boolean pluginFound = false;
            for (PluginInfo pluginInfo : nodeInfo.getPlugins().getPluginInfos()) {
                if (pluginInfo.getName().equals(PqlPlugin.class.getName())) {
                    pluginFound = true;
                    break;
                }
            }
            assertThat(pluginFound, is(true));
        }
    }

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder()
                .put(super.nodeSettings(nodeOrdinal))
                .put(NetworkModule.HTTP_ENABLED.getKey(), "true")
                .put(NetworkModule.HTTP_ENABLED.getKey(), "true")
                .build();
    }

    protected void setUpDb(Client client, String index, String mappingLocation, String dataLocation)
            throws Exception {
        super.setUp();
        String mapping = IOUtils.toString(getClass().getResourceAsStream(mappingLocation));
        String data = IOUtils.toString(getClass().getResourceAsStream(dataLocation));
        BulkRequestBuilder bulkRequest = client().prepareBulk();

        client.admin().indices().prepareCreate(index)
                .addMapping(index, mapping, XContentType.JSON).execute().get();
        stream(data.trim().split("\n")).forEach(s -> bulkRequest.add(
                client.prepareIndex(index, index).setSource(s, XContentType.JSON)
        ));
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            throw new Exception(bulkResponse.buildFailureMessage());
        }
        client.admin().indices().refresh(new RefreshRequest(index, index)).actionGet();
    }


    protected String run(String query) throws IOException {
        logger.info("Request for query : {}", query);

        Response explainResponse = getRestClient()
                .performRequest("GET", "/_pql/explain", Collections.singletonMap("query", query));
        String explainResponseJson = IOUtils.toString(explainResponse.getEntity().getContent());

        logger.info("Explain : {} ", explainResponseJson);
        Response response = getRestClient()
                .performRequest("GET", "/_pql", Collections.singletonMap("query", query));
        String responseJson = IOUtils.toString(response.getEntity().getContent());
        logger.info("Response : {}", responseJson);
        return responseJson;
    }
}
