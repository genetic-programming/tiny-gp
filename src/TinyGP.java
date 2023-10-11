import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

public class TinyGP {
    double[] fitness;
    char[][] pop;
    static Random random = new Random();
    static final int ADD = 110;
    static final int SUB = 111;
    static final int MUL = 112;
    static final int DIV = 113;
    static final int F_SET_START = ADD;
    static final int F_SET_END = DIV;
    static double[] x = new double[F_SET_START];
    static double minRandom;
    static double maxRandom;
    static char[] program;
    static int PC;
    static int varNumber;
    static int fitnessCases;
    static int randomNumber;
    static double fBestPop = 0.0;
    static double fAvgPop = 0.0;
    static long seed;
    static double avgLen;
    static final int MAX_LEN = 10000;
    static final int POP_SIZE = 100000;
    static final int DEPTH = 5;
    static final int GENERATIONS = 100;
    static final int T_SIZE = 2;
    public static final double P_MUT_PER_NODE = 0.05;
    public static final double CROSSOVER_PROB = 0.9;
    static double[][] targets;

    double run() { /* Interpreter */
        char primitive = program[PC++];
        if (primitive < F_SET_START)
            return (x[primitive]);
        switch (primitive) {
            case ADD:
                return (run() + run());
            case SUB:
                return (run() - run());
            case MUL:
                return (run() * run());
            case DIV: {
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
        if (buffer[buffercount] < F_SET_START)
            return (++buffercount);

        return switch (buffer[buffercount]) {
            case ADD, SUB, MUL, DIV -> (traverse(buffer, traverse(buffer, ++buffercount)));
            default -> (0);
        };
    }

    void setupFitness(String fileName) {
        try {
            int i, j;
            String line;

            BufferedReader in =
                    new BufferedReader(
                            new
                                    FileReader(fileName));
            line = in.readLine();
            StringTokenizer tokens = new StringTokenizer(line);
            varNumber = Integer.parseInt(tokens.nextToken().trim());
            randomNumber = Integer.parseInt(tokens.nextToken().trim());
            minRandom = Double.parseDouble(tokens.nextToken().trim());
            maxRandom = Double.parseDouble(tokens.nextToken().trim());
            fitnessCases = Integer.parseInt(tokens.nextToken().trim());
            targets = new double[fitnessCases][varNumber + 1];
            if (varNumber + randomNumber >= F_SET_START)
                System.out.println("too many variables and constants");

            for (i = 0; i < fitnessCases; i++) {
                line = in.readLine();
                tokens = new StringTokenizer(line);
                for (j = 0; j <= varNumber; j++) {
                    targets[i][j] = Double.parseDouble(tokens.nextToken().trim());
                }
            }
            in.close();
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
            PC = 0;
            result = run();
            fit += Math.abs(result - targets[i][varNumber]);
        }
        return (-fit);
    }

    int grow(char[] buffer, int pos, int max, int depth) {
        char prim = (char) random.nextInt(2);
        int one_child;

        if (pos >= max)
            return (-1);

        if (pos == 0)
            prim = 1;

        if (prim == 0 || depth == 0) {
            prim = (char) random.nextInt(varNumber + randomNumber);
            buffer[pos] = prim;
            return (pos + 1);
        } else {
            prim = (char) (random.nextInt(F_SET_END - F_SET_START + 1) + F_SET_START);
            switch (prim) {
                case ADD:
                case SUB:
                case MUL:
                case DIV:
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
        if (buffer[buffercounter] < F_SET_START) {
            if (buffer[buffercounter] < varNumber)
                System.out.print("X" + (buffer[buffercounter] + 1) + " ");
            else
                System.out.print(x[buffer[buffercounter]]);
            return (++buffercounter);
        }
        switch (buffer[buffercounter]) {
            case ADD:
                System.out.print("(");
                a1 = printIndiv(buffer, ++buffercounter);
                System.out.print(" + ");
                break;
            case SUB:
                System.out.print("(");
                a1 = printIndiv(buffer, ++buffercounter);
                System.out.print(" - ");
                break;
            case MUL:
                System.out.print("(");
                a1 = printIndiv(buffer, ++buffercounter);
                System.out.print(" * ");
                break;
            case DIV:
                System.out.print("(");
                a1 = printIndiv(buffer, ++buffercounter);
                System.out.print(" / ");
                break;
        }
        a2 = printIndiv(buffer, a1);
        System.out.print(")");
        return (a2);
    }


    static char[] buffer = new char[MAX_LEN];

    char[] createRandomIndiv(int depth) {
        char[] ind;
        int len;

        len = grow(buffer, 0, MAX_LEN, depth);

        while (len < 0)
            len = grow(buffer, 0, MAX_LEN, depth);

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
        int i, best = random.nextInt(POP_SIZE);
        int node_count = 0;
        fBestPop = fitness[best];
        fAvgPop = 0.0;

        for (i = 0; i < POP_SIZE; i++) {
            node_count += traverse(pop[i], 0);
            fAvgPop += fitness[i];
            if (fitness[i] > fBestPop) {
                best = i;
                fBestPop = fitness[i];
            }
        }
        avgLen = (double) node_count / POP_SIZE;
        fAvgPop /= POP_SIZE;
        System.out.print("Generation=" + gen + " Avg Fitness=" + (-fAvgPop) +
                " Best Fitness=" + (-fBestPop) + " Avg Size=" + avgLen +
                "\nBest Individual: ");
        printIndiv(pop[best], 0);
        System.out.print("\n");
        System.out.flush();
    }

    int tournament(double[] fitness, int tsize) {
        int best = random.nextInt(POP_SIZE), i, competitor;
        double fbest = -1.0e34;

        for (i = 0; i < tsize; i++) {
            competitor = random.nextInt(POP_SIZE);
            if (fitness[competitor] > fbest) {
                fbest = fitness[competitor];
                best = competitor;
            }
        }
        return (best);
    }

    int negativeTournament(double[] fitness, int tsize) {
        int worst = random.nextInt(POP_SIZE), i, competitor;
        double fworst = 1e34;

        for (i = 0; i < tsize; i++) {
            competitor = random.nextInt(POP_SIZE);
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
        int lenoff;

        xo1start = random.nextInt(len1);
        xo1end = traverse(parent1, xo1start);

        xo2start = random.nextInt(len2);
        xo2end = traverse(parent2, xo2start);

        lenoff = xo1start + (xo2end - xo2start) + (len1 - xo1end);

        offspring = new char[lenoff];

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
            if (random.nextDouble() < pmut) {
                mutsite = i;
                if (parentcopy[mutsite] < F_SET_START)
                    parentcopy[mutsite] = (char) random.nextInt(varNumber + randomNumber);
                else
                    switch (parentcopy[mutsite]) {
                        case ADD:
                        case SUB:
                        case MUL:
                        case DIV:
                            parentcopy[mutsite] =
                                    (char) (random.nextInt(F_SET_END - F_SET_START + 1)
                                            + F_SET_START);
                    }
            }
        }
        return (parentcopy);
    }

    void printParams() {
        System.out.print("-- TINY GP (Java version) --\n");
        System.out.print("SEED=" + seed + "\nMAX_LEN=" + MAX_LEN +
                "\nPOPSIZE=" + POP_SIZE + "\nDEPTH=" + DEPTH +
                "\nCROSSOVER_PROB=" + CROSSOVER_PROB +
                "\nPMUT_PER_NODE=" + P_MUT_PER_NODE +
                "\nMIN_RANDOM=" + minRandom +
                "\nMAX_RANDOM=" + maxRandom +
                "\nGENERATIONS=" + GENERATIONS +
                "\nTSIZE=" + T_SIZE +
                "\n----------------------------------\n");
    }

    public TinyGP(String fileName, long s) {
        fitness = new double[POP_SIZE];
        seed = s;
        if (seed >= 0)
            random.setSeed(seed);
        setupFitness(fileName);
        for (int i = 0; i < F_SET_START; i++)
            x[i] = (maxRandom - minRandom) * random.nextDouble() + minRandom;
        pop = createRandomPop(POP_SIZE, DEPTH, fitness);
    }

    void evolve() {
        int gen = 0, indivs, offspring, parent1, parent2, parent;
        double newfit;
        char[] newind;
        printParams();
        stats(fitness, pop, 0);
        for (gen = 1; gen < GENERATIONS; gen++) {
            if (fBestPop > -1e-5) {
                System.out.print("PROBLEM SOLVED\n");
                System.exit(0);
            }
            for (indivs = 0; indivs < POP_SIZE; indivs++) {
                if (random.nextDouble() < CROSSOVER_PROB) {
                    parent1 = tournament(fitness, T_SIZE);
                    parent2 = tournament(fitness, T_SIZE);
                    newind = crossover(pop[parent1], pop[parent2]);
                } else {
                    parent = tournament(fitness, T_SIZE);
                    newind = mutation(pop[parent], P_MUT_PER_NODE);
                }
                newfit = fitnessFunction(newind);
                offspring = negativeTournament(fitness, T_SIZE);
                pop[offspring] = newind;
                fitness[offspring] = newfit;
            }
            stats(fitness, pop, gen);
        }
        System.out.print("PROBLEM *NOT* SOLVED\n");
        System.exit(1);
    }

    public static void main(String[] args) throws IOException {
        String fileName = "data.dat";

        DataGenerator dataGenerator = new DataGenerator();
        dataGenerator.generateData(fileName, -10, 0, 100);

        long s = -1;

        if (args.length == 2) {
            s = Integer.parseInt(args[0]);
            fileName = args[1];
        }
        if (args.length == 1) {
            fileName = args[0];
        }

        TinyGP gp = new TinyGP(fileName, s);
        gp.evolve();
    }
};