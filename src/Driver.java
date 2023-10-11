import java.io.File;
import java.io.IOException;

public class Driver {

    public static void main(String[] args) throws IOException {
//        String dataFileName = "data.dat";
        String dataFileName = "xmxp2.dat";
        long seed = -1;

        if (args.length == 2) {
            seed = Integer.parseInt(args[0]);
            dataFileName = args[1];
        }
        if (args.length == 1) {
            dataFileName = args[0];
        }

        dataFileName = "data/" + dataFileName;
        File dataFile = new File(dataFileName);
        if (!dataFile.exists() || dataFile.isDirectory()) {
            DataGenerator dataGenerator = new DataGenerator();
            dataGenerator.generateData(dataFileName, -10, 0, 100);
        }

        Settings settings = new Settings(dataFileName, seed);
        TinyGP gp = new TinyGP(settings);
        gp.evolve();
    }
}
