package com.angel.e46scannerbt;

import android.app.Activity;
import android.os.Bundle;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
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
    private final SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private boolean connected=false, elmReady=false, protocolReady=false, motorRead=false, dtcRead=false, backupDone=false, busy=false;

    private final int BG=Color.rgb(0,6,14), CARD=Color.rgb(5,14,26), CARD2=Color.rgb(7,17,31), LINE=Color.rgb(13,31,52);
    private final int WHITE=Color.WHITE, MUTED=Color.rgb(155,170,192), BLUE=Color.rgb(0,122,255), GREEN=Color.rgb(42,225,112), RED=Color.rgb(255,70,90), YELLOW=Color.rgb(245,205,50), PURPLE=Color.rgb(160,90,255);

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
        } else if (!"home".equals(current())) go("home", false);
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

    private GradientDrawable grad() { return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.rgb(2,14,28), BG}); }

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
        body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(18), dp(8), dp(18), dp(12));
        scroll.addView(body); root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
        header(title, back);
        if (nav) bottomNav(active);
    }

    private void header(String title, boolean back) {
        LinearLayout h = new LinearLayout(this); h.setGravity(Gravity.CENTER_VERTICAL);
        TextView left = text(back ? "‹" : "☰", 32, WHITE, false); left.setGravity(Gravity.CENTER); left.setOnClickListener(v -> onBackPressed());
        h.addView(left, new LinearLayout.LayoutParams(dp(42), dp(46)));
        TextView mid = text(title, 20, WHITE, false); mid.setGravity(Gravity.CENTER); h.addView(mid, new LinearLayout.LayoutParams(0, dp(46), 1));
        TextView right = text(back ? "▱" : "E46", back ? 22 : 14, back ? WHITE : BLUE, true); right.setGravity(Gravity.CENTER);
        h.addView(right, new LinearLayout.LayoutParams(dp(52), dp(46))); body.addView(h);

        status = text(statusLine(), 10, statusColor(), true);
        status.setGravity(Gravity.CENTER); status.setPadding(dp(10), dp(5), dp(10), dp(5));
        status.setBackground(bg(Color.rgb(3,13,24), 20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(34)); lp.setMargins(0,0,0,dp(10)); body.addView(status, lp);
    }

    private int statusColor() { return busy ? YELLOW : (connected ? GREEN : RED); }
    private String statusLine() { return (busy ? "EJECUTANDO" : "LISTO") + "  |  " + (connected ? "CONECTADO" : "DESCONECTADO") + "  |  ELM " + (elmReady ? "OK" : "--") + "  |  BACKUP " + (backupDone ? "OK" : "--") + "  |  SAFE"; }

    private void refreshStatus() {
        runOnUiThread(() -> {
            if (status != null) { status.setText(statusLine()); status.setTextColor(statusColor()); }
            if (checklistBox != null) checklistBox.setText(checklist());
            if (logBox != null) logBox.setText(log.toString());
            if (diagnosticBox != null) diagnosticBox.setText(elmClient.diagnosticText());
        });
    }

    private void runTask(Runnable r) { if (busy) { addLog("ESPERA: operación ya en curso."); return; } busy=true; refreshStatus(); try{r.run();} finally{busy=false; refreshStatus();} }
    private void space(int h) { body.addView(new Space(this), new LinearLayout.LayoutParams(1, dp(h))); }

    private void showHome() {
        base("Coding Lab E46", false, true, 0);
        hero(); chips(); statusCards();
        section("MENU PRINCIPAL");
        menu("", "Coding Lab", "Confort y personalización", BLUE, () -> go("coding", true));
        menu("", "Backup seguro", "Estado OBD/ECU antes de pruebas", BLUE, () -> go("backup", true));
        menu("", "Pruebas", "Test guiado seguro", YELLOW, () -> go("tests", true));
        menu("", "Luces", "Iluminación exterior e interior", YELLOW, () -> go("lights", true));
        menu("", "Ventanillas", "Funciones de confort", GREEN, () -> go("coding", true));
        menu("", "Confort", "Cierre, apertura y GM5", PURPLE, () -> go("comfort", true));
        menu("", "Diagnóstico", "Errores y módulos", GREEN, () -> go("diag", true));
        menu("", "Logs", "Registros de sesión", PURPLE, () -> go("logs", true));
    }

    private void hero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(16), dp(14), dp(16), dp(10));
        hero.setBackground(bg(Color.rgb(3,12,22), 18));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, dp(250));
        hp.setMargins(0, 0, 0, dp(12));
        body.addView(hero, hp);

        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView model = text("BMW E46", 28, WHITE, true); top.addView(model, new LinearLayout.LayoutParams(0, dp(40), 1));
        TextView badge = text("320d M47N", 12, BLUE, true); badge.setGravity(Gravity.CENTER); badge.setBackground(bg(Color.rgb(4,15,28), 18)); top.addView(badge, new LinearLayout.LayoutParams(dp(118), dp(32)));
        hero.addView(top);
        hero.addView(text("Coding Lab · Safe Mode", 13, MUTED, false));

        ImageView car = new ImageView(this);
        int res = getResources().getIdentifier("bmw_e46_user_hero", "drawable", getPackageName());
        if (res != 0) car.setImageResource(res);
        car.setScaleType(ImageView.ScaleType.FIT_CENTER); car.setAlpha(0.92f);
        LinearLayout.LayoutParams carLp = new LinearLayout.LayoutParams(-1, 0, 1); carLp.setMargins(0, dp(2), 0, dp(2));
        hero.addView(car, carLp);

        LinearLayout foot = new LinearLayout(this); foot.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout left = new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        left.addView(text(connected ? "● Bluetooth conectado" : "● Bluetooth desconectado", 12, connected ? GREEN : RED, true));
        left.addView(text("READ ONLY · NO WRITE", 12, BLUE, true));
        foot.addView(left, new LinearLayout.LayoutParams(0, dp(42), 1));
        TextView mods = text("DDE  LSZ  GM5", 11, BLUE, true); mods.setGravity(Gravity.CENTER); mods.setBackground(bg(Color.rgb(5,17,32), 16));
        foot.addView(mods, new LinearLayout.LayoutParams(dp(120), dp(34)));
        hero.addView(foot);
    }

    private void chips() {
        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false); hsv.setClipToPadding(false);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        chip(row, "Backup", BLUE, () -> go("backup", true)); chip(row, "Pruebas", YELLOW, () -> go("tests", true)); chip(row, "Diagnóstico", GREEN, () -> go("diag", true)); chip(row, "Logs", PURPLE, () -> go("logs", true)); chip(row, "Confort", PURPLE, () -> go("comfort", true)); chip(row, "LED", YELLOW, () -> go("lights", true));
        hsv.addView(row); body.addView(hsv, new LinearLayout.LayoutParams(-1, dp(48)));
    }

    private void chip(LinearLayout row, String s, int color, Runnable r) {
        TextView v = text(s, 12, color, true); v.setGravity(Gravity.CENTER); v.setPadding(dp(14),0,dp(14),0); v.setBackground(bg(Color.rgb(4,14,26), 22)); v.setOnClickListener(x -> r.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(38)); lp.setMargins(0,0,dp(8),0); row.addView(v, lp);
    }

    private void statusCards() {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        smallStatus(row, "BT", connected ? "OK" : "--", connected ? GREEN : RED);
        smallStatus(row, "ELM", elmReady ? "OK" : "--", elmReady ? GREEN : MUTED);
        smallStatus(row, "BACKUP", backupDone ? "OK" : "--", backupDone ? GREEN : MUTED);
        smallStatus(row, "MODE", "SAFE", BLUE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(58)); lp.setMargins(0,0,0,dp(8)); body.addView(row, lp);
    }

    private void smallStatus(LinearLayout row, String title, String value, int color) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setBackground(bg(Color.rgb(4,14,26), 14));
        TextView a = text(title, 9, MUTED, true); a.setGravity(Gravity.CENTER); TextView b = text(value, 14, color, true); b.setGravity(Gravity.CENTER); box.addView(a); box.addView(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1); lp.setMargins(0,0,dp(6),0); row.addView(box, lp);
    }

    private void section(String s) { TextView v=text(s,11,MUTED,true); v.setLetterSpacing(0.08f); v.setPadding(0,dp(12),0,dp(7)); body.addView(v); }

    private void menu(String icon, String title, String sub, int color, Runnable click) {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(14),0,dp(12),0); row.setBackground(bg(CARD2,14)); row.setOnClickListener(v -> click.run());
        View accent = new View(this); accent.setBackgroundColor(color); LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp(3), dp(34)); ap.setMargins(0,0,dp(14),0); row.addView(accent, ap);
        LinearLayout tx = new LinearLayout(this); tx.setOrientation(LinearLayout.VERTICAL); tx.setGravity(Gravity.CENTER_VERTICAL); tx.addView(text(title,16,WHITE,true)); tx.addView(text(sub,12,MUTED,false)); row.addView(tx, new LinearLayout.LayoutParams(0,-1,1));
        TextView ar = text("›", 28, MUTED, false); ar.setGravity(Gravity.CENTER); row.addView(ar, new LinearLayout.LayoutParams(dp(24), -1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(62)); lp.setMargins(0,0,0,dp(8)); body.addView(row, lp);
    }

    private void showCoding(int activeTab) {
        base("Coding Lab", true, false, 1); tabs(activeTab);
        info("Cambios bloqueados hasta Backup seguro. Escritura real deshabilitada. LSZ/GM5 ya tienen lectura BMW READ ONLY preparada.");
        section("LECTURA DE MODULOS");
        body.addView(action("Leer LSZ / LCM luces READ ONLY",()->realModule("LSZ")));
        body.addView(action("Leer GM5 / ZKE confort READ ONLY",()->realModule("GM5")));
        body.addView(action("Scan LSZ cabeceras",()->scanModule("LSZ")));
        body.addView(action("Scan GM5 cabeceras",()->scanModule("GM5")));
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
        sw.setOnCheckedChangeListener((b,c)->{ if(!backupDone){ b.setChecked(!c); addLog("BLOQUEADO: primero Backup seguro antes de cambiar " + title); } else addLog(realWritePlan(title)); }); row.addView(sw,new LinearLayout.LayoutParams(dp(62),dp(44))); return row;
    }

    private void codingBar() {
        LinearLayout bar=new LinearLayout(this); bar.setPadding(dp(22),dp(8),dp(22),dp(12)); bar.setBackgroundColor(Color.rgb(1,8,16));
        TextView reset=bottomButton("RESTABLECER",false), save=bottomButton("GUARDAR",true); reset.setOnClickListener(v->addLog("Restablecer visual. Sin modificar módulos.")); save.setOnClickListener(v->{ if(!backupDone)addLog("GUARDAR BLOQUEADO: primero Backup seguro."); else addLog("GUARDAR BLOQUEADO: escritura real deshabilitada. Solo WRITE PLAN disponible."); });
        bar.addView(reset,new LinearLayout.LayoutParams(0,dp(52),1)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1); lp.leftMargin=dp(12); bar.addView(save,lp); root.addView(bar,new LinearLayout.LayoutParams(-1,dp(74)));
    }

    private TextView bottomButton(String s, boolean primary) { TextView v=text(s,14,primary?WHITE:BLUE,true); v.setGravity(Gravity.CENTER); v.setBackground(bg(primary?BLUE:Color.TRANSPARENT,10)); return v; }

    private void showBackup() { base("Backup seguro", true, true, 1); info("Obligatorio antes de pruebas avanzadas. Todo READ ONLY."); section("CHECKLIST"); checklistBox=card(checklist(),13); body.addView(checklistBox); section("ACCIONES"); body.addView(action("1 · Conectar Bluetooth",this::realConnect)); body.addView(action("2 · Inicializar ELM327",this::realElm)); body.addView(action("3 · Backup OBD/ECU READ ONLY",this::realBackup)); body.addView(action("4 · Leer LSZ luces READ ONLY",()->realModule("LSZ"))); body.addView(action("5 · Leer GM5 confort READ ONLY",()->realModule("GM5"))); body.addView(action("6 · Scan LSZ cabeceras",()->scanModule("LSZ"))); body.addView(action("7 · Scan GM5 cabeceras",()->scanModule("GM5"))); body.addView(action("8 · Compartir backup completo",this::share)); logSection(); }

    private void showTests() { base("Pruebas", true, true, 1); info("Test guiado. Avanzados bloqueados hasta backup. LSZ/GM5 ejecutan lectura BMW sin escritura."); section("CHECKLIST"); checklistBox=card(checklist(),13); body.addView(checklistBox); section("LECTURA SEGURA"); body.addView(action("Test 1 · Conexión + ELM",()->{realConnect();realElm();})); body.addView(action("Test 2 · Protocolo ISO/KWP",this::realProtocol)); body.addView(action("Test 3 · Motor/PIDs",this::realMotor)); body.addView(action("Test 4 · DTC motor",this::realDtc)); section("MODULOS BMW"); body.addView(action("Test 5 · Init BMW KWP",this::realBmwKwp)); body.addView(action("Test 6 · Leer LSZ / LCM luces",()->realModule("LSZ"))); body.addView(action("Test 7 · Leer GM5 / ZKE confort",()->realModule("GM5"))); body.addView(action("Test 8 · Scan LSZ cabeceras",()->scanModule("LSZ"))); body.addView(action("Test 9 · Scan GM5 cabeceras",()->scanModule("GM5"))); section("WRITE PLAN BLOQUEADO"); body.addView(action("Plan LSZ · LED cold/warm check",()->addLog(elmClient.buildWritePlan("LSZ","LED cold/warm check")))); body.addView(action("Plan GM5 · comfort close / blink",()->addLog(elmClient.buildWritePlan("GM5","comfort close / blink")))); body.addView(action("Exportar sesión",this::share)); logSection(); }

    private String checklist() { return (connected?"[OK]":"[--]")+" Bluetooth\n"+(elmReady?"[OK]":"[--]")+" ELM inicializado\n"+(protocolReady?"[OK]":"[--]")+" Protocolo identificado\n"+(motorRead?"[OK]":"[--]")+" Motor/PIDs leidos\n"+(dtcRead?"[OK]":"[--]")+" DTC leidos\n"+(backupDone?"[OK]":"[--]")+" Backup seguro exportable"; }

    private void realConnect(){String r=elmClient.connect();connected=elmClient.isConnected();addLog(r);refreshStatus();}
    private void realElm(){String r=elmClient.initElm();elmReady=elmClient.isConnected()&&!r.contains("ERROR");addLog(r);refreshStatus();}
    private void realBmwKwp(){String r=elmClient.initBmwKwp();connected=elmClient.isConnected();protocolReady=connected&&!r.contains("ERROR")&&!r.contains("BLOQUEADO");addLog(r);refreshStatus();}
    private void realProtocol(){String r=elmClient.readProtocol();protocolReady=elmClient.isConnected()&&!r.contains("ERROR");addLog(r);refreshStatus();}
    private void realMotor(){String r=elmClient.readMotor();motorRead=elmClient.isConnected()&&!r.contains("ERROR");addLog(r);refreshStatus();}
    private void realDtc(){String r=elmClient.readDtc();dtcRead=elmClient.isConnected()&&!r.contains("ERROR");addLog(r);refreshStatus();}
    private void realModule(String moduleKey){String r=elmClient.readModule(moduleKey);connected=elmClient.isConnected();protocolReady=connected&&!r.contains("ERROR")&&!r.contains("BLOQUEADO");addLog(r);refreshStatus();}
    private void scanModule(String moduleKey){String r=elmClient.scanModuleHeaders(moduleKey);connected=elmClient.isConnected();protocolReady=connected&&!r.contains("ERROR")&&!r.contains("BLOQUEADO");addLog(r);refreshStatus();}
    private void realBackup(){String r=elmClient.backupSafe();connected=elmClient.isConnected();backupDone=connected&&!r.contains("ERROR")&&!r.contains("BLOQUEADO");if(backupDone){elmReady=true;protocolReady=true;motorRead=true;dtcRead=true;}addLog(r+(backupDone?"\nBACKUP OK: RAW exportable. No se ha escrito nada.":""));refreshStatus();}
    private String realWritePlan(String title){String module=title.toLowerCase().contains("led") ? "LSZ" : "GM5"; return elmClient.buildWritePlan(module,title);}

    private void showDiagnostics() { base("Diagnóstico", true, true, 2); section("INFORME MECANICO"); diagnosticBox=card(elmClient.diagnosticText(),14); body.addView(diagnosticBox); section("ACCIONES"); body.addView(action("Conectar ELM327",this::realConnect)); body.addView(action("Inicializar ELM",this::realElm)); body.addView(action("Init BMW KWP",this::realBmwKwp)); body.addView(action("Leer motor básico",this::realMotor)); body.addView(action("Leer DTC",this::realDtc)); body.addView(action("Leer LSZ luces READ ONLY",()->realModule("LSZ"))); body.addView(action("Leer GM5 confort READ ONLY",()->realModule("GM5"))); body.addView(action("Scan LSZ cabeceras",()->scanModule("LSZ"))); body.addView(action("Scan GM5 cabeceras",()->scanModule("GM5"))); body.addView(action("Backup seguro READ ONLY",this::realBackup)); body.addView(action("Compartir sesión completa",this::share)); logSection(); }
    private void showInfo(){base("Información",true,true,3);section("VEHICULO");body.addView(card("BMW E46 320d/320Cd M47N\nMotor: M47N / diesel\nApp: Coding Lab E46 V4.5\nModo: SAFE / READ ONLY",14));section("MODULOS PREPARADOS");body.addView(card(E46ModuleRegistry.describeAll(),13));section("ESTADO DEL SISTEMA");body.addView(card(checklist(),13));section("SEGURIDAD ACTIVA");body.addView(card("SafeCommandBatch activo antes de transmitir.\nPermitido READ ONLY: AT, 01xx, 03, 07, 0A, 21xx, 1Axx.\nBloqueados: 04, 14, 2E, 3B, 30, 31, 34, 36, 37.\nLSZ/GM5: lectura real preparada con scan de cabeceras. Escritura solo WRITE PLAN bloqueado.\nNo borra errores. No escribe módulos.",13));section("OBJETIVOS");body.addView(card("LSZ LED cold/warm check.\nGM5 comfort close.\nBlink unlock.\nVentanillas coupe.\nTodo queda pendiente de backup verificable y lectura de modulo correcta.",13));}
    private void showLogs(){base("Logs",true,true,2);section("EXPORTAR");body.addView(action("Compartir sesión completa",this::share));body.addView(action("Limpiar registro visual",this::clearLog));logSection();}

    private TextView card(String s,int sp){TextView v=text(s,sp,WHITE,false);v.setLineSpacing(dp(2),1.0f);v.setPadding(dp(14),dp(12),dp(14),dp(12));v.setBackground(bg(CARD2,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));v.setLayoutParams(lp);return v;}
    private TextView action(String s,Runnable r){TextView v=card(s + "    ›",14);v.setGravity(Gravity.CENTER_VERTICAL);v.setHeight(dp(54));v.setOnClickListener(x->{ if(busy){addLog("ESPERA: operación ya en curso.");return;} new Thread(()->runTask(r)).start();});return v;}
    private void logSection(){section("REGISTRO");logBox=card(log.toString(),12);body.addView(logBox,new LinearLayout.LayoutParams(-1,dp(245)));}
    private void addLog(String s){log.append("[").append(logTime.format(new Date())).append("] ").append(s).append("\n\n");refreshStatus();}
    private void clearLog(){log.setLength(0);log.append("BMW E46 SCANNER SESSION\n").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())).append("\nSAFE MODE - READ ONLY\n\n");addLog("Registro visual reiniciado.");}

    private void bottomNav(int active){LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(6),dp(4),dp(6),dp(4));nav.setBackgroundColor(Color.rgb(1,8,16));String[] labels={"⌂\nInicio","◇\nMódulos","▤\nLogs","⚙\nAjustes"};Runnable[] actions={()->go("home",false),()->go("coding",true),()->go("logs",true),()->go("info",true)};for(int i=0;i<4;i++){final int idx=i;TextView v=text(labels[i],12,i==active?BLUE:MUTED,i==active);v.setGravity(Gravity.CENTER);if(i==active)v.setBackground(bg(Color.rgb(4,16,30),16));v.setOnClickListener(x->actions[idx].run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);lp.setMargins(dp(3),0,dp(3),0);nav.addView(v,lp);}root.addView(nav,new LinearLayout.LayoutParams(-1,dp(66)));}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session");i.putExtra(Intent.EXTRA_TEXT,log.toString());startActivity(Intent.createChooser(i,"Enviar sesión"));}
}
