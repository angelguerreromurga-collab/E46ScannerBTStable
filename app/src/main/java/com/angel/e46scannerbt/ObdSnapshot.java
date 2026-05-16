package com.angel.e46scannerbt;

import java.util.Locale;

public class ObdSnapshot {
    public int rpm = -1;
    public int coolantC = Integer.MIN_VALUE;
    public int speedKmh = -1;
    public int mapKpa = -1;
    public int iatC = Integer.MIN_VALUE;
    public double mafGps = -1;
    public String dtc = "--";
    public String voltage = "--";
    public String protocol = "--";

    public String liveText() {
        return "RPM  " + intVal(rpm, "rpm") + "     TEMP  " + tempVal(coolantC) + "\n" +
                "MAP  " + intVal(mapKpa, "kPa") + "     MAF  " + mafVal() + "\n" +
                "VEL  " + intVal(speedKmh, "km/h") + "     IAT  " + tempVal(iatC) + "\n" +
                "DTC  " + dtc;
    }

    private String intVal(int v, String unit) {
        return v < 0 ? "--" : v + " " + unit;
    }

    private String tempVal(int v) {
        return v == Integer.MIN_VALUE ? "--" : v + " °C";
    }

    private String mafVal() {
        return mafGps < 0 ? "--" : String.format(Locale.US, "%.2f g/s", mafGps);
    }
}
