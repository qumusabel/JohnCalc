package se.itmo.imf.equsolve.solvers.single;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import se.itmo.imf.equsolve.solvers.SolverException;

public class SingleViewModel extends ViewModel {
    private DisplayEquation eqn = null;
    private SingleSolver.Method method;

    private Double a = null;

    public void setA(Double a) {
        this.a = a;
        updateSolveEnabled();
    }

    private Double b = null;

    public void setB(Double b) {
        this.b = b;
        updateSolveEnabled();
    }

    private Double eps = null;

    public void setEps(Double eps) {
        this.eps = eps;
        updateSolveEnabled();
    }

    private final MutableLiveData<Boolean> isSolveEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> lastError = new MutableLiveData<>();
    private final MutableLiveData<SingleSolver.Result> lastResult = new MutableLiveData<>();
    private final MutableLiveData<Pair<Double, Double>> lastBounds = new MutableLiveData<>();

    public void solve() {
        if (Boolean.FALSE.equals(isSolveEnabled.getValue())) {
            throw new IllegalStateException("Must provide valid inputs");
        }

        if (a < b) {
            lastBounds.setValue(Pair.create(a, b));
        }

        try {
            var solver = new SingleSolver(eqn.eqn(), method, a, b, eps);
            SingleSolver.Result result = solver.solve();
            lastResult.setValue(result);
        } catch (SolverException e) {
            lastError.setValue(e.getMessage());
        }
    }

    private void updateSolveEnabled() {
        isSolveEnabled.setValue(
                isAValid() && isBValid() && isEpsValid() && method != null && eqn != null);
    }

    private boolean isAValid() {
        return a != null && !a.isNaN() && !a.isInfinite();
    }

    private boolean isBValid() {
        return b != null && !b.isNaN() && !b.isInfinite();
    }

    private boolean isEpsValid() {
        return eps != null && !eps.isNaN() && !eps.isInfinite()
                && eps > 0;
    }

    public LiveData<Boolean> getSolveEnabled() {
        return isSolveEnabled;
    }

    public LiveData<SingleSolver.Result> getLastResult() {
        return lastResult;
    }

    public LiveData<String> getLastError() {
        return lastError;
    }

    public void selectEquation(int position) {
        this.eqn = getEquations()[position];
    }

    public void setMethod(SingleSolver.Method method) {
        this.method = method;
        updateSolveEnabled();
    }

    public DisplayEquation[] getEquations() {
        return DisplayEquation.EQUATIONS;
    }

    public LiveData<Pair<Double, Double>> getLastBounds() {
        return lastBounds;
    }
}
