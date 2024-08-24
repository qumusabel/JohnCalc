package se.itmo.imf.equsolve.math.integral;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import se.itmo.imf.equsolve.R;
import se.itmo.imf.equsolve.uiframework.GraphView;
import se.itmo.imf.equsolve.uiframework.SolverFragment;

public class IntegralFragment extends SolverFragment {
    private IntegralViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(IntegralViewModel.class);
    }

    @NonNull
    @Override
    protected String title() {
        return "Integrate";
    }

    @NonNull
    @Override
    protected String subtitle() {
        return "Find area under a curve";
    }

    @NonNull
    @Override
    protected ProblemAdapter problemAdapter() {
        return new ProblemAdapter() {
            @Override
            public int count() {
                return Function.FUNCTIONS.length;
            }

            @Override
            public void initView(GraphView gv, int index) {
                Function f = Function.FUNCTIONS[index];

                gv.reloadAndRun(() -> {
                    gv.plotFunction(f.jessieCode(), requireContext().getColor(R.color.md_theme_primary));
                    gv.addText(f.latex(), requireContext().getColor(R.color.md_theme_primary));
                });
            }

            @Override
            public void onPageChange(int index) {
                viewModel.setFunction(Function.FUNCTIONS[index]);
            }
        };
    }

    @Override
    protected void initView() {
        addField("a", true, true, viewModel::setA);
        addField("b", true, true, viewModel::setB);
        addField("ε", false, true, viewModel::setEps);

        useMethodSelector(
                Arrays.stream(Integrator.Method.values()).map(m -> m.getName()).toArray(String[]::new),
                Integrator.Method.values(),
                viewModel::setMethod
        );


        viewModel.getIsSolveEnabled().observe(this, this::setButtonEnabled);
        viewModel.getLastResult().observe(this, (res) -> {
            NumberFormat format = DecimalFormat.getInstance(getLocale());
            format.setMaximumFractionDigits(Integer.MAX_VALUE);
            showSuccess(
                    String.format(getLocale(),
                            "Calculated using %d intervals", res.n()),
                    String.format(getLocale(),
                            "Area under curve equals %s", format.format(res.value()))
            );
        });
        viewModel.getLastError().observe(this, this::showError);
        // viewModel.getLastBounds().observe(this, (bounds) -> {
        //     getCurrentGraphView().zoomOxy(bounds.x(), bounds.y(), bounds.r());
        // });
    }

    @Override
    protected void onClickSolve() {
        viewModel.solve();
    }
}
