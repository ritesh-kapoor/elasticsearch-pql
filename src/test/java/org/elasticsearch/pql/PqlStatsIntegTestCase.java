package org.elasticsearch.pql;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;

public class PqlStatsIntegTestCase extends PqlIntegTestCase {

    @Before
    public void setUp() throws Exception {
        setUpDb(client(), "employee", "/employee-test/employee-mapping.json",
                "/employee-test/employee-data.json");
    }

    public void testMetricAggregation() throws Exception {
        String response = run(
                "source 'employee' | stats minAge=min(Age),maxAge=max(Age),countAge=count(Age),percentilesAge=percentiles(Age),sumAge=sum(Age),cardinalityAge=cardinality(Age)");
        assertThat(response, hasJsonPath("$.aggregations.cardinalityAge.value", equalTo(17)));
        assertThat(response, hasJsonPath("$.aggregations.countAge.value", equalTo(26)));
        assertThat(response, hasJsonPath("$.aggregations.maxAge.value", equalTo(65.0)));
        assertThat(response, hasJsonPath("$.aggregations.minAge.value", equalTo(44.0)));
        assertThat(response, hasJsonPath("$.aggregations.percentilesAge.values", notNullValue()));
        assertThat(response, hasJsonPath("$.aggregations.sumAge.value", equalTo(1415.0)));

    }

    public void testStatsAfterSearchOverField() throws Exception {
        String response = run(
                "source 'employee' | search Gender='Male' | stats min_age=min(Age),max_age=max(Age) by MaritalStatus ");
        assertThat(response,
                hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Married')].max_age.value", contains(65.0)));
        assertThat(response, hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Unmarried')].max_age.value",
                contains(60.0)));
    }

    public void testStatsOverMultipleColumns() throws Exception {
        String response = run(
                "source 'employee' |  stats min_age=min(Age),max_age=max(Age) by MaritalStatus,Gender ");
        assertThat(response, hasJsonPath(
                "$.aggregations.MaritalStatus.buckets[?(@.key=='Married')].Gender.buckets[?(@.key=='Male')].min_age.value",
                contains(44.0)));
        assertThat(response, hasJsonPath(
                "$.aggregations.MaritalStatus.buckets[?(@.key=='Married')].Gender.buckets[?(@.key=='Female')].min_age.value",
                contains(44.0)));
        assertThat(response, hasJsonPath(
                "$.aggregations.MaritalStatus.buckets[?(@.key=='Unmarried')].Gender.buckets[?(@.key=='Male')].min_age.value",
                contains(46.0)));
        assertThat(response, hasJsonPath(
                "$.aggregations.MaritalStatus.buckets[?(@.key=='Unmarried')].Gender.buckets[?(@.key=='Female')].min_age.value",
                contains(45.0)));
    }

    public void testDateHistogram() throws Exception {
        String response = run(
                "source 'employee' | datehistogram field='DateOfJoining', interval='year' ");
        assertThat(response,
                hasJsonPath("$.aggregations.date_histogram.buckets.size()", equalTo(47)));
    }

    public void testDateHistogramGroupByColumn() throws Exception {
        String response = run(
                "source 'employee' | datehistogram field='DateOfJoining', interval='year' by Gender");
        assertThat(response,
                hasJsonPath("$.aggregations.date_histogram.buckets[0].Gender", notNullValue()));
    }

    public void testHistogramGroupByColumn() throws Exception {
        String response = run(
                "source 'employee' | histogram field='Age', interval='10' by Gender");
        assertThat(response,
                hasJsonPath(
                        "$.aggregations.histogram.buckets[?(@.key==40.0)].Gender.buckets[?(@.key=='Female')].doc_count",
                        contains(5)));
        assertThat(response, hasJsonPath(
                "$.aggregations.histogram.buckets[?(@.key==40.0)].Gender.buckets[?(@.key=='Male')].doc_count",
                contains(3)));
    }

    public void testHistogramGroupByColumnAndMetricFunction() throws Exception {
        String response = run(
                "source 'employee' | histogram field='Age', interval='10' do max_age=max(Age) by Gender");
        assertThat(response,
                hasJsonPath(
                        "$.aggregations.histogram.buckets[?(@.key==40.0)].Gender.buckets[?(@.key=='Female')].max_age.value",
                        contains(49.0)));
        assertThat(response, hasJsonPath(
                "$.aggregations.histogram.buckets[?(@.key==40.0)].Gender.buckets[?(@.key=='Male')].max_age.value",
                contains(46.0)));
    }
}
