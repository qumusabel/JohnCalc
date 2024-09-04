package se.itmo.imf.gauss;

import java.util.Arrays;

public class Matrix {
    private final Vector[] rows;

    /*
    Constructors
     */
    public Matrix(Vector... rows) {
        assertEqualLengths(rows);
        this.rows = rows;
    }

    public Matrix(double[][] m) {
        this(fromFloats(m));
    }

    public static Matrix random(int n) {
        Vector[] newRows = new Vector[n];
        for (int i = 0; i < n; i++) {
            newRows[i] = Vector.random(n);
        }
        return new Matrix(newRows);
    }

    private static Vector[] fromFloats(double[][] m) {
        Vector[] rows = new Vector[m.length];
        for (int i = 0; i < m.length; i++) {
            rows[i] = new Vector(m[i]);
        }
        return rows;
    }

    public Matrix aug(Vector vec) {
        Vector[] newRows = new Vector[nRows()];
        for (int i = 0; i < nRows(); i++) {
            double[] newRow = Arrays.copyOf(rows[i].getData(), nCols() + 1);
            newRow[nCols()] = vec.get(i);
            newRows[i] = new Vector(newRow);
        }
        return new Matrix(newRows);
    }

    public Matrix unaug() {
        Vector[] newRows = new Vector[nRows()];
        for (int i = 0; i < nRows(); i++) {
            newRows[i] = rows[i].takeFirst(nCols()-1);
        }
        return new Matrix(newRows);
    }

    private static void assertEqualLengths(Vector... vectors) {
        for (int i = 0; i < vectors.length - 1; i++) {
            if (vectors[i].size() != vectors[i].size()) {
                throw new IllegalArgumentException("row vectors must be of equal length");
            }
        }
    }

    /*
    Basic access
     */
    public int nRows() {
        return rows.length;
    }

    public Vector row(int i) {
        return rows[i];
    }

    public int nCols() {
        return rows[0].size();
    }

    public Vector col(int i) {
        double[] newData = new double[nCols()];
        for (int j = 0; j < nRows(); j++) {
            newData[j] = rows[j].get(i);
        }
        return new Vector(newData);
    }


    /*
    Arithmetic, returns new objects
     */
    public Vector mul(Vector vec) {
        if (nCols() != vec.size()) {
            throw new IllegalArgumentException(
                String.format("incompatible row and vec lengths: row=%d, vec=%d", nCols(), vec.size()));
        }

        double[] result = new double[nCols()];
        for (int i = 0; i < result.length; i++) {
            result[i] = rows[i].dot(vec);
        }
        return new Vector(result);
    }

    /*
    Special facility for non-copying manipulation
     */
    public MatrixInplace doInplace() {
        return new MatrixInplaceImpl();
    }

    public interface MatrixInplace {
        MatrixInplace swapRows(int i, int j);
        MatrixInplace addRow(int i, Vector vec);
        MatrixInplace mulRow(int i, double x);
    }

    private class MatrixInplaceImpl implements MatrixInplace {
        Vector[] rows = Matrix.this.rows;
        private MatrixInplaceImpl() {
        }

        public MatrixInplaceImpl swapRows(int i, int j) {
            Vector tmp = rows[i];
            rows[i] = rows[j];
            rows[j] = tmp;
            return this;
        }

        public MatrixInplaceImpl addRow(int i, Vector vec) {
            rows[i].doInplace().add(vec);
            return this;
        }

        public MatrixInplaceImpl mulRow(int i, double x) {
            rows[i].doInplace().mul(x);
            return this;
        }
    }


    @Override
    public Matrix clone() {
        Vector[] newRows = new Vector[rows.length];

        for (int i = 0; i < rows.length; i++) {
            newRows[i] = rows[i].clone();
        }

        return new Matrix(newRows);
    }

    @Override
    public String toString() {
//        int maxLen = 0;
//        for (Vector row: rows) {
//            for (int i = 0; i < row.size(); i++) {
//                maxLen = Math.max(maxLen, "%f".formatted(row.get(i)).length());
//            }
//        }
        StringBuilder result = new StringBuilder();
        for (Vector row : rows) {
            result.append(row);
            result.append("\n");
        }
        return result.toString();
    }
}
