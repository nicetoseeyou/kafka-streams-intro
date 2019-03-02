package fuck.hsbc.kafka.stream.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PropHandler {
    public static Properties read(String file) throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(Paths.get(file)));
        return properties;
    }
}
