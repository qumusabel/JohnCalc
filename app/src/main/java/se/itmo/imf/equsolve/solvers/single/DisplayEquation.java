package se.itmo.imf.equsolve.solvers.single;

import static java.lang.StrictMath.*;

record DisplayEquation(String latex, String jessieCode, Equation eqn) {
    public static DisplayEquation[] EQUATIONS = {
            new DisplayEquation(
                    "-0.38x^3 - 3.42x^2 + 2.51x + 8.75 = 0",
                    "-0.38*pow(x, 3) - 3.42*pow(x, 2) + 2.51*x + 8.75",
                    new Equation(
                            (x) -> -0.38 * pow(x, 3) - 3.42 * pow(x, 2) + 2.51 * x + 8.75,
                            (x) -> -0.38 * 3 * pow(x, 2) - 3.42 * 2 * x + 2.51,
                            (x) -> -0.38 * 6 * x - 3.42 * 2
                    )
            ),
            new DisplayEquation(
                    "\\sin x \\cdot \\ln x = 2",
                    "sin(x) * ln(x) - 2",
                    new Equation(
                            (x) -> sin(x) * log(x) - 2,
                            (x) -> cos(x) * pow(x, -1),
                            (x) -> -sin(x) * pow(x, -2)
                    )
            )
    };
}
