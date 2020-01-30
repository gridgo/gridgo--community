package io.gridgo.connector.jetty.server;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PACKAGE, staticName = "of")
class DelegatingServlet extends GenericServlet {

    private static final long serialVersionUID = 2512710354394670721L;

    private transient final @NonNull JettyRequestHandler handler;

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        handler.onRequest((HttpServletRequest) req, (HttpServletResponse) res);
    }
}
