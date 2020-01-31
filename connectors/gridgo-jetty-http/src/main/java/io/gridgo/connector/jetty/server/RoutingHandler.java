package io.gridgo.connector.jetty.server;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    private final Set<PatternHandlerPair> handlers = new TreeSet<>();
    private final @NonNull PathMatcher pathMatcher;

    public Disposable addHandler(String path, Handler handler) {
        synchronized (handlers) {
            var pair = new PatternHandlerPair(path, handler);
            if (handlers.add(pair)) {
                super.addHandler(handler);
                return () -> {
                    synchronized (handlers) {
                        super.removeHandler(handler);
                        handlers.remove(pair);
                    }
                };
            }
        }
        throw new PathExistingException("pathSpec '" + path + "' has been added");
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

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
