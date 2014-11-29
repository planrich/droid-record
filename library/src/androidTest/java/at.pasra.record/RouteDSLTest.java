package at.pasra.record;

import junit.framework.TestCase;

import at.pasra.record.remote.RouteDSL;

import static junit.framework.Assert.assertEquals;

public class RouteDSLTest extends TestCase {

    private class Target {
        private int prop0 = 0;
        private String name = "william";
        private Long id = 123456L;

        private Target t;
        public Long getId() {
            return id;
        }
    }

    public void testRouteDSL_Success() {
        Target target = new Target();
        target.t = target;
        assertEquals("/abc", RouteDSL.resolveDSL("/abc", null));
        assertEquals("/user/william", RouteDSL.resolveDSL("/user/{name}", target));
        assertEquals("/user/william", RouteDSL.resolveDSL("/user/{name#toString}", target));
        assertEquals("/user/william/details", RouteDSL.resolveDSL("/user/{name}/details", target));
        assertEquals("/i/0", RouteDSL.resolveDSL("/i/{prop0}", target));
        assertEquals("/long/123456", RouteDSL.resolveDSL("/long/{t#getId}", target));
    }

    public void testRouteDSL_Failure() {
        try {
            assertNotSame("/{t}", RouteDSL.resolveDSL("/{t}", null));
            fail();
        } catch (NullPointerException e) {

        }
    }

}