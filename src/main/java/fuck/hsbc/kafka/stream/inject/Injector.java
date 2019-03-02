package fuck.hsbc.kafka.stream.inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Injector implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Injector.class);
    private final Integer included, excluded, batchSize;
    private final String keyTemplate, valueTemplate, targetTopic;
    private final Properties config;
    private final AtomicInteger counter;

    private KafkaProducer<byte[], byte[]> producer;

    public Injector(Integer included, Integer excluded, Integer batchSize, String keyTemplate, String valueTemplate, String targetTopic, Properties config, AtomicInteger counter) {
        this.included = included;
        this.excluded = excluded;
        this.batchSize = batchSize;
        this.keyTemplate = keyTemplate;
        this.valueTemplate = valueTemplate;
        this.targetTopic = targetTopic;
        this.config = config;
        this.counter = counter;
    }

    private ProducerRecord<byte[], byte[]> getRecord(int randomKey) {
        byte[] key = String.format(keyTemplate, randomKey).getBytes();
        byte[] value = String.format(valueTemplate, randomKey, randomKey).getBytes();
        return new ProducerRecord<>(targetTopic, key, value);
    }

    @Override
    public void run() {
        producer = new KafkaProducer<>(config);
        LOGGER.info("TargetTopic={}, Key=[{},{}], BatchSize={}", targetTopic, included, excluded, batchSize);
        for (int i = 0; i < batchSize; i++) {
            int key = ThreadLocalRandom.current().nextInt(included, excluded);
            producer.send(getRecord(key));
            LOGGER.info("TargetTopic={}, Key={}", targetTopic, key);
        }
        LOGGER.info("Batch[{}] done", counter.incrementAndGet());
    }
}
