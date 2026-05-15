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
    TextView status, log, live;
    LinearLayout main;
    BluetoothSocket socket; InputStream in; OutputStream out; BluetoothDevice device;
    StringBuilder session = new StringBuilder();
    int rpm=-1,temp=-999,speed=-1,map=-1,iat=-999; double maf=-1; String volts="--", proto="--", lastDtc="--", lastTest="LISTO";
    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG=Color.rgb(3,7,13), CARD=Color.rgb(14,20,30), BLUE=Color.rgb(0,122,255), TXT=Color.WHITE, MUTED=Color.rgb(160,168,178), GREEN=Color.rgb(40,210,120), RED=Color.rgb(255,75,85);

    protected void onCreate(Bundle b){ super.onCreate(b); stampSession(); showHome(); }
    void stampSession(){ session.append("BMW E46 SCANNER SESSION\n").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date())).append("\nCar: BMW E46 coupe 320d/320Cd M47N\nMode: READ ONLY\n\n"); }

    void base(String title){ main=new LinearLayout(this); main.setOrientation(LinearLayout.VERTICAL); main.setPadding(22,50,22,10); main.setBackgroundColor(BG); TextView bar=tv("☰    "+title+"  E46",21,TXT,false); status=tv(stateText(),14,Color.rgb(120,200,255),false); main.addView(bar); main.addView(status); setContentView(main); }
    String stateText(){ return "● "+lastTest+" · BT "+(socket!=null&&socket.isConnected()?"OK":"OFF")+" · "+proto+" · "+volts; }
    void refresh(){ if(status!=null) runOnUiThread(() -> status.setText(stateText())); if(live!=null) runOnUiThread(() -> live.setText(liveText())); }
    String liveText(){ return "RPM  "+val(rpm,"rpm")+"\nRefrigerante  "+val(temp,"°C")+"\nMAP  "+val(map,"kPa")+"     MAF  "+(maf<0?"--":String.format(Locale.US,"%.2f g/s",maf))+"\nVelocidad  "+val(speed,"km/h")+"     IAT  "+val(iat,"°C")+"\nDTC  "+lastDtc; }
    String val(int v,String u){ return v<-100||v<0?"--":v+" "+u; }

    void showHome(){ base("Coding Lab"); main.addView(tv("✓ Conectado\nBMW E46 320d M47N\nVIN: pendiente de lectura",15,GREEN,false)); live=box(liveText(),15); main.addView(live); main.addView(card("🔧  Coding Lab", "Funciones de confort y personalización", () -> showCoding())); main.addView(card("💡  Luces", "Iluminación exterior e interior", () -> showLights())); main.addView(card("🪟  Ventanillas", "Funciones de confort de ventanas", () -> showWindows())); main.addView(card("🧪  Diagnóstico", "Leer errores y estado de módulos", () -> showDiag())); main.addView(card("📋  Logs", "Registros y sesiones guardadas", () -> showLogs())); main.addView(nav()); }
    TextView nav(){ return tv("⌂ Inicio        ◇ Módulos        ≡ Logs        ⚙ Ajustes",13,Color.rgb(180,190,205),true); }
    TextView card(String a,String b, final Runnable r){ TextView v=box(a+"\n"+b,17); v.setOnClickListener(x -> r.run()); return v; }
    TextView box(String s,int size){ TextView v=tv(s,size,TXT,false); v.setBackgroundColor(CARD); v.setPadding(18,14,18,14); return v; }
    TextView tv(String s,int size,int color,boolean center){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(0,7,0,9); if(center)v.setGravity(Gravity.CENTER); return v; }
    TextView section(String s){ return tv("\n"+s,13,MUTED,false); }
    Button btn(String s, final Runnable r){ Button b=new Button(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false); b.setOnClickListener(v -> new Thread(r).start()); return b; }
    Switch sw(String title,String sub,boolean on){ Switch s=new Switch(this); s.setText(title+"\n"+sub); s.setTextColor(TXT); s.setTextSize(15); s.setChecked(on); s.setEnabled(false); return s; }
    void back(){ main.addView(btn("← Atrás", () -> runOnUiThread(() -> showHome()))); }
    void addBox(){ log=tv("",13,Color.rgb(225,232,240),false); log.setPadding(16,16,16,16); ScrollView sv=new ScrollView(this); sv.setBackgroundColor(Color.rgb(10,15,23)); sv.addView(log); main.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); }
    void stat(String s){ lastTest=s; runOnUiThread(() -> status.setText(stateText())); }
    void add(String s){ session.append(s).append("\n\n"); runOnUiThread(() -> { if(log!=null) log.append(s+"\n\n"); }); }

    void showCoding(){ base("Coding Lab"); back(); main.addView(section("LUCES   VENTANILLAS   CONFORT   OTROS")); main.addView(box("ⓘ Personaliza el comportamiento de funciones OEM. Escritura bloqueada hasta backup real.",14)); main.addView(section("OBJETIVOS")); main.addView(sw("LSZ check frío posición LED", "KALTUEBERWACHUNG_SL_*", true)); main.addView(sw("LSZ check caliente posición LED", "WARMUEBERWACHUNG_SL_*", true)); main.addView(sw("GM5 cierre confort mando", "KOMFORTSCHLIESSUNG_FB", true)); main.addView(sw("Blink al abrir", "QUIT_BLK_ENTSCH", true)); main.addView(btn("Generar informe objetivos", () -> add("OBJETIVOS CODING: LSZ LED cold/warm, GM5 comfort close, blink unlock. Escritura bloqueada."))); main.addView(btn("Compartir sesión", () -> shareSession())); addBox(); }
    void showLights(){ base("Luces"); back(); main.addView(section("LED / CHECK")); main.addView(sw("Check frío posición", "parpadeo al contacto", true)); main.addView(sw("Check caliente posición", "aviso fijo en cuadro", true)); main.addView(sw("Blink cerrar", "funciona", true)); main.addView(sw("Blink abrir", "pendiente", false)); main.addView(btn("Test DTC ahora", () -> { connect(); initElm(); runCmds(new String[]{"03","07"},"DTC"); })); addBox(); }
    void showWindows(){ base("Ventanillas"); back(); main.addView(section("CIERRE CON MANDO")); main.addView(sw("Cerrar delanteras manteniendo cerrar", "funciona", true)); main.addView(sw("Cerrar traseras manteniendo cerrar", "objetivo GM5", false)); main.addView(sw("Doble clic para cerrar traseras", "experimental", false)); main.addView(section("APERTURA CON MANDO")); main.addView(sw("Abrir cuatro manteniendo abrir", "funciona", true)); main.addView(sw("Doble clic abrir traseras", "pendiente", false)); main.addView(btn("Registrar baseline", () -> add("BASELINE VENTANILLAS: abrir mando abre 4; cerrar mando solo delanteras; traseras coupe no cierran; sin techo."))); addBox(); }
    void showDiag(){ base("Diagnóstico"); back(); live=box(liveText(),15); main.addView(live); main.addView(section("TESTS EN DIRECTO")); main.addView(btn("1 · Conectar ELM327", () -> connect())); main.addView(btn("2 · Inicializar ELM", () -> initElm())); main.addView(btn("3 · Leer y decodificar motor", () -> readEngine())); main.addView(btn("4 · Leer DTC traducidos", () -> runCmds(new String[]{"03","07"},"DTC"))); main.addView(btn("5 · Test protocolo seguro", () -> runCmds(new String[]{"ATDP","ATDPN","0100"},"PROTOCOLO"))); main.addView(btn("Compartir sesión completa", () -> shareSession())); main.addView(btn("Limpiar log", () -> runOnUiThread(() -> log.setText("")))); addBox(); }
    void showLogs(){ base("Logs"); back(); main.addView(btn("Compartir sesión completa", () -> shareSession())); main.addView(btn("Añadir resumen decodificado", () -> add("RESUMEN:\n"+liveText()))); addBox(); add("LOG READY. Usa Compartir sesión completa."); }
    void showInfo(){ base("Ajustes"); back(); main.addView(box("BMW E46 coupe/restyling 320d/320Cd M47N\nELM327 v2.1 detectado\nATSP0 principal\nNo usar ATSP3/4/5 agresivo\nModo seguro: solo lectura",15)); main.addView(btn("Compartir sesión", () -> shareSession())); }

    void shareSession(){ try{ Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session"); i.putExtra(Intent.EXTRA_TEXT,session.toString()); startActivity(Intent.createChooser(i,"Enviar sesión")); }catch(Exception e){ add("ERROR SHARE: "+e.getMessage()); }}
    boolean perm(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46)); stat("Permiso Bluetooth solicitado"); return false;} return true; }
    void connect(){ try{ stat("Conectando BT"); if(!perm())return; BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter(); if(a==null){stat("Sin BT");return;} if(!a.isEnabled()){stat("Activa BT");return;} device=pick(a.getBondedDevices()); if(device==null){stat("Empareja ELM");return;} close(); socket=device.createRfcommSocketToServiceRecord(SPP); socket.connect(); in=socket.getInputStream(); out=socket.getOutputStream(); stat("BT OK"); add("BT OK: "+device.getName()+" / "+device.getAddress()); refresh(); }catch(Exception e){ stat("Fallo BT"); add("ERROR CONEXION: "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    BluetoothDevice pick(Set<BluetoothDevice> ds){ BluetoothDevice f=null; for(BluetoothDevice d:ds){ if(f==null)f=d; String n=d.getName()==null?"":d.getName().toLowerCase(); if(n.contains("obd")||n.contains("elm")||n.contains("vlink")||n.contains("icar"))return d;} return f; }
    boolean ready(){ if(socket==null||!socket.isConnected()||in==null||out==null){ stat("No conectado"); add("Pulsa conectar primero"); return false;} return true; }
    void close(){ try{ if(socket!=null)socket.close(); }catch(Exception ignored){} socket=null; in=null; out=null; }
    void initElm(){ runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM"); }
    void readEngine(){ runCmds(new String[]{"0120","010C","010C","0105","010D","010B","010F","0110"},"MOTOR"); }
    void runCmds(String[] cs,String name){ try{ if(!ready())return; stat(name+" iniciado"); add("===== "+name+" ====="); for(String c:cs)send(c); stat(name+" terminado"); refresh(); }catch(Exception e){ stat("Error "+name); add("ERROR "+name+": "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    void send(String c)throws Exception{ out.write((c+"\r").getBytes("US-ASCII")); out.flush(); Thread.sleep(c.equals("ATZ")?1700:1000); String r=read(); add("> "+c+"\n"+r); parse(c,r); refresh(); }
    String read()throws Exception{ byte[] b=new byte[512]; StringBuilder s=new StringBuilder(); long end=System.currentTimeMillis()+1600; while(System.currentTimeMillis()<end){ while(in.available()>0){ int n=in.read(b); if(n>0)s.append(new String(b,0,n,"US-ASCII")); } if(s.toString().contains(">"))break; Thread.sleep(60);} String r=s.toString().replace('\r',' ').replace('\n',' ').trim(); return r.length()==0?"SIN RESPUESTA":r; }
    void parse(String cmd,String r){ try{ if(cmd.equals("ATRV"))volts=r.replace(">","").trim(); if(cmd.equals("ATDP"))proto=r.replace(">","").trim(); String h=r.replace(" ","").replace(">",""); parseAll(h); }catch(Exception ignored){} }
    void parseAll(String h){ try{ int i; i=h.indexOf("410C"); if(i>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16), b=Integer.parseInt(h.substring(i+6,i+8),16); rpm=((a*256)+b)/4;} i=h.indexOf("4105"); if(i>=0)temp=Integer.parseInt(h.substring(i+4,i+6),16)-40; i=h.indexOf("410D"); if(i>=0)speed=Integer.parseInt(h.substring(i+4,i+6),16); i=h.indexOf("410B"); if(i>=0)map=Integer.parseInt(h.substring(i+4,i+6),16); i=h.indexOf("410F"); if(i>=0)iat=Integer.parseInt(h.substring(i+4,i+6),16)-40; i=h.indexOf("4110"); if(i>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16), b=Integer.parseInt(h.substring(i+6,i+8),16); maf=((a*256)+b)/100.0;} if(h.contains("430401"))lastDtc="P0401 EGR insuficiente"; else if(h.contains("43"))lastDtc="DTC RAW"; }catch(Exception ignored){} }
}
