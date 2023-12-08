import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Target {
    private double[][] targets;
    private final Settings settings;
    private DataFileHeader dataFileHeader;

    public Target(Settings settings) {
        this.settings = settings;
        setupFitness();
    }

    void setupFitness() {
        try {
            int i, j;
            String header;

            BufferedReader input = new BufferedReader(new FileReader(settings.fileName));
            header = input.readLine();
            StringTokenizer tokens = new StringTokenizer(header);

            dataFileHeader = new DataFileHeader(
                    Integer.parseInt(tokens.nextToken().trim()),
                    Integer.parseInt(tokens.nextToken().trim()),
                    Double.parseDouble(tokens.nextToken().trim()),
                    Double.parseDouble(tokens.nextToken().trim()),
                    Integer.parseInt(tokens.nextToken().trim())
            );
            targets = new double[dataFileHeader.FITNESS_CASES_NUMBER][dataFileHeader.VARIABLES_NUMBER + 1];

            if (dataFileHeader.VARIABLES_NUMBER + dataFileHeader.RANDOM_NUMBER >= settings.F_SET_START)
                System.out.println("too many variables and constants");

            for (i = 0; i < dataFileHeader.FITNESS_CASES_NUMBER; i++) {
                header = input.readLine();
                tokens = new StringTokenizer(header);
                for (j = 0; j <= dataFileHeader.VARIABLES_NUMBER; j++) {
                    targets[i][j] = Double.parseDouble(tokens.nextToken().trim());
                }
            }
            input.close();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Please provide a data file");
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public double[][] getTargets() {
        return targets;
    }

    public DataFileHeader getDataFileHeader() {
        return dataFileHeader;
    }
}
