package tests;

import main.MathParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;



class MathParserTest {
    @Test
    void getResult() {
        MathParser mathParser = new MathParser();

        List<Double> results = new ArrayList<>();
        results.add(mathParser.getResult("sin(2*(-5+1.5*4)+28)")); // 0.5
        results.add(mathParser.getResult("tan(2025 ^ 0.5)")); // 1
        results.add(mathParser.getResult("1+(1+(1+1)*(1+1))*(1+1)+1"));  // 12
        results.add(mathParser.getResult("-2^(-2)"));  // 0.25
        results.add(mathParser.getResult("(-2^(-2))+2+(-2^(-2))")); // 2.5
        results.add(mathParser.getResult("(-2)*(-2)")); // 4
        results.add(mathParser.getResult("(-2)/(-2)")); // 1
        results.add(mathParser.getResult("1-(-1)-1")); // 1
        results.add(mathParser.getResult("sin(-30)")); // -0.5
        results.add(mathParser.getResult("cos(-30)")); // 0.87
        results.add(mathParser.getResult("tan(-30)")); // -0.58
        results.add(mathParser.getResult("2+8*(9/4-1.5)^(1+1)")); // 6.48
        results.add(mathParser.getResult("tan(44+sin(89-cos(180)^2))")); // 1
        results.add(mathParser.getResult("-cos(180)^2")); // -1

        List<Double> answers = new ArrayList<>();
        answers.add(0.5);
        answers.add(1.0);
        answers.add(12.0);
        answers.add(0.25);
        answers.add(2.5);
        answers.add(4.0);
        answers.add(1.0);
        answers.add(1.0);
        answers.add(-0.5);
        answers.add(0.87);
        answers.add(-0.58);
        answers.add(6.48);
        answers.add(1.0);
        answers.add(-1.0);

        for (int i = 0; i < answers.size(); i++) {
            double actual = results.get(i);
            double expected = answers.get(i);
            Assertions.assertEquals(actual, expected);
        }
    }


}