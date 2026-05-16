package com.angel.e46scannerbt;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PremiumActivity extends Activity {
    private LinearLayout root;
    private LinearLayout body;
    private TextView status;
    private TextView logBox;
    private TextView checklistBox;
    private final ArrayList<String> history = new ArrayList<>();
    private final StringBuilder log = new StringBuilder();

    private boolean connected = false;
    private boolean elmReady = false;
    private boolean protocolReady = false;
    private boolean motorRead = false;
    private boolean dtcRead = false;
    private boolean backupDone = false;

    private final int BG = Color.rgb(0, 6, 14);
    private final int BG2 = Color.rgb(2, 13, 25);
    private final int CARD = Color.rgb(7, 18, 32);
    private final int CARD2 = Color.rgb(8, 20, 36);
    private final int LINE = Color.rgb(25, 49, 78);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(170, 181, 198);
    private final int BLUE = Color.rgb(0, 122, 255);
    private final int GREEN = Color.rgb(42, 225, 112);
    private final int RED = Color.rgb(255, 70, 90);
    private final int YELLOW = Color.rgb(245, 205, 50);
    private final int PURPLE = Color.rgb(160, 90, 255);

    @Override public void onCreate(Bundle b) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        log.append("BMW E46 SCANNER SESSION\n")
           .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()))
           .append("\nSAFE MODE · READ ONLY\n\n");
        go("home", false);
    }

    @Override public void onBackPressed() {
        if (history.size() > 1) {
            history.remove(history.size() - 1);
            draw(history.get(history.size() - 1));
        } else super.onBackPressed();
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setIncludeFontPadding(true);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        g.setStroke(dp(1), LINE);
        return g;
    }

    private void go(String screen, boolean push) {
        if (push) history.add(screen);
        else { history.clear(); history.add(screen); }
        draw(screen);
    }

    private void draw(String screen) {
        if (screen.equals("home")) showHome();
        else if (screen.equals("coding")) showCoding(1);
        else if (screen.equals("lights")) showCoding(0);
        else if (screen.equals("comfort")) showCoding(2);
        else if (screen.equals("backup")) showBackup();
        else if (screen.equals("tests")) showTests();
        else if (screen.equals("diag")) showDiagnostics();
        else if (screen.equals("logs")) showLogs();
        else showInfo();
    }

    private void base(String title, boolean back, boolean bottomNav, int activeNav) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{BG2, BG}));

        ScrollView scroll = new ScrollView(this);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(26), dp(10), dp(26), dp(8));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        header(title, back);
        if (bottomNav) bottomNav(activeNav);
    }

    private void header(String title, boolean back) {
        LinearLayout h = new LinearLayout(this);
        h.setGravity(Gravity.CENTER_VERTICAL);

        TextView left = text(back ? "‹" : "☰", 34, WHITE, false);
        left.setGravity(Gravity.CENTER);
        left.setOnClickListener(v -> onBackPressed());
        h.addView(left, new LinearLayout.LayoutParams(dp(44), dp(46)));

        TextView mid = text(title, 20, WHITE, false);
        mid.setGravity(Gravity.CENTER);
        h.addView(mid, new LinearLayout.LayoutParams(0, dp(46), 1));

        TextView right = text(back ? "▱" : "E46", back ? 22 : 14, back ? WHITE : BLUE, true);
        right.setGravity(Gravity.CENTER);
        h.addView(right, new LinearLayout.LayoutParams(dp(56), dp(46)));
        body.addView(h);

        status = text(statusLine(), 11, connected ? GREEN : RED, false);
        status.setGravity(Gravity.CENTER);
        body.addView(status);
        space(8);
    }

    private String statusLine() {
        return (connected ? "● Conectado" : "● Desconectado")
                + " · LISTO · ELM " + (elmReady ? "OK" : "--")
                + " · Backup " + (backupDone ? "OK" : "--");
    }

    private void refreshStatus() {
        if (status != null) {
            status.setText(statusLine());
            status.setTextColor(connected ? GREEN : RED);
        }
        if (checklistBox != null) checklistBox.setText(checklist());
        if (logBox != null) logBox.setText(log.toString());
    }

    private void space(int h) { body.addView(new Space(this), new LinearLayout.LayoutParams(1, dp(h))); }

    private void showHome() {
        base("Coding Lab E46", false, true, 0);

        FrameLayout hero = new FrameLayout(this);
        body.addView(hero, new LinearLayout.LayoutParams(-1, dp(232)));

        TextView car = text("", 1, WHITE, false);
        car.setBackgroundResource(getResources().getIdentifier("bmw_e46_black_coupe_hero", "drawable", getPackageName()));
        FrameLayout.LayoutParams carLp = new FrameLayout.LayoutParams(dp(398), dp(176), Gravity.RIGHT | Gravity.TOP);
        carLp.topMargin = dp(6);
        carLp.rightMargin = dp(-8);
        hero.addView(car, carLp);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(text(connected ? "Bluetooth   Conectado" : "Bluetooth   Desconectado", 13, connected ? GREEN : RED, false));
        info.addView(text("BMW E46 320d M47N", 17, WHITE, true));
        info.addView(text("VIN: WBABN510X0JU12345", 12, MUTED, false));
        TextView safe = text("SAFE MODE · BACKUP OBLIGATORIO", 11, BLUE, true);
        safe.setPadding(0, dp(5), 0, 0);
        info.addView(safe);
        hero.addView(info, new FrameLayout.LayoutParams(dp(232), -1, Gravity.LEFT));

        section("MENÚ PRINCIPAL");
        menu("⌕", "Coding Lab", "Funciones de confort y personalización", BLUE, () -> go("coding", true));
        menu("▣", "Backup seguro", "Guardar estado OBD/ECU antes de pruebas", BLUE, () -> go("backup", true));
        menu("⚡", "Pruebas", "Test guiado seguro por pasos", YELLOW, () -> go("tests", true));
        menu("☼", "Luces", "Iluminación exterior e interior", YELLOW, () -> go("lights", true));
        menu("▭", "Ventanillas", "Funciones de confort de ventanas", GREEN, () -> go("coding", true));
        menu("▣", "Confort", "Cierre, apertura y otras funciones", PURPLE, () -> go("comfort", true));
        menu("◌", "LED / Check", "Gestión de LED y testigos", YELLOW, () -> go("lights", true));
        menu("▰", "Diagnóstico", "Leer errores y estado de módulos", GREEN, () -> go("diag", true));
        menu("ⓘ", "Información del coche", "Detalles del vehículo y módulos", BLUE, () -> go("info", true));
        menu("▤", "Logs", "Registros y sesiones guardadas", PURPLE, () -> go("logs", true));
    }

    private void section(String s) {
        TextView v = text(s, 12, MUTED, false);
        v.setLetterSpacing(0.08f);
        v.setPadding(0, dp(12), 0, dp(7));
        body.addView(v);
    }

    private void menu(String icon, String title, String sub, int color, Runnable click) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), 0, dp(10), 0);
        row.setBackground(bg(CARD2, 10));
        row.setOnClickListener(v -> click.run());

        TextView ic = text(icon, 26, color, false);
        ic.setGravity(Gravity.CENTER);
        row.addView(ic, new LinearLayout.LayoutParams(dp(62), -1));

        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setGravity(Gravity.CENTER_VERTICAL);
        tx.addView(text(title, 16, WHITE, false));
        tx.addView(text(sub, 12, MUTED, false));
        row.addView(tx, new LinearLayout.LayoutParams(0, -1, 1));

        TextView arrow = text("›", 30, MUTED, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), -1));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(64));
        lp.setMargins(0, 0, 0, dp(7));
        body.addView(row, lp);
    }

    private void showCoding(int activeTab) {
        base("Coding Lab", true, false, 1);
        tabs(activeTab);
        info("Personaliza el comportamiento de las ventanillas y funciones de confort asociadas. Cambios bloqueados hasta Backup seguro.");
        section("CIERRE CON MANDO");
        group(new String[][]{
                {"Cerrar ventanillas con mantener pulsado", "Cierra todas las ventanillas al mantener pulsado cerrar.", "1"},
                {"Cerrar ventanillas traseras con mantener", "Cierra solo las traseras al mantener pulsado cerrar.", "1"},
                {"Doble clic para cerrar traseras", "Doble clic en cerrar para subir traseras automáticamente.", "1"},
                {"Doble clic para abrir traseras", "Doble clic en abrir para bajar traseras automáticamente.", "0"}
        });
        section("APERTURA CON MANDO");
        group(new String[][]{
                {"Abrir ventanillas con mantener pulsado", "Abre todas las ventanillas al mantener pulsado abrir.", "1"},
                {"Doble clic para abrir traseras", "Baja traseras automáticamente.", "1"}
        });
        section("OTRAS OPCIONES");
        group(new String[][]{
                {"Bajar ventanillas al abrir la puerta", "Baja ligeramente la ventanilla del conductor.", "0"},
                {"Blink unlock", "Intermitentes al abrir con mando.", "0"},
                {"LED cold/warm check", "Preparado para LSZ. Escritura bloqueada.", "1"}
        });
        codingBar();
    }

    private void tabs(int active) {
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        String[] names = {"LUCES", "VENTANILLAS", "CONFORT", "OTROS"};
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            TextView tab = text(names[i], 12, i == active ? BLUE : MUTED, true);
            tab.setGravity(Gravity.CENTER);
            tab.setOnClickListener(v -> showCoding(idx));
            row.addView(tab, new LinearLayout.LayoutParams(dp(116), dp(42)));
        }
        hsv.addView(row);
        body.addView(hsv);
        View line = new View(this);
        line.setBackgroundColor(BLUE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(116), dp(2));
        lp.leftMargin = dp(116 * active);
        body.addView(line, lp);
        space(10);
    }

    private void info(String s) {
        TextView v = text("ⓘ    " + s, 13, Color.rgb(215, 225, 240), false);
        v.setPadding(dp(14), dp(11), dp(14), dp(11));
        v.setBackground(bg(CARD2, 8));
        body.addView(v, new LinearLayout.LayoutParams(-1, -2));
    }

    private void group(String[][] rows) {
        LinearLayout g = new LinearLayout(this);
        g.setOrientation(LinearLayout.VERTICAL);
        g.setBackground(bg(CARD2, 8));
        for (String[] r : rows) g.addView(switchRow(r[0], r[1], r[2].equals("1")));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(7));
        body.addView(g, lp);
    }

    private View switchRow(String title, String sub, boolean checked) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(7), dp(10), dp(7));

        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.addView(text(title, 14, WHITE, false));
        TextView desc = text(sub, 12, MUTED, false);
        desc.setMaxLines(2);
        tx.addView(desc);
        row.addView(tx, new LinearLayout.LayoutParams(0, dp(55), 1));

        Switch sw = new Switch(this);
        sw.setChecked(checked);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}};
            sw.setThumbTintList(new ColorStateList(states, new int[]{Color.WHITE, Color.WHITE}));
            sw.setTrackTintList(new ColorStateList(states, new int[]{BLUE, Color.rgb(50,65,82)}));
        }
        sw.setOnCheckedChangeListener((b, c) -> {
            if (!backupDone) {
                b.setChecked(!c);
                addLog("BLOQUEADO: primero haz Backup seguro antes de cambiar " + title);
            } else addLog("SWITCH " + title + " = " + (c ? "ON" : "OFF"));
        });
        row.addView(sw, new LinearLayout.LayoutParams(dp(62), dp(42)));
        return row;
    }

    private void codingBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(dp(22), dp(8), dp(22), dp(12));
        bar.setBackgroundColor(Color.rgb(1,8,16));
        TextView reset = bottomButton("↻  RESTABLECER", false);
        TextView save = bottomButton("▣  GUARDAR CAMBIOS", true);
        reset.setOnClickListener(v -> addLog("Restablecer visual. No se modifican módulos."));
        save.setOnClickListener(v -> {
            if (!backupDone) addLog("GUARDAR BLOQUEADO: primero Backup seguro.");
            else addLog("GUARDAR BLOQUEADO: backup existe, pero escritura real aún está deshabilitada.");
        });
        bar.addView(reset, new LinearLayout.LayoutParams(0, dp(52), 1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(52), 1);
        lp.leftMargin = dp(12);
        bar.addView(save, lp);
        root.addView(bar, new LinearLayout.LayoutParams(-1, dp(74)));
    }

    private TextView bottomButton(String s, boolean primary) {
        TextView v = text(s, 14, primary ? WHITE : BLUE, true);
        v.setGravity(Gravity.CENTER);
        v.setBackground(bg(primary ? BLUE : Color.TRANSPARENT, 8));
        return v;
    }

    private void showBackup() {
        base("Backup seguro", true, true, 1);
        info("Paso obligatorio. No se permite preparar módulos ni pruebas avanzadas sin backup RAW exportable.");
        checklistBox = card(checklist(), 13);
        body.addView(checklistBox);
        body.addView(action("1 · Conectar Bluetooth", () -> fakeConnect()));
        body.addView(action("2 · Inicializar ELM327", () -> fakeElm()));
        body.addView(action("3 · Backup OBD/ECU READ ONLY", () -> fakeBackup()));
        body.addView(action("4 · Compartir backup completo", () -> share()));
        logSection();
    }

    private void showTests() {
        base("Pruebas", true, true, 1);
        info("Test guiado seguro. Las pruebas avanzadas quedan bloqueadas hasta completar Backup seguro.");
        checklistBox = card(checklist(), 13);
        body.addView(checklistBox);
        section("LECTURA SEGURA");
        body.addView(action("Test 1 · Conexión + ELM", () -> { fakeConnect(); fakeElm(); }));
        body.addView(action("Test 2 · Protocolo ISO/KWP", () -> fakeProtocol()));
        body.addView(action("Test 3 · Motor/PIDs", () -> fakeMotor()));
        body.addView(action("Test 4 · DTC motor", () -> fakeDtc()));
        section("PRUEBAS AVANZADAS");
        body.addView(action("Test 5 · Preparar LSZ LED", () -> guarded("Preparar LSZ LED cold/warm")));
        body.addView(action("Test 6 · Preparar GM5 confort", () -> guarded("Preparar GM5 comfort close / blink")));
        body.addView(action("Exportar sesión para ChatGPT", () -> share()));
        logSection();
    }

    private String checklist() {
        return (connected ? "✓" : "□") + " Bluetooth\n"
                + (elmReady ? "✓" : "□") + " ELM inicializado\n"
                + (protocolReady ? "✓" : "□") + " Protocolo identificado\n"
                + (motorRead ? "✓" : "□") + " Motor/PIDs leídos\n"
                + (dtcRead ? "✓" : "□") + " DTC leídos\n"
                + (backupDone ? "✓" : "□") + " Backup seguro exportable";
    }

    private void fakeConnect() { connected = true; addLog("BT OK: OBDII / adaptador seleccionado"); refreshStatus(); }
    private void fakeElm() { elmReady = true; addLog("ELM OK: ATZ / ATE0 / ATL0 / ATS0 / ATH1 / ATSP0"); refreshStatus(); }
    private void fakeProtocol() { protocolReady = true; addLog("PROTOCOLO OK: AUTO, ISO 9141-2 / KWP"); refreshStatus(); }
    private void fakeMotor() { motorRead = true; addLog("MOTOR OK: PIDs básicos preparados para lectura real"); refreshStatus(); }
    private void fakeDtc() { dtcRead = true; addLog("DTC OK: lectura preparada. Sin borrado."); refreshStatus(); }
    private void fakeBackup() { connected = true; elmReady = true; protocolReady = true; motorRead = true; dtcRead = true; backupDone = true; addLog("BACKUP OK: sesión RAW exportable. No se ha escrito nada."); refreshStatus(); }
    private void guarded(String name) { if (!backupDone) addLog("BLOQUEADO: " + name + " requiere Backup seguro. No se modifica nada."); else addLog(name + " listo en SIMULACIÓN. Escritura real deshabilitada."); refreshStatus(); }

    private void showDiagnostics() {
        base("Diagnóstico", true, true, 2);
        body.addView(card("RPM --     TEMP --\nMAP --     MAF --\nVEL --     IAT --\nDTC --", 14));
        body.addView(action("Conectar ELM327", () -> fakeConnect()));
        body.addView(action("Inicializar ELM", () -> fakeElm()));
        body.addView(action("Leer motor básico", () -> fakeMotor()));
        body.addView(action("Leer DTC", () -> fakeDtc()));
        body.addView(action("Backup seguro READ ONLY", () -> fakeBackup()));
        body.addView(action("Compartir sesión completa", () -> share()));
        logSection();
    }

    private void showInfo() {
        base("Información del coche", true, true, 3);
        body.addView(card("BMW E46 320d/320Cd M47N\nVIN: WBABN510X0JU12345\nModo: SAFE / READ ONLY\nObjetivos: LSZ LED cold/warm, GM5 comfort close, blink unlock.\nRegla: nada se modifica sin backup y sin confirmación.", 14));
    }

    private void showLogs() {
        base("Logs", true, true, 2);
        body.addView(action("Compartir sesión completa", () -> share()));
        logSection();
    }

    private TextView card(String s, int sp) {
        TextView v = text(s, sp, WHITE, false);
        v.setPadding(dp(14), dp(12), dp(14), dp(12));
        v.setBackground(bg(CARD2, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(8));
        v.setLayoutParams(lp);
        return v;
    }

    private TextView action(String s, Runnable r) {
        TextView v = card(s, 14);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setHeight(dp(52));
        v.setOnClickListener(x -> r.run());
        return v;
    }

    private void logSection() {
        logBox = card(log.toString(), 12);
        body.addView(logBox, new LinearLayout.LayoutParams(-1, dp(245)));
    }

    private void addLog(String s) {
        log.append(s).append("\n\n");
        refreshStatus();
    }

    private void bottomNav(int active) {
        LinearLayout nav = new LinearLayout(this);
        nav.setBackgroundColor(Color.rgb(1,8,16));
        String[] labels = {"⌂\nInicio", "◇\nMódulos", "▤\nLogs", "⚙\nAjustes"};
        Runnable[] actions = {() -> go("home", false), () -> go("coding", true), () -> go("logs", true), () -> go("info", true)};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            TextView v = text(labels[i], 12, i == active ? BLUE : MUTED, false);
            v.setGravity(Gravity.CENTER);
            v.setOnClickListener(x -> actions[idx].run());
            nav.addView(v, new LinearLayout.LayoutParams(0, dp(58), 1));
        }
        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(62)));
    }

    private void share() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "BMW E46 Scanner Session");
        i.putExtra(Intent.EXTRA_TEXT, log.toString());
        startActivity(Intent.createChooser(i, "Enviar sesión"));
    }
}
