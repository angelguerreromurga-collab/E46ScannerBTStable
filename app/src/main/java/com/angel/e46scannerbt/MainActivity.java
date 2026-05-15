package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private TextView outText;
    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;
    private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        Button run = new Button(this);
        run.setText("Conectar ELM327 y leer");
        outText = new TextView(this);
        outText.setTextSize(14);
        outText.setPadding(20,20,20,20);
        ScrollView sv = new ScrollView(this);
        sv.addView(outText);
        root.addView(run);
        root.addView(sv, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        add("E46 Scanner BT v0.2");
        add("Solo lectura. Empareja antes el ELM327 en Android.");
        run.setOnClickListener(v -> new Thread(this::work).start());
    }

    private void work() {
        try {
            if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 46));
                add("Acepta permisos y pulsa otra vez.");
                return;
            }
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) { add("ERROR: sin Bluetooth"); return; }
            if (!adapter.isEnabled()) { add("ERROR: activa Bluetooth"); return; }
            BluetoothDevice dev = choose(adapter.getBondedDevices());
            if (dev == null) { add("ERROR: no hay ELM emparejado"); return; }
            add("Conectando: " + dev.getName());
            socket = dev.createRfcommSocketToServiceRecord(SPP);
            socket.connect();
            input = socket.getInputStream();
            output = socket.getOutputStream();
            add("Conectado");
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
            send("010C",900);
            send("0105",900);
            send("010B",900);
            send("0110",900);
            add("FIN. Haz captura completa y mandamela.");
        } catch (Exception e) {
            add("ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private BluetoothDevice choose(Set<BluetoothDevice> ds) {
        BluetoothDevice first = null;
        for (BluetoothDevice d: ds) {
            if (first == null) first = d;
            String n = d.getName()==null ? "" : d.getName().toLowerCase();
            if (n.contains("obd") || n.contains("elm")) return d;
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
        long end = System.currentTimeMillis() + 1000;
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

    private void add(String s) { runOnUiThread(() -> outText.append(s + "\n\n")); }
}
