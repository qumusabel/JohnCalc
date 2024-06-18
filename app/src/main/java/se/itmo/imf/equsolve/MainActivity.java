package se.itmo.imf.equsolve;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}