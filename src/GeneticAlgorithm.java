import java.util.ArrayList;
import java.util.List;

public class GeneticAlgorithm {
    private final Settings settings;
    private final DataFileHeader dataFileHeader;
    private final Population population;
    private final Target target;
    private final Utils utils;
    Statistics stats;

    public GeneticAlgorithm(Settings settings) {
        this.target = new Target(settings);
        this.settings = settings;
        this.dataFileHeader = target.getDataFileHeader();
        if (settings.seed >= 0)
            settings.RANDOM.setSeed(settings.seed);
        this.population = new Population(settings, dataFileHeader, target);
        this.utils = new Utils(settings, dataFileHeader, target, population);
        this.stats = new Statistics(settings, utils);
    }


    public int evolve() {
        int gen;
        int indivs, offspring, parent1, parent2, parent;
        double newfit;
        char[] newind;
        utils.printParams();
        char[][] population = this.population.getPopulation();
        double [] fitness = this.population.getFitness();
        double fBestPop =  stats.calculateStats(fitness, population, 0);
        for (gen = 1; gen < settings.GENERATIONS; gen++) {
            if (fBestPop > settings.ACCEPTABLE_ERROR) {
                System.out.print("PROBLEM SOLVED\n");
                return 0;
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
                newfit = utils.fitnessFunction(newind);
                offspring = negativeTournament(fitness, settings.T_SIZE);
                population[offspring] = newind;
                fitness[offspring] = newfit;
            }
            stats.calculateStats(fitness, population, gen);
        }
        System.out.print("PROBLEM *NOT* SOLVED\n");
        return 1;
    }

    private int tournament(double[] fitness, int tsize) {
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

    private char[] crossover(char[] parent1, char[] parent2) {
        int xo1start, xo1end, xo2start, xo2end;
        char[] offspring;
        int len1 = utils.traverse(parent1, 0);
        int len2 = utils.traverse(parent2, 0);
        int lenOff;

        xo1start = settings.RANDOM.nextInt(len1);
        xo1end = utils.traverse(parent1, xo1start);

        xo2start = settings.RANDOM.nextInt(len2);
        xo2end = utils.traverse(parent2, xo2start);

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

    private char[] mutation(char[] parent, double pmut) {
        int len = utils.traverse(parent, 0), i;
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

    private int negativeTournament(double[] fitness, int tsize) {
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

    public List<Double> returnResults() {
        char[] bestProgram = stats.getBestProgram();
        double[] xAxis = population.getxAxis();
        double [][] targets = target.getTargets();
        List<Double> results = new ArrayList<>();
        double result = 0.0;
        for (int i = 0; i < dataFileHeader.FITNESS_CASES_NUMBER; i++) {
            if (dataFileHeader.VARIABLES_NUMBER >= 0)
                System.arraycopy(targets[i], 0, xAxis, 0, dataFileHeader.VARIABLES_NUMBER);
            Interpreter interpreter = new Interpreter(bestProgram, settings, xAxis);
            result = interpreter.run();
            results.add(result);
        }
        return results;
    }


    public Target getTarget() {
        return target;
    }

    public Statistics getStats() {
        return stats;
    }
}
