import java.io.File;
import java.io.IOException;

public class Driver {

    static final int QUANTITY = 100;

    public static void main(String[] args) throws IOException {
        String dataFileName = "data.dat";
//        String dataFileName = "xmxp2.dat";
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
        DataGenerator dataGenerator = new DataGenerator();
        String functionName = dataGenerator.generateData(dataFileName, -10, 10, QUANTITY, 1);

        Settings settings = new Settings(dataFileName, seed);
        TinyGP gp = new TinyGP(settings);
        gp.evolve();
        CsvResultConverter.convert(gp.getTargets(), gp.returnResults(), functionName,QUANTITY);

//        CsvResultConverter.convertFitness(gp.getBestFitness(), gp.averageFitness, functionName);

        System.exit(0);
    }

}

// f(x) = 5*x^3 - 2x^2 + 3x - 17 dziedzina: [-10, 10], [0,100], [-1, 1], [-1000, 1000]
// f(x) = sin(x) + cos(x) dziedzina: [-3.14, 3.14], [0,7], [0, 100], [-100, 100]
// f(x) = 2* ln(x+1) dziedzina: [0,4], [0, 9], [0,99], [0,999]
// f(x,y) = x + 2*y dziedzina: x i y [0, 1], [-10, 10], [0, 100], [-1000, 1000]
// f(x, y) = sin(x/2) + 2* cos(x) dziedzina x, y: [-3.14, 3.14], [0,7], [0, 100], [-100, 100]
// f(x,y) = x^2 + 3x*y - 7y + 1 dziedzina x,y: [-10, 10], [0,100], [-1, 1], [-1000, 1000]


