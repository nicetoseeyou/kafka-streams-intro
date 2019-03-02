package fuck.hsbc.kafka.stream.filter;

import org.apache.kafka.streams.Topology;

import java.util.Properties;

public interface Filter {
    void init();

    Topology topology();

    Properties getConfig();
}
