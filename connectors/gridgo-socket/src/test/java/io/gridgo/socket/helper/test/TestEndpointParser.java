package io.gridgo.socket.helper.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.helper.EndpointParser;

public class TestEndpointParser {

    @Test
    public void testFineCase() {
        String host = "localhost";
        int port = 1000;
        String scheme = "tcp";
        String address = scheme + "://" + host + ":" + port;

        Endpoint endpoint = EndpointParser.parse(address);

        assertEquals(scheme, endpoint.getProtocol());
        assertEquals("127.0.0.1", endpoint.getHost());
        assertEquals(port, endpoint.getPort());
    }

    @Test
    public void testMissingPort() {
        String host = "localhost";
        String scheme = "tcp";
        String address = scheme + "://" + host;

        Endpoint endpoint = EndpointParser.parse(address);

        assertEquals(scheme, endpoint.getProtocol());
        assertEquals("127.0.0.1", endpoint.getHost());
        assertEquals(-1, endpoint.getPort());
    }

    @Test
    public void testMissingHost() {
        String host = "";
        String scheme = "tcp";
        int port = 1000;
        String address = scheme + "://" + host + ":" + port;

        Endpoint endpoint = EndpointParser.parse(address);

        assertEquals(scheme, endpoint.getProtocol());
        assertEquals("127.0.0.1", endpoint.getHost());
        assertEquals(port, endpoint.getPort());
    }
}
