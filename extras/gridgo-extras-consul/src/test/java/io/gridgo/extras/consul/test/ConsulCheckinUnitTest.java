package io.gridgo.extras.consul.test;

import org.junit.Test;

import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;

import io.gridgo.extras.consul.ConsulCheckinComponent;

public class ConsulCheckinUnitTest {

    @Test
    public void testComponentWithHeartbeat() throws InterruptedException {
        var registration = ImmutableRegistration.builder() //
                                                .id("gridgo-test").name("Gridgo Consul Test").port(8080)
                                                .check(Registration.RegCheck.ttl(3L)) //
                                                .build();
        var component = new ConsulCheckinComponent(registration, 1000L);
        component.start();
        Thread.sleep(5000);
        component.stop();
    }

    @Test
    public void testComponent() throws InterruptedException {
        var registration = ImmutableRegistration.builder() //
                                                .id("gridgo-test-non-expired") //
                                                .name("Gridgo Consul Test Non-Expired") //
                                                .build();
        var component = new ConsulCheckinComponent(registration, 1000L);
        component.start();
        component.stop();
    }
}
