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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private LinearLayout root, body;
    private TextView statusText, liveText, logText;
    private final ArrayList<String> stack = new ArrayList<>();
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;
    private BluetoothDevice device;
    private final StringBuilder session = new StringBuilder();

    private boolean bt=false, elm=false, protocol=false, engine=false, dtcOk=false, backup=false;
    private String volts="--", proto="--", dtc="--", vin="WBABN510X0JU12345", state="LISTO";
    private int rpm=-1, temp=-999, map=-1, speed=-1, iat=-999;
    private double maf=-1;

    private static final UUID SPP=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final int BG=Color.rgb(0,6,14), CARD=Color.rgb(6,17,31), CARD2=Color.rgb(8,20,36), LINE=Color.rgb(24,48,76);
    private final int TXT=Color.WHITE, MUT=Color.rgb(170,181,198), BLUE=Color.rgb(0,122,255), GREEN=Color.rgb(42,225,112), RED=Color.rgb(255,70,90), YELLOW=Color.rgb(245,205,50), PURPLE=Color.rgb(160,90,255);

    @Override public void onCreate(Bundle b){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        session.append("BMW E46 SCANNER SESSION\n").append(now()).append("\nCar: BMW E46 coupe 320d/320Cd M47N\nMode: READ ONLY\n\n");
        go("home", false);
    }

    @Override public void onBackPressed(){
        if(stack.size()>1){stack.remove(stack.size()-1);render(stack.get(stack.size()-1), false);} else super.onBackPressed();
    }

    private String now(){return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date());}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}    
    private TextView tv(String s,int sp,int c,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(c);v.setIncludeFontPadding(true);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private GradientDrawable box(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));g.setStroke(dp(1),LINE);return g;}
    private GradientDrawable mainBg(){return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Color.rgb(2,13,25),BG});}
    private void addSpace(int h){Space s=new Space(this);body.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}

    private void go(String screen, boolean push){ if(push)stack.add(screen); else {stack.clear();stack.add(screen);} render(screen,false); }
    private void render(String screen, boolean ignored){
        if(screen.equals("home")) home();
        else if(screen.equals("coding")) coding(1);
        else if(screen.equals("lights")) coding(0);
        else if(screen.equals("comfort")) coding(2);
        else if(screen.equals("diag")) diag();
        else if(screen.equals("info")) info();
        else if(screen.equals("logs")) logs();
    }

    private void base(String title, boolean back, boolean nav, boolean actionBar, int activeNav){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackground(mainBg());
        ScrollView sc=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(28),dp(10),dp(28),dp(8));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        header(title,back);
        if(actionBar) bottomActions();
        if(nav) bottomNav(activeNav);
    }

    private void header(String title, boolean back){
        LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);
        TextView left=tv(back?"‹":"☰",34,TXT,false);left.setGravity(Gravity.CENTER);left.setOnClickListener(v->onBackPressed());h.addView(left,new LinearLayout.LayoutParams(dp(44),dp(46)));
        TextView mid=tv(title,20,TXT,false);mid.setGravity(Gravity.CENTER);h.addView(mid,new LinearLayout.LayoutParams(0,dp(46),1));
        TextView right=tv(back?"▱":"E46",back?23:14,back?TXT:BLUE,true);right.setGravity(Gravity.CENTER);h.addView(right,new LinearLayout.LayoutParams(dp(56),dp(46)));
        body.addView(h);
        statusText=tv(statusLine(),11,bt?GREEN:RED,false);statusText.setGravity(Gravity.CENTER);body.addView(statusText);
        addSpace(8);
    }

    private String statusLine(){return (bt?"● Conectado":"● Desconectado")+"  ·  "+state+"  ·  ELM "+(elm?"OK":"--")+"  ·  "+volts;}
    private void refresh(){runOnUiThread(()->{if(statusText!=null){statusText.setText(statusLine());statusText.setTextColor(bt?GREEN:RED);}if(liveText!=null)liveText.setText(live());});}

    private void home(){
        base("Coding Lab E46",false,true,false,0);
        FrameLayout hero=new FrameLayout(this);body.addView(hero,new LinearLayout.LayoutParams(-1,dp(232)));
        TextView car=tv("",1,TXT,false);car.setBackgroundResource(getResources().getIdentifier("bmw_e46_black_coupe_hero","drawable",getPackageName()));
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(dp(400),dp(176),Gravity.RIGHT|Gravity.TOP);cp.topMargin=dp(8);cp.rightMargin=dp(-10);hero.addView(car,cp);
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(tv(bt?"Bluetooth   Conectado":"Bluetooth   Desconectado",13,bt?GREEN:RED,false));
        info.addView(tv("BMW E46 320d M47N",17,TXT,true));
        info.addView(tv("VIN: "+vin,12,MUT,false));
        TextView read=tv("READ ONLY · SAFE MODE",11,BLUE,true);read.setPadding(0,dp(5),0,0);info.addView(read);
        hero.addView(info,new FrameLayout.LayoutParams(dp(230),-1,Gravity.LEFT));
        section("MENÚ PRINCIPAL");
        menu("⌕","Coding Lab","Funciones de confort y personalización",BLUE,()->go("coding",true));
        menu("☼","Luces","Iluminación exterior e interior",YELLOW,()->go("lights",true));
        menu("▭","Ventanillas","Funciones de confort de ventanas",GREEN,()->go("coding",true));
        menu("▣","Confort","Cierre, apertura y otras funciones",PURPLE,()->go("comfort",true));
        menu("◌","LED / Check","Gestión de LED y testigos",YELLOW,()->go("lights",true));
        menu("▰","Diagnóstico","Leer errores y estado de módulos",GREEN,()->go("diag",true));
        menu("ⓘ","Información del coche","Detalles del vehículo y módulos",BLUE,()->go("info",true));
        menu("▤","Logs","Registros y sesiones guardadas",PURPLE,()->go("logs",true));
    }

    private void section(String s){TextView v=tv(s,12,MUT,false);v.setLetterSpacing(0.08f);v.setPadding(0,dp(12),0,dp(7));body.addView(v);}    
    private void menu(String ic,String title,String sub,int col,Runnable click){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(12),0,dp(10),0);r.setBackground(box(CARD2,10));r.setOnClickListener(v->click.run());
        TextView i=tv(ic,26,col,false);i.setGravity(Gravity.CENTER);r.addView(i,new LinearLayout.LayoutParams(dp(62),-1));
        LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);texts.setGravity(Gravity.CENTER_VERTICAL);texts.addView(tv(title,16,TXT,false));texts.addView(tv(sub,12,MUT,false));r.addView(texts,new LinearLayout.LayoutParams(0,-1,1));
        TextView ar=tv("›",30,MUT,false);ar.setGravity(Gravity.CENTER);r.addView(ar,new LinearLayout.LayoutParams(dp(26),-1));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(64));lp.setMargins(0,0,0,dp(7));body.addView(r,lp);
    }

    private void coding(int tab){
        base("Coding Lab",true,true,true,1);
        tabs(tab);
        infoCard("Personaliza el comportamiento de las ventanillas y funciones de confort asociadas.");
        section("CIERRE CON MANDO"); group(new String[][]{{"Cerrar ventanillas con mantener pulsado","Cierra todas las ventanillas al mantener pulsado el botón de cerrar.","1"},{"Cerrar ventanillas traseras con mantener","Cierra solo las ventanillas traseras al mantener pulsado cerrar.","1"},{"Doble clic para cerrar traseras","Doble clic en cerrar para subir traseras automáticamente.","1"},{"Doble clic para abrir traseras","Doble clic en abrir para bajar traseras automáticamente.","0"}});
        section("APERTURA CON MANDO"); group(new String[][]{{"Abrir ventanillas con mantener pulsado","Abre todas las ventanillas al mantener pulsado abrir.","1"},{"Doble clic para abrir traseras","Doble clic en abrir para bajar traseras automáticamente.","1"}});
        section("OTRAS OPCIONES"); group(new String[][]{{"Bajar ventanillas al abrir la puerta","Baja ligeramente la ventanilla al abrir puerta del conductor.","0"},{"Blink unlock","Intermitentes al abrir con mando.","0"},{"LED cold/warm check","Preparado para LSZ. Escritura bloqueada hasta backup.","1"}});
    }

    private void tabs(int active){
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout r=new LinearLayout(this);String[] names={"LUCES","VENTANILLAS","CONFORT","OTROS"};
        for(int i=0;i<names.length;i++){final int idx=i;TextView v=tv(names[i],12,i==active?BLUE:MUT,true);v.setGravity(Gravity.CENTER);v.setOnClickListener(x->coding(idx));r.addView(v,new LinearLayout.LayoutParams(dp(116),dp(42)));}
        hsv.addView(r);body.addView(hsv);View line=new View(this);line.setBackgroundColor(BLUE);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(116),dp(2));lp.leftMargin=dp(116*active);body.addView(line,lp);addSpace(10);
    }

    private void infoCard(String s){TextView v=tv("ⓘ    "+s,13,Color.rgb(215,225,240),false);v.setPadding(dp(14),dp(11),dp(14),dp(11));v.setBackground(box(CARD2,8));body.addView(v,new LinearLayout.LayoutParams(-1,-2));}
    private void group(String[][] rows){LinearLayout g=new LinearLayout(this);g.setOrientation(LinearLayout.VERTICAL);g.setBackground(box(CARD2,8));for(String[] row:rows)g.addView(switchRow(row[0],row[1],row[2].equals("1")));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));body.addView(g,lp);}    
    private View switchRow(String title,String sub,boolean on){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(14),dp(7),dp(10),dp(7));
        LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.addView(tv(title,14,TXT,false));TextView desc=tv(sub,12,MUT,false);desc.setMaxLines(2);tx.addView(desc);r.addView(tx,new LinearLayout.LayoutParams(0,dp(55),1));
        Switch sw=new Switch(this);sw.setChecked(on);tint(sw);sw.setOnCheckedChangeListener((b,c)->add("SWITCH "+title+" = "+(c?"ON":"OFF")));r.addView(sw,new LinearLayout.LayoutParams(dp(62),dp(42)));return r;
    }
    private void tint(Switch sw){if(Build.VERSION.SDK_INT>=21){int[][] st={new int[]{android.R.attr.state_checked},new int[]{-android.R.attr.state_checked}};sw.setThumbTintList(new ColorStateList(st,new int[]{Color.WHITE,Color.WHITE}));sw.setTrackTintList(new ColorStateList(st,new int[]{BLUE,Color.rgb(50,65,82)}));}}

    private void bottomActions(){LinearLayout bar=new LinearLayout(this);bar.setPadding(dp(22),dp(8),dp(22),dp(12));bar.setBackgroundColor(Color.rgb(1,8,16));TextView r=button("↻  RESTABLECER",false);TextView s=button("▣  GUARDAR CAMBIOS",true);r.setOnClickListener(v->add("RESTABLECER: visual, sin escritura."));s.setOnClickListener(v->add("GUARDAR BLOQUEADO: falta backup real del módulo."));bar.addView(r,new LinearLayout.LayoutParams(0,dp(52),1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.leftMargin=dp(12);bar.addView(s,lp);root.addView(bar,new LinearLayout.LayoutParams(-1,dp(74)));}
    private TextView button(String s,boolean primary){TextView v=tv(s,14,primary?TXT:BLUE,true);v.setGravity(Gravity.CENTER);v.setBackground(box(primary?BLUE:Color.TRANSPARENT,8));return v;}

    private void bottomNav(int active){LinearLayout n=new LinearLayout(this);n.setBackgroundColor(Color.rgb(1,8,16));String[] a={"⌂\nInicio","◇\nMódulos","▤\nLogs","⚙\nAjustes"};Runnable[] run={()->go("home",false),()->go("coding",true),()->go("logs",true),()->go("info",true)};for(int i=0;i<4;i++){final int idx=i;TextView v=tv(a[i],12,i==active?BLUE:MUT,false);v.setGravity(Gravity.CENTER);v.setOnClickListener(x->run[idx].run());n.addView(v,new LinearLayout.LayoutParams(0,dp(58),1));}root.addView(n,new LinearLayout.LayoutParams(-1,dp(62)));}

    private void diag(){base("Diagnóstico",true,true,false,2);liveText=card(live(),14);body.addView(liveText);body.addView(action("Conectar ELM327",()->connect()));body.addView(action("Inicializar ELM",()->initElm()));body.addView(action("Leer motor básico",()->readEngine()));body.addView(action("Leer DTC",()->runCmds(new String[]{"03","07"},"DTC")));body.addView(action("Backup seguro READ ONLY",()->safeBackup()));body.addView(action("Compartir sesión completa",()->share()));logBox();}
    private void info(){base("Información del coche",true,true,false,3);body.addView(card("BMW E46 320d/320Cd M47N\nVIN: "+vin+"\nModo: READ ONLY\nObjetivos: LSZ LED cold/warm, GM5 comfort close, blink unlock.\nEscritura bloqueada hasta backup.",14));}
    private void logs(){base("Logs",true,true,false,2);body.addView(action("Compartir sesión completa",()->share()));logBox();}
    private TextView card(String s,int sp){TextView v=tv(s,sp,TXT,false);v.setPadding(dp(14),dp(12),dp(14),dp(12));v.setBackground(box(CARD2,10));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));v.setLayoutParams(lp);return v;}
    private TextView action(String s,Runnable run){TextView v=card(s,14);v.setGravity(Gravity.CENTER_VERTICAL);v.setOnClickListener(x->new Thread(run).start());v.setHeight(dp(52));return v;}
    private void logBox(){logText=card(session.toString(),12);body.addView(logText,new LinearLayout.LayoutParams(-1,dp(245)));}
    private void add(String s){session.append(s).append("\n\n");runOnUiThread(()->{if(logText!=null)logText.setText(session.toString());});}

    private String live(){return "RPM  "+val(rpm,"rpm")+"     TEMP  "+val(temp,"°C")+"\nMAP  "+val(map,"kPa")+"     MAF  "+(maf<0?"--":String.format(Locale.US,"%.2f g/s",maf))+"\nVEL  "+val(speed,"km/h")+"     IAT  "+val(iat,"°C")+"\nDTC  "+dtc;} private String val(int v,String u){return v<-100||v<0?"--":v+" "+u;}

    private boolean perm(){if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){runOnUiThread(()->requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46));return false;}return true;}
    private void connect(){try{state="Conectando BT";refresh();if(!perm())return;BluetoothAdapter ad=BluetoothAdapter.getDefaultAdapter();if(ad==null){add("Sin Bluetooth");return;}Set<BluetoothDevice> set=ad.getBondedDevices();device=null;for(BluetoothDevice d:set){String n=d.getName()==null?"":d.getName().toLowerCase();if(device==null||n.contains("obd")||n.contains("elm")||n.contains("vlink")||n.contains("icar"))device=d;}if(device==null){add("Empareja primero el ELM327");return;}close();socket=device.createRfcommSocketToServiceRecord(SPP);socket.connect();in=socket.getInputStream();out=socket.getOutputStream();bt=true;state="BT OK";add("BT OK: "+device.getName()+" / "+device.getAddress());refresh();}catch(Exception e){bt=false;add("ERROR BT: "+e.getClass().getSimpleName()+" - "+e.getMessage());close();refresh();}}
    private void close(){try{if(socket!=null)socket.close();}catch(Exception ignored){}socket=null;in=null;out=null;}
    private boolean ready(){if(socket==null||!socket.isConnected()||in==null||out==null){add("Pulsa conectar primero");return false;}return true;}
    private void initElm(){connectIfNeeded();runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM");elm=true;refresh();}
    private void readEngine(){connectIfNeeded();runCmds(new String[]{"0120","010C","0105","010D","010B","010F","0110"},"MOTOR");engine=true;refresh();}
    private void safeBackup(){connectIfNeeded();initElm();runCmds(new String[]{"ATI","ATRV","ATDP","ATDPN","0100","0120","0140","03","07","0A"},"BACKUP SEGURO");backup=true;add("BACKUP OK: RAW listo para exportar. No se ha escrito nada.");refresh();}
    private void connectIfNeeded(){if(socket==null||!socket.isConnected())connect();}
    private void runCmds(String[] cs,String name){try{if(!ready())return;state=name;refresh();add("===== "+name+" =====");for(String c:cs)send(c);if(name.contains("DTC"))dtcOk=true;if(name.contains("PROTO"))protocol=true;state=name+" OK";refresh();}catch(Exception e){add("ERROR "+name+": "+e.getMessage());close();refresh();}}
    private void send(String c)throws Exception{out.write((c+"\r").getBytes("US-ASCII"));out.flush();Thread.sleep(c.equals("ATZ")?1600:850);String r=read();add("> "+c+"\n"+r);parse(c,r);}
    private String read()throws Exception{byte[] b=new byte[512];StringBuilder s=new StringBuilder();long end=System.currentTimeMillis()+1600;while(System.currentTimeMillis()<end){while(in.available()>0){int n=in.read(b);if(n>0)s.append(new String(b,0,n,"US-ASCII"));}if(s.toString().contains(">"))break;Thread.sleep(60);}String r=s.toString().replace('\r',' ').replace('\n',' ').trim();return r.length()==0?"SIN RESPUESTA":r;}
    private void parse(String c,String r){try{if(c.equals("ATRV"))volts=r.replace(">","").trim();if(c.equals("ATDP"))proto=r.replace(">","").trim();String h=r.replace(" ","").replace(">","");int i;if((i=h.indexOf("410C"))>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16);rpm=((a*256)+b)/4;}if((i=h.indexOf("4105"))>=0)temp=Integer.parseInt(h.substring(i+4,i+6),16)-40;if((i=h.indexOf("410D"))>=0)speed=Integer.parseInt(h.substring(i+4,i+6),16);if((i=h.indexOf("410B"))>=0)map=Integer.parseInt(h.substring(i+4,i+6),16);if((i=h.indexOf("410F"))>=0)iat=Integer.parseInt(h.substring(i+4,i+6),16)-40;if((i=h.indexOf("4110"))>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16);maf=((a*256)+b)/100.0;}if(h.contains("430401"))dtc="P0401 EGR insuficiente";}catch(Exception ignored){}refresh();}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session");i.putExtra(Intent.EXTRA_TEXT,session.toString());startActivity(Intent.createChooser(i,"Enviar sesión"));}
}
