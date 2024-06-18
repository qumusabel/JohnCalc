package se.itmo.imf.equsolve.solvers.system;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import se.itmo.imf.equsolve.R;
import se.itmo.imf.equsolve.uiframework.GraphView;
import se.itmo.imf.equsolve.uiframework.SolverFragment;

public class SystemFragment extends SolverFragment {
    private SystemViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SystemViewModel.class);
    }

    @NonNull
    @Override
    protected String title() {
        return "System";
    }

    @NonNull
    @Override
    protected String subtitle() {
        return "Solve a system of equations";
    }

    @NonNull
    @Override
    protected ProblemAdapter problemAdapter() {
        return new ProblemAdapter() {
            @Override
            public int count() {
//                return 1;
                return viewModel.getSystems().length;
            }

            @Override
            public void initView(GraphView gv, int index) {
                EquationSystem eqs = viewModel.getSystems()[index];
                gv.reloadAndRun(() -> {
                    gv.plotImplicit(eqs.eq1().jessieCode(), requireContext().getColor(R.color.md_theme_primary));
                    gv.plotImplicit(eqs.eq2().jessieCode(), requireContext().getColor(R.color.md_theme_tertiary));

                    gv.addText(eqs.latex(), requireContext().getColor(R.color.md_theme_secondary));
                });
            }

            @Override
            public void onPageChange(int index) {
                viewModel.selectSystem(index);
            }
        };
    }

    @Override
    protected void initView() {
        addField("x", true, true, viewModel::setX);
        addField("y", true, true, viewModel::setY);
        addField("r", false, true, viewModel::setR);
        addField("ε", false, true, viewModel::setEps);

        viewModel.getIsSolveEnabled().observe(this, this::setButtonEnabled);
        viewModel.getLastResult().observe(this, (res) -> {
            NumberFormat format = DecimalFormat.getInstance(getLocale());
            format.setMaximumFractionDigits(Integer.MAX_VALUE);
            showSuccess(
                    String.format(getLocale(), "Solved in %d iterations", res.iters()),
                    String.format(getLocale(), "x = %s\ny = %s",
                            format.format(res.x()), format.format(res.y()))
            );
        });
        viewModel.getLastError().observe(this, this::showError);
        viewModel.getLastBounds().observe(this, (bounds) -> {
            getCurrentGraphView().zoomOxy(bounds.x(), bounds.y(), bounds.r());
        });
    }

    @Override
    protected void onClickSolve() {
        viewModel.solve();
    }
}
