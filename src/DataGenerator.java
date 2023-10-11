import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataGenerator {
    public void generateData(
            String fileName,
            double start,
            double end,
            int quantity
    ) throws IOException {
        DataOutputStream output = new DataOutputStream(Files.newOutputStream(Paths.get(fileName)));
        double argument = start;
        for (int i = 0; i < quantity; i++) {
            output.writeDouble(argument);
            output.writeBytes(" ");
            output.writeDouble(this.function(argument));
            output.writeBytes("\n");
            argument = start + ((end - start) / quantity);
        }
    }

    private double function(double x) {
        return 5 * Math.pow(x, 3) - 2 * Math.pow(x, 2) + 3 * x - 17;
    }
}