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
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PremiumActivity extends Activity {
    private LinearLayout root, body;
    private TextView status, logBox, checklistBox, diagnosticBox;
    private ElmClient elmClient;
    private final ArrayList<String> history = new ArrayList<>();
    private final StringBuilder log = new StringBuilder();

    private boolean connected=false, elmReady=false, protocolReady=false, motorRead=false, dtcRead=false, backupDone=false, busy=false;

    private final int BG=Color.rgb(0,6,14), BG2=Color.rgb(2,13,25), CARD=Color.rgb(7,18,32), CARD2=Color.rgb(9,23,42), LINE=Color.rgb(25,55,90);
    private final int WHITE=Color.WHITE, MUTED=Color.rgb(168,181,201), BLUE=Color.rgb(0,122,255), GREEN=Color.rgb(42,225,112), RED=Color.rgb(255,70,90), YELLOW=Color.rgb(245,205,50), PURPLE=Color.rgb(160,90,255);

    @Override public void onCreate(Bundle b) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(Color.rgb(1,8,16));
        elmClient = new ElmClient(this, this::addLog);
        log.append("BMW E46 SCANNER SESSION\n")
                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()))
                .append("\nSAFE MODE - READ ONLY\n\n");
        go("home", false);
    }

    @Override public void onBackPressed() {
        if (busy) { addLog("ESPERA: hay una operación en curso."); return; }
        if (history.size() > 1) {
            history.remove(history.size() - 1);
            draw(history.get(history.size() - 1));
        } else {
            if (!"home".equals(current())) go("home", false);
        }
    }

    private String current() { return history.size() == 0 ? "home" : history.get(history.size() - 1); }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(sp); t.setTextColor(color); t.setIncludeFontPadding(true);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius)); g.setStroke(dp(1), LINE);
        return g;
    }

    private GradientDrawable grad() { return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.rgb(3,18,36), BG}); }

    private void go(String screen, boolean push) {
        if (busy) { addLog("ESPERA: termina la operación actual antes de cambiar de pantalla."); return; }
        if (push) history.add(screen); else { history.clear(); history.add(screen); }
        draw(screen);
    }

    private void draw(String s) {
        if (s.equals("home")) showHome();
        else if (s.equals("coding")) showCoding(1);
        else if (s.equals("lights")) showCoding(0);
        else if (s.equals("comfort")) showCoding(2);
        else if (s.equals("backup")) showBackup();
        else if (s.equals("tests")) showTests();
        else if (s.equals("diag")) showDiagnostics();
        else if (s.equals("logs")) showLogs();
        else showInfo();
    }

    private void base(String title, boolean back, boolean nav, int active) {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackground(grad());
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(false);
        body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(24), dp(10), dp(24), dp(14));
        scroll.addView(body); root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
        header(title, back);
        if (nav) bottomNav(active);
    }

    private void header(String title, boolean back) {
        LinearLayout h = new LinearLayout(this); h.setGravity(Gravity.CENTER_VERTICAL);
        TextView left = text(back ? "‹" : "☰", 34, WHITE, false); left.setGravity(Gravity.CENTER); left.setOnClickListener(v -> onBackPressed());
        h.addView(left, new LinearLayout.LayoutParams(dp(44), dp(48)));
        TextView mid = text(title, 20, WHITE, false); mid.setGravity(Gravity.CENTER); h.addView(mid, new LinearLayout.LayoutParams(0, dp(48), 1));
        TextView right = text(back ? "▱" : "E46", back ? 23 : 14, back ? WHITE : BLUE, true); right.setGravity(Gravity.CENTER);
        h.addView(right, new LinearLayout.LayoutParams(dp(56), dp(48))); body.addView(h);

        status = text(statusLine(), 11, statusColor(), true);
        status.setGravity(Gravity.CENTER); status.setPadding(dp(14), dp(6), dp(14), dp(6));
        status.setBackground(bg(Color.rgb(4,16,30), 20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(36)); lp.setMargins(0,0,0,dp(8)); body.addView(status, lp);
    }

    private int statusColor() { return busy ? YELLOW : (connected ? GREEN : RED); }

    private String statusLine() {
        String run = busy ? "EJECUTANDO" : "LISTO";
        return run + "  |  " + (connected ? "CONECTADO" : "DESCONECTADO") + "  |  ELM " + (elmReady ? "OK" : "--") + "  |  BACKUP " + (backupDone ? "OK" : "--") + "  |  SAFE";
    }

    private void refreshStatus() {
        runOnUiThread(() -> {
            if (status != null) { status.setText(statusLine()); status.setTextColor(statusColor()); }
            if (checklistBox != null) checklistBox.setText(checklist());
            if (logBox != null) logBox.setText(log.toString());
            if (diagnosticBox != null) diagnosticBox.setText(elmClient.diagnosticText());
        });
    }

    private void runTask(Runnable r) {
        if (busy) { addLog("ESPERA: operación ya en curso."); return; }
        busy = true; refreshStatus();
        try { r.run(); }
        finally { busy = false; refreshStatus(); }
    }

    private void space(int h) { body.addView(new Space(this), new LinearLayout.LayoutParams(1, dp(h))); }

    private void showHome() {
        base("Coding Lab E46", false, true, 0);
        hero(); chips(); statusCards();
        section("MENU PRINCIPAL");
        menu("⌕", "Coding Lab", "Funciones de confort y personalización", BLUE, () -> go("coding", true));
        menu("▣", "Backup seguro", "Guardar estado OBD/ECU antes de pruebas", BLUE, () -> go("backup", true));
        menu("⚡", "Pruebas", "Test guiado seguro por pasos", YELLOW, () -> go("tests", true));
        menu("☼", "Luces", "Iluminación exterior e interior", YELLOW, () -> go("lights", true));
        menu("▭", "Ventanillas", "Funciones de confort de ventanas", GREEN, () -> go("coding", true));
        menu("▣", "Confort", "Cierre, apertura y funciones GM5", PURPLE, () -> go("comfort", true));
        menu("◌", "LED / Check", "Gestión de LED y testigos", YELLOW, () -> go("lights", true));
        menu("▰", "Diagnóstico", "Leer errores y estado de módulos", GREEN, () -> go("diag", true));
        menu("ⓘ", "Información", "Detalles del vehículo y seguridad", BLUE, () -> go("info", true));
        menu("▤", "Logs", "Registros y sesiones guardadas", PURPLE, () -> go("logs", true));
    }

    private void hero() {
        FrameLayout hero = new FrameLayout(this); hero.setBackground(bg(Color.rgb(3,13,24), 18));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, dp(236)); hp.setMargins(0, dp(4), 0, dp(12)); body.addView(hero, hp);
        TextView glow = text("",1,WHITE,false); glow.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Color.TRANSPARENT, Color.argb(105,0,122,255), Color.TRANSPARENT}));
        FrameLayout.LayoutParams gp = new FrameLayout.LayoutParams(-1, dp(92), Gravity.BOTTOM); gp.bottomMargin = dp(22); hero.addView(glow, gp);
        TextView car = text("BMW E46", 16, BLUE, true); car.setGravity(Gravity.CENTER);
        int res = getResources().getIdentifier("bmw_e46_black_coupe_hero", "drawable", getPackageName()); if (res != 0) car.setBackgroundResource(res);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(dp(405), dp(178), Gravity.RIGHT | Gravity.TOP); cp.topMargin=dp(8); cp.rightMargin=dp(-18); hero.addView(car, cp);
        LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setGravity(Gravity.CENTER_VERTICAL); info.setPadding(dp(16),0,0,0);
        info.addView(text(connected ? "Bluetooth conectado" : "Bluetooth desconectado", 13, connected ? GREEN : RED, true));
        info.addView(text("BMW E46 320d M47N", 18, WHITE, true));
        info.addView(text("SAFE MODE · READ ONLY", 12, BLUE, true));
        info.addView(text("No escribe sin backup", 12, MUTED, false));
        info.addView(text("V4.2 · decoded diagnostics", 11, MUTED, false));
        hero.addView(info, new FrameLayout.LayoutParams(dp(238), -1, Gravity.LEFT));
    }

    private void chips() {
        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        chip(row, "Backup", BLUE, () -> go("backup", true)); chip(row, "Pruebas", YELLOW, () -> go("tests", true)); chip(row, "Diagnóstico", GREEN, () -> go("diag", true)); chip(row, "Logs", PURPLE, () -> go("logs", true)); chip(row, "Confort", PURPLE, () -> go("comfort", true)); chip(row, "LED", YELLOW, () -> go("lights", true));
        hsv.addView(row); body.addView(hsv, new LinearLayout.LayoutParams(-1, dp(52)));
    }

    private void chip(LinearLayout row, String s, int color, Runnable r) {
        TextView v = text(s, 13, color, true); v.setGravity(Gravity.CENTER); v.setPadding(dp(16),0,dp(16),0); v.setBackground(bg(Color.rgb(5,16,30), 24)); v.setOnClickListener(x -> r.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(42)); lp.setMargins(0,0,dp(8),0); row.addView(v, lp);
    }

    private void statusCards() {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        smallStatus(row, "BT", connected ? "OK" : "--", connected ? GREEN : RED);
        smallStatus(row, "ELM", elmReady ? "OK" : "--", elmReady ? GREEN : MUTED);
        smallStatus(row, "BACKUP", backupDone ? "OK" : "--", backupDone ? GREEN : MUTED);
        smallStatus(row, "MODE", "SAFE", BLUE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(62)); lp.setMargins(0,0,0,dp(6)); body.addView(row, lp);
    }

    private void smallStatus(LinearLayout row, String title, String value, int color) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setBackground(bg(Color.rgb(5,17,32), 14));
        TextView a = text(title, 10, MUTED, true); a.setGravity(Gravity.CENTER); TextView b = text(value, 15, color, true); b.setGravity(Gravity.CENTER); box.addView(a); box.addView(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1); lp.setMargins(0,0,dp(6),0); row.addView(box, lp);
    }

    private void section(String s) { TextView v=text(s,12,MUTED,true); v.setLetterSpacing(0.08f); v.setPadding(0,dp(14),0,dp(8)); body.addView(v); }

    private void menu(String icon, String title, String sub, int color, Runnable click) {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),0,dp(12),0); row.setBackground(bg(CARD2,14)); row.setOnClickListener(v -> click.run());
        TextView ic = text(icon, 26, color, false); ic.setGravity(Gravity.CENTER); row.addView(ic, new LinearLayout.LayoutParams(dp(58), -1));
        LinearLayout tx = new LinearLayout(this); tx.setOrientation(LinearLayout.VERTICAL); tx.setGravity(Gravity.CENTER_VERTICAL); tx.addView(text(title,16,WHITE,true)); tx.addView(text(sub,12,MUTED,false)); row.addView(tx, new LinearLayout.LayoutParams(0,-1,1));
        TextView ar = text("›", 30, MUTED, false); ar.setGravity(Gravity.CENTER); row.addView(ar, new LinearLayout.LayoutParams(dp(24), -1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(66)); lp.setMargins(0,0,0,dp(8)); body.addView(row, lp);
    }

    private void showCoding(int activeTab) {
        base("Coding Lab", true, false, 1); tabs(activeTab);
        info("Cambios bloqueados hasta Backup seguro. LSZ/GM5 siguen en simulación.");
        section("CIERRE CON MANDO"); group(new String[][]{{"Cerrar ventanillas con mantener pulsado","Cierra todas las ventanillas al mantener pulsado cerrar.","1"},{"Cerrar traseras con mantener","Cierra solo las traseras al mantener pulsado cerrar.","1"},{"Doble clic cerrar traseras","Sube traseras automáticamente.","1"},{"Doble clic abrir traseras","Baja traseras automáticamente.","0"}});
        section("APERTURA CON MANDO"); group(new String[][]{{"Abrir ventanillas con mantener","Abre todas las ventanillas al mantener abrir.","1"},{"Blink unlock","Intermitentes al abrir con mando.","0"}});
        section("LED / CHECK"); group(new String[][]{{"LED cold check","Preparado para LSZ. Bloqueado.","1"},{"LED warm check","Preparado para LSZ. Bloqueado.","1"}});
        codingBar();
    }

    private void tabs(int active) {
        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false); LinearLayout row = new LinearLayout(this);
        String[] names={"LUCES","VENTANILLAS","CONFORT","OTROS","SEGURIDAD","BACKUP"};
        for(int i=0;i<names.length;i++){ final int idx=i; TextView tab=text(names[i],12,i==active?BLUE:MUTED,true); tab.setGravity(Gravity.CENTER); tab.setOnClickListener(v->showCoding(idx)); row.addView(tab,new LinearLayout.LayoutParams(dp(118),dp(42))); }
        hsv.addView(row); body.addView(hsv); View line=new View(this); line.setBackgroundColor(BLUE); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(118),dp(2)); lp.leftMargin=dp(118*active); body.addView(line,lp); space(12);
    }

    private void info(String s) { TextView v=text("ⓘ  "+s,13,Color.rgb(215,225,240),false); v.setPadding(dp(14),dp(12),dp(14),dp(12)); v.setBackground(bg(Color.rgb(5,17,32),12)); body.addView(v,new LinearLayout.LayoutParams(-1,-2)); }

    private void group(String[][] rows) { LinearLayout g=new LinearLayout(this); g.setOrientation(LinearLayout.VERTICAL); g.setBackground(bg(CARD2,12)); for(String[] r:rows)g.addView(switchRow(r[0],r[1],r[2].equals("1"))); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(8)); body.addView(g,lp); }

    private View switchRow(String title,String sub,boolean checked) {
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(14),dp(8),dp(10),dp(8));
        LinearLayout tx=new LinearLayout(this); tx.setOrientation(LinearLayout.VERTICAL); tx.addView(text(title,14,WHITE,true)); TextView desc=text(sub,12,MUTED,false); desc.setMaxLines(2); tx.addView(desc); row.addView(tx,new LinearLayout.LayoutParams(0,dp(56),1));
        Switch sw=new Switch(this); sw.setChecked(checked); if(android.os.Build.VERSION.SDK_INT>=21){int[][] st=new int[][]{new int[]{android.R.attr.state_checked},new int[]{-android.R.attr.state_checked}}; sw.setThumbTintList(new ColorStateList(st,new int[]{Color.WHITE,Color.WHITE})); sw.setTrackTintList(new ColorStateList(st,new int[]{BLUE,Color.rgb(50,65,82)}));}
        sw.setOnCheckedChangeListener((b,c)->{ if(!backupDone){ b.setChecked(!c); addLog("BLOQUEADO: primero Backup seguro antes de cambiar " + title); } else addLog("SWITCH " + title + " = " + (c?"ON":"OFF")); }); row.addView(sw,new LinearLayout.LayoutParams(dp(62),dp(44))); return row;
    }

    private void codingBar() {
        LinearLayout bar=new LinearLayout(this); bar.setPadding(dp(22),dp(8),dp(22),dp(12)); bar.setBackgroundColor(Color.rgb(1,8,16));
        TextView reset=bottomButton("RESTABLECER",false), save=bottomButton("GUARDAR",true); reset.setOnClickListener(v->addLog("Restablecer visual. Sin modificar módulos.")); save.setOnClickListener(v->{ if(!backupDone)addLog("GUARDAR BLOQUEADO: primero Backup seguro."); else addLog("GUARDAR BLOQUEADO: escritura real deshabilitada."); });
        bar.addView(reset,new LinearLayout.LayoutParams(0,dp(52),1)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1); lp.leftMargin=dp(12); bar.addView(save,lp); root.addView(bar,new LinearLayout.LayoutParams(-1,dp(74)));
    }

    private TextView bottomButton(String s, boolean primary) { TextView v=text(s,14,primary?WHITE:BLUE,true); v.setGravity(Gravity.CENTER); v.setBackground(bg(primary?BLUE:Color.TRANSPARENT,10)); return v; }

    private void showBackup() { base("Backup seguro", true, true, 1); info("Obligatorio antes de pruebas avanzadas. Todo READ ONLY."); checklistBox=card(checklist(),13); body.addView(checklistBox); body.addView(action("1 · Conectar Bluetooth",this::realConnect)); body.addView(action("2 · Inicializar ELM327",this::realElm)); body.addView(action("3 · Backup OBD/ECU READ ONLY",this::realBackup)); body.addView(action("4 · Compartir backup completo",this::share)); logSection(); }

    private void showTests() { base("Pruebas", true, true, 1); info("Test guiado. Avanzados bloqueados hasta backup."); checklistBox=card(checklist(),13); body.addView(checklistBox); section("LECTURA SEGURA"); body.addView(action("Test 1 · Conexión + ELM",()->{realConnect();realElm();})); body.addView(action("Test 2 · Protocolo ISO/KWP",this::realProtocol)); body.addView(action("Test 3 · Motor/PIDs",this::realMotor)); body.addView(action("Test 4 · DTC motor",this::realDtc)); section("PRUEBAS AVANZADAS"); body.addView(action("Test 5 · Preparar LSZ LED",()->guarded("Preparar LSZ LED cold/warm"))); body.addView(action("Test 6 · Preparar GM5 confort",()->guarded("Preparar GM5 comfort close / blink"))); body.addView(action("Exportar sesión",this::share)); logSection(); }

    private String checklist() { return (connected?"[OK]":"[--]")+" Bluetooth\n"+(elmReady?"[OK]":"[--]")+" ELM inicializado\n"+(protocolReady?"[OK]":"[--]")+" Protocolo identificado\n"+(motorRead?"[OK]":"[--]")+" Motor/PIDs leidos\n"+(dtcRead?"[OK]":"[--]")+" DTC leidos\n"+(backupDone?"[OK]":"[--]")+" Backup seguro exportable"; }

    private void realConnect(){String r=elmClient.connect();connected=elmClient.isConnected();addLog(r);refreshStatus();}
    private void realElm(){String r=elmClient.initElm();elmReady=elmClient.isConnected()&&!r.contains("ERROR");addLog(r);refreshStatus();}
    private void realProtocol(){String r=elmClient.readProtocol();protocolReady=elmClient.isConnected()&&!r.contains("ERROR");addLog(r);refreshStatus();}
    private void realMotor(){String r=elmClient.readMotor();motorRead=elmClient.isConnected()&&!r.contains("ERROR");addLog(r);refreshStatus();}
    private void realDtc(){String r=elmClient.readDtc();dtcRead=elmClient.isConnected()&&!r.contains("ERROR");addLog(r);refreshStatus();}
    private void realBackup(){String r=elmClient.backupSafe();connected=elmClient.isConnected();backupDone=connected&&!r.contains("ERROR")&&!r.contains("BLOQUEADO");if(backupDone){elmReady=true;protocolReady=true;motorRead=true;dtcRead=true;}addLog(r+(backupDone?"\nBACKUP OK: RAW exportable. No se ha escrito nada.":""));refreshStatus();}
    private void guarded(String name){if(!backupDone)addLog("BLOQUEADO: "+name+" requiere Backup seguro. No se modifica nada.");else addLog(name+" listo en SIMULACION. Escritura real deshabilitada.");refreshStatus();}

    private void showDiagnostics() { base("Diagnóstico", true, true, 2); diagnosticBox=card(elmClient.diagnosticText(),14); body.addView(diagnosticBox); body.addView(action("Conectar ELM327",this::realConnect)); body.addView(action("Inicializar ELM",this::realElm)); body.addView(action("Leer motor básico",this::realMotor)); body.addView(action("Leer DTC",this::realDtc)); body.addView(action("Backup seguro READ ONLY",this::realBackup)); body.addView(action("Compartir sesión completa",this::share)); logSection(); }
    private void showInfo(){base("Información",true,true,3);body.addView(card("BMW E46 320d/320Cd M47N\nModo: SAFE / READ ONLY\nObjetivos: LSZ LED cold/warm, GM5 comfort close, blink unlock.\nRegla: nada se modifica sin backup y sin confirmación.",14));}
    private void showLogs(){base("Logs",true,true,2);body.addView(action("Compartir sesión completa",this::share));logSection();}

    private TextView card(String s,int sp){TextView v=text(s,sp,WHITE,false);v.setPadding(dp(14),dp(12),dp(14),dp(12));v.setBackground(bg(CARD2,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));v.setLayoutParams(lp);return v;}
    private TextView action(String s,Runnable r){TextView v=card(s + "    ›",14);v.setGravity(Gravity.CENTER_VERTICAL);v.setHeight(dp(54));v.setOnClickListener(x->{ if(busy){addLog("ESPERA: operación ya en curso.");return;} new Thread(()->runTask(r)).start();});return v;}
    private void logSection(){logBox=card(log.toString(),12);body.addView(logBox,new LinearLayout.LayoutParams(-1,dp(245)));}
    private void addLog(String s){log.append(s).append("\n\n");refreshStatus();}

    private void bottomNav(int active){LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(6),dp(4),dp(6),dp(4));nav.setBackgroundColor(Color.rgb(1,8,16));String[] labels={"⌂\nInicio","◇\nMódulos","▤\nLogs","⚙\nAjustes"};Runnable[] actions={()->go("home",false),()->go("coding",true),()->go("logs",true),()->go("info",true)};for(int i=0;i<4;i++){final int idx=i;TextView v=text(labels[i],12,i==active?BLUE:MUTED,i==active);v.setGravity(Gravity.CENTER);if(i==active)v.setBackground(bg(Color.rgb(4,16,30),16));v.setOnClickListener(x->actions[idx].run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);lp.setMargins(dp(3),0,dp(3),0);nav.addView(v,lp);}root.addView(nav,new LinearLayout.LayoutParams(-1,dp(66)));}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session");i.putExtra(Intent.EXTRA_TEXT,log.toString());startActivity(Intent.createChooser(i,"Enviar sesión"));}
}
