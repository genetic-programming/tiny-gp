public class DataFileHeader {
    public int VAR_NUMBER;
    public int RANDOM_NUMBER;
    public double MIN_RANDOM;
    public double MAX_RANDOM;
    public int FITNESS_CASES;

    public DataFileHeader(int varNumber, int randomNumber, double minRandom, double maxRandom, int fitnessCases) {
        this.VAR_NUMBER = varNumber;
        this.RANDOM_NUMBER = randomNumber;
        this.MIN_RANDOM = minRandom;
        this.MAX_RANDOM = maxRandom;
        this.FITNESS_CASES = fitnessCases;
    }
}
