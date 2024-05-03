package pro.akosarev.sandbox.notForApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.util.function.DoubleUnaryOperator;

public class DerivativeServiceTest {

    @Test
    public void testFirstDerivative() {
        DoubleUnaryOperator sinFunction = Math::sin;
        double x = 30;
        double h = 0.00001;
        double expected = Math.cos(Math.toRadians(x)); // Производная первая
        double actual = DerivativeService.derivative(sinFunction, x, h, 1);

        assertEquals(expected, actual, 0.0001, "Производной 1-го разряда не найдено");
    }

    @Test
    public void testSecondDerivative() {
        DoubleUnaryOperator sinFunction = Math::sin;
        double x = 30;
        double h = 0.00001;
        double expected = -Math.sin(Math.toRadians(x)); // Производная вторая
        double actual = DerivativeService.derivative(sinFunction, x, h, 2);
        assertEquals(expected, actual, 0.0001, "Производной 2-го разряда не найдено");
    }

    @Test
    public void testNaN() {
        // Проверка на NaN
        DoubleUnaryOperator identityFunction = x -> x;
        double x = 10;
        double h = 0.00001;
        double actual = DerivativeService.derivative(identityFunction, x, h, 3);
        assertEquals(Double.NaN, actual, "NaN");
    }
}
