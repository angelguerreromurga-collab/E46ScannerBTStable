package com.angel.e46scannerbt;

public class ObdParserSelfTest {
    public static String run() {
        ObdSnapshot s = new ObdSnapshot();
        ObdParser.parseInto(s, "486B12410C000012");
        ObdParser.parseInto(s, "486B1241055964");
        ObdParser.parseInto(s, "486B12410D0013");
        ObdParser.parseInto(s, "486B12410B6273");
        ObdParser.parseInto(s, "486B12410F5469");
        ObdParser.parseInto(s, "486B124110007F95");
        ObdParser.parseInto(s, "486B12430401000000000D");

        StringBuilder out = new StringBuilder();
        out.append("OBD PARSER SELF TEST\n");
        out.append(check("RPM", s.rpm == 0, "expected 0, got " + s.rpm));
        out.append(check("TEMP", s.coolantC == 49, "expected 49, got " + s.coolantC));
        out.append(check("VEL", s.speedKmh == 19, "expected 19, got " + s.speedKmh));
        out.append(check("MAP", s.mapKpa == 98, "expected 98, got " + s.mapKpa));
        out.append(check("IAT", s.iatC == 44, "expected 44, got " + s.iatC));
        out.append(check("MAF", s.mafGps > 1.26 && s.mafGps < 1.28, "expected 1.27, got " + s.mafGps));
        out.append(check("DTC", s.dtc.contains("P0401"), "expected P0401, got " + s.dtc));
        out.append("\n").append(s.liveText());
        return out.toString();
    }

    private static String check(String name, boolean ok, String detail) {
        return (ok ? "OK " : "FAIL ") + name + " - " + detail + "\n";
    }
}
