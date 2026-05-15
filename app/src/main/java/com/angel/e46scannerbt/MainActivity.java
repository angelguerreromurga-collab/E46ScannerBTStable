package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private TextView status;
    private TextView outText;
    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;
    private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 70, 28, 24);
        root.setBackgroundColor(Color.rgb(5,7,10));

        TextView title = label("E46 SCANNER BT", 24, true);
        TextView sub = label("v0.3 READ ONLY · ELM327 RAW", 14, false);
        status = label("Estado: listo. Empareja el ELM327 en Android.", 15, false);
        status.setTextColor(Color.rgb(130,190,255));

        Button bt = button("1 · CONECTAR ELM327 Y LEER TODO");
        Button clear = button("LIMPIAR LOG");

        outText = label("", 13, false);
        outText.setTextColor(Color.rgb(220,230,240));
        outText.setPadding(18,18,18,18);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.rgb(14,18,26));
        sv.addView(outText);

        root.addView(title);
        root.addView(sub);
        root.addView(status);
        root.addView(bt);
        root.addView(clear);
        root.addView(sv, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);

        add("MODO SEGURO: no codifica, no borra fallos, no escribe modulos.");
        add("Pulsa el boton grande. Al terminar, manda captura del log completo.");

        bt.setOnClickListener(v -> new Thread(this::work).start());
        clear.setOnClickListener(v -> outText.setText(""));
    }

    private TextView label(String text, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(Color.WHITE);
        t.setPadding(0,8,0,10);
        if (bold) t.setGravity(Gravity.CENTER_HORIZONTAL);
        return t;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        b.setAllCaps(false);
        return b;
    }

    private void work() {
        try {
            if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 46));
                stat("Permiso solicitado. Acepta y pulsa otra vez.");
                return;
            }
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) { stat("ERROR: telefono sin Bluetooth"); return; }
            if (!adapter.isEnabled()) { stat("ERROR: activa Bluetooth"); return; }
            BluetoothDevice dev = choose(adapter.getBondedDevices());
            if (dev == null) { stat("ERROR: no hay ELM emparejado"); return; }
            stat("Conectando a " + dev.getName());
            try { if (socket != null) socket.close(); } catch(Exception ignored) {}
            socket = dev.createRfcommSocketToServiceRecord(SPP);
            socket.connect();
            input = socket.getInputStream();
            output = socket.getOutputStream();
            stat("Conectado. Leyendo RAW...");
            add("BT OK: " + dev.getName() + " / " + dev.getAddress());
            send("ATZ",1400);
            send("ATE0",500);
            send("ATL0",500);
            send("ATS0",500);
            send("ATH1",500);
            send("ATI",800);
            send("ATRV",800);
            send("ATSP0",1000);
            send("0100",1400);
            send("ATDP",800);
            send("ATDPN",800);
            send("010C",900);
            send("0105",900);
            send("010D",900);
            send("010B",900);
            send("010F",900);
            send("0110",900);
            send("03",1500);
            stat("Lectura terminada. Manda capturas.");
            add("FIN. Captura desde arriba hasta abajo.");
        } catch (Exception e) {
            stat("ERROR");
            add("ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private BluetoothDevice choose(Set<BluetoothDevice> ds) {
        BluetoothDevice first = null;
        for (BluetoothDevice d: ds) {
            if (first == null) first = d;
            String n = d.getName()==null ? "" : d.getName().toLowerCase();
            if (n.contains("obd") || n.contains("elm") || n.contains("vlink") || n.contains("icar")) return d;
        }
        return first;
    }

    private void send(String c, long wait) throws Exception {
        output.write((c + "\r").getBytes("US-ASCII"));
        output.flush();
        Thread.sleep(wait);
        add("> " + c + "\n" + read());
    }

    private String read() throws Exception {
        byte[] b = new byte[512];
        StringBuilder s = new StringBuilder();
        long end = System.currentTimeMillis() + 1100;
        while (System.currentTimeMillis() < end) {
            while (input.available() > 0) {
                int n = input.read(b);
                if (n > 0) s.append(new String(b,0,n,"US-ASCII"));
            }
            if (s.toString().contains(">")) break;
            Thread.sleep(50);
        }
        String r = s.toString().replace('\r',' ').replace('\n',' ').trim();
        return r.length()==0 ? "SIN RESPUESTA" : r;
    }

    private void stat(String s) { runOnUiThread(() -> status.setText("Estado: " + s)); }
    private void add(String s) { runOnUiThread(() -> outText.append(s + "\n\n")); }
}
