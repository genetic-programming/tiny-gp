import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TinyGP {
    Settings settings;
    DataFileHeader dataFileHeader;
    double[] fitness;
    char[][] population;
    double[] xAxis;
    char[] program;
    int pc;
    double fBestPop = 0.0;
    double fAvgPop = 0.0;
    double averageLen;
    double[][] targets;
    char[] buffer;
    char[] bestProgram;
    List<Double> bestFitness = new ArrayList<>();
    List<Double> averageFitness = new ArrayList<>();

    public TinyGP(Settings settings) {
        this.settings = settings;
        fitness = new double[settings.POP_SIZE];
        buffer = new char[settings.MAX_LEN];
        xAxis = new double[settings.F_SET_START];

        if (settings.seed >= 0)
            settings.RANDOM.setSeed(settings.seed);

        setupFitness();

        for (int i = 0; i < settings.F_SET_START; i++)
            xAxis[i] = (dataFileHeader.MAX_RANDOM - dataFileHeader.MIN_RANDOM) * settings.RANDOM.nextDouble() + dataFileHeader.MIN_RANDOM;
        population = createRandomPopulation(settings.POP_SIZE, settings.DEPTH, fitness);
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

    char[][] createRandomPopulation(int n, int depth, double[] fitness) {
        char[][] pop = new char[n][];
        int i;

        for (i = 0; i < n; i++) {
            pop[i] = createRandomIndividual(depth);
            fitness[i] = fitnessFunction(pop[i]);
        }
        return (pop);
    }

    char[] createRandomIndividual(int depth) {
        char[] ind;
        int len;

        len = grow(buffer, 0, settings.MAX_LEN, depth);

        while (len < 0)
            len = grow(buffer, 0, settings.MAX_LEN, depth);

        ind = new char[len];

        System.arraycopy(buffer, 0, ind, 0, len);
        return (ind);
    }

    int grow(char[] buffer, int pos, int max, int depth) {
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

    void evolve() {
        int gen;
        int indivs, offspring, parent1, parent2, parent;
        double newfit;
        char[] newind;
        printParams();
        stats(fitness, population, 0);
        for (gen = 1; gen < settings.GENERATIONS; gen++) {
            if (fBestPop > settings.ACCEPTABLE_ERROR) {
                System.out.print("PROBLEM SOLVED\n");
                return;
            }
            for (indivs = 0; indivs < settings.POP_SIZE; indivs++) {
                if (settings.RANDOM.nextDouble() < settings.CROSSOVER_PROBABILITY) {
                    parent1 = tournament(fitness, settings.T_SIZE);
                    parent2 = tournament(fitness, settings.T_SIZE);
                    newind = crossover(population[parent1], population[parent2]);
                } else {
                    parent = tournament(fitness, settings.T_SIZE);
                    newind = mutation(population[parent], settings.P_MUT_PER_NODE);
                }
                newfit = fitnessFunction(newind);
                offspring = negativeTournament(fitness, settings.T_SIZE);
                population[offspring] = newind;
                fitness[offspring] = newfit;
            }
            stats(fitness, population, gen);
        }
        System.out.print("PROBLEM *NOT* SOLVED\n");
        System.exit(1);
    }

    void stats(double[] fitness, char[][] pop, int gen) {
        int i, best = settings.RANDOM.nextInt(settings.POP_SIZE);
        int node_count = 0;
        fBestPop = fitness[best];
        fAvgPop = 0.0;

        for (i = 0; i < settings.POP_SIZE; i++) {
            node_count += traverse(pop[i], 0);
            fAvgPop += fitness[i];
            if (fitness[i] > fBestPop) {
                best = i;
                fBestPop = fitness[i];
            }
        }
        averageLen = (double) node_count / settings.POP_SIZE;
        fAvgPop /= settings.POP_SIZE;

        bestFitness.add(-fBestPop);
        averageFitness.add(-fAvgPop);

        System.out.print("Generation=" + gen + " Avg Fitness=" + (-fAvgPop) +
                " Best Fitness=" + (-fBestPop) + " Avg Size=" + averageLen +
                "\nBest Individual: ");
        StringBuffer output = new StringBuffer();
        printIndividual(pop[best], 0, output);
        String optimizedOutput = ExpressionOptimizer.optimizeExpression(output.toString());
        System.out.println("\n\nOriginal: ");
        System.out.println(output);
        System.out.println("\nOptimized:");
        System.out.println(optimizedOutput);
        bestProgram = pop[best];
        System.out.print("\n");
        System.out.flush();
    }

    int traverse(char[] buffer, int buffercount) {
        if (buffer[buffercount] < settings.F_SET_START)
            return (++buffercount);

        switch (buffer[buffercount]) {
            case Settings.ADD:
            case Settings.SUB:
            case Settings.MUL:
            case Settings.DIV:
            case Settings.SIN:
            case Settings.COS:
                return (traverse(buffer, traverse(buffer, ++buffercount)));
            default:
                return (0);
        }
    }

    int printIndividual(char[] buffer, int buffercounter, StringBuffer output) {

        int a1 = 0, a2;
        if (buffer[buffercounter] < settings.F_SET_START) {
            if (buffer[buffercounter] < dataFileHeader.VARIABLES_NUMBER)
//                System.out.print(" X" + (buffer[buffercounter] + 1) + " ");
                output.append(" X" + (buffer[buffercounter] + 1) + " ");
            else
//                System.out.print(xAxis[buffer[buffercounter]]);
                output.append(xAxis[buffer[buffercounter]]);
            return (++buffercounter);
        }
        switch (buffer[buffercounter]) {
            case Settings.ADD:
//                System.out.print("(");
                output.append("(");
                a1 = printIndividual(buffer, ++buffercounter, output);
//                System.out.print(" + ");
                output.append(" + ");
                break;
            case Settings.SUB:
//                System.out.print("(");
                output.append("(");
                a1 = printIndividual(buffer, ++buffercounter, output);
//                System.out.print(" - ");
                output.append(" - ");
                break;
            case Settings.MUL:
//                System.out.print("(");
                output.append("(");
                a1 = printIndividual(buffer, ++buffercounter, output);
//                System.out.print(" * ");
                output.append(" * ");
                break;
            case Settings.DIV:
//                System.out.print("(");
                output.append("(");
                a1 = printIndividual(buffer, ++buffercounter, output);
//                System.out.print(" / ");
                output.append(" / ");
                break;
            case Settings.SIN:
//                System.out.print("sin(");
                output.append("sin(");
                a1 = printIndividual(buffer, ++buffercounter, output);
//                System.out.println(")");
                output.append(")");
                return (a1);
            case Settings.COS:
//                System.out.print("cos(");
                output.append("cos(");
                a1 = printIndividual(buffer, ++buffercounter, output);
//                System.out.println(")");
                output.append(")");
                return (a1);
        }
        a2 = printIndividual(buffer, a1, output);
//        System.out.print(")");
        output.append(")");
        return (a2);
    }

    void printParams() {
        System.out.print(
                "-- TINY GP (Java version) --" +
                        "\nSEED=" + settings.seed +
                        "\nMAX_LEN=" + settings.MAX_LEN +
                        "\nPOP_SIZE=" + settings.POP_SIZE +
                        "\nDEPTH=" + settings.DEPTH +
                        "\nCROSSOVER_PROB=" + settings.CROSSOVER_PROBABILITY +
                        "\nP_MUT_PER_NODE=" + settings.P_MUT_PER_NODE +
                        "\nMIN_RANDOM=" + dataFileHeader.MIN_RANDOM +
                        "\nMAX_RANDOM=" + dataFileHeader.MAX_RANDOM +
                        "\nGENERATIONS=" + settings.GENERATIONS +
                        "\nT_SIZE=" + settings.T_SIZE +
                        "\n----------------------------------\n"
        );
    }

    int tournament(double[] fitness, int tsize) {
        int best = settings.RANDOM.nextInt(settings.POP_SIZE), i, competitor;
        double fbest = -1.0e34;

        for (i = 0; i < tsize; i++) {
            competitor = settings.RANDOM.nextInt(settings.POP_SIZE);
            if (fitness[competitor] > fbest) {
                fbest = fitness[competitor];
                best = competitor;
            }
        }
        return (best);
    }

    char[] crossover(char[] parent1, char[] parent2) {
        int xo1start, xo1end, xo2start, xo2end;
        char[] offspring;
        int len1 = traverse(parent1, 0);
        int len2 = traverse(parent2, 0);
        int lenOff;

        xo1start = settings.RANDOM.nextInt(len1);
        xo1end = traverse(parent1, xo1start);

        xo2start = settings.RANDOM.nextInt(len2);
        xo2end = traverse(parent2, xo2start);

        lenOff = xo1start + (xo2end - xo2start) + (len1 - xo1end);

        offspring = new char[lenOff];

        System.arraycopy(parent1, 0, offspring, 0, xo1start);
        System.arraycopy(parent2, xo2start, offspring, xo1start,
                (xo2end - xo2start));
        System.arraycopy(parent1, xo1end, offspring,
                xo1start + (xo2end - xo2start),
                (len1 - xo1end));

        return (offspring);
    }

    char[] mutation(char[] parent, double pmut) {
        int len = traverse(parent, 0), i;
        int mutsite;
        char[] parentcopy = new char[len];

        System.arraycopy(parent, 0, parentcopy, 0, len);
        for (i = 0; i < len; i++) {
            if (settings.RANDOM.nextDouble() < pmut) {
                mutsite = i;
                if (parentcopy[mutsite] < settings.F_SET_START)
                    parentcopy[mutsite] = (char) settings.RANDOM.nextInt(dataFileHeader.VARIABLES_NUMBER + dataFileHeader.RANDOM_NUMBER);
                else
                    switch (parentcopy[mutsite]) {
                        case Settings.ADD:
                        case Settings.SUB:
                        case Settings.MUL:
                        case Settings.DIV:
                        case Settings.SIN:
                        case Settings.COS:
                            parentcopy[mutsite] =
                                    (char) (settings.RANDOM.nextInt(settings.F_SET_END - settings.F_SET_START + 1)
                                            + settings.F_SET_START);
                    }
            }
        }
        return (parentcopy);
    }

    double fitnessFunction(char[] program_) {
        int i = 0, len;
        double result, fit = 0.0;

        len = traverse(program_, 0);
        for (i = 0; i < dataFileHeader.FITNESS_CASES_NUMBER; i++) {
            if (dataFileHeader.VARIABLES_NUMBER >= 0) System.arraycopy(targets[i], 0, xAxis, 0, dataFileHeader.VARIABLES_NUMBER);
            program = program_;
            pc = 0;
            result = run();
            fit += Math.abs(result - targets[i][dataFileHeader.VARIABLES_NUMBER]);
        }
        return (-fit);
    }

    double run() { /* Interpreter */
        char primitive = program[pc++];
        if (primitive < settings.F_SET_START)
            return (xAxis[primitive]);
        switch (primitive) {
            case Settings.ADD:
                return (run() + run());
            case Settings.SUB:
                return (run() - run());
            case Settings.MUL:
                return (run() * run());
            case Settings.DIV: {
                double num = run(), den = run();
                if (Math.abs(den) <= 0.001)
                    return (num);
                else
                    return (num / den);
                }
            case Settings.SIN:
                return (Math.sin(run()));
            case Settings.COS:
                return (Math.cos(run()));
        }
        return (0.0); // should never get here
    }

    int negativeTournament(double[] fitness, int tsize) {
        int worst = settings.RANDOM.nextInt(settings.POP_SIZE), i, competitor;
        double fworst = 1e34;

        for (i = 0; i < tsize; i++) {
            competitor = settings.RANDOM.nextInt(settings.POP_SIZE);
            if (fitness[competitor] < fworst) {
                fworst = fitness[competitor];
                worst = competitor;
            }
        }
        return (worst);
    }

    public double[][] getTargets() {
        return targets;
    }

    public List<Double> getBestFitness() {
        return bestFitness;
    }

    public List<Double> getAverageFitness() {
        return averageFitness;
    }

    public List<Double> returnResults() {
        List<Double> results = new ArrayList<>();
        double result = 0.0;
        for (int i = 0; i < dataFileHeader.FITNESS_CASES_NUMBER; i++) {
            if (dataFileHeader.VARIABLES_NUMBER >= 0)
                System.arraycopy(targets[i], 0, xAxis, 0, dataFileHeader.VARIABLES_NUMBER);
            program = bestProgram;
            pc = 0;
            result = run();
            results.add(result);
        }
        return results;
    }

}
