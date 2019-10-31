package io.gridgo.socket;

import static io.gridgo.socket.SocketConstants.BATCHING_ENABLED;
import static io.gridgo.socket.SocketConstants.BUFFER_SIZE;
import static io.gridgo.socket.SocketConstants.MAX_BATCH_SIZE;
import static io.gridgo.socket.SocketConstants.RING_BUFFER_SIZE;
import static io.gridgo.socket.SocketConstants.USE_DIRECT_BUFFER;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.HasReceiver;
import io.gridgo.connector.HasResponder;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.exceptions.InvalidPlaceholderException;
import io.gridgo.connector.support.exceptions.MalformedEndpointException;

/**
 * The sub-class must annotated by {@link io.gridgo.connector.ConnectorResolver
 * ConnectorResolver} which syntax has some placeholders as:
 * <ol>
 * <li><b>{type}</b>: push, pull, pub, sub, pair</li>
 * <li><b>{transport}</b>: tcp, pgm, epgm, inproc, ipc</li>
 * <li><b>[{role}]</b>: for <b>'pair'</b> pattern, indicate the socket will be
 * <i>active</i> or <i>passive</i></li>
 * <li><b>{host}</b>: allow ipv4, ipv6 (with bracket [])</li>
 * <li><b>[{interface}]</b>: use for multicast transport types (pgm or epgm)
 * </li>
 * <li><b>{port}</b>: port (to bind-on or connect-to)</li>
 * </ol>
 *
 * @author bachden
 *
 */
public class SocketConnector extends AbstractConnector implements Connector {

    public static final Set<String> MULTICAST_TRANSPORTS = //
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("pgm", "epgm")));

    public static final int DEFAULT_BUFFER_SIZE = 128 * 1024;

    public static final int DEFAULT_RINGBUFFER_SIZE = 1024;

    public static final int DEFAULT_MAX_BATCH_SIZE = 1000;

    private String address;

    private SocketOptions options;

    private final SocketFactory factory;

    private boolean batchingEnabled = false;

    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;

    private int bufferSize = DEFAULT_BUFFER_SIZE;

    private int ringBufferSize = DEFAULT_RINGBUFFER_SIZE;

    private boolean useDirectBuffer = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    protected SocketConnector(SocketFactory factory) {
        this.factory = factory;
    }

    private void initConsumerAndProducer() {
        Producer p = null;
        Consumer c = null;
        switch (this.options.getType().toLowerCase()) {
        case "push":
        case "pub":
            p = SocketProducer.builder() //
                    .context(getContext()) //
                    .factory(factory) //
                    .options(options) //
                    .address(address) //
                    .bufferSize(bufferSize) //
                    .ringBufferSize(ringBufferSize) //
                    .batchingEnabled(batchingEnabled) //
                    .maxBatchSize(maxBatchSize) //
                    .build();
            break;
        case "pull":
        case "sub":
            c = SocketConsumer.builder() //
                    .context(getContext()) //
                    .factory(factory) //
                    .options(options) //
                    .address(address) //
                    .bufferSize(bufferSize) //
                    .useDirectBuffer(useDirectBuffer) //
                    .build();
            break;
        case "pair":
            String role = this.getPlaceholder("role");
            if (role == null || role.isBlank()) {
                throw new MalformedEndpointException("Pair socket require socket role (connect or bind)");
            }
            switch (role.trim().toLowerCase()) {
            case "connect":
                p = SocketProducer.builder() //
                        .context(getContext()) //
                        .factory(factory) //
                        .options(options) //
                        .address(address) //
                        .bufferSize(bufferSize) //
                        .ringBufferSize(ringBufferSize) //
                        .batchingEnabled(batchingEnabled) //
                        .maxBatchSize(maxBatchSize) //
                        .build();
                p.start();
                c = ((HasReceiver) p).getReceiver();
                break;
            case "bind":
                c = SocketConsumer.builder() //
                        .context(getContext()) //
                        .factory(factory) //
                        .options(options) //
                        .address(address) //
                        .bufferSize(bufferSize) //
                        .useDirectBuffer(useDirectBuffer) //
                        .build();
                c.start();
                p = ((HasResponder) c).getResponder();
                break;
            default:
                throw new InvalidPlaceholderException("Invalid pair socket role, expected 'connect' or 'bind'");
            }
            break;
        default:
        }
        this.producer = Optional.ofNullable(p);
        this.consumer = Optional.ofNullable(c);
    }

    @Override
    public void onInit() {
        var config = getConnectorConfig();
        var type = config.getPlaceholders().getProperty(SocketConstants.TYPE);
        var transport = config.getPlaceholders().getProperty(SocketConstants.TRANSPORT);
        var host = config.getPlaceholders().getProperty(SocketConstants.HOST);
        var portPlaceholder = config.getPlaceholders().getProperty(SocketConstants.PORT);
        var port = portPlaceholder != null ? Integer.parseInt(portPlaceholder) : 0;

        String nic = null;
        if (MULTICAST_TRANSPORTS.contains(transport.trim().toLowerCase()))
            nic = getPlaceholder("interface");

        address = transport + "://" + ((nic == null || nic.isBlank()) ? "" : (nic + ";")) + host
                + (port > 0 ? (":" + port) : "");

        var params = config.getParameters();
        ringBufferSize = Integer.valueOf(params.getOrDefault(RING_BUFFER_SIZE, ringBufferSize).toString());
        batchingEnabled = Boolean.valueOf(params.getOrDefault(BATCHING_ENABLED, batchingEnabled).toString());
        maxBatchSize = Integer.valueOf(params.getOrDefault(MAX_BATCH_SIZE, maxBatchSize).toString());
        bufferSize = Integer.valueOf(params.getOrDefault(BUFFER_SIZE, bufferSize).toString());
        useDirectBuffer = Boolean.valueOf(params.getOrDefault(USE_DIRECT_BUFFER, useDirectBuffer).toString());

        options = new SocketOptions(type);
        options.getConfig().putAll(params);

        initConsumerAndProducer();
    }
}
