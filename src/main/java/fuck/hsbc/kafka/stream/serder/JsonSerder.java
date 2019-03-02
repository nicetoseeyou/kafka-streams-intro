package fuck.hsbc.kafka.stream.serder;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;

import java.util.Map;

public class JsonSerder implements Serde<JsonNode> {
    private Serializer<JsonNode> serializer;
    private Deserializer<JsonNode> deserializer;

    public JsonSerder() {
        super();
        this.serializer = new JsonSerializer();
        this.deserializer = new JsonDeserializer();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.serializer.configure(configs, isKey);
        this.deserializer.configure(configs, isKey);
    }

    @Override
    public void close() {
        this.serializer.close();
        this.deserializer.close();
    }

    @Override
    public Serializer<JsonNode> serializer() {
        return this.serializer;
    }

    @Override
    public Deserializer<JsonNode> deserializer() {
        return this.deserializer;
    }
}
