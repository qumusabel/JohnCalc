package se.itmo.imf.equsolve.uiframework;

import android.text.Editable;
import android.text.TextWatcher;

public class TextWatchers {
    @FunctionalInterface
    public interface CallAfterChanged extends TextWatcher {
        @Override
        default void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        default void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        default void afterTextChanged(Editable s) {
            onNewText(s.toString());
        }

        void onNewText(String s);
    }
}
