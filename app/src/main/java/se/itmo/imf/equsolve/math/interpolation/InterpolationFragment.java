package se.itmo.imf.equsolve.math.interpolation;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

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
import java.util.Locale;

import se.itmo.imf.equsolve.R;
import se.itmo.imf.equsolve.databinding.FragmentTabularBinding;
import se.itmo.imf.equsolve.databinding.ViewFunctionFillBinding;
import se.itmo.imf.equsolve.uiframework.AnyDecimalDigitsKeyListener;

public class InterpolationFragment extends Fragment {
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

        binding.setTitle("Interpolate");
        binding.setSubtitle("Find a value between known points");

        LinearLayout xColumn = binding.xColumn;
        LinearLayout yColumn = binding.yColumn;
        for (int i = 0; i < ROWS_COUNT; i++) {
            xColumn.addView(newTextField(xColumn));
            yColumn.addView(newTextField(yColumn));
        }

        binding.buttonFunction.setVisibility(View.VISIBLE);
        binding.upperRow.setVisibility(View.VISIBLE);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.solverTopBar.setNavigationOnClickListener((View v) -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.buttonFunction.setOnClickListener((View v) -> {
            var formBinding = ViewFunctionFillBinding.inflate(getLayoutInflater(), null, false);
            setInputType(formBinding.fillA, true, true);
            setInputType(formBinding.fillB, true, true);
            setInputType(formBinding.fillN, false, false);
            formBinding.selectFunction.setSimpleItems(Arrays.stream(Function.INSTANCES).map(Function::expr).toArray(String[]::new));

            var dialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Use a function")
                    .setIcon(R.drawable.baseline_ssid_chart_24)
                    .setView(formBinding.getRoot())
                    .setPositiveButton("OK", (_dialog, which) -> {
                        double a = textInputToDouble(formBinding.fillA);
                        double b = textInputToDouble(formBinding.fillB);
                        int n = (int) (double) textInputToDouble(formBinding.fillN);
                        if (n < 2) {
                            return;
                        }

                        String selectedExpr = formBinding.selectFunction.getText().toString();
                        if (selectedExpr.isEmpty()) {
                            return;
                        }
                        Function selected = Arrays.stream(Function.INSTANCES).filter(f -> f.expr().equals(selectedExpr)).findFirst().get();
                        TableFunction func = selected.evalToTable(a, b, n);
                        fillFunction(func);
                    })
                    .create();
            showDialog(dialog);
        });

        binding.selectMethod.setSimpleItems(new String[]{"Lagrange", "Newton", "Gauss"});

        binding.buttonRun.setOnClickListener((View v) -> {
            TableFunction func = functionFromInputs();
            Interpolation interpolation = switch (binding.selectMethod.getText().toString()) {
                case "Lagrange" -> new Interpolations.Lagrange(func);
                case "Newton" -> new Interpolations.Newton(func);
                case "Gauss" -> new Interpolations.Gauss(func);
                default -> null;
            };
            if (interpolation == null) {
                return;
            }
            if (textInputToDouble(binding.inputX) == null) {
                return;
            }

            double x = textInputToDouble(binding.inputX);
            double value;
            try {
                value = interpolation.at(x);
            } catch (RuntimeException e) {
                showError(e.getMessage());
                return;
            }

            String result = "f(x) = " + value;
            if (!interpolation.willBePreciseAt(x)) {
                result += "\nResult not precise, choose another x or nodes";
            }

            showDialog("Result", result, false);
        });

        binding.buttonClear.setOnClickListener((View v) -> clearInputs());
    }

    private void fillFunction(TableFunction func) {
        clearInputs();
        for (int i = 0; i < func.numPoints(); i++) {
            TableFunction.Point p = func.points()[i];
            ((TextInputLayout) binding.xColumn.getChildAt(i+1)).getEditText().setText(String.valueOf(p.x()));
            ((TextInputLayout) binding.yColumn.getChildAt(i+1)).getEditText().setText(String.valueOf(p.y()));
        }
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

    private static Double textInputToDouble(TextInputLayout textInputLayout) {
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

        setInputType(textInputLayout, true, true);

        return textInputLayout;
    }

    private void setInputType(TextInputLayout textInputLayout, boolean sign, boolean decimal) {
        EditText et = textInputLayout.getEditText();

        et.setKeyListener(new AnyDecimalDigitsKeyListener(sign, decimal));
        et.setRawInputType(EditorInfo.TYPE_CLASS_NUMBER);
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
