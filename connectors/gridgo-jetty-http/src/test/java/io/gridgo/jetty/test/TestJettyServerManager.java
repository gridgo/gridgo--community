package io.gridgo.jetty.test;

import org.junit.Before;
import org.junit.Test;

import io.gridgo.connector.jetty.server.JettyHttpServerManager;
import io.gridgo.utils.ThreadUtils;

public class TestJettyServerManager {

    private final String address = "localhost:8089";

    private JettyHttpServerManager serverManager;

    @Before
    public void setUp() {
        serverManager = JettyHttpServerManager.getInstance();
        serverManager.getOrCreateJettyServer(address, true, null, true, null);
    }

    @Test
    public void testShutdownServerManager() throws Exception {
        var shutdownMethod = ThreadUtils.class.getDeclaredMethod("doShutdown");
        shutdownMethod.setAccessible(true);
        shutdownMethod.invoke(null);
    }
}
