package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.view.Gravity;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    TextView status, log;
    LinearLayout main;
    BluetoothSocket socket; InputStream in; OutputStream out; BluetoothDevice device;
    StringBuilder session = new StringBuilder();
    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG=Color.rgb(4,8,14), CARD=Color.rgb(14,20,30), TXT=Color.WHITE, MUTED=Color.rgb(160,168,178);

    protected void onCreate(Bundle b){ super.onCreate(b); stampSession(); showHome(); }

    void stampSession(){ session.append("BMW E46 SCANNER SESSION\n"); session.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date())).append("\n"); session.append("Car: BMW E46 coupe 320d/320Cd M47N\nMode: READ ONLY\n\n"); }
    void base(String title){ main=new LinearLayout(this); main.setOrientation(LinearLayout.VERTICAL); main.setPadding(22,58,22,14); main.setBackgroundColor(BG); TextView top=tv(title,24,TXT,true); status=tv("Bluetooth: desconectado · modo seguro",14,Color.rgb(120,200,255),false); main.addView(top); main.addView(status); setContentView(main); }
    void showHome(){ base("Coding Lab E46"); main.addView(tv("BMW E46 320d M47N\nREAD ONLY hasta backup real de modulo",15,Color.rgb(220,225,232),false)); main.addView(card("🔧  Coding Lab", "Funciones confort OEM bloqueadas", () -> showCoding())); main.addView(card("💡  Luces / LED Check", "Posicion LED, check frio/caliente", () -> showLights())); main.addView(card("🪟  Ventanillas", "Cierre confort y traseras coupe", () -> showWindows())); main.addView(card("🧪  Diagnostico", "ELM327, PIDs, DTC, protocolos", () -> showDiag())); main.addView(card("📋  Logs / Compartir", "Enviar sesion completa", () -> showLogs())); main.addView(card("ℹ️  Info coche", "Baseline y limites", () -> showInfo())); }
    TextView card(String a,String b, final Runnable r){ TextView v=tv(a+"\n"+b,17,TXT,false); v.setBackgroundColor(CARD); v.setPadding(18,16,18,16); v.setOnClickListener(x -> r.run()); return v; }
    TextView tv(String s,int size,int color,boolean center){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(0,7,0,9); if(center)v.setGravity(Gravity.CENTER); return v; }
    TextView section(String s){ return tv("\n"+s,13,MUTED,false); }
    Button btn(String s, final Runnable r){ Button b=new Button(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false); b.setOnClickListener(v -> new Thread(r).start()); return b; }
    Switch sw(String title,String sub,boolean on){ Switch s=new Switch(this); s.setText(title+"\n"+sub); s.setTextColor(TXT); s.setTextSize(15); s.setChecked(on); s.setEnabled(false); return s; }
    void back(){ main.addView(btn("← Inicio", () -> runOnUiThread(() -> showHome()))); }
    void addBox(){ log=tv("",13,Color.rgb(225,232,240),false); log.setPadding(16,16,16,16); ScrollView sv=new ScrollView(this); sv.setBackgroundColor(Color.rgb(10,15,23)); sv.addView(log); main.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); }
    void stat(String s){ runOnUiThread(() -> status.setText(s)); }
    void add(String s){ session.append(s).append("\n\n"); runOnUiThread(() -> { if(log!=null) log.append(s+"\n\n"); }); }

    void showCoding(){ base("Coding Lab"); back(); main.addView(section("ESTADO")); main.addView(tv("ESCRITURA BLOQUEADA. Primero lectura, backup y validacion. No tocar LSZ/GM5 a ciegas.",15,Color.rgb(230,235,240),false)); main.addView(sw("LSZ check frio posicion LED", "KALTUEBERWACHUNG_SL_*", true)); main.addView(sw("LSZ check caliente posicion LED", "WARMUEBERWACHUNG_SL_*", true)); main.addView(sw("GM5 cierre confort mando", "KOMFORTSCHLIESSUNG_FB", true)); main.addView(sw("Blink al abrir", "QUIT_BLK_ENTSCH", true)); main.addView(btn("Generar informe objetivos", () -> add("OBJETIVOS CODING: LSZ LED cold/warm, GM5 comfort close, blink unlock. Escritura bloqueada."))); main.addView(btn("Compartir sesion", () -> shareSession())); addBox(); }
    void showLights(){ base("Luces / LED Check"); back(); main.addView(section("POSICION LED")); main.addView(sw("Parpadeo al contacto", "Check frio activo", true)); main.addView(sw("Aviso fijo en cuadro", "Check caliente probable", true)); main.addView(section("CONFIRMACION")); main.addView(sw("Blink cerrar", "Funciona", true)); main.addView(sw("Blink abrir", "Pendiente", false)); main.addView(btn("Leer DTC ahora", () -> { connect(); initElm(); runCmds(new String[]{"03","07"},"DTC"); })); main.addView(btn("Compartir sesion", () -> shareSession())); addBox(); }
    void showWindows(){ base("Ventanillas"); back(); main.addView(sw("Abrir 4 con mando", "Funciona manteniendo abrir", true)); main.addView(sw("Cerrar delanteras con mando", "Funciona", true)); main.addView(sw("Cerrar traseras coupe", "Objetivo GM5", false)); main.addView(sw("One-touch cierre trasero", "Experimental", false)); main.addView(btn("Registrar baseline", () -> add("BASELINE VENTANILLAS: abrir mando abre 4; cerrar mando solo delanteras; traseras coupe no cierran con mando; sin techo."))); main.addView(btn("Compartir sesion", () -> shareSession())); addBox(); }
    void showDiag(){ base("Diagnostico"); back(); main.addView(section("CONEXION")); main.addView(btn("1 · Conectar ELM327", () -> connect())); main.addView(btn("2 · Inicializar ELM", () -> initElm())); main.addView(section("LECTURAS")); main.addView(btn("3 · Leer motor basico", () -> readEngine())); main.addView(btn("4 · Leer DTC", () -> runCmds(new String[]{"03","07"},"DTC"))); main.addView(btn("5 · Probar ISO/KWP", () -> protocols())); main.addView(btn("Compartir sesion completa", () -> shareSession())); main.addView(btn("Limpiar log", () -> runOnUiThread(() -> log.setText("")))); addBox(); }
    void showLogs(){ base("Logs / Compartir"); back(); main.addView(btn("Compartir sesion completa", () -> shareSession())); main.addView(btn("Copiar resumen al log", () -> add("RESUMEN: BT/ELM/DTC/PIDs RAW listos para enviar a ChatGPT."))); addBox(); add("LOG READY. Usa Compartir sesion completa."); }
    void showInfo(){ base("Info coche"); back(); main.addView(tv("BMW E46 coupe/restyling 320d/320Cd M47N\nELM327 clon v2.1\nLectura motor viable\nLSZ/GM5 por ELM no garantizado\nModo seguro: solo lectura\nPrioridad v1.0: parser + export + estabilidad",15,Color.rgb(230,235,240),false)); main.addView(btn("Compartir sesion", () -> shareSession())); }

    void shareSession(){ try{ Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session"); i.putExtra(Intent.EXTRA_TEXT,session.toString()); startActivity(Intent.createChooser(i,"Enviar sesion a ChatGPT")); }catch(Exception e){ add("ERROR SHARE: "+e.getMessage()); }}
    boolean perm(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46)); stat("Permiso Bluetooth solicitado"); return false;} return true; }
    void connect(){ try{ if(!perm())return; BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter(); if(a==null){stat("Sin Bluetooth");return;} if(!a.isEnabled()){stat("Activa Bluetooth");return;} device=pick(a.getBondedDevices()); if(device==null){stat("Empareja ELM primero");return;} close(); stat("Conectando "+device.getName()); socket=device.createRfcommSocketToServiceRecord(SPP); socket.connect(); in=socket.getInputStream(); out=socket.getOutputStream(); stat("Bluetooth conectado"); add("BT OK: "+device.getName()+" / "+device.getAddress()); }catch(Exception e){ stat("Error conexion"); add("ERROR CONEXION: "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    BluetoothDevice pick(Set<BluetoothDevice> ds){ BluetoothDevice f=null; for(BluetoothDevice d:ds){ if(f==null)f=d; String n=d.getName()==null?"":d.getName().toLowerCase(); if(n.contains("obd")||n.contains("elm")||n.contains("vlink")||n.contains("icar"))return d;} return f; }
    boolean ready(){ if(socket==null||!socket.isConnected()||in==null||out==null){ stat("No conectado"); add("Pulsa conectar primero"); return false;} return true; }
    void close(){ try{ if(socket!=null)socket.close(); }catch(Exception ignored){} socket=null; in=null; out=null; }
    void initElm(){ runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM"); }
    void readEngine(){ runCmds(new String[]{"0100","0120","0140","010C","0105","010D","010B","010F","0110","0142"},"MOTOR BASICO"); }
    void protocols(){ runCmds(new String[]{"ATSP0","0100","ATSP3","0100","ATSP4","0100","ATSP5","0100","ATSP0"},"PROTOCOLOS"); }
    void runCmds(String[] cs,String name){ try{ if(!ready())return; stat(name); add("===== "+name+" ====="); for(String c:cs)send(c); stat(name+" terminado"); }catch(Exception e){ stat("Error"); add("ERROR "+name+": "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    void send(String c)throws Exception{ out.write((c+"\r").getBytes("US-ASCII")); out.flush(); Thread.sleep(c.equals("ATZ")?1700:900); add("> "+c+"\n"+read()); }
    String read()throws Exception{ byte[] b=new byte[512]; StringBuilder s=new StringBuilder(); long end=System.currentTimeMillis()+1400; while(System.currentTimeMillis()<end){ while(in.available()>0){ int n=in.read(b); if(n>0)s.append(new String(b,0,n,"US-ASCII")); } if(s.toString().contains(">"))break; Thread.sleep(60);} String r=s.toString().replace('\r',' ').replace('\n',' ').trim(); return r.length()==0?"SIN RESPUESTA":r; }
}
