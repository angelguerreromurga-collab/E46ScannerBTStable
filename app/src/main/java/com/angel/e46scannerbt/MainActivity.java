package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.view.Gravity;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    TextView status, content, log;
    LinearLayout main;
    BluetoothSocket socket; InputStream in; OutputStream out; BluetoothDevice device;
    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG=Color.rgb(4,8,14), CARD=Color.rgb(14,20,30), BLUE=Color.rgb(0,122,255), TXT=Color.WHITE, MUTED=Color.rgb(160,168,178);

    protected void onCreate(Bundle b){ super.onCreate(b); showHome(); }

    void base(String title){
        main=new LinearLayout(this); main.setOrientation(LinearLayout.VERTICAL); main.setPadding(22,58,22,14); main.setBackgroundColor(BG);
        TextView top=tv(title,24,TXT,true); status=tv("Bluetooth: desconectado · modo seguro",14,Color.rgb(120,200,255),false);
        main.addView(top); main.addView(status); setContentView(main);
    }

    void showHome(){
        base("Coding Lab E46");
        content=tv("BMW E46 320d M47N\nREAD ONLY hasta backup real de modulo",15,Color.rgb(220,225,232),false); content.setPadding(6,8,6,18); main.addView(content);
        main.addView(card("🔧  Coding Lab", "Funciones de confort y personalizacion", () -> showCoding()));
        main.addView(card("💡  Luces / LED Check", "Posicion LED, check frio/caliente, blink", () -> showLights()));
        main.addView(card("🪟  Ventanillas", "Cierre confort, traseras coupe, mando", () -> showWindows()));
        main.addView(card("🧪  Diagnostico", "ELM327, PIDs, DTC, protocolos", () -> showDiag()));
        main.addView(card("📋  Logs", "RAW para mandar a ChatGPT", () -> showLogs()));
        main.addView(card("ℹ️  Informacion coche", "Baseline E46 y limites reales", () -> showInfo()));
    }

    TextView card(String a,String b, final Runnable r){ TextView v=tv(a+"\n"+b,17,TXT,false); v.setBackgroundColor(CARD); v.setPadding(18,16,18,16); v.setOnClickListener(x -> r.run()); return v; }
    TextView section(String s){ TextView v=tv("\n"+s,13,MUTED,false); return v; }
    TextView tv(String s,int size,int color,boolean center){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(0,7,0,9); if(center)v.setGravity(Gravity.CENTER); return v; }
    Button btn(String s, final Runnable r){ Button b=new Button(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false); b.setOnClickListener(v -> new Thread(r).start()); return b; }
    Switch sw(String title,String sub,boolean on){ Switch s=new Switch(this); s.setText(title+"\n"+sub); s.setTextColor(TXT); s.setTextSize(15); s.setChecked(on); s.setPadding(10,10,10,10); s.setEnabled(false); return s; }
    void back(){ Button b=btn("← Inicio", () -> runOnUiThread(() -> showHome())); main.addView(b); }
    void stat(String s){ runOnUiThread(() -> status.setText(s)); }
    void add(String s){ runOnUiThread(() -> log.append(s+"\n\n")); }

    void showCoding(){ base("Coding Lab"); back(); main.addView(section("ESTADO")); main.addView(tv("Escritura bloqueada. Primero hay que leer, validar y guardar backup. No voy a dejar que una app toque LSZ/GM5 a ciegas.",15,Color.rgb(230,235,240),false)); main.addView(btn("Leer capacidades ELM / protocolo", () -> { connect(); initElm(); })); main.addView(section("OBJETIVOS CONFIGURADOS")); main.addView(sw("LSZ: desactivar check frio posicion LED", "KALTUEBERWACHUNG_SL_* = nicht_aktiv", true)); main.addView(sw("LSZ: desactivar check caliente posicion LED", "WARMUEBERWACHUNG_SL_* = nicht_aktiv", true)); main.addView(sw("GM5: cierre confort mando", "KOMFORTSCHLIESSUNG_FB = aktiv", true)); main.addView(sw("LSZ/GM5: blink al abrir", "QUIT_BLK_ENTSCH / QUIT_OPT_ENTSCH", true)); main.addView(btn("Guardar informe de objetivos en log", () -> logTargets())); addLogBox(); }
    void showLights(){ base("Luces / LED Check"); back(); main.addView(section("POSICION DELANTERA LED")); main.addView(sw("Check frio posicion delantera", "Parpadeo al contacto", true)); main.addView(sw("Check caliente posicion delantera", "Aviso bombilla encendida", true)); main.addView(section("CONFIRMACION VISUAL")); main.addView(sw("Parpadeo al cerrar", "Ya funciona en tu coche", true)); main.addView(sw("Parpadeo al abrir", "Pendiente de coding", false)); main.addView(btn("Leer DTC motor ahora", () -> { connect(); initElm(); runCmds(new String[]{"03","07"},"DTC"); })); addLogBox(); }
    void showWindows(){ base("Ventanillas"); back(); main.addView(section("CIERRE CON MANDO")); main.addView(sw("Cerrar delanteras manteniendo cerrar", "Funciona actualmente", true)); main.addView(sw("Cerrar traseras coupe manteniendo cerrar", "Objetivo GM5", false)); main.addView(sw("Doble clic traseras", "Experimental, posible limite hardware", false)); main.addView(section("APERTURA CON MANDO")); main.addView(sw("Abrir cuatro manteniendo abrir", "Funciona actualmente", true)); main.addView(btn("Registrar baseline ventanillas", () -> logWindows())); addLogBox(); }
    void showDiag(){ base("Diagnostico"); back(); main.addView(section("CONEXION")); main.addView(btn("1 · Conectar ELM327", () -> connect())); main.addView(btn("2 · Inicializar ELM", () -> initElm())); main.addView(section("LECTURAS")); main.addView(btn("3 · Leer motor basico", () -> readEngine())); main.addView(btn("4 · Leer DTC", () -> runCmds(new String[]{"03","07"},"DTC"))); main.addView(btn("5 · Probar ISO/KWP", () -> protocols())); main.addView(btn("Limpiar log", () -> runOnUiThread(() -> log.setText("")))); addLogBox(); }
    void showLogs(){ base("Logs"); back(); addLogBox(); add("LOG READY. Copia o haz captura completa."); }
    void showInfo(){ base("Informacion coche"); back(); main.addView(tv("BMW E46 coupe/restyling 320d/320Cd M47N\nELM327 detectado: clon v2.1\nLectura motor: viable\nLSZ/GM5 por ELM: no garantizado\nModo actual: seguro, solo lectura\nPrioridad: interpretar PIDs y generar informe",15,Color.rgb(230,235,240),false)); }
    void addLogBox(){ log=tv("",13,Color.rgb(225,232,240),false); log.setPadding(16,16,16,16); ScrollView sv=new ScrollView(this); sv.setBackgroundColor(Color.rgb(10,15,23)); sv.addView(log); main.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); }

    void logTargets(){ add("OBJETIVOS: LSZ LED check frio/caliente; blink abrir; GM5 cierre confort traseras; escritura bloqueada hasta backup."); }
    void logWindows(){ add("BASELINE: abrir mando abre cuatro; cerrar mando cierra delanteras; traseras coupe no cierran con mando; sin techo solar."); }

    boolean perm(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46)); stat("Permiso Bluetooth solicitado"); return false;} return true; }
    void connect(){ try{ if(!perm())return; BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter(); if(a==null){stat("Sin Bluetooth");return;} if(!a.isEnabled()){stat("Activa Bluetooth");return;} device=pick(a.getBondedDevices()); if(device==null){stat("Empareja ELM primero");return;} close(); stat("Conectando "+device.getName()); socket=device.createRfcommSocketToServiceRecord(SPP); socket.connect(); in=socket.getInputStream(); out=socket.getOutputStream(); stat("Bluetooth conectado"); if(log!=null)add("BT OK: "+device.getName()+" / "+device.getAddress()); }catch(Exception e){ stat("Error conexion"); if(log!=null)add("ERROR CONEXION: "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    BluetoothDevice pick(Set<BluetoothDevice> ds){ BluetoothDevice f=null; for(BluetoothDevice d:ds){ if(f==null)f=d; String n=d.getName()==null?"":d.getName().toLowerCase(); if(n.contains("obd")||n.contains("elm")||n.contains("vlink")||n.contains("icar"))return d;} return f; }
    boolean ready(){ if(socket==null||!socket.isConnected()||in==null||out==null){ stat("No conectado"); if(log!=null)add("Pulsa conectar primero"); return false;} return true; }
    void close(){ try{ if(socket!=null)socket.close(); }catch(Exception ignored){} socket=null; in=null; out=null; }
    void initElm(){ runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM"); }
    void readEngine(){ runCmds(new String[]{"0100","0120","0140","010C","0105","010D","010B","010F","0110","0142"},"MOTOR BASICO"); }
    void protocols(){ runCmds(new String[]{"ATSP0","0100","ATSP3","0100","ATSP4","0100","ATSP5","0100","ATSP0"},"PROTOCOLOS"); }
    void runCmds(String[] cs,String name){ try{ if(!ready())return; stat(name); add("===== "+name+" ====="); for(String c:cs)send(c); stat(name+" terminado"); }catch(Exception e){ stat("Error"); add("ERROR "+name+": "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    void send(String c)throws Exception{ out.write((c+"\r").getBytes("US-ASCII")); out.flush(); Thread.sleep(c.equals("ATZ")?1700:900); add("> "+c+"\n"+read()); }
    String read()throws Exception{ byte[] b=new byte[512]; StringBuilder s=new StringBuilder(); long end=System.currentTimeMillis()+1400; while(System.currentTimeMillis()<end){ while(in.available()>0){ int n=in.read(b); if(n>0)s.append(new String(b,0,n,"US-ASCII")); } if(s.toString().contains(">"))break; Thread.sleep(60);} String r=s.toString().replace('\r',' ').replace('\n',' ').trim(); return r.length()==0?"SIN RESPUESTA":r; }
}
