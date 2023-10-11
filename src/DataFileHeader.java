public class DataFileHeader {
    /**
     * Data class that represents the header of the data file.
     * It basically means the first line of the data file with 5 numbers.
     */

    public int VARIABLES_NUMBER;
    public int RANDOM_NUMBER;
    public double MIN_RANDOM;
    public double MAX_RANDOM;
    public int FITNESS_CASES_NUMBER;

    public DataFileHeader(int varNumber, int randomNumber, double minRandom, double maxRandom, int fitnessCases) {
        this.VARIABLES_NUMBER = varNumber;
        this.RANDOM_NUMBER = randomNumber;
        this.MIN_RANDOM = minRandom;
        this.MAX_RANDOM = maxRandom;
        this.FITNESS_CASES_NUMBER = fitnessCases;
    }
}
