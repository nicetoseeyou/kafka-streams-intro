package fuck.hsbc.kafka.stream.inject;

import fuck.hsbc.kafka.stream.util.PropHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InjectApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(InjectApp.class);

    public static void main(String[] args) {

        String targetTopic = args[0],
                keyTemplateFile = args[1],
                valueTemplateFile = args[2];
        Integer keyIncluded = Integer.parseInt(args[3]),
                keyExcluded = Integer.parseInt(args[4]),
                batchSize = Integer.parseInt(args[5]);
        String configFile = args[6];
        Integer totalBatches = Integer.parseInt(args[7]);

        LOGGER.info("TargetTopic={}, BatchSize={}, TotalBatches={}", targetTopic, batchSize, totalBatches);
        LOGGER.info("KeyTemplateFile={}, ValueTemplateFile={}, KeyRange=[{},{}]", keyTemplateFile, valueTemplateFile, keyIncluded, keyExcluded);
        LOGGER.info("ConfigFile={}", configFile);

        try {
            Properties config = PropHandler.read(configFile);
            String keyTemplate = Files.lines(Paths.get(keyTemplateFile)).findFirst().get();
            String valueTemplate = Files.lines(Paths.get(valueTemplateFile)).findFirst().get();
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
            LOGGER.info("Data Inject will Start");
            final ScheduledFuture taskHandler = executor.scheduleAtFixedRate(
                    new Injector(keyIncluded, keyExcluded, batchSize, keyTemplate, valueTemplate, targetTopic, config, new AtomicInteger(0)),
                    1, 1, TimeUnit.SECONDS);
            executor.schedule(() -> {
                taskHandler.cancel(false);
                executor.shutdown();
            }, totalBatches, TimeUnit.SECONDS);
        } catch (IOException e) {
            LOGGER.error("Failed to read file.", e);
            System.exit(1);
        }

        try {
            TimeUnit.SECONDS.sleep(totalBatches + 10);
            LOGGER.info("Data Inject terminated");
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }
    }
}
