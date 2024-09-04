package se.itmo.imf.gauss;

import static java.lang.Math.*;

public class Gauss {
    final static double EPS = 1e-5f;

    public static Vector solve(Matrix A, Vector B) {
        if (A.nRows() != B.size()) {
            throw new SolverException(
                    String.format("length of B (%d) must be equal to A's nrows (%d)", B.size(), A.nRows()));
        }

        Matrix tri = triangular(A.aug(B));

        double[] solution = new double[tri.nCols()];
        for (int i = tri.nRows()-1; i >= 0; i--) {
            Vector triRow = tri.row(i);
            double knownSum = triRow.get(triRow.size()-1);
            for (int j = tri.nCols()-1; j > i; j--) {
                knownSum -= solution[j] * triRow.get(j);
            }
            solution[i] = knownSum / triRow.get(i);
        }

        return new Vector(solution).takeFirst(A.nCols());
    }

    public static double determinant(Matrix M) {
        if (M.nRows() != M.nCols()) {
            throw new SolverException(String.format("not a square matrix: nrows = %d, ncols = %d", M.nRows(), M.nCols()));
        }
        Counter cnt = new Counter();
        Matrix tri = triangular(M, cnt);

        double det = (double) pow(-1, cnt.count);
        for (int i = 0; i < tri.nRows(); i++) {
            det *= tri.row(i).get(i);
        }
        return det;
    }

    private static Matrix triangular(Matrix M, Counter swaps) {
        Matrix G = M.clone();
        var m = G.doInplace();

        for (int i = 0; i < M.nRows(); i++) {
            int idx = findAbsMax(G.col(i).dropFirst(i));
            if (idx != 0) {
                swaps.inc();
            }
            m.swapRows(i, i + idx);

            Vector theRow = G.row(i);

            if (abs(theRow.get(i)) < EPS) {
                throw new SolverException("zero diagonal element encountered");
            }

            for (int j = i+1; j < G.nRows(); j++) {
                Vector fac = theRow.mul(-1 * G.row(j).get(i) / theRow.get(i));
                m.addRow(j, fac);
            }
        }

        return G;
    }

    public static Matrix triangular(Matrix M) {
        return triangular(M, new Counter());
    }

    public static int findAbsMax(Vector vec) {
        int idx = 0;
        double max = 0;
        for (int i = 0; i < vec.size(); i++) {
            double cur = abs(vec.get(i));
            if (cur > max) {
                max = cur;
                idx = i;
            }
        }
        return idx;
    }

    private static class Counter {
        int count = 0;
        void inc() {
            add(1);
        }
        void add(int n) {
            count += n;
        }
    }

    static class SolverException extends IllegalArgumentException {
        SolverException(String msg) {
            super(msg);
        }
    }
}
