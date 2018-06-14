package org.elasticsearch.pql;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;

public class PqlSourceIntegTestCase extends PqlIntegTestCase {

    @Before
    public void setUp() throws Exception {
        setUpDb(client(), "employee", "/employee-test/employee-mapping.json",
                "/employee-test/employee-data.json");
    }

    public void testSourceQuery() throws Exception {
        assertThat(
                run("source 'employee'"),
                hasJsonPath("$.hits.total", equalTo(26))
        );
    }
}
