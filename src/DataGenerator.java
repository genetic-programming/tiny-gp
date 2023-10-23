import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataGenerator {
    private String functionName;

    public String generateData(
            String fileName,
            double start,
            double end,
            int quantity,
            int numberOfParameters
    ) throws IOException {
        File Candidatefile = new File(fileName);
        BufferedWriter output = new BufferedWriter(new FileWriter(Candidatefile));

        if(numberOfParameters == 1){
            output.write("1 100 -5 5 " + quantity);
            output.newLine();

            double argument = start;
            for (int i = 0; i < quantity; i++) {
                output.write(argument + " " + this.fun2(argument));
                output.newLine();
                argument += ((end - start) / quantity);
            }
        } else if (numberOfParameters == 2) {
            output.write("2 100 -5 5 " + quantity);
            output.newLine();
            double arg1 = start;
            double arg2 = start;
            quantity = (int) Math.sqrt(quantity);
            for (int i = 0; i < quantity; i++) {
                for (int j = 0; j < quantity; j++) {
                    output.write(arg1 + " " + arg2 + " " + this.fun6(arg1, arg2));
                    output.newLine();
                    arg2 += ((end - start) / quantity);
                }
                arg1 += ((end - start) / quantity);
                arg2 = start;
            }
        }




        output.close();

        return String.format("%s_[%.2f_%.2f]", functionName, start, end);
    }


    private double fun1(double x) {
        this.functionName = "fun1";
        return 5 * Math.pow(x, 3) - 2 * Math.pow(x, 2) + 3 * x - 17;
    }

    private double fun2(double x) {
        this.functionName = "fun2";
        return Math.sin(x) + Math.cos(x);
    }

    private double fun3(double x) {
        this.functionName = "fun3";
        return 2 * Math.log(x + 1);
    }

    private double fun4(double x, double y) {
        this.functionName = "fun4";
        return x + 2 * y;
    }

    private double fun5(double x, double y) {
        this.functionName = "fun5";
        return Math.sin(x / 2) + Math.cos(x);
    }

    private double fun6(double x, double y) {
        this.functionName = "fun6";
        return Math.pow(x, 2) + 3 * x * y - 7 * y + 1;
    }

}