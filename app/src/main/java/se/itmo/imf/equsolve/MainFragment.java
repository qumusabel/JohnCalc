package se.itmo.imf.equsolve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.Hold;

import se.itmo.imf.equsolve.databinding.FragmentMainBinding;
import se.itmo.imf.equsolve.math.approximation.ApproximationFragment;
import se.itmo.imf.equsolve.math.differential_equation.DifferentialEquationFragment;
import se.itmo.imf.equsolve.math.integral.IntegralFragment;
import se.itmo.imf.equsolve.math.interpolation.InterpolationFragment;
import se.itmo.imf.equsolve.math.single_equation.SingleFragment2;
import se.itmo.imf.equsolve.math.two_equations.SystemFragment;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitTransition(new Hold());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
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

        binding.menuSingle.menuCard.setOnClickListener(v -> openFragment(new SingleFragment2(), v));
        binding.menuSystem.menuCard.setOnClickListener(v -> openFragment(new SystemFragment(), v));
        binding.menuIntegrate.menuCard.setOnClickListener(v -> openFragment(new IntegralFragment(), v));
        binding.menuApproximate.menuCard.setOnClickListener(v -> openFragment(new ApproximationFragment(), v));
        binding.menuInterpolate.menuCard.setOnClickListener(v -> openFragment(new InterpolationFragment(), v));
        binding.menuDifferential.menuCard.setOnClickListener(v -> openFragment(new DifferentialEquationFragment(), v));
    }

    private void openFragment(Fragment fragment, View sharedElement) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(sharedElement, "solver_container")
                .replace(R.id.fragment_container, fragment, null)
                .addToBackStack(null)
                .commit();
    }
}
