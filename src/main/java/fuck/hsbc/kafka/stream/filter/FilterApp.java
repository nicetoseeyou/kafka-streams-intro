package fuck.hsbc.kafka.stream.filter;

import fuck.hsbc.kafka.stream.util.PropHandler;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class FilterApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterApp.class);

    public static void main(String[] args) {
        String leftInputTopic = args[0],
                rightInputTopic = args[1],
                rightStateStore = args[2],
                configFile = args[3];
        LOGGER.info("Data Filter: LeftInputTopic={}, RightInputTopic={}, RightStateStore={}, ConfigFile={}", leftInputTopic, rightInputTopic, rightStateStore, configFile);
        Properties config = null;
        try {
            config = PropHandler.read(configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to ConfigFile {}", configFile, e);
            System.exit(1);
        }
        DataFiltering filtering = new DataFiltering(leftInputTopic, rightInputTopic, rightStateStore, config);
        filtering.init();
        final KafkaStreams kafkaStreams = new KafkaStreams(filtering.topology(), filtering.getConfig());
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("data-filtering-shutdown-hook") {
            @Override
            public void run() {
                kafkaStreams.close();
                latch.countDown();
            }
        });

        try {
            kafkaStreams.start();
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }
        System.exit(0);
    }
}
