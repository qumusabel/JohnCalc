package se.itmo.imf.gauss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class GaussCli {
    public static void main(String... args) {
        if (args.length == 1 && args[0].equals("--help")) {
            System.out.println(
                    """
                            Usage:
                            \tgauss\t\t- read matrix from stdin (use pipes for file input)
                            \tgauss --random n\t- solve a random NxN system, N <= 20
                            \tgauss --help\t- show this message""");
            return;
        }

        Matrix A;
        Vector B;
        if (args.length == 0) {
            if (System.console() != null) {
                System.out.println("\tReading matrix A");
            }
            A = readMatrix();
            if (System.console() != null) {
                System.out.println("\tReading column vector B");
            }
            B = readVector();
        } else if (args.length == 2 && args[0].equals("--random")) {
            int size = Integer.parseInt(args[1]);

            if (size > 20 || size <= 0) {
                System.err.println("Size must be not more than 20");
                System.exit(1);
                return;
            }

            A = Matrix.random(size);
            B = Vector.random(size);

            System.out.println("\tRandom matrix A:");
            System.out.println(A);
            System.out.println("\tFree coefficients column vector B:");
            System.out.println(B);
            System.out.println();
        } else {
            System.err.println("check --help");
            System.exit(2);
            return;
        }

        try {
            runSolution(A, B);
        } catch (Gauss.SolverException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    static void runSolution(Matrix A, Vector B) {
        System.out.println("\tDeterminant of A:");
        System.out.println(Gauss.determinant(A));

        System.out.println("\t[A|B] in triangular form:");
        System.out.println(Gauss.triangular(A.aug(B)));

        Vector solution = Gauss.solve(A, B);
        System.out.println("\tSolution x:");
        System.out.println(solution);

        Vector residual = A.mul(solution).add(B.mul(-1));
        System.out.println("\tResidual Ax - B");
        System.out.println(residual);
    }

    static Matrix readMatrix() {
        if (System.console() != null) {
            System.out.println("Enter your matrix elements, separated by spaces and newlines. Finish with a blank line");
        }

        List<Vector> rows = new ArrayList<>();
        for (Vector row = readVector(true); row != null; row = readVector(true)) {
            rows.add(row);
        }

        return new Matrix(rows.toArray(new Vector[0]));
    }

    static Vector readVector() {
        return readVector(false);
    }

    private static Vector readVector(boolean forMatrix) {
        if (!forMatrix && System.console() != null) {
            System.out.println("Enter vector elements, separated by spaces");
        }

        Scanner sc = null;
        try {
            String s = readLine();
            if (forMatrix && s.isBlank()) {
                return null;
            }
            sc = new Scanner(s);
        } catch (InputMismatchException e) {
            if (forMatrix) {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Float> data = new ArrayList<>();
        while (sc.hasNext() && sc.hasNextFloat()) {
            data.add(sc.nextFloat());
        }
        return new Vector(unbox(data));
    }

    private static float[] unbox(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(new PreventClosingInputStream(System.in)));

    private static String readLine() throws IOException {
        if (System.console() != null) {
            return System.console().readLine();
        }
        return reader.readLine();
    }

    static class PreventClosingInputStream extends InputStream {
        private final InputStream inputStream;

        public PreventClosingInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public void close() throws IOException {
            System.err.println("Bitch tried to bail");
            // Don't call super.close();
        }
    }
}
