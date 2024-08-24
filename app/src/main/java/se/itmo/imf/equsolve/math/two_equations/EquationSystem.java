package se.itmo.imf.equsolve.math.two_equations;

import static java.lang.StrictMath.*;

public record EquationSystem(String latex, BiEquation eq1, BiEquation eq2) {
    public static EquationSystem[] SYSTEMS = {
        new EquationSystem(
        "\\begin{cases} " +
                "\\tan x \\cdot y = x^2 \\" +
                "\\0.5x^2 + 2y^2 = 1" +
                "\\end{cases}",
            new BiEquation(
                "tan(x) * y - x^2",
                (x, y) -> tan(x) * y - pow(x, 2),
                (x, y) -> y / pow(cos(x), 2) - 2*x,
                (x, y) -> tan(x)
            ),
            new BiEquation(
                "0.5*x^2 + 2*y^2 - 1",
                (x, y) -> 0.5*pow(x, 2) + 2*pow(y, 2) - 1,
                (x, y) -> x,
                (x, y) -> 4*y
            )
        )
    };
}