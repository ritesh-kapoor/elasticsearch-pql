package org.elasticsearch.pql;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;

public class PqlQueryIntegTestCase extends PqlIntegTestCase {

    @Before
    public void setUp() throws Exception {
        setUpDb(client(), "employee", "/employee-test/employee-mapping.json",
                "/employee-test/employee-data.json");
    }

    public void testMatchQuery() throws Exception {
        assertThat(
                run("source 'employee' | search Gender='Male' "),
                hasJsonPath("$.hits.total", equalTo(12))
        );
    }

    public void testTermQuery() throws Exception {
        assertThat(
                run("source 'employee' | search Gender==='Male' "),
                hasJsonPath("$.hits.total", equalTo(12))
        );
    }

    public void testAndQuery() throws Exception {
        String response = run("source 'employee' | search Gender='Male' AND MaritalStatus='Unmarried' ");
        assertThat(response, hasJsonPath("$.hits.total", equalTo(3)));
    }

    public void testOrQuery() throws Exception {
        String response = run("source 'employee' | search Gender='Male' OR MaritalStatus='Unmarried' ");
        assertThat(response, hasJsonPath("$.hits.total", equalTo(21)));
    }

    public void testWildCardQuery() throws Exception {
        String response = run("source 'employee' | search wildcard MaritalStatus='Marr*' ");
        assertThat(response, hasJsonPath("$.hits.total", equalTo(14)));
    }

    public void testInclusiveComparisonQuery() throws Exception {
        String response = run("source 'employee' | search Salary>=100000 && Salary<=103000 ");
        assertThat(response, hasJsonPath("$.hits.total", equalTo(7)));
    }

    public void testComparisonQuery() throws Exception {
        String response = run("source 'employee' | search Salary>100000 && Salary<103000 ");
        assertThat(response, hasJsonPath("$.hits.total", equalTo(2)));
    }

    public void testNotEqualQuery() throws Exception {
        String response = run("source 'employee' | search LastName!='RECHKEMMER' ");
        assertThat(response, hasJsonPath("$.hits.total", equalTo(25)));
    }

    public void testRegexQuery() throws Exception {
        String response = run("source 'employee' | search regex MaritalStatus='Marr.*' ");
        assertThat(response, hasJsonPath("$.hits.total", equalTo(14)));
    }

    public void testLimitAndSize() throws Exception {
        String response = run("source 'employee' | limit 1,2");
        assertThat(response, hasJsonPath("$.hits.hits.size()", equalTo(2)));
    }

    public void testSortByColumn() throws Exception {
        String response = run("source 'employee' | sort Gender Desc");
        assertThat(response, hasJsonPath("$.hits.hits[0]._source.Gender", equalTo("Male")));
    }

    public void testSortByMultipleColumns() throws Exception {
        String response = run("source 'employee' | sort Gender Desc,MaritalStatus ASC");
        assertThat(response, hasJsonPath("$.hits.hits[0]._source.Gender", equalTo("Male")));
    }

    public void testAndOrQuery() throws Exception {
        String response = run(
                "source 'employee' | search Gender='Male' AND (MaritalStatus='Unmarried' OR FirstName='GENARO') ");
        assertThat(response, hasJsonPath("$.hits.total", equalTo(4)));
    }

    public void testEvalQuery() throws Exception {
        String response = run(
                "source 'employee' | eval NewSalary='return 9000+doc[\"Salary\"].value',NewSalary2='return 9000+doc[\"Salary\"].value'");
        assertThat(response, hasJsonPath("$.hits.hits[0].fields.NewSalary"));
        assertThat(response, hasJsonPath("$.hits.hits[0]._source"));
    }
}
