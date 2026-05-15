package com.angel.e46scannerbt;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity {
    private TextView log;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        Button button = new Button(this);
        button.setText("E46 Scanner BT v0.1 - Solo lectura");
        log = new TextView(this);
        log.setTextSize(15);
        log.setPadding(24, 24, 24, 24);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(log);
        root.addView(button);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        write("E46 Scanner BT v0.1");
        write("APK inicial segura: solo lectura.");
        write("Coding Lab bloqueado.");
        write("Siguiente build: Bluetooth ELM327 + comandos AT.");
        button.setOnClickListener(v -> {
            write("Prueba UI OK.");
            write("Esta build confirma instalacion y arranque en Android.");
        });
    }

    private void write(String s) {
        log.append("- " + s + "\n");
    }
}
