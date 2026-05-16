package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class ElmClient {
    public interface LogSink { void log(String text); }

    private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final Activity activity;
    private final LogSink sink;
    private final ObdSnapshot snapshot = new ObdSnapshot();
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;
    private BluetoothDevice device;

    public ElmClient(Activity activity, LogSink sink) {
        this.activity = activity;
        this.sink = sink;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && in != null && out != null;
    }

    public ObdSnapshot snapshot() { return snapshot; }
    public String liveText() { return snapshot.liveText(); }
    public String diagnosticText() { return DiagnosticFormatter.format(snapshot); }

    public String connect() {
        try {
            if (!checkPermission()) return "PERMISO BLUETOOTH SOLICITADO";
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) return "ERROR: este dispositivo no tiene Bluetooth";
            Set<BluetoothDevice> bonded = adapter.getBondedDevices();
            device = null;
            for (BluetoothDevice d : bonded) {
                String name = d.getName() == null ? "" : d.getName().toLowerCase();
                if (device == null || name.contains("obd") || name.contains("elm") || name.contains("vlink") || name.contains("icar")) device = d;
            }
            if (device == null) return "ERROR: empareja primero el ELM327";
            close();
            socket = device.createRfcommSocketToServiceRecord(SPP);
            socket.connect();
            in = socket.getInputStream();
            out = socket.getOutputStream();
            return "BT OK: " + device.getName() + " / " + device.getAddress();
        } catch (Exception e) {
            close();
            return "ERROR BT: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }

    public String initElm() {
        return run("INIT ELM", new String[]{"ATZ", "ATE0", "ATL0", "ATS0", "ATH1", "ATI", "ATRV", "ATSP0", "ATDP", "ATDPN"});
    }

    public String initBmwKwp() {
        return run("INIT BMW KWP", new String[]{"ATSP3", "ATIB10", "ATSH 68 6A F1", "ATSW00", "ATST32", "ATAT1", "ATDP", "ATDPN"});
    }

    public String readMotor() {
        return run("MOTOR", new String[]{"0120", "010C", "0105", "010D", "010B", "010F", "0110"});
    }

    public String readDtc() { return run("DTC", new String[]{"03", "07"}); }
    public String readProtocol() { return run("PROTOCOLO", new String[]{"ATDP", "ATDPN", "0100"}); }

    public String backupSafe() {
        return run("BACKUP SEGURO READ ONLY", new String[]{"ATI", "ATRV", "ATDP", "ATDPN", "0100", "0120", "0140", "03", "07", "0A"});
    }

    public String readModule(String moduleKey) {
        E46ModuleProfile profile = E46ModuleRegistry.get(moduleKey);
        if (profile == null) return "ERROR: modulo no definido: " + moduleKey;
        StringBuilder out = new StringBuilder();
        out.append("===== MODULO BMW READ ONLY =====\n");
        out.append(profile.describe()).append("\n\n");
        out.append(initBmwKwp()).append("\n");
        out.append(run(profile.name + " READ ONLY", withHeader(profile.header, profile.safeReadCommands)));
        out.append("\nFIN READ ONLY: no se ha escrito ni borrado nada.\n");
        return out.toString();
    }

    public String scanModuleHeaders(String moduleKey) {
        E46ModuleProfile profile = E46ModuleRegistry.get(moduleKey);
        if (profile == null) return "ERROR: modulo no definido: " + moduleKey;
        StringBuilder out = new StringBuilder();
        out.append("===== SCAN CABECERAS BMW READ ONLY =====\n");
        out.append(profile.name).append("\n");
        out.append("Objetivo: encontrar cabecera que responda sin escribir.\n\n");
        out.append(initBmwKwp()).append("\n");
        for (String header : profile.headerVariants) {
            out.append("--- CANDIDATO ").append(header).append(" ---\n");
            out.append(run(profile.name + " HEADER SCAN", withHeader(header, profile.safeReadCommands)));
            out.append("\n");
        }
        out.append("FIN SCAN: no se ha escrito ni borrado nada.\n");
        return out.toString();
    }

    private String[] withHeader(String header, String[] commands) {
        String[] out = new String[commands.length + 1];
        out[0] = header;
        System.arraycopy(commands, 0, out, 1, commands.length);
        return out;
    }

    public String buildWritePlan(String moduleKey, String featureName) {
        E46ModuleProfile profile = E46ModuleRegistry.get(moduleKey);
        if (profile == null) return "WRITE PLAN ERROR: modulo no definido: " + moduleKey;
        StringBuilder out = new StringBuilder();
        out.append("===== WRITE PLAN BLOQUEADO =====\n");
        out.append("Modulo: ").append(profile.name).append("\n");
        out.append("Funcion: ").append(featureName).append("\n");
        out.append("Header previsto: ").append(profile.header).append("\n");
        out.append("Estado: SIMULACION / NO TRANSMITIDO\n");
        out.append("Motivo: escritura real bloqueada hasta backup verificable, tension estable y lectura de modulo correcta.\n");
        out.append("Servicios prohibidos por politica: 2E, 3B, 14, 30, 31, 34, 36, 37.\n");
        return out.toString();
    }

    public String run(String title, String[] commands) {
        if (!SafeCommandBatch.isAllowed(commands)) {
            return "BLOQUEADO POR SEGURIDAD: " + title + "\n" + SafeCommandBatch.report(commands) + "\n" + SafeCommandBatch.firstBlockedReason(commands);
        }
        if (!isConnected()) {
            String c = connect();
            if (!isConnected()) return c;
        }
        StringBuilder outText = new StringBuilder();
        outText.append("===== ").append(title).append(" =====\n");
        try {
            for (String c : commands) outText.append(send(c));
        } catch (Exception e) {
            close();
            outText.append("ERROR ").append(title).append(": ").append(e.getClass().getSimpleName()).append(" - ").append(e.getMessage()).append("\n");
        }
        return outText.toString();
    }

    private String send(String cmd) throws Exception {
        out.write((cmd + "\r").getBytes("US-ASCII"));
        out.flush();
        Thread.sleep(cmd.equals("ATZ") ? 1600 : 850);
        String response = read();
        ObdParser.parseInto(snapshot, cmd + "\n" + response);
        return "> " + cmd + "\n" + response + "\n\n";
    }

    private String read() throws Exception {
        byte[] buffer = new byte[512];
        StringBuilder s = new StringBuilder();
        long end = System.currentTimeMillis() + 1800;
        while (System.currentTimeMillis() < end) {
            while (in.available() > 0) {
                int n = in.read(buffer);
                if (n > 0) s.append(new String(buffer, 0, n, "US-ASCII"));
            }
            if (s.toString().contains(">")) break;
            Thread.sleep(60);
        }
        String r = s.toString().replace('\r', ' ').replace('\n', ' ').trim();
        return r.length() == 0 ? "SIN RESPUESTA" : r;
    }

    public void close() {
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        socket = null;
        in = null;
        out = null;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 31 && activity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            activity.runOnUiThread(() -> activity.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 46));
            return false;
        }
        return true;
    }
}
