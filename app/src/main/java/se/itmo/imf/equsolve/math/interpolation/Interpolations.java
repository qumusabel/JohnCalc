package se.itmo.imf.equsolve.math.interpolation;

import java.util.Arrays;

import static java.lang.StrictMath.abs;

class Interpolations {
    private Interpolations() {}

    static class Lagrange implements Interpolation {
        private final TableFunction func;
        private final double[] c;

        public Lagrange(TableFunction func) {
            this.func = func;
            c = new double[func.numPoints()];

            for (int i = 0; i < func.numPoints(); i++) {
                double prod = 1;
                for (int j = 0; j < func.numPoints(); j++) {
                    if (i != j) {
                        prod *= (func.points()[i].x() - func.points()[j].x());
                    }
                }

                c[i] = func.points()[i].y() / prod;
            }
        }

        @Override
        public double at(double x) {
            double result = 0;
            for (int i = 0; i < func.numPoints(); i++) {
                double prod = 1;
                for (int j = 0; j < func.numPoints(); j++) {
                    if (i != j) {
                        prod *= (x - func.points()[j].x());
                    }
                }

                result += c[i] * prod;
            }
            return result;
        }

        @Override
        public boolean willBePreciseAt(double x) {
            // just check if we're extrapolating
            double fMax = Arrays.stream(func.points()).mapToDouble(p -> p.x()).max().getAsDouble();
            double fMin = Arrays.stream(func.points()).mapToDouble(p -> p.x()).min().getAsDouble();
            return fMin <= x && x <= fMax;
        }
    }

    static class Newton implements Interpolation {
        private final TableFunction func;
        private final double[][] f;

        public Newton(TableFunction func) {
            this.func = func;
            f = new double[func.numPoints()][func.numPoints()];
            for (int i = 0; i < func.numPoints(); i++) {
                f[0][i] = func.points()[i].y();
            }

            for (int k = 1; k < f.length; k++) {
                for (int i = 0; i < f.length - k; i++) {
                    f[k][i] = (f[k - 1][i + 1] - f[k - 1][i]) / (func.points()[i + k].x() - func.points()[i].x());
                }
            }
        }

        @Override
        public double at(double x) {
            double result = 0;
            double prod = 1;
            for (int i = 0; i < f.length; i++) {
                result += f[i][0] * prod;
                prod *= (x - func.points()[i].x());
            }
            return result;
        }

        @Override
        public boolean willBePreciseAt(double x) {
            // just check if we're extrapolating
            double fMax = Arrays.stream(func.points()).mapToDouble(p -> p.x()).max().getAsDouble();
            double fMin = Arrays.stream(func.points()).mapToDouble(p -> p.x()).min().getAsDouble();
            return fMin <= x && x <= fMax;
        }
    }

    static class Gauss implements Interpolation {
        private final TableFunction func;
        private final double d[][];
        private final int mi;
        private final double h;

        public Gauss(TableFunction func) {
            this.func = func;

            if (func.numPoints() % 2 != 1) {
                throw new RuntimeException("Function must have odd number of points");
            }

            if (!isEvenGrid(func)) {
                throw new RuntimeException("Nodes must be evenly spaced on Ox");
            }
            h = func.points()[1].x() - func.points()[0].x();

            mi = func.numPoints() / 2;
            d = new double[func.numPoints()][func.numPoints()];

            for (int i = 0; i < func.numPoints(); i++) {
                d[0][i] = func.points()[i].y();
            }

            for (int k = 1; k < d.length; k++) {
                for (int i = 0; i < d.length - k; i++) {
                    d[k][i] = d[k - 1][i + 1] - d[k - 1][i];
                }
            }
        }

        @Override
        public double at(double x) {
            if (x < func.points()[mi].x()) {
                return atBackward(x);
            } else {
                return atForward(x);
            }
        }

        private double atForward(double x) {
            double t = (x - func.points()[mi].x()) / h;

            double tProd = t;
            double fact = 1, curFact = 1;
            double result = d[0][mi];

            double sign = -1;

            for (int j = 1; j < d.length; j++) {
                int i = mi - j / 2;
                double y = d[j][i];

                fact *= curFact;
                curFact += 1;
                result += y * tProd / fact;
                tProd *= (t + (j + 1) / 2 * sign);
                sign = -sign;
            }

            return result;
        }

        private double atBackward(double x) {
            double t = (x - func.points()[mi].x()) / h;

            double tProd = t;
            double fact = 1, curFact = 1;
            double result = d[0][mi];

            double sign = +1;

            for (int j = 1; j < d.length; j++) {
                int i = mi - (j + 1) / 2;
                double y = d[j][i];

                fact *= curFact;
                curFact += 1;
                result += y * tProd / fact;
                tProd *= (t + (j + 1) / 2 * sign);
                sign = -sign;
            }

            return result;
        }

        @Override
        public boolean willBePreciseAt(double x) {
            return (func.points()[mi].x() - x) <= h;
        }

        public boolean isEvenGrid(TableFunction func) {
            final double d = func.points()[1].x() - func.points()[0].x();
            for (int i = 0; i < func.numPoints()-1; i++) {
                double di = func.points()[i+1].x() - func.points()[i].x();
                if (abs(di - d) > 1e-6) {
                    return false;
                }
            }
            return true;
        }
    }
}
