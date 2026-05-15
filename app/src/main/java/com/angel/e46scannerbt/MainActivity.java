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
    int rpm=-1,temp=-999,speed=-1,map=-1,iat=-999; double maf=-1; String volts="--", proto="--", lastDtc="--";
    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG=Color.rgb(3,7,13), CARD=Color.rgb(14,20,30), BLUE=Color.rgb(0,122,255), TXT=Color.WHITE, MUTED=Color.rgb(160,168,178), GREEN=Color.rgb(40,210,120), YELLOW=Color.rgb(245,190,60);

    protected void onCreate(Bundle b){ super.onCreate(b); stampSession(); showHome(); }
    void stampSession(){ session.append("BMW E46 SCANNER SESSION\n").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date())).append("\nCar: BMW E46 coupe 320d/320Cd M47N\nMode: READ ONLY\n\n"); }

    void base(String title){ main=new LinearLayout(this); main.setOrientation(LinearLayout.VERTICAL); main.setPadding(22,54,22,12); main.setBackgroundColor(BG); TextView top=tv(title,24,TXT,true); status=tv(stateText(),14,Color.rgb(120,200,255),false); main.addView(top); main.addView(status); setContentView(main); }
    String stateText(){ return "BT: "+(socket!=null&&socket.isConnected()?"conectado":"desconectado")+" · Protocolo: "+proto+" · V: "+volts; }
    void refreshStatus(){ if(status!=null) runOnUiThread(() -> status.setText(stateText())); if(live!=null) runOnUiThread(() -> live.setText(liveText())); }
    String liveText(){ return "RPM: "+val(rpm,"rpm")+"   Temp: "+val(temp,"°C")+"\nMAP: "+val(map,"kPa")+"   MAF: "+(maf<0?"--":String.format(Locale.US,"%.2f g/s",maf))+"\nVel: "+val(speed,"km/h")+"   IAT: "+val(iat,"°C")+"\nDTC: "+lastDtc; }
    String val(int v,String u){ return v<-100||v<0?"--":v+" "+u; }

    void showHome(){ base("Coding Lab E46"); main.addView(tv("Conectado a tu BMW E46 320d M47N\nModo seguro: lectura y pruebas",15,Color.rgb(220,225,232),false)); live=tv(liveText(),16,Color.rgb(235,240,245),false); live.setBackgroundColor(CARD); live.setPadding(18,16,18,16); main.addView(live); main.addView(card("🔧  Coding Lab", "Funciones confort y personalizacion", () -> showCoding())); main.addView(card("💡  Luces", "LED, check frio/caliente, confirmacion", () -> showLights())); main.addView(card("🪟  Ventanillas", "Cierre confort y traseras coupe", () -> showWindows())); main.addView(card("🧪  Diagnostico", "Tests con estado, PIDs, DTC", () -> showDiag())); main.addView(card("📋  Logs", "Compartir sesion completa", () -> showLogs())); main.addView(card("⚙️  Ajustes", "ELM y seguridad", () -> showInfo())); }
    TextView card(String a,String b, final Runnable r){ TextView v=tv(a+"\n"+b,17,TXT,false); v.setBackgroundColor(CARD); v.setPadding(18,16,18,16); v.setOnClickListener(x -> r.run()); return v; }
    TextView tv(String s,int size,int color,boolean center){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(0,7,0,9); if(center)v.setGravity(Gravity.CENTER); return v; }
    TextView section(String s){ return tv("\n"+s,13,MUTED,false); }
    Button btn(String s, final Runnable r){ Button b=new Button(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false); b.setOnClickListener(v -> new Thread(r).start()); return b; }
    Switch sw(String title,String sub,boolean on){ Switch s=new Switch(this); s.setText(title+"\n"+sub); s.setTextColor(TXT); s.setTextSize(15); s.setChecked(on); s.setEnabled(false); return s; }
    void back(){ main.addView(btn("← Inicio", () -> runOnUiThread(() -> showHome()))); }
    void addBox(){ log=tv("",13,Color.rgb(225,232,240),false); log.setPadding(16,16,16,16); ScrollView sv=new ScrollView(this); sv.setBackgroundColor(Color.rgb(10,15,23)); sv.addView(log); main.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); }
    void stat(String s){ runOnUiThread(() -> status.setText("TEST: "+s+" · "+stateText())); }
    void add(String s){ session.append(s).append("\n\n"); runOnUiThread(() -> { if(log!=null) log.append(s+"\n\n"); }); }

    void showCoding(){ base("Coding Lab"); back(); main.addView(section("ESTADO")); main.addView(tv("Escritura bloqueada hasta tener backup real. La app prepara objetivos y pruebas, no toca modulos.",15,Color.rgb(230,235,240),false)); main.addView(sw("LSZ check frio posicion LED", "KALTUEBERWACHUNG_SL_*", true)); main.addView(sw("LSZ check caliente posicion LED", "WARMUEBERWACHUNG_SL_*", true)); main.addView(sw("GM5 cierre confort mando", "KOMFORTSCHLIESSUNG_FB", true)); main.addView(sw("Blink al abrir", "QUIT_BLK_ENTSCH", true)); main.addView(btn("Generar informe objetivos", () -> add("OBJETIVOS CODING: LSZ LED cold/warm, GM5 comfort close, blink unlock. Escritura bloqueada."))); main.addView(btn("Compartir sesion", () -> shareSession())); addBox(); }
    void showLights(){ base("Luces"); back(); main.addView(section("LED CHECK")); main.addView(sw("Check frio posicion", "parpadeo al contacto", true)); main.addView(sw("Check caliente posicion", "aviso fijo en cuadro", true)); main.addView(section("CONFIRMACION")); main.addView(sw("Blink cerrar", "funciona", true)); main.addView(sw("Blink abrir", "pendiente", false)); main.addView(btn("Leer DTC ahora", () -> { connect(); initElm(); runCmds(new String[]{"03","07"},"DTC"); })); main.addView(btn("Compartir sesion", () -> shareSession())); addBox(); }
    void showWindows(){ base("Ventanillas"); back(); main.addView(section("BASELINE")); main.addView(sw("Abrir 4 con mando", "funciona", true)); main.addView(sw("Cerrar delanteras", "funciona", true)); main.addView(sw("Cerrar traseras coupe", "objetivo GM5", false)); main.addView(sw("One-touch trasero", "experimental", false)); main.addView(btn("Registrar baseline", () -> add("BASELINE VENTANILLAS: abrir mando abre 4; cerrar mando solo delanteras; traseras coupe no cierran; sin techo."))); main.addView(btn("Compartir sesion", () -> shareSession())); addBox(); }
    void showDiag(){ base("Diagnostico"); back(); live=tv(liveText(),16,Color.rgb(235,240,245),false); live.setBackgroundColor(CARD); live.setPadding(18,16,18,16); main.addView(live); main.addView(section("TESTS")); main.addView(btn("1 · Conectar ELM327", () -> connect())); main.addView(btn("2 · Inicializar ELM", () -> initElm())); main.addView(btn("3 · Leer y decodificar motor", () -> readEngine())); main.addView(btn("4 · Leer DTC traducidos", () -> runCmds(new String[]{"03","07"},"DTC"))); main.addView(btn("5 · Test protocolo seguro", () -> runCmds(new String[]{"ATDP","ATDPN","0100"},"PROTOCOLO SEGURO"))); main.addView(btn("Compartir sesion completa", () -> shareSession())); main.addView(btn("Limpiar log", () -> runOnUiThread(() -> log.setText("")))); addBox(); }
    void showLogs(){ base("Logs"); back(); main.addView(btn("Compartir sesion completa", () -> shareSession())); main.addView(btn("Copiar resumen al log", () -> add("RESUMEN: "+liveText()))); addBox(); add("LOG READY. Usa Compartir sesion completa."); }
    void showInfo(){ base("Ajustes / Info"); back(); main.addView(tv("BMW E46 coupe/restyling 320d/320Cd M47N\nELM327 v2.1 detectado\nNo insistir ATSP3/4/5: deja colgado el clon\nATSP0 principal\nModo: solo lectura\nSiguiente: parser, estabilidad, UI OEM",15,Color.rgb(230,235,240),false)); main.addView(btn("Compartir sesion", () -> shareSession())); }

    void shareSession(){ try{ Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session"); i.putExtra(Intent.EXTRA_TEXT,session.toString()); startActivity(Intent.createChooser(i,"Enviar sesion")); }catch(Exception e){ add("ERROR SHARE: "+e.getMessage()); }}
    boolean perm(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46)); stat("Permiso Bluetooth solicitado"); return false;} return true; }
    void connect(){ try{ stat("conectando Bluetooth"); if(!perm())return; BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter(); if(a==null){stat("sin Bluetooth");return;} if(!a.isEnabled()){stat("activa Bluetooth");return;} device=pick(a.getBondedDevices()); if(device==null){stat("empareja ELM primero");return;} close(); socket=device.createRfcommSocketToServiceRecord(SPP); socket.connect(); in=socket.getInputStream(); out=socket.getOutputStream(); stat("Bluetooth OK"); add("BT OK: "+device.getName()+" / "+device.getAddress()); refreshStatus(); }catch(Exception e){ stat("fallo conexion"); add("ERROR CONEXION: "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    BluetoothDevice pick(Set<BluetoothDevice> ds){ BluetoothDevice f=null; for(BluetoothDevice d:ds){ if(f==null)f=d; String n=d.getName()==null?"":d.getName().toLowerCase(); if(n.contains("obd")||n.contains("elm")||n.contains("vlink")||n.contains("icar"))return d;} return f; }
    boolean ready(){ if(socket==null||!socket.isConnected()||in==null||out==null){ stat("no conectado"); add("Pulsa conectar primero"); return false;} return true; }
    void close(){ try{ if(socket!=null)socket.close(); }catch(Exception ignored){} socket=null; in=null; out=null; }
    void initElm(){ runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM"); }
    void readEngine(){ runCmds(new String[]{"0120","010C","0105","010D","010B","010F","0110"},"MOTOR DECODIFICADO"); }
    void runCmds(String[] cs,String name){ try{ if(!ready())return; stat(name+" iniciado"); add("===== "+name+" ====="); for(String c:cs)send(c); stat(name+" terminado"); refreshStatus(); }catch(Exception e){ stat("error en "+name); add("ERROR "+name+": "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    void send(String c)throws Exception{ out.write((c+"\r").getBytes("US-ASCII")); out.flush(); Thread.sleep(c.equals("ATZ")?1700:900); String r=read(); add("> "+c+"\n"+r); parse(c,r); refreshStatus(); }
    String read()throws Exception{ byte[] b=new byte[512]; StringBuilder s=new StringBuilder(); long end=System.currentTimeMillis()+1400; while(System.currentTimeMillis()<end){ while(in.available()>0){ int n=in.read(b); if(n>0)s.append(new String(b,0,n,"US-ASCII")); } if(s.toString().contains(">"))break; Thread.sleep(60);} String r=s.toString().replace('\r',' ').replace('\n',' ').trim(); return r.length()==0?"SIN RESPUESTA":r; }
    void parse(String cmd,String r){ try{ if(cmd.equals("ATRV"))volts=r.replace(">","").trim(); if(cmd.equals("ATDP"))proto=r.replace(">","").trim(); String h=r.replace(" ","").replace(">",""); int p=h.indexOf("41"+cmd.substring(Math.max(0,cmd.length()-2))); if(cmd.equals("010C")){int i=h.indexOf("410C"); if(i>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16), b=Integer.parseInt(h.substring(i+6,i+8),16); rpm=((a*256)+b)/4;}} if(cmd.equals("0105")){int i=h.indexOf("4105"); if(i>=0)temp=Integer.parseInt(h.substring(i+4,i+6),16)-40;} if(cmd.equals("010D")){int i=h.indexOf("410D"); if(i>=0)speed=Integer.parseInt(h.substring(i+4,i+6),16);} if(cmd.equals("010B")){int i=h.indexOf("410B"); if(i>=0)map=Integer.parseInt(h.substring(i+4,i+6),16);} if(cmd.equals("010F")){int i=h.indexOf("410F"); if(i>=0)iat=Integer.parseInt(h.substring(i+4,i+6),16)-40;} if(cmd.equals("0110")){int i=h.indexOf("4110"); if(i>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16), b=Integer.parseInt(h.substring(i+6,i+8),16); maf=((a*256)+b)/100.0;}} if(cmd.equals("03")){ if(h.contains("430401")) lastDtc="P0401 EGR insuficiente"; else if(h.contains("43")) lastDtc="DTC RAW: "+h; }}catch(Exception ignored){} }
}
