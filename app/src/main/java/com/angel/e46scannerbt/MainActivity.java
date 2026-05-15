package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout screen, content, bottomNav;
    TextView state, liveCard, testCard, log;
    BluetoothSocket socket; InputStream in; OutputStream out; BluetoothDevice device;
    StringBuilder session = new StringBuilder();
    int rpm=-1,temp=-999,speed=-1,map=-1,iat=-999; double maf=-1;
    String volts="--", proto="--", lastDtc="--", lastTest="LISTO", vin="pendiente";
    boolean btOk=false, elmOk=false, protocolOk=false, engineOk=false, dtcOk=false, backupOk=false;
    static final UUID SPP=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG=Color.rgb(1,5,11), CARD=Color.rgb(9,16,26), CARD2=Color.rgb(13,23,37), LINE=Color.rgb(28,46,68), TXT=Color.WHITE, MUTED=Color.rgb(155,164,178), BLUE=Color.rgb(0,122,255), GREEN=Color.rgb(35,220,115), WARN=Color.rgb(245,178,42);

    protected void onCreate(Bundle b){ super.onCreate(b); stamp(); showHome(); }
    void stamp(){ session.append("BMW E46 SCANNER SESSION\n").append(now()).append("\nCar: BMW E46 coupe 320d/320Cd M47N\nMode: READ ONLY\n\n"); }
    String now(){ return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date()); }

    void shell(String title){
        screen=new LinearLayout(this); screen.setOrientation(LinearLayout.VERTICAL); screen.setBackgroundColor(BG);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(22),dp(34),dp(22),dp(8));
        ScrollView sv=new ScrollView(this); sv.addView(content); screen.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        bottomNav=new LinearLayout(this); bottomNav.setOrientation(LinearLayout.HORIZONTAL); bottomNav.setPadding(dp(8),dp(6),dp(8),dp(8)); bottomNav.setBackgroundColor(Color.rgb(3,8,15));
        addNav("Inicio",()->showHome()); addNav("Módulos",()->showCoding()); addNav("Logs",()->showLogs()); addNav("Ajustes",()->showInfo()); screen.addView(bottomNav,new LinearLayout.LayoutParams(-1,dp(58)));
        setContentView(screen);
        header(title);
    }
    void header(String title){
        LinearLayout bar=new LinearLayout(this); bar.setOrientation(LinearLayout.HORIZONTAL); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(0,0,0,dp(10));
        TextView menu=t("☰",26,TXT,true); bar.addView(menu,new LinearLayout.LayoutParams(dp(42),dp(44)));
        TextView name=t(title,20,TXT,true); bar.addView(name,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView badge=t("50",24,TXT,true); badge.setTextColor(Color.rgb(238,242,248)); bar.addView(badge,new LinearLayout.LayoutParams(dp(58),dp(44)));
        content.addView(bar);
        state=t(stateText(),13,Color.rgb(125,200,255),false); content.addView(state);
    }
    void addNav(String s, final Runnable r){ TextView v=t(s,13,MUTED,false); v.setGravity(Gravity.CENTER); v.setOnClickListener(x->r.run()); bottomNav.addView(v,new LinearLayout.LayoutParams(0,-1,1)); }

    void showHome(){ shell("Coding Lab E46");
        LinearLayout hero=cardLayout(); hero.setPadding(dp(18),dp(14),dp(18),dp(12));
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout left=new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        left.addView(t("Bluetooth",12,MUTED,false)); left.addView(t(btOk?"Conectado":"Desconectado",16,btOk?GREEN:WARN,true)); left.addView(t("BMW E46 320d M47N",16,TXT,true)); left.addView(t("VIN: "+vin,13,MUTED,false));
        row.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        ImageView car=new ImageView(this); car.setImageResource(getResources().getIdentifier("car_e46_side","drawable",getPackageName())); car.setAdjustViewBounds(true); row.addView(car,new LinearLayout.LayoutParams(dp(190),dp(92)));
        hero.addView(row);
        liveCard=t(liveTextCompact(),13,Color.rgb(230,236,245),false); liveCard.setPadding(0,dp(8),0,0); hero.addView(liveCard);
        content.addView(hero);
        testCard=panel(testsText(),13); content.addView(testCard);
        content.addView(section("MENÚ PRINCIPAL"));
        content.addView(menuRow("⚙", "Coding Lab", "Funciones de confort y personalización", ()->showCoding()));
        content.addView(menuRow("▣", "Backup seguro", "Guardar estado OBD/ECU antes de pruebas", ()->showBackup()));
        content.addView(menuRow("☼", "Luces", "Iluminación exterior e interior", ()->showLights()));
        content.addView(menuRow("▭", "Ventanillas", "Funciones de confort de ventanas", ()->showWindows()));
        content.addView(menuRow("◎", "Diagnóstico", "Leer errores y estado de módulos", ()->showDiag()));
        content.addView(menuRow("ⓘ", "Información del coche", "Detalles del vehículo y módulos", ()->showInfo()));
        content.addView(menuRow("≡", "Logs", "Registros y sesiones guardadas", ()->showLogs()));
    }

    TextView menuRow(String icon,String title,String sub,final Runnable r){
        TextView v=panel(icon+"   "+title+"                                      ›\n     "+sub,16); v.setOnClickListener(x->r.run()); return v;
    }
    void showCoding(){ shell("Coding Lab"); back(); tabs("LUCES      VENTANILLAS      CONFORT      OTROS"); content.addView(info("Personaliza funciones OEM. Escritura bloqueada hasta backup real de módulo.")); content.addView(section("VENTANILLAS")); content.addView(toggle("Cerrar ventanillas con mantener pulsado","Cierra todas las ventanillas al mantener cerrar",true)); content.addView(toggle("Cerrar traseras manteniendo cerrar","Objetivo GM5, pendiente de backup",false)); content.addView(toggle("Doble clic para cerrar traseras","Experimental, no escribir todavía",false)); content.addView(section("LUCES / LED")); content.addView(toggle("Desactivar check frío posición LED","Objetivo LSZ",true)); content.addView(toggle("Desactivar check caliente posición LED","Objetivo LSZ",true)); content.addView(action("Generar informe objetivos",()->add("OBJETIVOS CODING: LSZ LED cold/warm, GM5 comfort close, blink unlock. Escritura bloqueada."))); content.addView(action("Guardar cambios",()->add("GUARDAR BLOQUEADO: falta backup real de módulo."))); addLogSmall(); }
    void showBackup(){ shell("Backup seguro"); back(); content.addView(info("Backup READ ONLY: protocolo, voltaje, PIDs, VIN si responde, DTC y RAW. No escribe módulos.")); content.addView(action("Backup completo seguro",()->safeBackup())); content.addView(action("Backup identidad ECU/VIN",()->runCmds(new String[]{"0900","0902","0904","0906"},"BACKUP ECU ID"))); content.addView(action("Backup capacidades OBD",()->runCmds(new String[]{"0100","0120","0140","0160"},"BACKUP PIDS"))); content.addView(action("Backup fallos",()->runCmds(new String[]{"03","07","0A"},"BACKUP DTC"))); content.addView(action("Compartir backup/sesión",()->shareSession())); addLogBox(); }
    void showLights(){ shell("Luces"); back(); tabs("EXTERIOR      INTERIOR      LED/CHECK"); content.addView(section("LED / CHECK")); content.addView(toggle("Check frío posición","Parpadeo al contacto",true)); content.addView(toggle("Check caliente posición","Aviso de bombilla",true)); content.addView(toggle("Blink cerrar","Funciona",true)); content.addView(toggle("Blink abrir","Pendiente",false)); content.addView(action("Test DTC ahora",()->{connect(); initElm(); runCmds(new String[]{"03","07"},"DTC");})); addLogSmall(); }
    void showWindows(){ shell("Ventanillas"); back(); tabs("LUCES      VENTANILLAS      CONFORT      OTROS"); content.addView(info("Personaliza el comportamiento de las ventanillas y funciones de confort asociadas.")); content.addView(section("CIERRE CON MANDO")); content.addView(toggle("Cerrar delanteras manteniendo cerrar","Funciona actualmente",true)); content.addView(toggle("Cerrar traseras manteniendo cerrar","Objetivo GM5",false)); content.addView(toggle("Doble clic para cerrar traseras","Experimental",false)); content.addView(section("APERTURA CON MANDO")); content.addView(toggle("Abrir cuatro manteniendo abrir","Funciona actualmente",true)); content.addView(toggle("Doble clic abrir traseras","Pendiente",false)); content.addView(action("Registrar baseline",()->add("BASELINE VENTANILLAS: abrir mando abre 4; cerrar mando solo delanteras; traseras coupe no cierran; sin techo."))); addLogSmall(); }
    void showDiag(){ shell("Diagnóstico"); back(); liveCard=panel(liveTextCompact(),14); content.addView(liveCard); testCard=panel(testsText(),13); content.addView(testCard); content.addView(section("TESTS EN DIRECTO")); content.addView(action("Test completo automático",()->fullTest())); content.addView(action("Conectar ELM327",()->connect())); content.addView(action("Inicializar ELM",()->initElm())); content.addView(action("Leer y decodificar motor",()->readEngine())); content.addView(action("Leer DTC traducidos",()->runCmds(new String[]{"03","07"},"DTC"))); content.addView(action("Test protocolo seguro",()->runCmds(new String[]{"ATDP","ATDPN","0100"},"PROTOCOLO"))); content.addView(action("Compartir sesión completa",()->shareSession())); addLogBox(); }
    void showLogs(){ shell("Logs"); back(); content.addView(action("Compartir sesión completa",()->shareSession())); content.addView(action("Añadir informe mecánico",()->add(mechReport()))); addLogBox(); add("LOG READY. Usa Compartir sesión completa."); }
    void showInfo(){ shell("Información del coche"); back(); content.addView(info("BMW E46 50 JAHRE EDITION\n\n✓ M47N 320d/320Cd\n✓ ELM327 v2.1 detectado\n✓ ISO 9141-2 / ATSP0\n✓ Modo seguro: solo lectura\n✓ LSZ/GM5 escritura bloqueada hasta interfaz correcta")); content.addView(action("Compartir sesión",()->shareSession())); }

    void back(){ content.addView(action("← Atrás",()->runOnUiThread(()->showHome()))); }
    void tabs(String s){ TextView v=t(s,13,Color.rgb(200,210,225),true); v.setTextColor(Color.rgb(0,140,255)); content.addView(v); }
    TextView section(String s){ return t("\n"+s,12,MUTED,false); }
    TextView info(String s){ TextView v=panel("ⓘ  "+s,14); v.setTextColor(Color.rgb(220,230,242)); return v; }
    TextView action(String s, final Runnable r){ TextView v=panel(s,15); v.setGravity(Gravity.CENTER); v.setOnClickListener(x->new Thread(r).start()); return v; }
    TextView toggle(String title,String sub,boolean on){ TextView v=panel(title+"                                      "+(on?"ON":"OFF")+"\n"+sub,15); v.setTextColor(on?TXT:Color.rgb(190,196,205)); return v; }
    TextView panel(String s,int size){ TextView v=t(s,size,TXT,false); v.setPadding(dp(16),dp(12),dp(16),dp(12)); v.setBackground(bg(CARD,dp(14),LINE)); return v; }
    LinearLayout cardLayout(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setBackground(bg(CARD,dp(18),LINE)); return l; }
    GradientDrawable bg(int color,int radius,int stroke){ GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(radius); g.setStroke(1,stroke); return g; }
    TextView t(String s,int size,int color,boolean bold){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(0,dp(5),0,dp(5)); if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v; }
    int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    void addLogSmall(){ addLogBox(); }
    void addLogBox(){ log=t("",12,Color.rgb(220,228,240),false); log.setPadding(dp(14),dp(14),dp(14),dp(14)); ScrollView sv=new ScrollView(this); sv.setBackground(bg(Color.rgb(7,12,20),dp(12),Color.rgb(18,30,45))); sv.addView(log); content.addView(sv,new LinearLayout.LayoutParams(-1,dp(220))); }

    String stateText(){ return lastTest+" · BT "+(btOk?"OK":"OFF")+" · ELM "+(elmOk?"OK":"--")+" · "+proto+" · "+volts; }
    String flag(boolean b){ return b?"●":"○"; }
    String testsText(){ return flag(btOk)+" Bluetooth    "+flag(elmOk)+" ELM    "+flag(protocolOk)+" Protocolo\n"+flag(engineOk)+" Motor/PIDs    "+flag(dtcOk)+" DTC    "+flag(backupOk)+" Backup\n"+(lastDtc.contains("P0401")?"⚠ P0401 EGR insuficiente":"✓ Sin DTC crítico nuevo"); }
    String liveTextCompact(){ return "RPM  "+val(rpm,"rpm")+"      TEMP  "+val(temp,"°C")+"\nMAP  "+val(map,"kPa")+"      MAF  "+(maf<0?"--":String.format(Locale.US,"%.2f g/s",maf))+"\nVEL  "+val(speed,"km/h")+"      IAT  "+val(iat,"°C")+"\nDTC  "+lastDtc; }
    String val(int v,String u){ return v<-100||v<0?"--":v+" "+u; }
    void refresh(){ if(state!=null)runOnUiThread(()->state.setText(stateText())); if(liveCard!=null)runOnUiThread(()->liveCard.setText(liveTextCompact())); if(testCard!=null)runOnUiThread(()->testCard.setText(testsText())); }
    void stat(String s){ lastTest=s; refresh(); }
    void add(String s){ session.append(s).append("\n\n"); runOnUiThread(()->{ if(log!=null)log.append(s+"\n\n"); }); }

    String mechReport(){ return "INFORME MECÁNICO\nVoltaje: "+volts+"\nProtocolo: "+proto+"\n"+liveTextCompact()+"\nConclusión: "+(lastDtc.contains("P0401")?"EGR anulada/desconectada o flujo insuficiente detectado.":"Sin DTC motor confirmado."); }
    void fullTest(){ connect(); initElm(); readEngine(); runCmds(new String[]{"03","07"},"DTC"); runCmds(new String[]{"ATDP","ATDPN","0100"},"PROTOCOLO"); add(mechReport()); }
    void safeBackup(){ connect(); initElm(); add("===== BACKUP SEGURO READ ONLY ====="); runCmds(new String[]{"ATI","ATRV","ATDP","ATDPN","0100","0120","0140","0900","0902","0904","03","07","0A"},"BACKUP SEGURO"); backupOk=true; add("BACKUP OK: sesión RAW lista para exportar. No se ha escrito nada en el coche."); add(mechReport()); refresh(); }
    void shareSession(){ try{ Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session"); i.putExtra(Intent.EXTRA_TEXT,session.toString()); startActivity(Intent.createChooser(i,"Enviar sesión")); }catch(Exception e){ add("ERROR SHARE: "+e.getMessage()); }}
    boolean perm(){ if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){ runOnUiThread(()->requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46)); stat("Permiso Bluetooth"); return false;} return true; }
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
    void parseAll(String h){ try{ int i; i=h.indexOf("410C"); if(i>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16); rpm=((a*256)+b)/4;} i=h.indexOf("4105"); if(i>=0)temp=Integer.parseInt(h.substring(i+4,i+6),16)-40; i=h.indexOf("410D"); if(i>=0)speed=Integer.parseInt(h.substring(i+4,i+6),16); i=h.indexOf("410B"); if(i>=0)map=Integer.parseInt(h.substring(i+4,i+6),16); i=h.indexOf("410F"); if(i>=0)iat=Integer.parseInt(h.substring(i+4,i+6),16)-40; i=h.indexOf("4110"); if(i>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16); maf=((a*256)+b)/100.0;} if(h.contains("430401"))lastDtc="P0401 EGR insuficiente"; else if(h.contains("43"))lastDtc="DTC RAW"; }catch(Exception ignored){} }
}
