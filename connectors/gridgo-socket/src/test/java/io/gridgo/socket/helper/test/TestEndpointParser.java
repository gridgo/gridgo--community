package io.gridgo.socket.helper.test;

import org.junit.Test;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.helper.EndpointParser;

public class TestEndpointParser {

    @Test
    public void testFineCase() {
        String address = "tcp://localhost:1000";
        Endpoint endpoint = EndpointParser.parse(address);
        System.out.println(endpoint);
    }
}
