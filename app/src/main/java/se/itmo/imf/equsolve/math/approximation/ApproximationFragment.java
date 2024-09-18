package se.itmo.imf.equsolve.math.approximation;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialContainerTransform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.itmo.imf.equsolve.R;
import se.itmo.imf.equsolve.databinding.FragmentTabularBinding;
import se.itmo.imf.equsolve.uiframework.AnyDecimalDigitsKeyListener;
import se.itmo.imf.equsolve.uiframework.GraphView;

public class ApproximationFragment extends Fragment {
    private static final int ROWS_COUNT = 12;

    private FragmentTabularBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var mct = new MaterialContainerTransform();
        mct.setStartContainerColor(requireContext().getColor(R.color.md_theme_background));
        mct.setEndContainerColor(requireContext().getColor(R.color.md_theme_background));
        mct.setDuration(650);
        setSharedElementEnterTransition(mct);

        setExitTransition(new Hold());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentTabularBinding.inflate(inflater, container, false);

        binding.setTitle("Approximate");
        binding.setSubtitle("Find the best approximation");

        LinearLayout xColumn = binding.xColumn;
        LinearLayout yColumn = binding.yColumn;
        for (int i = 0; i < ROWS_COUNT; i++) {
            xColumn.addView(newTextField(xColumn));
            yColumn.addView(newTextField(yColumn));
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.solverTopBar.setNavigationOnClickListener((View v) -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.buttonRun.setOnClickListener((View v) -> {
            TableFunction func = functionFromInputs();
            if (func.numPoints() < 8 || func.numPoints() > 12) {
                showError("Please enter between 8 and 12 points");
                return;
            }

            var approximator = new Approximator(func);

            StringBuilder resultText = new StringBuilder("The best approximation is:\n" + approximator.getBest().describe() + "\n");
            resultText.append("Others are:\n");
            for (Approximation approximation : approximator.getAll()) {
                if (approximation != approximator.getBest()) {
                    resultText.append(approximation.describe()).append("\n");
                }
            }

            var content = getLayoutInflater().inflate(R.layout.fragment_results, null);
            ((TextView) content.findViewById(R.id.results_text)).setText(resultText.toString());

            var graphView = (GraphView) content.findViewById(R.id.results_graph);
            graphView.reloadAndRun(() -> {
                for (var approximation: approximator.getAll()) {
                    if (approximation != approximator.getBest()) {
                        graphView.plotFunction(approximation.getJessieCode(), requireContext().getColor(R.color.md_theme_secondaryFixedDim));
                    }
                }
                graphView.plotFunction(approximator.getBest().getJessieCode(), requireContext().getColor(R.color.md_theme_tertiary_mediumContrast));

                double xMin = Arrays.stream(func.points()).mapToDouble(p -> p.x()).min().getAsDouble();
                double xMax = Arrays.stream(func.points()).mapToDouble(p -> p.x()).max().getAsDouble();
                double yMin = Arrays.stream(func.points()).mapToDouble(p -> p.y()).min().getAsDouble();
                double yMax = Arrays.stream(func.points()).mapToDouble(p -> p.y()).max().getAsDouble();
                double r = 1.1 * Math.max(xMax - xMin, yMax - yMin);
                graphView.zoomOxy((xMin + xMax) / 2, (yMin + yMax) / 2, r);
            });

            var dialog = new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.baseline_check_24)
                    .setTitle("Results")
                    .setView(content)
                    .create();
            showDialog(dialog);
        });

        binding.buttonClear.setOnClickListener((View v) -> clearInputs());
        binding.buttonImport.setOnClickListener((View v) -> {

        });
    }

    private TableFunction functionFromInputs() {
        List<TableFunction.Point> pointList = new ArrayList<>();

        for (int i = 1; i <= ROWS_COUNT; i++) {
            Double x = textInputToDouble((TextInputLayout) binding.xColumn.getChildAt(i));
            Double y = textInputToDouble((TextInputLayout) binding.yColumn.getChildAt(i));

            if (x == null || y == null) {
                continue;
            }

            pointList.add(new TableFunction.Point(x, y));
        }

        return new TableFunction(pointList.stream().toArray(TableFunction.Point[]::new));
    }

    private void clearInputs() {
        for (int i = 1; i <= ROWS_COUNT; i++) {
            ((TextInputLayout)binding.xColumn.getChildAt(i)).getEditText().setText("");
            ((TextInputLayout)binding.yColumn.getChildAt(i)).getEditText().setText("");
        }
    }

    private Double textInputToDouble(TextInputLayout textInputLayout) {
        EditText et = textInputLayout.getEditText();
        if (et.getText() == null) {
            return null;
        }

        String text = et.getText().toString();
        try {
            return Double.valueOf(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private View newTextField(ViewGroup container) {
        var textInputLayout = (TextInputLayout) getLayoutInflater()
                .inflate(R.layout.view_textfield, container, false);

        var layoutParams = textInputLayout.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        textInputLayout.setLayoutParams(layoutParams);

        EditText et = textInputLayout.getEditText();

        et.setKeyListener(new AnyDecimalDigitsKeyListener(true, true));
        et.setRawInputType(EditorInfo.TYPE_CLASS_NUMBER);

        return textInputLayout;
    }

    protected void showError(String message) {
        showDialog("Error", message, true);
    }

    private void showDialog(String title, String message, boolean isError) {
        var dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(isError ? R.drawable.baseline_highlight_off_24 : R.drawable.baseline_check_24)
                .create();
        showDialog(dialog);
    }

    private void showDialog(AlertDialog dialog) {
        var statusBarColor = requireActivity().getWindow().getStatusBarColor();
        var window = dialog.getWindow();

        // Doesn't work
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(statusBarColor);

        var params = window.getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.y = 20;
        dialog.show();
    }
}
