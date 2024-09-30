package se.itmo.imf.equsolve.math.differential_equation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;

import se.itmo.imf.equsolve.R;
import se.itmo.imf.equsolve.databinding.ViewResultWithTableBinding;
import se.itmo.imf.equsolve.uiframework.GraphView;
import se.itmo.imf.equsolve.uiframework.SolverFragment;

public class DifferentialEquationFragment extends SolverFragment {
    private Double x0, xn, y0, h;
    private Solver.Method method;
    private Equation eqn;

    @NonNull
    @Override
    protected String title() {
        return "Differential Equation";
    }

    @NonNull
    @Override
    protected String subtitle() {
        return "The Industrial Revolution and its consequences have been a disaster for the human race.";
    }

    @NonNull
    @Override
    protected ProblemAdapter problemAdapter() {
        return new ProblemAdapter() {
            @Override
            public int count() {
                return Equation.INSTANCES.length;
            }

            @Override
            public void initView(GraphView gv, int index) {
                Equation eqn = Equation.INSTANCES[index];
                gv.reloadAndRun(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    gv.addText(eqn.latex(), requireContext().getColor(R.color.md_theme_primary));
                });
            }

            @Override
            public void onPageChange(int index) {
                eqn = Equation.INSTANCES[index];
            }
        };
    }

    @Override
    protected void initView() {
        addField("x₀", true, true, (Double v) -> x0 = v);
        addField("xₙ", true, true, (Double v) -> xn = v);
        addField("h", false, true, (Double v) -> h = v);
        addField("y₀", true, true, (Double v) -> y0 = v);

        useMethodSelector(Solver.Method.names(), Solver.Method.values(), m -> method = m);
        setButtonEnabled(true);
    }

    @Override
    protected void onClickSolve() {
        Solver.Result result = Solver.solve(eqn, method, x0, xn, h, y0, 1337);

        double yCenter = Arrays.stream(result.p()).mapToDouble(Point::y).average().getAsDouble();
        double yAmplitude = Arrays.stream(result.p()).mapToDouble(Point::y).max().getAsDouble() - yCenter;

        GraphView gv = getCurrentGraphView();
        gv.plotFunction(eqn.jessieCode(x0, y0), requireContext().getColor(R.color.md_theme_primary));
        gv.zoomOxy((x0 + xn) / 2, yCenter, Math.max((xn - x0) / 2, yAmplitude) * 1.1);
        // gv.zoomOx(x0, xn);

        String template = """
        var dataX = [[DATAX]];
        var dataY = [[DATAY]];
        board.create('curve', [dataX, dataY], {strokeColor: 'red', strokeWidth: 1.5});
        """;

        StringBuilder sbx = new StringBuilder();
        StringBuilder sby = new StringBuilder();
        for (int i = 0; i < result.p().length; i++) {
            sbx.append(result.p()[i].x()).append(", ");
            sby.append(result.p()[i].y()).append(", ");
        }

        String script = template
                .replace("[DATAX]", sbx.toString())
                .replace("[DATAY]", sby.toString());

        gv.evaluateJavascript(script, null);

        // showSuccess("Solved",
        //        ((method == Solver.Method.ADAMS) ? "R = " : "ε = ") + result.eps());
        showResult(result);
    }

    private void showResult(Solver.Result result) {
        StringBuilder html = new StringBuilder("<table>");
        html.append("<tr><th>x</th><th>y*</th><th>y</th><th>ε</th></tr>");

        double c = eqn.c().apply(x0, y0);
        for (Point p : result.p()) {
            double x = p.x();
            double ya = p.y();
            double y = eqn.y().apply(x, c);

            html.append("<tr><td>" + x + "</td><td>" + ya + "</td><td>" + y + "</td><td>" + (ya - y) + "</td></tr>");
        }
        html.append("</table>");
        html.append("""
                <style>
                    table { border-collapse: collapse; }
                    tr { border: none; }
                    td {
                      border-right: solid 1px #f00; 
                      border-left: solid 1px #f00;
                    }
                </style>
                """);

        ViewResultWithTableBinding binding = ViewResultWithTableBinding.inflate(getLayoutInflater());
        binding.resultText.setText(((method != Solver.Method.ADAMS) ? "R = " : "ε = ") + result.eps());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Done")
                .setIcon(R.drawable.baseline_check_24)
                .setView(binding.getRoot())
                .create();
        showDialog(dialog);

        binding.resultWebview.loadData(html.toString(), "text/html", "utf-8");
    }
}
