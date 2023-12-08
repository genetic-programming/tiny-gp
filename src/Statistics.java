import java.util.ArrayList;
import java.util.List;

public class Statistics {
    private final Settings settings;
    Utils utils;

    private char[] bestProgram;
    private final List<Double> bestFitness = new ArrayList<>();
    private final List<Double> averageFitness = new ArrayList<>();

    public Statistics(Settings settings, Utils utils) {
        this.settings = settings;
        this.utils = utils;
    }


    public double calculateStats(double[] fitness, char[][] pop, int gen) {
        int i, best = settings.RANDOM.nextInt(settings.POP_SIZE);
        int node_count = 0;
        double fBestPop = fitness[best];
        double fAvgPop = 0.0;

        for (i = 0; i < settings.POP_SIZE; i++) {
            node_count += utils.traverse(pop[i], 0);
            fAvgPop += fitness[i];
            if (fitness[i] > fBestPop) {
                best = i;
                fBestPop = fitness[i];
            }
        }
        double averageLen = (double) node_count / settings.POP_SIZE;
        fAvgPop /= settings.POP_SIZE;

        bestFitness.add(-fBestPop);
        averageFitness.add(-fAvgPop);

        System.out.print("Generation=" + gen + " Avg Fitness=" + (-fAvgPop) +
                " Best Fitness=" + (-fBestPop) + " Avg Size=" + averageLen +
                "\nBest Individual: ");
        StringBuffer output = new StringBuffer();
        utils.printIndividual(pop[best], 0, output);
        String optimizedOutput = ExpressionOptimizer.optimizeExpression(output.toString());
        System.out.println("\n\nOriginal: ");
        System.out.println(output);
        System.out.println("\nOptimized:");
        System.out.println(optimizedOutput);
        bestProgram = pop[best];
        System.out.print("\n");
        System.out.flush();
        return fBestPop;
    }

    public List<Double> getBestFitness() {
        return bestFitness;
    }
    public List<Double> getAverageFitness() {
        return averageFitness;
    }

    public char[] getBestProgram() {
        return bestProgram;
    }
}
