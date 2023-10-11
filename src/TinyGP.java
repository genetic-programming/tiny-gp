import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.StringTokenizer;

public class TinyGP {
    Settings settings;
    double[] fitness;
    char[][] pop;
    static double[] x;
    static double minRandom;
    static double maxRandom;
    static char[] program;
    static int pc;
    static int varNumber;
    static int fitnessCases;
    static int randomNumber;
    static double fBestPop = 0.0;
    static double fAvgPop = 0.0;
    static double avgLen;
    static double[][] targets;
    static char[] buffer;

    public TinyGP(Settings settings) {
        this.settings = settings;
        fitness = new double[settings.POP_SIZE];
        buffer = new char[settings.MAX_LEN];
        x = new double[settings.F_SET_START];

        if (settings.seed >= 0)
            settings.RANDOM.setSeed(settings.seed);

        setupFitness();

        for (int i = 0; i < settings.F_SET_START; i++)
            x[i] = (maxRandom - minRandom) * settings.RANDOM.nextDouble() + minRandom;
        pop = createRandomPop(settings.POP_SIZE, settings.DEPTH, fitness);
    }

    double run() { /* Interpreter */
        char primitive = program[pc++];
        if (primitive < settings.F_SET_START)
            return (x[primitive]);
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
        }
        return (0.0); // should never get here
    }

    int traverse(char[] buffer, int buffercount) {
        if (buffer[buffercount] < settings.F_SET_START)
            return (++buffercount);

        switch (buffer[buffercount]) {
            case Settings.ADD:
            case Settings.SUB:
            case Settings.MUL:
            case Settings.DIV:
                return (traverse(buffer, traverse(buffer, ++buffercount)));
            default:
                return (0);
        }
    }

    void setupFitness() {
        try {
            int i, j;
            String header;

            BufferedReader input = new BufferedReader(new FileReader(settings.fileName));
            header = input.readLine();
            StringTokenizer tokens = new StringTokenizer(header);
            varNumber = Integer.parseInt(tokens.nextToken().trim());
            randomNumber = Integer.parseInt(tokens.nextToken().trim());
            minRandom = Double.parseDouble(tokens.nextToken().trim());
            maxRandom = Double.parseDouble(tokens.nextToken().trim());
            fitnessCases = Integer.parseInt(tokens.nextToken().trim());
            targets = new double[fitnessCases][varNumber + 1];

            if (varNumber + randomNumber >= settings.F_SET_START)
                System.out.println("too many variables and constants");

            for (i = 0; i < fitnessCases; i++) {
                header = input.readLine();
                tokens = new StringTokenizer(header);
                for (j = 0; j <= varNumber; j++) {
                    targets[i][j] = Double.parseDouble(tokens.nextToken().trim());
                }
            }
            input.close();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Please provide a data file");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("ERROR: Incorrect data format");
            System.exit(0);
        }
    }

    double fitnessFunction(char[] program) {
        int i = 0, len;
        double result, fit = 0.0;

        len = traverse(program, 0);
        for (i = 0; i < fitnessCases; i++) {
            if (varNumber >= 0) System.arraycopy(targets[i], 0, x, 0, varNumber);
            TinyGP.program = program;
            pc = 0;
            result = run();
            fit += Math.abs(result - targets[i][varNumber]);
        }
        return (-fit);
    }

    int grow(char[] buffer, int pos, int max, int depth) {
        char prim = (char) settings.RANDOM.nextInt(2);
        int one_child;

        if (pos >= max)
            return (-1);

        if (pos == 0)
            prim = 1;

        if (prim == 0 || depth == 0) {
            prim = (char) settings.RANDOM.nextInt(varNumber + randomNumber);
            buffer[pos] = prim;
            return (pos + 1);
        } else {
            prim = (char) (settings.RANDOM.nextInt(settings.F_SET_END - settings.F_SET_START + 1) + settings.F_SET_START);
            switch (prim) {
                case Settings.ADD:
                case Settings.SUB:
                case Settings.MUL:
                case Settings.DIV:
                    buffer[pos] = prim;
                    one_child = grow(buffer, pos + 1, max, depth - 1);
                    if (one_child < 0)
                        return (-1);
                    return (grow(buffer, one_child, max, depth - 1));
            }
        }
        return (0); // should never get here
    }

    int printIndiv(char[] buffer, int buffercounter) {
        int a1 = 0, a2;
        if (buffer[buffercounter] < settings.F_SET_START) {
            if (buffer[buffercounter] < varNumber)
                System.out.print("X" + (buffer[buffercounter] + 1) + " ");
            else
                System.out.print(x[buffer[buffercounter]]);
            return (++buffercounter);
        }
        switch (buffer[buffercounter]) {
            case Settings.ADD:
                System.out.print("(");
                a1 = printIndiv(buffer, ++buffercounter);
                System.out.print(" + ");
                break;
            case Settings.SUB:
                System.out.print("(");
                a1 = printIndiv(buffer, ++buffercounter);
                System.out.print(" - ");
                break;
            case Settings.MUL:
                System.out.print("(");
                a1 = printIndiv(buffer, ++buffercounter);
                System.out.print(" * ");
                break;
            case Settings.DIV:
                System.out.print("(");
                a1 = printIndiv(buffer, ++buffercounter);
                System.out.print(" / ");
                break;
        }
        a2 = printIndiv(buffer, a1);
        System.out.print(")");
        return (a2);
    }

    char[] createRandomIndiv(int depth) {
        char[] ind;
        int len;

        len = grow(buffer, 0, settings.MAX_LEN, depth);

        while (len < 0)
            len = grow(buffer, 0, settings.MAX_LEN, depth);

        ind = new char[len];

        System.arraycopy(buffer, 0, ind, 0, len);
        return (ind);
    }

    char[][] createRandomPop(int n, int depth, double[] fitness) {
        char[][] pop = new char[n][];
        int i;

        for (i = 0; i < n; i++) {
            pop[i] = createRandomIndiv(depth);
            fitness[i] = fitnessFunction(pop[i]);
        }
        return (pop);
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
        avgLen = (double) node_count / settings.POP_SIZE;
        fAvgPop /= settings.POP_SIZE;
        System.out.print("Generation=" + gen + " Avg Fitness=" + (-fAvgPop) +
                " Best Fitness=" + (-fBestPop) + " Avg Size=" + avgLen +
                "\nBest Individual: ");
        printIndiv(pop[best], 0);
        System.out.print("\n");
        System.out.flush();
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
                    parentcopy[mutsite] = (char) settings.RANDOM.nextInt(varNumber + randomNumber);
                else
                    switch (parentcopy[mutsite]) {
                        case Settings.ADD:
                        case Settings.SUB:
                        case Settings.MUL:
                        case Settings.DIV:
                            parentcopy[mutsite] =
                                    (char) (settings.RANDOM.nextInt(settings.F_SET_END - settings.F_SET_START + 1)
                                            + settings.F_SET_START);
                    }
            }
        }
        return (parentcopy);
    }

    void printParams() {
        System.out.print(
            "-- TINY GP (Java version) --" +
            "\nSEED=" + settings.seed +
            "\nMAX_LEN=" + settings.MAX_LEN +
            "\nPOP_SIZE=" + settings.POP_SIZE +
            "\nDEPTH=" + settings.DEPTH +
            "\nCROSSOVER_PROB=" + settings.CROSSOVER_PROB +
            "\nP_MUT_PER_NODE=" + settings.P_MUT_PER_NODE +
            "\nMIN_RANDOM=" + minRandom +
            "\nMAX_RANDOM=" + maxRandom +
            "\nGENERATIONS=" + settings.GENERATIONS +
            "\nT_SIZE=" + settings.T_SIZE +
            "\n----------------------------------\n"
        );
    }

    void evolve() {
        int gen;
        int indivs, offspring, parent1, parent2, parent;
        double newfit;
        char[] newind;
        printParams();
        stats(fitness, pop, 0);
        for (gen = 1; gen < settings.GENERATIONS; gen++) {
            if (fBestPop > -1e-5) {
                System.out.print("PROBLEM SOLVED\n");
                System.exit(0);
            }
            for (indivs = 0; indivs < settings.POP_SIZE; indivs++) {
                if (settings.RANDOM.nextDouble() < settings.CROSSOVER_PROB) {
                    parent1 = tournament(fitness, settings.T_SIZE);
                    parent2 = tournament(fitness, settings.T_SIZE);
                    newind = crossover(pop[parent1], pop[parent2]);
                } else {
                    parent = tournament(fitness, settings.T_SIZE);
                    newind = mutation(pop[parent], settings.P_MUT_PER_NODE);
                }
                newfit = fitnessFunction(newind);
                offspring = negativeTournament(fitness, settings.T_SIZE);
                pop[offspring] = newind;
                fitness[offspring] = newfit;
            }
            stats(fitness, pop, gen);
        }
        System.out.print("PROBLEM *NOT* SOLVED\n");
        System.exit(1);
    }
}
