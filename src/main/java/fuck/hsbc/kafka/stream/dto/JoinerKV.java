package fuck.hsbc.kafka.stream.dto;

public class JoinerKV {
    public final String leftColumn, rightColumn;

    public JoinerKV(final String mapping) {
        String[] arr = mapping.split("\\|");
        this.leftColumn = arr[1];
        this.rightColumn = arr[0];
    }
}
