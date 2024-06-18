package se.itmo.imf.equsolve.solvers;

public class SolverException extends RuntimeException {
    private final String message;

    public SolverException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
