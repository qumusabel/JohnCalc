package se.itmo.imf.equsolve.solvers.system;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import kotlin.Triple;
import se.itmo.imf.equsolve.solvers.SolverException;

public class SystemViewModel extends ViewModel {
    private Double x = null;
    private Double y = null;
    private Double r = null;
    private Double eps = null;
    private EquationSystem equationSystem;

    private final MutableLiveData<Boolean> isSolveEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> lastError = new MutableLiveData<>();
    private final MutableLiveData<DualSystemSolver.Result> lastResult = new MutableLiveData<>();
    private final MutableLiveData<Bounds> lastBounds = new MutableLiveData<>();

    public void solve() {
        if (Boolean.FALSE.equals(isSolveEnabled.getValue())) {
            throw new IllegalStateException("Must provide valid inputs");
        }

        lastBounds.setValue(new Bounds(x, y, r));

        try {
            var solver = new DualSystemSolver(x, y, r, eps, equationSystem);
            lastResult.setValue(solver.solve());
        } catch (SolverException e) {
            lastError.setValue(e.getMessage());
        }
    }

    public void selectSystem(int index) {
        this.equationSystem = EquationSystem.SYSTEMS[index];
    }

    public EquationSystem[] getSystems() {
        return EquationSystem.SYSTEMS;
    }

    public void setX(Double x) {
        this.x = x;
        updateSolveEnabled();
    }

    private boolean isXValid() {
        return x != null && !x.isInfinite() && !x.isNaN();
    }

    public void setY(Double y) {
        this.y = y;
        updateSolveEnabled();
    }

    private boolean isYValid() {
        return y != null && !y.isInfinite() && !y.isNaN();
    }

    public void setR(Double r) {
        this.r = r;
        updateSolveEnabled();
    }

    public boolean isRValid() {
        return r != null && !(y.isInfinite() || y.isNaN()) && r > 0;
    }

    public void setEps(Double eps) {
        this.eps = eps;
        updateSolveEnabled();
    }

    private boolean isEpsValid() {
        return eps != null && !eps.isInfinite() && !eps.isNaN() && eps > 0;
    }

    private void updateSolveEnabled() {
        isSolveEnabled.setValue(isXValid() && isYValid() && isRValid() && isEpsValid());
    }

    public LiveData<Boolean> getIsSolveEnabled() {
        return isSolveEnabled;
    }

    public LiveData<String> getLastError() {
        return lastError;
    }

    public LiveData<DualSystemSolver.Result> getLastResult() {
        return lastResult;
    }

    public LiveData<Bounds> getLastBounds() {
        return lastBounds;
    }

    record Bounds(double x, double y, double r) {
    }
}
