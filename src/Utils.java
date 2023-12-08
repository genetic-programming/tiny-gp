public class Utils {

    private final Settings settings;
    private final DataFileHeader dataFileHeader;
    private final Target target;
    private final Population population;

    public Utils(Settings settings, DataFileHeader dataFileHeader, Target target, Population population) {
        this.settings = settings;
        this.dataFileHeader = dataFileHeader;
        this.target = target;
        this.population = population;
    }
    public double fitnessFunction(char[] program_) {
        double result, fit = 0.0;
        double[][] targets = target.getTargets();

        for (int i = 0; i < dataFileHeader.FITNESS_CASES_NUMBER; i++) {
            if (dataFileHeader.VARIABLES_NUMBER >= 0)
                System.arraycopy(targets[i], 0, population.getxAxis(), 0, dataFileHeader.VARIABLES_NUMBER);
            Interpreter interpreter = new Interpreter(program_, settings, population.getxAxis());
            result = interpreter.run();
            fit += Math.abs(result - targets[i][dataFileHeader.VARIABLES_NUMBER]);
        }
        return (-fit);
    }

    public int printIndividual(char[] buffer, int buffercounter, StringBuffer output) {

        int a1 = 0, a2;
        if (buffer[buffercounter] < settings.F_SET_START) {
            if (buffer[buffercounter] < dataFileHeader.VARIABLES_NUMBER)
                output.append(" X" + (buffer[buffercounter] + 1) + " ");
            else
                output.append(population.getxAxis()[buffer[buffercounter]]);
            return (++buffercounter);
        }
        switch (buffer[buffercounter]) {
            case Settings.ADD:
                output.append("(");
                a1 = printIndividual(buffer, ++buffercounter, output);
                output.append(" + ");
                break;
            case Settings.SUB:
                output.append("(");
                a1 = printIndividual(buffer, ++buffercounter, output);
                output.append(" - ");
                break;
            case Settings.MUL:
                output.append("(");
                a1 = printIndividual(buffer, ++buffercounter, output);
                output.append(" * ");
                break;
            case Settings.DIV:
                output.append("(");
                a1 = printIndividual(buffer, ++buffercounter, output);
                output.append(" / ");
                break;
            case Settings.SIN:
                output.append("sin(");
                a1 = printIndividual(buffer, ++buffercounter, output);
                output.append(")");
                return (a1);
            case Settings.COS:
                output.append("cos(");
                a1 = printIndividual(buffer, ++buffercounter, output);
                output.append(")");
                return (a1);
        }
        a2 = printIndividual(buffer, a1, output);
        output.append(")");
        return (a2);
    }

    public void printParams() {
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

    public int traverse(char[] buffer, int buffercount) {
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


}
