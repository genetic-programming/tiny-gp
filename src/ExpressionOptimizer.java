import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionOptimizer {

    public static String optimizeExpression(String input) {

        Pattern pattern = Pattern.compile("\\(([^()]+)\\)");
        Matcher matcher = pattern.matcher(input);


        while (matcher.find()) {
            String expression = matcher.group(1);
            if(!(expression.contains("X1")|| expression.contains("X2")) && expression.contains(" ")) {
                Expression e = new ExpressionBuilder(expression).build();
                double result = e.evaluate();
                String optimizedExpression = String.valueOf(result);
                input = input.replace("(" + expression + ")", optimizedExpression);
                matcher = pattern.matcher(input);
            }

        }


    return input;
    }
}
