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
import android.widget.ImageView;
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
    View statusStrip;
    TextView statusText, liveBox, testsBox, logBox;
    BluetoothSocket socket;
    InputStream in;
    OutputStream out;
    BluetoothDevice device;
    StringBuilder session = new StringBuilder();

    String volts = "--", proto = "--", dtc = "--", test = "LISTO", vin = "pendiente";
    int rpm = -1, temp = -999, map = -1, speed = -1, iat = -999;
    double maf = -1;
    boolean bt = false, elm = false, protocol = false, engine = false, dtcOk = false, backup = false;

    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int BG = Color.rgb(1,5,11), PANEL = Color.rgb(7,15,26), PANEL2 = Color.rgb(9,18,31), LINE = Color.rgb(22,39,60);
    final int TXT = Color.WHITE, MUTED = Color.rgb(156,166,182), BLUE = Color.rgb(0,122,255), GREEN = Color.rgb(42,220,105), RED = Color.rgb(255,58,72), YELLOW = Color.rgb(245,195,45), PURPLE = Color.rgb(145,85,255);

    @Override public void onCreate(Bundle b){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(b);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        session.append("BMW E46 SCANNER SESSION\n").append(now()).append("\nCar: BMW E46 coupe 320d/320Cd M47N\nMode: READ ONLY\n\n");
        showHome();
    }

    String now(){return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());}
    int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}    
    TextView txt(String s,int sp,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(color);v.setIncludeFontPadding(true);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    GradientDrawable bg(int color,int radius,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));g.setStroke(1,stroke);return g;}

    void base(String title, boolean back, boolean bottomNav, boolean codingButtons, int tab){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        statusStrip=new View(this); statusStrip.setBackgroundColor(bt?GREEN:RED); root.addView(statusStrip,new LinearLayout.LayoutParams(-1,dp(4)));
        ScrollView scroll=new ScrollView(this); body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(22),dp(14),dp(22),dp(6)); scroll.addView(body); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
        header(title,back); if(tab>=0)tabs(tab); if(bottomNav)addBottomNav(0); if(codingButtons)addCodingButtons();
    }

    void header(String title, boolean back){
        LinearLayout h=new LinearLayout(this); h.setOrientation(LinearLayout.HORIZONTAL); h.setGravity(Gravity.CENTER_VERTICAL);
        TextView left=txt(back?"‹":"☰",32,TXT,false); left.setGravity(Gravity.CENTER); left.setOnClickListener(v->showHome()); h.addView(left,new LinearLayout.LayoutParams(dp(42),dp(42)));
        TextView name=txt(title,20,TXT,false); name.setGravity(Gravity.CENTER); h.addView(name,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView right=txt(back?"▱":"V2.1",12,back?TXT:BLUE,true); right.setGravity(Gravity.CENTER); h.addView(right,new LinearLayout.LayoutParams(dp(48),dp(42))); body.addView(h);
        statusText=txt(statusLine(),11,bt?GREEN:RED,false); statusText.setGravity(Gravity.CENTER); body.addView(statusText); space(6);
    }

    String statusLine(){return (bt?"CONECTADO":"DESCONECTADO")+" · "+test+" · ELM "+(elm?"OK":"--")+" · "+proto+" · "+volts;}
    void refresh(){runOnUiThread(()->{if(statusStrip!=null)statusStrip.setBackgroundColor(bt?GREEN:RED);if(statusText!=null){statusText.setText(statusLine());statusText.setTextColor(bt?GREEN:RED);}if(liveBox!=null)liveBox.setText(live());if(testsBox!=null)testsBox.setText(tests());});}
    void space(int h){Space s=new Space(this);body.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}
    TextView section(String s){TextView v=txt(s,12,MUTED,false);v.setLetterSpacing(0.08f);v.setPadding(0,dp(10),0,dp(7));return v;}

    public void showHome(){
        base("Coding Lab E46",false,true,false,-1);

        FrameLayout hero=new FrameLayout(this); hero.setPadding(0,0,0,0);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(190)); hp.setMargins(0,0,0,dp(4)); hero.setLayoutParams(hp);
        ImageView car=new ImageView(this); car.setImageResource(getResources().getIdentifier("bmw_e46_black_coupe_hero","drawable",getPackageName())); car.setScaleType(ImageView.ScaleType.FIT_CENTER); car.setAlpha(0.98f);
        FrameLayout.LayoutParams cip=new FrameLayout.LayoutParams(dp(430),dp(185),Gravity.RIGHT|Gravity.CENTER_VERTICAL); hero.addView(car,cip);
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setGravity(Gravity.CENTER_VERTICAL); info.setPadding(0,0,0,0);
        info.addView(txt((bt?"●  Conectado":"●  Desconectado"),14,bt?GREEN:RED,true));
        info.addView(txt("BMW E46 320d M47N",15,TXT,false)); info.addView(txt("VIN: "+vin,12,Color.rgb(205,210,220),false));
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(210),-1,Gravity.LEFT|Gravity.CENTER_VERTICAL); hero.addView(info,ip);
        body.addView(hero);

        body.addView(section("MENÚ PRINCIPAL"));
        body.addView(menuRow("Coding Lab","Funciones de confort y personalización","⌕",BLUE,()->showCoding()));
        body.addView(menuRow("Luces","Iluminación exterior e interior","☼",YELLOW,()->showLights()));
        body.addView(menuRow("Ventanillas","Funciones de confort de ventanas","▭",GREEN,()->showCoding()));
        body.addView(menuRow("Confort","Cierre, apertura y funciones GM5","▣",PURPLE,()->showComfort()));
        body.addView(menuRow("LED / Check","Gestión de LED y testigos","◌",YELLOW,()->showLights()));
        body.addView(menuRow("Diagnóstico","Leer errores y estado de módulos","◎",GREEN,()->showDiag()));
        body.addView(menuRow("Información del coche","Detalles del vehículo y módulos","i",BLUE,()->showInfo()));
        body.addView(menuRow("Logs","Registros y sesiones guardadas","≡",PURPLE,()->showLogs()));
    }

    View menuRow(String title,String sub,String icon,int color,final Runnable click){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(5),dp(12),dp(5)); row.setBackground(bg(PANEL2,10,LINE));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(64)); lp.setMargins(0,0,0,dp(7)); row.setLayoutParams(lp);
        TextView ic=txt(icon,27,color,false); ic.setGravity(Gravity.CENTER); row.addView(ic,new LinearLayout.LayoutParams(dp(52),-1));
        LinearLayout texts=new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL); texts.setGravity(Gravity.CENTER_VERTICAL); texts.addView(txt(title,16,TXT,false)); texts.addView(txt(sub,12,Color.rgb(183,192,205),false)); row.addView(texts,new LinearLayout.LayoutParams(0,-1,1));
        TextView arrow=txt("›",31,Color.rgb(188,196,208),false); arrow.setGravity(Gravity.CENTER); row.addView(arrow,new LinearLayout.LayoutParams(dp(30),-1)); row.setOnClickListener(v->click.run()); return row;
    }

    void addBottomNav(int active){
        LinearLayout nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setPadding(dp(14),dp(3),dp(14),dp(4)); nav.setBackgroundColor(Color.rgb(2,7,13));
        String[] names={"⌂\nInicio","◇\nMódulos","≡\nLogs","⚙\nAjustes"}; Runnable[] runs={()->showHome(),()->showCoding(),()->showLogs(),()->showInfo()};
        for(int i=0;i<4;i++){final int idx=i;TextView v=txt(names[i],12,i==active?BLUE:MUTED,false);v.setGravity(Gravity.CENTER);v.setOnClickListener(x->runs[idx].run());nav.addView(v,new LinearLayout.LayoutParams(0,dp(54),1));}
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(62)));
    }

    void tabs(int active){
        LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); String[] a={"LUCES","VENTANILLAS","CONFORT","OTROS"};
        for(int i=0;i<4;i++){final int idx=i;TextView v=txt(a[i],12,i==active?BLUE:Color.rgb(215,218,225),true);v.setGravity(Gravity.CENTER);v.setOnClickListener(x->{if(idx==0)showLights();else if(idx==1)showCoding();else if(idx==2)showComfort();else showInfo();});r.addView(v,new LinearLayout.LayoutParams(0,dp(36),1));} body.addView(r);
        LinearLayout lines=new LinearLayout(this); lines.setOrientation(LinearLayout.HORIZONTAL); for(int i=0;i<4;i++){View line=new View(this); line.setBackgroundColor(i==active?BLUE:Color.TRANSPARENT); lines.addView(line,new LinearLayout.LayoutParams(0,dp(2),1));} body.addView(lines); space(9);
    }

    TextView infoCard(String s){TextView v=txt("ⓘ    "+s,13,Color.rgb(210,220,232),false);v.setPadding(dp(14),dp(10),dp(14),dp(10));v.setBackground(bg(PANEL2,8,LINE));return v;}

    public void showCoding(){base("Coding Lab",true,false,true,1);body.addView(infoCard("Personaliza el comportamiento de las ventanillas y\nfunciones de confort asociadas."));body.addView(section("CIERRE CON MANDO"));body.addView(group(new View[]{switchRow("Cerrar ventanillas con mantener pulsado","Cierra todas las ventanillas al mantener pulsado cerrar.",true),switchRow("Cerrar ventanillas traseras con mantener","Cierra solo las traseras al mantener pulsado cerrar.",true),switchRow("Doble clic para cerrar traseras","Doble clic en cerrar para subir traseras automáticamente.",true),switchRow("Doble clic para abrir traseras","Doble clic en abrir para bajar traseras automáticamente.",false)}));body.addView(section("APERTURA CON MANDO"));body.addView(group(new View[]{switchRow("Abrir ventanillas con mantener pulsado","Abre todas las ventanillas manteniendo abrir.",true),switchRow("Doble clic para abrir traseras","Doble clic en abrir para bajar traseras automáticamente.",true)}));body.addView(section("PRUEBAS"));body.addView(action("Backup seguro READ ONLY",()->safeBackup()));body.addView(action("Test completo automático",()->fullTest()));body.addView(action("Compartir sesión",()->share()));}
    public void showLights(){base("Luces",true,false,true,0);body.addView(infoCard("LED y check-control. Escritura bloqueada hasta backup real."));body.addView(section("LED / CHECK"));body.addView(group(new View[]{switchRow("Check frío posición LED","Parpadeo breve al contacto.",true),switchRow("Check caliente posición LED","Aviso de bombilla en cuadro.",true),switchRow("Confirmación al cerrar","Intermitentes al cerrar con mando.",true),switchRow("Confirmación al abrir","Intermitentes al abrir con mando.",false)}));body.addView(section("PRUEBAS"));body.addView(action("Leer DTC",()->{connect();initElm();runCmds(new String[]{"03","07"},"DTC");}));}
    public void showComfort(){base("Confort",true,false,true,2);body.addView(infoCard("Cierre centralizado, confirmaciones y funciones GM5. Modo visual/backup."));body.addView(group(new View[]{switchRow("Blink al cerrar","Confirmación visual al cerrar.",true),switchRow("Blink al abrir","Confirmación visual al abrir.",false),switchRow("Cierre selectivo","Bloqueado hasta backup GM5.",false)}));body.addView(action("Backup completo seguro",()->safeBackup()));}

    LinearLayout group(View[] rows){LinearLayout g=new LinearLayout(this);g.setOrientation(LinearLayout.VERTICAL);g.setBackground(bg(PANEL2,8,LINE));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));g.setLayoutParams(lp);for(View v:rows)g.addView(v);return g;}
    View switchRow(String title,String sub,boolean checked){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(14),dp(7),dp(10),dp(7));LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);texts.addView(txt(title,14,TXT,false));TextView desc=txt(sub,12,Color.rgb(188,198,210),false);desc.setMaxLines(2);texts.addView(desc);row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));Switch sw=new Switch(this);sw.setChecked(checked);tintSwitch(sw);sw.setOnCheckedChangeListener((button,isChecked)->add("SWITCH: "+title+" = "+(isChecked?"ON":"OFF")));row.addView(sw,new LinearLayout.LayoutParams(dp(62),dp(42)));return row;}
    void tintSwitch(Switch sw){if(Build.VERSION.SDK_INT>=21){int[][] states=new int[][]{new int[]{android.R.attr.state_checked},new int[]{-android.R.attr.state_checked}};sw.setThumbTintList(new ColorStateList(states,new int[]{Color.WHITE,Color.rgb(205,210,218)}));sw.setTrackTintList(new ColorStateList(states,new int[]{BLUE,Color.rgb(55,65,78)}));}}

    TextView action(String s,final Runnable r){TextView v=txt(s,14,TXT,true);v.setGravity(Gravity.CENTER);v.setPadding(dp(14),dp(10),dp(14),dp(10));v.setBackground(bg(PANEL2,10,LINE));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,0,0,dp(7));v.setLayoutParams(lp);v.setOnClickListener(x->new Thread(r).start());return v;}
    void addCodingButtons(){LinearLayout bar=new LinearLayout(this);bar.setPadding(dp(22),dp(8),dp(22),dp(12));bar.setBackgroundColor(Color.rgb(2,7,13));TextView reset=bottomButton("↻  RESTABLECER",false);TextView save=bottomButton("▣  GUARDAR CAMBIOS",true);reset.setOnClickListener(v->add("RESTABLECER: visual, sin escritura."));save.setOnClickListener(v->add("GUARDAR BLOQUEADO: falta backup real de módulo."));bar.addView(reset,new LinearLayout.LayoutParams(0,dp(52),1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.leftMargin=dp(12);bar.addView(save,lp);root.addView(bar,new LinearLayout.LayoutParams(-1,dp(74)));}
    TextView bottomButton(String s,boolean primary){TextView v=txt(s,14,primary?TXT:BLUE,true);v.setGravity(Gravity.CENTER);v.setBackground(bg(primary?BLUE:Color.TRANSPARENT,8,BLUE));return v;}

    public void showDiag(){base("Diagnóstico",true,false,false,-1);liveBox=smallCard(live());testsBox=smallCard(tests());body.addView(liveBox);body.addView(testsBox);body.addView(action("Conectar ELM327",()->connect()));body.addView(action("Inicializar ELM",()->initElm()));body.addView(action("Leer motor",()->readEngine()));body.addView(action("Leer DTC",()->runCmds(new String[]{"03","07"},"DTC")));body.addView(action("Backup seguro READ ONLY",()->safeBackup()));body.addView(action("Test completo automático",()->fullTest()));body.addView(action("Compartir sesión",()->share()));addLogBox();}
    public void showBackup(){base("Backup seguro",true,false,false,-1);body.addView(infoCard("Backup READ ONLY: protocolo, voltaje, PIDs, VIN si responde, DTC y RAW. No escribe módulos."));body.addView(action("Backup completo seguro",()->safeBackup()));body.addView(action("Backup identidad ECU/VIN",()->runCmds(new String[]{"0900","0902","0904","0906"},"BACKUP ECU ID")));body.addView(action("Backup capacidades OBD",()->runCmds(new String[]{"0100","0120","0140","0160"},"BACKUP PIDS")));body.addView(action("Backup fallos",()->runCmds(new String[]{"03","07","0A"},"BACKUP DTC")));body.addView(action("Compartir backup/sesión",()->share()));addLogBox();}
    public void showInfo(){base("Información",true,false,false,-1);body.addView(infoCard("BMW E46 320d/320Cd M47N\nVIN: "+vin+"\nELM327 v2.1\nModo seguro: solo lectura\nLSZ/GM5 escritura bloqueada hasta backup real."));body.addView(action("Compartir sesión",()->share()));}
    public void showLogs(){base("Logs",true,false,false,-1);body.addView(action("Compartir sesión completa",()->share()));body.addView(action("Añadir informe mecánico",()->add(report())));addLogBox();add("LOG READY. Usa Compartir sesión completa.");}
    TextView smallCard(String s){TextView v=txt(s,13,TXT,false);v.setPadding(dp(14),dp(10),dp(14),dp(10));v.setBackground(bg(PANEL2,10,LINE));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));v.setLayoutParams(lp);return v;}
    void addLogBox(){logBox=txt("",12,Color.rgb(220,228,240),false);logBox.setPadding(dp(14),dp(14),dp(14),dp(14));ScrollView sv=new ScrollView(this);sv.setBackground(bg(Color.rgb(5,11,19),10,LINE));sv.addView(logBox);body.addView(sv,new LinearLayout.LayoutParams(-1,dp(245)));}

    String live(){return "RPM  "+val(rpm,"rpm")+"      TEMP  "+val(temp,"°C")+"\nMAP  "+val(map,"kPa")+"      MAF  "+(maf<0?"--":String.format(Locale.US,"%.2f g/s",maf))+"\nVEL  "+val(speed,"km/h")+"      IAT  "+val(iat,"°C")+"\nDTC  "+dtc;}
    String val(int v,String unit){return v<0?"--":v+" "+unit;}
    String tests(){return (bt?"●":"○")+" Bluetooth    "+(elm?"●":"○")+" ELM    "+(protocol?"●":"○")+" Protocolo\n"+(engine?"●":"○")+" Motor/PIDs    "+(dtcOk?"●":"○")+" DTC    "+(backup?"●":"○")+" Backup";}
    void add(String s){session.append(s).append("\n\n");runOnUiThread(()->{if(logBox!=null)logBox.append(s+"\n\n");});}
    String report(){return "INFORME MECÁNICO\nVoltaje: "+volts+"\nProtocolo: "+proto+"\n"+live()+"\nConclusión: "+(dtc.contains("P0401")?"EGR anulada/desconectada o flujo insuficiente detectado.":"Sin DTC motor confirmado.");}

    void fullTest(){connect();initElm();readEngine();runCmds(new String[]{"03","07"},"DTC");runCmds(new String[]{"ATDP","ATDPN","0100"},"PROTOCOLO");add(report());}
    void safeBackup(){connect();initElm();add("===== BACKUP SEGURO READ ONLY =====");runCmds(new String[]{"ATI","ATRV","ATDP","ATDPN","0100","0120","0140","0900","0902","0904","03","07","0A"},"BACKUP SEGURO");backup=true;add("BACKUP OK: sesión RAW lista para exportar. No se ha escrito nada.");add(report());refresh();}
    void share(){try{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"BMW E46 Scanner Session");i.putExtra(Intent.EXTRA_TEXT,session.toString());startActivity(Intent.createChooser(i,"Enviar sesión"));}catch(Exception e){add("ERROR SHARE: "+e.getMessage());}}

    boolean perm(){if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){runOnUiThread(()->requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},46));return false;}return true;}
    void connect(){try{test="Conectando BT";refresh();if(!perm())return;BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();if(adapter==null){add("Sin Bluetooth");return;}Set<BluetoothDevice> bonded=adapter.getBondedDevices();device=null;for(BluetoothDevice d:bonded){String name=d.getName()==null?"":d.getName().toLowerCase();if(device==null||name.contains("obd")||name.contains("elm")||name.contains("vlink")||name.contains("icar"))device=d;}if(device==null){add("Empareja ELM");return;}close();socket=device.createRfcommSocketToServiceRecord(SPP);socket.connect();in=socket.getInputStream();out=socket.getOutputStream();bt=true;test="BT OK";add("BT OK: "+device.getName()+" / "+device.getAddress());refresh();}catch(Exception e){bt=false;add("ERROR CONEXION: "+e.getClass().getSimpleName()+" - "+e.getMessage());close();refresh();}}
    void close(){try{if(socket!=null)socket.close();}catch(Exception ignored){}socket=null;in=null;out=null;}
    boolean ready(){if(socket==null||!socket.isConnected()||in==null||out==null){add("Pulsa conectar primero");return false;}return true;}
    void initElm(){runCmds(new String[]{"ATZ","ATE0","ATL0","ATS0","ATH1","ATI","ATRV","ATSP0","ATDP","ATDPN"},"INIT ELM");elm=true;refresh();}
    void readEngine(){runCmds(new String[]{"0120","010C","010C","0105","010D","010B","010F","0110"},"MOTOR");engine=true;refresh();}
    void runCmds(String[] commands,String name){try{if(!ready())return;test=name;refresh();add("===== "+name+" =====");for(String c:commands)send(c);if(name.contains("DTC"))dtcOk=true;if(name.contains("PROTOCOLO"))protocol=true;test=name+" terminado";refresh();}catch(Exception e){add("ERROR "+name+": "+e.getClass().getSimpleName()+" - "+e.getMessage());close();refresh();}}
    void send(String c)throws Exception{out.write((c+"\r").getBytes("US-ASCII"));out.flush();Thread.sleep(c.equals("ATZ")?1700:1000);String r=read();add("> "+c+"\n"+r);parse(c,r);refresh();}
    String read()throws Exception{byte[] buffer=new byte[512];StringBuilder s=new StringBuilder();long end=System.currentTimeMillis()+1600;while(System.currentTimeMillis()<end){while(in.available()>0){int n=in.read(buffer);if(n>0)s.append(new String(buffer,0,n,"US-ASCII"));}if(s.toString().contains(">"))break;Thread.sleep(60);}String r=s.toString().replace('\r',' ').replace('\n',' ').trim();return r.length()==0?"SIN RESPUESTA":r;}
    void parse(String cmd,String r){try{if(cmd.equals("ATRV"))volts=r.replace(">","").trim();if(cmd.equals("ATDP"))proto=r.replace(">","").trim();String h=r.replace(" ","").replace(">","");int i;if((i=h.indexOf("410C"))>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16);rpm=((a*256)+b)/4;}if((i=h.indexOf("4105"))>=0)temp=Integer.parseInt(h.substring(i+4,i+6),16)-40;if((i=h.indexOf("410D"))>=0)speed=Integer.parseInt(h.substring(i+4,i+6),16);if((i=h.indexOf("410B"))>=0)map=Integer.parseInt(h.substring(i+4,i+6),16);if((i=h.indexOf("410F"))>=0)iat=Integer.parseInt(h.substring(i+4,i+6),16)-40;if((i=h.indexOf("4110"))>=0){int a=Integer.parseInt(h.substring(i+4,i+6),16),b=Integer.parseInt(h.substring(i+6,i+8),16);maf=((a*256)+b)/100.0;}if(h.contains("430401"))dtc="P0401 EGR insuficiente";else if(h.contains("43"))dtc="DTC RAW";}catch(Exception ignored){}}
}
