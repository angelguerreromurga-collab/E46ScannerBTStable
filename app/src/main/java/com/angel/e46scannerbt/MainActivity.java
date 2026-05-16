package com.angel.e46scannerbt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    LinearLayout root, body;
    TextView status, live, log;
    BluetoothSocket socket; InputStream in; OutputStream out; BluetoothDevice device;
    StringBuilder session = new StringBuilder();
    boolean bt=false, elm=false; String volts="--", proto="--", dtc="--", vin="pendiente", test="LISTO";
    int rpm=-1,temp=-999,map=-1,speed=-1,iat=-999; double maf=-1;
    static final UUID SPP=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG=Color.rgb(0,6,14), CARD=Color.rgb(7,18,32), LINE=Color.rgb(28,55,86), TXT=Color.WHITE, MUT=Color.rgb(170,180,197), BLUE=Color.rgb(42,145,255), GREEN=Color.rgb(49,230,119), RED=Color.rgb(255,70,90), YELLOW=Color.rgb(255,210,55), PURPLE=Color.rgb(174,88,255);

    public void onCreate(Bundle b){super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);session.append("BMW E46 SCANNER SESSION\n").append(now()).append("\nMode: READ ONLY\n\n");home();}
    int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);} String now(){return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date());}
    TextView t(String s,int sp,int c,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(c);if(b)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    GradientDrawable bg(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));g.setStroke(1,LINE);return g;}
    void base(String title,boolean back,boolean nav,boolean buttons,int tab){root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Color.rgb(2,12,24),BG}));ScrollView sc=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(22),dp(12),dp(22),0);sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);header(title,back);if(tab>=0)tabs(tab);if(nav)bottomNav();if(buttons)codingBar();}
    void header(String title,boolean back){LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);TextView l=t(back?"‹":"☰",34,TXT,false);l.setGravity(Gravity.CENTER);l.setOnClickListener(v->home());h.addView(l,new LinearLayout.LayoutParams(dp(42),dp(44)));TextView m=t(title,20,TXT,false);m.setGravity(Gravity.CENTER);h.addView(m,new LinearLayout.LayoutParams(0,dp(44),1));TextView r=t(back?"▱":"V3.1",back?24:12,back?TXT:BLUE,true);r.setGravity(Gravity.CENTER);h.addView(r,new LinearLayout.LayoutParams(dp(52),dp(44)));body.addView(h);status=t(statusLine(),11,bt?GREEN:RED,false);status.setGravity(Gravity.CENTER);body.addView(status);space(6);}    
    String statusLine(){return (bt?"● CONECTADO":"● DESCONECTADO")+"  •  "+test+"  •  ELM "+(elm?"OK":"--")+"  "+volts;}
    void ref(){runOnUiThread(()->{if(status!=null){status.setText(statusLine());status.setTextColor(bt?GREEN:RED);}if(live!=null)live.setText(liveText());});}
    void space(int h){Space s=new Space(this);body.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}

    public void home(){base("Coding Lab E46",false,true,false,-1);FrameLayout hero=new FrameLayout(this);body.addView(hero,new LinearLayout.LayoutParams(-1,dp(360)));TextView car=t("",1,TXT,false);car.setBackgroundResource(getResources().getIdentifier("e46_front_premium","drawable",getPackageName()));FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,dp(250),Gravity.TOP);cp.topMargin=dp(38);hero.addView(car,cp);LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setGravity(Gravity.BOTTOM);info.addView(t("BMW E46 320d M47N",20,TXT,false));info.addView(t("VIN: "+vin,13,MUT,false));info.addView(t("READ ONLY · SAFE MODE  ⓘ",12,BLUE,true));FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(-1,dp(100),Gravity.BOTTOM);hero.addView(info,ip);quick();section("MENÚ PRINCIPAL");row("‹/›","Coding Lab","Funciones de confort y personalización",BLUE,()->coding());row("◖","Luces","Iluminación exterior e interior",YELLOW,()->lights());row("▱","Ventanas","Funciones de confort de ventanas",GREEN,()->coding());row("▢","Confort","Cierre, apertura y funciones GM5",PURPLE,()->comfort());row("◌","LED / Check","Gestión de LED y testigos",YELLOW,()->lights());row("◎","Diagnóstico","Leer errores y estado de módulos",GREEN,()->diag());row("ⓘ","Información del coche","Detalles del vehículo y módulos",BLUE,()->info());row("☷","Logs","Registros de actividad y operaciones",PURPLE,()->logs());}
    void quick(){section("ACCESOS RÁPIDOS");LinearLayout q=new LinearLayout(this);q.setOrientation(LinearLayout.HORIZONTAL);String[][] a={{"‹/›","Coding Lab"},{"◖","Luces"},{"▱","Ventanas"},{"▢","Confort"}};for(String[] x:a){TextView v=t(x[0]+"\n"+x[1],13,x[1].equals("Luces")?YELLOW:x[1].equals("Ventanas")?GREEN:x[1].equals("Confort")?PURPLE:BLUE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(CARD,10));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(92),1);lp.setMargins(0,0,dp(8),0);q.addView(v,lp);}body.addView(q);}
    void section(String s){TextView v=t(s,12,MUT,false);v.setLetterSpacing(.12f);v.setPadding(0,dp(16),0,dp(8));body.addView(v);}    
    void row(String ic,String title,String sub,int col,Runnable run){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(16),0,dp(14),0);r.setBackground(bg(CARD,10));TextView i=t(ic,26,col,false);i.setGravity(Gravity.CENTER);r.addView(i,new LinearLayout.LayoutParams(dp(58),-1));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setGravity(Gravity.CENTER_VERTICAL);tx.addView(t(title,17,TXT,false));tx.addView(t(sub,12,MUT,false));r.addView(tx,new LinearLayout.LayoutParams(0,-1,1));TextView ar=t("›",30,MUT,false);ar.setGravity(Gravity.CENTER);r.addView(ar,new LinearLayout.LayoutParams(dp(25),-1));r.setOnClickListener(v->run.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(72));lp.setMargins(0,0,0,dp(8));body.addView(r,lp);}    

    public void coding(){base("Coding Lab",true,true,false,0);settings();}
    public void lights(){base("Coding Lab",true,true,false,1);settings();}
    public void comfort(){base("Coding Lab",true,true,false,0);settings();}
    void tabs(int active){LinearLayout r=new LinearLayout(this);String[] a={"CONFORT","EXTERIOR","INTERIOR","OTROS"};for(int i=0;i<4;i++){TextView v=t(a[i],12,i==active?BLUE:MUT,true);v.setGravity(Gravity.CENTER);r.addView(v,new LinearLayout.LayoutParams(0,dp(42),1));}body.addView(r);}    
    void settings(){LinearLayout g=new LinearLayout(this);g.setOrientation(LinearLayout.VERTICAL);g.setBackground(bg(CARD,8));String[][] rows={{"Cierre automático","Bloquear al alejarse del coche","1"},{"Apertura confort","Abrir con una pulsación","1"},{"Cerrar ventanillas con mando","Mantener pulsado para cerrar","1"},{"Seatbelt warning","Aviso acústico cinturón","0"},{"Luz de bienvenida","Activar al desbloquear","1"},{"Luz follow me home","Al salir del vehículo","1"},{"Tercera luz de freno","Activar con codificación","1"},{"Parpadeo confort","3 parpadeos al tocar intermitente","1"},{"Climatización al abrir","Recircular al abrir puertas","0"},{"Espejos abatibles","Plegar con mando","1"},{"Advertencia puerta abierta","Aviso al abrir con contacto","1"}};for(String[] x:rows)g.addView(switchRow(x[0],x[1],x[2].equals("1")));body.addView(g);body.addView(action("↻    Restaurar valores de fábrica",()->add("Restaurar valores visuales")));}
    View switchRow(String title,String sub,boolean on){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(16),dp(10),dp(12),dp(10));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.addView(t(title,16,TXT,false));tx.addView(t(sub,13,MUT,false));r.addView(tx,new LinearLayout.LayoutParams(0,dp(58),1));Switch sw=new Switch(this);sw.setChecked(on);if(Build.VERSION.SDK_INT>=21){int[][] st={new int[]{android.R.attr.state_checked},new int[]{-android.R.attr.state_checked}};sw.setThumbTintList(new ColorStateList(st,new int[]{Color.WHITE,Color.WHITE}));sw.setTrackTintList(new ColorStateList(st,new int[]{BLUE,Color.rgb(45,62,88)}));}r.addView(sw,new LinearLayout.LayoutParams(dp(62),dp(48)));TextView ar=t("›",28,MUT,false);ar.setGravity(Gravity.CENTER);r.addView(ar,new LinearLayout.LayoutParams(dp(25),-1));return r;}
    TextView action(String s,Runnable run){TextView v=t(s,15,TXT,false);v.setGravity(Gravity.CENTER_VERTICAL);v.setPadding(dp(18),0,0,0);v.setBackground(bg(CARD,8));v.setOnClickListener(x->new Thread(run).start());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(76));lp.setMargins(0,dp(16),0,0);v.setLayoutParams(lp);return v;}
    void bottomNav(){LinearLayout n=new LinearLayout(this);n.setBackgroundColor(Color.rgb(1,8,16));String[] a={"⌂\nInicio","◇\nMódulos","▤\nLogs","⚙\nAjustes"};for(String s:a){TextView v=t(s,12,s.startsWith("⌂")?BLUE:MUT,false);v.setGravity(Gravity.CENTER);n.addView(v,new LinearLayout.LayoutParams(0,dp(70),1));}root.addView(n);}void codingBar(){}

    public void diag(){base("Diagnóstico",true,true,false,-1);live=t(liveText(),14,TXT,false);live.setPadding(dp(14),dp(14),dp(14),dp(14));live.setBackground(bg(CARD,10));body.addView(live);body.addView(action("Conectar ELM327",()->connect()));body.addView(action("Inicializar ELM",()->initElm()));body.addView(action("Leer motor",()->readEngine()));body.addView(action("Leer DTC",()->runCmds(new String[]{"03","07"},"DTC")));body.addView(action("Backup seguro READ ONLY",()->safeBackup()));body.addView(action("Compartir sesión",()->share()));logBox();}
    public void info(){base("Información",true,true,false,-1);body.addView(action("Compartir sesión",()->share()));}
    public void logs(){base("Logs",true,true,false,-1);body.addView(action("Compartir sesión completa",()->share()));logBox();}
    void logBox(){log=t("",12,Color.rgb(220,230,240),false);log.setPadding(dp(14),dp(14),dp(14),dp(14));log.setBackground(bg(CARD,10));body.addView(log,new LinearLayout.LayoutParams(-1,dp(260)));}
    String liveText(){return "RPM "+(rpm<0?"--":rpm)+"   TEMP "+(temp<-100?"--":temp)+"\nMAP "+(map<0?"--":map)+"   MAF "+(maf<0?"--":String.format(Locale.US,"%.2f",maf))+"\nVEL "+(speed<0?"--":speed)+"   IAT "+(iat<-100?"--":iat)+"\nDTC "+dtc;}
    void add(String s){session.append(s).append("\n\n");runOnUiThread(()->{if(log!=null)log.append(s+"\n\n");});}
    void connect(){try{test="Conectando";ref();if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46);return;}BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter();Set<BluetoothDevice> b=a.getBondedDevices();for(BluetoothDevice d:b){String n=d.getName()==null?"":d.getName().toLowerCase();if(device==null||n.contains("obd")||n.contains("elm"))device=d;}socket=device.createRfcommSocketToServiceRecord(SPP);socket.connect();in=socket.getInputStream();out=socket.getOutputStream();bt=true;test="BT OK";add("BT OK: "+device.getName());ref();}catch(Exception e){bt=false;add("ERROR BT: "+e.getMessage());ref();}}
    boolean ready(){if(socket==null||!socket.isConnected()){add("Pulsa conectar primero");return false;}return true;}
    void initElm(){runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM");elm=true;ref();}
    void readEngine(){runCmds(new String[]{"010C","0105","010D","010B","010F","0110"},"MOTOR");}
    void safeBackup(){connect();initElm();runCmds(new String[]{"ATI","ATRV","ATDP","ATDPN","0100","0120","0140","03","07","0A"},"BACKUP");}
    void runCmds(String[] cs,String name){try{if(!ready())return;test=name;ref();for(String c:cs)send(c);test=name+" OK";ref();}catch(Exception e){add("ERROR "+name+": "+e.getMessage());}}
    void send(String c)throws Exception{out.write((c+"\r").getBytes("US-ASCII"));out.flush();Thread.sleep(c.equals("ATZ")?1600:900);String r=read();add("> "+c+"\n"+r);parse(c,r);}String read()throws Exception{byte[] buf=new byte[512];StringBuilder s=new StringBuilder();long end=System.currentTimeMillis()+1600;while(System.currentTimeMillis()<end){while(in.available()>0){int n=in.read(buf);if(n>0)s.append(new String(buf,0,n,"US-ASCII"));}if(s.toString().contains(">"))break;Thread.sleep(60);}String r=s.toString().replace('\r',' ').replace('\n',' ').trim();return r.length()==0?"SIN RESPUESTA":r;}
    void parse(String cmd,String r){try{if(cmd.equals("ATRV"))volts=r.replace(">","").trim();if(cmd.equals("ATDP"))proto=r.replace(">","").trim();String h=r.replace(" ","").replace(">","");int i;if((i=h.indexOf("410C"))>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16);rpm=((a*256)+b)/4;}if((i=h.indexOf("4105"))>=0)temp=Integer.parseInt(h.substring(i+4,i+6),16)-40;if((i=h.indexOf("410D"))>=0)speed=Integer.parseInt(h.substring(i+4,i+6),16);if((i=h.indexOf("410B"))>=0)map=Integer.parseInt(h.substring(i+4,i+6),16);if((i=h.indexOf("410F"))>=0)iat=Integer.parseInt(h.substring(i+4,i+6),16)-40;if((i=h.indexOf("4110"))>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16);maf=((a*256)+b)/100.0;}if(h.contains("430401"))dtc="P0401 EGR insuficiente";}catch(Exception ignored){}ref();}
    void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,session.toString());startActivity(Intent.createChooser(i,"Enviar sesión"));}
}
