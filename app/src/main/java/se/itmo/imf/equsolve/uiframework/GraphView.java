package se.itmo.imf.equsolve.uiframework;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GraphView extends WebView {
    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void init() {
        setOnTouchListener((View v, MotionEvent event) -> true);

        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        loadUrl("file:///android_asset/graph.html");

        setBackgroundColor(Color.TRANSPARENT);
    }

    public void plotFunction(String definition, int lineColor) {
        String scriptTemplate = """
                (function() {
                    board.create('functiongraph', [board.jc.snippet('[DEF]', true, 'x', true)], {
                        strokeColor: '[COL]',
                        strokeWidth: 2,
                    });
                })();
                """;
        String hexColor = String.format("#%06X", (0xFFFFFF & lineColor));
        String script = scriptTemplate
                .replace("[DEF]", definition)
                .replace("[COL]", hexColor);
        evaluateJavascript(script, null);
    }

    public void addText(String latex, int textColor) {
        String scriptTemplate = """
                (function() {
                    let equationDiv = document.createElement('div');
                    equationDiv.innerHTML = '\\([LATEX]\\)';
                    equationDiv.style.color = '[COLOR]'; // Default to black if color not provided
                    document.getElementById('equations').appendChild(equationDiv);
                    MathJax.typesetPromise();
                })();
                    """;
        String hexColor = String.format("#%06X", (0xFFFFFF & textColor));
        String script = scriptTemplate
                .replace("[LATEX]", latex)
                .replace("[COLOR]", hexColor);
        evaluateJavascript(script.replace("\\", "\\\\"), null);
    }

    public void addText(String latex) {
        String scriptTemplate = """
                                (function() {
                                    let equationDiv = document.createElement('div');
                                    equationDiv.innerHTML = '\\([LATEX]\\)';
                                    document.getElementById('equations').appendChild(equationDiv);
                //                    MathJax.typesetPromise();
                                })();
                                    """;
        String script = scriptTemplate
                .replace("[LATEX]", latex);
        evaluateJavascript(script.replace("\\", "\\\\"), null);
    }

    public void zoomOx(double x1, double x2) {
        double slack = (x2 - x1) * 0.0625;
        String scriptTemplate = """
                (function() {
                    board.setBoundingBox([%f, +5, %f, -5]);
                    board.update();
                })();
                """;
        String script = String.format(scriptTemplate, x1 - slack, x2 + slack);
        evaluateJavascript(script, null);
    }

    public void zoomOxy(double x, double y, double r) {
        String scriptTemplate = """
                (function() {
                    board.setBoundingBox([%f, %f, %f, %f]);
                    board.update();
                })();
                """;
        String script = String.format(scriptTemplate, x - r, y + r, x + r, y - r);
        evaluateJavascript(script, null);
    }

    public void plotImplicit(String definition, int lineColor) {
        String scriptTemplate = """
                (function() {
                    board.create('implicitcurve', [board.jc.snippet('[DEF]', true, 'x,y', true)], {
                        strokeColor: '[COL]',
                        strokeWidth: 2,
                    });
                })();
                """;
        String hexColor = String.format("#%06X", (0xFFFFFF & lineColor));
        String script = scriptTemplate
                .replace("[DEF]", definition)
                .replace("[COL]", hexColor);
        evaluateJavascript(script, null);
    }

    public void reloadAndRun(Runnable actions) {
        reload();
        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (getProgress() == 100) {
                    actions.run();
                }
            }
        });
    }
}
