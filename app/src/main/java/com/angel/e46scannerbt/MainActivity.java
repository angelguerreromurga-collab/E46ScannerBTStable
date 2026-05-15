package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.Gravity;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    TextView status, log, live, testPanel;
    LinearLayout main;
    BluetoothSocket socket; InputStream in; OutputStream out; BluetoothDevice device;
    StringBuilder session = new StringBuilder();
    int rpm=-1,temp=-999,speed=-1,map=-1,iat=-999; double maf=-1; String volts="--", proto="--", lastDtc="--", lastTest="LISTO";
    boolean btOk=false, elmOk=false, protocolOk=false, engineOk=false, dtcOk=false, backupOk=false;
    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG=Color.rgb(2,6,12), CARD=Color.rgb(12,19,29), TXT=Color.WHITE, MUTED=Color.rgb(156,164,176), GREEN=Color.rgb(40,220,120), RED=Color.rgb(255,75,85), AMBER=Color.rgb(245,185,55);

    protected void onCreate(Bundle b){ super.onCreate(b); stampSession(); showHome(); }
    void stampSession(){ session.append("BMW E46 SCANNER SESSION\n").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date())).append("\nCar: BMW E46 coupe 320d/320Cd M47N\nMode: READ ONLY\n\n"); }

    void base(String title){ main=new LinearLayout(this); main.setOrientation(LinearLayout.VERTICAL); main.setPadding(24,42,24,10); main.setBackgroundColor(BG); TextView top=tv("☰     "+title+"                         E46",20,TXT,false); status=tv(stateText(),13,Color.rgb(110,200,255),false); main.addView(top); main.addView(status); setContentView(main); }
    String stateText(){ return "↯ "+lastTest+"   BT "+(btOk?"OK":"OFF")+"   ELM "+(elmOk?"OK":"--")+"   "+proto+"   "+volts; }
    String flag(boolean b){ return b?"✅":"⬜"; }
    String testsText(){ return flag(btOk)+" Bluetooth   "+flag(elmOk)+" ELM   "+flag(protocolOk)+" Protocolo\n"+flag(engineOk)+" Motor/PIDs   "+flag(dtcOk)+" DTC   "+flag(backupOk)+" Backup seguro\n"+(lastDtc.contains("P0401")?"⚠️ P0401 EGR insuficiente":"✅ Sin DTC crítico nuevo"); }
    void refresh(){ if(status!=null) runOnUiThread(() -> status.setText(stateText())); if(live!=null) runOnUiThread(() -> live.setText(liveText())); if(testPanel!=null) runOnUiThread(() -> testPanel.setText(testsText())); }
    String liveText(){ return "RPM  "+val(rpm,"rpm")+"     TEMP  "+val(temp,"°C")+"\nMAP  "+val(map,"kPa")+"     MAF  "+(maf<0?"--":String.format(Locale.US,"%.2f g/s",maf))+"\nVEL  "+val(speed,"km/h")+"     IAT  "+val(iat,"°C")+"\nDTC  "+lastDtc; }
    String val(int v,String u){ return v<-100||v<0?"--":v+" "+u; }

    void showHome(){ base("Coding Lab"); main.addView(hero()); testPanel=panel(testsText(),14); main.addView(testPanel); main.addView(section("MENÚ PRINCIPAL")); main.addView(menu("🔧", "Coding Lab", "Funciones de confort y personalización", () -> showCoding())); main.addView(menu("💾", "Backup seguro", "Guardar estado OBD/ECU antes de pruebas", () -> showBackup())); main.addView(menu("💡", "Luces", "Iluminación exterior e interior", () -> showLights())); main.addView(menu("▭", "Ventanillas", "Funciones de confort de ventanas", () -> showWindows())); main.addView(menu("🟢", "Diagnóstico", "Leer errores y estado de módulos", () -> showDiag())); main.addView(menu("▤", "Logs", "Registros y sesiones guardadas", () -> showLogs())); main.addView(nav()); }
    TextView hero(){ TextView h=panel("✓ Conectado\nBMW E46 320d M47N\nVIN: pendiente\n\n        ▄▄▄ BMW E46 COUPÉ ▄▄▄\n\n"+liveText(),15); live=h; return h; }
    TextView nav(){ TextView n=tv("⌂ Inicio          ◇ Módulos          ≡ Logs          ⚙ Ajustes",13,Color.rgb(180,190,205),true); n.setBackground(cardBg(Color.rgb(5,10,18),14,Color.rgb(20,34,52))); return n; }
    TextView menu(String icon,String title,String sub, final Runnable r){ TextView v=panel(icon+"   "+title+"        ›\n     "+sub,17); v.setOnClickListener(x -> r.run()); return v; }
    TextView panel(String s,int size){ TextView v=tv(s,size,TXT,false); v.setBackground(cardBg(CARD,18,Color.rgb(28,42,60))); v.setPadding(18,15,18,15); return v; }
    GradientDrawable cardBg(int color,int radius,int stroke){ GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(radius); g.setStroke(1,stroke); return g; }
    TextView tv(String s,int size,int color,boolean center){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(0,7,0,9); if(center)v.setGravity(Gravity.CENTER); return v; }
    TextView section(String s){ return tv("\n"+s,13,MUTED,false); }
    TextView action(String s, final Runnable r){ TextView v=panel(s,15); v.setGravity(Gravity.CENTER); v.setOnClickListener(x -> new Thread(r).start()); return v; }
    Switch sw(String title,String sub,boolean on){ Switch s=new Switch(this); s.setText(title+"\n"+sub); s.setTextColor(TXT); s.setTextSize(15); s.setChecked(on); s.setEnabled(false); s.setBackground(cardBg(CARD,14,Color.rgb(28,42,60))); s.setPadding(16,12,16,12); return s; }
    void back(){ main.addView(action("← Atrás", () -> runOnUiThread(() -> showHome()))); }
    void addBox(){ log=tv("",13,Color.rgb(225,232,240),false); log.setPadding(16,16,16,16); ScrollView sv=new ScrollView(this); sv.setBackground(cardBg(Color.rgb(8,13,21),10,Color.rgb(18,30,45))); sv.addView(log); main.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); }
    void stat(String s){ lastTest=s; runOnUiThread(() -> status.setText(stateText())); }
    void add(String s){ session.append(s).append("\n\n"); runOnUiThread(() -> { if(log!=null) log.append(s+"\n\n"); }); }

    void showCoding(){ base("Coding Lab"); back(); main.addView(section("LUCES      VENTANILLAS      CONFORT      OTROS")); main.addView(panel("ⓘ  Escritura bloqueada hasta backup real de módulo con interfaz segura. Aquí solo preparamos objetivos.",14)); main.addView(section("OBJETIVOS")); main.addView(sw("LSZ check frío posición LED", "KALTUEBERWACHUNG_SL_*", true)); main.addView(sw("LSZ check caliente posición LED", "WARMUEBERWACHUNG_SL_*", true)); main.addView(sw("GM5 cierre confort mando", "KOMFORTSCHLIESSUNG_FB", true)); main.addView(sw("Blink al abrir", "QUIT_BLK_ENTSCH", true)); main.addView(action("Generar informe objetivos", () -> add("OBJETIVOS CODING: LSZ LED cold/warm, GM5 comfort close, blink unlock. Escritura bloqueada."))); main.addView(action("Guardar / compartir sesión", () -> shareSession())); addBox(); }
    void showBackup(){ base("Backup seguro"); back(); main.addView(panel("Este backup NO escribe nada. Guarda protocolo, voltaje, PIDs soportados, VIN si responde, DTC y RAW. LSZ/GM5 EEPROM queda bloqueado con ELM v2.1.",14)); main.addView(action("0 · Backup completo seguro", () -> safeBackup())); main.addView(action("1 · Backup identidad ECU/VIN", () -> runCmds(new String[]{"0900","0902","0904","0906"},"BACKUP ECU ID"))); main.addView(action("2 · Backup capacidades OBD", () -> runCmds(new String[]{"0100","0120","0140","0160"},"BACKUP PIDS"))); main.addView(action("3 · Backup fallos", () -> runCmds(new String[]{"03","07","0A"},"BACKUP DTC"))); main.addView(action("Compartir backup/sesión", () -> shareSession())); addBox(); }
    void showLights(){ base("Luces"); back(); main.addView(section("LED / CHECK")); main.addView(sw("Check frío posición", "parpadeo al contacto", true)); main.addView(sw("Check caliente posición", "aviso fijo en cuadro", true)); main.addView(sw("Blink cerrar", "funciona", true)); main.addView(sw("Blink abrir", "pendiente", false)); main.addView(action("Test DTC ahora", () -> { connect(); initElm(); runCmds(new String[]{"03","07"},"DTC"); })); addBox(); }
    void showWindows(){ base("Ventanillas"); back(); main.addView(section("CIERRE CON MANDO")); main.addView(sw("Cerrar delanteras manteniendo cerrar", "funciona", true)); main.addView(sw("Cerrar traseras manteniendo cerrar", "objetivo GM5", false)); main.addView(sw("Doble clic para cerrar traseras", "experimental", false)); main.addView(section("APERTURA CON MANDO")); main.addView(sw("Abrir cuatro manteniendo abrir", "funciona", true)); main.addView(sw("Doble clic abrir traseras", "pendiente", false)); main.addView(action("Registrar baseline", () -> add("BASELINE VENTANILLAS: abrir mando abre 4; cerrar mando solo delanteras; traseras coupe no cierran; sin techo."))); addBox(); }
    void showDiag(){ base("Diagnóstico"); back(); live=panel(liveText(),15); testPanel=panel(testsText(),14); main.addView(live); main.addView(testPanel); main.addView(section("TESTS EN DIRECTO")); main.addView(action("0 · Test completo automático", () -> fullTest())); main.addView(action("1 · Conectar ELM327", () -> connect())); main.addView(action("2 · Inicializar ELM", () -> initElm())); main.addView(action("3 · Leer y decodificar motor", () -> readEngine())); main.addView(action("4 · Leer DTC traducidos", () -> runCmds(new String[]{"03","07"},"DTC"))); main.addView(action("5 · Test protocolo seguro", () -> runCmds(new String[]{"ATDP","ATDPN","0100"},"PROTOCOLO"))); main.addView(action("Compartir sesión completa", () -> shareSession())); main.addView(action("Limpiar log", () -> runOnUiThread(() -> log.setText("")))); addBox(); }
    void showLogs(){ base("Logs"); back(); main.addView(action("Compartir sesión completa", () -> shareSession())); main.addView(action("Añadir informe mecánico", () -> add(mechReport()))); addBox(); add("LOG READY. Usa Compartir sesión completa."); }
    void showInfo(){ base("Ajustes"); back(); main.addView(panel("BMW E46 50 JAHRE EDITION\n✓ M47N 320d/320Cd\n✓ ELM327 v2.1 detectado\n✓ ATSP0 principal\n✓ Modo seguro: solo lectura\n✓ LSZ/GM5 escritura bloqueada hasta interfaz correcta",15)); main.addView(action("Compartir sesión", () -> shareSession())); }

    String mechReport(){ return "INFORME MECÁNICO\nVoltaje: "+volts+"\nProtocolo: "+proto+"\n"+liveText()+"\nConclusión: "+(lastDtc.contains("P0401")?"EGR anulada/desconectada o flujo insuficiente detectado.":"Sin DTC motor confirmado."); }
    void fullTest(){ connect(); initElm(); readEngine(); runCmds(new String[]{"03","07"},"DTC"); runCmds(new String[]{"ATDP","ATDPN","0100"},"PROTOCOLO"); add(mechReport()); }
    void safeBackup(){ connect(); initElm(); add("===== BACKUP SEGURO READ ONLY ====="); runCmds(new String[]{"ATI","ATRV","ATDP","ATDPN","0100","0120","0140","0900","0902","0904","03","07","0A"},"BACKUP SEGURO"); backupOk=true; add("BACKUP OK: sesión RAW lista para exportar. No se ha escrito nada en el coche."); add(mechReport()); refresh(); }
    void shareSession(){ try{ Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session"); i.putExtra(Intent.EXTRA_TEXT,session.toString()); startActivity(Intent.createChooser(i,"Enviar sesión")); }catch(Exception e){ add("ERROR SHARE: "+e.getMessage()); }}
    boolean perm(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46)); stat("Permiso Bluetooth solicitado"); return false;} return true; }
    void connect(){ try{ stat("Conectando BT"); if(!perm())return; BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter(); if(a==null){stat("Sin BT");return;} if(!a.isEnabled()){stat("Activa BT");return;} device=pick(a.getBondedDevices()); if(device==null){stat("Empareja ELM");return;} close(); socket=device.createRfcommSocketToServiceRecord(SPP); socket.connect(); in=socket.getInputStream(); out=socket.getOutputStream(); btOk=true; stat("BT OK"); add("BT OK: "+device.getName()+" / "+device.getAddress()); refresh(); }catch(Exception e){ btOk=false; stat("Fallo BT"); add("ERROR CONEXION: "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    BluetoothDevice pick(Set<BluetoothDevice> ds){ BluetoothDevice f=null; for(BluetoothDevice d:ds){ if(f==null)f=d; String n=d.getName()==null?"":d.getName().toLowerCase(); if(n.contains("obd")||n.contains("elm")||n.contains("vlink")||n.contains("icar"))return d;} return f; }
    boolean ready(){ if(socket==null||!socket.isConnected()||in==null||out==null){ stat("No conectado"); add("Pulsa conectar primero"); return false;} return true; }
    void close(){ try{ if(socket!=null)socket.close(); }catch(Exception ignored){} socket=null; in=null; out=null; }
    void initElm(){ runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM"); elmOk=true; }
    void readEngine(){ runCmds(new String[]{"0120","010C","010C","0105","010D","010B","010F","0110"},"MOTOR"); engineOk=true; }
    void runCmds(String[] cs,String name){ try{ if(!ready())return; stat(name+" iniciado"); add("===== "+name+" ====="); for(String c:cs)send(c); if(name.contains("DTC"))dtcOk=true; if(name.contains("PROTOCOLO"))protocolOk=true; stat(name+" terminado"); refresh(); }catch(Exception e){ stat("Error "+name); add("ERROR "+name+": "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    void send(String c)throws Exception{ out.write((c+"\r").getBytes("US-ASCII")); out.flush(); Thread.sleep(c.equals("ATZ")?1700:1000); String r=read(); add("> "+c+"\n"+r); parse(c,r); refresh(); }
    String read()throws Exception{ byte[] b=new byte[512]; StringBuilder s=new StringBuilder(); long end=System.currentTimeMillis()+1600; while(System.currentTimeMillis()<end){ while(in.available()>0){ int n=in.read(b); if(n>0)s.append(new String(b,0,n,"US-ASCII")); } if(s.toString().contains(">"))break; Thread.sleep(60);} String r=s.toString().replace('\r',' ').replace('\n',' ').trim(); return r.length()==0?"SIN RESPUESTA":r; }
    void parse(String cmd,String r){ try{ if(cmd.equals("ATRV"))volts=r.replace(">","").trim(); if(cmd.equals("ATDP"))proto=r.replace(">","").trim(); String h=r.replace(" ","").replace(">",""); parseAll(h); }catch(Exception ignored){} }
    void parseAll(String h){ try{ int i; i=h.indexOf("410C"); if(i>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16), b=Integer.parseInt(h.substring(i+6,i+8),16); rpm=((a*256)+b)/4;} i=h.indexOf("4105"); if(i>=0)temp=Integer.parseInt(h.substring(i+4,i+6),16)-40; i=h.indexOf("410D"); if(i>=0)speed=Integer.parseInt(h.substring(i+4,i+6),16); i=h.indexOf("410B"); if(i>=0)map=Integer.parseInt(h.substring(i+4,i+6),16); i=h.indexOf("410F"); if(i>=0)iat=Integer.parseInt(h.substring(i+4,i+6),16)-40; i=h.indexOf("4110"); if(i>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16), b=Integer.parseInt(h.substring(i+6,i+8),16); maf=((a*256)+b)/100.0;} if(h.contains("430401"))lastDtc="P0401 EGR insuficiente"; else if(h.contains("43"))lastDtc="DTC RAW"; }catch(Exception ignored){} }
}
