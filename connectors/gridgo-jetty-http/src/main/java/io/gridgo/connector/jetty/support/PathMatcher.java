package io.gridgo.connector.jetty.support;

import java.util.Comparator;
import java.util.Map;

import io.gridgo.connector.jetty.exceptions.PathNotMatchException;
import io.gridgo.connector.jetty.support.DefaultPathMatcher.PathMatcherBuilder;

public interface PathMatcher {

    static final PathMatcher DEFAULT_CASE_SENSITIVE = builder().caseSensitive(true).build();

    static final PathMatcher DEFAULT_CASE_INSENSITIVE = builder().caseSensitive(false).build();

    static PathMatcherBuilder builder() {
        return DefaultPathMatcher.builder();
    }

    boolean isPattern(String path);

    boolean match(String pattern, String path);

    boolean matchStart(String pattern, String path);

    String extractPathWithinPattern(String pattern, String path);

    Map<String, String> extractUriTemplateVariables(String pattern, String path) throws PathNotMatchException;

    Map<String, String> extractUriTemplateVariablesOrNull(String pattern, String path);

    Comparator<String> getPatternComparator(String path);

    String combine(String pattern1, String pattern2);

}
