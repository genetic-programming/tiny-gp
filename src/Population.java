public class Population {
   private final char[][] population;
   private final double[] xAxis;
   private final Settings settings;
   private final DataFileHeader dataFileHeader;
   private final char[] buffer;
   private final double[] fitness;
   private final Target target;

    public Population(Settings settings, DataFileHeader dataFileHeader, Target target) {
         this.settings = settings;
         this.dataFileHeader = dataFileHeader;
         this.target = target;
         buffer = new char[settings.MAX_LEN];
         fitness = new double[settings.POP_SIZE];
         xAxis = new double[settings.F_SET_START];

        for (int i = 0; i < settings.F_SET_START; i++)
            xAxis[i] = (dataFileHeader.MAX_RANDOM - dataFileHeader.MIN_RANDOM) * settings.RANDOM.nextDouble() + dataFileHeader.MIN_RANDOM;
         population = createRandomPopulation(settings.POP_SIZE, settings.DEPTH, fitness);
    }


   private char[] createRandomIndividual(int depth) {
       char[] ind;
       int len;

       len = grow(buffer, 0, settings.MAX_LEN, depth);

       while (len < 0)
           len = grow(buffer, 0, settings.MAX_LEN, depth);

       ind = new char[len];

       System.arraycopy(buffer, 0, ind, 0, len);
       return (ind);
   }


    private char[][] createRandomPopulation(int n, int depth, double[] fitness) {
        char[][] pop = new char[n][];
        int i;
        Utils utils = new Utils(settings, dataFileHeader, target, this);

        for (i = 0; i < n; i++) {
            pop[i] = createRandomIndividual(depth);
            fitness[i] = utils.fitnessFunction(pop[i]);
        }
        return (pop);
    }

    private int grow(char[] buffer, int pos, int max, int depth) {
        char prim = (char) settings.RANDOM.nextInt(2);
        int one_child;

        if (pos >= max)
            return (-1);

        if (pos == 0)
            prim = 1;

        if (prim == 0 || depth == 0) {
            prim = (char) settings.RANDOM.nextInt(dataFileHeader.VARIABLES_NUMBER + dataFileHeader.RANDOM_NUMBER);
            buffer[pos] = prim;
            return (pos + 1);
        } else {
            prim = (char) (settings.RANDOM.nextInt(settings.F_SET_END - settings.F_SET_START + 1) + settings.F_SET_START);
            switch (prim) {
                case Settings.ADD:
                case Settings.SUB:
                case Settings.MUL:
                case Settings.DIV:
                case Settings.SIN:
                case Settings.COS:
                    buffer[pos] = prim;
                    one_child = grow(buffer, pos + 1, max, depth - 1);
                    if (one_child < 0)
                        return (-1);
                    return (grow(buffer, one_child, max, depth - 1));
            }
        }
        return (0); // should never get here
    }
    public char[][] getPopulation() {
        return population;
    }

    public double[] getFitness() {
        return fitness;
    }

    public double[] getxAxis() {
        return xAxis;
    }
}
