package io.gridgo.jetty.test;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.gridgo.connector.jetty.server.JettyHttpServer;
import io.gridgo.connector.jetty.server.JettyHttpServerManager;
import io.gridgo.utils.ThreadUtils;

public class TestJettyServerManager {

    private final String address = "localhost:8089";

    private JettyHttpServerManager serverManager;

    private JettyHttpServer server;

    @Before
    public void setUp() {
        serverManager = JettyHttpServerManager.getInstance();
        server = serverManager.getOrCreateJettyServer(address, true, null, true, null);
        server.start();
    }

    @Test
    public void testShutdownServerManager() throws Exception {
        var shutdownMethod = ThreadUtils.class.getDeclaredMethod("doShutdown");
        shutdownMethod.setAccessible(true);
        shutdownMethod.invoke(null);

        ThreadUtils.sleep(100);
        assertTrue(!server.isStarted());
    }
}
