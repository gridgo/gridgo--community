package io.gridgo.extras.prometheus;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashSet;

import io.gridgo.core.GridgoContext;
import io.gridgo.core.Processor;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

public class MetricsProcessor implements Processor {

    private CollectorRegistry collectorRegistry;

    private String prefix;

    public MetricsProcessor(String prefix) {
        this.collectorRegistry = CollectorRegistry.defaultRegistry;
        this.prefix = prefix;
    }

    @Override
    public void process(RoutingContext rc, GridgoContext gc) {
        try (var writer = new StringWriter()) {
            write004(writer, collectorRegistry.filteredMetricFamilySamples(new HashSet<>()));
            rc.getDeferred().resolve(Message.ofAny(writer.getBuffer().toString()));
        } catch (IOException e) {
            rc.getDeferred().reject(e);
        }
    }

    /**
     * Write out the text version 0.0.4 of the given MetricFamilySamples. Add custom
     * prefix for pegasus app.
     */
    private void write004(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
        /*
         * See http://prometheus.io/docs/instrumenting/exposition_formats/ for the
         * output format specification.
         */
        while (mfs.hasMoreElements()) {
            Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
            writer.write("# HELP ");
            writer.write(prefix + "_");
            writer.write(metricFamilySamples.name);
            writer.write(' ');
            writeEscapedHelp(writer, metricFamilySamples.help);
            writer.write('\n');

            writer.write("# TYPE ");
            writer.write(prefix + "_");
            writer.write(metricFamilySamples.name);
            writer.write(' ');
            writer.write(typeString(metricFamilySamples.type));
            writer.write('\n');

            for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
                writer.write("pegasus_");
                writer.write(sample.name);
                if (sample.labelNames.size() > 0) {
                    writer.write('{');
                    for (int i = 0; i < sample.labelNames.size(); ++i) {
                        writer.write(prefix + "_");
                        writer.write(sample.labelNames.get(i));
                        writer.write("=\"");
                        writeEscapedLabelValue(writer, sample.labelValues.get(i));
                        writer.write("\",");
                    }
                    writer.write('}');
                }
                writer.write(' ');
                writer.write(Collector.doubleToGoString(sample.value));
                if (sample.timestampMs != null) {
                    writer.write(' ');
                    writer.write(sample.timestampMs.toString());
                }
                writer.write('\n');
            }
        }
    }

    private static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\\':
                writer.append("\\\\");
                break;
            case '\"':
                writer.append("\\\"");
                break;
            case '\n':
                writer.append("\\n");
                break;
            default:
                writer.append(c);
            }
        }
    }

    private static void writeEscapedHelp(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\\':
                writer.append("\\\\");
                break;
            case '\n':
                writer.append("\\n");
                break;
            default:
                writer.append(c);
            }
        }
    }

    private static String typeString(Collector.Type t) {
        switch (t) {
        case GAUGE:
            return "gauge";
        case COUNTER:
            return "counter";
        case SUMMARY:
            return "summary";
        case HISTOGRAM:
            return "histogram";
        default:
            return "untyped";
        }
    }

}
