package io.gridgo.connector.jetty.server;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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
import lombok.NonNull;

@AllArgsConstructor
class RoutingHandler extends HandlerCollection {

    private final Map<String, Handler> map = new LinkedHashMap<>();
    private final @NonNull PathMatcher pathMatcher;

    public Disposable addHandler(String pathSpec, Handler handler) {
        synchronized (map) {
            var old = map.putIfAbsent(pathSpec, handler);
            if (old == null) {
                super.addHandler(handler);
                return () -> {
                    synchronized (map) {
                        super.removeHandler(handler);
                        map.remove(pathSpec, handler);
                    }
                };
            }
        }
        throw new PathExistingException("pathSpec '" + pathSpec + "' has been added");
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        for (var entry : map.entrySet()) {
            var pattern = entry.getKey();
            var path = request.getPathInfo();
            var variables = pathMatcher.extractUriTemplateVariablesOrNull(pattern, path);
            if (variables != null) {
                request.setAttribute(HttpCommonConstants.URI_TEMPLATE_VARIABLES, variables);
                entry.getValue().handle(target, baseRequest, request, response);
                return;
            }
        }
    }
}
