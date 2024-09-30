package se.itmo.imf.equsolve.uiframework;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.Hold;

import java.util.HashMap;
import java.util.Locale;
import java.util.function.Consumer;

import se.itmo.imf.equsolve.R;
import se.itmo.imf.equsolve.databinding.FragmentSolverBinding;

public abstract class SolverFragment extends Fragment {
    public final String TAG = "SolverFragment[" + title() + "]";

    private FragmentSolverBinding binding;

    private final ProblemAdapter problemAdapterInstance = problemAdapter();

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSolverBinding.inflate(inflater, container, false);

        initView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        ((ViewGroup) view.getParent()).getViewTreeObserver()
                .addOnPreDrawListener(() -> {
                    startPostponedEnterTransition();
                    return true;
                });

        binding.setTitle(title());
        binding.setSubtitle(subtitle());

        binding.solverTopBar.setNavigationOnClickListener((View v) -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        var pager = binding.problemsPager;
        pager.setClipToPadding(false);
        pager.setClipChildren(false);
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(new ProblemAdapterWrapper());
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                problemAdapterInstance.onPageChange(position);
            }
        });

        binding.solveButton.setOnClickListener((View v) -> onClickSolve());
    }

    private class ProblemAdapterWrapper extends RecyclerView.Adapter<ProblemViewHolder> {
        ProblemAdapter pa = problemAdapterInstance;

        @NonNull
        @Override
        public ProblemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = requireActivity().getLayoutInflater()
                    .inflate(R.layout.item_problem_card, parent, false);
            return new ProblemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProblemViewHolder holder, int position) {
            pa.initView(holder.gv, position);
        }

        @Override
        public int getItemCount() {
            return pa.count();
        }
    }

    private static class ProblemViewHolder extends RecyclerView.ViewHolder {
        GraphView gv;

        public ProblemViewHolder(@NonNull View itemView) {
            super(itemView);
            gv = itemView.findViewById(R.id.graphView);
        }
    }

    protected void showSuccess(String title, String message) {
        showDialog(title, message, false);
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

    protected void showDialog(AlertDialog dialog) {
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

    protected <N extends Number> void addField(String hint, boolean sign, boolean decimal, Consumer<N> onValueChange) {
        var textInputLayout = (TextInputLayout) getLayoutInflater()
                .inflate(R.layout.view_textfield, binding.textFieldsHolder, false);
        textInputLayout.setHint(hint);

        EditText et = textInputLayout.getEditText();

        et.setKeyListener(new AnyDecimalDigitsKeyListener(sign, decimal));
        et.setRawInputType(EditorInfo.TYPE_CLASS_NUMBER);
        if (decimal) {
            et.addTextChangedListener((TextWatchers.CallAfterChanged) (s) -> {
                try {
                    onValueChange.accept((N) Double.valueOf(s.replace(",", ".")));
                } catch (NumberFormatException e) {
                    onValueChange.accept(null);
                }
            });
        } else {
            et.addTextChangedListener((TextWatchers.CallAfterChanged) (s) -> {
                try {
                    onValueChange.accept((N) Integer.valueOf(s));
                } catch (NumberFormatException e) {
                    onValueChange.accept(null);
                }
            });
        }

        binding.textFieldsHolder.addView(textInputLayout);
    }

    protected <T> void useMethodSelector(String[] names, T[] sentinels, Consumer<T> onValueChange) {
        if (names.length != sentinels.length) {
            throw new IllegalArgumentException("names.length != sentinels.length");
        }
        if (names.length == 0) {
            throw new IllegalArgumentException("provide at least one name and sentinel");
        }

        binding.selectMethodLayout.setVisibility(View.VISIBLE);
        binding.selectMethodLayout.getParent().requestLayout();

        HashMap<String, T> lookup = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            lookup.put(names[i], sentinels[i]);
        }

        binding.selectMethod.setSimpleItems(names);
        binding.selectMethod.addTextChangedListener(
                (TextWatchers.CallAfterChanged) (s) -> onValueChange.accept(lookup.get(s)));
    }

    protected void setButtonEnabled(boolean enabled) {
        binding.solveButton.setEnabled(enabled);
    }

    protected Locale getLocale() {
        return requireContext().getResources().getConfiguration().getLocales().get(0);
    }

    protected GraphView getCurrentGraphView() {
        int pos = binding.problemsPager.getCurrentItem();
        var rv = (RecyclerView) binding.problemsPager.getChildAt(0);
        var layout = (FrameLayout) rv.getLayoutManager().findViewByPosition(pos);
        var card = (CardView) layout.getChildAt(0);
        return (GraphView) card.getChildAt(0);
    }

    @NonNull
    protected abstract String title();

    @NonNull
    protected abstract String subtitle();

    @NonNull
    protected abstract ProblemAdapter problemAdapter();

    protected abstract void initView();

    protected abstract void onClickSolve();

    protected interface ProblemAdapter {
        int count();

        void initView(GraphView gv, int index);

        void onPageChange(int index);
    }
}