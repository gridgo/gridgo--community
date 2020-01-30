package io.gridgo.connector.jetty.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.server.handler.StatisticsHandler;

import io.prometheus.client.Collector;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Collect metrics from jetty's
 * org.eclipse.jetty.server.handler.StatisticsHandler.
 * <p>
 * 
 * <pre>
 * {
 *     &#64;code
 *     Server server = new Server(8080);
 *
 *     ServletContextHandler context = new ServletContextHandler();
 *     context.setContextPath("/");
 *     server.setHandler(context);
 *
 *     HandlerCollection handlers = new HandlerCollection();
 *
 *     StatisticsHandler statisticsHandler = new StatisticsHandler();
 *     statisticsHandler.setServer(server);
 *     handlers.addHandler(statisticsHandler);
 *
 * // Register collector.
 *     new JettyStatisticsCollector(statisticsHandler).register();
 *
 *     server.setHandler(handlers);
 *
 *     server.start();
 * }
 * </pre>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticsCollector extends Collector {

    private static final List<String> EMPTY_LIST = Collections.emptyList();

    private final @NonNull StatisticsHandler sHandler;
    private final String prefix;

    public static StatisticsCollector newStatisticsCollector(StatisticsHandler statisticsHandler, String prefix) {
        return new StatisticsCollector(statisticsHandler, prefix);
    }

    @Override
    public List<MetricFamilySamples> collect() {
        return Arrays.asList(buildCounter(prefix + "_requests_total", "Number of requests", sHandler.getRequests()),
                buildGauge(prefix + "_requests_active", "Number of requests currently active",
                        sHandler.getRequestsActive()),
                buildGauge(prefix + "_requests_active_max", "Maximum number of requests that have been active at once",
                        sHandler.getRequestsActiveMax()),
                buildGauge(prefix + "_request_time_max_seconds", "Maximum time spent handling requests",
                        sHandler.getRequestTimeMax() / 1000.0),
                buildCounter(prefix + "_request_time_seconds_total", "Total time spent in all request handling",
                        sHandler.getRequestTimeTotal() / 1000.0),
                buildCounter(prefix + "_dispatched_total", "Number of dispatches", sHandler.getDispatched()),
                buildGauge(prefix + "_dispatched_active", "Number of dispatches currently active",
                        sHandler.getDispatchedActive()),
                buildGauge(prefix + "_dispatched_active_max", "Maximum number of active dispatches being handled",
                        sHandler.getDispatchedActiveMax()),
                buildGauge(prefix + "_dispatched_time_max", "Maximum time spent in dispatch handling",
                        sHandler.getDispatchedTimeMax()),
                buildCounter(prefix + "_dispatched_time_seconds_total", "Total time spent in dispatch handling",
                        sHandler.getDispatchedTimeTotal() / 1000.0),
                buildCounter(prefix + "_async_requests_total", "Total number of async requests",
                        sHandler.getAsyncRequests()),
                buildGauge(prefix + "_async_requests_waiting", "Currently waiting async requests",
                        sHandler.getAsyncRequestsWaiting()),
                buildGauge(prefix + "_async_requests_waiting_max", "Maximum number of waiting async requests",
                        sHandler.getAsyncRequestsWaitingMax()),
                buildCounter(prefix + "_async_dispatches_total",
                        "Number of requested that have been asynchronously dispatched", sHandler.getAsyncDispatches()),
                buildCounter(prefix + "_expires_total", "Number of async requests requests that have expired",
                        sHandler.getExpires()),
                buildStatusCounter(),
                buildGauge(prefix + "_stats_seconds", "Time in seconds stats have been collected for",
                        sHandler.getStatsOnMs() / 1000.0),
                buildCounter(prefix + "_responses_bytes_total", "Total number of bytes across all responses",
                        sHandler.getResponsesBytesTotal()));
    }

    private static MetricFamilySamples buildGauge(String name, String help, double value) {
        return new MetricFamilySamples(name, Type.GAUGE, help,
                Collections.singletonList(new MetricFamilySamples.Sample(name, EMPTY_LIST, EMPTY_LIST, value)));
    }

    private static MetricFamilySamples buildCounter(String name, String help, double value) {
        return new MetricFamilySamples(name, Type.COUNTER, help,
                Collections.singletonList(new MetricFamilySamples.Sample(name, EMPTY_LIST, EMPTY_LIST, value)));
    }

    private MetricFamilySamples buildStatusCounter() {
        String name = prefix + "_responses_total";
        return new MetricFamilySamples(name, Type.COUNTER, "Number of requests with response status",
                Arrays.asList(buildStatusSample(name, "1xx", sHandler.getResponses1xx()),
                        buildStatusSample(name, "2xx", sHandler.getResponses2xx()),
                        buildStatusSample(name, "3xx", sHandler.getResponses3xx()),
                        buildStatusSample(name, "4xx", sHandler.getResponses4xx()),
                        buildStatusSample(name, "5xx", sHandler.getResponses5xx())));
    }

    private static MetricFamilySamples.Sample buildStatusSample(String name, String status, double value) {
        return new MetricFamilySamples.Sample(name, Collections.singletonList("code"),
                Collections.singletonList(status), value);
    }
}
