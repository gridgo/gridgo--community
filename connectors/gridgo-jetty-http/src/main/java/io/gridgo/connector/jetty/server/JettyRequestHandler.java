package io.gridgo.connector.jetty.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface JettyRequestHandler {

    void onRequest(HttpServletRequest req, HttpServletResponse resp);
}
