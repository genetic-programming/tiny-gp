import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataGenerator {
    public void generateData(
        String fileName,
        double start,
        double end,
        int quantity
    ) throws IOException {
        File Candidatefile = new File(fileName);
        BufferedWriter output = new BufferedWriter(new FileWriter(Candidatefile));
        double argument = start;
        for (int i = 0; i < quantity; i++) {
            output.write(argument + " " + this.function(argument));
            output.newLine();
            argument = start + ((end - start) / quantity);
        }
    }

    private double function(double x) {
        return 5 * Math.pow(x, 3) - 2 * Math.pow(x, 2) + 3 * x - 17;
    }
}