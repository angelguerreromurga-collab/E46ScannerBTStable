package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, body, bottomBar;
    View statusStrip;
    TextView statusText, liveCard, testsCard, logText;
    BluetoothSocket socket;
    InputStream in;
    OutputStream out;
    BluetoothDevice device;
    StringBuilder session = new StringBuilder();

    String volts = "--", proto = "--", dtc = "--", test = "LISTO", vin = "pendiente";
    int rpm = -1, temp = -999, map = -1, speed = -1, iat = -999;
    double maf = -1;
    boolean bt = false, elm = false, protocol = false, engine = false, dtcOk = false, backup = false;

    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG = Color.rgb(1, 5, 11);
    final int CARD = Color.rgb(8, 15, 25);
    final int LINE = Color.rgb(26, 44, 66);
    final int TXT = Color.WHITE;
    final int MUTED = Color.rgb(150, 160, 174);
    final int BLUE = Color.rgb(0, 122, 255);
    final int BLUE2 = Color.rgb(0, 90, 220);
    final int GREEN = Color.rgb(35, 220, 115);
    final int RED = Color.rgb(255, 65, 75);
    final int AMBER = Color.rgb(245, 180, 45);

    public void onCreate(Bundle b) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        stampSession();
        showHome();
    }

    void stampSession() {
        session.append("BMW E46 SCANNER SESSION\n")
                .append(now()).append("\n")
                .append("Car: BMW E46 coupe 320d/320Cd M47N\n")
                .append("Mode: READ ONLY\n\n");
    }

    String now() { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()); }
    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    TextView text(String s, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setIncludeFontPadding(true);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    GradientDrawable bg(int color, int radius, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        g.setStroke(1, stroke);
        return g;
    }

    GradientDrawable gradient() {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.rgb(2, 8, 15), BG});
    }

    void shell(String title, boolean bottomActions, int activeTab) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(gradient());

        statusStrip = new View(this);
        statusStrip.setBackgroundColor(bt ? GREEN : RED);
        root.addView(statusStrip, new LinearLayout.LayoutParams(-1, dp(4)));

        ScrollView scroll = new ScrollView(this);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(22), dp(18), dp(22), dp(10));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        header(title, activeTab);
        if (bottomActions) bottomActions();
    }

    void header(String title, int activeTab) {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);

        TextView left = text(title.equals("Coding Lab") ? "‹" : "☰", 31, TXT, false);
        left.setGravity(Gravity.CENTER);
        left.setOnClickListener(v -> showHome());
        h.addView(left, new LinearLayout.LayoutParams(dp(42), dp(44)));

        TextView titleView = text(title, 20, TXT, false);
        titleView.setGravity(Gravity.CENTER);
        h.addView(titleView, new LinearLayout.LayoutParams(0, dp(44), 1));

        TextView car = text("▱", 25, TXT, false);
        car.setGravity(Gravity.CENTER);
        h.addView(car, new LinearLayout.LayoutParams(dp(42), dp(44)));
        body.addView(h);

        statusText = text(statusLine(), 12, bt ? GREEN : RED, false);
        statusText.setGravity(Gravity.CENTER);
        body.addView(statusText);

        if (activeTab >= 0) tabs(activeTab);
        else space(8);
    }

    String statusLine() {
        return (bt ? "CONECTADO" : "DESCONECTADO") + " · " + test + " · ELM " + (elm ? "OK" : "--") + " · " + proto + " · " + volts;
    }

    void refresh() {
        runOnUiThread(() -> {
            if (statusStrip != null) statusStrip.setBackgroundColor(bt ? GREEN : RED);
            if (statusText != null) {
                statusText.setText(statusLine());
                statusText.setTextColor(bt ? GREEN : RED);
            }
            if (liveCard != null) liveCard.setText(live());
            if (testsCard != null) testsCard.setText(tests());
        });
    }

    void space(int h) {
        Space s = new Space(this);
        body.addView(s, new LinearLayout.LayoutParams(1, dp(h)));
    }

    LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16), dp(13), dp(16), dp(13));
        l.setBackground(bg(CARD, 15, LINE));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(5), 0, dp(7));
        l.setLayoutParams(lp);
        return l;
    }

    TextView cardText(String s, int size) {
        TextView v = text(s, size, TXT, false);
        v.setPadding(dp(16), dp(12), dp(16), dp(12));
        v.setBackground(bg(CARD, 14, LINE));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(5), 0, dp(7));
        v.setLayoutParams(lp);
        return v;
    }

    TextView section(String s) {
        TextView v = text(s, 12, MUTED, false);
        v.setPadding(0, dp(19), 0, dp(8));
        return v;
    }

    void showHome() {
        shell("Coding Lab E46", false, -1);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(10), dp(4), dp(10), dp(10));
        hero.setBackground(bg(Color.rgb(4, 10, 18), 18, Color.rgb(14, 25, 40)));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, dp(4), 0, dp(8));
        hero.setLayoutParams(hp);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(6), dp(8), dp(6), 0);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(text("Bluetooth", 12, MUTED, false));
        info.addView(text(bt ? "Conectado" : "Desconectado", 16, bt ? GREEN : AMBER, true));
        info.addView(text("BMW E46 320d M47N", 16, TXT, true));
        info.addView(text("VIN: " + vin, 12, MUTED, false));
        top.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        hero.addView(top);

        ImageView img = new ImageView(this);
        img.setImageResource(getResources().getIdentifier("bmw_e46_black_coupe_hero", "drawable", getPackageName()));
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        hero.addView(img, new LinearLayout.LayoutParams(-1, dp(145)));

        liveCard = text(live(), 12, Color.rgb(220, 230, 242), false);
        liveCard.setPadding(dp(6), 0, 0, 0);
        hero.addView(liveCard);
        body.addView(hero);

        testsCard = cardText(tests(), 13);
        body.addView(testsCard);

        body.addView(section("MENÚ PRINCIPAL"));
        body.addView(menuRow("Coding Lab", "Funciones de confort y personalización", "🔧", () -> showCoding()));
        body.addView(menuRow("Backup seguro", "Guardar estado OBD/ECU antes de pruebas", "▣", () -> showBackup()));
        body.addView(menuRow("Luces", "Iluminación exterior e interior", "☼", () -> showLights()));
        body.addView(menuRow("Ventanillas", "Funciones de confort de ventanas", "▭", () -> showCoding()));
        body.addView(menuRow("Diagnóstico", "Leer errores y estado de módulos", "◎", () -> showDiag()));
        body.addView(menuRow("Logs", "Registros y sesiones guardadas", "≡", () -> showLogs()));
        bottomNav(0);
    }

    View menuRow(String title, String sub, String icon, final Runnable r) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setBackground(bg(CARD, 13, LINE));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(5), 0, dp(6));
        row.setLayoutParams(lp);

        TextView ic = text(icon, 25, BLUE, false);
        ic.setGravity(Gravity.CENTER);
        row.addView(ic, new LinearLayout.LayoutParams(dp(46), dp(48)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(text(title, 16, TXT, false));
        texts.addView(text(sub, 13, Color.rgb(180, 190, 204), false));
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 30, Color.rgb(190, 198, 208), false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(28), dp(48)));
        row.setOnClickListener(x -> r.run());
        return row;
    }

    void bottomNav(int active) {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, dp(8), 0, dp(5));
        nav.setBackgroundColor(Color.rgb(3, 8, 15));
        String[] names = {"Inicio", "Módulos", "Logs", "Ajustes"};
        Runnable[] runs = {() -> showHome(), () -> showCoding(), () -> showLogs(), () -> showInfo()};
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            TextView v = text(names[i], 13, i == active ? BLUE : MUTED, false);
            v.setGravity(Gravity.CENTER);
            v.setOnClickListener(x -> runs[idx].run());
            nav.addView(v, new LinearLayout.LayoutParams(0, dp(44), 1));
        }
        body.addView(nav, new LinearLayout.LayoutParams(-1, dp(58)));
    }

    void showCoding() {
        shell("Coding Lab", true, 1);
        body.addView(infoCard("Personaliza el comportamiento de las ventanillas y\nfunciones de confort asociadas."));
        body.addView(section("CIERRE CON MANDO"));
        body.addView(group(new View[]{
                switchRow("Cerrar ventanillas con mantener pulsado", "Cierra todas las ventanillas al mantener pulsado el botón de cerrar.", true),
                switchRow("Cerrar ventanillas traseras con mantener", "Cierra solo las ventanillas traseras al mantener pulsado el botón de cerrar.", true),
                switchRow("Doble clic para cerrar traseras", "Doble clic en el botón de cerrar para subir traseras automáticamente.", true),
                switchRow("Doble clic para abrir traseras", "Doble clic en el botón de abrir para bajar traseras automáticamente.", false)
        }));
        body.addView(section("APERTURA CON MANDO"));
        body.addView(group(new View[]{
                switchRow("Abrir ventanillas con mantener pulsado", "Abre todas las ventanillas al mantener pulsado el botón de abrir.", true),
                switchRow("Doble clic para abrir traseras", "Doble clic en el botón de abrir para bajar traseras automáticamente.", true)
        }));
        body.addView(section("OTRAS OPCIONES"));
        body.addView(group(new View[]{
                switchRow("Bajar ventanillas al abrir la puerta", "Baja ligeramente las ventanillas al abrir la puerta del conductor.", false)
        }));
        body.addView(section("PRUEBAS Y BACKUP"));
        body.addView(action("Backup seguro READ ONLY", () -> safeBackup()));
        body.addView(action("Test completo automático", () -> fullTest()));
        body.addView(action("Compartir sesión", () -> share()));
    }

    void showLights() {
        shell("Luces", true, 0);
        body.addView(infoCard("Gestión visual de LED y check-control. Escritura bloqueada."));
        body.addView(section("LED / CHECK"));
        body.addView(group(new View[]{
                switchRow("Check frío posición LED", "Parpadeo breve al contacto.", true),
                switchRow("Check caliente posición LED", "Aviso de bombilla en cuadro.", true),
                switchRow("Confirmación al cerrar", "Intermitentes al cerrar con mando.", true),
                switchRow("Confirmación al abrir", "Pendiente de codificación segura.", false)
        }));
        body.addView(section("PRUEBAS"));
        body.addView(action("Leer DTC de motor", () -> { connect(); initElm(); runCmds(new String[]{"03", "07"}, "DTC"); }));
        body.addView(action("Compartir sesión", () -> share()));
    }

    void tabs(int active) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        String[] a = {"LUCES", "VENTANILLAS", "CONFORT", "OTROS"};
        for (int i = 0; i < a.length; i++) {
            TextView v = text(a[i], 12, i == active ? BLUE : Color.rgb(210, 216, 225), true);
            v.setGravity(Gravity.CENTER);
            final int idx = i;
            v.setOnClickListener(x -> { if (idx == 0) showLights(); else if (idx == 1) showCoding(); });
            r.addView(v, new LinearLayout.LayoutParams(0, dp(38), 1));
        }
        body.addView(r);
        View line = new View(this);
        line.setBackgroundColor(BLUE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(112), dp(2));
        lp.leftMargin = active == 0 ? dp(5) : active == 1 ? dp(123) : active == 2 ? dp(240) : dp(350);
        body.addView(line, lp);
        space(14);
    }

    TextView infoCard(String s) {
        TextView v = cardText("ⓘ    " + s, 13);
        v.setTextColor(Color.rgb(210, 220, 232));
        return v;
    }

    LinearLayout group(View[] rows) {
        LinearLayout g = new LinearLayout(this);
        g.setOrientation(LinearLayout.VERTICAL);
        g.setBackground(bg(CARD, 10, LINE));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(2), 0, dp(6));
        g.setLayoutParams(lp);
        for (View v : rows) g.addView(v);
        return g;
    }

    View switchRow(String title, String sub, boolean checked) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(8), dp(14), dp(8));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(text(title, 14, TXT, false));
        TextView desc = text(sub, 12, Color.rgb(188, 198, 210), false);
        desc.setMaxLines(2);
        texts.addView(desc);
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        Switch sw = new Switch(this);
        sw.setChecked(checked);
        tintSwitch(sw);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> add("SWITCH: " + title + " = " + (isChecked ? "ON" : "OFF")));
        row.addView(sw, new LinearLayout.LayoutParams(dp(66), dp(48)));
        return row;
    }

    void tintSwitch(Switch sw) {
        if (Build.VERSION.SDK_INT >= 21) {
            int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}};
            sw.setThumbTintList(new ColorStateList(states, new int[]{Color.WHITE, Color.rgb(210, 215, 222)}));
            sw.setTrackTintList(new ColorStateList(states, new int[]{BLUE, Color.rgb(56, 66, 78)}));
        }
    }

    TextView action(String s, final Runnable r) {
        TextView v = cardText(s, 14);
        v.setGravity(Gravity.CENTER);
        v.setOnClickListener(x -> new Thread(r).start());
        return v;
    }

    void bottomActions() {
        bottomBar = new LinearLayout(this);
        bottomBar.setPadding(dp(22), dp(10), dp(22), dp(16));
        bottomBar.setBackgroundColor(Color.rgb(3, 7, 13));
        TextView reset = bottomButton("↻  RESTABLECER", false);
        reset.setOnClickListener(v -> add("RESTABLECER: interfaz, sin escribir."));
        TextView save = bottomButton("▣  GUARDAR CAMBIOS", true);
        save.setOnClickListener(v -> add("GUARDAR BLOQUEADO: falta backup real de módulo."));
        bottomBar.addView(reset, new LinearLayout.LayoutParams(0, dp(54), 1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(54), 1);
        lp.leftMargin = dp(12);
        bottomBar.addView(save, lp);
        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, dp(80)));
    }

    TextView bottomButton(String s, boolean primary) {
        TextView v = text(s, 14, primary ? TXT : BLUE, true);
        v.setGravity(Gravity.CENTER);
        v.setBackground(primary ? bg(BLUE, 8, BLUE2) : bg(Color.TRANSPARENT, 8, BLUE));
        return v;
    }

    void showBackup() {
        shell("Backup seguro", false, -1);
        body.addView(infoCard("Backup READ ONLY: protocolo, voltaje, PIDs, VIN si responde, DTC y RAW. No escribe módulos."));
        body.addView(action("Backup completo seguro", () -> safeBackup()));
        body.addView(action("Backup identidad ECU/VIN", () -> runCmds(new String[]{"0900", "0902", "0904", "0906"}, "BACKUP ECU ID")));
        body.addView(action("Backup capacidades OBD", () -> runCmds(new String[]{"0100", "0120", "0140", "0160"}, "BACKUP PIDS")));
        body.addView(action("Backup fallos", () -> runCmds(new String[]{"03", "07", "0A"}, "BACKUP DTC")));
        body.addView(action("Compartir backup/sesión", () -> share()));
        addLogBox();
    }

    void showDiag() {
        shell("Diagnóstico", false, -1);
        liveCard = cardText(live(), 14);
        body.addView(liveCard);
        testsCard = cardText(tests(), 13);
        body.addView(testsCard);
        body.addView(action("Conectar ELM327", () -> connect()));
        body.addView(action("Inicializar ELM", () -> initElm()));
        body.addView(action("Leer motor", () -> readEngine()));
        body.addView(action("Leer DTC", () -> runCmds(new String[]{"03", "07"}, "DTC")));
        body.addView(action("Test completo automático", () -> fullTest()));
        body.addView(action("Compartir sesión", () -> share()));
        addLogBox();
    }

    void showInfo() {
        shell("Ajustes", false, -1);
        body.addView(infoCard("BMW E46 320d/320Cd M47N\nELM327 v2.1 detectado\nModo seguro: solo lectura\nLSZ/GM5 escritura bloqueada hasta backup real."));
        body.addView(action("Compartir sesión", () -> share()));
    }

    void showLogs() {
        shell("Logs", false, -1);
        body.addView(action("Compartir sesión completa", () -> share()));
        body.addView(action("Añadir informe mecánico", () -> add(report())));
        addLogBox();
        add("LOG READY. Usa Compartir sesión completa.");
    }

    void addLogBox() {
        logText = text("", 12, Color.rgb(220, 228, 240), false);
        logText.setPadding(dp(14), dp(14), dp(14), dp(14));
        ScrollView sv = new ScrollView(this);
        sv.setBackground(bg(Color.rgb(7, 12, 20), 12, Color.rgb(18, 30, 45)));
        sv.addView(logText);
        body.addView(sv, new LinearLayout.LayoutParams(-1, dp(245)));
    }

    String live() {
        return "RPM  " + val(rpm, "rpm") + "      TEMP  " + val(temp, "°C") + "\n" +
                "MAP  " + val(map, "kPa") + "      MAF  " + (maf < 0 ? "--" : String.format(Locale.US, "%.2f g/s", maf)) + "\n" +
                "VEL  " + val(speed, "km/h") + "      IAT  " + val(iat, "°C") + "\n" +
                "DTC  " + dtc;
    }

    String val(int v, String u) { return v < -100 || v < 0 ? "--" : v + " " + u; }

    String tests() {
        return (bt ? "●" : "○") + " Bluetooth    " + (elm ? "●" : "○") + " ELM    " + (protocol ? "●" : "○") + " Protocolo\n" +
                (engine ? "●" : "○") + " Motor/PIDs    " + (dtcOk ? "●" : "○") + " DTC    " + (backup ? "●" : "○") + " Backup";
    }

    void add(String s) {
        session.append(s).append("\n\n");
        runOnUiThread(() -> { if (logText != null) logText.append(s + "\n\n"); });
    }

    String report() {
        return "INFORME MECÁNICO\nVoltaje: " + volts + "\nProtocolo: " + proto + "\n" + live() + "\nConclusión: " +
                (dtc.contains("P0401") ? "EGR anulada/desconectada o flujo insuficiente detectado." : "Sin DTC motor confirmado.");
    }

    void fullTest() {
        connect(); initElm(); readEngine();
        runCmds(new String[]{"03", "07"}, "DTC");
        runCmds(new String[]{"ATDP", "ATDPN", "0100"}, "PROTOCOLO");
        add(report());
    }

    void safeBackup() {
        connect(); initElm();
        add("===== BACKUP SEGURO READ ONLY =====");
        runCmds(new String[]{"ATI", "ATRV", "ATDP", "ATDPN", "0100", "0120", "0140", "0900", "0902", "0904", "03", "07", "0A"}, "BACKUP SEGURO");
        backup = true;
        add("BACKUP OK: sesión RAW lista para exportar. No se ha escrito nada.");
        add(report());
        refresh();
    }

    void share() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "BMW E46 Scanner Session");
            i.putExtra(Intent.EXTRA_TEXT, session.toString());
            startActivity(Intent.createChooser(i, "Enviar sesión"));
        } catch (Exception e) { add("ERROR SHARE: " + e.getMessage()); }
    }

    boolean perm() {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 46));
            return false;
        }
        return true;
    }

    void connect() {
        try {
            test = "Conectando BT"; refresh();
            if (!perm()) return;
            BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
            if (a == null) { add("Sin Bluetooth"); return; }
            device = null;
            for (BluetoothDevice d : a.getBondedDevices()) {
                String n = d.getName() == null ? "" : d.getName().toLowerCase();
                if (device == null || n.contains("obd") || n.contains("elm")) device = d;
            }
            if (device == null) { add("Empareja ELM"); return; }
            close();
            socket = device.createRfcommSocketToServiceRecord(SPP);
            socket.connect();
            in = socket.getInputStream(); out = socket.getOutputStream();
            bt = true; test = "BT OK";
            add("BT OK: " + device.getName() + " / " + device.getAddress());
            refresh();
        } catch (Exception e) {
            bt = false;
            add("ERROR CONEXION: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            close(); refresh();
        }
    }

    void close() { try { if (socket != null) socket.close(); } catch (Exception ignored) {} socket = null; in = null; out = null; }
    boolean ready() { if (socket == null || !socket.isConnected() || in == null || out == null) { add("Pulsa conectar primero"); return false; } return true; }
    void initElm() { runCmds(new String[]{"ATZ", "ATE0", "ATL0", "ATS0", "ATH1", "ATI", "ATRV", "ATSP0", "ATDP", "ATDPN"}, "INIT ELM"); elm = true; refresh(); }
    void readEngine() { runCmds(new String[]{"0120", "010C", "010C", "0105", "010D", "010B", "010F", "0110"}, "MOTOR"); engine = true; refresh(); }

    void runCmds(String[] cs, String name) {
        try {
            if (!ready()) return;
            test = name; refresh();
            add("===== " + name + " =====");
            for (String c : cs) send(c);
            if (name.contains("DTC")) dtcOk = true;
            if (name.contains("PROTOCOLO")) protocol = true;
            test = name + " terminado";
            refresh();
        } catch (Exception e) {
            add("ERROR " + name + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            close(); refresh();
        }
    }

    void send(String c) throws Exception {
        out.write((c + "\r").getBytes("US-ASCII")); out.flush();
        Thread.sleep(c.equals("ATZ") ? 1700 : 1000);
        String r = read();
        add("> " + c + "\n" + r);
        parse(c, r);
        refresh();
    }

    String read() throws Exception {
        byte[] b = new byte[512]; StringBuilder s = new StringBuilder(); long end = System.currentTimeMillis() + 1600;
        while (System.currentTimeMillis() < end) {
            while (in.available() > 0) { int n = in.read(b); if (n > 0) s.append(new String(b, 0, n, "US-ASCII")); }
            if (s.toString().contains(">")) break;
            Thread.sleep(60);
        }
        String r = s.toString().replace('\r', ' ').replace('\n', ' ').trim();
        return r.length() == 0 ? "SIN RESPUESTA" : r;
    }

    void parse(String cmd, String r) {
        try {
            if (cmd.equals("ATRV")) volts = r.replace(">", "").trim();
            if (cmd.equals("ATDP")) proto = r.replace(">", "").trim();
            String h = r.replace(" ", "").replace(">", ""); int i;
            if ((i = h.indexOf("410C")) >= 0) { int a = Integer.parseInt(h.substring(i+4,i+6),16), b = Integer.parseInt(h.substring(i+6,i+8),16); rpm = ((a*256)+b)/4; }
            if ((i = h.indexOf("4105")) >= 0) temp = Integer.parseInt(h.substring(i+4,i+6),16)-40;
            if ((i = h.indexOf("410D")) >= 0) speed = Integer.parseInt(h.substring(i+4,i+6),16);
            if ((i = h.indexOf("410B")) >= 0) map = Integer.parseInt(h.substring(i+4,i+6),16);
            if ((i = h.indexOf("410F")) >= 0) iat = Integer.parseInt(h.substring(i+4,i+6),16)-40;
            if ((i = h.indexOf("4110")) >= 0) { int a = Integer.parseInt(h.substring(i+4,i+6),16), b = Integer.parseInt(h.substring(i+6,i+8),16); maf = ((a*256)+b)/100.0; }
            if (h.contains("430401")) dtc = "P0401 EGR insuficiente"; else if (h.contains("43")) dtc = "DTC RAW";
        } catch (Exception ignored) {}
    }
}
