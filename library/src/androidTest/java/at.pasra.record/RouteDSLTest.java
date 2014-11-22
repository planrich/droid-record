package at.pasra.record;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import at.pasra.record.remote.RouteUtil;

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
        assertEquals("/abc", RouteUtil.resolveDSL("/abc", null));
        assertEquals("/user/william", RouteUtil.resolveDSL("/user/{name}", target));
        assertEquals("/user/william", RouteUtil.resolveDSL("/user/{name#toString}", target));
        assertEquals("/user/william/details", RouteUtil.resolveDSL("/user/{name}/details", target));
        assertEquals("/i/0", RouteUtil.resolveDSL("/i/{prop0}", target));
        assertEquals("/long/123456", RouteUtil.resolveDSL("/long/{t#getId}", target));
    }

    public void testRouteDSL_Failure() {
        try {
            assertNotSame("/{t}", RouteUtil.resolveDSL("/{t}", null));
            fail();
        } catch (NullPointerException e) {

        }
    }

}