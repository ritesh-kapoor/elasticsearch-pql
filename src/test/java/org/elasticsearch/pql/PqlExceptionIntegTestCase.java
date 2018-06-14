package org.elasticsearch.pql;

import org.junit.Before;
import org.junit.Test;

public class PqlExceptionIntegTestCase extends PqlIntegTestCase {

    @Before
    public void setUp() throws Exception {
        setUpDb(client(), "employee", "/employee-test/employee-mapping.json",
                "/employee-test/employee-data.json");
    }

    @Test(expected = Exception.class)
    public void testWrongQuery() throws Exception {
        run("source 'employee' s");
    }


    @Test(expected = Exception.class)
    public void testWrongQuerydel() throws Exception {
        run("source 'employee' | ");
    }


    @Test(expected = Exception.class)
    public void testWrongStatsFunction() throws Exception {
        run("source 'employee' | search Gender='Male' | stats min_age=min2(Age) by MaritalStatus ");
    }

   /* @Test
    public void testWrongStatsFunction2() throws Exception {
        run("source 'employee' | stats minAge=min(Age),maxAge=max(Age),countAge=count(Age),percentilesAge=percentiles(Age),sumAge=sum(Age),cardinalityAge=cardinality(Age) | bucket stats min_age2=min(min_age) ");
    }*/
}
