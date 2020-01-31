package io.gridgo.connector.jetty.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerCollection;

import io.gridgo.connector.httpcommon.HttpCommonConstants;
import io.gridgo.connector.jetty.exceptions.PathExistingException;
import io.gridgo.connector.jetty.support.PathMatcher;
import io.gridgo.framework.support.watch.Disposable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
class RoutingHandler extends HandlerCollection {

    @AllArgsConstructor
    @ToString(onlyExplicitlyIncluded = true)
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class PatternHandlerPair implements Comparable<PatternHandlerPair> {

        @ToString.Include
        @EqualsAndHashCode.Include
        private final @NonNull String pattern;
        private final @NonNull Handler handler;

        @Override
        public int compareTo(PatternHandlerPair o) {
            if (this.equals(o))
                return 0;

            var delta = o.pattern.split("/").length - this.pattern.split("/").length;
            if (delta != 0)
                return delta;

            return o.pattern.compareTo(this.pattern);
        }
    }

    private static final HttpMethod[] ALL_METHODS = HttpMethod.values();

    private final Map<HttpMethod, Set<PatternHandlerPair>> methodToHandlers = new NonBlockingHashMap<>();
    private final @NonNull PathMatcher pathMatcher;

    public Disposable addHandler(String path, Handler handler, HttpMethod... methods) {
        methods = methods == null || methods.length == 0 ? ALL_METHODS : methods;
        var pair = new PatternHandlerPair(path, handler);
        var jobs = new ArrayList<Runnable>();

        for (var method : methods) {
            var handlers = methodToHandlers.compute(method, (k, v) -> v != null ? v : new TreeSet<>());
            if (handlers.add(pair)) {
                jobs.add(() -> {
                    super.removeHandler(handler);
                    handlers.remove(pair);
                });
            }
        }

        if (jobs.isEmpty())
            throw new PathExistingException("pathSpec '" + path + "' has been added for methods: " + methods);

        super.addHandler(handler);
        return () -> jobs.forEach(Runnable::run);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        var method = HttpMethod.fromString(request.getMethod());
        var handlers = methodToHandlers.get(method);
        if (handlers == null)
            return;

        for (var entry : handlers) {
            var pattern = entry.pattern;
            var path = request.getPathInfo();
            var variables = pathMatcher.extractUriTemplateVariablesOrNull(pattern, path);
            if (variables != null) {
                request.setAttribute(HttpCommonConstants.URI_TEMPLATE_VARIABLES, variables);
                entry.handler.handle(target, baseRequest, request, response);
                return;
            }
        }
    }
}
