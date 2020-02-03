package io.gridgo.connector.jetty.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestPartMatcher {

    @Test
    public void testsimple() {
        var pathMatcher = PathMatcher.DEFAULT_CASE_INSENSITIVE;
        var pattern = "/key/{key}";

        var key = "akljsdf";
        var path = "/key/" + key;

        assertTrue(pathMatcher.match(pattern, path));

        var uriVariables = pathMatcher.extractUriTemplateVariablesOrNull(pattern, path);
        assertEquals(key, uriVariables.get("key"));
    }
}
