package se.itmo.imf.equsolve.math.single_equation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import se.itmo.imf.equsolve.R;
import se.itmo.imf.equsolve.uiframework.GraphView;
import se.itmo.imf.equsolve.uiframework.SolverFragment;

public class SingleFragment2 extends SolverFragment {
    private SingleViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SingleViewModel.class);
    }

    @NonNull
    @Override
    protected String title() {
        return "Single";
    }

    @NonNull
    @Override
    protected String subtitle() {
        return "Solve a single equation";
    }

    @Override
    protected void initView() {
        addField("a", true, true, viewModel::setA);
        addField("b", true, true, viewModel::setB);
        addField("ε", false, true, viewModel::setEps);

        useMethodSelector(SingleSolver.Method.getNames(), SingleSolver.Method.values(),
                viewModel::setMethod);

        viewModel.getSolveEnabled().observe(this, this::setButtonEnabled);
        viewModel.getLastResult().observe(this, (res) -> {
            NumberFormat format = DecimalFormat.getInstance(getLocale());
            format.setMaximumFractionDigits(Integer.MAX_VALUE);
            showSuccess(
                    String.format(getLocale(), "Solved in %d iterations", res.iters()),
                    String.format(getLocale(), "x = %s\nf = %s",
                            format.format(res.x()), format.format(res.y()))
            );
        });
        viewModel.getLastError().observe(this, this::showError);
        viewModel.getLastBounds().observe(this, (p) -> {
            getCurrentGraphView().zoomOx(p.first, p.second);
        });
    }

    @Override
    protected void onClickSolve() {
        viewModel.solve();
    }

    @NonNull
    @Override
    protected ProblemAdapter problemAdapter() {
        return new ProblemAdapter() {
            @Override
            public int count() {
                return 2;
            }

            @Override
            public void initView(GraphView gv, int index) {
                DisplayEquation eq = viewModel.getEquations()[index];
                gv.reloadAndRun(() -> {
                    gv.plotFunction(eq.jessieCode(), requireContext().getColor(R.color.md_theme_primary));
                    gv.addText(eq.latex(), requireContext().getColor(R.color.md_theme_primary));
                });
//                gv.setEquation(viewModel.getEquations()[index], requireContext().getColor(R.color.md_theme_primary));
            }

            @Override
            public void onPageChange(int index) {
                viewModel.selectEquation(index);
            }
        };
    }
}
