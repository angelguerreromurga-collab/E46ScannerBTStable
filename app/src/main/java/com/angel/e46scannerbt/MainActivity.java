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
    private TextView status, log;
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;
    private BluetoothDevice device;
    private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 64, 24, 18);
        root.setBackgroundColor(Color.rgb(5,7,10));

        TextView title = tv("E46 SCANNER BT", 25, Color.WHITE, true);
        TextView sub = tv("v0.4 · DIAGNOSIS READ ONLY", 14, Color.rgb(150,190,255), false);
        status = tv("Estado: listo", 15, Color.rgb(120,210,255), false);

        root.addView(title); root.addView(sub); root.addView(status);
        root.addView(section("CONEXION"));
        root.addView(btn("1 · Conectar adaptador", () -> connect()));
        root.addView(btn("2 · Inicializar ELM", () -> initElm()));
        root.addView(section("LECTURAS"));
        root.addView(btn("3 · Leer motor basico", () -> readEngine()));
        root.addView(btn("4 · Leer DTC motor", () -> runCmds(new String[]{"03","07"}, "DTC")));
        root.addView(btn("5 · Prueba protocolos ISO/KWP", () -> protocols()));
        root.addView(section("UTILIDADES"));
        root.addView(btn("Limpiar log", () -> runOnUiThread(() -> log.setText(""))));

        log = tv("", 13, Color.rgb(225,232,240), false);
        log.setPadding(16,16,16,16);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.rgb(13,17,24));
        sv.addView(log);
        root.addView(sv, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        add("SEGURIDAD: no borra, no codifica, no escribe modulos.");
        add("USO: conectar > inicializar > leer. Si sale error, pulsa conectar otra vez.");
    }

    private TextView tv(String s,int size,int color,boolean center){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(0,6,0,8); if(center)v.setGravity(Gravity.CENTER); return v; }
    private TextView section(String s){ TextView v=tv("\n"+s,13,Color.rgb(155,155,155),false); return v; }
    private Button btn(String s, final Runnable r){ Button b=new Button(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false); b.setOnClickListener(v -> new Thread(r).start()); return b; }
    private void stat(String s){ runOnUiThread(() -> status.setText("Estado: "+s)); }
    private void add(String s){ runOnUiThread(() -> log.append(s+"\n\n")); }

    private boolean perm(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(() -> requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46)); stat("acepta permiso Bluetooth"); return false; } return true; }

    private void connect(){ try{ if(!perm())return; BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter(); if(a==null){stat("sin Bluetooth");return;} if(!a.isEnabled()){stat("activa Bluetooth");return;} device=pick(a.getBondedDevices()); if(device==null){stat("empareja ELM primero");return;} close(); stat("conectando a "+device.getName()); socket=device.createRfcommSocketToServiceRecord(SPP); socket.connect(); in=socket.getInputStream(); out=socket.getOutputStream(); stat("conectado"); add("BT OK: "+device.getName()+" / "+device.getAddress()); }catch(Exception e){ stat("fallo conexion"); add("ERROR CONEXION: "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}
    private BluetoothDevice pick(Set<BluetoothDevice> ds){ BluetoothDevice f=null; for(BluetoothDevice d:ds){ if(f==null)f=d; String n=d.getName()==null?"":d.getName().toLowerCase(); if(n.contains("obd")||n.contains("elm")||n.contains("vlink")||n.contains("icar"))return d;} return f; }
    private boolean ready(){ if(socket==null||!socket.isConnected()||in==null||out==null){ stat("no conectado"); add("Pulsa 1 · Conectar adaptador"); return false;} return true; }
    private void close(){ try{ if(socket!=null)socket.close(); }catch(Exception ignored){} socket=null; in=null; out=null; }

    private void initElm(){ runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"}, "INIT ELM"); }
    private void readEngine(){ runCmds(new String[]{"0100","0120","0140","010C","0105","010D","010B","010F","0110","0142"}, "MOTOR BASICO"); }
    private void protocols(){ runCmds(new String[]{"ATSP0","0100","ATSP3","0100","ATSP4","0100","ATSP5","0100","ATSP0"}, "PROTOCOLOS"); }
    private void runCmds(String[] cs,String name){ try{ if(!ready())return; stat("ejecutando "+name); add("===== "+name+" ====="); for(String c:cs) send(c); stat(name+" terminado"); }catch(Exception e){ stat("error"); add("ERROR "+name+": "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); }}

    private void send(String c)throws Exception{ out.write((c+"\r").getBytes("US-ASCII")); out.flush(); Thread.sleep(c.equals("ATZ")?1700:900); add("> "+c+"\n"+read()); }
    private String read()throws Exception{ byte[] b=new byte[512]; StringBuilder s=new StringBuilder(); long end=System.currentTimeMillis()+1400; while(System.currentTimeMillis()<end){ while(in.available()>0){ int n=in.read(b); if(n>0)s.append(new String(b,0,n,"US-ASCII")); } if(s.toString().contains(">"))break; Thread.sleep(60);} String r=s.toString().replace('\r',' ').replace('\n',' ').trim(); return r.length()==0?"SIN RESPUESTA":r; }
}
