import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvResultConverter {
    public static void convert(double[][] targets,
                               List<Double> results,
                               String functionName,
                               int quantity
    ) throws IOException {

        String fileName = functionName + ".csv";
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("results", fileName)));

        if (targets[0].length == 2) {

            writer.write("x,");
            writer.write("expected result,");
            writer.write("program result\n");

            for (int i = 0; i < targets.length; i++) {
                for (int j = 0; j < targets[i].length; j++) {
                    writer.write(targets[i][j] + ",");
                }
                writer.write(results.get(i) + "\n");
            }
        } else {

            List<Double> arguments = new ArrayList<>();
            for (int i = 0; i < quantity; i += (int) Math.sqrt(quantity)) {
                arguments.add(targets[i][0]);
            }
            writer.write("x/y");
            for (double y : arguments) {
                writer.write("," + y);
            }
            writer.write("\n");
            int row = 0;
            for (double x : arguments) {
                writer.write(String.valueOf(x));
                for (int i = 0; i < arguments.size(); i++) {
                    writer.write("," + targets[row][2]);
                    row++;
                }
                writer.write("\n");
            }

            writer.write(" ");
            for (int i = 0; i < arguments.size(); i++) {
                writer.write(",");
            }

            writer.write("\n");

            writer.write("x/y");
            for (double y : arguments) {
                writer.write("," + y);
            }
            writer.write("\n");

            row = 0;
            for (double x : arguments) {
                writer.write(String.valueOf(x));
                for (int i = 0; i < arguments.size(); i++) {
                    writer.write("," + results.get(row));
                    row++;
                }
                writer.write("\n");
            }
        }
        writer.close();
    }
}
