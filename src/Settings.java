import java.util.Random;

public class Settings {
    public String fileName;
    public long seed;
    public final Random RANDOM = new Random();
    public static final int ADD = 110;
    public static final int SUB = 111;
    public static final int MUL = 112;
    public static final int DIV = 113;
    public final int F_SET_START = ADD;
    public final int F_SET_END = DIV;
    public final int MAX_LEN = 10000;
    public final int POP_SIZE = 100000;
    public final int DEPTH = 5;
    public final int GENERATIONS = 100;
    public final int T_SIZE = 2;
    public final double P_MUT_PER_NODE = 0.05;
    public final double CROSSOVER_PROB = 0.9;

    public Settings(String fileName, long seed) {
        this.fileName = fileName;
        this.seed = seed;
    }
}
