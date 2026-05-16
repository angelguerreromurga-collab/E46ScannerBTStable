package com.angel.e46scannerbt;

public class ObdParser {
    public static void parseInto(ObdSnapshot s, String raw) {
        if (s == null || raw == null) return;
        String compact = raw.replace(" ", "").replace(">", "").replace("\r", "").replace("\n", "").toUpperCase();
        parsePidValues(s, compact);
        parseMeta(s, raw);
        parseDtc(s, compact);
    }

    private static void parseMeta(ObdSnapshot s, String raw) {
        String upper = raw.toUpperCase();
        int v = upper.indexOf("V");
        if (v > 0) {
            int start = Math.max(0, v - 6);
            String chunk = upper.substring(start, v + 1).replace(">", "").trim();
            if (chunk.matches(".*[0-9]{2}\\.[0-9]V.*")) s.voltage = chunk;
        }
        if (upper.contains("ISO 9141") || upper.contains("KWP")) {
            int p = upper.indexOf("ISO");
            if (p < 0) p = upper.indexOf("KWP");
            int end = Math.min(upper.length(), p + 32);
            s.protocol = upper.substring(p, end).replace(">", "").trim();
        }
    }

    private static void parsePidValues(ObdSnapshot s, String h) {
        int i;
        if ((i = h.indexOf("410C")) >= 0 && has(h, i, 8)) {
            int a = hex(h, i + 4), b = hex(h, i + 6);
            if (a >= 0 && b >= 0) s.rpm = ((a * 256) + b) / 4;
        }
        if ((i = h.indexOf("4105")) >= 0 && has(h, i, 6)) {
            int a = hex(h, i + 4);
            if (a >= 0) s.coolantC = a - 40;
        }
        if ((i = h.indexOf("410D")) >= 0 && has(h, i, 6)) {
            int a = hex(h, i + 4);
            if (a >= 0) s.speedKmh = a;
        }
        if ((i = h.indexOf("410B")) >= 0 && has(h, i, 6)) {
            int a = hex(h, i + 4);
            if (a >= 0) s.mapKpa = a;
        }
        if ((i = h.indexOf("410F")) >= 0 && has(h, i, 6)) {
            int a = hex(h, i + 4);
            if (a >= 0) s.iatC = a - 40;
        }
        if ((i = h.indexOf("4110")) >= 0 && has(h, i, 8)) {
            int a = hex(h, i + 4), b = hex(h, i + 6);
            if (a >= 0 && b >= 0) s.mafGps = ((a * 256) + b) / 100.0;
        }
    }

    private static void parseDtc(ObdSnapshot s, String h) {
        if (h.contains("430401")) s.dtc = "P0401 EGR insuficiente";
        else if (h.contains("470401")) s.dtc = "P0401 pendiente";
        else if (h.contains("430000000000") || h.contains("470000000000")) s.dtc = "Sin DTC confirmados";
        else if (h.contains("43") || h.contains("47")) s.dtc = "DTC RAW presente";
    }

    private static boolean has(String h, int i, int len) {
        return i >= 0 && h.length() >= i + len;
    }

    private static int hex(String h, int pos) {
        try { return Integer.parseInt(h.substring(pos, pos + 2), 16); }
        catch (Exception e) { return -1; }
    }
}
