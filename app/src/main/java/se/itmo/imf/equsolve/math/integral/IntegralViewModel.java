package se.itmo.imf.equsolve.math.integral;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IntegralViewModel extends ViewModel {
    private Double a = null;
    private Double b = null;
    private Double eps = null;
    private Integrator.Method method = null;
    private Function function;

    private final MutableLiveData<Boolean> isSolveEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> lastError = new MutableLiveData<>();
    private final MutableLiveData<Integrator.Result> lastResult = new MutableLiveData<>();

    void solve() {
        if (function == null) {
            throw new NullPointerException("'function' is null!");
        }
        lastResult.setValue(Integrator.integrate(function, a, b, eps, method));
    }

    private void updateSolveEnabled() {
        isSolveEnabled.setValue(isAValid() && isBValid() && isEpsValid() && method != null);
    }

    private boolean isAValid() {
        return a != null && !a.isInfinite() && !a.isNaN();
    }

    private boolean isBValid() {
        return b != null && !b.isInfinite() && !b.isNaN();
    }

    private boolean isEpsValid() {
        return eps != null && !eps.isInfinite() && !eps.isNaN() && eps > 0;
    }

    void setA(Double a) {
        this.a = a;
        updateSolveEnabled();
    }

    void setB(Double b) {
        this.b = b;
        updateSolveEnabled();
    }

    void setEps(Double eps) {
        this.eps = eps;
        updateSolveEnabled();
    }

    void setMethod(Integrator.Method method) {
        this.method = method;
        updateSolveEnabled();
    }

    void setFunction(Function function) {
        this.function = function;
    }

    public MutableLiveData<Boolean> getIsSolveEnabled() {
        return isSolveEnabled;
    }

    public MutableLiveData<String> getLastError() {
        return lastError;
    }

    public MutableLiveData<Integrator.Result> getLastResult() {
        return lastResult;
    }

}
