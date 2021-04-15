package main;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class MathParser {

    public static void main(String[] args) {
        MathParser mathParser = new MathParser();

        mathParser.printResult("sin(2*(-5+1.5*4)+28)"); // 0.5
        mathParser.printResult("tan(2025 ^ 0.5)"); // 1
        mathParser.printResult("1+(1+(1+1)*(1+1))*(1+1)+1");  // 12
        mathParser.printResult("-2^(-2)");  // 0.25
        mathParser.printResult("(-2^(-2))+2+(-2^(-2))"); // 2.5
        mathParser.printResult("(-2)*(-2)"); // 4
        mathParser.printResult("(-2)/(-2)"); // 1
        mathParser.printResult("1-(-1)-1"); // 1
        mathParser.printResult("sin(-30)"); // -0.5
        mathParser.printResult("cos(-30)"); // 0.87
        mathParser.printResult("tan(-30)"); // -0.58
        mathParser.printResult("2+8*(9/4-1.5)^(1+1)"); // 6.48
        mathParser.printResult("tan(44+sin(89-cos(180)^2))"); // 1
        mathParser.printResult("-cos(180)^2"); // -1

    }

    public void printResult(String expression) {
        System.out.println(expression + " = " + getResult(expression));
    }


    public double getResult(String expression) {
        /* Проверяем не допущены ли ошибки в начальном выражении */
        checkExpression(expression);

        /* до тех пор, пока выражение не будет соответствовать числу */
        while (!isNumber(expression)) {
            /* получаем выражение из скобок (при их наличии)*/
            String bracketExpression = getBracketsExpression(expression);
            /* разбиваем выражение на список элементов (чисел, операторов и тригонометрических функций) */
            List<String> elements = splitForElements(bracketExpression);
            /* полуаем промежуточный результат */
            double result = getIntermediateResult(elements);
            /* подставляем промежуточный результат в начально выражение */
            expression = expression.replace(bracketExpression, String.valueOf(result));
        }

        return Double.parseDouble(expression);
    }


    private String getBracketsExpression(String expression) {
        String bracketExpression = expression;
        if (expression.contains("(")) {
            int indexOpen = expression.lastIndexOf("(");
            int indexClose = expression.indexOf(")", indexOpen) + 1;
            bracketExpression = expression.substring(indexOpen, indexClose);
        }
        return bracketExpression;
    }


    private List<String> splitForElements(String expression) {
        /* Регулярное выражение: число может быть целым или с плавающей  точкой и со знаком "-" в начале строки,
          | знаки | тригонометрия */
        final String regex = "\\B-?\\d+\\.?\\d*|(\\d+\\.?\\d*)|[\\^*/\\-+]|(sin|cos|tan|ctg)";

        Matcher matcher = Pattern.compile(regex).matcher(expression);
        Stack<String> elements = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            if (matcher.find()) {
                String element = matcher.group();
                elements.push(element);
            }
        }
        /* Форматируем отрицательные значения */
        return formattingElements(elements);
    }

    private Stack<String> formattingElements(Stack<String> elements) {
        Stack<String> formattedStack = new Stack<>();

        String sign = "";
        for (String element : elements) {

            if (!formattedStack.isEmpty()) {
            /* елси предыдущий элемент - знак или тригонометрическая функция,
               а текущий - знак "-" или "+", сохраняем его в переменную sign
             */
                if (!isNumber(formattedStack.peek()) && isMinusOrPlus(element)) {
                    sign = element;
                    continue;
                }
            }

            /* если тригонометрическая функция - элемены добваляем по раздельности */
            if (isTrigonometry(element) && isMinusOrPlus(sign)) {
                formattedStack.push(sign);
                formattedStack.push(element);
            } else {
                formattedStack.push(sign + element);
            }
            sign = "";
        }
        return formattedStack;
    }

    /* Поучаем список операторов из списка элементов */
    private List<String> getOperators(List<String> elements) {
        return elements.stream()
                /* сортируем в порядке уменьшения приоритета */
                .sorted(Comparator.comparing(x -> 10 / getOperationPriority(x)))
                .filter(x -> isOperator(x) || isTrigonometry(x))
                .collect(Collectors.toList());
    }

    /* Получаем промежуточный результат в скобках */
    private double getIntermediateResult(List<String> elements) {
        String result;
        List<String> operators = getOperators(elements);

        while (elements.size() != 1) {
            for (String operator : operators) {
                int index = elements.indexOf(operator);

                if (isMinusOrPlus(operator) && elements.size() == 2) {
                    result  = operator + elements.get(1);
                    if (!isNumber(result)) {
                        result = result.replace("--", "");
                        result = result.replace("+", "");
                    }
                    return Double.parseDouble(result);
                }

                if (isTrigonometry(operator)) {
                    double a = Double.parseDouble(elements.get(index + 1));
                    result = String.valueOf(makeOperation(operator, a));
                    /* меняем значения в списке элементов на результат операции */
                    elements.set(index, result + "");
                    elements.remove(index + 1);
                }

                if (isOperator(operator) && elements.size() > 2) {
                    double a = Double.parseDouble(elements.get(index - 1));
                    double b = Double.parseDouble(elements.get(index + 1));
                    result = String.valueOf(makeOperation(operator, a, b));

                    elements.set(index, result);
                    elements.remove(index - 1);
                    elements.remove(index);
                }
            }
        }
        result = elements.get(0);

        return Double.parseDouble(result);
    }


    private int getOperationPriority(String operation) {
        return switch (operation) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case ("^") -> 3;
            case ("sin"), ("cos"), ("tan") -> 4;
            default -> -1;
        };
    }

    private double makeOperation(String operation, double a, double b) {
        /* Округляем значения до 2 знаков после запятой */
        return switch (operation) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> new BigDecimal(a / b).setScale(2, RoundingMode.HALF_UP).doubleValue();
            case "^" -> BigDecimal.valueOf(Math.pow(a, b)).setScale(2, RoundingMode.HALF_UP).doubleValue();
            default -> throw new IllegalArgumentException("Неверное значение операции");
        };
    }

    private double makeOperation(String operation, double a) {
        double result = switch (operation) {
            case "sin" -> Math.sin(Math.toRadians(a));
            case "cos" -> Math.cos(Math.toRadians(a));
            case "tan" -> Math.tan(Math.toRadians(a));
            case "ctg" -> Math.cos(Math.toRadians(a)) / Math.sin(Math.toRadians(a));
            default -> 0;
        };
        return new BigDecimal(result).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean isTrigonometry(String element) {
        return element.equals("sin") || element.equals("cos")
                || element.equals("tan") || element.equals("ctg");
    }

    private boolean isOperator(String element) {
        return element.equals("+") || element.equals("-") || element.equals("*")
                || element.equals("/") || element.equals("^");
    }

    private boolean isMinusOrPlus(String element) {
        return element.equals("+") || element.equals("-");
    }

    private boolean isNumber(String element) {
        return element.matches("-?\\d+\\.?\\d*");
    }

    private void checkExpression(String expression) {
        /* не указан знак перед скобками или после скобок */
        Matcher errors = Pattern.compile("\\d+\\(\\d+|\\d+\\)\\d").matcher(expression);
        /* тригонометрические функции */
        Matcher trigonometry = Pattern.compile("[a-zA-Z]{1,3}").matcher(expression);
        /* дублирование знаков */
        Matcher doubleSign = Pattern.compile("[^a-zA-Z0-9()\\s]{2}").matcher(expression);

        for (int i = 0; i < expression.length(); i++) {
            if (errors.find()) {
                throw new IllegalArgumentException("Вы ввели данные некоректно: " + errors.group());
            }
            if (trigonometry.find()) {
                if (!isTrigonometry(trigonometry.group()))
                    throw new IllegalArgumentException("" + trigonometry.group());
            }
            if (doubleSign.find()) {
                throw new IllegalArgumentException("" + doubleSign.group());
            }
        }
        /* не закрыты все скобки */
        long s1 = Arrays.stream(expression.split("")).filter(x -> x.equals("(")).count();
        long s2 = Arrays.stream(expression.split("")).filter(x -> x.equals(")")).count();
        if (s1 != s2)
            throw new IllegalArgumentException("Проверьте, закрыты ли все скобки");
    }


}
