package se.itmo.imf.equsolve.uiframework;

import android.annotation.SuppressLint;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.NumberKeyListener;

import androidx.annotation.NonNull;

public class AnyDecimalDigitsKeyListener extends NumberKeyListener {
    private final boolean sign, decimal;
    private final char[] accepted;

    @SuppressLint("SoonBlockedPrivateApi")
    public AnyDecimalDigitsKeyListener(boolean sign, boolean decimal) {
        super();
        this.sign = sign;
        this.decimal = decimal;
        this.accepted = ("0123456789" + (sign ? "+-" : "") + (decimal ? ",." : "") ).toCharArray();
    }

    private boolean isSignChar(char c) {
        return c == '-' || c == '+';
    }

    private boolean isDecimalPointChar(char c) {
        return c == ',' || c == '.';
    }


    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);

        if (this.sign == false && this.decimal == false) {
            return out;
        }

        if (out != null) {
            source = out;
            start = 0;
            end = out.length();
        }

        int sign = -1;
        int decimal = -1;
        int dlen = dest.length();

        /*
         * Find out if the existing text has a sign or decimal point characters.
         */

        for (int i = 0; i < dstart; i++) {
            char c = dest.charAt(i);

            if (isSignChar(c)) {
                sign = i;
            } else if (isDecimalPointChar(c)) {
                decimal = i;
            }
        }
        for (int i = dend; i < dlen; i++) {
            char c = dest.charAt(i);

            if (isSignChar(c)) {
                return "";    // Nothing can be inserted in front of a sign character.
            } else if (isDecimalPointChar(c)) {
                decimal = i;
            }
        }

        /*
         * If it does, we must strip them out from the source.
         * In addition, a sign character must be the very first character,
         * and nothing can be inserted before an existing sign character.
         * Go in reverse order so the offsets are stable.
         */

        SpannableStringBuilder stripped = null;

        for (int i = end - 1; i >= start; i--) {
            char c = source.charAt(i);
            boolean strip = false;

            if (isSignChar(c)) {
                if (i != start || dstart != 0) {
                    strip = true;
                } else if (sign >= 0) {
                    strip = true;
                } else {
                    sign = i;
                }
            } else if (isDecimalPointChar(c)) {
                if (decimal >= 0) {
                    strip = true;
                } else {
                    decimal = i;
                }
            }

            if (strip) {
                if (end == start + 1) {
                    return "";  // Only one character, and it was stripped.
                }

                if (stripped == null) {
                    stripped = new SpannableStringBuilder(source, start, end);
                }

                stripped.delete(i - start, i + 1 - start);
            }
        }

        if (stripped != null) {
            return stripped;
        } else if (out != null) {
            return out;
        } else {
            return null;
        }
    }

    @Override
    public int getInputType() {
        int contentType = InputType.TYPE_CLASS_NUMBER;
        if (sign) {
            contentType |= InputType.TYPE_NUMBER_FLAG_SIGNED;
        }
        if (decimal) {
            contentType |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
        }
        return contentType;
    }

    @NonNull
    @Override
    protected char[] getAcceptedChars() {
        return accepted;
    }
}
