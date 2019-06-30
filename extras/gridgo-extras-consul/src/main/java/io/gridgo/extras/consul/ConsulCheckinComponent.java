package io.gridgo.extras.consul;

import com.orbitz.consul.Consul;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.agent.Registration;

import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.ContextAwareComponent;
import lombok.Getter;
import lombok.Setter;

public class ConsulCheckinComponent implements ContextAwareComponent {

    @Getter
    private Consul client;

    @Setter
    private GridgoContext context;

    private Registration registration;

    private Thread heartbeatThread;

    private boolean ownedClient = false;

    private long heartbeatIntervalMs = 0;

    public ConsulCheckinComponent(Registration registration) {
        this(registration, 0L);
    }

    public ConsulCheckinComponent(Registration registration, long heartbeatIntervalMs) {
        this(registration, heartbeatIntervalMs, Consul.builder().build());
        this.ownedClient = true;
    }

    public ConsulCheckinComponent(Registration registration, long heartbeatIntervalMs, String url) {
        this(registration, heartbeatIntervalMs, Consul.builder().withUrl(url).build());
        this.ownedClient = true;
    }

    public ConsulCheckinComponent(Registration registration, long heartbeatIntervalMs, Consul client) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
        this.registration = registration;
        this.client = client;
    }

    @Override
    public void start() {
        var agentClient = client.agentClient();
        agentClient.register(registration);

        if (heartbeatIntervalMs > 0) {
            this.heartbeatThread = new Thread(this::runHeartbeat);
            heartbeatThread.setDaemon(true);
            heartbeatThread.start();
        }
    }

    private void runHeartbeat() {
        var agentClient = client.agentClient();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                agentClient.pass(registration.getId());
            } catch (NotRegisteredException e) {
                agentClient.register(registration);
            }

            try {
                Thread.sleep(heartbeatIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void stop() {
        this.heartbeatThread.interrupt();
        client.agentClient().deregister(registration.getId());
        if (ownedClient)
            this.client.destroy();
    }

    @Override
    public String getName() {
        return "component.consul." + registration.getName() + "#" + registration.getId();
    }
}
