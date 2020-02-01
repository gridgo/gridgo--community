package io.gridgo.connector.jetty.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
class DelegateHandler extends AbstractHandler {

    private final @NonNull JettyRequestHandler _handler;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        _handler.onRequest(request, response);
        baseRequest.setHandled(true);
    }
}
