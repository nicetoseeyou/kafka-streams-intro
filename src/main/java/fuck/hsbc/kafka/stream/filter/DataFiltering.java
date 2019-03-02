package fuck.hsbc.kafka.stream.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fuck.hsbc.kafka.stream.config.FilterConfig;
import fuck.hsbc.kafka.stream.dto.JoinerKV;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataFiltering implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFiltering.class);
    private final String leftInputTopic, rightInputTopic, rightStateStore, leftOutputTopic;
    private final Properties config;

    private String filterColumn;
    private Set<String> filterCondition, leftKeys, leftValues, rightKeys;
    private Map<String, String> keyJoinerMap;

    public DataFiltering(String leftInputTopic, String rightInputTopic, String rightStateStore, Properties config) {
        this.leftInputTopic = leftInputTopic;
        this.rightInputTopic = rightInputTopic;
        this.rightStateStore = rightStateStore;
        this.leftOutputTopic = leftInputTopic + "-streaming-out";
        this.config = config;
    }

    @Override
    public void init() {
        filterColumn = config.getProperty(FilterConfig.FILTER_COLUMN_CONFIG);

        keyJoinerMap = Stream.of(config.getProperty(FilterConfig.RIGHT_KEY_MAPPING_CONFIG).split(","))
                .map(String::trim)
                .map(JoinerKV::new)
                .collect(
                        Collectors.toMap(
                                joiner -> joiner.leftColumn,
                                joiner -> joiner.rightColumn,
                                (k1, k2) -> k2)
                );

        filterCondition = Stream.of(config.getProperty(FilterConfig.FILTER_COLUMN_CONDITION_CONFIG).split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        leftKeys = Stream.of(config.getProperty(FilterConfig.LEFT_KEY_CONFIG).split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        leftValues = Stream.of(config.getProperty(FilterConfig.LEFT_VALUE_CONFIG).split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        rightKeys = Stream.of(config.getProperty(FilterConfig.RIGHT_KEY_CONFIG).split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    @Override
    public Topology topology() {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<JsonNode, JsonNode> left = builder.stream(leftInputTopic);
        final GlobalKTable<JsonNode, JsonNode> right = builder.globalTable(rightInputTopic, Materialized.as(rightStateStore));
        left
                .join(right, alterLeftKey, joinValue)
                .filter((k, v) -> {
                    String cv = v.findValue(filterColumn).asText();
                    LOGGER.info("Filter => {}={}", filterColumn, cv);
                    return filterCondition.contains(cv);
                })
                .map(restoreRecord)
                .to(leftOutputTopic);
        return builder.build();
    }

    @Override
    public Properties getConfig() {
        return config;
    }

    /**
     * Handling some key different case before joining.
     * Possible cases:
     * 1. right.key is a subset of left.value
     * 1.1 right.key = left.key, do nothing
     * 1.2 right.key != left.key
     * 1.2.1 right.key is a subset of left.key, cut left.key
     * 1.2.2 right.key not a subset of left.key, enrich left.key
     * 2. right.key not a subset of left.value, TBD, filter in ProcessorAPI
     * <p>
     * Note: joiner must be the right.keys, "filtering.right.keys.mapping" is for joiner mapping
     * Usage: filtering.right.keys.mapping=right.keyColumn_a|left.valueColumn_a,right.keyColumn_b|left.valueColumn_b
     * It means two mapping: right.keyColumn_a=left.valueColumn_a and right.keyColumn_b=left.valueColumn_b
     */
    private KeyValueMapper<JsonNode, JsonNode, JsonNode> alterLeftKey = (k, v) -> {
        if (rightKeys.equals(leftKeys)) {
            return k;
        } else {
            ObjectNode alteredLeftKey = JsonNodeFactory.instance.objectNode();
            keyJoinerMap.forEach((rightKeyColumn, leftColumn) -> alteredLeftKey.set(rightKeyColumn, v.findValue(leftColumn)));
            LOGGER.info("AfterAlterKey => left.key={}", alteredLeftKey);
            return alteredLeftKey;
        }
    };

    /**
     * Value Join logic
     */
    private ValueJoiner<JsonNode, JsonNode, JsonNode> joinValue = (lv, rv) -> {
        ((ObjectNode) lv).set(filterColumn, rv.findValue(filterColumn));
        return lv;
    };

    /**
     * Restore left record after joining
     * left.key rebuild possible cases:
     * 1. right.key is a subset of left.value
     * 1.1 right.key = left.key, do nothing
     * 1.2 right.key != left.key
     * 1.2.1 right.key is a subset of left.key, enrich left.key
     * 1.2.2 right.key not a subset of left.key, cut left.key
     * 2. right.key not a subset of left.value, TBD, filter in ProcessorAPI
     * <p>
     * Note: joiner must be the right keys
     */
    private KeyValueMapper<JsonNode, JsonNode, KeyValue<JsonNode, JsonNode>> restoreRecord = (k, v) -> {
        ((ObjectNode) v).remove(filterColumn);
        if (rightKeys.equals(leftKeys)) {
            return KeyValue.pair(k, v);
        } else {
            ObjectNode restoredLeftKey = JsonNodeFactory.instance.objectNode();
            leftKeys.forEach(leftKeyColumn -> restoredLeftKey.set(leftKeyColumn, v.findValue(leftKeyColumn)));
            LOGGER.info("AfterRecordRestore => left.key={}", restoredLeftKey);
            return KeyValue.pair(restoredLeftKey, v);
        }
    };

}
