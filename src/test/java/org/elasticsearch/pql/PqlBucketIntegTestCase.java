package org.elasticsearch.pql;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.contains;

import org.junit.Before;
import org.junit.Ignore;

public class PqlBucketIntegTestCase extends PqlIntegTestCase {

    @Before
    public void setUp() throws Exception {
        setUpDb(client(), "employee", "/employee-test/employee-mapping.json",
                "/employee-test/employee-data.json");
    }

    // TODO: Need to think for supporting this
   /* public void testAggregationMetricsWithFilter() throws Exception {
        String response = run(
                "source 'employee' | search Gender='Male' | stats min_age=min(Age),max_age=max(Age) | search max_age=66");
        assertThat(response,
                hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Married')].max_age.value", contains(65.0)));
        assertThat(response, hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Unmarried')].max_age.value",
                contains(60.0)));
    }*/


    public void testBucketAndSelectionOverStatsByColumns() throws Exception {
        String response = run(
                "source 'employee' |  stats min_age=min(Age),max_age=max(Age) by MaritalStatus,Gender | bucket select min_age=44.0 AND max_age=65.0");
        assertThat(response, hasJsonPath(
                "$.aggregations.MaritalStatus.buckets[?(@.key=='Married')].Gender.buckets[?(@.key=='Male')].min_age.value",
                contains(44.0)));
        assertThat(response,
                hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Unmarried')].Gender.buckets.size()",
                        contains(0)));
    }

    @Ignore
    public void testBucketStatsOverStatsByColumns() throws Exception {
        String response = run(
//                "source 'employee' |  stats min_age=min(Age),max_age=max(Age) by MaritalStatus,Gender | bucket select min_age=44.0 AND max_age=65.0 | bucket stats min_age2=min(min_age)");
                "source 'employee' |  stats min_age=min(Age),max_age=max(Age) by MaritalStatus,Gender| bucket stats min_age2=min(min_age)");
        assertThat(response, hasJsonPath(
                "$.aggregations.MaritalStatus.buckets[?(@.key=='Married')].Gender.buckets[?(@.key=='Male')].min_age.value",
                contains(44.0)));
        assertThat(response,
                hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Unmarried')].Gender.buckets.size()",
                        contains(0)));
    }

    public void testHistogramAndBucketDerivative() throws Exception {
        String response = run(
                "source 'employee' |  datehistogram field='DateOfJoining', interval='year' do min_age=min(Age)| bucket stats derivative_age=derivative(min_age)");
        assertThat(response,
                hasJsonPath(
                        "$.aggregations.date_histogram.buckets[?(@.key_as_string=='2010-01-01')].derivative_age.value",
                        contains(8.0)));
    }


    public void testBucketSelectionTwice() throws Exception {
        String response = run(
                "source 'employee' |  stats min_age=min(Age),max_age=max(Age) by MaritalStatus,Gender | bucket select min_age=44.0 | select max_age=65.0");
        assertThat(response, hasJsonPath(
                "$.aggregations.MaritalStatus.buckets[?(@.key=='Married')].Gender.buckets[?(@.key=='Male')].min_age.value",
                contains(44.0)));
        assertThat(response,
                hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Unmarried')].Gender.buckets.size()",
                        contains(0)));
    }


    public void testAggregationMultipleSummaryOverMultipleColumns3() throws Exception {
        String response = run(
                "source 'employee' |  stats min_age=min(Age),max_age=max(Age) by MaritalStatus,Gender | bucket select min_age=44.0  |  select max_age=67.0");
        assertThat(response,
                hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Married')].Gender.buckets.size()",
                        contains(0)));
        assertThat(response,
                hasJsonPath("$.aggregations.MaritalStatus.buckets[?(@.key=='Unmarried')].Gender.buckets.size()",
                        contains(0)));
    }
}
