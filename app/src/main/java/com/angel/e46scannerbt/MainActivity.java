package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
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
    TextView stateText, logText, liveText, testsText;
    BluetoothSocket socket;
    InputStream in;
    OutputStream out;
    BluetoothDevice device;
    StringBuilder session = new StringBuilder();

    String volts="--", proto="--", dtc="--", test="LISTO", vin="pendiente";
    int rpm=-1, temp=-999, map=-1, speed=-1, iat=-999;
    double maf=-1;
    boolean bt=false, elm=false, protocol=false, engine=false, dtcOk=false, backup=false;

    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG=Color.rgb(1,5,11), CARD=Color.rgb(9,16,26), CARD2=Color.rgb(12,21,34);
    final int LINE=Color.rgb(24,39,58), TXT=Color.WHITE, MUTED=Color.rgb(156,164,176);
    final int BLUE=Color.rgb(0,122,255), GREEN=Color.rgb(35,220,115), RED=Color.rgb(255,65,75), AMBER=Color.rgb(245,180,45);

    public void onCreate(Bundle b){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        stampSession();
        showHome();
    }

    void stampSession(){
        session.append("BMW E46 SCANNER SESSION\n")
                .append(now()).append("\n")
                .append("Car: BMW E46 coupe 320d/320Cd M47N\n")
                .append("Mode: READ ONLY\n\n");
    }

    String now(){ return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()); }
    int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }

    TextView tv(String s,int size,int color,boolean bold){
        TextView v=new TextView(this);
        v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setIncludeFontPadding(true);
        if(bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return v;
    }

    GradientDrawable bg(int color,int radius,int stroke){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius)); g.setStroke(1,stroke);
        return g;
    }

    void shell(String title, boolean showBottomActions){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        statusStrip=new View(this); statusStrip.setBackgroundColor(bt?GREEN:RED);
        root.addView(statusStrip,new LinearLayout.LayoutParams(-1,dp(4)));

        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(false);
        body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(22),dp(22),dp(22),dp(8));
        scroll.addView(body); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        header(title);
        if(showBottomActions) bottomActions();
    }

    void header(String title){
        LinearLayout h=new LinearLayout(this); h.setOrientation(LinearLayout.HORIZONTAL); h.setGravity(Gravity.CENTER_VERTICAL);
        TextView left=tv(title.equals("Coding Lab")?"‹":"☰",30,TXT,false); left.setGravity(Gravity.CENTER); left.setOnClickListener(v->showHome());
        h.addView(left,new LinearLayout.LayoutParams(dp(42),dp(44)));
        TextView titleView=tv(title,20,TXT,false); titleView.setGravity(Gravity.CENTER);
        h.addView(titleView,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView car=tv("▱",26,TXT,false); car.setGravity(Gravity.CENTER); h.addView(car,new LinearLayout.LayoutParams(dp(42),dp(44)));
        body.addView(h);
        stateText=tv(statusLine(),12,bt?GREEN:RED,false); stateText.setGravity(Gravity.CENTER); body.addView(stateText);
        space(8);
    }

    String statusLine(){
        return (bt?"CONECTADO":"DESCONECTADO")+" · "+test+" · ELM "+(elm?"OK":"--")+" · "+proto+" · "+volts;
    }

    void refresh(){
        runOnUiThread(()->{
            if(statusStrip!=null) statusStrip.setBackgroundColor(bt?GREEN:RED);
            if(stateText!=null){ stateText.setText(statusLine()); stateText.setTextColor(bt?GREEN:RED); }
            if(liveText!=null) liveText.setText(live());
            if(testsText!=null) testsText.setText(tests());
        });
    }

    void space(int h){ Space s=new Space(this); body.addView(s,new LinearLayout.LayoutParams(1,dp(h))); }

    TextView cardText(String s,int size){
        TextView v=tv(s,size,TXT,false); v.setPadding(dp(16),dp(12),dp(16),dp(12));
        v.setBackground(bg(CARD,12,LINE)); return v;
    }

    LinearLayout cardLayout(){
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16),dp(14),dp(16),dp(14)); l.setBackground(bg(CARD,16,LINE));
        return l;
    }

    TextView section(String s){ TextView v=tv(s,12,MUTED,false); v.setPadding(0,dp(20),0,dp(8)); return v; }

    void showHome(){
        shell("Coding Lab E46", false);
        LinearLayout hero=cardLayout();
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout left=new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        left.addView(tv("Bluetooth",12,MUTED,false));
        left.addView(tv(bt?"Conectado":"Desconectado",16,bt?GREEN:AMBER,true));
        left.addView(tv("BMW E46 320d M47N",17,TXT,true));
        left.addView(tv("VIN: "+vin,13,MUTED,false));
        row.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        ImageView img=new ImageView(this); img.setImageResource(getResources().getIdentifier("car_e46_side","drawable",getPackageName())); img.setAdjustViewBounds(true);
        row.addView(img,new LinearLayout.LayoutParams(dp(190),dp(95)));
        hero.addView(row);
        liveText=tv(live(),13,Color.rgb(220,230,242),false); liveText.setPadding(0,dp(10),0,0); hero.addView(liveText);
        body.addView(hero);

        testsText=cardText(tests(),13); body.addView(testsText);
        body.addView(section("MENÚ PRINCIPAL"));
        body.addView(menuRow("Coding Lab", "Funciones de confort y personalización", ()->showCoding()));
        body.addView(menuRow("Backup seguro", "Guardar estado OBD/ECU antes de pruebas", ()->showBackup()));
        body.addView(menuRow("Diagnóstico", "Leer errores y estado de módulos", ()->showDiag()));
        body.addView(menuRow("Logs", "Registros y sesiones guardadas", ()->showLogs()));
    }

    TextView menuRow(String title,String sub,final Runnable r){
        TextView v=cardText(title+"                                      ›\n"+sub,16);
        v.setOnClickListener(x->r.run()); return v;
    }

    void showCoding(){
        shell("Coding Lab", true);
        tabs();
        body.addView(infoCard("Personaliza el comportamiento de las ventanillas y\nfunciones de confort asociadas."));
        body.addView(section("CIERRE CON MANDO"));
        body.addView(group(new View[]{
                switchRow("Cerrar ventanillas con mantener pulsado","Cierra todas las ventanillas al mantener pulsado el botón de cerrar.",true),
                switchRow("Cerrar ventanillas traseras con mantener","Cierra solo las ventanillas traseras al mantener pulsado el botón de cerrar.",true),
                switchRow("Doble clic para cerrar traseras","Doble clic en el botón de cerrar para subir traseras automáticamente.",true),
                switchRow("Doble clic para abrir traseras","Doble clic en el botón de abrir para bajar traseras automáticamente.",false)
        }));
        body.addView(section("APERTURA CON MANDO"));
        body.addView(group(new View[]{
                switchRow("Abrir ventanillas con mantener pulsado","Abre todas las ventanillas al mantener pulsado el botón de abrir.",true),
                switchRow("Doble clic para abrir traseras","Doble clic en el botón de abrir para bajar traseras automáticamente.",true)
        }));
        body.addView(section("OTRAS OPCIONES"));
        body.addView(group(new View[]{
                switchRow("Bajar ventanillas al abrir la puerta","Baja ligeramente las ventanillas al abrir la puerta del conductor.",false)
        }));
        body.addView(section("PRUEBAS Y BACKUP"));
        body.addView(action("Backup seguro READ ONLY",()->safeBackup()));
        body.addView(action("Test completo automático",()->fullTest()));
        body.addView(action("Compartir sesión",()->share()));
    }

    void tabs(){
        LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL);
        String[] a={"LUCES","VENTANILLAS","CONFORT","OTROS"};
        for(String s:a){ TextView v=tv(s,12,s.equals("VENTANILLAS")?BLUE:Color.rgb(210,216,225),true); v.setGravity(Gravity.CENTER); r.addView(v,new LinearLayout.LayoutParams(0,dp(38),1)); }
        body.addView(r);
        View line=new View(this); line.setBackgroundColor(BLUE); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(118),dp(2)); lp.leftMargin=dp(124); body.addView(line,lp); space(14);
    }

    TextView infoCard(String s){ TextView v=cardText("ⓘ    "+s,13); v.setTextColor(Color.rgb(210,220,232)); return v; }

    LinearLayout group(View[] rows){
        LinearLayout g=new LinearLayout(this); g.setOrientation(LinearLayout.VERTICAL); g.setBackground(bg(CARD,10,LINE));
        for(View v:rows) g.addView(v); return g;
    }

    View switchRow(String title,String sub,boolean checked){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(16),dp(9),dp(14),dp(9));
        LinearLayout texts=new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL);
        TextView a=tv(title,14,TXT,false); TextView b=tv(sub,12,Color.rgb(190,200,212),false); texts.addView(a); texts.addView(b);
        row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
        Switch sw=new Switch(this); sw.setChecked(checked); sw.setOnCheckedChangeListener((buttonView,isChecked)->add("SWITCH: "+title+" = "+(isChecked?"ON":"OFF")));
        row.addView(sw,new LinearLayout.LayoutParams(dp(64),dp(48)));
        return row;
    }

    TextView action(String s,final Runnable r){ TextView v=cardText(s,14); v.setGravity(Gravity.CENTER); v.setOnClickListener(x->new Thread(r).start()); return v; }

    void bottomActions(){
        bottomBar=new LinearLayout(this); bottomBar.setPadding(dp(22),dp(10),dp(22),dp(16)); bottomBar.setBackgroundColor(Color.rgb(3,7,13));
        TextView reset=bottomButton("↻  RESTABLECER",false); reset.setOnClickListener(v->add("RESTABLECER: interfaz, sin escribir."));
        TextView save=bottomButton("▣  GUARDAR CAMBIOS",true); save.setOnClickListener(v->add("GUARDAR BLOQUEADO: falta backup real de módulo."));
        bottomBar.addView(reset,new LinearLayout.LayoutParams(0,dp(54),1));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1); lp.leftMargin=dp(12); bottomBar.addView(save,lp);
        root.addView(bottomBar,new LinearLayout.LayoutParams(-1,dp(80)));
    }

    TextView bottomButton(String s,boolean primary){
        TextView v=tv(s,14,primary?TXT:BLUE,true); v.setGravity(Gravity.CENTER);
        v.setBackground(primary?bg(BLUE,8,BLUE):bg(Color.TRANSPARENT,8,BLUE)); return v;
    }

    void showBackup(){
        shell("Backup seguro", false);
        body.addView(infoCard("Backup READ ONLY: protocolo, voltaje, PIDs, VIN si responde, DTC y RAW. No escribe módulos."));
        body.addView(action("Backup completo seguro",()->safeBackup()));
        body.addView(action("Backup identidad ECU/VIN",()->runCmds(new String[]{"0900","0902","0904","0906"},"BACKUP ECU ID")));
        body.addView(action("Backup capacidades OBD",()->runCmds(new String[]{"0100","0120","0140","0160"},"BACKUP PIDS")));
        body.addView(action("Backup fallos",()->runCmds(new String[]{"03","07","0A"},"BACKUP DTC")));
        body.addView(action("Compartir backup/sesión",()->share()));
        addLogBox();
    }

    void showDiag(){
        shell("Diagnóstico", false);
        liveText=cardText(live(),14); body.addView(liveText);
        testsText=cardText(tests(),13); body.addView(testsText);
        body.addView(action("Conectar ELM327",()->connect()));
        body.addView(action("Inicializar ELM",()->initElm()));
        body.addView(action("Leer motor",()->readEngine()));
        body.addView(action("Leer DTC",()->runCmds(new String[]{"03","07"},"DTC")));
        body.addView(action("Compartir sesión",()->share()));
        addLogBox();
    }

    void showLogs(){ shell("Logs",false); body.addView(action("Compartir sesión completa",()->share())); body.addView(action("Añadir informe mecánico",()->add(report()))); addLogBox(); add("LOG READY. Usa Compartir sesión completa."); }

    void addLogBox(){ logText=tv("",12,Color.rgb(220,228,240),false); logText.setPadding(dp(14),dp(14),dp(14),dp(14)); ScrollView sv=new ScrollView(this); sv.setBackground(bg(Color.rgb(7,12,20),12,Color.rgb(18,30,45))); sv.addView(logText); body.addView(sv,new LinearLayout.LayoutParams(-1,dp(240))); }

    String live(){ return "RPM  "+val(rpm,"rpm")+"      TEMP  "+val(temp,"°C")+"\nMAP  "+val(map,"kPa")+"      MAF  "+(maf<0?"--":String.format(Locale.US,"%.2f g/s",maf))+"\nVEL  "+val(speed,"km/h")+"      IAT  "+val(iat,"°C")+"\nDTC  "+dtc; }
    String val(int v,String u){ return v<-100||v<0?"--":v+" "+u; }
    String tests(){ return (bt?"●":"○")+" Bluetooth    "+(elm?"●":"○")+" ELM    "+(protocol?"●":"○")+" Protocolo\n"+(engine?"●":"○")+" Motor/PIDs    "+(dtcOk?"●":"○")+" DTC    "+(backup?"●":"○")+" Backup"; }

    void add(String s){ session.append(s).append("\n\n"); runOnUiThread(()->{ if(logText!=null) logText.append(s+"\n\n"); }); }
    String report(){ return "INFORME MECÁNICO\nVoltaje: "+volts+"\nProtocolo: "+proto+"\n"+live()+"\nConclusión: "+(dtc.contains("P0401")?"EGR anulada/desconectada o flujo insuficiente detectado.":"Sin DTC motor confirmado."); }

    void fullTest(){ connect(); initElm(); readEngine(); runCmds(new String[]{"03","07"},"DTC"); runCmds(new String[]{"ATDP","ATDPN","0100"},"PROTOCOLO"); add(report()); }
    void safeBackup(){ connect(); initElm(); add("===== BACKUP SEGURO READ ONLY ====="); runCmds(new String[]{"ATI","ATRV","ATDP","ATDPN","0100","0120","0140","0900","0902","0904","03","07","0A"},"BACKUP SEGURO"); backup=true; add("BACKUP OK: sesión RAW lista para exportar. No se ha escrito nada."); add(report()); refresh(); }
    void share(){ try{ Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session"); i.putExtra(Intent.EXTRA_TEXT,session.toString()); startActivity(Intent.createChooser(i,"Enviar sesión")); }catch(Exception e){ add("ERROR SHARE: "+e.getMessage()); } }

    boolean perm(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(()->requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46)); return false; } return true; }
    void connect(){ try{ test="Conectando BT"; refresh(); if(!perm())return; BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter(); if(a==null){add("Sin Bluetooth");return;} device=null; for(BluetoothDevice d:a.getBondedDevices()){ String n=d.getName()==null?"":d.getName().toLowerCase(); if(device==null||n.contains("obd")||n.contains("elm")) device=d; } if(device==null){add("Empareja ELM");return;} close(); socket=device.createRfcommSocketToServiceRecord(SPP); socket.connect(); in=socket.getInputStream(); out=socket.getOutputStream(); bt=true; test="BT OK"; add("BT OK: "+device.getName()+" / "+device.getAddress()); refresh(); }catch(Exception e){ bt=false; add("ERROR CONEXION: "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); refresh(); } }
    void close(){ try{ if(socket!=null) socket.close(); }catch(Exception ignored){} socket=null; in=null; out=null; }
    boolean ready(){ if(socket==null||!socket.isConnected()||in==null||out==null){ add("Pulsa conectar primero"); return false; } return true; }
    void initElm(){ runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM"); elm=true; }
    void readEngine(){ runCmds(new String[]{"0120","010C","010C","0105","010D","010B","010F","0110"},"MOTOR"); engine=true; }
    void runCmds(String[] cs,String name){ try{ if(!ready())return; test=name; add("===== "+name+" ====="); for(String c:cs)send(c); if(name.contains("DTC"))dtcOk=true; if(name.contains("PROTOCOLO"))protocol=true; test=name+" terminado"; refresh(); }catch(Exception e){ add("ERROR "+name+": "+e.getClass().getSimpleName()+" - "+e.getMessage()); close(); refresh(); } }
    void send(String c)throws Exception{ out.write((c+"\r").getBytes("US-ASCII")); out.flush(); Thread.sleep(c.equals("ATZ")?1700:1000); String r=read(); add("> "+c+"\n"+r); parse(c,r); refresh(); }
    String read()throws Exception{ byte[] b=new byte[512]; StringBuilder s=new StringBuilder(); long end=System.currentTimeMillis()+1600; while(System.currentTimeMillis()<end){ while(in.available()>0){ int n=in.read(b); if(n>0)s.append(new String(b,0,n,"US-ASCII")); } if(s.toString().contains(">"))break; Thread.sleep(60); } String r=s.toString().replace('\r',' ').replace('\n',' ').trim(); return r.length()==0?"SIN RESPUESTA":r; }
    void parse(String cmd,String r){ try{ if(cmd.equals("ATRV"))volts=r.replace(">","").trim(); if(cmd.equals("ATDP"))proto=r.replace(">","").trim(); String h=r.replace(" ","").replace(">",""); int i; if((i=h.indexOf("410C"))>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16);rpm=((a*256)+b)/4;} if((i=h.indexOf("4105"))>=0)temp=Integer.parseInt(h.substring(i+4,i+6),16)-40; if((i=h.indexOf("410D"))>=0)speed=Integer.parseInt(h.substring(i+4,i+6),16); if((i=h.indexOf("410B"))>=0)map=Integer.parseInt(h.substring(i+4,i+6),16); if((i=h.indexOf("410F"))>=0)iat=Integer.parseInt(h.substring(i+4,i+6),16)-40; if((i=h.indexOf("4110"))>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16);maf=((a*256)+b)/100.0;} if(h.contains("430401"))dtc="P0401 EGR insuficiente"; else if(h.contains("43"))dtc="DTC RAW"; }catch(Exception ignored){} }
}
